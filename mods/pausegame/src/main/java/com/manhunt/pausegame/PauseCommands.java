package com.manhunt.pausegame;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class PauseCommands {
	private PauseCommands() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
				Commands.literal("pausegame")
						.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
						.executes(PauseCommands::toggle)
						.then(Commands.literal("on").executes(PauseCommands::on))
						.then(Commands.literal("off").executes(PauseCommands::off))
						.then(Commands.literal("status").executes(PauseCommands::status))
		);
	}

	private static int toggle(CommandContext<CommandSourceStack> ctx) {
		return PauseController.isPaused() ? off(ctx) : on(ctx);
	}

	private static int on(CommandContext<CommandSourceStack> ctx) {
		final CommandSourceStack source = ctx.getSource();
		final ServerPlayer player = source.getEntity() instanceof ServerPlayer sp ? sp : null;

		if (PauseController.isPaused()) {
			source.sendFailure(Component.literal("Game is already paused. Use /pausegame off to resume.")
					.withStyle(ChatFormatting.RED));
			return 0;
		}

		if (!PauseController.pause(source.getServer(), player)) {
			source.sendFailure(Component.literal("Failed to pause the game.").withStyle(ChatFormatting.RED));
			return 0;
		}

		source.sendSuccess(() -> Component.literal("Paused.").withStyle(ChatFormatting.GOLD), true);
		return 1;
	}

	private static int off(CommandContext<CommandSourceStack> ctx) {
		final CommandSourceStack source = ctx.getSource();

		if (!PauseController.isPaused()) {
			source.sendFailure(Component.literal("Game is not paused.").withStyle(ChatFormatting.RED));
			return 0;
		}

		final String by = source.getEntity() instanceof ServerPlayer sp
				? sp.getGameProfile().name()
				: source.getTextName();

		if (!PauseController.unpause(by)) {
			source.sendFailure(Component.literal("Failed to resume the game.").withStyle(ChatFormatting.RED));
			return 0;
		}

		source.sendSuccess(() -> Component.literal("Resumed.").withStyle(ChatFormatting.GREEN), true);
		return 1;
	}

	private static int status(CommandContext<CommandSourceStack> ctx) {
		if (PauseController.isPaused()) {
			ctx.getSource().sendSuccess(
					() -> Component.literal("Paused.").withStyle(ChatFormatting.GOLD),
					false
			);
		} else {
			ctx.getSource().sendSuccess(
					() -> Component.literal("Not paused.").withStyle(ChatFormatting.GREEN),
					false
			);
		}
		return 1;
	}
}
