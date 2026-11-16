package com.manhunt.entitycounter.mixin;

import com.manhunt.entitycounter.EntityCounterClient;
import net.minecraft.client.gui.components.debug.DebugScreenEntryList;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Re-apply the classic entity counter after debug profiles / options are loaded,
 * so a DEFAULT profile that has entity_render_stats OFF cannot hide it again.
 */
@Mixin(DebugScreenEntryList.class)
public abstract class DebugScreenEntryListMixin {
	@Inject(method = "load()V", at = @At("TAIL"))
	private void manhunt$afterLoad(CallbackInfo ci) {
		((DebugScreenEntryList) (Object) this).setStatus(DebugScreenEntries.ENTITY_RENDER_STATS, DebugScreenEntryStatus.IN_OVERLAY);
	}

	@Inject(method = "loadProfile(Lnet/minecraft/client/gui/components/debug/DebugScreenProfile;)V", at = @At("TAIL"))
	private void manhunt$afterLoadProfile(DebugScreenProfile profile, CallbackInfo ci) {
		((DebugScreenEntryList) (Object) this).setStatus(DebugScreenEntries.ENTITY_RENDER_STATS, DebugScreenEntryStatus.IN_OVERLAY);
		EntityCounterClient.LOGGER.debug("Re-enabled entity_render_stats after loading profile {}", profile);
	}
}
