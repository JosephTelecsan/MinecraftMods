package com.manhunt.pausegame.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.manhunt.pausegame.PauseController;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TickRateManager.class)
public abstract class TickRateManagerMixin {
	/**
	 * Vanilla /tick freeze still ticks all players. During pause, keep only the
	 * operator who paused (and their vehicle) moving.
	 */
	@ModifyReturnValue(method = "isEntityFrozen(Lnet/minecraft/world/entity/Entity;)Z", at = @At("RETURN"))
	private boolean pausegame$freezeNonExemptPlayers(boolean original, Entity entity) {
		if (!PauseController.isPaused() || entity == null) {
			return original;
		}

		try {
			if (entity instanceof Player player) {
				return PauseController.shouldFreeze(player);
			}

			if (PauseController.hasExemptPassenger(entity)) {
				return false;
			}

			return true;
		} catch (RuntimeException e) {
			return original || PauseController.isPaused();
		}
	}
}
