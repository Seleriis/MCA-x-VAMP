package com.guilh.mca_vampirism_compat.network.client;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.network.MissionDialogueClient;
import com.guilh.mca_vampirism_compat.network.MissionDialogueClientBridge;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/** Installs physical-client mission handlers without exposing client classes on dedicated servers. */
@EventBusSubscriber(modid = McaVampirismCompat.MOD_ID, value = Dist.CLIENT)
public final class MissionDialogueClientSetup {
    private MissionDialogueClientSetup() {
    }

    @SubscribeEvent
    public static void installHandlers(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MissionDialogueClientBridge.install(
                MissionDialogueClient::speak,
                MissionDialogueClient::closeIfStillTalkingTo
        ));
    }
}
