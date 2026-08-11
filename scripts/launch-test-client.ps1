<#
.SYNOPSIS
  Autonomously launch a Minecraft 26.1.2 dev client loaded with Meteor Client,
  the BlackOut addon, and the MC Test Harness (meteor variant), so the harness'
  in-game MCP server comes up and the agent can drive the game.

.DESCRIPTION
  Pipeline:
    1. Build the test-harness jar (meteor variant) if not -SkipBuild.
    2. Stage it into the BlackOut runClient mods folder (blackout\run\mods).
    3. Launch BlackOut's `runClient` detached (Meteor + BlackOut + harness all load).
    4. Poll the harness MCP HTTP endpoint until it responds (game booted).
    5. Print connection info (server name for .pi/mcp.json, endpoint, log path).

  RunClient is launched DETACHED so the client keeps running after this script
  exits. Pair with stop-test-client.ps1 to tear it down.

  Gradle always runs with --no-daemon to avoid orphaned daemon processes.

.PARAMETER Variant
  meteor (default, port 38861) -- controls Meteor/BlackOut modules. Use this to
                                  verify BlackOut.
  universal (port 38862)        -- engine-agnostic DOM testing, no Meteor module CRUD.

.PARAMETER SkipBuild
  Reuse the already-built harness jar (skip the gradle build). Speeds up re-launches.

.PARAMETER NoLaunch
  Build + stage only; do not launch the client. Useful to prepare the mods folder.

.PARAMETER BootTimeoutSec
  How long to wait for the MCP endpoint to come up (default 240s; first boot may
  need longer if MC assets cache is cold).

.EXAMPLE
  .\scripts\launch-test-client.ps1
  .\scripts\launch-test-client.ps1 -SkipBuild
  .\scripts\launch-test-client.ps1 -Variant universal
#>
[CmdletBinding()]
param(
    [ValidateSet("meteor", "universal")]
    [string]$Variant = "meteor",

    [string]$BlackoutDir = "C:\Users\coper\Documents\GitHub\1meteor-addons-etc\blackout-addon-updated",
    [string]$HarnessDir  = "C:\Users\coper\Documents\GitHub\1meteor-addons-etc\meteor-test-harness",

    [switch]$SkipBuild,
    [switch]$NoLaunch,
    [int]$BootTimeoutSec = 240
)
$ErrorActionPreference = "Stop"

# --- variant config -----------------------------------------------------------
$variantConfig = @{
    meteor    = @{ GradleTask = ":meteor-addon:build"; SubDir = "meteor-addon"; JarGlob = "mc-test-harness-meteor-*.jar"; Port = 38861; ServerName = "meteor-harness" }
    universal = @{ GradleTask = ":universal:build";     SubDir = "universal";     JarGlob = "mc-test-harness-universal-*.jar"; Port = 38862; ServerName = "mc-test-harness-universal" }
}[$Variant]
$port        = $variantConfig.Port
$serverName  = $variantConfig.ServerName
$endpoint    = "http://127.0.0.1:$port/mcp"
$modsDir     = Join-Path $BlackoutDir "run\mods"
$harnessSub  = Join-Path $HarnessDir $variantConfig.SubDir
$logPath     = Join-Path $BlackoutDir "_runclient_$Variant.log"

function Write-Step($msg) { Write-Host "[launch] $msg" -ForegroundColor Cyan }

# --- 1. build harness jar -----------------------------------------------------
if (-not $SkipBuild) {
    Write-Step "Building harness jar ($($variantConfig.GradleTask)) ..."
    Push-Location $HarnessDir
    try {
        & (Join-Path $HarnessDir "gradlew.bat") $variantConfig.GradleTask --console=plain --no-daemon
        if ($LASTEXITCODE -ne 0) { throw "Harness build failed (exit $LASTEXITCODE)." }
    }
    finally { Pop-Location }
} else {
    Write-Step "SkipBuild set -- reusing existing harness jar."
}

# --- 2. stage jar into blackout runClient mods --------------------------------
$jar = Get-ChildItem -Path (Join-Path $harnessSub "build\libs") -Filter $variantConfig.JarGlob -File |
    Sort-Object LastWriteTime -Descending | Select-Object -First 1
if (-not $jar) { throw "Harness jar not found: $($variantConfig.JarGlob) under $harnessSub\build\libs. Build it first." }

if (-not (Test-Path $modsDir)) { New-Item -ItemType Directory -Path $modsDir -Force | Out-Null }
$dest = Join-Path $modsDir $jar.Name

# Refresh: remove any prior harness jar of this variant, then copy the fresh one.
Get-ChildItem $modsDir -Filter ($variantConfig.JarGlob -replace '\*','*') -File -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -like ($variantConfig.JarGlob) } | Remove-Item -Force -ErrorAction SilentlyContinue
Copy-Item $jar.FullName $dest -Force
Write-Step "Staged $($jar.Name) -> $dest"

if ($NoLaunch) { Write-Step "NoLaunch set -- staged only. Exiting."; return }

# --- 3. launch blackout runClient detached ------------------------------------
Write-Step "Launching BlackOut runClient (MC 26.1.2 + Meteor + BlackOut + harness) ..."
# Start-Process so the client survives this script exiting.
# NOTE: invoke the wrapper by FULL PATH. Agent/CI shells often set
# NoDefaultCurrentDirectoryInExePath=1, which makes cmd.exe refuse to resolve a
# bare `gradlew.bat` from the current directory ("is not recognized as an
# internal or external command") even right after `cd /d` into the project.
$gradlew = Join-Path $BlackoutDir "gradlew.bat"
if (-not (Test-Path $gradlew)) { throw "Gradle wrapper not found: $gradlew" }
Start-Process -FilePath "cmd.exe" `
    -ArgumentList "/c","cd /d `"$BlackoutDir`" && `"$gradlew`" runClient --console=plain --no-daemon > `"$logPath`" 2>&1" `
    -WindowStyle Hidden -PassThru | Out-Null
Write-Step "Client launching. Log: $logPath"

# --- 4. poll the MCP endpoint -------------------------------------------------
Write-Step "Waiting for harness MCP endpoint $endpoint (timeout ${BootTimeoutSec}s) ..."
$deadline = (Get-Date).AddSeconds($BootTimeoutSec)
$up = $false
while ((Get-Date) -lt $deadline) {
    Start-Sleep -Seconds 3
    try {
        # Any HTTP response (even 400/405/406) means the Tomcat server is up.
        # Connection-refused throws with a null Response.
        $null = Invoke-WebRequest -Uri $endpoint -Method Post -TimeoutSec 3 -UseBasicParsing -ErrorAction Stop
        $up = $true; break
    } catch [System.Net.WebException] {
        if ($_.Exception.Response -ne $null) { $up = $true; break }
        # else: connection refused -- keep polling
    } catch {
        # newer PS: Microsoft.PowerShell.Commands.HttpResponseException has .Response
        if ($_.Exception.Response -ne $null) { $up = $true; break }
    }
}

# --- 5. report ----------------------------------------------------------------
if ($up) {
    Write-Host ""
    Write-Host "=== TEST CLIENT READY ===" -ForegroundColor Green
    Write-Host "MCP endpoint : $endpoint"
    Write-Host "Server name  : $serverName   (add to .pi/mcp.json if not present)"
    Write-Host "Variant      : $Variant  (mods: BlackOut + Meteor + $($jar.Name))"
    Write-Host "Log          : $logPath"
    Write-Host "Stop with    : .\scripts\stop-test-client.ps1"
} else {
    Write-Host ""
    Write-Host "=== MCP ENDPOINT DID NOT COME UP within ${BootTimeoutSec}s ===" -ForegroundColor Yellow
    Write-Host "Check the log for a mixin/crash: $logPath"
    Write-Host "(The client may still be booting -- re-run the poll or raise -BootTimeoutSec.)"
}
