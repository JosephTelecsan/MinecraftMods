package com.manhunt.pausegame;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManhuntPauseGame implements ModInitializer {
	public static final String MOD_ID = "manhunt-pausegame";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				PauseCommands.register(dispatcher));

		ServerTickEvents.END_SERVER_TICK.register(PauseController::tick);

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			try {
				PauseController.onPlayerJoin(handler.getPlayer());
			} catch (RuntimeException e) {
				LOGGER.warn("Pause join hook failed", e);
			}
		});

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			try {
				PauseController.onPlayerLeave(handler.getPlayer());
			} catch (RuntimeException e) {
				LOGGER.warn("Pause leave hook failed", e);
			}
		});

		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			try {
				PauseController.onServerStopping();
			} catch (RuntimeException e) {
				LOGGER.warn("Pause shutdown hook failed", e);
				PauseController.clear();
			}
		});

		LOGGER.info("Manhunt Pause Game loaded — /pausegame");
	}
}
