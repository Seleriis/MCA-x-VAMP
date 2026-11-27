package com.guilh.mca_vampirism_compat.network;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.easyvillagers.client.EasyVillagersCycleClientState;
import com.guilh.mca_vampirism_compat.mission.MissionHighlightClientState;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class ModNetwork {
    private ModNetwork() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar(McaVampirismCompat.MOD_ID)
                .versioned("4")
                .playToClient(MissionJournalPayload.TYPE, MissionJournalPayload.STREAM_CODEC,
                        (payload, context) -> context.enqueueWork(() -> MissionJournalClientBridge.receive(payload)))
                .playToServer(MissionJournalRequest.TYPE, MissionJournalRequest.STREAM_CODEC,
                        (payload, context) -> context.enqueueWork(() -> {
                            if (context.player() instanceof net.minecraft.server.level.ServerPlayer player)
                                com.guilh.mca_vampirism_compat.mission.OccultVillageMissionService.journalRequest(player, payload);
                        }))
                .playToClient(
                        EasyVillagersCycleStatePayload.TYPE,
                        EasyVillagersCycleStatePayload.STREAM_CODEC,
                        (payload, context) -> context.enqueueWork(
                                () -> EasyVillagersCycleClientState.update(
                                        payload.containerId(),
                                        payload.enabled()
                                )
                        )
                )
                .playToClient(
                        MissionHighlightPayload.TYPE,
                        MissionHighlightPayload.STREAM_CODEC,
                        (payload, context) -> context.enqueueWork(
                                () -> MissionHighlightClientState.set(
                                        payload.villagerId(), payload.highlighted()
                                )
                        )
                )
                .playToClient(
                        MissionDialogueSpeechPayload.TYPE,
                        MissionDialogueSpeechPayload.STREAM_CODEC,
                        (payload, context) -> context.enqueueWork(
                                () -> MissionDialogueClientBridge.speak(payload)
                        )
                )
                .playToClient(
                        MissionDialogueClosePayload.TYPE,
                        MissionDialogueClosePayload.STREAM_CODEC,
                        (payload, context) -> context.enqueueWork(
                                () -> MissionDialogueClientBridge.close(payload)
                        )
                );
    }
}
