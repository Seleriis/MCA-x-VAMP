package com.guilh.mca_vampirism_compat.service;

import forge.net.conczin.mca.entity.VillagerEntityMCA;

public final class McaWerewolfVillagerService {
    private McaWerewolfVillagerService() {
    }

    public static void ensureWerewolfState(VillagerEntityMCA villager) {
        McaWerewolfStateService.ensureWerewolfState(villager);
    }

    public static void processWerewolfTick(VillagerEntityMCA villager) {
        McaWerewolfInfectionService.processWerewolfTick(villager);
    }
}
