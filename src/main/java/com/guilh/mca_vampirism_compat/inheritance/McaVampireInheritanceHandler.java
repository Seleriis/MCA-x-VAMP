package com.guilh.mca_vampirism_compat.inheritance;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.service.McaVampireStateService;
import com.guilh.mca_vampirism_compat.service.McaWerewolfStateService;
import com.guilh.mca_vampirism_compat.trait.McaTraitBridge;
import com.guilh.mca_vampirism_compat.util.McaVillagerUtil;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;

public final class McaVampireInheritanceHandler {
    private McaVampireInheritanceHandler() {
    }

    public static void applyInheritance(AgeableMob child, Entity parentA, Entity parentB) {
        if (!(child instanceof VillagerEntityMCA villager) || !McaVillagerUtil.isMcaVillager(child)) {
            return;
        }
        ModCapabilities.get(child).ifPresent(state -> {
            if (state.isFactionInheritanceProcessed()) {
                return;
            }
            InheritedFaction decision = decide(parentA, parentB, child.getRandom());
            applyDecision(villager, decision);
        });
    }

    public static void applyDecision(VillagerEntityMCA child, InheritedFaction decision) {
        if (child == null || decision == null) {
            return;
        }
        ModCapabilities.get(child).ifPresent(state -> {
            if (state.isFactionInheritanceProcessed()) {
                return;
            }
            boolean applied = switch (decision) {
                case VAMPIRE -> {
                    state.markInheritedVampire();
                    McaTraitBridge.ensureVampireState(child);
                    yield state.isConverted();
                }
                case WEREWOLF -> {
                    state.markInheritedWerewolf(child.level().getGameTime());
                    McaWerewolfStateService.ensureWerewolfState(child);
                    yield state.isWerewolfConverted()
                            && state.isWerewolfAppearanceApplied()
                            && state.isWerewolfPartialForm();
                }
                case HUMAN -> {
                    McaTraitBridge.removeVampireTrait(child);
                    McaTraitBridge.removeWerewolfTrait(child);
                    yield true;
                }
            };
            if (!applied) {
                McaVampirismCompat.debugLog(
                        "[INHERITANCE] application deferred child={} decision={} vampire={} werewolf={}",
                        child.getUUID(),
                        decision,
                        state.isConverted(),
                        state.isWerewolfConverted()
                );
                return;
            }
            state.markFactionInheritanceProcessed();
            McaVampirismCompat.debugLog(
                    "[INHERITANCE] applied child={} decision={} vampire={} werewolf={}",
                    child.getUUID(),
                    decision,
                    state.isConverted(),
                    state.isWerewolfConverted()
            );
        });
    }

    public static InheritedFaction decide(Entity parentA, Entity parentB, RandomSource random) {
        int vampireParents = countVampireParents(parentA, parentB);
        int werewolfParents = countWerewolfParents(parentA, parentB);
        return decide(
                vampireParents,
                werewolfParents,
                random.nextDouble(),
                random.nextDouble(),
                random.nextBoolean()
        );
    }

    static InheritedFaction decide(int vampireParents, int werewolfParents,
                                   double vampireRoll, double werewolfRoll, boolean vampireWinsTie) {
        return decide(
                vampireParents,
                werewolfParents,
                vampireRoll,
                werewolfRoll,
                vampireWinsTie,
                McaVampirismCompatConfig.vampireTraitControlsInheritance()
                        ? McaVampirismCompatConfig.oneVampireParentTraitInheritanceChance()
                        : McaVampirismCompatConfig.oneVampireParentInheritanceChance(),
                McaVampirismCompatConfig.vampireTraitControlsInheritance()
                        ? McaVampirismCompatConfig.twoVampireParentsTraitInheritanceChance()
                        : McaVampirismCompatConfig.twoVampireParentsInheritanceChance(),
                McaVampirismCompatConfig.oneWerewolfParentInheritanceChance(),
                McaVampirismCompatConfig.twoWerewolfParentsInheritanceChance()
        );
    }

    static InheritedFaction decide(int vampireParents, int werewolfParents,
                                   double vampireRoll, double werewolfRoll, boolean vampireWinsTie,
                                   double oneVampireChance, double twoVampireChance,
                                   double oneWerewolfChance, double twoWerewolfChance) {
        double vampireChance = vampireParents >= 2 ? twoVampireChance
                : vampireParents == 1 ? oneVampireChance : 0.0D;
        double werewolfChance = werewolfParents >= 2 ? twoWerewolfChance
                : werewolfParents == 1 ? oneWerewolfChance : 0.0D;
        boolean inheritVampire = vampireRoll < vampireChance;
        boolean inheritWerewolf = werewolfRoll < werewolfChance;
        if (inheritVampire && inheritWerewolf) {
            return vampireWinsTie ? InheritedFaction.VAMPIRE : InheritedFaction.WEREWOLF;
        }
        if (inheritVampire) {
            return InheritedFaction.VAMPIRE;
        }
        return inheritWerewolf ? InheritedFaction.WEREWOLF : InheritedFaction.HUMAN;
    }

    public static int countVampireParents(Entity parentA, Entity parentB) {
        return (isInheritanceVampire(parentA) ? 1 : 0) + (isInheritanceVampire(parentB) ? 1 : 0);
    }

    public static int countWerewolfParents(Entity parentA, Entity parentB) {
        return (isInheritanceWerewolf(parentA) ? 1 : 0) + (isInheritanceWerewolf(parentB) ? 1 : 0);
    }

    public static double getInheritanceChance(Entity parentA, Entity parentB) {
        return vampireChance(countVampireParents(parentA, parentB));
    }

    private static double vampireChance(int parentCount) {
        if (parentCount >= 2) {
            return McaVampirismCompatConfig.vampireTraitControlsInheritance()
                    ? McaVampirismCompatConfig.twoVampireParentsTraitInheritanceChance()
                    : McaVampirismCompatConfig.twoVampireParentsInheritanceChance();
        }
        if (parentCount == 1) {
            return McaVampirismCompatConfig.vampireTraitControlsInheritance()
                    ? McaVampirismCompatConfig.oneVampireParentTraitInheritanceChance()
                    : McaVampirismCompatConfig.oneVampireParentInheritanceChance();
        }
        return 0.0D;
    }

    private static double werewolfChance(int parentCount) {
        if (parentCount >= 2) {
            return McaVampirismCompatConfig.twoWerewolfParentsInheritanceChance();
        }
        return parentCount == 1 ? McaVampirismCompatConfig.oneWerewolfParentInheritanceChance() : 0.0D;
    }

    private static boolean isInheritanceVampire(Entity entity) {
        if (McaVampirismCompatConfig.vampireTraitControlsInheritance()) {
            return McaTraitBridge.isVampireByTraitOrCapability(entity);
        }
        return entity != null && ModCapabilities.get(entity)
                .map(state -> state.isConverted())
                .orElse(false);
    }

    private static boolean isInheritanceWerewolf(Entity entity) {
        return McaWerewolfStateService.isWerewolf(entity);
    }

    public enum InheritedFaction {
        HUMAN,
        VAMPIRE,
        WEREWOLF
    }
}
