package com.manhunt.pausegame.mixin;

import com.manhunt.pausegame.PauseController;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPickItemFromBlockPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemFromEntityPacket;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Players are ticked from {@link ServerGamePacketListenerImpl#tickPlayer()}, not only via
 * {@code TickRateManager.isEntityFrozen}. Skip that tick for frozen players, reject gameplay
 * packets, and keep idle-timeout from kicking people during long pauses.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
	@Shadow
	public ServerPlayer player;

	@Shadow
	public abstract void resetFlyingTicks();

	@Inject(method = "tickPlayer()Z", at = @At("HEAD"), cancellable = true)
	private void pausegame$skipFrozenPlayerTick(CallbackInfoReturnable<Boolean> cir) {
		if (!PauseController.shouldFreeze(this.player)) {
			return;
		}

		try {
			// Prevent multiplayer.disconnect.idling during long technical pauses / breaks.
			this.player.resetLastActionTime();
			this.resetFlyingTicks();
			this.player.setDeltaMovement(Vec3.ZERO);
		} catch (RuntimeException ignored) {
		}

		// Returning false still runs keepConnectionAlive() in tick().
		cir.setReturnValue(false);
	}

	@Inject(method = "handleMovePlayer(Lnet/minecraft/network/protocol/game/ServerboundMovePlayerPacket;)V", at = @At("HEAD"), cancellable = true)
	private void pausegame$blockMove(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
		this.pausegame$cancelIfFrozen(ci);
	}

	@Inject(method = "handleMoveVehicle(Lnet/minecraft/network/protocol/game/ServerboundMoveVehiclePacket;)V", at = @At("HEAD"), cancellable = true)
	private void pausegame$blockMoveVehicle(ServerboundMoveVehiclePacket packet, CallbackInfo ci) {
		this.pausegame$cancelIfFrozen(ci);
	}

	@Inject(method = "handlePlayerInput(Lnet/minecraft/network/protocol/game/ServerboundPlayerInputPacket;)V", at = @At("HEAD"), cancellable = true)
	private void pausegame$blockInput(ServerboundPlayerInputPacket packet, CallbackInfo ci) {
		this.pausegame$cancelIfFrozen(ci);
	}

	@Inject(method = "handlePlayerAction(Lnet/minecraft/network/protocol/game/ServerboundPlayerActionPacket;)V", at = @At("HEAD"), cancellable = true)
	private void pausegame$blockAction(ServerboundPlayerActionPacket packet, CallbackInfo ci) {
		this.pausegame$cancelIfFrozen(ci);
	}

	@Inject(method = "handleUseItem(Lnet/minecraft/network/protocol/game/ServerboundUseItemPacket;)V", at = @At("HEAD"), cancellable = true)
	private void pausegame$blockUseItem(ServerboundUseItemPacket packet, CallbackInfo ci) {
		this.pausegame$cancelIfFrozen(ci);
	}

	@Inject(method = "handleUseItemOn(Lnet/minecraft/network/protocol/game/ServerboundUseItemOnPacket;)V", at = @At("HEAD"), cancellable = true)
	private void pausegame$blockUseItemOn(ServerboundUseItemOnPacket packet, CallbackInfo ci) {
		this.pausegame$cancelIfFrozen(ci);
	}

	@Inject(method = "handleInteract(Lnet/minecraft/network/protocol/game/ServerboundInteractPacket;)V", at = @At("HEAD"), cancellable = true)
	private void pausegame$blockInteract(ServerboundInteractPacket packet, CallbackInfo ci) {
		this.pausegame$cancelIfFrozen(ci);
	}

	@Inject(method = "handlePlayerCommand(Lnet/minecraft/network/protocol/game/ServerboundPlayerCommandPacket;)V", at = @At("HEAD"), cancellable = true)
	private void pausegame$blockPlayerCommand(ServerboundPlayerCommandPacket packet, CallbackInfo ci) {
		this.pausegame$cancelIfFrozen(ci);
	}

	@Inject(method = "handleContainerClick(Lnet/minecraft/network/protocol/game/ServerboundContainerClickPacket;)V", at = @At("HEAD"), cancellable = true)
	private void pausegame$blockContainerClick(ServerboundContainerClickPacket packet, CallbackInfo ci) {
		this.pausegame$cancelIfFrozen(ci);
	}

	@Inject(method = "handleSetCarriedItem(Lnet/minecraft/network/protocol/game/ServerboundSetCarriedItemPacket;)V", at = @At("HEAD"), cancellable = true)
	private void pausegame$blockHotbar(ServerboundSetCarriedItemPacket packet, CallbackInfo ci) {
		this.pausegame$cancelIfFrozen(ci);
	}

	@Inject(method = "handleSetCreativeModeSlot(Lnet/minecraft/network/protocol/game/ServerboundSetCreativeModeSlotPacket;)V", at = @At("HEAD"), cancellable = true)
	private void pausegame$blockCreativeSlot(ServerboundSetCreativeModeSlotPacket packet, CallbackInfo ci) {
		this.pausegame$cancelIfFrozen(ci);
	}

	@Inject(method = "handlePickItemFromBlock(Lnet/minecraft/network/protocol/game/ServerboundPickItemFromBlockPacket;)V", at = @At("HEAD"), cancellable = true)
	private void pausegame$blockPickBlock(ServerboundPickItemFromBlockPacket packet, CallbackInfo ci) {
		this.pausegame$cancelIfFrozen(ci);
	}

	@Inject(method = "handlePickItemFromEntity(Lnet/minecraft/network/protocol/game/ServerboundPickItemFromEntityPacket;)V", at = @At("HEAD"), cancellable = true)
	private void pausegame$blockPickEntity(ServerboundPickItemFromEntityPacket packet, CallbackInfo ci) {
		this.pausegame$cancelIfFrozen(ci);
	}

	@Inject(method = "handlePlaceRecipe(Lnet/minecraft/network/protocol/game/ServerboundPlaceRecipePacket;)V", at = @At("HEAD"), cancellable = true)
	private void pausegame$blockPlaceRecipe(ServerboundPlaceRecipePacket packet, CallbackInfo ci) {
		this.pausegame$cancelIfFrozen(ci);
	}

	@Inject(method = "handleRenameItem(Lnet/minecraft/network/protocol/game/ServerboundRenameItemPacket;)V", at = @At("HEAD"), cancellable = true)
	private void pausegame$blockRename(ServerboundRenameItemPacket packet, CallbackInfo ci) {
		this.pausegame$cancelIfFrozen(ci);
	}

	@Inject(method = "handleSelectTrade(Lnet/minecraft/network/protocol/game/ServerboundSelectTradePacket;)V", at = @At("HEAD"), cancellable = true)
	private void pausegame$blockTrade(ServerboundSelectTradePacket packet, CallbackInfo ci) {
		this.pausegame$cancelIfFrozen(ci);
	}

	@Inject(method = "handleEditBook(Lnet/minecraft/network/protocol/game/ServerboundEditBookPacket;)V", at = @At("HEAD"), cancellable = true)
	private void pausegame$blockBook(ServerboundEditBookPacket packet, CallbackInfo ci) {
		this.pausegame$cancelIfFrozen(ci);
	}

	@Inject(method = "handleSignUpdate(Lnet/minecraft/network/protocol/game/ServerboundSignUpdatePacket;)V", at = @At("HEAD"), cancellable = true)
	private void pausegame$blockSign(ServerboundSignUpdatePacket packet, CallbackInfo ci) {
		this.pausegame$cancelIfFrozen(ci);
	}

	@Inject(method = "handleTeleportToEntityPacket(Lnet/minecraft/network/protocol/game/ServerboundTeleportToEntityPacket;)V", at = @At("HEAD"), cancellable = true)
	private void pausegame$blockSpectatorTeleport(ServerboundTeleportToEntityPacket packet, CallbackInfo ci) {
		this.pausegame$cancelIfFrozen(ci);
	}

	@Unique
	private void pausegame$cancelIfFrozen(CallbackInfo ci) {
		if (PauseController.shouldFreeze(this.player)) {
			ci.cancel();
		}
	}
}
