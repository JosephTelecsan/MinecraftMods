package com.hpfxd.spectatorplus.fabric.client.mixin;

import com.hpfxd.spectatorplus.fabric.client.SpectatorClientMod;
import com.hpfxd.spectatorplus.fabric.client.SpectatorHands;
import com.hpfxd.spectatorplus.fabric.client.util.SpecUtil;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.FirstPersonHandsAndItems;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LevelExtractor.class)
public abstract class LevelExtractorMixin {
    @Shadow @Final private Minecraft minecraft;

    private static final String EXTRACT_PLAYER_STATE = "extractPlayerState(Lnet/minecraft/client/Camera;Lnet/minecraft/client/DeltaTracker;FLnet/minecraft/client/renderer/state/level/PlayerRenderState;)V";

    /**
     * Use the spectated player to extract the avatar render state (skin, arm, item use state, invisibility, light...)
     * that is used for rendering the first person hands, instead of the local player.
     */
    @ModifyArg(
            method = EXTRACT_PLAYER_STATE,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/extract/LevelExtractor;extractEntity(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;"),
            index = 0
    )
    private Entity spectatorplus$extractSpectatedAvatar(Entity player) {
        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
        if (spectated != null) {
            return spectated;
        }
        return player;
    }

    /**
     * Extract the first person hands and items for the spectated player instead of the local player.
     */
    @WrapOperation(
            method = EXTRACT_PLAYER_STATE,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/FirstPersonHandsAndItems;extractRenderState(Lnet/minecraft/client/player/LocalPlayer;FLnet/minecraft/client/renderer/state/level/FirstPersonHandsAndItemsRenderState;)V")
    )
    private void spectatorplus$extractSpectatedHands(FirstPersonHandsAndItems instance, LocalPlayer player, float partialTicks, FirstPersonHandsAndItemsRenderState state, Operation<Void> original) {
        if (SpectatorClientMod.config.renderArms) {
            final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
            if (spectated != null && !spectated.isSpectator()) {
                SpectatorHands.get().extractRenderState(this.minecraft, spectated, partialTicks, state);
                return;
            }
        }

        original.call(instance, player, partialTicks, state);
    }

    // Redirect calls from mc.player to the current camera entity

    @ModifyExpressionValue(
            method = EXTRACT_PLAYER_STATE,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z")
    )
    private boolean spectatorplus$modifyIsEyeInFluid(boolean original) {
        final Entity camera = this.minecraft.getCameraEntity();
        return camera != null ? camera.isEyeInFluid(FluidTags.WATER) : original;
    }

    @ModifyExpressionValue(
            method = EXTRACT_PLAYER_STATE,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isOnFire()Z")
    )
    private boolean spectatorplus$modifyIsOnFire(boolean original) {
        final Entity camera = this.minecraft.getCameraEntity();
        return camera != null ? camera.isOnFire() : original;
    }

    @ModifyExpressionValue(
            method = "getViewBlockingState(Lnet/minecraft/client/player/LocalPlayer;Lnet/minecraft/client/renderer/culling/Frustum;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;noPhysics:Z")
    )
    private static boolean spectatorplus$allowViewBlockingWhenSpectatingPlayer(boolean original) {
        // The local player has noPhysics while spectating, which would prevent the view blocking overlay from ever
        // rendering. Allow it if the camera is a player that is not spectating themselves.
        if (original) {
            final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(Minecraft.getInstance());
            return spectated == null || spectated.isSpectator();
        }
        return false;
    }

    /**
     * Hide other spectators from the spectator's view, if configured.
     */
    @ModifyExpressionValue(
            method = "extractVisibleEntities(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;Lnet/minecraft/client/DeltaTracker;Lnet/minecraft/client/renderer/state/level/LevelRenderState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/extract/LevelExtractor;isEntityVisible(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/culling/Frustum;DDDFJ)Z")
    )
    private boolean spectatorplus$hideOtherSpectators(boolean original, @Local Entity entity) {
        if (original && !SpectatorClientMod.config.showSpectators && this.minecraft.player != null && this.minecraft.player.isSpectator()
                && entity instanceof final RemotePlayer player && player.isSpectator()) {
            return false;
        }
        return original;
    }
}
