# About

My Name is Joseph Telecsan. I created this repo as a spot for me to hold/present my minecraft mods. A lot of them started as small fixes for manhunt sessions, and at its current stage the project could see further use beyond that.

I am currently in the midst of a software engineering degree at Miami University, thus, mod updates may take longer than you would instinctively assume.

# MinecraftMod

Mods made for Minecraft.

Mod Versions and Loaders/Launchers vary depending on the mod.

Typical standard is fabric with intentions to work within the newest versions of minecraft.

## Mods

| Path | What it does | Minecraft Version | Loader Version | License |
|---|---|---|---|---|
| [`mods/teamchat`](mods/teamchat) | `/chat hunter` · `/chat runner` · `/chat global` using Manhunt team tags | 26.3 | Fabric 0.20.1 | MIT |
| [`mods/more-pearls`](mods/more-pearls) | Piglin pearl barters at ~6.7%, stacks 4-7 | 26.3 | Fabric 0.20.1 | MIT |
| [`mods/pausegame`](mods/pausegame) | `/pausegame` pauses the match for a break | 26.3 | Fabric 0.20.1 | MIT |
| [`mods/entity-counter`](mods/entity-counter) | Puts the old F3 entity counter back on | 26.3 | Fabric 0.20.1 | MIT |
| [`mods/spectatorplus`](mods/spectatorplus) | SpectatorPlus ported to 26.3 | 26.3 | Fabric 0.20.1 | GPL-3.0-or-later |

Each Mod has its own dependencies and functionalities so be sure to read into each mod individually.

I went with MIT on the mods I wrote myself. Theyre simple fixes, so they mostly just need my stamp of creation on them. SpectatorPlus stays GPL-3.0-or-later because that one is a direct update of hpfxd's original, and Im keeping their license and credit.

## Publication

Each mod once it goes through proper testing and creation is then published to modrinth and sometimes curseforge for the general public to use.

Here are some of the mods that are published and their respective links:

| Mod Name | Link |
|---|---|
| *(none live yet, links get added as each one goes up)* | |

# Modder's Insight

## Repo layout

```
mods/            source for each mod
releases/        newest jars after a local sync (not committed)
live-release/    newest jars that have been posted to modrinth/curseforge
assets/modrinth/ icons / art references
docs/            notes, md, readmes, etc.
scripts/         helpers
```

## Building/Contribution

The best most ideal way to get these is from modrinth once theyre up, otherwise drop the .jar into your profile's /mods folder.

To build from source youll need JDK 25+ for most of these right now.

```bat
cd mods\more-pearls
gradlew.bat build
```

```bat
powershell -File .\scripts\sync-releases.ps1
```

Anyone is welcome to propose changes or updates, bug fixes and tweaks can be done by a simple pull request and approval, new features or entirely new mods can go through myself before creation to determine their value.

Discord link for those who have further inquiry who'd like to communicate to myself about new ideas aforementioned:  
https://discord.gg/E562WQvzDt
