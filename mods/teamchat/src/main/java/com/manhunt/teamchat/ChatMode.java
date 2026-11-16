package com.manhunt.teamchat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.scores.TeamColor;

/**
 * Send channels. Fallback colors match Lprogrammer Manhunt {@code first_load}:
 * {@code team modify hunters color blue} / {@code team modify runners color red}.
 * Live chat prefixes prefer the scoreboard team's actual color when present.
 */
public enum ChatMode {
	GLOBAL("global", null, "Global", null, ChatFormatting.WHITE),
	HUNTER("hunter", "hunters", "Hunter", TeamColor.BLUE, ChatFormatting.BLUE),
	RUNNER("runner", "runners", "Runner", TeamColor.RED, ChatFormatting.RED);

	private final String commandName;
	private final String teamName;
	private final String displayName;
	private final TeamColor manhuntTeamColor;
	private final ChatFormatting fallbackFormatting;

	ChatMode(
			String commandName,
			String teamName,
			String displayName,
			TeamColor manhuntTeamColor,
			ChatFormatting fallbackFormatting
	) {
		this.commandName = commandName;
		this.teamName = teamName;
		this.displayName = displayName;
		this.manhuntTeamColor = manhuntTeamColor;
		this.fallbackFormatting = fallbackFormatting;
	}

	public String commandName() {
		return commandName;
	}

	/** Scoreboard team id from Lprogrammer Manhunt (`hunters` / `runners`), or null for global. */
	public String teamName() {
		return teamName;
	}

	public String displayName() {
		return displayName;
	}

	/** Manhunt's default scoreboard team color (used if the team has no color set yet). */
	public TeamColor manhuntTeamColor() {
		return manhuntTeamColor;
	}

	public ChatFormatting color() {
		return fallbackFormatting;
	}

	public boolean isTeamChannel() {
		return teamName != null;
	}

	public TextColor fallbackTextColor() {
		if (manhuntTeamColor != null) {
			return manhuntTeamColor.textColor();
		}
		return TextColor.fromLegacyFormat(fallbackFormatting);
	}

	public MutableComponent label(TextColor color) {
		return Component.literal(displayName).withStyle(style -> style.withColor(color));
	}

	public MutableComponent channelPrefix(TextColor color) {
		return Component.literal("[" + displayName + "] ").withStyle(style -> style.withColor(color));
	}

	public static ChatMode fromCommand(String name) {
		for (ChatMode mode : values()) {
			if (mode.commandName.equalsIgnoreCase(name)) {
				return mode;
			}
		}
		return null;
	}
}
