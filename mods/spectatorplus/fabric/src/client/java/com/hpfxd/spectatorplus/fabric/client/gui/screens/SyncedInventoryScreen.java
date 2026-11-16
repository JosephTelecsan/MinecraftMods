package com.hpfxd.spectatorplus.fabric.client.gui.screens;

import com.hpfxd.spectatorplus.fabric.client.mixin.screen.AbstractRecipeBookScreenAccessor;
import com.hpfxd.spectatorplus.fabric.client.mixin.screen.ImageButtonAccessor;
import com.hpfxd.spectatorplus.fabric.client.sync.screen.ScreenSyncController;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class SyncedInventoryScreen extends InventoryScreen {
    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};

    public SyncedInventoryScreen(Player player) {
        super(player);
        this.syncOtherItems();
    }

    @Override
    public void containerTick() {
        super.containerTick();

        if (!this.syncOtherItems()) {
            ScreenSyncController.closeSyncedInventory();
            return;
        }

        final RecipeBookComponent<?> recipeBookComponent = ((AbstractRecipeBookScreenAccessor) this).getRecipeBookComponent();
        if (recipeBookComponent.isVisible()) {
            recipeBookComponent.toggleVisibility();
        }
    }

    private boolean syncOtherItems() {
        if (!(this.menu instanceof final SyncedInventoryMenu menu)) {
            return false;
        }

        final Player owner = menu.getOwner();
        if (owner == null || owner.isRemoved()) {
            return false;
        }

        final Inventory playerInventory = owner.getInventory();
        final Inventory fakeInventory = menu.getInventory();
        if (playerInventory == null || fakeInventory == null) {
            return false;
        }

        // armor slots (feet, legs, chest, head)
        for (final EquipmentSlot slot : ARMOR_SLOTS) {
            final int slotIndex = slot.getIndex(Inventory.INVENTORY_SIZE);
            fakeInventory.setItem(slotIndex, playerInventory.getItem(slotIndex));
        }

        fakeInventory.setItem(Inventory.SLOT_OFFHAND, playerInventory.getItem(Inventory.SLOT_OFFHAND));
        return true;
    }

    @Override
    protected <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget) {
        if (widget instanceof final ImageButton button) {
            final WidgetSprites sprites = ((ImageButtonAccessor) button).getSprites();

            if (sprites == RecipeBookComponent.RECIPE_BUTTON_SPRITES) {
                // skip adding recipe book button
                return widget;
            }
        }

        return super.addRenderableWidget(widget);
    }
}
