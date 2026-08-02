package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.util.McaHunterTargetUtil;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.UUID;

public final class McaVillageVampireThreatService {
    private McaVillageVampireThreatService() {
    }

    public static void recordVampireThreat(LivingEntity source, LivingEntity center, String reason) {
        if (!McaVampirismCompatConfig.enableMcaVillagerDefenseAgainstVampires()
                || center == null
                || center.level().isClientSide
                || !(center.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        int radius = McaVampirismCompatConfig.vampireThreatAlertRadius();
        AABB area = center.getBoundingBox().inflate(radius, 8.0D, radius);
        UUID targetUuid = source == null ? null : source.getUUID();
        for (VillagerEntityMCA villager : serverLevel.getEntitiesOfClass(VillagerEntityMCA.class, area)) {
            if (villager == center || !McaDefensiveVillagerService.canEnterDefensiveMode(villager)) {
                continue;
            }
            ModCapabilities.get(villager).ifPresent(state -> {
                int count = state.recordVampireThreat(
                        villager.level().getGameTime(),
                        McaVampirismCompatConfig.vampireThreatWindowTicks()
                );
                McaVampirismCompat.debugLog(
                        "[DEFENSE] vampire threat recorded villager={} count={} reason={} source={}",
                        villager.getUUID(),
                        count,
                        reason,
                        targetUuid
                );
                if (count >= McaVampirismCompatConfig.vampireThreatCountToTriggerDefense()) {
                    McaDefensiveVillagerService.enableDefense(villager, reason, targetUuid);
                    if (source != null) {
                        McaHunterTargetUtil.markRetaliation(villager, source);
                    }
                }
            });
        }
    }

    public static void recordConversionThreat(VillagerEntityMCA convertedVillager) {
        if (convertedVillager == null || convertedVillager.level().isClientSide) {
            return;
        }
        recordVampireThreat(convertedVillager, convertedVillager, "conversion");
    }
}
