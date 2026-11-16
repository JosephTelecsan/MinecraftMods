package com.manhunt.morepearls;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Piglin bartering is changed by the datapack loot table shipped in this jar:
 * data/minecraft/loot_table/gameplay/piglin_bartering.json
 *
 * Vanilla 26.3: ~2.13% chance, 2-4 pearls
 * Classic 1.16 / 1.16.1: ~4.73% chance, 4-8 pearls
 * This mod: ~6.7% chance, 4-7 pearls
 */
public class ManhuntMorePearls implements ModInitializer {
	public static final String MOD_ID = "manhunt-more-pearls";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Manhunt More Pearls loaded - piglin pearl trades ~6.7% (4-7)");
	}
}
