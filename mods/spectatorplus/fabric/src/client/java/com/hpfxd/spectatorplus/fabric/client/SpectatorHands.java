package com.hpfxd.spectatorplus.fabric.client;

import com.hpfxd.spectatorplus.fabric.client.sync.ClientSyncController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

/**
 * Tracks and extracts the first-person hand/item render state of the player currently being spectated.
 * <p>
 * Vanilla only tracks this state for the local player (see {@code net.minecraft.client.player.FirstPersonHandsAndItems}),
 * so this class mirrors that logic for an arbitrary {@link AbstractClientPlayer}.
 */
public final class SpectatorHands {
    private static final SpectatorHands INSTANCE = new SpectatorHands();

    private @Nullable AbstractClientPlayer tracked;

    private ItemStack mainHandItem = ItemStack.EMPTY;
    private ItemStack offHandItem = ItemStack.EMPTY;
    private float mainHandHeight;
    private float oMainHandHeight;
    private float offHandHeight;
    private float oOffHandHeight;

    private float xBob;
    private float yBob;
    private float xBobO;
    private float yBobO;

    private SpectatorHands() {
    }

    public static SpectatorHands get() {
        return INSTANCE;
    }

    public void tick(Minecraft mc, @Nullable AbstractClientPlayer spectated) {
        if (spectated == null) {
            this.tracked = null;
            this.mainHandHeight = this.oMainHandHeight = 1F;
            this.offHandHeight = this.oOffHandHeight = 1F;
            this.mainHandItem = ItemStack.EMPTY;
            this.offHandItem = ItemStack.EMPTY;
            return;
        }

        this.oMainHandHeight = this.mainHandHeight;
        this.oOffHandHeight = this.offHandHeight;

        final ItemStack nextMainHand = resolveMainHand(spectated);
        final ItemStack nextOffHand = resolveOffHand(spectated);

        if (this.tracked != spectated) {
            // this is the first tick of spectating a new player
            this.tracked = spectated;
            this.mainHandHeight = this.oMainHandHeight = 1F;
            this.offHandHeight = this.oOffHandHeight = 1F;
            this.mainHandItem = nextMainHand;
            this.offHandItem = nextOffHand;
            this.xBob = this.xBobO = spectated.getXRot();
            this.yBob = this.yBobO = spectated.getYRot();
            return;
        }

        // hand sway
        this.yBobO = this.yBob;
        this.xBobO = this.xBob;
        this.xBob += Mth.degreesDifference(this.xBob, spectated.getXRot()) * 0.5F;
        this.yBob += Mth.degreesDifference(this.yBob, spectated.getYRot()) * 0.5F;

        if (this.shouldInstantlyReplaceVisibleItem(mc, this.mainHandItem, nextMainHand)) {
            this.mainHandItem = nextMainHand;
        }
        if (this.shouldInstantlyReplaceVisibleItem(mc, this.offHandItem, nextOffHand)) {
            this.offHandItem = nextOffHand;
        }

        // Remote players don't advance attack-strength the way LocalPlayer does, so getItemSwapScale
        // often stays ~0 and the hand sinks to the middle/bottom of the screen. Keep hands raised.
        float attackAnim = spectated.getItemSwapScale(1.0F);
        if (!(spectated instanceof LocalPlayer) && attackAnim < 0.99F) {
            attackAnim = 1.0F;
        }

        // Use content match (not reference) — hotbar sync sends new ItemStack copies that would
        // otherwise look like a constant item-swap and pin hands mid-screen.
        final float mainHandTargetHeight = ItemStack.matches(this.mainHandItem, nextMainHand)
                ? attackAnim * attackAnim * attackAnim
                : 0.0F;
        final float offHandTargetHeight = ItemStack.matches(this.offHandItem, nextOffHand) ? 1.0F : 0.0F;
        this.mainHandHeight += Mth.clamp(mainHandTargetHeight - this.mainHandHeight, -0.4F, 0.4F);
        this.offHandHeight += Mth.clamp(offHandTargetHeight - this.offHandHeight, -0.4F, 0.4F);

        if (this.mainHandHeight < 0.1F) {
            this.mainHandItem = nextMainHand;
        }
        if (this.offHandHeight < 0.1F) {
            this.offHandItem = nextOffHand;
        }
    }

    /**
     * Prefer the synced hotbar selected slot so the held item matches the hotbar HUD.
     * Entity equipment alone can lag or disagree with our hotbar sync packets.
     */
    private static ItemStack resolveMainHand(AbstractClientPlayer spectated) {
        if (ClientSyncController.syncData != null
                && ClientSyncController.syncData.playerId.equals(spectated.getUUID())
                && ClientSyncController.syncData.selectedHotbarSlot >= 0
                && ClientSyncController.syncData.selectedHotbarSlot < 9
                && ClientSyncController.syncData.selectedHotbarSlot < ClientSyncController.syncData.hotbarItems.size()) {
            final ItemStack synced = ClientSyncController.syncData.hotbarItems.get(ClientSyncController.syncData.selectedHotbarSlot);
            if (synced != null) {
                return synced;
            }
        }
        return spectated.getMainHandItem();
    }

    private static ItemStack resolveOffHand(AbstractClientPlayer spectated) {
        if (ClientSyncController.syncData != null
                && ClientSyncController.syncData.playerId.equals(spectated.getUUID())) {
            final ItemStack synced = ClientSyncController.getSyncedOffhand();
            if (synced != null) {
                return synced;
            }
        }
        return spectated.getOffhandItem();
    }

    private boolean shouldInstantlyReplaceVisibleItem(Minecraft mc, ItemStack visible, ItemStack expected) {
        return ItemStack.matchesIgnoringComponents(visible, expected, DataComponentType::ignoreSwapAnimation)
                || !mc.getItemModelResolver().shouldPlaySwapAnimation(expected);
    }

    /**
     * Equivalent of {@code FirstPersonHandsAndItems#extractRenderState}, using the spectated player rather than the
     * local player.
     */
    public void extractRenderState(Minecraft mc, AbstractClientPlayer player, float partialTicks, FirstPersonHandsAndItemsRenderState state) {
        if (this.tracked != player) {
            // not yet ticked for this player, make sure we have sane state
            this.tick(mc, player);
        }

        // Never render hands sunk mid-screen due to a bad height value.
        final float mainH = Mth.clamp(this.mainHandHeight, 0F, 1F);
        final float offH = Mth.clamp(this.offHandHeight, 0F, 1F);
        final float oMainH = Mth.clamp(this.oMainHandHeight, 0F, 1F);
        final float oOffH = Mth.clamp(this.oOffHandHeight, 0F, 1F);

        final LivingEntity.SwingDescription currentSwing = player.getCurrentSwing();
        state.attackHand = currentSwing == null ? InteractionHand.MAIN_HAND : currentSwing.hand();
        state.viewXRot = player.getViewXRot(partialTicks);
        state.viewYRot = player.getViewYRot(partialTicks);
        state.xBob = Mth.lerp(partialTicks, this.xBobO, this.xBob);
        state.yBob = Mth.lerp(partialTicks, this.yBobO, this.yBob);
        state.isScoping = player.isScoping();
        state.useItemRemainingTicks = player.getUseItemRemainingTicks();
        state.handRenderSelection = evaluateWhichHandsToRender(player);
        state.mainHandItem = this.mainHandItem;
        state.offHandItem = this.offHandItem;
        state.mainHandHeight = mainH;
        state.oldMainHandHeight = oMainH;
        state.offHandHeight = offH;
        state.oldOffHandHeight = oOffH;

        final boolean isMainHandRight = player.getMainArm() == HumanoidArm.RIGHT;
        final ItemDisplayContext mainHandDisplayContext = isMainHandRight ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        final ItemDisplayContext offHandDisplayContext = isMainHandRight ? ItemDisplayContext.FIRST_PERSON_LEFT_HAND : ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;

        state.mainHandRenderState.clear();
        state.offHandRenderState.clear();
        mc.getItemModelResolver().updateForTopItem(state.mainHandRenderState, state.mainHandItem, mainHandDisplayContext, player.level(), player, player.getId() + mainHandDisplayContext.ordinal());
        mc.getItemModelResolver().updateForTopItem(state.offHandRenderState, state.offHandItem, offHandDisplayContext, player.level(), player, player.getId() + offHandDisplayContext.ordinal());

        state.mainHandUseDuration = state.mainHandItem.getUseDuration(player);
        state.offHandUseDuration = state.offHandItem.getUseDuration(player);
        state.mainHandChargeDuration = CrossbowItem.getChargeDuration(state.mainHandItem, player);
        state.offHandChargeDuration = CrossbowItem.getChargeDuration(state.offHandItem, player);
        state.mainHandSwapScale = mc.getItemModelResolver().swapAnimationScale(state.mainHandItem);
        state.offHandSwapScale = mc.getItemModelResolver().swapAnimationScale(state.offHandItem);
        state.hasMainHandMapData = extractMapRenderState(mc, player, state.mainHandItem, state.mainHandMapRenderState);
        state.hasOffHandMapData = extractMapRenderState(mc, player, state.offHandItem, state.offHandMapRenderState);
    }

    private static boolean extractMapRenderState(Minecraft mc, AbstractClientPlayer player, ItemStack itemStack, MapRenderState state) {
        final MapId mapId = itemStack.get(DataComponents.MAP_ID);
        final MapItemSavedData mapData = mapId == null ? null : MapItem.getSavedData(mapId, player.level());
        if (mapId != null && mapData != null) {
            mc.getMapRenderer().extractRenderState(mapId, mapData, state);
            return true;
        }
        return false;
    }

    // see FirstPersonHandsAndItems#evaluateWhichHandsToRender, which only accepts LocalPlayer
    public static FirstPersonHandsAndItemsRenderState.HandRenderSelection evaluateWhichHandsToRender(AbstractClientPlayer player) {
        final ItemStack mainHandItem = resolveMainHand(player);
        final ItemStack offhandItem = resolveOffHand(player);
        final boolean holdsBow = mainHandItem.is(Items.BOW) || offhandItem.is(Items.BOW);
        final boolean holdsCrossbow = mainHandItem.is(Items.CROSSBOW) || offhandItem.is(Items.CROSSBOW);

        if (!holdsBow && !holdsCrossbow) {
            return FirstPersonHandsAndItemsRenderState.HandRenderSelection.RENDER_BOTH_HANDS;
        } else if (!player.isUsingItem()) {
            return isChargedCrossbow(mainHandItem)
                    ? FirstPersonHandsAndItemsRenderState.HandRenderSelection.RENDER_MAIN_HAND_ONLY
                    : FirstPersonHandsAndItemsRenderState.HandRenderSelection.RENDER_BOTH_HANDS;
        } else {
            final ItemStack usedItemStack = player.getUseItem();
            final InteractionHand usedHand = player.getUsedItemHand();
            if (!usedItemStack.is(Items.BOW) && !usedItemStack.is(Items.CROSSBOW)) {
                return usedHand == InteractionHand.MAIN_HAND && isChargedCrossbow(offhandItem)
                        ? FirstPersonHandsAndItemsRenderState.HandRenderSelection.RENDER_MAIN_HAND_ONLY
                        : FirstPersonHandsAndItemsRenderState.HandRenderSelection.RENDER_BOTH_HANDS;
            }
            return FirstPersonHandsAndItemsRenderState.HandRenderSelection.onlyForHand(usedHand);
        }
    }

    private static boolean isChargedCrossbow(ItemStack item) {
        return item.is(Items.CROSSBOW) && CrossbowItem.isCharged(item);
    }
}
