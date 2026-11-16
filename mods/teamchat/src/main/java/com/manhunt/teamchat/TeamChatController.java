package com.manhunt.teamchat;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.TeamColor;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TeamChatController {
	private static final Map<UUID, ChatMode> MODES = new ConcurrentHashMap<>();

	private TeamChatController() {
	}

	public static ChatMode getMode(ServerPlayer player) {
		return MODES.getOrDefault(player.getUUID(), ChatMode.GLOBAL);
	}

	public static void setMode(ServerPlayer player, ChatMode mode) {
		if (mode == ChatMode.GLOBAL) {
			MODES.remove(player.getUUID());
		} else {
			MODES.put(player.getUUID(), mode);
		}
	}

	public static void clearPlayer(ServerPlayer player) {
		MODES.remove(player.getUUID());
	}

	public static void clearAll() {
		MODES.clear();
	}

	public static boolean isOnManhuntTeam(ServerPlayer player, ChatMode mode) {
		if (!mode.isTeamChannel()) {
			return true;
		}
		PlayerTeam team = player.getTeam();
		return team != null && mode.teamName().equals(team.getName());
	}

	public static boolean isGamemaster(ServerPlayer player) {
		return Commands.LEVEL_GAMEMASTERS.check(player.permissions());
	}

	/**
	 * Who may SEND on a team channel. Visibility is separate: what you see is based
	 * only on Manhunt team (and op overhear), never on your selected send channel.
	 * Ops may send on either channel while spectating/officiating.
	 */
	public static boolean canUseChannel(ServerPlayer player, ChatMode mode) {
		if (!mode.isTeamChannel()) {
			return true;
		}
		return isOnManhuntTeam(player, mode) || isGamemaster(player);
	}

	/**
	 * Routes outbound chat by the sender's selected channel only.
	 * Global senders use vanilla broadcast (everyone still sees them, including
	 * players currently set to hunter/runner send mode).
	 */
	public static boolean handleChat(PlayerChatMessage message, ServerPlayer sender) {
		ChatMode mode = getMode(sender);
		if (!mode.isTeamChannel()) {
			return true;
		}

		if (!canUseChannel(sender, mode)) {
			setMode(sender, ChatMode.GLOBAL);
			sender.sendSystemMessage(
					Component.literal("You are not on the " + mode.displayName().toLowerCase()
									+ " team, so chat was switched to Global. "
									+ "Only " + mode.teamName() + " (or ops) can use that channel.")
							.withStyle(ChatFormatting.RED)
			);
			return true;
		}

		broadcastTeamMessage(sender, mode, message.decoratedContent());
		return false;
	}

	/**
	 * Prefer the live Manhunt scoreboard team color; fall back to Manhunt's
	 * {@code first_load} defaults (hunters=blue, runners=red).
	 */
	public static TextColor colorFor(MinecraftServer server, ChatMode mode) {
		if (mode.isTeamChannel() && server != null) {
			PlayerTeam team = server.getScoreboard().getPlayerTeam(mode.teamName());
			if (team != null) {
				Optional<TeamColor> teamColor = team.getColor();
				if (teamColor.isPresent()) {
					return teamColor.get().textColor();
				}
			}
		}
		return mode.fallbackTextColor();
	}

	private static void broadcastTeamMessage(ServerPlayer sender, ChatMode mode, Component content) {
		MinecraftServer server = sender.level().getServer();
		if (server == null) {
			return;
		}

		TextColor channelColor = colorFor(server, mode);
		MutableComponent formatted = Component.empty()
				.append(mode.channelPrefix(channelColor))
				.append(Component.literal(sender.getGameProfile().name()).withStyle(ChatFormatting.YELLOW))
				.append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
				.append(content.copy().withStyle(ChatFormatting.WHITE));

		for (ServerPlayer recipient : server.getPlayerList().getPlayers()) {
			if (shouldReceive(sender, recipient, mode)) {
				recipient.sendSystemMessage(formatted);
			}
		}
	}

	/**
	 * Team-channel visibility ignores the recipient's /chat mode.
	 * Hunters always see hunter-channel traffic; runners see runner; ops see both.
	 * Global traffic is never filtered here (vanilla delivers it to everyone).
	 */
	private static boolean shouldReceive(ServerPlayer sender, ServerPlayer recipient, ChatMode mode) {
		if (recipient.getUUID().equals(sender.getUUID())) {
			return true;
		}
		if (isOnManhuntTeam(recipient, mode)) {
			return true;
		}
		return isGamemaster(recipient);
	}

	/**
	 * Ensures Manhunt scoreboard teams exist so /team join works even before start_game.
	 * Does not overwrite colors if Manhunt already created the teams.
	 */
	public static void ensureTeams(MinecraftServer server) {
		Scoreboard scoreboard = server.getScoreboard();
		ensureTeam(scoreboard, ChatMode.HUNTER);
		ensureTeam(scoreboard, ChatMode.RUNNER);
	}

	private static void ensureTeam(Scoreboard scoreboard, ChatMode mode) {
		if (scoreboard.getPlayerTeam(mode.teamName()) != null) {
			return;
		}
		PlayerTeam team = scoreboard.addPlayerTeam(mode.teamName());
		TextColor color = mode.fallbackTextColor();
		team.setDisplayName(Component.literal(mode.displayName() + "s")
				.withStyle(style -> style.withColor(color)));
		if (mode.manhuntTeamColor() != null) {
			team.setColor(Optional.of(mode.manhuntTeamColor()));
		}
	}
}
