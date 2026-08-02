package com.guilh.mca_vampirism_compat.book;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = McaVampirismCompat.MOD_ID)
public final class OccultVillageBookEvents {
    private OccultVillageBookEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            OccultVillageBookDeliveryService.onPlayerTick(player);
        }
    }
}
