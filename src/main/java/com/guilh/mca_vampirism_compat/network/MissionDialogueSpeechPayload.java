package com.guilh.mca_vampirism_compat.network;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/** Carries a resident's line to MCA's native local chat messenger. */
public record MissionDialogueSpeechPayload(UUID villagerId, Component phrase) implements CustomPacketPayload {
    public static final Type<MissionDialogueSpeechPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(McaVampirismCompat.MOD_ID, "mission_dialogue_speech")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, MissionDialogueSpeechPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, payload) -> {
                        buffer.writeUUID(payload.villagerId());
                        ComponentSerialization.STREAM_CODEC.encode(buffer, payload.phrase());
                    },
                    buffer -> new MissionDialogueSpeechPayload(
                            buffer.readUUID(),
                            ComponentSerialization.STREAM_CODEC.decode(buffer)
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
