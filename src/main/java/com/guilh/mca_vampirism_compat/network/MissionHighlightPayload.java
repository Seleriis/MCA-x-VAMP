package com.guilh.mca_vampirism_compat.network;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/** Client-only visual marker for the villager a player agreed to help. */
public record MissionHighlightPayload(UUID villagerId, boolean highlighted) implements CustomPacketPayload {
    public static final Type<MissionHighlightPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(McaVampirismCompat.MOD_ID, "mission_highlight")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, MissionHighlightPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, payload) -> {
                        buffer.writeUUID(payload.villagerId());
                        buffer.writeBoolean(payload.highlighted());
                    },
                    buffer -> new MissionHighlightPayload(buffer.readUUID(), buffer.readBoolean())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
