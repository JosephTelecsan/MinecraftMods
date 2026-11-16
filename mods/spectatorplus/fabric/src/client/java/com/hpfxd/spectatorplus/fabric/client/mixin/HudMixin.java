package com.hpfxd.spectatorplus.fabric.client.mixin;

import com.hpfxd.spectatorplus.fabric.client.SpectatorClientMod;
import com.hpfxd.spectatorplus.fabric.client.sync.ClientSyncController;
import com.hpfxd.spectatorplus.fabric.client.util.SpecUtil;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Hud.class)
public abstract class HudMixin {
    @Unique private static final String EXTRACT_CAMERA_OVERLAYS = "extractCameraOverlays(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V";
    @Unique private static final String EXTRACT_HOTBAR_AND_DECORATIONS = "extractHotbarAndDecorations(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V";
    @Unique private static final String EXTRACT_ITEM_HOTBAR = "extractItemHotbar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V";
    @Unique private static final String EXTRACT_CROSSHAIR = "extractCrosshair(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V";
    @Unique private static final String EXTRACT_PLAYER_HEALTH = "extractPlayerHealth(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V";
    @Unique private static final String EXTRACT_SELECTED_ITEM_NAME = "extractSelectedItemName(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V";

    @Shadow @Final private Minecraft minecraft;
    @Shadow public abstract SpectatorGui getSpectatorGui();
    @Shadow public abstract boolean isHidden();
    @Shadow protected abstract void extractItemHotbar(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker);
    @Shadow protected abstract void extractSelectedItemName(GuiGraphicsExtractor graphics);

    @Redirect(method = EXTRACT_CAMERA_OVERLAYS, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isScoping()Z"))
    private boolean spectatorplus$renderScoping(LocalPlayer instance) {
        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
        if (spectated != null) {
            return spectated.isScoping();
        }
        return instance.isScoping();
    }

    @Redirect(method = EXTRACT_CAMERA_OVERLAYS, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack spectatorplus$renderItemCameraOverlay(LocalPlayer instance, EquipmentSlot slot) {
        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
        if (spectated != null) {
            return spectated.getItemBySlot(slot);
        }
        return instance.getItemBySlot(slot);
    }

    @Redirect(method = EXTRACT_CAMERA_OVERLAYS, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getTicksFrozen()I"))
    private int spectatorplus$renderFreezeOverlay(LocalPlayer instance) {
        final var camera = this.minecraft.getCameraEntity();
        return camera != null ? camera.getTicksFrozen() : instance.getTicksFrozen();
    }

    @Redirect(method = EXTRACT_CAMERA_OVERLAYS, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getPercentFrozen()F"))
    private float spectatorplus$renderFreezeOverlayPercent(LocalPlayer instance) {
        final var camera = this.minecraft.getCameraEntity();
        return camera != null ? camera.getPercentFrozen() : instance.getPercentFrozen();
    }

    @Inject(method = EXTRACT_HOTBAR_AND_DECORATIONS, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/spectator/SpectatorGui;extractHotbar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"))
    private void spectatorplus$renderHotbar(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci, @Share("spectated") LocalRef<AbstractClientPlayer> spectatedRef) {
        if (!this.getSpectatorGui().isMenuActive() && !this.isHidden()) {
            final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
            spectatedRef.set(spectated);

            if (spectated != null) {
                if (!spectated.isSpectator()
                        && SpectatorClientMod.config.renderHotbar
                        && ClientSyncController.syncData != null
                        && ClientSyncController.syncData.playerId.equals(spectated.getUUID())) {
                    if (ClientSyncController.syncData.selectedHotbarSlot < 0) {
                        ClientSyncController.syncData.selectedHotbarSlot = 0;
                    }
                    this.extractItemHotbar(graphics, deltaTracker);
                }

                this.extractSelectedItemName(graphics);
            }
        }
    }

    @ModifyExpressionValue(method = EXTRACT_HOTBAR_AND_DECORATIONS, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;canHurtPlayer()Z"))
    private boolean spectatorplus$renderHealth(boolean original, @Share("spectated") LocalRef<AbstractClientPlayer> spectatedRef) {
        if (original) {
            return true;
        }

        final AbstractClientPlayer spectated = spectatedRef.get();
        return spectated != null && !spectated.isCreative() && !spectated.isSpectator() && this.spectatorplus$isStatusEnabled();
    }

    @Redirect(method = {EXTRACT_HOTBAR_AND_DECORATIONS, "nextContextualInfoState()Lnet/minecraft/client/gui/Hud$ContextualInfo;"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;hasExperience()Z"))
    private boolean spectatorplus$renderExperience(MultiPlayerGameMode instance) {
        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
        if (spectated != null) {
            return !spectated.isCreative() && !spectated.isSpectator() && ClientSyncController.syncData != null && ClientSyncController.syncData.experienceLevel != -1 && this.spectatorplus$isStatusEnabled();
        }

        return instance.hasExperience();
    }

    @Unique
    private boolean spectatorplus$isStatusEnabled() {
        if (!SpectatorClientMod.config.renderStatus) {
            return false;
        }

        return SpectatorClientMod.config.renderStatusIfNoHotbar || (ClientSyncController.syncData != null && ClientSyncController.syncData.selectedHotbarSlot != -1);
    }

    @Inject(method = "canRenderCrosshairForSpectator(Lnet/minecraft/world/phys/HitResult;)Z", at = @At(value = "HEAD"), cancellable = true)
    private void spectatorplus$renderCrosshair(HitResult rayTrace, CallbackInfoReturnable<Boolean> cir) {
        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
        if (spectated != null) {
            cir.setReturnValue(!spectated.isSpectator());
        }
    }

    @Redirect(method = {EXTRACT_CROSSHAIR, EXTRACT_ITEM_HOTBAR}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getAttackStrengthScale(F)F"))
    private float spectatorplus$fixCrosshairAttackStrength(LocalPlayer instance, float adjustTicks) {
        if (this.minecraft.getCameraEntity() instanceof Player player) {
            return player.getAttackStrengthScale(adjustTicks);
        }
        return instance.getAttackStrengthScale(adjustTicks);
    }

    @Redirect(method = EXTRACT_CROSSHAIR, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getCurrentItemAttackStrengthDelay()F"))
    private float spectatorplus$fixCrosshairCurrentItemAttackStrengthDelay(LocalPlayer instance) {
        if (this.minecraft.getCameraEntity() instanceof Player player) {
            return player.getCurrentItemAttackStrengthDelay();
        }
        return instance.getCurrentItemAttackStrengthDelay();
    }

    @Redirect(method = EXTRACT_SELECTED_ITEM_NAME, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;canHurtPlayer()Z"))
    private boolean spectatorplus$moveHeldItemTooltipUp(MultiPlayerGameMode instance) {
        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
        if (spectated != null && !spectated.isCreative() && !spectated.isSpectator()) {
            return true;
        }
        return instance.canHurtPlayer();
    }

    @ModifyConstant(method = EXTRACT_PLAYER_HEALTH, constant = @Constant(intValue = 39))
    private int spectatorplus$moveHealthDown(int constant) {
        if ((ClientSyncController.syncData == null || ClientSyncController.syncData.selectedHotbarSlot == -1) && SpecUtil.getCameraPlayer(this.minecraft) != null) {
            // hotbar sync data not present, shift health down
            return constant - 27;
        }
        return constant;
    }

    @WrapWithCondition(method = EXTRACT_PLAYER_HEALTH, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractFood(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;II)V"))
    private boolean spectatorplus$hideNonSyncedFood(Hud instance, GuiGraphicsExtractor graphics, Player player, int yLineBase, int xRight) {
        return (ClientSyncController.syncData != null && ClientSyncController.syncData.foodData != null) || SpecUtil.getCameraPlayer(this.minecraft) == null;
    }

    @ModifyExpressionValue(method = EXTRACT_ITEM_HOTBAR, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getSelectedSlot()I"))
    private int spectatorplus$showSyncedSelectedSlot(int original) {
        if (ClientSyncController.syncData != null && ClientSyncController.syncData.selectedHotbarSlot != -1 && SpecUtil.getCameraPlayer(this.minecraft) != null) {
            return ClientSyncController.syncData.selectedHotbarSlot;
        }
        return original;
    }

    @Redirect(method = "extractFood(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getFoodData()Lnet/minecraft/world/food/FoodData;"))
    private FoodData spectatorplus$showSyncedFood(Player instance) {
        if (ClientSyncController.syncData != null && ClientSyncController.syncData.foodData != null && SpecUtil.getCameraPlayer(this.minecraft) != null) {
            return ClientSyncController.syncData.foodData;
        }
        return instance.getFoodData();
    }

    @WrapOperation(method = EXTRACT_ITEM_HOTBAR, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getItem(I)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack spectatorplus$showSyncedItems(Inventory instance, int slot, Operation<ItemStack> original) {
        if (ClientSyncController.syncData != null && ClientSyncController.syncData.selectedHotbarSlot != -1 && SpecUtil.getCameraPlayer(this.minecraft) != null
                && slot >= 0 && slot < 9 && slot < ClientSyncController.syncData.hotbarItems.size()) {
            final ItemStack synced = ClientSyncController.syncData.hotbarItems.get(slot);
            return synced == null ? ItemStack.EMPTY : synced;
        }
        return original.call(instance, slot);
    }

    @Redirect(method = EXTRACT_ITEM_HOTBAR, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getOffhandItem()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack spectatorplus$showSyncedOffhand(Player instance) {
        if (ClientSyncController.syncData != null
                && ClientSyncController.syncData.selectedHotbarSlot != -1
                && SpecUtil.getCameraPlayer(this.minecraft) != null) {
            final ItemStack synced = ClientSyncController.getSyncedOffhand();
            if (synced != null) {
                return synced;
            }
        }
        return instance.getOffhandItem();
    }

    @Redirect(method = "extractHotbarAndDecorations(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;experienceLevel:I", opcode = Opcodes.GETFIELD))
    private int spectatorplus$showSyncedExperienceLevel(LocalPlayer instance) {
        if (ClientSyncController.syncData != null && ClientSyncController.syncData.experienceLevel != -1 && SpecUtil.getCameraPlayer(this.minecraft) != null) {
            return ClientSyncController.syncData.experienceLevel;
        }
        return instance.experienceLevel;
    }

    @ModifyReceiver(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getSelectedItem()Lnet/minecraft/world/item/ItemStack;"))
    private Inventory spectatorplus$modifyTooltipTick(Inventory instance) {
        if (this.minecraft.getCameraEntity() instanceof Player player) {
            return player.getInventory();
        }
        return instance;
    }
}
