# SpectatorPlus Fabric port: Minecraft 1.21.4 -> 26.3

Built jar: `fabric/build/libs/spectatorplus-fabric-1.2.3-local-mc26.3.jar`

Targets MC 26.3 (unobfuscated), Fabric Loader >= 0.20.1, Fabric API 0.165.2+26.3, Java 25.
The Paper module is untouched (still excluded from the Gradle build). GPL license and authorship are unchanged.

## Verification
- `:spectatorplus-fabric:compileJava`, `compileClientJava` and `build` succeed.
- `runServer`: mod loads, all server mixins apply, server reaches "Done".
- `runClient`: every mixin target class (client and common) loads with all injectors matching
  (`defaultRequire: 1`). Client reached initialization with no mixin errors.
- NOT verified: visual/in-game behavior (GUI, hands, HUD sync) with a real spectating session.

## API renames
- `ResourceLocation` -> `Identifier`
- `PayloadTypeRegistry.playC2S/playS2C` -> `serverboundPlay/clientboundPlay`
- `END_WORLD_TICK` -> `END_LEVEL_TICK`
- `Inventory.items/selected` -> `getNonEquipmentItems()` / `getSelectedSlot()`
- `ClickType` -> `ContainerInput`; `RenderType::guiTextured` -> `RenderPipelines.GUI_TEXTURED`
- `GuiGraphics` -> `GuiGraphicsExtractor`; HUD is now `Hud` (extract* methods); screens via `mc.gui.screen()/setScreen`
- Keybinds: `KeyMappingHelper.registerKeyMapping`, `KeyMapping.Category.register(Identifier)`
  (lang key `key.category.spectatorplus.main`), SDL-style `InputConstants.KEY_*`
- Access widener updated (`HandRenderSelection`, official namespace)

## Mixin changes
- `GuiMixin` -> `HudMixin` (+ new `ExperienceBarMixin`).
- Removed: `ItemInHandRendererMixin/Accessor`, `ScreenEffectRendererMixin`, `LevelRendererMixin/Accessor`,
  `GameRendererAccessor` (rendering became state-based).
- New `LevelExtractorMixin`: redirects avatar/first-person-hands render state, eye-in-fluid, on-fire,
  view blocking and hidden other spectators to the spectated player.
- `GameRendererMixin`: fakes non-spectator game mode in `renderItemInHand` so arms render.
- New client `SpectatorHands` tracker (hand items, swap/equip height, bob) for the spectated player.
- New `LocalPlayerMixin/Accessor` (interaction ranges, `pick`), `ClientLevelMixin` (dig-finish cooldown reset).
- Server: `ServerGamePacketListenerImplMixin` now injects before `Inventory.setSelectedSlot`.

## Behavior caveats
- Spectated-player view bobbing now uses vanilla `ClientAvatarState` (old custom bob code removed).
- Item swap animation uses the vanilla item swap scale instead of attack-strength scale.
- Use-item animation reset is not handled.

## Fabric inventory viewing (no Paper)
Implemented server handlers for `ServerboundRequestInventoryOpenPacket` / inventory sync.
While spectating a player, press the normal **Inventory** key (`E` by default) to open a read-only view of their inventory.
Live slot updates sync while that view is open. Host must have this jar; spectator client needs SpectatorPlus too.
