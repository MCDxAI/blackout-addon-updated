---
name: meteor-addon
description: Meteor Client addon development for Minecraft. Use when creating, updating, or working with Meteor Client addons - a Fabric mod framework for Minecraft. Covers workspace setup from the template repository, build configuration with Gradle Kotlin DSL and version catalogs, updating addons when Minecraft/Meteor versions change, finding reference implementations from verified addons, and understanding Meteor's addon structure, APIs, and threading model.
---

# Meteor Client Addon Development

This skill provides guidance for developing addons for Meteor Client, a Fabric-based Minecraft utility mod.

## Key Resources

- **Template Repository**: https://github.com/MeteorDevelopment/meteor-addon-template
- **Meteor Client Source**: https://github.com/MeteorDevelopment/meteor-client
- **Addon Database**: https://raw.githubusercontent.com/cqb13/meteor-addon-scanner/refs/heads/addons/addons.json
- **Meteor Addons Site**: https://meteoraddons.com/about

## Workflow Overview

1. **Setup**: Start from the template, configure build files with current versions
2. **Update Check**: Verify versions are current with Meteor/Minecraft releases
3. **Development**: Implement features following Meteor API patterns
4. **Reference**: Use `scripts/filter_addons.py` to find examples from verified addons

## Current Version Baseline (as of 2025-04)

| Component | Version |
|-----------|---------|
| Minecraft | 1.21.11 |
| Yarn Mappings | 1.21.11+build.3 |
| Fabric Loader | 0.18.2 |
| Fabric Loom | 1.14-SNAPSHOT |
| Meteor Client | 1.21.11-SNAPSHOT |
| Java | 21 |

Always verify these are current by checking the template repo and meteor-client releases.

## Starting a New Addon

### From Template

```bash
git clone https://github.com/MeteorDevelopment/meteor-addon-template my-addon
cd my-addon
```

### Build System (Kotlin DSL + Version Catalog)

Modern Meteor addons use **Gradle Kotlin DSL** (`build.gradle.kts`) with a **version catalog** (`gradle/libs.versions.toml`). All version management lives in the TOML file — NOT in `gradle.properties` or `build.gradle.kts`.

**Key files to configure:**

| File | Purpose |
|------|---------|
| `gradle/libs.versions.toml` | ALL version numbers, dependency declarations, plugin declarations |
| `build.gradle.kts` | Build logic, repositories, dependency references via `libs.*` |
| `gradle.properties` | Only `maven_group` and `archives_base_name` (mod identity) |
| `settings.gradle.kts` | Plugin management with Fabric Maven |

See `references/build-setup.md` for complete file templates.

### Essential Build Configuration

**Required Meteor Maven repositories** (in `build.gradle.kts`):
```kotlin
repositories {
    mavenCentral()
    maven {
        name = "meteor-maven"
        url = uri("https://maven.meteordev.org/releases")
    }
    maven {
        name = "meteor-maven-snapshots"
        url = uri("https://maven.meteordev.org/snapshots")
    }
}
```

**Bundling dependencies** — use the `modInclude` pattern to shade libraries into your JAR:
```kotlin
fun DependencyHandler.modInclude(
    dependencyProvider: Provider<out MinimalExternalModuleDependency>
) {
    modImplementation(dependencyProvider)
    include(dependencyProvider)
}
```

Use `modInclude(libs.gson)` etc. for any library your addon needs at runtime (Meteor doesn't provide transitive deps).

### fabric.mod.json

Required fields for a Meteor addon:

```json
{
  "schemaVersion": 1,
  "id": "my-addon",
  "version": "${version}",
  "name": "My Addon",
  "description": "What it does",
  "authors": ["AuthorName"],
  "icon": "assets/my-addon/icon.png",
  "environment": "client",
  "entrypoints": {
    "meteor": ["com.example.myaddon.MyAddon"]
  },
  "mixins": [],
  "custom": {
    "meteor-client:color": "150,100,255"
  },
  "depends": {
    "java": ">=21",
    "minecraft": ">=1.21.11",
    "meteor-client": ">=0.5.0"
  }
}
```

Key points:
- **Entrypoint**: `"meteor"` array — points to your class extending `MeteorAddon`
- **`custom.meteor-client:color`**: RGB color for your addon in Meteor's GUI (e.g., `"150,100,255"`)
- **`depends.java`**: `>=21` required for modern Meteor
- **`${version}`**: Expanded by `processResources` from your version catalog

### meteor-addon-list.json (Optional)

Create this file in your project root to customize how your addon appears on [meteoraddons.com](https://meteoraddons.com/about). This file overrides the scraped data:

```json
{
  "description": "A short description of your addon.",
  "tags": ["PvP", "Utility"],
  "supported_versions": ["1.21.7", "1.21.8"],
  "icon": "https://example.com/icon.webp",
  "discord": "https://discord.gg/your-invite",
  "homepage": "https://your-site.com",
  "feature_directories": {
    "commands": ["modules/commands"],
    "modules": ["modules/general"],
    "hud_elements": ["modules/hud"]
  }
}
```

**Supported tags**: PvP, Utility, Theme, Render, Movement, Building, World, Misc, QoL, Exploit, Fun, Automation

**feature_directories**: Tells the scanner where to find your Java files. Paths are relative to the entrypoint package (e.g., if your addon package is `com.example.myaddon`, directories are relative to that). Use forward slashes, no leading/trailing slashes. Only list directories, not files.

## Meteor API Patterns

### Addon Entry Point

Every addon extends `MeteorAddon` and is registered via `fabric.mod.json`:

```java
public class MyAddon extends MeteorAddon {
    public static final Logger LOG = LoggerFactory.getLogger("My Addon");

    @Override
    public void onInitialize() {
        // Register systems, tabs, commands here
        Systems.add(new MyConfig());
        Tabs.add(new MyTab());
    }

    @Override
    public void onRegisterCategories() {
        // Register custom module categories if needed
    }

    @Override
    public String getPackage() {
        return "com.example.myaddon";  // Your base package
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("owner", "repo-name");
    }
}
```

### Core Registration APIs

| API | Purpose | Example |
|-----|---------|---------|
| `Systems.add(new MySystem())` | Persistent singleton with NBT storage | Config, manager classes |
| `Tabs.add(new MyTab())` | GUI tab in Meteor's menu | Settings/config screens |
| `Commands.add(new MyCommand())` | Chat command | `.mycommand` |
| `MeteorClient.EVENT_BUS` | Event subscribe/unsubscribe | `@EventHandler` methods |

### Threading Model (CRITICAL)

**NEVER block the render thread.** All network I/O and heavy processing must run on background threads:

```java
// Background work
MeteorExecutor.execute(() -> {
    String result = doNetworkCall();

    // Back to render thread for GUI updates
    mc.execute(() -> {
        // Safe to interact with Minecraft GUI here
    });
});
```

### GUI Widget Lifecycle (CRITICAL)

Meteor's GUI framework has a strict initialization pattern. Violating it causes NPE crashes:

```java
// CORRECT
public class MyWidget extends WVerticalList {
    private final Data data;

    public MyWidget(Data data) {
        this.data = data;
        // Do NOT call init() here!
        // Do NOT accept GuiTheme in constructor!
    }

    @Override
    public void init() {
        // Framework calls this AFTER widget is added to tree with theme set
        add(theme.label("text")).centerX();
    }
}
```

```java
// WRONG — causes crashes
public MyWidget(GuiTheme theme, Data data) {
    this.theme = theme;  // WRONG: shadowing parent field
    init();              // WRONG: theme not set yet in parent
}
```

The `theme` field is set by the framework when the widget is added to the widget tree. Never set it manually or call `init()` from the constructor.

### Common Addon Components

- **Modules** (`extends Module`): Toggleable features, organized by category
- **Commands** (`extends Command`): Chat commands with Brigadier integration
- **HUD Elements** (`extends HudElement`): On-screen display components
- **Systems**: Persistent singletons with automatic NBT serialization (`toTag()`/`fromTag()`)
- **GUI Tabs**: Custom tabs in Meteor's GUI menu
- **GUI Screens**: Full-screen interfaces extending Meteor's screen base classes
- **Mixins**: Fabric mixins for modifying Minecraft/Meteor behavior

See `references/api-patterns.md` for detailed patterns and examples.

## Updating Existing Addons

When Minecraft or Meteor updates:

1. **Update version catalog** — change versions in `gradle/libs.versions.toml`
2. **Check meteor-client commit history** for API changes:
   ```bash
   git clone --depth=1 https://github.com/MeteorDevelopment/meteor-client
   cd meteor-client && git log --oneline --grep="breaking" -20
   ```
3. **Check for breaking changes**:
   - Deprecated methods or refactored classes
   - New Java version requirements
   - Changed dependency formats
4. **Find updated examples**: `python scripts/filter_addons.py --mc-version X.XX.XX --verified --sort-by last_update`

## Finding Reference Implementations

The addon database contains metadata about all known Meteor Client addons. Use the filter script to find high-quality examples.

### Basic Usage

```bash
# Find verified addons for a specific Minecraft version
python scripts/filter_addons.py --mc-version 1.21.11 --verified

# Find all addons for a version (including unverified)
python scripts/filter_addons.py --mc-version 1.21.11 --no-verified

# Limit results
python scripts/filter_addons.py --mc-version 1.21.11 --verified --limit 10
```

### Advanced Filtering

```bash
# Find addons with specific features
python scripts/filter_addons.py --feature-type modules --feature-name "AutoTotem"

# Filter by minimum stars
python scripts/filter_addons.py --mc-version 1.21.11 --min-stars 50

# Sort by different criteria
python scripts/filter_addons.py --mc-version 1.21.11 --sort-by last_update

# Search by description
python scripts/filter_addons.py --search "PvP"
# Tags: PvP, Utility, Theme, Render, Movement, Building, World, Misc, QoL, Exploit, Fun, Automation
```

### Cloning References

```bash
# Single addon
python scripts/clone_for_analysis.py https://github.com/owner/addon-name

# Bulk cloning
python scripts/clone_for_analysis.py https://github.com/owner/addon1 https://github.com/owner/addon2

# Custom target directory
python scripts/clone_for_analysis.py https://github.com/owner/addon --target ./my_refs
```

**Best practice**: Store cloned repos in an `ai_reference/` directory (git-ignored).

### Quality Rules

1. **Skip archived addons** unless porting legacy code
2. **Prefer verified addons** — only use unverified if no verified examples exist
3. **Match versions** — ensure addon's versions are compatible with your project
4. **Consider stars** — higher star count often means better maintained code

## Common Addon Structure

```
src/main/java/com/example/myaddon/
├── MyAddon.java              # Entry point (extends MeteorAddon)
├── modules/                  # Module classes (extends Module)
├── commands/                 # Chat commands (extends Command)
├── hud/                      # HUD elements (extends HudElement)
├── systems/                  # Persistent singletons (Systems pattern)
├── gui/
│   ├── tabs/                 # GUI tabs
│   ├── screens/              # Full-screen GUI screens
│   └── widgets/              # Custom GUI widgets
├── mixin/                    # Fabric mixins
├── util/                     # Utility classes
└── models/                   # Data models

src/main/resources/
├── fabric.mod.json           # Mod metadata + Meteor entrypoint
├── my-addon.mixins.json      # Mixin configuration (if using mixins)
└── assets/my-addon/
    └── icon.png              # Addon icon
```

## Common Issues

### Crash: "theme is null" in widget
- **Cause**: Called `init()` from constructor or accepted `theme` in constructor
- **Fix**: Override `init()`, let the framework call it, never pass `theme` to constructors

### "Cannot find Meteor classes" compilation error
- **Cause**: Missing Meteor Maven repository in `build.gradle.kts`
- **Fix**: Add both `meteor-maven` and `meteor-maven-snapshots` repositories
- **Then**: `./gradlew clean build --refresh-dependencies`

### Template is outdated
1. Check current versions in this skill's Version Baseline table
2. Use `filter_addons.py --verified --sort-by last_update` to find recently updated addons
3. Compare their `libs.versions.toml` files

### Dependencies not available at runtime
- **Cause**: Meteor doesn't provide transitive dependencies
- **Fix**: Use `modInclude()` to bundle required libraries into your JAR

## Best Practices

- Keep addon updated with latest Minecraft/Meteor versions
- Use version catalog (`libs.versions.toml`) for ALL version management
- Follow Meteor's code style and patterns (see `references/api-patterns.md`)
- Use `modInclude()` to shade any runtime dependencies
- Never block the render thread — use `MeteorExecutor` for background work
- Use verified addons as reference implementations
- Store cloned references in `ai_reference/` for easy access
- Check meteor-client source for authoritative API documentation
