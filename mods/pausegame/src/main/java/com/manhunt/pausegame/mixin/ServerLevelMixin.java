package com.manhunt.pausegame.mixin;

import com.manhunt.pausegame.PauseController;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Belt-and-suspenders: vanilla tick-freeze already stops most level progression, but sleep-based
 * wakeups can still jump clocks outside the {@code runsNormally()} guard. Cancel those while paused.
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
	@Inject(method = "wakeUpAllPlayers()V", at = @At("HEAD"), cancellable = true)
	private void pausegame$noSleepSkipWhilePaused(CallbackInfo ci) {
		if (PauseController.isPaused()) {
			ci.cancel();
		}
	}

	@Inject(method = "tickTime()V", at = @At("HEAD"), cancellable = true)
	private void pausegame$noGameTimeWhilePaused(CallbackInfo ci) {
		if (PauseController.isPaused()) {
			ci.cancel();
		}
	}

	@Inject(method = "advanceWeatherCycle()V", at = @At("HEAD"), cancellable = true)
	private void pausegame$noWeatherWhilePaused(CallbackInfo ci) {
		if (PauseController.isPaused()) {
			ci.cancel();
		}
	}

	@Inject(method = "tickCustomSpawners(Z)V", at = @At("HEAD"), cancellable = true)
	private void pausegame$noSpawnersWhilePaused(boolean spawnEnemies, CallbackInfo ci) {
		if (PauseController.isPaused()) {
			ci.cancel();
		}
	}
}
