package com.guilh.mca_vampirism_compat.service.faction.hunter;

import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.service.faction.McaFactionCombatRules;
import com.guilh.mca_vampirism_compat.util.McaHunterTargetUtil;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.world.entity.LivingEntity;

public final class McaHunterCombatRules implements McaFactionCombatRules {
    @Override
    public boolean canTarget(VillagerEntityMCA villager, LivingEntity target) {
        return McaHunterTargetUtil.isValidHunterTarget(villager, target);
    }

    @Override
    public String targetDropReason(VillagerEntityMCA villager, LivingEntity target) {
        McaHunterTargetUtil.DropReason dropReason = McaHunterTargetUtil.targetDropReason(villager, target);
        return dropReason == McaHunterTargetUtil.DropReason.NONE ? null : dropReason.name();
    }

    @Override
    public int targetPriority(VillagerEntityMCA villager, LivingEntity target) {
        return switch (McaHunterTargetUtil.targetReason(villager, target)) {
            case MCA_VAMPIRE_VILLAGER -> 0;
            case MCA_WEREWOLF_VILLAGER -> 1;
            case RECENT_VAMPIRE_AGGRESSOR, VAMPIRE_PLAYER, VAMPIRE_MOB -> 0;
            case TRANSFORMED_WEREWOLF -> 1;
            case PROTECTED_AGGRESSOR -> 0;
            case NONE -> 100;
        };
    }

    @Override
    public Object targetReason(VillagerEntityMCA villager, LivingEntity target) {
        return McaHunterTargetUtil.targetReason(villager, target);
    }

    @Override
    public double followRange(VillagerEntityMCA villager) {
        return McaVampirismCompatConfig.hunterVillagerFollowRange();
    }

    @Override
    public float attackDamage(VillagerEntityMCA villager) {
        return (float) McaVampirismCompatConfig.hunterVillagerAttackDamage();
    }
}
