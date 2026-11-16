package com.hpfxd.spectatorplus.fabric.sync.packet;

import com.hpfxd.spectatorplus.fabric.sync.ClientboundSyncPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record ClientboundScreenSyncPacket(
        UUID playerId,
        int flags
) implements ClientboundSyncPacket {
    public static final StreamCodec<FriendlyByteBuf, ClientboundScreenSyncPacket> STREAM_CODEC = CustomPacketPayload.codec(ClientboundScreenSyncPacket::write, ClientboundScreenSyncPacket::new);
    public static final CustomPacketPayload.Type<ClientboundScreenSyncPacket> TYPE = new CustomPacketPayload.Type<>(Identifier.parse("spectatorplus:screen_sync"));

    public ClientboundScreenSyncPacket(FriendlyByteBuf buf) {
        this(buf.readUUID(), buf.readByte());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerId);
        buf.writeByte(this.flags);
    }

    public static int flags(boolean isSurvivalInventory, boolean isClientRequested, boolean hasDummySlots) {
        int flags = 0;
        if (isSurvivalInventory) {
            flags |= 0x01;
        }
        if (isClientRequested) {
            flags |= 0x02;
        }
        if (hasDummySlots) {
            flags |= 0x04;
        }
        return flags;
    }

    public boolean isSurvivalInventory() {
        return (this.flags & 1) == 1;
    }

    public boolean isClientRequested() {
        return (this.flags >> 1 & 1) == 1;
    }

    public boolean hasDummySlots() {
        return (this.flags >> 2 & 1) == 1;
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean canSend(ServerPlayer receiver) {
        return true;
    }
}
