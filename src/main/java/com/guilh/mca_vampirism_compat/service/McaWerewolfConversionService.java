package com.guilh.mca_vampirism_compat.service;

import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.world.entity.LivingEntity;

public final class McaWerewolfConversionService {
    private McaWerewolfConversionService() {
    }

    public static void handleWerewolfAttack(LivingEntity source, LivingEntity target) {
        McaWerewolfInfectionService.handleWerewolfAttack(source, target);
    }

    public static boolean tryApplyWerewolfInfection(LivingEntity source, VillagerEntityMCA target, boolean forceChance) {
        return McaWerewolfInfectionService.tryApplyWerewolfInfection(source, target, forceChance);
    }

    public static boolean canApplyWerewolfInfection(LivingEntity source, VillagerEntityMCA target) {
        return McaWerewolfInfectionService.canApplyWerewolfInfection(source, target);
    }

    public static void processWerewolfTick(VillagerEntityMCA villager) {
        McaWerewolfInfectionService.processWerewolfTick(villager);
    }

    public static boolean isLupusSanguinemActive(VillagerEntityMCA villager) {
        return McaWerewolfInfectionService.isLupusSanguinemActive(villager);
    }

    public static boolean removeLupusSanguinem(VillagerEntityMCA villager) {
        return McaWerewolfInfectionService.removeLupusSanguinem(villager);
    }
}
