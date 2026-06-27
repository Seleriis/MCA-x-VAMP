package com.guilh.mca_vampirism_compat.ai;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.service.McaCommandRespectService;
import com.guilh.mca_vampirism_compat.service.McaSpecialVillagerCombatService;
import com.guilh.mca_vampirism_compat.service.McaSpecialVillagerClassifier;
import com.guilh.mca_vampirism_compat.service.McaSpecialVillagerType;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class McaSpecialMeleeAttackGoal extends Goal {
    private static final double REPATH_TARGET_MOVED_DISTANCE_SQR = 2.25D;

    private final PathfinderMob mob;
    private final double meleeSpeedModifier;
    private final double chaseSpeedModifier;
    private int attackCooldown;
    private int repathCooldown;
    private boolean wasControllingCombatNavigation;
    private LivingEntity lastNavigationTarget;
    private double lastTargetX;
    private double lastTargetY;
    private double lastTargetZ;

    public McaSpecialMeleeAttackGoal(PathfinderMob mob) {
        this.mob = mob;
        this.meleeSpeedModifier = Math.min(
                McaVampirismCompatConfig.specialVillagerMeleeGoalSpeed(),
                Math.min(McaVampirismCompatConfig.specialVillagerMaxNavigationSpeed(), 1.0D)
        );
        this.chaseSpeedModifier = Math.min(
                McaVampirismCompatConfig.specialVillagerChaseGoalSpeed(),
                Math.min(McaVampirismCompatConfig.specialVillagerMaxNavigationSpeed(), 1.0D)
        );
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return mob instanceof VillagerEntityMCA villager
                && McaSpecialVillagerCombatService.isValidMeleeTarget(villager, mob.getTarget());
    }

    @Override
    public boolean canContinueToUse() {
        return mob instanceof VillagerEntityMCA villager
                && McaSpecialVillagerCombatService.isValidMeleeTarget(villager, mob.getTarget())
                && McaSpecialVillagerCombatService.targetDropReason(villager, mob.getTarget()) == null;
    }

    @Override
    public void start() {
        mob.setAggressive(true);
        attackCooldown = 0;
        repathCooldown = 0;
        wasControllingCombatNavigation = false;
        resetNavigationTracking();
    }

    @Override
    public void stop() {
        mob.setAggressive(false);
        boolean preserveMcaMovement = mob instanceof VillagerEntityMCA villager
                && McaCommandRespectService.shouldPreserveMcaMovement(villager);
        if (wasControllingCombatNavigation && !preserveMcaMovement) {
            mob.getNavigation().stop();
            if (mob instanceof VillagerEntityMCA villager) {
                McaVampirismCompat.verboseAiLog(
                        "[MOVEMENT] combat navigation stopped villager={} reason=GOAL_STOP",
                        villager.getUUID()
                );
            }
        } else if (wasControllingCombatNavigation && mob instanceof VillagerEntityMCA villager) {
            McaVampirismCompat.verboseAiLog(
                    "[MOVEMENT] preserve MCA navigation villager={} reason={}",
                    villager.getUUID(),
                    McaCommandRespectService.getCommandState(villager)
            );
        }
        wasControllingCombatNavigation = false;
        repathCooldown = 0;
        resetNavigationTracking();
    }

    @Override
    public void tick() {
        if (!(mob instanceof VillagerEntityMCA villager)) {
            return;
        }
        LivingEntity target = mob.getTarget();
        String dropReason = McaSpecialVillagerCombatService.targetDropReason(villager, target);
        if (dropReason != null) {
            McaSpecialVillagerCombatService.dropTarget(villager, dropReason);
            return;
        }
        if (!McaSpecialVillagerCombatService.isValidMeleeTarget(villager, target)) {
            return;
        }

        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (repathCooldown > 0) {
            repathCooldown--;
        }
        double distanceToTarget = mob.distanceToSqr(target);
        double attackReach = attackReachSqr(target);
        double speedModifier = distanceToTarget > attackReach
                ? chaseSpeedModifier
                : meleeSpeedModifier;
        if (distanceToTarget > attackReach) {
            boolean wasAlreadyControllingNavigation = wasControllingCombatNavigation;
            if (shouldRepathTo(target) && mob.getNavigation().moveTo(target, speedModifier)) {
                wasControllingCombatNavigation = true;
                markNavigationTarget(target);
                if (!wasAlreadyControllingNavigation) {
                    McaVampirismCompat.verboseAiLog(
                            "[MOVEMENT] combat navigation started villager={} target={} speed={}",
                            villager.getUUID(),
                            target.getUUID(),
                            speedModifier
                    );
                }
            }
        } else if (wasControllingCombatNavigation) {
            mob.getNavigation().stop();
            wasControllingCombatNavigation = false;
            resetNavigationTracking();
            McaVampirismCompat.verboseAiLog(
                    "[MOVEMENT] combat navigation stopped villager={} reason=IN_ATTACK_RANGE",
                    villager.getUUID()
            );
        }
        if (villager.tickCount % 20 == 0) {
            McaVampirismCompat.verboseAiLog(
                    "[MOVEMENT] looking at combat target villager={} target={}",
                    villager.getUUID(),
                    target.getUUID()
            );
        }
        if (attackCooldown > 0) {
            attackCooldown--;
        }
        if (distanceToTarget > attackReach || attackCooldown > 0) {
            return;
        }

        McaSpecialVillagerType type = McaSpecialVillagerClassifier.classify(villager);
        attackCooldown = McaSpecialVillagerCombatService.attackCooldownFor(type);
        boolean attacked = McaSpecialVillagerCombatService.performMeleeAttack(villager, target, type);
        if (attacked) {
            McaVampirismCompat.debugLog(
                    "[SPECIAL-AI] attack villager={} villagerType={} target={} damage={}",
                    villager.getUUID(),
                    type,
                    target.getUUID(),
                    McaSpecialVillagerCombatService.attackDamageFor(type)
            );
        }
    }

    private double attackReachSqr(LivingEntity target) {
        double reach = mob.getBbWidth() * 2.0D + target.getBbWidth();
        return reach * reach;
    }

    private boolean shouldRepathTo(LivingEntity target) {
        if (target == null) {
            return false;
        }
        if (lastNavigationTarget != target || mob.getNavigation().isDone()) {
            return true;
        }
        if (repathCooldown > 0) {
            return false;
        }
        return target.distanceToSqr(lastTargetX, lastTargetY, lastTargetZ) >= REPATH_TARGET_MOVED_DISTANCE_SQR;
    }

    private void markNavigationTarget(LivingEntity target) {
        lastNavigationTarget = target;
        lastTargetX = target.getX();
        lastTargetY = target.getY();
        lastTargetZ = target.getZ();
        repathCooldown = Math.max(1, McaVampirismCompatConfig.specialVillagerCombatRepathIntervalTicks());
    }

    private void resetNavigationTracking() {
        lastNavigationTarget = null;
        lastTargetX = 0.0D;
        lastTargetY = 0.0D;
        lastTargetZ = 0.0D;
    }
}
