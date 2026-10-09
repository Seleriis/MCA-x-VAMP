package com.guilh.mca_vampirism_compat.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import java.util.UUID;

/** Carries a resident's line to MCA's native local chat messenger. */
public record MissionDialogueSpeechPayload(UUID villagerId, Component phrase) {
    public static void encode(MissionDialogueSpeechPayload payload, FriendlyByteBuf buffer) {
        buffer.writeUUID(payload.villagerId());
        buffer.writeComponent(payload.phrase());
    }

    public static MissionDialogueSpeechPayload decode(FriendlyByteBuf buffer) {
        return new MissionDialogueSpeechPayload(buffer.readUUID(), buffer.readComponent());
    }
}
