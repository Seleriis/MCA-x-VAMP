package com.guilh.mca_vampirism_compat.network;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.easyvillagers.client.EasyVillagersCycleClientState;
import com.guilh.mca_vampirism_compat.mission.MissionHighlightClientState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public final class ModNetwork {
    private static final String PROTOCOL_VERSION = "4";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(McaVampirismCompat.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static boolean registered;

    private ModNetwork() {
    }

    /** Registers the fixed clientbound protocol once during mod construction. */
    public static synchronized void register() {
        if (registered) {
            return;
        }

        int discriminator = 0;
        CHANNEL.messageBuilder(
                        EasyVillagersCycleStatePayload.class,
                        discriminator++,
                        NetworkDirection.PLAY_TO_CLIENT
                )
                .encoder(EasyVillagersCycleStatePayload::encode)
                .decoder(EasyVillagersCycleStatePayload::decode)
                .consumerMainThread(ModNetwork::handleEasyVillagersCycleState)
                .add();
        CHANNEL.messageBuilder(
                        MissionHighlightPayload.class,
                        discriminator++,
                        NetworkDirection.PLAY_TO_CLIENT
                )
                .encoder(MissionHighlightPayload::encode)
                .decoder(MissionHighlightPayload::decode)
                .consumerMainThread(ModNetwork::handleMissionHighlight)
                .add();
        CHANNEL.messageBuilder(
                        MissionDialogueSpeechPayload.class,
                        discriminator++,
                        NetworkDirection.PLAY_TO_CLIENT
                )
                .encoder(MissionDialogueSpeechPayload::encode)
                .decoder(MissionDialogueSpeechPayload::decode)
                .consumerMainThread(ModNetwork::handleMissionDialogueSpeech)
                .add();
        CHANNEL.messageBuilder(
                        MissionDialogueClosePayload.class,
                        discriminator++,
                        NetworkDirection.PLAY_TO_CLIENT
                )
                .encoder(MissionDialogueClosePayload::encode)
                .decoder(MissionDialogueClosePayload::decode)
                .consumerMainThread(ModNetwork::handleMissionDialogueClose)
                .add();
        CHANNEL.messageBuilder(MissionJournalPayload.class, discriminator++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(MissionJournalPayload::encode).decoder(MissionJournalPayload::decode)
                .consumerMainThread((payload, context) -> { MissionJournalClientBridge.receive(payload); context.get().setPacketHandled(true); }).add();
        CHANNEL.messageBuilder(MissionJournalRequest.class, discriminator++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(MissionJournalRequest::encode).decoder(MissionJournalRequest::decode)
                .consumerMainThread((payload, context) -> {
                    var player = context.get().getSender();
                    if (player != null) com.guilh.mca_vampirism_compat.mission.OccultVillageMissionService.journalRequest(player, payload);
                    context.get().setPacketHandled(true);
                }).add();
        registered = true;
    }

    /** Common Forge 1.20.1 replacement for NeoForge PacketDistributor.sendToPlayer. */
    public static <T> void sendToPlayer(ServerPlayer player, T message) {
        if (player != null && player.connection != null && message != null) {
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
        }
    }

    private static void handleEasyVillagersCycleState(
            EasyVillagersCycleStatePayload payload,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> EasyVillagersCycleClientState.update(payload.containerId(), payload.enabled()));
        context.setPacketHandled(true);
    }

    private static void handleMissionHighlight(
            MissionHighlightPayload payload,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> MissionHighlightClientState.set(payload.villagerId(), payload.highlighted()));
        context.setPacketHandled(true);
    }

    private static void handleMissionDialogueSpeech(
            MissionDialogueSpeechPayload payload,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> MissionDialogueClientBridge.speak(payload));
        context.setPacketHandled(true);
    }

    private static void handleMissionDialogueClose(
            MissionDialogueClosePayload payload,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> MissionDialogueClientBridge.close(payload));
        context.setPacketHandled(true);
    }
}
