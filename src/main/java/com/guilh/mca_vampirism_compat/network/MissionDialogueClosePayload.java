package com.guilh.mca_vampirism_compat.network;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/** Tells one client to close a completed MCA favor conversation safely. */
public record MissionDialogueClosePayload(UUID villagerId) implements CustomPacketPayload {
    public static final Type<MissionDialogueClosePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(McaVampirismCompat.MOD_ID, "mission_dialogue_close")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, MissionDialogueClosePayload> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, payload) -> buffer.writeUUID(payload.villagerId()),
                    buffer -> new MissionDialogueClosePayload(buffer.readUUID())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
