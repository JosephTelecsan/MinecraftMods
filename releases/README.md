# releases

This folder holds the newest built jar for each mod after a local sync. I use it as the staging spot before anything goes up on Modrinth or CurseForge.

Build the mod under `mods/<name>/`, then from the repo root:

```bat
powershell -File .\scripts\sync-releases.ps1
```

That script copies the latest jar per mod into here and drops older jars of the same mod so the folder stays clean. The jars themselves are gitignored on purpose. Source lives in `mods/`, this is just the ready-to-upload copies.

When a build has been tested and posted publicly, that jar moves into `live-release/` as the record of what actually went live.
