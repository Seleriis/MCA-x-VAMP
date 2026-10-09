package com.guilh.mca_vampirism_compat.network;

import net.minecraft.network.FriendlyByteBuf;

/** Forge 1.20.1 SimpleChannel message for the optional Cycle Trades button. */
public record EasyVillagersCycleStatePayload(int containerId, boolean enabled) {
    public static void encode(EasyVillagersCycleStatePayload payload, FriendlyByteBuf buffer) {
        buffer.writeVarInt(payload.containerId());
        buffer.writeBoolean(payload.enabled());
    }

    public static EasyVillagersCycleStatePayload decode(FriendlyByteBuf buffer) {
        return new EasyVillagersCycleStatePayload(buffer.readVarInt(), buffer.readBoolean());
    }
}
