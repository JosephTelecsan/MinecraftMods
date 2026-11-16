package com.hpfxd.spectatorplus.fabric.client.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.LevelEntityGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.server.level.BlockDestructionProgress;

@Mixin(ClientLevel.class)
public interface ClientLevelAccessor {
    @Invoker
    LevelEntityGetter<Entity> invokeGetEntities();

    @Accessor
    Int2ObjectMap<BlockDestructionProgress> getDestroyingBlocks();
}
