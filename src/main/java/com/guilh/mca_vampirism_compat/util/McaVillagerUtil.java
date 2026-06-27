package com.guilh.mca_vampirism_compat.util;

import com.guilh.mca_vampirism_compat.service.McaVampireStateService;
import com.guilh.mca_vampirism_compat.service.McaWerewolfStateService;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.VillagerLike;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class McaVillagerUtil {
    private McaVillagerUtil() {
    }

    public static boolean isMcaVillager(Entity entity) {
        return McaVampireStateService.isMcaVillager(entity);
    }

    public static boolean isMcaVillager(LivingEntity entity) {
        return isMcaVillager((Entity) entity);
    }

    public static boolean isMcaVampire(Entity entity) {
        return McaVampireStateService.isVampire(entity);
    }

    public static boolean isMcaWerewolf(Entity entity) {
        return McaWerewolfStateService.isWerewolf(entity);
    }
}
