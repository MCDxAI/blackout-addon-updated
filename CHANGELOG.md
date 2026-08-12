# Changelog

All notable changes to the **BlackOut** addon's port to Meteor Client 26.1.2 /
Minecraft 26.1.2 are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

No releases have been cut yet, so all changes currently live under
**[Unreleased]**. Each entry is annotated with the short commit hash (`abcdefg`)
it shipped in, plus GitHub issue references where applicable. Once tagging
begins, semantic-version sections with a compare-link footer will be added.

## [Unreleased]

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

### Removed

- Cleanup: dropped stale IDE-remapper junk comments left behind in 7 modules.
  (`f6a62d4`)

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
