# Modrinth publishing guide

Single source of truth for uploading / updating our Fabric 26.3 manhunt mods on Modrinth.

**Loader:** Fabric  
**Minecraft:** 26.3  
**Fabric Loader:** 0.20.1  
**Java:** 25+  
**Latest jars:** `releases/` (sync with `.\scripts\sync-releases.ps1` after builds)  
**Mod sources:** `mods/<name>/`  
**Icons / references:** `assets/modrinth/`

Always match the license in Modrinth UI with `fabric.mod.json` and the jar’s `LICENSE` file.

---

## Shared author / branding

| Field | Value |
|---|---|
| Author display | Manhunt Officiator |
| Theme | Manhunt / Open-to-LAN utility mods for Minecraft 26.3 |
| Icon style | Minecraft default-texture pixel art; diagonal grass (upper-left) / netherrack (lower-right) split; chunky center glyph |

Reference icon used for Team Chat: `assets/modrinth/team-chat/team-chat-icon.png`  
(Variants in the same folder: bandage, compass, photo.)

---

## 1. Manhunt Team Chat *(already submitted — can still update)*

| Field | Value |
|---|---|
| Jar | `releases/manhunt-teamchat-1.0.5.jar` |
| Mod ID | `manhunt-teamchat` |
| Version | 1.0.5 |
| License | Currently **CC0-1.0** in the submitted jar — prefer updating to **MIT** on next upload (match UI + jar) |
| Side | **Server-side only** + **Works in singleplayer too** |
| Required deps | Fabric API |
| Optional deps | [Lprogrammer’s Manhunt](https://modrinth.com/datapack/manhunt_pack) (soft companion) |
| Categories | Social / Utility / Game Mechanics |

### Summary
Team chat channels for manhunt worlds. `/chat hunter`, `/chat runner`, or `/chat global`.

### Description (paste)
```text
Team chat for manhunt worlds. Use /chat hunter, /chat runner, or /chat global.

Messages go to Manhunt’s hunters / runners scoreboard teams (from Lprogrammer’s Manhunt). Ops can overhear both channels. Visibility is based on team membership; /chat only changes where your messages are sent. You still see global chat while in a team channel.

Works without Manhunt if the hunters/runners teams exist (the mod can create them). Not affiliated with Manhunt — built to work with it.

Host/server install only; vanilla clients can join.
```

### Icon prompt (Nano Banana)
```text
Minecraft Java Edition default texture pack pixel art mod icon, square 1:1, chunky visible pixels like a 32x32 item icon upscaled cleanly, flat 2D no blur no anti-aliasing, no text, no watermark, no UI chrome, high contrast. Background split diagonally bottom-left to top-right by a thick jagged black pixel line: upper-left is Minecraft grass block top texture (lime and forest green speckles), lower-right is Minecraft netherrack texture (red, maroon, dark brown speckles). Centered simple white/light-cyan pixel speech bubble with two cyan text bars inside and a thin darker outline.
```
Attach `assets/modrinth/team-chat/team-chat-icon.png` as style reference when regenerating.

### Update checklist
- [ ] Optionally rebuild with MIT license (currently CC0 in 1.0.5)
- [ ] Confirm optional Manhunt dependency link
- [ ] Icon / gallery if needed
- [ ] Re-upload or edit project metadata

---

## 2. Manhunt More Pearls

| Field | Value |
|---|---|
| Jar | `releases/manhunt-more-pearls-1.1.2.jar` |
| Mod ID | `manhunt-more-pearls` |
| Version | 1.1.2 |
| License | **MIT** |
| Side | **Server-side only** + **Works in singleplayer too** |
| Required deps | None (Fabric Loader only) |
| Optional deps | None |
| Categories | Game Mechanics / Utility |

### Summary
Tunes piglin bartering ender pearls to ~6.7% with stacks of 4–7 (above classic 1.16 rates). Host-only for Open to LAN manhunts.

### Description (paste)
```text
Buffs piglin bartering ender pearls for manhunt / speedrun-style worlds on Minecraft 26.3.

Rates:
- Vanilla 26.3: ~2.13%, stacks 2–4
- Classic 1.16 / 1.16.1: ~4.73%, stacks 4–8
- This mod: ~6.7%, stacks 4–7

Only the ender pearl entry in minecraft:gameplay/piglin_bartering is changed. Other barter loot is unchanged.

Install on the world host / server only. Clients joining LAN do not need the mod.
```

### Icon prompt (Nano Banana)
```text
Minecraft Java Edition default texture pack pixel art mod icon, square 1:1, chunky visible pixels like a 32x32 item icon upscaled cleanly, flat 2D no blur no anti-aliasing, no text, no watermark, no UI chrome, high contrast. Background split diagonally bottom-left to top-right by a thick jagged black pixel line: upper-left is Minecraft grass block top texture (lime and forest green speckles), lower-right is Minecraft netherrack texture (red, maroon, dark brown speckles). Centered classic Minecraft ender pearl item, glossy teal-green sphere with a bright white specular highlight and darker teal rim, slightly overlapping the diagonal split. Tiny gold nugget or gold ingot pixel accent at the bottom-left of the pearl to hint at piglin bartering. Keep the pearl large and readable.
```
Style note: *Match `assets/modrinth/team-chat/team-chat-icon.png` pixel density and diagonal grass/netherrack look; only change the center symbol.*

---

## 3. Manhunt Pause Game

| Field | Value |
|---|---|
| Jar | `releases/manhunt-pausegame-1.0.1.jar` |
| Mod ID | `manhunt-pausegame` |
| Version | 1.0.1 |
| License | **MIT** |
| Side | **Server-side only** + **Works in singleplayer too** |
| Required deps | Fabric API |
| Optional deps | None |
| Categories | Utility / Management |

### Summary
`/pausegame` freezes the world, mobs, time, and other players — executor stays free to move. For manhunt breaks.

### Description (paste)
```text
Adds /pausegame for manhunt breaks and technical pauses on Minecraft 26.3.

When paused:
- World / mob / time progression freezes
- Other players are locked
- The executor can still move and act
- Disconnecting the pause executor auto-unpauses (anti-softlock)

Commands:
- /pausegame — toggle
- /pausegame on
- /pausegame off
- /pausegame status

Requires operator / gamemaster permission. Install on host/server; vanilla clients can join.
```

### Icon prompt (Nano Banana)
```text
Minecraft Java Edition default texture pack pixel art mod icon, square 1:1, chunky visible pixels like a 32x32 item icon upscaled cleanly, flat 2D no blur no anti-aliasing, no text, no watermark, no UI chrome, high contrast. Background split diagonally bottom-left to top-right by a thick jagged black pixel line: upper-left is Minecraft grass block top texture (lime and forest green speckles), lower-right is Minecraft netherrack texture (red, maroon, dark brown speckles). Centered big Minecraft-style pause glyph — two thick vertical white bars like a media pause button, light cyan edge highlight. Behind the bars, a small frozen clock or sundial of pale stone/gold pixels barely peeking out. Feels like time stopped mid-manhunt. No play triangle.
```

---

## 4. Manhunt Entity Counter

| Field | Value |
|---|---|
| Jar | `releases/manhunt-entity-counter-1.0.1.jar` |
| Mod ID | `manhunt-entity-counter` |
| Version | 1.0.1 |
| License | **MIT** |
| Side | **Client-side only** |
| Required deps | Fabric API |
| Optional deps | None |
| Categories | Utility / Cosmetic |

### Summary
Restores the classic F3 entity counter (`E:` / `SD:`) on Minecraft 26.3.

### Description (paste)
```text
Restores the classic F3 debug entity counter on Minecraft 26.3 (E: rendered/total, SD: simulation distance style stats) by keeping entity render stats enabled.

Client-side only. Install on each player who wants the F3 readout. Not required on the server.
```

### Icon prompt (Nano Banana)
```text
Minecraft Java Edition default texture pack pixel art mod icon, square 1:1, chunky visible pixels like a 32x32 item icon upscaled cleanly, flat 2D no blur no anti-aliasing, no text, no watermark, no UI chrome, high contrast. Background split diagonally bottom-left to top-right by a thick jagged black pixel line: upper-left is Minecraft grass block top texture (lime and forest green speckles), lower-right is Minecraft netherrack texture (red, maroon, dark brown speckles). Centered Minecraft-style debug/F3 vibe icon — small white square monitor/HUD panel with cyan pixel readout bars inside (tiny graph lines), and a simple blocky zombie or generic mob silhouette peeking from behind in dark gray/green pixels. Suggest entity counting without readable letters or numbers.
```

---

## 5. SpectatorPlus (26.3 port) — publish carefully

| Field | Value |
|---|---|
| Jar | `releases/spectatorplus-fabric-1.2.3-local-mc26.3.jar` |
| Mod ID | (from upstream / fabric.mod.json) |
| Version | 1.2.3-local-mc26.3 |
| License | **GPL-3.0-or-later** (upstream) — do **not** publish as MIT |
| Side | Client and server (spectate inventory sync needs both sides as upstream expects) |
| Notes | Port/derivative of hpfxd SpectatorPlus. Credit original author. Keep GPL. |

### Summary
Minecraft 26.3 Fabric port of SpectatorPlus — spectator inventory view (E) and related sync hardening for manhunt officiating.

### Description (paste)
```text
Unofficial Fabric port of SpectatorPlus to Minecraft 26.3 for manhunt officiating.

Lets spectators open the target’s inventory (E) with hotbar/offhand/hand sync improvements and crash hardening.

This is a derivative of SpectatorPlus by hpfxd. Licensed GPL-3.0-or-later. Not affiliated with the original author unless they endorse it.
```

### Icon prompt (Nano Banana)
```text
Minecraft Java Edition default texture pack pixel art mod icon, square 1:1, chunky visible pixels like a 32x32 item icon upscaled cleanly, flat 2D no blur no anti-aliasing, no text, no watermark, no UI chrome, high contrast. Background split diagonally bottom-left to top-right by a thick jagged black pixel line: upper-left is Minecraft grass block top texture (lime and forest green speckles), lower-right is Minecraft netherrack texture (red, maroon, dark brown speckles). Centered Minecraft spyglass / spectator eye hybrid — circular cyan glass lens with dark pupil, iron/gray rim, white highlight. Optional tiny 2x2 dull gray inventory slots faintly behind the lens. Keep the lens dominant.
```

---

## Nano Banana — shared style lock

Paste this before any custom center-subject paragraph, or attach `assets/modrinth/team-chat/team-chat-icon.png` and say:

> Match this exact pixel density, diagonal grass/netherrack background, and flat Minecraft icon look; only change the center symbol.

```text
Minecraft Java Edition default texture pack pixel art mod icon, square 1:1, chunky visible pixels like a 32x32 item icon upscaled cleanly, flat 2D no blur no anti-aliasing, no text, no watermark, no UI chrome, high contrast. Background split diagonally bottom-left to top-right by a thick jagged black pixel line: upper-left is Minecraft grass block top texture (lime and forest green speckles), lower-right is Minecraft netherrack texture (red, maroon, dark brown speckles). Centered simple white/light-cyan pixel symbol with a thin darker outline, same vibe as a manhunt team-chat speech-bubble icon but a different glyph.
```

---

## Release sync workflow

1. Build the mod (`gradlew.bat build` inside `mods/<name>/`).
2. From repo root:
   ```bat
   powershell -ExecutionPolicy Bypass -File .\scripts\sync-releases.ps1
   ```
3. Upload the jar from `releases/` (old versions of the same mod are removed automatically).

---

## Suggested upload order

1. Manhunt Team Chat — *(submitted; update metadata/license/icon as needed)*
2. Manhunt More Pearls
3. Manhunt Pause Game
4. Manhunt Entity Counter
5. SpectatorPlus — last (GPL / derivative diligence)

---

## License quick reference

| Mod | Publish as |
|---|---|
| Team Chat | MIT preferred (update from CC0 if/when you republish) |
| More Pearls | MIT |
| Pause Game | MIT |
| Entity Counter | MIT |
| SpectatorPlus | GPL-3.0-or-later |
