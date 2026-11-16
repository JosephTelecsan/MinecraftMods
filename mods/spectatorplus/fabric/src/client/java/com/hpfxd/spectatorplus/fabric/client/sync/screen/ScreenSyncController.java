package com.hpfxd.spectatorplus.fabric.client.sync.screen;

import com.hpfxd.spectatorplus.fabric.client.gui.screens.SyncedInventoryScreen;
import com.hpfxd.spectatorplus.fabric.client.sync.ClientSyncController;
import com.hpfxd.spectatorplus.fabric.client.util.SpecUtil;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundInventorySyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundScreenCursorSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundScreenSyncPacket;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import static com.hpfxd.spectatorplus.fabric.client.sync.ClientSyncController.setSyncData;
import static com.hpfxd.spectatorplus.fabric.client.sync.ClientSyncController.syncData;

public class ScreenSyncController {
    public static boolean isPendingOpen = false;

    public static int syncedWindowId = Integer.MIN_VALUE;
    public static Inventory syncedInventory;
    public static Screen syncedScreen;

    private static boolean closing;

    public static void init() {
        ClientEntityEvents.ENTITY_UNLOAD.register((entity, level) -> {
            if (entity instanceof final Player player && syncData != null && syncData.playerId.equals(player.getUUID())) {
                // Target left tracking range / died / changed dimension — drop all sync, not just the screen.
                ClientSyncController.setSyncData(null);
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(ScreenSyncController::tickSafety);

        ClientPlayNetworking.registerGlobalReceiver(ClientboundScreenSyncPacket.TYPE, ScreenSyncController::handle);
        ClientPlayNetworking.registerGlobalReceiver(ClientboundInventorySyncPacket.TYPE, ScreenSyncController::handle);
        ClientPlayNetworking.registerGlobalReceiver(ClientboundScreenCursorSyncPacket.TYPE, ScreenSyncController::handle);
    }

    /**
     * Safety net for races camera/mixin order can miss: inventory still open after target gone,
     * spectator left spectator mode, syncData/screen desynced from camera, etc.
     */
    private static void tickSafety(Minecraft mc) {
        if (mc.player == null) {
            if (syncedScreen != null || syncData != null || isPendingOpen) {
                ClientSyncController.setSyncData(null);
                isPendingOpen = false;
            }
            return;
        }

        final AbstractClientPlayer camera = SpecUtil.getCameraPlayer(mc);

        if (syncData != null && (camera == null || !syncData.playerId.equals(camera.getUUID()))) {
            ClientSyncController.setSyncData(null);
            return;
        }

        if (syncedScreen != null) {
            if (syncData == null || syncData.screen == null || camera == null || camera.isRemoved()) {
                closeSyncedInventory();
            }
        }

        if (isPendingOpen && (syncData == null || syncData.screen == null || camera == null)) {
            isPendingOpen = false;
        }
    }

    private static void handle(ClientboundScreenSyncPacket packet, ClientPlayNetworking.Context context) {
        if (!setSyncData(packet.playerId()) || syncData == null) {
            return;
        }
        syncData.setScreen();

        isPendingOpen = true;
        syncData.screen.isSurvivalInventory = packet.isSurvivalInventory();
        syncData.screen.isClientRequested = packet.isClientRequested();
        syncData.screen.hasDummySlots = packet.hasDummySlots();
    }

    private static void handle(ClientboundInventorySyncPacket packet, ClientPlayNetworking.Context context) {
        if (!setSyncData(packet.playerId()) || syncData == null) {
            return;
        }
        syncData.setScreen();

        if (syncData.screen.inventoryItems == null) {
            syncData.screen.inventoryItems = NonNullList.withSize(ClientboundInventorySyncPacket.ITEMS_LENGTH, ItemStack.EMPTY);
        }

        final ItemStack[] items = packet.items();
        final int limit = Math.min(items.length, syncData.screen.inventoryItems.size());
        for (int slot = 0; slot < limit; slot++) {
            final ItemStack item = items[slot];

            if (item != null) {
                syncData.screen.inventoryItems.set(slot, item);

                if (syncedInventory != null && slot < syncedInventory.getContainerSize()) {
                    syncedInventory.setItem(slot, item);
                }
            }
        }
    }

    private static void handle(ClientboundScreenCursorSyncPacket packet, ClientPlayNetworking.Context context) {
        if (!setSyncData(packet.playerId()) || syncData == null) {
            return;
        }
        syncData.setScreen();

        final ItemStack cursor = packet.cursor();
        syncData.screen.cursorItem = cursor == null ? ItemStack.EMPTY : cursor;
        syncData.screen.cursorItemSlot = packet.originSlot();
    }

    public static void closeSyncedInventory() {
        if (closing) {
            return;
        }

        final Screen screen = syncedScreen;
        syncedScreen = null;
        syncedInventory = null;
        syncedWindowId = Integer.MIN_VALUE;
        isPendingOpen = false;

        if (ClientSyncController.syncData != null) {
            ClientSyncController.syncData.screen = null;
        }

        if (screen == null) {
            return;
        }

        closing = true;
        try {
            final Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.gui != null && mc.gui.screen() == screen) {
                mc.gui.setScreen(null);
            } else {
                try {
                    screen.onClose();
                } catch (RuntimeException ignored) {
                    // Screen may already be tearing down during disconnect / dimension change.
                }
            }
        } finally {
            closing = false;
        }
    }

    public static void openPlayerInventory(Minecraft mc) {
        final Player player = SpecUtil.getCameraPlayer(mc);
        if (player == null || player.isRemoved() || syncData == null || syncData.screen == null) {
            isPendingOpen = false;
            return;
        }

        final SyncedInventoryScreen screen = new SyncedInventoryScreen(player);
        handleNewSyncedScreen(mc, screen);
    }

    public static <S extends Screen & MenuAccess<?>> void handleNewSyncedScreen(Minecraft mc, S screen) {
        isPendingOpen = false;

        if (mc.player == null || syncData == null || syncData.screen == null) {
            syncedInventory = null;
            return;
        }

        mc.player.containerMenu = screen.getMenu();
        mc.gui.setScreen(screen);

        if (mc.gui.screen() != screen) {
            syncedInventory = null;
            syncData.screen = null;
            return;
        }

        syncedScreen = screen;

        ScreenEvents.remove(screen).register(s -> {
            if (syncedScreen == s) {
                syncedScreen = null;
            }
            syncedInventory = null;
            syncedWindowId = Integer.MIN_VALUE;
            if (syncData != null) {
                syncData.screen = null;
            }
        });
    }

    public static boolean createInventory(Player spectated) {
        if (spectated == null || spectated.isRemoved()
                || syncData == null || syncData.screen == null || syncData.screen.inventoryItems == null) {
            return false;
        }

        syncedInventory = new Inventory(spectated, new EntityEquipment());

        for (int i = 0; i < syncData.screen.inventoryItems.size(); i++) {
            final ItemStack stack = syncData.screen.inventoryItems.get(i);
            syncedInventory.setItem(i, stack == null ? ItemStack.EMPTY : stack);
        }
        return true;
    }
}
