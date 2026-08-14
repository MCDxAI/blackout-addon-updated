# Changelog

All notable changes to the **BlackOut** addon — the Meteor Client port and its
Minecraft version updates through **26.2** — are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

Each entry is annotated with the short commit hash (`abcdefg`) it shipped in,
plus GitHub issue references where applicable. Compare links are at the bottom
of this file.

## [Unreleased]

## [2.1.0] - 2026-08-14

### Changed

- Build: updated to **Minecraft 26.2** / **Meteor Client 26.2-SNAPSHOT** — Baritone
  26.2-SNAPSHOT, Fabric Loader 0.19.3, Fabric Loom 1.17-SNAPSHOT, Gradle 9.6.1;
  mod version 2.1.0, with `fabric.mod.json` declaring `~26.2` compatibility and
  requiring meteor-client `>=26.2-0` (matches Meteor's distributed `26.2-N` pre-release versions). (`39a6dd5`)
- Port: migrated all 62 `BlockPos.getCenter()` call sites (22 files) to the static
  `Vec3.atCenterOf(pos)` — the instance method was removed in Minecraft 26.2.
  (`39a6dd5`)

### Fixed

- Port: moved entity-type constants to 26.2's new `EntityTypes` class
  (`EntityTypes.ITEM`, `EntityTypes.END_CRYSTAL`) in AutoTrapPlus and SelfTrapPlus.
  (`39a6dd5`)
- OffHandPlus: adapted to 26.2's bed item collapse — `Items.RED_BED` no longer
  exists as a constant; bed detection now uses `instanceof BedItem` and the red-bed
  default resolves via `Items.BED.pick(DyeColor.RED)`. (`39a6dd5`)
- Port: replaced the removed `Minecraft.screen` field with `mc.gui.screen()`
  (AutoPvp death-screen check, OffHandPlus inventory check) and routed chat output
  through `mc.gui.hud.getChat()` (BlackOutModule). (`39a6dd5`)
- PacketNames: `ClientboundSetPlayerTeamPacket.Parameters` became a record in
  26.2 — accessors renamed, and the team color is read from the new
  `Optional<TeamColor>` via its serialized name. (`39a6dd5`)
- Mixins: retargeted `MixinItemInHandRenderer` to 26.2's renamed
  `ItemInHandRenderer.submitHandsWithItems`/`submitArmWithItem` — the stale strings
  compiled fine but would have hard-crashed at launch under `defaultRequire: 1`.
  All 12 mixins and both `blackout.accesswidener` entries were revalidated against
  26.2 mojmap. (`39a6dd5`)
- Verified: boots and loads cleanly in a 26.2 test instance alongside the
  distributed meteor-client `26.2-4` build, Baritone 26.2-SNAPSHOT, and the
  MCDxAI addon set. (`39a6dd5`)

## [2.0.0] - 2026-08-12

### Added

- Docs: established this `CHANGELOG.md` (Keep a Changelog format) and backfilled
  it with the 26.1.x port work to date.
- Project automation: initialized the `MCDxAI/blackout-addon-updated` repo with
  the full `.pi/` config — agents (`port-engineer`, `mixin-mapping-specialist`,
  `code-reviewer`), skills, the `minecraft-dev` MCP server config, and the
  bootstrap manifest. (`e2a871f`)
- Tooling: autonomous Minecraft test-client launcher, meteor-harness MCP wiring,
  and supporting docs for verifying the port in-game. (`66edbf6`)

### Changed

- Build: wired Spotless with Google Java Format and normalized the entire source
  tree to the project style (2-space indent, no wildcard imports, K&R braces,
  100-column limit). (`9a2b392`, #8)
- RaytraceSettings: renamed the public field `ClipContext` -> `clipContext` (it
  shadowed its own type name; Google Style wants lowerCamelCase) and updated all
  19 access sites. The `ClipContext` type is unchanged. (`8d902fa`)
- Housekeeping: expanded the `RaytraceSettings` wildcard settings import to
  explicit imports (Google Style §3.3.1), and corrected the `CLAUDE.md` target
  table's stale Yarn-mappings row (the build uses no mappings line — MC 26.1.x
  ships de-obfuscated). (`aba89ff`)
- RaytraceSettings: consolidated onto Meteor's built-in `IClipContext`
  (`meteordevelopment...mixininterface.IClipContext`), dropping BlackOut's own
  duplicate `mixins/IClipContext`. The two parallel mutation paths
  (`blackout$set*` vs `meteor$set`) targeted the same `ClipContext`; Meteor's
  exposes the same `from`/`to` fields plus more, so BlackOut's was a pure
  maintenance duplicate with zero unique capability. The 8 `setTo` call sites
  now route through a small `setClipTo()` helper. (`16226a4`)

### Removed

- Cleanup: dropped stale IDE-remapper junk comments left behind in 7 modules.
  (`f6a62d4`)
- blackout.accesswidener: removed the unused `Holder$Reference bindKey` entry
  (added by the port, referenced nowhere in `src/`); the widener now resolves
  against MC 26.1.2 with its two surviving entries. (`8d902fa`)
- Cleanup: deleted 7 vestigial accessor mixins that had zero call sites in
  `src/` (latent startup-crash risk under `defaultRequire: 1`): BlackOut's own
  `IClipContext` (see Changed) plus six packet accessors —
  `IServerboundMovePlayerPacket`, `IClientboundMoveEntityPacket`,
  `IClientboundRotateHeadPacket`, `IClientboundEntityEventPacket`,
  `IClientboundSetCameraPacket`, `IServerboundTeleportToEntityPacket`.
  `blackout.mixins.json` now lists 12 mixins matching 12 files. (`16226a4`)

### Fixed

- Build: locked the mojmap environment and corrected `fabric.mod.json`
  dependency declarations. (`92538fd`, #1)
- Port: converted 15 source files from mixed yarn/mojmap to a consistent mapping
  set. (`ccea3af`, #2)
- Port: fixed all remaining Minecraft + Meteor 26.1.2 API breaks across the
  addon. (`f67c37c`, #3, #4)
- Port: revalidated every mixin and the access widener against Minecraft
  26.1.2. (`ef22985`, #5)
- Port: fixed four runtime mixin-resolution failures discovered via `runClient`
  smoke tests. (`c52de3a`, #6)
- Port: faithfully retargeted the `MixinPlayer` `SoundModifier.crystalHits`
  injection to the 26.1.2 server-side sound path. (`f84a148`, #9)
- Tooling: fixed PowerShell 5.1 encoding in the launcher and added Baritone to
  `runClient` runtime so the dev client boots cleanly. (`4cf1196`)
- Tooling: invoke `gradlew` by full path in `launch-test-client.ps1`. (`f0af983`)
- AutoCart: repaired the **Auto Ignite** mode, which was a no-op — it sent a
  block-interact packet at the floor instead of interacting with the
  TNT-minecart entity. Also removed the dead `placeMode` setting, used the flat
  rail render box it was already building, replaced the hardcoded `26` placement
  cap with a configurable `placementLimit` setting, and switched `isInHole` to
  the shared `HoleUtils`. (`ecdfcee`)
- BOEntityUtils: **restored the section-scoped, allocation-free, first-hit
  entity-intersection fast path** that was lost in `f67c37c` / `1a53cd9` when a
  Meteor accessor rename was mistaken for a removal. The interim vanilla
  `Level.getEntities` replacement was a genuine regression — it pre-filtered
  candidates by each entity's *real* bounding box, silently dropping players
  whose *extrapolated* box was the only overlap (AutoCrystal/HoleFill accuracy),
  and allocated a `List` per call with no early exit on the crystal hot path. An
  in-code Javadoc now documents this so it is not reintroduced. (`e08e498`, #7)

[Unreleased]: https://github.com/MCDxAI/blackout-addon-updated/compare/v2.1.0...HEAD
[2.1.0]: https://github.com/MCDxAI/blackout-addon-updated/releases/tag/v2.1.0
[2.0.0]: https://github.com/MCDxAI/blackout-addon-updated/releases/tag/v2.0.0
