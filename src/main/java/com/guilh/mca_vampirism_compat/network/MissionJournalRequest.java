package com.guilh.mca_vampirism_compat.network;
import net.minecraft.network.RegistryFriendlyByteBuf;
public record MissionJournalRequest(long session, int page, boolean select, java.util.UUID target) implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
    public static void encode(MissionJournalRequest payload, RegistryFriendlyByteBuf buffer) { buffer.writeLong(payload.session()); buffer.writeVarInt(payload.page()); buffer.writeBoolean(payload.select());
        buffer.writeBoolean(payload.target()!=null); if(payload.target()!=null) buffer.writeUUID(payload.target()); }
    public static MissionJournalRequest decode(RegistryFriendlyByteBuf buffer) { return new MissionJournalRequest(buffer.readLong(),buffer.readVarInt(),buffer.readBoolean(),buffer.readBoolean()?buffer.readUUID():null); }

    public static final Type<MissionJournalRequest> TYPE = new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("mca_vamp_compat", "mission_journal_request"));
    public static final net.minecraft.network.codec.StreamCodec<RegistryFriendlyByteBuf, MissionJournalRequest> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((buffer,payload)->encode(payload,buffer), MissionJournalRequest::decode);
    @Override public Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type() { return TYPE; }
}
