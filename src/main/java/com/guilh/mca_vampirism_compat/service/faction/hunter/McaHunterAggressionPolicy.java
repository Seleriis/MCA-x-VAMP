package com.guilh.mca_vampirism_compat.service.faction.hunter;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.service.McaSpecialVillagerClassifier;
import com.guilh.mca_vampirism_compat.service.McaVampireStateService;
import com.guilh.mca_vampirism_compat.service.McaVillageFactionControlService;
import com.guilh.mca_vampirism_compat.service.McaWerewolfStateService;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.relationship.AgeState;

import java.util.HashMap;
import java.util.Map;

public final class McaHunterAggressionPolicy {
    private static final Map<String, Long> LAST_LOG_TICKS = new HashMap<>();

    private McaHunterAggressionPolicy() {
    }

    public static boolean canBecomeHunterAligned(VillagerEntityMCA villager, HunterSource source) {
        String denyReason = denyHunterAlignmentReason(villager, source);
        logDecision("hunterAligned", villager, source, denyReason);
        return denyReason == null;
    }

    public static boolean canEquipHunterWeapon(VillagerEntityMCA villager, HunterSource source) {
        String denyReason = denyHunterAlignmentReason(villager, source);
        if (denyReason == null && source == HunterSource.DEFENSIVE_MODE
                && !McaVampirismCompatConfig.defensiveModeUsesPitchfork()) {
            denyReason = "DEFENSIVE_WEAPON_DISABLED";
        }
        logDecision("hunterWeapon", villager, source, denyReason);
        return denyReason == null;
    }

    public static boolean canEnterDefensiveAggressiveMode(VillagerEntityMCA villager, DefensiveReason reason) {
        String denyReason = denyDefensiveReason(villager, reason);
        logDecision("defensiveMode", villager, HunterSource.DEFENSIVE_MODE, denyReason);
        return denyReason == null;
    }

    public static boolean canApplyMakeAggressiveAsHunter(VillagerEntityMCA villager, CaptureContext context) {
        String denyReason = context == CaptureContext.HUNTER_VILLAGE
                ? denyHunterAlignmentReason(villager, HunterSource.MAKE_AGGRESSIVE)
                : "NOT_HUNTER_VILLAGE";
        logDecision("makeAggressive", villager, HunterSource.MAKE_AGGRESSIVE, denyReason);
        return denyReason == null;
    }

    public static boolean isHunterControlledVillageContext(VillagerEntityMCA villager) {
        return McaVillageFactionControlService.shouldVillagerBeHunterByVillageControl(villager);
    }

    public static boolean isManualHunterAllowed(VillagerEntityMCA villager) {
        return villager != null
                && isEligibleHumanAdult(villager)
                && McaVampirismCompatConfig.hunterVillagerKeepManualHunterTrait();
    }

    public static String denyReason(VillagerEntityMCA villager, HunterSource source) {
        return denyHunterAlignmentReason(villager, source);
    }

    private static String denyHunterAlignmentReason(VillagerEntityMCA villager, HunterSource source) {
        if (!isEligibleHumanAdult(villager)) {
            return "NOT_ELIGIBLE_HUMAN_ADULT";
        }
        return switch (source) {
            case HUNTER_VILLAGE -> isHunterControlledVillageContext(villager) ? null : "NOT_HUNTER_VILLAGE";
            case HUNTER_CAPTURE_FINISHED -> null;
            case MANUAL_TRAIT -> McaVampirismCompatConfig.hunterVillagerKeepManualHunterTrait()
                    ? null
                    : "MANUAL_HUNTER_DISABLED";
            case MAKE_AGGRESSIVE -> isHunterControlledVillageContext(villager) ? null : "NOT_HUNTER_VILLAGE";
            case DEBUG_COMMAND -> McaVampirismCompatConfig.enableDebugLogs() ? null : "DEBUG_DISABLED";
            case VAMPIRE_THREAT, DEFENSIVE_MODE, UNKNOWN -> "SOURCE_NOT_ALLOWED";
        };
    }

    private static String denyDefensiveReason(VillagerEntityMCA villager, DefensiveReason reason) {
        if (!McaVampirismCompatConfig.enableMcaVillagerDefenseAgainstVampires()) {
            return "LEGACY_DEFENSE_DISABLED";
        }
        if (!McaVampirismCompatConfig.enableTemporaryDefensiveMode()) {
            return "TEMPORARY_DEFENSE_DISABLED";
        }
        if (!isEligibleHumanAdult(villager)) {
            return "NOT_ELIGIBLE_HUMAN_ADULT";
        }
        if (McaVampirismCompatConfig.defensiveModeOnlyInHunterVillages()
                && !isHunterControlledVillageContext(villager)) {
            return "NOT_HUNTER_VILLAGE";
        }
        if (reason == DefensiveReason.VAMPIRE_THREAT || reason == DefensiveReason.MAKE_AGGRESSIVE) {
            return McaVampirismCompatConfig.defensiveModeOnlyInHunterVillages()
                    && !isHunterControlledVillageContext(villager)
                    ? "NOT_HUNTER_VILLAGE"
                    : null;
        }
        return null;
    }

    private static boolean isEligibleHumanAdult(VillagerEntityMCA villager) {
        if (villager == null
                || villager.level().isClientSide
                || !villager.isAlive()
                || villager.getAgeState() != AgeState.ADULT
                || McaVampireStateService.isVampire(villager)
                || McaWerewolfStateService.isWerewolf(villager)
                || McaSpecialVillagerClassifier.isCuring(villager)) {
            return false;
        }
        return true;
    }

    private static void logDecision(String action, VillagerEntityMCA villager, HunterSource source, String denyReason) {
        if (villager != null) {
            long gameTime = villager.level().getGameTime();
            String key = action + "|" + source + "|" + villager.getUUID() + "|" + denyReason;
            Long last = LAST_LOG_TICKS.get(key);
            if (last != null && gameTime - last < 200L) {
                return;
            }
            if (LAST_LOG_TICKS.size() > 512) {
                LAST_LOG_TICKS.clear();
            }
            LAST_LOG_TICKS.put(key, gameTime);
        }
        McaVampirismCompat.debugLog(
                "[HUNTER-POLICY] {} source={} villager={} allowed={} reason={}",
                action,
                source,
                villager == null ? null : villager.getUUID(),
                denyReason == null,
                denyReason == null ? "OK" : denyReason
        );
    }

    public enum HunterSource {
        HUNTER_VILLAGE,
        HUNTER_CAPTURE_FINISHED,
        MANUAL_TRAIT,
        DEBUG_COMMAND,
        VAMPIRE_THREAT,
        MAKE_AGGRESSIVE,
        DEFENSIVE_MODE,
        UNKNOWN
    }

    public enum DefensiveReason {
        VAMPIRE_THREAT,
        MAKE_AGGRESSIVE,
        TEMPORARY_DEFENSE,
        UNKNOWN
    }

    public enum CaptureContext {
        HUNTER_VILLAGE,
        HUNTER_CAPTURE_FINISHED,
        NON_HUNTER
    }
}
