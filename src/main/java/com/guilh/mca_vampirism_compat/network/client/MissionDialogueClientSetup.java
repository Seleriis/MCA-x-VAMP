package com.guilh.mca_vampirism_compat.network.client;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.network.MissionDialogueClient;
import com.guilh.mca_vampirism_compat.network.MissionDialogueClientBridge;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Installs physical-client mission handlers without exposing client classes on dedicated servers. */
@Mod.EventBusSubscriber(
        modid = McaVampirismCompat.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
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
