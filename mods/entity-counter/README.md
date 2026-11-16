# Manhunt Entity Counter

Fabric **client** mod for Minecraft **26.3** that restores the classic F3 entity counter.

## What the old E counter was

Before the 1.21.9+ customizable debug overlay, F3 always showed something like:

```text
E: 12/104, SD: 12
```

| Value | Meaning |
|---|---|
| First number (`12`) | Entities currently rendered / in view (frustum) |
| Second number (`104`) | Total loaded entities |
| `SD` | Simulation distance |

That is exactly what vanilla still computes in `LevelExtractor#entityStatistics()` on 26.3. It was not deleted — it became optional entry `entity_render_stats`, **off by default**.

You can also enable it manually with **F3 + F6** → turn on `entity_render_stats`. This mod just keeps it on automatically for manhunt packs.

## Install

Drop into the instance `mods` folder (everyone who wants to see it):

```text
entity-counter/build/libs/manhunt-entity-counter-1.0.0.jar
```

Requires **Fabric API**. Client-side only (host Open to LAN does not need it for others to see their own F3).

## Build

```bat
gradlew.bat build
```
