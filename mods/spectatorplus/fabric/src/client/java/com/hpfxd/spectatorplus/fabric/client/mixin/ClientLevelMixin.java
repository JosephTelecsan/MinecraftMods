package com.hpfxd.spectatorplus.fabric.client.mixin;

import com.hpfxd.spectatorplus.fabric.client.util.SpecUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private Int2ObjectMap<BlockDestructionProgress> destroyingBlocks;

    @Redirect(method = "getMarkerParticleTarget()Lnet/minecraft/world/level/block/Block;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;getPlayerMode()Lnet/minecraft/world/level/GameType;"))
    private GameType spectatorplus$useCameraGameModeForMarkerParticleCheck(MultiPlayerGameMode instance) {
        if (this.minecraft.getCameraEntity() instanceof Player player && player.isCreative()) {
            return GameType.CREATIVE;
        }

        return instance.getPlayerMode();
    }

    @Redirect(method = "getMarkerParticleTarget()Lnet/minecraft/world/level/block/Block;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack spectatorplus$useCameraForMarkerParticleCheck(LocalPlayer instance) {
        if (this.minecraft.getCameraEntity() instanceof LivingEntity livingEntity) {
            return livingEntity.getMainHandItem();
        }
        return ItemStack.EMPTY;
    }

    @Inject(method = "destroyBlockProgress(ILnet/minecraft/core/BlockPos;I)V", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;remove(I)Ljava/lang/Object;", remap = false))
    private void spectatorplus$resetAttackCooldownOnDigFinish(int breakerId, BlockPos pos, int progress, CallbackInfo ci) {
        if (progress == -1) {
            final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
            if (spectated != null && spectated.getId() == breakerId) {
                if (this.destroyingBlocks.containsKey(breakerId)) {
                    spectated.resetAttackStrengthTicker();
                }
            }
        }
    }
}
