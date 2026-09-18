package com.guilh.mca_vampirism_compat.network;

import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

/** Client-only visual marker for the villager a player agreed to help. */
public record MissionHighlightPayload(UUID villagerId, boolean highlighted) {
    public static void encode(MissionHighlightPayload payload, FriendlyByteBuf buffer) {
        buffer.writeUUID(payload.villagerId());
        buffer.writeBoolean(payload.highlighted());
    }

    public static MissionHighlightPayload decode(FriendlyByteBuf buffer) {
        return new MissionHighlightPayload(buffer.readUUID(), buffer.readBoolean());
    }
}
