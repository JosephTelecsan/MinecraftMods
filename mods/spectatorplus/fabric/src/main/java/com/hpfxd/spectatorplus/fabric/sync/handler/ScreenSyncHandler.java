package com.hpfxd.spectatorplus.fabric.sync.handler;

import com.hpfxd.spectatorplus.fabric.sync.ServerSyncController;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundInventorySyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundScreenSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ServerboundOpenedInventorySyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ServerboundRequestInventoryOpenPacket;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Fabric implementation of spectator inventory viewing (no Paper plugin required).
 * <p>
 * When a spectator presses the inventory key while looking at a player, the client sends
 * {@link ServerboundRequestInventoryOpenPacket}. We reply with screen/inventory sync packets
 * and open a dummy menu so the client can show {@code SyncedInventoryScreen}.
 */
public class ScreenSyncHandler {
    private static final String INVENTORY_PERMISSION = "spectatorplus.sync.inventory";
    private static final String SCREEN_PERMISSION = "spectatorplus.sync.screen";

    /** spectator UUID -> target UUID currently being viewed via synced inventory */
    private static final Map<UUID, UUID> VIEWING = new HashMap<>();
    /** target UUID -> last sent inventory snapshot for diffing */
    private static final Map<UUID, ItemStack[]> LAST_INVENTORY = new HashMap<>();

    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(ServerboundRequestInventoryOpenPacket.TYPE, ScreenSyncHandler::handleRequestOpen);
        ServerPlayNetworking.registerGlobalReceiver(ServerboundOpenedInventorySyncPacket.TYPE, ScreenSyncHandler::handleTargetOpenedInventory);

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            final UUID id = handler.getPlayer().getUUID();
            VIEWING.remove(id);
            LAST_INVENTORY.remove(id);
            // If the disconnecting player was being viewed, drop those sessions and close menus.
            closeViewersOf(server, id);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            VIEWING.clear();
            LAST_INVENTORY.clear();
        });

        ServerLivingEntityEvents.AFTER_DEATH.register(ScreenSyncHandler::onDeath);

        ServerTickEvents.END_LEVEL_TICK.register(ScreenSyncHandler::tick);
    }

    private static void onDeath(LivingEntity entity, net.minecraft.world.damagesource.DamageSource damageSource) {
        if (entity instanceof final ServerPlayer dead) {
            LAST_INVENTORY.remove(dead.getUUID());
            closeViewersOf(dead.level().getServer(), dead.getUUID());
        }
    }

    private static void closeViewersOf(MinecraftServer server, UUID targetId) {
        if (server == null || targetId == null) {
            return;
        }

        for (final Iterator<Map.Entry<UUID, UUID>> it = VIEWING.entrySet().iterator(); it.hasNext(); ) {
            final Map.Entry<UUID, UUID> entry = it.next();
            if (!targetId.equals(entry.getValue())) {
                continue;
            }

            final ServerPlayer spectator = server.getPlayerList().getPlayer(entry.getKey());
            if (spectator != null && spectator.containerMenu instanceof SpectatorDummyMenu) {
                try {
                    spectator.closeContainer();
                } catch (RuntimeException ignored) {
                    // Menu may already be closing during logout/death cleanup.
                }
            }
            it.remove();
        }
    }

    private static void handleRequestOpen(ServerboundRequestInventoryOpenPacket packet, ServerPlayNetworking.Context ctx) {
        final ServerPlayer spectator = ctx.player();
        if (!spectator.isSpectator()) {
            return;
        }

        if (!Permissions.check(spectator, INVENTORY_PERMISSION, true)) {
            spectator.sendOverlayMessage(Component.translatable("spectatorplus.no-inventory-permission").withStyle(ChatFormatting.RED));
            return;
        }

        final Entity camera = spectator.getCamera();
        if (!(camera instanceof final ServerPlayer target) || !target.getUUID().equals(packet.playerId())) {
            return;
        }

        if (!isViewableTarget(target)) {
            return;
        }

        openPlayerInventory(spectator, target, true);
    }

    private static void handleTargetOpenedInventory(ServerboundOpenedInventorySyncPacket packet, ServerPlayNetworking.Context ctx) {
        final ServerPlayer target = ctx.player();
        if (!isViewableTarget(target)) {
            return;
        }

        for (final ServerPlayer spectator : ServerSyncController.getSpectators(target)) {
            if (!Permissions.check(spectator, SCREEN_PERMISSION, true)) {
                continue;
            }
            if (!Permissions.check(spectator, INVENTORY_PERMISSION, true)) {
                continue;
            }
            // Keep the inventory screen the spectator already opened
            if (VIEWING.containsKey(spectator.getUUID())) {
                continue;
            }
            openPlayerInventory(spectator, target, false);
        }
    }

    private static boolean isViewableTarget(ServerPlayer target) {
        return target != null && target.isAlive() && !target.isRemoved() && !target.isSpectator();
    }

    private static void openPlayerInventory(ServerPlayer spectator, ServerPlayer target, boolean clientRequested) {
        if (!isViewableTarget(target) || spectator == null || !spectator.isSpectator()) {
            return;
        }

        VIEWING.put(spectator.getUUID(), target.getUUID());

        final int flags = ClientboundScreenSyncPacket.flags(true, clientRequested, true);
        ServerSyncController.sendPacket(spectator, new ClientboundScreenSyncPacket(target.getUUID(), flags));
        ServerSyncController.sendPacket(spectator, new ClientboundInventorySyncPacket(target.getUUID(), snapshotInventory(target)));

        LAST_INVENTORY.put(target.getUUID(), snapshotInventory(target));

        spectator.openMenu(new SimpleMenuProvider(
                (syncId, inv, player) -> new SpectatorDummyMenu(syncId, inv),
                Component.translatable("container.inventory")
        ));
    }

    private static void tick(ServerLevel level) {
        for (final Iterator<Map.Entry<UUID, UUID>> it = VIEWING.entrySet().iterator(); it.hasNext(); ) {
            final Map.Entry<UUID, UUID> entry = it.next();
            final ServerPlayer spectator = level.getServer().getPlayerList().getPlayer(entry.getKey());
            if (spectator == null) {
                it.remove();
                continue;
            }
            if (!spectator.level().equals(level)) {
                continue;
            }

            final Entity camera = spectator.getCamera();
            if (!(camera instanceof final ServerPlayer target) || !target.getUUID().equals(entry.getValue()) || !isViewableTarget(target)) {
                if (spectator.containerMenu instanceof SpectatorDummyMenu) {
                    try {
                        spectator.closeContainer();
                    } catch (RuntimeException ignored) {
                    }
                }
                it.remove();
                continue;
            }

            if (!(spectator.containerMenu instanceof SpectatorDummyMenu)) {
                it.remove();
                continue;
            }

            if (!spectator.isSpectator()) {
                try {
                    spectator.closeContainer();
                } catch (RuntimeException ignored) {
                }
                it.remove();
                continue;
            }

            final ItemStack[] previous = LAST_INVENTORY.computeIfAbsent(target.getUUID(), k -> emptyInventory());
            final ItemStack[] current = snapshotInventory(target);
            final ItemStack[] delta = new ItemStack[ClientboundInventorySyncPacket.ITEMS_LENGTH];
            boolean changed = false;

            for (int i = 0; i < current.length; i++) {
                if (!ItemStack.matches(current[i], previous[i])) {
                    previous[i] = current[i].copy();
                    delta[i] = current[i];
                    changed = true;
                }
            }

            if (changed) {
                ServerSyncController.sendPacket(spectator, new ClientboundInventorySyncPacket(target.getUUID(), delta));
            }
        }
    }

    private static ItemStack[] snapshotInventory(ServerPlayer player) {
        final ItemStack[] items = new ItemStack[ClientboundInventorySyncPacket.ITEMS_LENGTH];
        for (int slot = 0; slot < items.length; slot++) {
            if (slot < player.getInventory().getContainerSize()) {
                final ItemStack stack = player.getInventory().getItem(slot);
                items[slot] = stack == null ? ItemStack.EMPTY : stack.copy();
            } else {
                items[slot] = ItemStack.EMPTY;
            }
        }
        return items;
    }

    private static ItemStack[] emptyInventory() {
        final ItemStack[] items = new ItemStack[ClientboundInventorySyncPacket.ITEMS_LENGTH];
        Arrays.fill(items, ItemStack.EMPTY);
        return items;
    }
}
