# MinecraftMod

Small Fabric mods I made for Minecraft **26.3** manhunts (Open to LAN / Fabric Loader **0.20.1**).

Mostly host-side stuff for officiating and pacing games. Entity counter is client-only.

## Mods

| Path | What it does | License |
|---|---|---|
| [`mods/teamchat`](mods/teamchat) | `/chat hunter` · `/chat runner` · `/chat global` using Manhunt team tags | MIT |
| [`mods/more-pearls`](mods/more-pearls) | Piglin pearl barters at ~6.7%, stacks 4–7 | MIT |
| [`mods/pausegame`](mods/pausegame) | `/pausegame` freezes everyone else | MIT |
| [`mods/entity-counter`](mods/entity-counter) | Puts the old F3 entity counter back on | MIT |
| [`mods/spectatorplus`](mods/spectatorplus) | SpectatorPlus ported to 26.3 | GPL-3.0-or-later |

Team Chat pairs with [Lprogrammer’s Manhunt](https://modrinth.com/datapack/manhunt_pack) (`hunters` / `runners` teams). It also works if those teams exist on their own.

## Repo layout

```
mods/            source for each mod
releases/        newest jars after a local sync (not committed)
assets/modrinth/ icons / art references
docs/            Modrinth notes, etc.
scripts/         helpers
```

## Building

Needs JDK 25+.

```bat
cd mods\more-pearls
gradlew.bat build
```

Copy the newest jars into `releases/`:

```bat
powershell -ExecutionPolicy Bypass -File .\scripts\sync-releases.ps1
```

## Notes

- [Modrinth upload notes](docs/MODRINTH.md)
- [SpectatorPlus port notes](mods/spectatorplus/PORT-26.3.md)
