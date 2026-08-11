---
name: reference-addons
description: "Locate and consult the reference Meteor Client addons when porting BlackOut or building any Meteor 26.1.x addon code. Use this to find real, working examples of Meteor 26.1.x API usage instead of guessing at renamed APIs. Covers two locations: third-party shallow clones in references/ (6Bees, Meteorist, Numby-hack, Trouser-Streak, catppuccin-addon, Seija-Printer, glazed, etc.) and the three locally-authored com.cope.* MCDxAI addons (meteor-mcp, meteor-webgui, meteor-addons). Use when you need examples of modules (extends Module, @Category), settings (IntSetting, EnumSetting, DoubleSetting, SettingGroup), events (@EventHandler, EVENT_BUS), mixins (@Mixin, @Inject, @ModifyArgs, @Redirect), packets (PacketEvent, PlayerMoveC2SPacket), or GUI rendering. Includes version caveats: 26.1.2-matching refs vs 26.2-ahead (MeteorPlus, Nora-Tweaks) vs stale 1.21.11 (glazed)."
metadata:
  project: blackout-addon
  target: "Minecraft 26.1.2 / Meteor 26.1.2-SNAPSHOT"
  verified: "2026-08-10"
---

# Reference Meteor Addons

When porting **BlackOut** or writing any **Meteor 26.1.x** addon code, consult the reference addons below before guessing at renamed APIs. They are real, buildable Meteor 26.1.x addons you can grep and read.

Use this skill when you need to declare a Module or HUD, add a Setting, subscribe to an event, write a Mixin, send/intercept a packet, or render on-screen — and you want to see how it's actually done against the current Meteor API.

## ⚠️ Critical version gotchas (read first)

Our target: **Minecraft 26.1.2 / Meteor 26.1.2-SNAPSHOT / Yarn 1.21.11+build.3 / Fabric Loader 0.19.2 / JDK 25**.

| Status | Addons | Meaning |
|---|---|---|
| ✅ Matches 26.1.2 | 6Bees, Baritone-Controller, Exodar-Addon, HIGTools, Meteorist, Numby-hack, PowHax, Seija-Printer, Trouser-Streak, catppuccin-addon, mc-games, meteor-litematica-printer | Safe to copy API usage directly. |
| ⚠️ AHEAD (26.2) | MeteorPlus (mixed 26.2 MC / 26.1.2-SNAPSHOT meteor), Nora-Tweaks (26.2) | Forward reference only. An API they use may not exist yet in 26.1.2 — re-verify. |
| ❌ STALE (1.21.11) | glazed | Not a 26.1.x ref. Use for intent only, then re-resolve every name via the minecraft-dev MCP. |
| 🔧 Code-quality caveat | Trouser-Streak | Rough code ("shitty code" per the user) — API-usage example only. Do **not** copy its structure/style. |

Other traps:

- **minecraft-dev MCP is the source of truth for names.** When an API name/signature is uncertain, cross-check with the `minecraft-dev` MCP server (configured in `.pi/mcp.json`) instead of trusting any single reference. It decompiles/remaps Minecraft source across versions and resolves renames between 1.21.11 and 26.1.x.
- **Other sibling folders are off-limits.** The parent dir `C:\Users\coper\Documents\GitHub\1meteor-addons-etc\` contains other folders that are OLD/OUTDATED. Consult **only** the three `com.cope.*` projects named below — not the others.

## Where the references live

### 1. Third-party clones — `references/` at the workspace root
`C:\Users\coper\Documents\GitHub\1meteor-addons-etc\blackout-addon-updated\references`

Each subdirectory is a **shallow `git clone --depth 1`** of a third-party Meteor addon. Read-only references — do not edit them.

For the full per-addon table (**repo, mod id, entry class, exact MC/Meteor/loader versions, what it does, porting caveats**), read **[`references/third-party-addons.md`](references/third-party-addons.md)** before picking one to copy from.

Quick pick by need:

- Clean general-purpose structure → **Meteorist** (or any `com.cope.*` below)
- GUI theming / settings styling → **catppuccin-addon**
- HUD + anarchy modules → **Numby-hack**, **6Bees**, **PowHax**
- Mixins-heavy block/fluid placement → **Seija-Printer**, **meteor-litematica-printer** (compare both)
- On-screen GUI / minigames / screen rendering → **mc-games**, **Baritone-Controller**
- Large module collections (forward-ref only, ahead of target) → **MeteorPlus**, **Nora-Tweaks**
- Intent-only (stale) → **glazed**

### 2. Locally-authored MCDxAI addons (`com.cope.*`)
Parent: `C:\Users\coper\Documents\GitHub\1meteor-addons-etc\` (consult **only** these three folders).

| Folder | modid | Entry class | What |
|---|---|---|---|
| `meteor-mcp-addon` | `meteor-mcp` | `com.cope.meteormcp.MeteorMCPAddon` | Model Context Protocol + Gemini integration for Meteor. Ships AGENTS.md / CLAUDE.md / GEMINI.md and an `ai_reference/ai_docs` dir. |
| `meteor-client-webgui` | `meteor-webgui` | `com.cope.meteorwebgui.MeteorWebGUIAddon` | Real-time bi-directional web interface for every Meteor module/setting (includes `webui/` + `scripts/`). Status: **Preview**. |
| `meteor-addons-addon` | `meteor-addons` | `com.cope.meteoraddons.MeteorAddonsAddon` | Browse, install, and update Meteor addons from inside Minecraft. |

All three target **26.1.2 / Meteor 26.1.2-SNAPSHOT / Fabric 0.19.2 / Java 25**, packages under `com.cope.*`. These and **Meteorist** are the highest-quality references — prefer them for idiomatic structure.

## How to search the references (concrete greps)

Run against `C:\Users\coper\Documents\GitHub\1meteor-addons-etc\blackout-addon-updated\references` and the three `com.cope.*` folders.

| Looking for | Grep for |
|---|---|
| Module declaration | `extends Module`, `getCategory()`, `@Category` |
| Setting types | `new IntSetting`, `new EnumSetting`, `new DoubleSetting`, `SettingGroup` |
| Events / handlers | `@EventHandler`, `event.`, `MeteorClient.EVENT_BUS` |
| Mixins | `@Mixin`, `@Inject`, `@ModifyArgs`, `@Redirect` |
| Packets | `PacketEvent`, `PlayerMoveC2SPacket`, `impl(` |

**Cross-check rule:** if a grep returns hits *only* in glazed / MeteorPlus / Nora-Tweaks, treat the result as suspect (stale or ahead-of-target) and re-resolve the name against a 26.1.2-matching addon or the minecraft-dev MCP before using it.
