package com.guilh.mca_vampirism_compat.network;

import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

/** Tells one client to close a completed MCA favor conversation safely. */
public record MissionDialogueClosePayload(UUID villagerId) {
    public static void encode(MissionDialogueClosePayload payload, FriendlyByteBuf buffer) {
        buffer.writeUUID(payload.villagerId());
    }

    public static MissionDialogueClosePayload decode(FriendlyByteBuf buffer) {
        return new MissionDialogueClosePayload(buffer.readUUID());
    }
}
