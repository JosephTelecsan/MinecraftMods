package com.hpfxd.spectatorplus.fabric.sync.handler;

import com.hpfxd.spectatorplus.fabric.sync.ServerSyncController;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundHotbarSyncPacket;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HotbarSyncHandler {
    /** 0-8 = hotbar, 9 = offhand */
    public static final int SLOT_COUNT = 10;
    public static final int OFFHAND_INDEX = 9;

    private static final Map<UUID, ItemStack[]> HOTBARS = new HashMap<>();

    public static void init() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> HOTBARS.remove(handler.getPlayer().getUUID()));

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> HOTBARS.clear());

        ServerTickEvents.END_LEVEL_TICK.register(HotbarSyncHandler::tick);
    }

    public static void remember(ServerPlayer player) {
        HOTBARS.put(player.getUUID(), snapshot(player));
    }

    public static ItemStack[] snapshot(ServerPlayer player) {
        final ItemStack[] items = new ItemStack[SLOT_COUNT];
        for (int i = 0; i < 9; i++) {
            items[i] = player.getInventory().getItem(i).copy();
        }
        items[OFFHAND_INDEX] = player.getInventory().getItem(Inventory.SLOT_OFFHAND).copy();
        return items;
    }

    private static void tick(ServerLevel level) {
        for (final ServerPlayer player : level.players()) {
            final ItemStack[] previous = HOTBARS.computeIfAbsent(player.getUUID(), k -> {
                final ItemStack[] arr = new ItemStack[SLOT_COUNT];
                Arrays.fill(arr, ItemStack.EMPTY);
                return arr;
            });

            // Migrate old 9-slot cache entries if needed
            final ItemStack[] prev = previous.length >= SLOT_COUNT ? previous : grow(previous);

            final ItemStack[] current = snapshot(player);
            boolean updated = false;

            for (int i = 0; i < SLOT_COUNT; i++) {
                if (!ItemStack.matches(current[i], prev[i])) {
                    updated = true;
                    break;
                }
            }

            if (updated) {
                HOTBARS.put(player.getUUID(), current);
                ServerSyncController.broadcastPacketToSpectators(player, new ClientboundHotbarSyncPacket(player.getUUID(), current));
            } else if (previous.length < SLOT_COUNT) {
                HOTBARS.put(player.getUUID(), prev);
            }
        }
    }

    private static ItemStack[] grow(ItemStack[] previous) {
        final ItemStack[] arr = new ItemStack[SLOT_COUNT];
        Arrays.fill(arr, ItemStack.EMPTY);
        System.arraycopy(previous, 0, arr, 0, Math.min(previous.length, SLOT_COUNT));
        return arr;
    }
}
