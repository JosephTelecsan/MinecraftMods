package com.hpfxd.spectatorplus.fabric.sync.handler;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Dummy 9-slot chest opened for spectators so the client receives a vanilla OpenScreen packet.
 * SpectatorPlus client replaces this UI with the spectated player's inventory screen.
 * All interaction is blocked server-side.
 */
public class SpectatorDummyMenu extends ChestMenu {
    public SpectatorDummyMenu(int containerId, Inventory playerInventory) {
        super(MenuType.GENERIC_9x1, containerId, playerInventory, new SimpleContainer(9), 1);
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput clickType, Player player) {
        // No-op: spectators cannot take or move items.
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.isSpectator();
    }
}
