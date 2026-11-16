package com.manhunt.teamchat;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;

public final class TeamChatCommands {
	private TeamChatCommands() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
				Commands.literal("chat")
						.executes(TeamChatCommands::status)
						.then(Commands.literal("global").executes(ctx -> setMode(ctx, ChatMode.GLOBAL)))
						.then(Commands.literal("hunter").executes(ctx -> setMode(ctx, ChatMode.HUNTER)))
						.then(Commands.literal("runner").executes(ctx -> setMode(ctx, ChatMode.RUNNER)))
						.then(Commands.literal("status").executes(TeamChatCommands::status))
		);
	}

	private static int setMode(CommandContext<CommandSourceStack> ctx, ChatMode mode) {
		CommandSourceStack source = ctx.getSource();
		if (!(source.getEntity() instanceof ServerPlayer player)) {
			source.sendFailure(Component.literal("Only players can switch chat channels.")
					.withStyle(ChatFormatting.RED));
			return 0;
		}

		if (!TeamChatController.canUseChannel(player, mode)) {
			TextColor teamColor = TeamChatController.colorFor(source.getServer(), mode);
			source.sendFailure(
					Component.literal("Only players on the ")
							.append(Component.literal(mode.teamName())
									.withStyle(style -> style.withColor(teamColor)))
							.append(Component.literal(" team (or ops) can use that channel. "
									+ "Join with /team join " + mode.teamName()
									+ " " + player.getGameProfile().name()))
							.withStyle(ChatFormatting.RED)
			);
			return 0;
		}

		TeamChatController.setMode(player, mode);
		TextColor color = TeamChatController.colorFor(source.getServer(), mode);

		if (mode == ChatMode.GLOBAL) {
			source.sendSuccess(
					() -> Component.literal("Send channel: ")
							.append(Component.literal("Global").withStyle(ChatFormatting.GREEN))
							.append(Component.literal(" - your messages go to everyone.")),
					false
			);
		} else {
			source.sendSuccess(
					() -> Component.literal("Send channel: ")
							.append(mode.label(color))
							.append(Component.literal(" - your messages go only to ")
									.append(Component.literal(mode.teamName())
											.withStyle(style -> style.withColor(color)))
									.append(Component.literal(". You still see global chat."))),
					false
			);
		}
		return 1;
	}

	private static int status(CommandContext<CommandSourceStack> ctx) {
		CommandSourceStack source = ctx.getSource();
		if (!(source.getEntity() instanceof ServerPlayer player)) {
			source.sendFailure(Component.literal("Only players have a chat mode.")
					.withStyle(ChatFormatting.RED));
			return 0;
		}

		ChatMode mode = TeamChatController.getMode(player);
		TextColor color = TeamChatController.colorFor(source.getServer(), mode);
		source.sendSuccess(
				() -> Component.literal("Send channel: ").append(mode.label(color))
						.append(Component.literal(" (what you see is based on your Manhunt team)")
								.withStyle(ChatFormatting.GRAY))
						.append(Component.literal("  |  /chat hunter | /chat runner | /chat global")
								.withStyle(ChatFormatting.DARK_GRAY)),
				false
		);
		return 1;
	}
}
