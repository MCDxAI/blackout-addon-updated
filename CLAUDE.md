# CLAUDE.md

Guidance for Claude Code working in this repository.

## Project Purpose

Autonomously maintain **BlackOut** — a Meteor Client Minecraft addon, ported to Meteor 26.1.X and updated through **Meteor 26.2 / Minecraft 26.2**.

- The original BlackOut (`kassuk/blackout`) is abandoned and targets old Minecraft/Meteor versions.
- Reference fork: <https://github.com/Marccccccccccccccc/BlackOut> — updated to MC 1.21.11, then further along its `main` branch it begins a **26.1.X port** (HEAD commit: *"start porting to 26.1.X"*).
- This workspace is where the actual porting work happens. The fork is treated as a reference/starting point.

## Target Versions (from reference fork's `gradle/libs.versions.toml`)

| Component        | Version            |
| ---------------- | ------------------ |
| Minecraft        | `26.2`             |
| Meteor Client    | `26.2-SNAPSHOT`    |
| Mappings         | n/a (mojmap)      |
| Fabric Loader    | `0.19.3`           |
| Fabric Loom      | `1.17-SNAPSHOT`    |
| JDK              | `25`               |
| Mod version      | `2.1.0`            |

Note: Meteor's new versioning scheme is `YY.D` (year.drop). The build script converts `26.2` → compatibility string `~26.2` for `fabric.mod.json`.

Note: MC 26.1.x ships **de-obfuscated** with Mojang official names, so the build uses **no Loom mappings line** and the source, mixins, and `blackout.accesswidener` (`v2 official` header) are all written in **mojmap** — this project is *not* yarn-mapped. See `build.gradle.kts` (lines ~25-29) for the details.

## Workspace Layout

```
.
├── .pi/
│   ├── agents/              # port-engineer, mixin-mapping-specialist, code-reviewer
│   ├── skills/              # 8 project skills (meteor-addon, minecraft-dev-mcp, …)
│   ├── mcp.json             # minecraft-dev MCP server config
│   └── bootstrap.json       # /bootstrap manifest (installed skills + generated agents)
├── references/              # 15 shallow third-party addon clones (gitignored; reference-only)
├── src/                      # BlackOut addon sources (we work directly on these)
├── build.gradle.kts          # Fabric Loom build (JDK 25, version catalog)
├── gradle/libs.versions.toml # pinned versions: MC 26.2, Meteor 26.2-SNAPSHOT
├── gradlew / gradlew.bat     # build wrapper
└── CLAUDE.md
```

The workspace root **is** the fork's git repo (HEAD: `b94d515` *"start porting to 26.1.X"*). It is now hosted at **`MCDxAI/blackout-addon-updated`** (private); the original reference fork `Marccccccccccccccc/BlackOut` is retained as the `upstream` remote. We work directly on top of the existing fork as-is rather than scaffolding a fresh project. The fork was last updated May 1st; this port picks up from there.

## Git Workflow

- **Repository:** `MCDxAI/blackout-addon-updated` (private). The reference fork `Marccccccccccccccc/BlackOut` is kept as the `upstream` remote.
- **Fully autonomous mode.** Do **not** use the harness's built-in git/commit tooling (`enable_git_tools` → `propose_commit` / `git_commit`). It gates behind an interactive approval TUI that blocks the agent turn. Instead drive git directly from the shell and **push to `origin` directly** — this authorization is intentional and is the standard operating mode for this repo.
- Use `gh` for GitHub operations (repo settings, issues, PRs, releases, secrets); use plain `git` for commits, branches, and pushes.
- `references/` holds shallow clones of third-party addons for porting reference only — it is **gitignored** and never committed.
- The full automation config lives under `.pi/` (agents, skills, `mcp.json`, `bootstrap.json`) and **is** committed so the setup is reproducible.

## Build Commands

Standard Fabric/Loom Gradle wrapper (Windows):

```bash
./gradlew build          # full build
./gradlew runClient      # launch dev client with the addon loaded
./gradlew genSources     # generate decompiled MC sources for reference
./gradlew shadowJar      # (if configured) produce a distributable jar
```

Use `gradlew.bat` under `cmd.exe` if the bash wrapper misbehaves on Windows.

## MCP Tooling

A **minecraft-dev** MCP server is registered in `.pi/mcp.json`. It runs the built entry point of <https://github.com/MCDxAI/minecraft-dev-mcp> and provides tools to **decompile, remap, and explore Minecraft source** across versions.

Use it to:
- Compare class/method/field names and signatures between MC 1.21.11 (what the reference fork targets) and 26.1.x.
- Look up Yarn-mapped Minecraft internals when porting mixin/rendering/packet code.
- Resolve mapping renames that break compilation during the port.

If the `minecraft-dev` tools are not visible in the current session, the server needs a session reload to pick up `.pi/mcp.json`.

## Porting Notes

- The build is already modernized (JDK 25, version catalog, new Meteor `YY.D` version-compat function), but the source under `src/` may only be **partially** ported. First task: audit how far `src` actually compiles against 26.1.x before doing further work.
- Watch for Meteor API breakages between the 1.21.11-era and 26.1.x snapshots (event system, render pipelines, module/category registration, settings API).
- BlackOut ships an access widener: `src/main/resources/blackout.accesswidener` — confirm it still resolves against 26.1.x mappings.
