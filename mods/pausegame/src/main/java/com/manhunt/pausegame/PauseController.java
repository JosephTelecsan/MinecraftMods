package com.manhunt.pausegame;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-authoritative manhunt pause.
 * <p>
 * Uses vanilla {@code TickRateManager} freeze for world/mobs/time, then additionally freezes every
 * player except the exempt executor and rejects their gameplay packets so they cannot dig/move/interact.
 */
public final class PauseController {
	private static final int REMINDER_INTERVAL_TICKS = 40;

	private static volatile boolean paused;
	private static volatile boolean frozenBeforePause;
	private static volatile @Nullable UUID exemptPlayerId;
	private static volatile @Nullable String exemptPlayerName;
	private static volatile @Nullable MinecraftServer server;

	/** Locked positions for frozen players (prevents packet-based sliding). */
	private static final Map<UUID, LockedPose> LOCKED = new ConcurrentHashMap<>();

	private static int reminderTicks;
	/** Monotonic pause-tick counter (frozen players no longer increment entity tickCount). */
	private static int pauseTicks;

	private PauseController() {
	}

	public static boolean isPaused() {
		return paused;
	}

	public static boolean isExempt(@Nullable Player player) {
		if (player == null || !paused) {
			return true;
		}
		final UUID exempt = exemptPlayerId;
		return exempt != null && exempt.equals(player.getUUID());
	}

	public static boolean shouldFreeze(@Nullable Player player) {
		return paused && player != null && !isExempt(player);
	}

	/**
	 * True if this entity should keep ticking because an exempt player is controlling/riding it.
	 */
	public static boolean hasExemptPassenger(Entity entity) {
		if (entity == null || !paused) {
			return false;
		}
		if (entity instanceof Player player) {
			return isExempt(player);
		}

		final Entity controller = entity.getControllingPassenger();
		if (controller instanceof Player player && isExempt(player)) {
			return true;
		}

		for (final Entity passenger : entity.getPassengers()) {
			if (hasExemptPassenger(passenger)) {
				return true;
			}
		}
		return false;
	}

	public static synchronized boolean pause(MinecraftServer minecraftServer, @Nullable ServerPlayer executor) {
		if (minecraftServer == null) {
			return false;
		}

		server = minecraftServer;

		if (paused) {
			return false;
		}

		try {
			frozenBeforePause = minecraftServer.tickRateManager().isFrozen();
			minecraftServer.tickRateManager().setFrozen(true);
		} catch (RuntimeException e) {
			ManhuntPauseGame.LOGGER.error("Failed to freeze TickRateManager", e);
			return false;
		}

		paused = true;
		exemptPlayerId = executor != null ? executor.getUUID() : null;
		exemptPlayerName = executor != null ? executor.getGameProfile().name() : "Console";
		reminderTicks = 0;
		pauseTicks = 0;
		LOCKED.clear();

		for (final ServerPlayer player : minecraftServer.getPlayerList().getPlayers()) {
			lockPlayer(player);
		}

		broadcast(Component.literal("GAME FULLY PAUSED by ")
				.withStyle(ChatFormatting.GOLD)
				.append(Component.literal(exemptPlayerName).withStyle(ChatFormatting.YELLOW))
				.append(Component.literal(" - ticks, time, mobs, and all other players frozen. Only they can act.")
						.withStyle(ChatFormatting.GRAY)));

		if (executor != null) {
			executor.sendSystemMessage(Component.literal("Everything is frozen except you. Run /pausegame again to resume.")
					.withStyle(ChatFormatting.GREEN));
		}

		ManhuntPauseGame.LOGGER.info("Manhunt pause ON by {}", exemptPlayerName);
		return true;
	}

	public static synchronized boolean unpause(@Nullable String byName) {
		if (!paused) {
			return false;
		}

		final MinecraftServer minecraftServer = server;
		paused = false;
		LOCKED.clear();
		reminderTicks = 0;
		pauseTicks = 0;

		final UUID wasExempt = exemptPlayerId;
		final String wasName = exemptPlayerName;
		exemptPlayerId = null;
		exemptPlayerName = null;

		if (minecraftServer != null) {
			try {
				minecraftServer.tickRateManager().setFrozen(frozenBeforePause);
			} catch (RuntimeException e) {
				ManhuntPauseGame.LOGGER.error("Failed to restore TickRateManager freeze state", e);
				try {
					minecraftServer.tickRateManager().setFrozen(false);
				} catch (RuntimeException ignored) {
				}
			}

			final String who = byName != null ? byName : (wasName != null ? wasName : "Unknown");
			broadcast(Component.literal("Game RESUMED by ")
					.withStyle(ChatFormatting.GREEN)
					.append(Component.literal(who).withStyle(ChatFormatting.YELLOW)));
		}

		ManhuntPauseGame.LOGGER.info("Manhunt pause OFF (was by {}, resumed by {})", wasExempt, byName);
		server = null;
		frozenBeforePause = false;
		return true;
	}

	public static void onServerStopping() {
		if (paused) {
			unpause("Server stop");
		}
		clear();
	}

	public static void clear() {
		paused = false;
		frozenBeforePause = false;
		exemptPlayerId = null;
		exemptPlayerName = null;
		server = null;
		LOCKED.clear();
		reminderTicks = 0;
		pauseTicks = 0;
	}

	public static void onPlayerJoin(ServerPlayer player) {
		if (!paused || player == null) {
			return;
		}
		lockPlayer(player);
		player.sendSystemMessage(Component.literal("The game is currently PAUSED.")
				.withStyle(ChatFormatting.GOLD));
		if (isExempt(player)) {
			player.sendSystemMessage(Component.literal("You are exempt and can still move.")
					.withStyle(ChatFormatting.GREEN));
		} else {
			player.sendSystemMessage(Component.literal("You are frozen until the pause ends.")
					.withStyle(ChatFormatting.RED));
		}
	}

	public static void onPlayerLeave(ServerPlayer player) {
		if (player == null) {
			return;
		}

		LOCKED.remove(player.getUUID());

		if (paused && exemptPlayerId != null && exemptPlayerId.equals(player.getUUID())) {
			// Host left while paused - resume so the world does not stay stuck.
			broadcast(Component.literal("Pause executor left - auto-resuming the game.")
					.withStyle(ChatFormatting.YELLOW));
			unpause(player.getGameProfile().name() + " (disconnect)");
		}
	}

	public static void tick(MinecraftServer minecraftServer) {
		if (!paused || minecraftServer == null) {
			return;
		}

		server = minecraftServer;

		// Re-assert vanilla freeze if something else cleared it (/tick unfreeze, another mod).
		try {
			if (!minecraftServer.tickRateManager().isFrozen()) {
				minecraftServer.tickRateManager().setFrozen(true);
			}
		} catch (RuntimeException e) {
			ManhuntPauseGame.LOGGER.warn("Could not re-assert tick freeze", e);
		}

		pauseTicks++;

		for (final ServerPlayer player : minecraftServer.getPlayerList().getPlayers()) {
			if (shouldFreeze(player)) {
				enforceLock(player);
			} else if (isExempt(player)) {
				LOCKED.remove(player.getUUID());
			}
		}

		if (++reminderTicks >= REMINDER_INTERVAL_TICKS) {
			reminderTicks = 0;
			final String name = exemptPlayerName != null ? exemptPlayerName : "operator";
			final Component overlay = Component.literal("FULL PAUSE by " + name + " - only they can act - /pausegame")
					.withStyle(ChatFormatting.GOLD);
			for (final ServerPlayer player : minecraftServer.getPlayerList().getPlayers()) {
				try {
					player.sendOverlayMessage(overlay);
				} catch (RuntimeException ignored) {
				}
			}
		}
	}

	private static void lockPlayer(ServerPlayer player) {
		if (player == null || isExempt(player)) {
			return;
		}
		LOCKED.put(player.getUUID(), LockedPose.from(player));
		try {
			player.setDeltaMovement(Vec3.ZERO);
			player.fallDistance = 0F;
			player.absSnapTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
		} catch (RuntimeException ignored) {
		}
	}

	private static void enforceLock(ServerPlayer player) {
		if (player == null || player.hasDisconnected()) {
			return;
		}

		LockedPose pose = LOCKED.get(player.getUUID());
		if (pose == null || pose.dimension != player.level().dimension()) {
			// Joined mid-pause, changed dimension somehow, or missing lock - re-lock where they are.
			lockPlayer(player);
			pose = LOCKED.get(player.getUUID());
			if (pose == null) {
				return;
			}
		}

		try {
			player.setDeltaMovement(Vec3.ZERO);
			player.fallDistance = 0F;
			player.absSnapTo(pose.x, pose.y, pose.z, pose.yRot, pose.xRot);

			// Sync client prediction every second. Use pauseTicks - frozen players no longer advance tickCount.
			if (pauseTicks % 20 == 0 && player.connection != null) {
				player.connection.teleport(pose.x, pose.y, pose.z, pose.yRot, pose.xRot);
			}
		} catch (RuntimeException e) {
			ManhuntPauseGame.LOGGER.debug("Failed enforcing pause lock for {}", player.getUUID(), e);
		}
	}

	private static void broadcast(Component message) {
		final MinecraftServer minecraftServer = server;
		if (minecraftServer == null) {
			return;
		}
		try {
			minecraftServer.getPlayerList().broadcastSystemMessage(message, false);
		} catch (RuntimeException e) {
			ManhuntPauseGame.LOGGER.warn("Failed broadcasting pause message", e);
		}
	}

	private record LockedPose(net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension,
							  double x, double y, double z, float yRot, float xRot) {
		static LockedPose from(ServerPlayer player) {
			return new LockedPose(
					player.level().dimension(),
					player.getX(),
					player.getY(),
					player.getZ(),
					player.getYRot(),
					player.getXRot()
			);
		}
	}
}
