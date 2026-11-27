package com.guilh.mca_vampirism_compat.network;
import net.minecraft.network.FriendlyByteBuf;
public record MissionJournalPayload(long session, boolean open, int page, int total, java.util.UUID target, boolean tracked, java.util.List<net.minecraft.network.chat.Component> lines) {
    public static void encode(MissionJournalPayload payload, FriendlyByteBuf buffer) { buffer.writeLong(payload.session()); buffer.writeBoolean(payload.open()); buffer.writeVarInt(payload.page()); buffer.writeVarInt(payload.total());
        buffer.writeBoolean(payload.target() != null); if (payload.target() != null) buffer.writeUUID(payload.target());
        buffer.writeBoolean(payload.tracked()); buffer.writeVarInt(payload.lines().size());
        for (var line : payload.lines()) { buffer.writeComponent(line); } }
    public static MissionJournalPayload decode(FriendlyByteBuf buffer) { long session=buffer.readLong(); boolean open=buffer.readBoolean(); int page=buffer.readVarInt(), total=buffer.readVarInt();
        java.util.UUID target=buffer.readBoolean()?buffer.readUUID():null; boolean tracked=buffer.readBoolean(); int count=buffer.readVarInt();
        if (count < 0 || count > 12 || page < 0 || total < 0) throw new IllegalArgumentException("Invalid diary page");
        var lines = new java.util.ArrayList<net.minecraft.network.chat.Component>();
        for (int i=0;i<count;i++) lines.add(buffer.readComponent());
        return new MissionJournalPayload(session,open,page,total,target,tracked,java.util.List.copyOf(lines)); }
}
