---
name: minecraft-dev-mcp
description: >-
  Operate the minecraft-dev MCP server's 26 tools to decompile, search, and
  compare Minecraft Java Edition source across versions; translate symbol
  mappings (yarn/mojmap/intermediary/official); and validate Mixins, Access
  Wideners, and Access Transformers. Use when porting mods between Minecraft or
  Meteor/Meteor-Client versions (e.g. the BlackOut 1.21.11 to 26.1.2 port),
  resolving mapping renames that break compilation, validating
  @Mixin/@Inject/@Redirect etc. injection targets against decompiled MC,
  re-validating a Fabric .accesswidener or Forge/NeoForge access transformer
  against a target MC version, translating obfuscated/intermediary symbols to
  readable names, diffing class/registry/API changes between MC versions,
  querying registry data (blocks/items/entities), or reading and decompiling a
  reference mod JAR. Covers source access, regex and FTS5 code search, registry
  data, mapping translation, AST-level version diffs, and mixin/AW/AT
  validation.
metadata:
  mcp_server: minecraft-dev
  tool_count: "26"
---

# minecraft-dev-mcp

Operational reference for the **minecraft-dev** MCP server (26 tools), connected
via this workspace's `.pi/mcp.json`. This skill tells you *which* tool to call
for each Minecraft modding task and the parameters that matter.

All tools are prefixed `minecraft_dev_` at the MCP gateway
(e.g. `minecraft_dev_get_minecraft_source`).

## When to use

Activate when the task involves any of:
- Porting a mod between Minecraft or Meteor/Meteor Client versions (canonical
  case: **BlackOut 1.21.11 → 26.1.2**).
- Decompiled-MC source questions: "what does class X look like in version Y",
  "find usages of symbol Z".
- Mapping-rename breakage: a symbol compiles under one mapping but not another.
- Validating `@Mixin` injection targets, a Fabric `.accesswidener`, or a
  Forge/NeoForge access transformer against a target MC version.
- Translating between yarn / mojmap / intermediary / obfuscated ("official").
- Diffing two MC versions (class/registry level or AST-level API breakage).
- Reading or decompiling a reference mod JAR.

## Gotchas — read before calling anything

1. **Mapping params are strict enums.** `mapping` / `toMapping` accept exactly
   `yarn` or `mojmap`. Only `find_mapping` additionally accepts `intermediary`
   and `official` (where `official` = obfuscated names like `"a"`, `"b"`, `"c"`).
   Pass these strings verbatim — typos tend to return empty results, not errors.
2. **Forge/NeoForge needs `mojmap`.** When you pass `jarPath` (a local patched
   MC JAR) to `decompile_minecraft_version`, `mapping` MUST be `mojmap`.
   Forge/NeoForge 1.17+ dev artifacts are mojmap-only. The `version` then acts
   as an opaque cache key (e.g. `"1.21.1-neoforge-21.1.72"`).
3. **Paths accept two forms.** `jarPath`, `source`, and `content` (when it is a
   path) accept both WSL (`/mnt/c/...`) and Windows (`C:\...`) forms.
4. **First call is slow, then cached.** A version's first decompile, first index
   build, and first registry-data generation each run a heavy pipeline
   (download → remap → decompile / data-gen) and cache afterward. Subsequent
   calls are fast. **If you will search a version more than once, build the FTS5
   index first** (`index_minecraft_version`) and query with `search_indexed` —
   it is much faster than `search_minecraft_code` for large queries.
5. **Decompiled output is line-addressable.** `get_minecraft_source` takes
   1-indexed `startLine` / `endLine` (inclusive) plus a `maxLines` cap applied
   after line filtering — use these to avoid pulling a huge class.

## Tool reference by category

For exact parameter names, enums, and defaults, load
**`references/tool-schemas.md`** before constructing a call. The summaries below
give purpose + required params + the optional params that matter most.

### 1. Source access
- `get_minecraft_source` — one class; auto-downloads/decompiles/remaps. Req:
  `version`, `className` (FQN), `mapping`. Opt: `startLine`, `endLine`,
  `maxLines`.
- `decompile_minecraft_version` — whole version (client JAR from Mojang → remap
  → VineFlower); cached after. Req: `version`, `mapping`. Opt: `force` (wipes
  cache + FTS5 index), `jarPath` (patched JAR; forces `mapping=mojmap`).
- `list_minecraft_versions` / `get_minecraft_versions_list` — available + cached
  versions. No args (the two are aliases).

### 2. Search
- `search_minecraft_code` — regex/literal over decompiled source, one-shot. Req:
  `version`, `query`, `searchType` (`class|method|field|content|all`),
  `mapping`. Opt: `limit` (default 50).
- Two-step FTS5 (much faster for repeated/large queries): `index_minecraft_version`
  (req `version`, `mapping`) once, then `search_indexed` (req `query` using FTS5
  syntax `AND OR NOT "phrase" prefix*`, `version`, `mapping`; opt `types[]`,
  `limit` default 100).
- `get_indexed_versions_list` — what's already indexed (no args). Call before
  indexing to avoid a redundant rebuild.

> Prefer the indexed path whenever repeat searching on the same version is
> expected.

### 3. Registry data
- `get_registry_data` — blocks/items/entities/etc. Req: `version`. Opt:
  `registry` (e.g. `"blocks"`; omit for all). **Runs the data generator on first
  call (slow), then cached.**

### 4. Mapping translation
- `find_mapping` — symbol lookup between systems. Req: `symbol`, `version`,
  `sourceMapping`, `targetMapping` (each `yarn|mojmap|intermediary|official`).
  `official` = obfuscated names like `"a"`.
- `remap_mod_jar` — Fabric mod JAR intermediary → human-readable. Req:
  `inputJar`, `outputJar`, `toMapping` (`yarn|mojmap`). Opt: `mcVersion`
  (auto-detected if omitted).

### 5. Version comparison (critical for porting)
- `compare_versions` — class/registry-level diff between two versions. Req:
  `fromVersion`, `toVersion`, `mapping`. Opt: `category`
  (`classes|registry|all`, default `all`).
- `compare_versions_detailed` — AST-level: method-signature, field, and
  breaking-API changes. Req: `fromVersion`, `toVersion`, `mapping`. Opt:
  `packages[]` (e.g. `["net.minecraft.entity"]`) to scope; `maxClasses`
  (default 1000).
- These are the primary tools for the **BlackOut 1.21.11 → 26.1.2** port.

### 6. Mixin & access-widener / access-transformer validation (critical)
- `analyze_mixin` — parses `@Mixin` / `@Inject` / `@Redirect` / etc. and
  validates targets against decompiled MC; suggests fixes. Req: `source` (Java
  source OR path to JAR/dir), `mcVersion`. Opt: `mapping` (default `yarn`).
- `validate_access_widener` — Fabric `.accesswidener`; verifies targets exist.
  Req: `content` (file content OR path), `mcVersion`. Opt: `mapping`
  (default `yarn`).
- `validate_access_transformer` — Forge/NeoForge `.cfg`; detects record-ctor
  crashes, inner-class accessibility, conflicting modifiers. Req: `content`,
  `mcVersion`. Opt: `mapping` (default `mojmap`), `extraFiles[]` (other AT files
  for cross-file conflict detection).
- Reference docs (no args): `get_mixin_documentation`,
  `get_access_widener_documentation`, `get_access_transformer_documentation`.
- **BlackOut ships `src/main/resources/blackout.accesswidener` — re-validate it
  against the target version.**

### 7. General documentation
- `get_documentation` — per-class docs (Fabric Wiki / Minecraft Wiki links,
  usage hints). Req: `className`.
- `search_documentation` — search all known MC/Fabric topics. Req: `query`.

### 8. Mod JAR analysis
Read a reference addon (often built against newer Meteor/MC) with this pipeline:
1. `analyze_mod_jar` — metadata/deps/entry points/mixins/classes. Req:
   `jarPath`. Opt: `includeAllClasses` (default false), `includeRawMetadata`
   (default false). Read `modId` / `modVersion` from the result.
2. `decompile_mod_jar` — readable Java; cached under
   `AppData/decompiled-mods/{modId}/{modVersion}/{mapping}`. Req: `jarPath`,
   `mapping` (match how the JAR was remapped). Opt: `modId`, `modVersion`
   (auto-detected).
3. Then search: one-shot `search_mod_code` (regex; req `modId`, `modVersion`,
   `query`, `searchType`, `mapping`) OR the faster FTS5 path `index_mod` +
   `search_mod_indexed` (same FTS5 syntax; index req `modId`, `modVersion`,
   `mapping`).

## BlackOut 1.21.11 → 26.1.2 port playbook

The project uses **Yarn** mappings (Yarn 1.21.11). Default every `mapping`
parameter to `yarn` unless a tool forces otherwise.

1. **Diff the versions.** `compare_versions` and `compare_versions_detailed`
   with `fromVersion="1.21.11"`, `toVersion="26.1.2"`, `mapping="yarn"`. Use the
   detailed call's `packages[]` to scope to packages BlackOut touches.
2. **Resolve renames that break compilation.** For each failing symbol, search
   the new source with `search_indexed`; use `find_mapping` to translate across
   systems (e.g. `mojmap`→`yarn`) to recover the new yarn name.
3. **Validate mixins.** `analyze_mixin(source=<mixins or dir>,
   mcVersion="26.1.2", mapping="yarn")` for each `@Mixin`; fix targets the report
   flags.
4. **Re-validate the access widener.**
   `validate_access_widener(content="src/main/resources/blackout.accesswidener",
   mcVersion="26.1.2", mapping="yarn")`. Every `accessible` / `extendable` /
   `mutable` entry must resolve against 26.1.x.
5. **Read reference addons.** If a newer Meteor addon shows the new API shape:
   `analyze_mod_jar` → `decompile_mod_jar` (`mapping` matching the addon's
   remap) → `search_mod_code` / `index_mod` + `search_mod_indexed`.

## Files in this skill

- `references/tool-schemas.md` — full per-tool schemas (required params, enums,
  defaults, path-form notes). **Load it before constructing any call** to
  confirm exact parameter names and accepted enum values.
