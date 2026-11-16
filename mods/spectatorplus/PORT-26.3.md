# SpectatorPlus → 26.3

Jar: `fabric/build/libs/spectatorplus-fabric-1.2.3-local-mc26.3.jar`

MC 26.3, Fabric Loader ≥ 0.20.1, Fabric API `0.165.2+26.3`, Java 25.  
Paper module is still in the tree but not part of the Fabric build. License stays GPL; original authorship stays credited.

## Status

- Fabric compile + build works
- `runServer` / `runClient` load without mixin failures
- In-game spectate / inventory UI has been used in manhunt sessions; keep an eye out for edge cases

## API renames (1.21.4 → 26.3)

- `ResourceLocation` → `Identifier`
- `PayloadTypeRegistry.playC2S/playS2C` → `serverboundPlay/clientboundPlay`
- `END_WORLD_TICK` → `END_LEVEL_TICK`
- `Inventory.items/selected` → `getNonEquipmentItems()` / `getSelectedSlot()`
- `ClickType` → `ContainerInput`; `RenderType::guiTextured` → `RenderPipelines.GUI_TEXTURED`
- `GuiGraphics` → `GuiGraphicsExtractor`; HUD is `Hud`; screens via `mc.gui.screen()` / `setScreen`
- Keybinds via `KeyMappingHelper.registerKeyMapping`, `KeyMapping.Category.register(Identifier)`
- Access widener updated for official mappings

## Mixin notes

- `GuiMixin` → `HudMixin` (+ `ExperienceBarMixin`)
- Dropped old hand/screen-effect/level renderer mixins that no longer apply
- `LevelExtractorMixin` redirects first-person / fluid / fire / spectator hiding to the spectated player
- `GameRendererMixin` fakes non-spectator mode in `renderItemInHand` so arms show
- `SpectatorHands` tracks hand items / swap / bob for the target
- Server inject before `Inventory.setSelectedSlot`

## Inventory view (Fabric, no Paper)

While spectating, **E** opens a read-only inventory of the target. Slots sync while that screen is open. Host and spectator both need this jar.
