# Test Harness — Autonomous In-Game Verification

The **MC Test Harness** (`C:\Users\coper\Documents\GitHub\1meteor-addons-etc\meteor-test-harness`)
is a Meteor Client addon that embeds an **MCP server inside the running Minecraft client**. Once
loaded, it exposes the game (screens, modules, world state, chat, pathing) as MCP tools over HTTP,
letting this agent drive a live client autonomously — click GUI elements, toggle Meteor/BlackOut
modules, read the player state, send commands — without a human at the keyboard.

This is how interactive/runtime verification of the BlackOut port is done headlessly.

---

## Variants — use **Meteor** for BlackOut

The harness ships two Fabric mods:

| Variant | Mod ID | Port | MCP endpoint | Use it to |
| --- | --- | --- | --- | --- |
| **Meteor** | `mc-test-harness-meteor` | **38861** | `http://127.0.0.1:38861/mcp` | **Verify BlackOut** — full Meteor module CRUD, HUD, Baritone pathing, DOM |
| Universal | `mc-test-harness-universal` | 38862 | `http://127.0.0.1:38862/mcp` | Engine-agnostic DOM/GUI testing, **no Meteor module control** |

For BlackOut work always launch the **Meteor** variant (it can list/toggle/configure Meteor modules,
which is what BlackOut's modules are). The launcher defaults to it.

> MCP server name in `.pi/mcp.json`: **`meteor-harness`** (matches the harness' `live-tester` agent
> convention — pass `server: "meteor-harness"` on every call).

---

## Launching the client autonomously

`scripts/launch-test-client.ps1` does the whole pipeline:

1. Builds the harness meteor jar (`:meteor-addon:build`).
2. Stages it into `run\mods\` (BlackOut's `runClient` loads mods from there).
3. Launches BlackOut's `runClient` **detached** → MC 26.1.2 + Meteor + BlackOut + harness all load.
4. Polls the MCP endpoint until the harness is up, then prints connection info.

```powershell
# From the blackout workspace root:
.\scripts\launch-test-client.ps1                 # build + stage + launch + wait for MCP
.\scripts\launch-test-client.ps1 -SkipBuild      # reuse the already-built harness jar (faster relaunch)
.\scripts\launch-test-client.ps1 -NoLaunch        # build + stage only, don't start the client
.\scripts\launch-test-client.ps1 -Variant universal   # (rare) launch the universal variant instead
```

**What loads:** MC 26.1.2 (mojmap, unobfuscated) · Fabric Loader 0.19.2 · Meteor Client
26.1.2-SNAPSHOT · BlackOut 0.67.0 · MC Test Harness (meteor). JDK 25 throughout.

**Gradle always runs `--no-daemon`** (orphaned-daemon hazard on this machine).

When the script prints `=== TEST CLIENT READY ===`, the MCP server is live. The client keeps running
after the script exits (it was launched detached). Stop it with:

```powershell
.\scripts\stop-test-client.ps1
```

> The first launch after a `clean` may take a couple minutes (MC assets). Subsequent boots are faster.

---

## Connecting

`.pi/mcp.json` already declares the server:

```json
"meteor-harness": { "type": "http", "url": "http://127.0.0.1:38861/mcp" }
```

After **reloading the harness** (so pi picks up the mcp.json entry), the server appears in the MCP
gateway. Because the server lives *inside the game*, it is only reachable while the client is up —
verify connectivity first:

```
mcp({ connect: "meteor-harness" })      # try to (re)connect
mcp({ server: "meteor-harness" })        # list the harness' tools
```

Then call tools with `server: "meteor-harness"`:

```javascript
// via mcp_execute:
const mods = await mcp.call("module_list", {}, "meteor-harness");
```

> **Discover tools dynamically — never hard-code names/schemas.** The surface changes between
> harness versions. Always start a session with `mcp({ server: "meteor-harness" })` and
> `mcp.describe(<tool>, "meteor-harness")`.

---

## Tool surface (Meteor variant)

Categories you'll typically find (derive from discovery, don't assume):

| Category | Examples | Notes |
| --- | --- | --- |
| **Core / status** | harness info, debug, **session release** | Release the session lock when done. |
| **Module** | list / get / **toggle** / configure settings | Meteor **and addon** modules — this is how BlackOut modules are driven. |
| **World state** | player pos/vitals/effects, inventory, crosshair, nearby entities | Read-only. |
| **World action** | chat, slash commands, attack, interact | Mutating. |
| **Pathing** | Baritone goto/stop, status | Needs Baritone (compileOnly in BlackOut; bundled in Meteor). |
| **DOM query** | snapshot a screen into a DOM tree, query by label/role/type | NPEs if no screen is open — open one first. |
| **DOM interaction** | click / scroll / drag (by coordinates) | See caveats below. |
| **DOM input** | text input, key simulation | — |

Full per-tool reference: `meteor-test-harness/docs/TOOLS.md`.

---

## Session model

The harness runs **single-session** by default: one agent owns it at a time. If a previous session
didn't clean up (e.g. a crashed run), the next connect is rejected until you **release** it — call
the harness' session-release/reset tool (discover its exact name) before starting.

---

## Critical caveats (these bite)

1. **Render-thread dispatch.** Every tool handler runs on Minecraft's render thread via
   `MainThreadInvoker`. A tool that triggers heavy work or a deadlock will **hang** — report any
   timeout as a likely threading issue, not a tool bug.
2. **DOM coordinates can be parent-relative.** Non-`ClickableWidget` elements get their x/y from the
   parent container; clicks may miss if you treat them as screen-relative. Prefer clicking through
   the screen (`screen.mouseClicked(x, y)`) for list entries.
3. **List entries route through the parent.** A world-list / server-list entry's own `mouseClicked()`
   often just returns `true`; selection happens in the parent dispatch chain. The click tool handles
   this, but if a click "does nothing", suspect the routing.
4. **Stale DOM.** Element paths from an old snapshot may not match after a screen update. Always take
   a fresh snapshot before interacting.
5. **The game is live.** A click really clicks; a chat tool really sends. Plan mutations, verify
   state after, and clean up (toggle modules back off).
6. **Obfuscated names = bug.** MC 26.1.x ships unobfuscated. If a tool returns `a`, `b`, `c`-style
   names, that's a harness/Mixin bug worth reporting.

---

## Worked example: verify a BlackOut module end-to-end

```javascript
// 0. ensure connected + discover the module tool names
mcp({ server: "meteor-harness" });
const desc = await mcp.describe("module_list", "meteor-harness");

// 1. list modules, find a BlackOut one (BlackOut modules warn about spaces in their names)
const mods = await mcp.call("module_list", {}, "meteor-harness");
//   -> look for e.g. "Auto Crystal+", "Surround+", "Anti Crawl" (category "BlackOut")

// 2. toggle it on, verify state
await mcp.call("module_toggle", { module: "Auto Crystal+" }, "meteor-harness");
const active = await mcp.call("module_get", { module: "Auto Crystal+" }, "meteor-harness");
//   -> assert active.isActive === true

// 3. (optional) change a setting + verify
await mcp.call("module_setting_set",
  { module: "Auto Crystal+", setting: "<settingName>", value: <value> }, "meteor-harness");

// 4. clean up
await mcp.call("module_toggle", { module: "Auto Crystal+" }, "meteor-harness");

// 5. release the session lock for the next run
await mcp.call("<session_release_tool>", {}, "meteor-harness");
```

For broad coverage, dispatch the **`live-tester`** agent (defined in the harness' own
`.pi/agents/live-tester.md`) — it does dynamic discovery, builds a coverage-driven test plan, and
exercises the whole surface.

---

## Verifying the BlackOut port specifically

Useful things to confirm in-game now that the client boots clean (post #1–#9):

- **Module registration** — `module_list` shows BlackOut's category + modules (Anchor Aura+, Auto
  Crystal+, Surround+, Anti Crawl, …). (Boot log already confirms registration.)
- **Module toggle** — toggle a module on/off; confirm `isActive` flips and no exception fires.
- **Mixin runtime paths** — the in-world mixins (`MixinLocalPlayer` send redirects,
  `MixinMultiPlayerGameMode` destroy, `MixinExplosion` sound, `MixinEntity` collide/step) only fire
  during gameplay; join a local world and exercise them (move, break a block, trigger an explosion).
- **#9 follow-up check** — `SoundModifier.crystalHits` (retargeted in `f84a148`): enable it, attack
  an EndCrystal, confirm the hit-sound volume/pitch scales (the agent's static retarget needs this
  runtime audio confirmation).

---

## File map

| Path | Purpose |
| --- | --- |
| `scripts/launch-test-client.ps1` | Build + stage + launch the loaded client; wait for MCP. |
| `scripts/stop-test-client.ps1` | Tear down the client + single-use gradle process. |
| `.pi/mcp.json` | Declares the `meteor-harness` MCP server (reload pi to activate). |
| `docs/test-harness.md` | This file. |
| `…/meteor-test-harness/.pi/agents/live-tester.md` | Full harness/agent documentation (source for this file). |
| `…/meteor-test-harness/README.md`, `docs/TOOLS.md` | Harness feature + per-tool reference. |
