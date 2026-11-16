# Modrinth notes

Target: Fabric, Minecraft **26.3**, Loader **0.20.1**, Java **25+**.

Build under `mods/<name>/`, then:

```bat
powershell -File .\scripts\sync-releases.ps1
```

Upload from `releases/`. Keep the Modrinth license field matching `fabric.mod.json` + `LICENSE`.

Author on Modrinth / in jars: **JosephTelecsan**.

Icons live in `assets/modrinth/`. Same general look: Minecraft pixel art, diagonal grass (top-left) / netherrack (bottom-right), simple center symbol. Team Chat icon: `assets/modrinth/team-chat/team-chat-icon.png`.

---

## Manhunt Team Chat

Live on Modrinth already — metadata can still be edited.

| | |
|---|---|
| Jar | `releases/manhunt-teamchat-1.0.5.jar` |
| ID | `manhunt-teamchat` |
| License | MIT (uploaded 1.0.5 may still say CC0 — update on next file) |
| Environment | Server-side only, works in singleplayer |
| Required | Fabric API |
| Optional | [Manhunt (Lprogrammer)](https://modrinth.com/datapack/manhunt_pack) |
| Categories | Social, Utility, Game Mechanics |

**Summary**

Team chat for manhunt. `/chat hunter`, `/chat runner`, `/chat global`.

**Description**

```text
Team chat for manhunt worlds. Use /chat hunter, /chat runner, or /chat global.

Routes chat through the hunters / runners scoreboard teams used by Lprogrammer’s Manhunt. Operators can read team channels when running the match. What you see depends on your team; /chat only changes where your messages go. Global chat still shows up while you’re in a team channel.

Works without Manhunt if those teams exist (the mod can create them). Not affiliated with Manhunt — just built to work with it.

Install on the world host. Vanilla clients are fine.
```

---

## Manhunt More Pearls

| | |
|---|---|
| Jar | `releases/manhunt-more-pearls-1.1.2.jar` |
| ID | `manhunt-more-pearls` |
| License | MIT |
| Environment | Server-side only, works in singleplayer |
| Required | — |
| Categories | Game Mechanics, Utility |

**Summary**

Piglin pearl barters at ~6.7%, stacks 4–7 (a bit above classic 1.16 rates).

**Description**

```text
Tweaks piglin bartering ender pearls for manhunt / speedrun-style worlds on 26.3.

- Vanilla 26.3: ~2.13%, stacks 2–4
- Classic 1.16 / 1.16.1: ~4.73%, stacks 4–8
- This mod: ~6.7%, stacks 4–7

Only the ender pearl entry in the piglin bartering loot table changes.

Install on the host / server. LAN clients don’t need it.
```

---

## Manhunt Pause Game

| | |
|---|---|
| Jar | `releases/manhunt-pausegame-1.0.1.jar` |
| ID | `manhunt-pausegame` |
| License | MIT |
| Environment | Server-side only, works in singleplayer |
| Required | Fabric API |
| Categories | Utility, Management |

**Summary**

`/pausegame` pauses the match for a break; the host who ran the command can still move.

**Description**

```text
/pausegame for agreed breaks and pauses on 26.3.

While paused:
- world / mobs / time pause
- other players stay paused
- the operator who paused can still move (to fix issues / talk)
- if that player disconnects, the game resumes

/pausegame
/pausegame on
/pausegame off
/pausegame status

Needs operator permission. Install on the host. Vanilla clients OK.
```

---

## Manhunt Entity Counter

| | |
|---|---|
| Jar | `releases/manhunt-entity-counter-1.0.1.jar` |
| ID | `manhunt-entity-counter` |
| License | MIT |
| Environment | Client-side only |
| Required | Fabric API |
| Categories | Utility, Cosmetic |

**Summary**

Brings back the classic F3 entity counter on 26.3.

**Description**

```text
Turns the classic F3 entity counter back on for 26.3 (E: rendered/total style stats).

Client-side only. Put it on whoever wants the readout. Server doesn’t need it.
```

---

## SpectatorPlus (26.3 port)

| | |
|---|---|
| Jar | `releases/spectatorplus-fabric-1.2.3-local-mc26.3.jar` |
| License | GPL-3.0-or-later (keep this — upstream license) |
| Environment | Client and server |
| Notes | Port of hpfxd’s SpectatorPlus. Credit them. Don’t relicense as MIT. |

**Summary**

SpectatorPlus on 26.3 — open the target’s inventory while spectating.

**Description**

```text
Unofficial Fabric port of SpectatorPlus to Minecraft 26.3.

For hosts/spectators in a match: view the spectated player’s hotbar and inventory (E) with sync fixes.

Based on SpectatorPlus by hpfxd. GPL-3.0-or-later. Not an official release from the original author.
```

---

## Icon prompts

Same base style as the Team Chat icon. Drop `assets/modrinth/team-chat/team-chat-icon.png` in as a reference when generating new ones.

**Base**

```text
Minecraft Java Edition default texture pack pixel art mod icon, square 1:1, chunky visible pixels like a 32x32 item icon upscaled cleanly, flat 2D no blur no anti-aliasing, no text, no watermark, no UI chrome, high contrast. Background split diagonally bottom-left to top-right by a thick jagged black pixel line: upper-left is Minecraft grass block top texture (lime and forest green speckles), lower-right is Minecraft netherrack texture (red, maroon, dark brown speckles). Centered simple white/light-cyan pixel symbol with a thin darker outline.
```

**More Pearls** — ender pearl + tiny gold accent  
**Pause Game** — two-bar pause glyph, optional tiny frozen clock behind it  
**Entity Counter** — small HUD panel + mob silhouette, no readable text  
**SpectatorPlus** — spyglass / eye lens, optional faint inventory slots behind it  

---

## Upload order I’m using

1. Team Chat (done)
2. More Pearls
3. Pause Game
4. Entity Counter
5. SpectatorPlus last (GPL / port credit)
