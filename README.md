# Minecraft Manhunt Mods

Fabric utility mods for **Minecraft 26.3** + **Fabric Loader 0.20.1**, built for Open-to-LAN manhunts.

## Mods

| Folder | Mod | Side | License |
|---|---|---|---|
| `mods/teamchat` | Manhunt Team Chat | Server | MIT preferred (1.0.5 jar may still say CC0) |
| `mods/more-pearls` | Manhunt More Pearls | Server | MIT |
| `mods/pausegame` | Manhunt Pause Game | Server | MIT |
| `mods/entity-counter` | Manhunt Entity Counter | Client | MIT |
| `mods/spectatorplus` | SpectatorPlus 26.3 port | Client + server | GPL-3.0-or-later |

## Layout

```
assets/modrinth/     Modrinth icons & prompt references
docs/MODRINTH.md     Publishing guide (descriptions, deps, Nano Banana prompts)
mods/                One Fabric project per mod
releases/            Latest built jars only (local; sync script)
scripts/             Repo helpers (sync-releases.ps1)
```

## Build a mod

```bat
cd mods\more-pearls
gradlew.bat build
```

Then from the repo root:

```bat
powershell -ExecutionPolicy Bypass -File .\scripts\sync-releases.ps1
```

Jars land in `releases/` (older versions of the same mod are removed).

## Docs

- **Modrinth upload notes:** [`docs/MODRINTH.md`](docs/MODRINTH.md)
- **SpectatorPlus port notes:** [`mods/spectatorplus/PORT-26.3.md`](mods/spectatorplus/PORT-26.3.md)

## Requirements

- JDK **25+**
- Fabric Loader **0.20.1**
- Minecraft **26.3**
