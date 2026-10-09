package com.guilh.mca_vampirism_compat.service.faction;

import forge.net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.world.entity.LivingEntity;

public interface McaFactionCombatRules {
    boolean canTarget(VillagerEntityMCA villager, LivingEntity target);

    default String targetDropReason(VillagerEntityMCA villager, LivingEntity target) {
        return null;
    }

    int targetPriority(VillagerEntityMCA villager, LivingEntity target);

    Object targetReason(VillagerEntityMCA villager, LivingEntity target);

    double followRange(VillagerEntityMCA villager);

    float attackDamage(VillagerEntityMCA villager);
}
