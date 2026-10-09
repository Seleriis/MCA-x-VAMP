package com.guilh.mca_vampirism_compat.book;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.mission.OccultVillageMissionService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = McaVampirismCompat.MOD_ID)
public final class OccultVillageBookEvents {
    private OccultVillageBookEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player) {
            OccultVillageBookDeliveryService.onPlayerTick(player);
            OccultVillageMissionService.onPlayerTick(player);
        }
    }
}
