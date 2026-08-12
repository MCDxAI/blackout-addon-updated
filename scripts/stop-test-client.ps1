<#
.SYNOPSIS
  Stop the autonomously-launched Minecraft test client (and the test-harness MCP
  server inside it), plus any lingering single-use Gradle wrapper process.

.DESCRIPTION
  Kills the Java process running the dev client (matched by the BlackOut run dir
  / KnotClient / Minecraft in its command line) and the detached gradlew wrapper.
  Safe to run when nothing is up (no-op). Does NOT kill unrelated Java processes.

.PARAMETER BlackoutDir
  The BlackOut workspace whose runClient is the home client. Used to scope the
  process match so only this project's dev client is targeted.

.EXAMPLE
  .\scripts\stop-test-client.ps1
#>
[CmdletBinding()]
param(
    [string]$BlackoutDir = "C:\Users\coper\Documents\GitHub\1meteor-addons-etc\blackout-addon-updated"
)
$ErrorActionPreference = "Continue"

$runDir = (Join-Path $BlackoutDir "run")
# Match the MC client JVM (KnotClient/Minecraft + the run dir) and the detached gradlew
# wrapper that launched it. Do not match unrelated Java processes (IDEs, etc.).
$pattern = "(?:KnotClient|Minecraft\.client\.main|net\.minecraft|gradlew\.bat.*runClient|$([regex]::Escape($runDir)))"

$procs = Get-CimInstance Win32_Process -Filter "Name = 'java.exe' OR Name = 'javaw.exe' OR Name = 'cmd.exe'" |
    Where-Object { $_.CommandLine -and $_.CommandLine -match $pattern }

if (-not $procs) {
    Write-Host "[stop] No matching test-client process found. Already stopped." -ForegroundColor DarkGray
    return
}

foreach ($p in $procs) {
    try {
        Write-Host "[stop] Killing PID $($p.ProcessId): $(($p.CommandLine -split '\s+')[0..3] -join ' ')" -ForegroundColor Yellow
        Stop-Process -Id $p.ProcessId -Force -ErrorAction Stop
    } catch {
        Write-Host "[stop] Could not kill PID $($p.ProcessId): $($_.Exception.Message)" -ForegroundColor DarkGray
    }
}

# Best-effort cleanup of any single-use Gradle daemon this project spawned.
Get-CimInstance Win32_Process -Filter "Name = 'java.exe'" |
    Where-Object { $_.CommandLine -and $_.CommandLine -match "GradleDaemon.*$([regex]::Escape($BlackoutDir))" } |
    ForEach-Object { try { Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue } catch {} }

Write-Host "[stop] Done." -ForegroundColor Green
