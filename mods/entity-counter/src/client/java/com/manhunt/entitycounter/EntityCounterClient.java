package com.manhunt.entitycounter;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * In 1.21.9+ / 26.x the old always-on F3 "E:" line became an optional debug entry
 * ({@code entity_render_stats}) that defaults to OFF.
 * <p>
 * Vanilla still computes the classic values via {@code LevelExtractor#entityStatistics()}:
 * {@code E: <rendered>/<total>, SD: <simulationDistance>} where rendered is entities
 * currently in the frustum / render pass and total is loaded entities.
 * <p>
 * This mod forces that entry into the F3 overlay so manhunt hosts get the old behavior
 * without opening F3+F6 every time.
 */
public class EntityCounterClient implements ClientModInitializer {
	public static final String MOD_ID = "manhunt-entity-counter";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		ClientLifecycleEvents.CLIENT_STARTED.register(EntityCounterClient::enableEntityCounter);
		LOGGER.info("Manhunt Entity Counter loaded - classic F3 E: rendered/total will stay enabled");
	}

	public static void enableEntityCounter(Minecraft client) {
		if (client.debugEntries == null) {
			return;
		}

		client.debugEntries.setStatus(DebugScreenEntries.ENTITY_RENDER_STATS, DebugScreenEntryStatus.IN_OVERLAY);
		client.debugEntries.save();
	}
}
