package com.guilh.mca_vampirism_compat.service.faction.werewolf;

import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.service.faction.McaFactionCombatRules;
import com.guilh.mca_vampirism_compat.util.McaWerewolfTargetUtil;
import forge.net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.world.entity.LivingEntity;

public final class McaWerewolfCombatRules implements McaFactionCombatRules {
    @Override
    public boolean canTarget(VillagerEntityMCA villager, LivingEntity target) {
        return McaWerewolfTargetUtil.isValidWerewolfTarget(villager, target);
    }

    @Override
    public int targetPriority(VillagerEntityMCA villager, LivingEntity target) {
        return switch (McaWerewolfTargetUtil.targetReason(villager, target)) {
            case MCA_HUNTER_VILLAGER -> 0;
            case MCA_VAMPIRE_VILLAGER -> 1;
            case HUNTER_PLAYER, HUNTER_MOB -> 0;
            case VAMPIRE_PLAYER, VAMPIRE_MOB -> 1;
            case PROTECTED_AGGRESSOR -> 0;
            case NONE -> 100;
        };
    }

    @Override
    public Object targetReason(VillagerEntityMCA villager, LivingEntity target) {
        return McaWerewolfTargetUtil.targetReason(villager, target);
    }

    @Override
    public double followRange(VillagerEntityMCA villager) {
        return McaVampirismCompatConfig.werewolfVillagerFollowRange();
    }

    @Override
    public float attackDamage(VillagerEntityMCA villager) {
        return (float) McaVampirismCompatConfig.werewolfVillagerAttackDamage();
    }
}
