package com.guilh.mca_vampirism_compat.easyvillagers.client;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = McaVampirismCompat.MOD_ID, value = Dist.CLIENT)
public final class EasyVillagersCycleClientEvents {
    private EasyVillagersCycleClientEvents() {
    }

    @SubscribeEvent
    public static void clearBeforeOpening(ScreenEvent.Opening event) {
        EasyVillagersCycleClientState.clear();
    }
}
