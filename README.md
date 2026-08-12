![logo](src/main/resources/assets/blackout/logo.png)

# BlackOut

A [Meteor Client](https://meteorclient.com/) addon focused on CrystalPVP — an improved CrystalAura, BedAura, PacketFly, and more, built to push you above the competition.

> A port of the original BlackOut addon by KassuK (now unmaintained), updated for **Minecraft 26.1.2** and **Meteor Client 26.1.x**.

## Requirements

| Dependency        | Version                |
| ----------------- | ---------------------- |
| Minecraft         | 26.1.x (tested 26.1.2) |
| Meteor Client     | 26.1.x                 |
| Fabric Loader     | 0.19.2 or newer        |
| Java              | 25 or newer            |

## Installation

1. Install [Fabric Loader](https://fabricmc.net/) for **Minecraft 26.1.x**.
2. Download [Meteor Client](https://meteorclient.com/) and place it in your `mods/` folder.
3. Download the latest BlackOut jar from the [releases page](https://github.com/MCDxAI/blackout-addon-updated/releases) and place it in `mods/` alongside Meteor.
4. Launch the game.

## Building from source

```bash
./gradlew build
```

The build targets **JDK 25** (Fabric Loom + Gradle version catalog). The finished jar is written to `build/libs/`.

## Credits

**Original authors** — KassuK, OLEPOSSU, H1ggsK, Crosby, Wide_Cat.

Thanks to Doogie13 (mining calculations and step offsets) and RickyTheRaccoon (InvSwitch).

See [CHANGELOG.md](CHANGELOG.md) for the 26.1.x port work.

## License

Distributed under the [GPL-3.0-or-later](LICENSE) license.
