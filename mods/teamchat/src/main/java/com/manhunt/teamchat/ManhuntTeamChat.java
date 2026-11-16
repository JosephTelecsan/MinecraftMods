package com.manhunt.teamchat;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManhuntTeamChat implements ModInitializer {
	public static final String MOD_ID = "manhunt-teamchat";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				TeamChatCommands.register(dispatcher));

		ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
			try {
				return TeamChatController.handleChat(message, sender);
			} catch (RuntimeException e) {
				LOGGER.warn("Team chat routing failed for {}", sender.getGameProfile().name(), e);
				return true;
			}
		});

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
				TeamChatController.clearPlayer(handler.getPlayer()));

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			try {
				TeamChatController.ensureTeams(server);
			} catch (RuntimeException e) {
				LOGGER.warn("Could not ensure Manhunt chat teams", e);
			}
		});

		ServerLifecycleEvents.SERVER_STOPPING.register(server -> TeamChatController.clearAll());

		LOGGER.info("Manhunt Team Chat loaded - /chat hunter | /chat runner | /chat global");
	}
}
