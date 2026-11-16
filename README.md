# MinecraftMod

Small Fabric mods I made for Minecraft **26.3** manhunts (Open to LAN / Fabric Loader **0.20.1**).

These are for private games you host with people who know the mods are installed — breaks, team chat, spectator tools, that kind of thing. They are not meant to hide features on someone else’s server.

Mostly host-side utilities. Entity counter is client-only.

## Mods

| Path | What it does | License |
|---|---|---|
| [`mods/teamchat`](mods/teamchat) | `/chat hunter` · `/chat runner` · `/chat global` using Manhunt team tags | MIT |
| [`mods/more-pearls`](mods/more-pearls) | Piglin pearl barters at ~6.7%, stacks 4–7 | MIT |
| [`mods/pausegame`](mods/pausegame) | `/pausegame` pauses the match for a break | MIT |
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
powershell -File .\scripts\sync-releases.ps1
```

## Notes

- [Modrinth upload notes](docs/MODRINTH.md)
- [SpectatorPlus port notes](mods/spectatorplus/PORT-26.3.md)
