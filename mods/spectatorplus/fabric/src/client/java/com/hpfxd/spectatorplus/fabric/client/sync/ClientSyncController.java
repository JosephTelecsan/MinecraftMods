package com.hpfxd.spectatorplus.fabric.client.sync;

import com.hpfxd.spectatorplus.fabric.client.sync.screen.ScreenSyncController;
import com.hpfxd.spectatorplus.fabric.client.util.SpecUtil;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundExperienceSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundFoodSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundHotbarSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundSelectedSlotSyncPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;

import java.util.UUID;

public class ClientSyncController {
    public static ClientSyncData syncData;

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(ClientboundExperienceSyncPacket.TYPE, ClientSyncController::handle);
        ClientPlayNetworking.registerGlobalReceiver(ClientboundFoodSyncPacket.TYPE, ClientSyncController::handle);
        ClientPlayNetworking.registerGlobalReceiver(ClientboundHotbarSyncPacket.TYPE, ClientSyncController::handle);
        ClientPlayNetworking.registerGlobalReceiver(ClientboundSelectedSlotSyncPacket.TYPE, ClientSyncController::handle);

        ClientLoginConnectionEvents.INIT.register((handler, client) -> setSyncData(null));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> setSyncData(null));

        ScreenSyncController.init();
    }

    /**
     * True when the client camera is already on this player in spectator mode.
     */
    public static boolean isSpectating(UUID playerId) {
        if (playerId == null) {
            return false;
        }

        final AbstractClientPlayer camera = SpecUtil.getCameraPlayer(Minecraft.getInstance());
        return camera != null && playerId.equals(camera.getUUID());
    }

    /**
     * Accept sync for the camera target, or briefly while the camera packet is still in flight
     * (spectator, camera still on self). Reject true stale packets for other players.
     */
    private static boolean canAcceptSync(UUID playerId) {
        if (isSpectating(playerId)) {
            return true;
        }

        final Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null || playerId == null) {
            return false;
        }

        final Entity camera = mc.getCameraEntity();
        if (camera != null && camera != mc.player && playerId.equals(camera.getUUID())) {
            return true;
        }

        // Updates for the player we already track (camera may flicker for a tick)
        if (syncData != null && playerId.equals(syncData.playerId)) {
            // Drop if we are clearly looking at someone else now
            if (camera != null && camera != mc.player && !playerId.equals(camera.getUUID())) {
                return false;
            }
            return true;
        }

        // Pending initial sync: still in spectator, camera packet not applied yet
        final boolean spectator = mc.player.isSpectator()
                || (mc.gameMode != null && mc.gameMode.getPlayerMode() == GameType.SPECTATOR);
        if (spectator && (camera == null || camera == mc.player)) {
            return true;
        }

        return false;
    }

    private static void handle(ClientboundExperienceSyncPacket packet, ClientPlayNetworking.Context context) {
        if (!setSyncData(packet.playerId())) {
            return;
        }

        syncData.experienceProgress = packet.progress();
        syncData.experienceLevel = packet.level();
        syncData.experienceNeededForNextLevel = packet.neededForNextLevel();
    }

    private static void handle(ClientboundFoodSyncPacket packet, ClientPlayNetworking.Context context) {
        if (!setSyncData(packet.playerId())) {
            return;
        }

        if (syncData.foodData == null) {
            syncData.foodData = new FoodData();
        }
        syncData.foodData.setFoodLevel(packet.food());
        syncData.foodData.setSaturation(packet.saturation());
    }

    private static void handle(ClientboundHotbarSyncPacket packet, ClientPlayNetworking.Context context) {
        if (!setSyncData(packet.playerId())) {
            return;
        }

        final ItemStack[] items = packet.items();
        int nonNull = 0;
        for (final ItemStack item : items) {
            if (item != null) {
                nonNull++;
            }
        }

        // Full snapshots replace every slot we received. Sparse/legacy diffs still merge.
        final boolean fullSnapshot = nonNull >= items.length && items.length >= 9;
        for (int slot = 0; slot < items.length && slot < syncData.hotbarItems.size(); slot++) {
            final ItemStack item = items[slot];
            if (item != null) {
                syncData.hotbarItems.set(slot, item);
            } else if (fullSnapshot) {
                syncData.hotbarItems.set(slot, ItemStack.EMPTY);
            }
        }

        // Hotbar packet alone should be enough to show the bar even if selected-slot packet was dropped.
        if (syncData.selectedHotbarSlot < 0) {
            syncData.selectedHotbarSlot = 0;
        }
    }

    public static ItemStack getSyncedOffhand() {
        if (syncData == null || syncData.hotbarItems.size() <= 9) {
            return null;
        }
        final ItemStack off = syncData.hotbarItems.get(9);
        return off == null ? ItemStack.EMPTY : off;
    }

    private static void handle(ClientboundSelectedSlotSyncPacket packet, ClientPlayNetworking.Context context) {
        if (!setSyncData(packet.playerId())) {
            return;
        }

        final int selected = packet.selectedSlot();
        // Selected slot is hotbar only (0-8); index 9 in hotbarItems is offhand.
        if (selected >= 0 && selected < 9) {
            syncData.selectedHotbarSlot = selected;
        }
    }

    /**
     * @return {@code true} if {@link #syncData} now matches {@code playerId} and callers may write fields
     */
    public static boolean setSyncData(UUID playerId) {
        if (playerId == null) {
            ScreenSyncController.closeSyncedInventory();
            syncData = null;
            return false;
        }

        if (!canAcceptSync(playerId)) {
            return false;
        }

        if (syncData == null || !syncData.playerId.equals(playerId)) {
            ScreenSyncController.closeSyncedInventory();
            syncData = new ClientSyncData(playerId);
        }

        return true;
    }
}
