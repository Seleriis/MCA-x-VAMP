package com.guilh.mca_vampirism_compat.easyvillagers.client;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = McaVampirismCompat.MOD_ID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class EasyVillagersCycleClientEvents {
    private EasyVillagersCycleClientEvents() {
    }

    @SubscribeEvent
    public static void clearBeforeOpening(ScreenEvent.Opening event) {
        EasyVillagersCycleClientState.clear();
    }
}
