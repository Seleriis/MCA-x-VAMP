package com.guilh.mca_vampirism_compat.network;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record EasyVillagersCycleStatePayload(int containerId, boolean enabled)
        implements CustomPacketPayload {
    public static final Type<EasyVillagersCycleStatePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(
                    McaVampirismCompat.MOD_ID,
                    "easy_villagers_cycle_state"
            )
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, EasyVillagersCycleStatePayload> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, payload) -> {
                        buffer.writeVarInt(payload.containerId());
                        buffer.writeBoolean(payload.enabled());
                    },
                    buffer -> new EasyVillagersCycleStatePayload(
                            buffer.readVarInt(),
                            buffer.readBoolean()
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
