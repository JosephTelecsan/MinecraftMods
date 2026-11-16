package com.hpfxd.spectatorplus.fabric.client.mixin;

import com.hpfxd.spectatorplus.fabric.client.sync.ClientSyncController;
import com.hpfxd.spectatorplus.fabric.client.util.SpecUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.contextualbar.ExperienceBar;
import net.minecraft.client.player.LocalPlayer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ExperienceBar.class)
public abstract class ExperienceBarMixin {
    @Shadow @Final private Minecraft minecraft;

    @Redirect(method = "extractBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getXpNeededForNextLevel()I"))
    private int spectatorplus$showSyncedExperienceBar(LocalPlayer instance) {
        if (ClientSyncController.syncData != null && ClientSyncController.syncData.experienceLevel != -1 && SpecUtil.getCameraPlayer(this.minecraft) != null) {
            return ClientSyncController.syncData.experienceNeededForNextLevel;
        }
        return instance.getXpNeededForNextLevel();
    }

    @Redirect(method = "extractBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;experienceProgress:F", opcode = Opcodes.GETFIELD))
    private float spectatorplus$showSyncedExperienceProgress(LocalPlayer instance) {
        if (ClientSyncController.syncData != null && ClientSyncController.syncData.experienceLevel != -1 && SpecUtil.getCameraPlayer(this.minecraft) != null) {
            return ClientSyncController.syncData.experienceProgress;
        }
        return instance.experienceProgress;
    }
}
