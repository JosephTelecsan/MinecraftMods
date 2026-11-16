# scripts

Helpers for keeping the repo's jar folders in order.

## sync-releases.ps1

Copies the newest built jar for each mod into `releases/` and removes older jars of the same mod from that folder.

Run from the repo root after building:

```bat
powershell -File .\scripts\sync-releases.ps1
```

Mods it watches right now: more-pearls, teamchat, pausegame, entity-counter, spectatorplus. It skips a mod if no built jar is found under that mod's `build/libs`.
