package com.guilh.mca_vampirism_compat.service.faction.vampire;

import com.guilh.mca_vampirism_compat.service.faction.McaFactionCombatRules;
import com.guilh.mca_vampirism_compat.service.McaHunterAlignmentService;
import com.guilh.mca_vampirism_compat.service.McaWerewolfStateService;
import com.guilh.mca_vampirism_compat.util.McaVampireTargetUtil;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.world.entity.LivingEntity;

public final class McaVampireCombatRules implements McaFactionCombatRules {
    @Override
    public boolean canTarget(VillagerEntityMCA villager, LivingEntity target) {
        return McaVampireTargetUtil.isValidTarget(villager, target);
    }

    @Override
    public int targetPriority(VillagerEntityMCA villager, LivingEntity target) {
        if (target instanceof VillagerEntityMCA targetVillager) {
            if (McaHunterAlignmentService.isMcaHunterVillager(targetVillager)) {
                return 0;
            }
            if (McaWerewolfStateService.isWerewolf(targetVillager)) {
                return 1;
            }
            return 100;
        }
        if (McaVampireTargetUtil.isHunterTarget(target)) {
            return 0;
        }
        if (McaVampireTargetUtil.isTransformedWerewolfTarget(target)) {
            return 1;
        }
        if (McaVampireTargetUtil.isInfectionBiteTarget(villager, target)) {
            return 2;
        }
        return 100;
    }

    @Override
    public Object targetReason(VillagerEntityMCA villager, LivingEntity target) {
        if (target instanceof VillagerEntityMCA targetVillager) {
            if (McaWerewolfStateService.isWerewolf(targetVillager)) {
                return "MCA_WEREWOLF";
            }
            if (McaHunterAlignmentService.isMcaHunterVillager(targetVillager)) {
                return "MCA_HUNTER";
            }
        }
        if (McaVampireTargetUtil.isHunterTarget(target)) {
            return "HUNTER";
        }
        if (McaVampireTargetUtil.isTransformedWerewolfTarget(target)) {
            return "TRANSFORMED_WEREWOLF";
        }
        return "FACTION_ENEMY";
    }

    @Override
    public double followRange(VillagerEntityMCA villager) {
        return 16.0D;
    }

    @Override
    public float attackDamage(VillagerEntityMCA villager) {
        return -1.0F;
    }
}
