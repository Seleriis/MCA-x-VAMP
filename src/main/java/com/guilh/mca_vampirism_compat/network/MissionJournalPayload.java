package com.guilh.mca_vampirism_compat.network;
import net.minecraft.network.RegistryFriendlyByteBuf;
public record MissionJournalPayload(long session, boolean open, int page, int total, java.util.UUID target, boolean tracked, java.util.List<net.minecraft.network.chat.Component> lines) implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
    public static void encode(MissionJournalPayload payload, RegistryFriendlyByteBuf buffer) { buffer.writeLong(payload.session()); buffer.writeBoolean(payload.open()); buffer.writeVarInt(payload.page()); buffer.writeVarInt(payload.total());
        buffer.writeBoolean(payload.target() != null); if (payload.target() != null) buffer.writeUUID(payload.target());
        buffer.writeBoolean(payload.tracked()); buffer.writeVarInt(payload.lines().size());
        for (var line : payload.lines()) { net.minecraft.network.chat.ComponentSerialization.STREAM_CODEC.encode(buffer, line); } }
    public static MissionJournalPayload decode(RegistryFriendlyByteBuf buffer) { long session=buffer.readLong(); boolean open=buffer.readBoolean(); int page=buffer.readVarInt(), total=buffer.readVarInt();
        java.util.UUID target=buffer.readBoolean()?buffer.readUUID():null; boolean tracked=buffer.readBoolean(); int count=buffer.readVarInt();
        if (count < 0 || count > 12 || page < 0 || total < 0) throw new IllegalArgumentException("Invalid diary page");
        var lines = new java.util.ArrayList<net.minecraft.network.chat.Component>();
        for (int i=0;i<count;i++) lines.add(net.minecraft.network.chat.ComponentSerialization.STREAM_CODEC.decode(buffer));
        return new MissionJournalPayload(session,open,page,total,target,tracked,java.util.List.copyOf(lines)); }

    public static final Type<MissionJournalPayload> TYPE = new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("mca_vamp_compat", "mission_journal"));
    public static final net.minecraft.network.codec.StreamCodec<RegistryFriendlyByteBuf, MissionJournalPayload> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((buffer,payload)->encode(payload,buffer), MissionJournalPayload::decode);
    @Override public Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type() { return TYPE; }
}
