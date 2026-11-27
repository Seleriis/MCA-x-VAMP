package com.guilh.mca_vampirism_compat.network;
import net.minecraft.network.FriendlyByteBuf;
public record MissionJournalRequest(long session, int page, boolean select, java.util.UUID target) {
    public static void encode(MissionJournalRequest payload, FriendlyByteBuf buffer) { buffer.writeLong(payload.session()); buffer.writeVarInt(payload.page()); buffer.writeBoolean(payload.select());
        buffer.writeBoolean(payload.target()!=null); if(payload.target()!=null) buffer.writeUUID(payload.target()); }
    public static MissionJournalRequest decode(FriendlyByteBuf buffer) { return new MissionJournalRequest(buffer.readLong(),buffer.readVarInt(),buffer.readBoolean(),buffer.readBoolean()?buffer.readUUID():null); }
}
