package com.hpfxd.spectatorplus.fabric.client.mixin;

import com.hpfxd.spectatorplus.fabric.client.SpectatorClientMod;
import com.hpfxd.spectatorplus.fabric.client.util.SpecUtil;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Hand rendering in 26.x is entirely driven by render state extracted by the {@code LevelExtractor}, see
 * {@link LevelExtractorMixin} and {@link com.hpfxd.spectatorplus.fabric.client.SpectatorHands}. The only thing left to
 * do here is to let vanilla render the hands even though the local player is in spectator mode.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow @Final private Minecraft minecraft;

    @ModifyExpressionValue(
            method = "renderItemInHand(Lnet/minecraft/client/renderer/state/level/CameraRenderState;Lnet/minecraft/client/renderer/state/level/PlayerRenderState;Lcom/mojang/renderpearl/api/textures/GpuTextureView;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;getPlayerMode()Lnet/minecraft/world/level/GameType;")
    )
    private GameType spectatorplus$renderArmsWhileSpectating(GameType original) {
        if (SpectatorClientMod.config.renderArms && original == GameType.SPECTATOR) {
            final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
            if (spectated != null && !spectated.isSpectator()) {
                // pretend we're not in spectator mode so that the first person hands are rendered
                return GameType.SURVIVAL;
            }
        }

        return original;
    }
}
