package com.guilh.mca_vampirism_compat.network;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.easyvillagers.client.EasyVillagersCycleClientState;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class ModNetwork {
    private ModNetwork() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar(McaVampirismCompat.MOD_ID)
                .versioned("1")
                .playToClient(
                        EasyVillagersCycleStatePayload.TYPE,
                        EasyVillagersCycleStatePayload.STREAM_CODEC,
                        (payload, context) -> context.enqueueWork(
                                () -> EasyVillagersCycleClientState.update(
                                        payload.containerId(),
                                        payload.enabled()
                                )
                        )
                );
    }
}
