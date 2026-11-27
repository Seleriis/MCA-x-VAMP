package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import de.teamlapen.vampirism.core.ModEffects;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.ai.relationship.AgeState;

public final class McaSpecialVillagerClassifier {
    private McaSpecialVillagerClassifier() {
    }

    public static McaSpecialVillagerType classify(VillagerEntityMCA villager) {
        if (villager == null) {
            return McaSpecialVillagerType.UNKNOWN;
        }
        McaSpecialVillagerType cureType = classifyCure(villager);
        if (cureType == McaSpecialVillagerType.CURING_VAMPIRE
                || cureType == McaSpecialVillagerType.CURING_WEREWOLF) {
            return cureType;
        }
        if (McaVampireStateService.isVampire(villager)) {
            return McaSpecialVillagerType.VAMPIRE;
        }
        if (McaWerewolfStateService.isWerewolf(villager)) {
            return McaSpecialVillagerType.WEREWOLF;
        }
        // Vampire and Werewolf are already ruled out above, so skip the public
        // wrappers that would repeat both lookups. classify runs several times
        // per special villager per tick.
        if (McaHunterAlignmentService.hasHunterSource(villager)) {
            return McaSpecialVillagerType.HUNTER;
        }
        if (McaDefensiveVillagerService.hasActiveDefensiveMode(villager)) {
            return McaSpecialVillagerType.DEFENSIVE;
        }
        if (villager.getAgeState() != AgeState.ADULT) {
            return McaSpecialVillagerType.CHILD;
        }
        if (isInfected(villager)) {
            return McaSpecialVillagerType.HUMAN_INFECTED;
        }
        return McaSpecialVillagerType.HUMAN_NORMAL;
    }

    public static McaSpecialVillagerType classifyCure(VillagerEntityMCA villager) {
        return villager == null ? McaSpecialVillagerType.UNKNOWN : ModCapabilities.get(villager)
                .map(state -> {
                    boolean vampireCure = state.isCuringVampire()
                            || state.getCureStartedTick() >= 0L
                            || state.getCureFinishTick() >= 0L;
                    boolean werewolfCure = state.isCuringWerewolf()
                            || state.getWerewolfCureStartedTick() >= 0L
                            || state.getWerewolfCureFinishTick() >= 0L;
                    if (vampireCure) {
                        return McaSpecialVillagerType.CURING_VAMPIRE;
                    }
                    if (werewolfCure) {
                        return McaSpecialVillagerType.CURING_WEREWOLF;
                    }
                    return McaSpecialVillagerType.UNKNOWN;
                })
                .orElse(McaSpecialVillagerType.UNKNOWN);
    }

    public static boolean isCuring(VillagerEntityMCA villager) {
        McaSpecialVillagerType type = classifyCure(villager);
        return type == McaSpecialVillagerType.CURING_VAMPIRE
                || type == McaSpecialVillagerType.CURING_WEREWOLF;
    }

    public static boolean isSpecialNonHuman(VillagerEntityMCA villager) {
        return switch (classify(villager)) {
            case VAMPIRE, HUNTER, WEREWOLF, DEFENSIVE, CURING_VAMPIRE, CURING_WEREWOLF -> true;
            case HUMAN_INFECTED -> McaVampirismCompatConfig.infectedMcaVillagersCountAsSpecialForMonsterIgnore();
            case HUMAN_NORMAL, CHILD, UNKNOWN -> false;
        };
    }

    public static boolean isCombatCapableSpecial(VillagerEntityMCA villager) {
        return villager != null
                && villager.getAgeState() == AgeState.ADULT
                && switch (classify(villager)) {
                    case VAMPIRE, HUNTER, WEREWOLF, DEFENSIVE -> true;
                    case HUMAN_NORMAL, HUMAN_INFECTED, CURING_VAMPIRE, CURING_WEREWOLF, CHILD, UNKNOWN -> false;
                };
    }

    public static boolean shouldSuppressMonsterPanic(VillagerEntityMCA villager) {
        return McaVampirismCompatConfig.suppressSpecialMcaVillagerMonsterPanic()
                && isSpecialNonHuman(villager);
    }

    private static boolean isInfected(VillagerEntityMCA villager) {
        return villager.hasEffect(com.guilh.mca_vampirism_compat.util.VampirismEffectCompat.sanguinare())
                || McaWerewolfConversionService.isLupusSanguinemActive(villager)
                || McaWerewolfStateService.isWerewolfInfected(villager)
                || ModCapabilities.get(villager)
                .map(state -> state.isInfected())
                .orElse(false);
    }
}
