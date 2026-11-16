package com.hpfxd.spectatorplus.fabric.client.mixin;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import com.hpfxd.spectatorplus.fabric.client.util.SpecUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

/**
 * The client-side pick (crosshair target) is calculated using the local player's interaction ranges and active item.
 * When spectating another player, use the spectated player's instead.
 */
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @ModifyExpressionValue(method = "raycastHitResult(FLnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/phys/HitResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;blockInteractionRange()D"))
    private double spectatorplus$modifyBlockInteractionRange(double original) {
        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(Minecraft.getInstance());
        if (spectated != null) {
            return spectated.blockInteractionRange();
        }
        return original;
    }

    @ModifyExpressionValue(method = "raycastHitResult(FLnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/phys/HitResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;entityInteractionRange()D"))
    private double spectatorplus$modifyEntityInteractionRange(double original) {
        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(Minecraft.getInstance());
        if (spectated != null) {
            return spectated.entityInteractionRange();
        }
        return original;
    }
}
