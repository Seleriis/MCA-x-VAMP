package com.guilh.mca_vampirism_compat.ai;

import com.guilh.mca_vampirism_compat.service.McaCommandRespectService;
import com.guilh.mca_vampirism_compat.util.McaVampireTargetUtil;
import com.guilh.mca_vampirism_compat.util.McaVillagerUtil;
import forge.net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

public class McaVampireInfectBiteGoal extends MeleeAttackGoal {
    private final PathfinderMob vampire;

    public McaVampireInfectBiteGoal(PathfinderMob vampire, double speedModifier, boolean followingTargetEvenIfNotSeen) {
        super(vampire, speedModifier, followingTargetEvenIfNotSeen);
        this.vampire = vampire;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = vampire.getTarget();
        return McaVillagerUtil.isMcaVampire(vampire)
                && !isCommandSuppressed()
                && target != null
                && McaVampireTargetUtil.isInfectionBiteTarget(vampire, target)
                && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = vampire.getTarget();
        return McaVillagerUtil.isMcaVampire(vampire)
                && !isCommandSuppressed()
                && target != null
                && McaVampireTargetUtil.isInfectionBiteTarget(vampire, target)
                && super.canContinueToUse();
    }

    @Override
    public void tick() {
        LivingEntity target = vampire.getTarget();
        if (target != null && target.isAlive()) {
            vampire.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }
        super.tick();
    }

    @Override
    public void stop() {
        if (vampire instanceof VillagerEntityMCA villager
                && McaCommandRespectService.shouldPreserveMcaMovement(villager)) {
            vampire.setAggressive(false);
            return;
        }
        super.stop();
    }

    private boolean isCommandSuppressed() {
        return vampire instanceof VillagerEntityMCA villager
                && McaCommandRespectService.shouldSuppressAutonomousBiteOrHunt(villager);
    }
}
