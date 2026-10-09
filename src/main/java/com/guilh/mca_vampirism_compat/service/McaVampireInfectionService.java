package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.advancement.McaAdvancementService;
import com.guilh.mca_vampirism_compat.advancement.McaCompatAdvancements;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.faction.PlayerFaction;
import com.guilh.mca_vampirism_compat.util.McaVampireTargetUtil;
import de.teamlapen.vampirism.core.ModEffects;
import forge.net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public final class McaVampireInfectionService {
    private McaVampireInfectionService() {
    }

    public static boolean tryApplyConfiguredSanguinare(LivingEntity source, LivingEntity target, InfectionSourceType sourceType) {
        return tryApplyConfiguredSanguinare(source, target, sourceType, false);
    }

    public static boolean tryApplyConfiguredSanguinare(LivingEntity source, LivingEntity target,
                                                       InfectionSourceType sourceType, boolean forceConversionChance) {
        if (!canApplyConfiguredSanguinare(source, target)) {
            return false;
        }
        int durationTicks = getConfiguredSanguinareDuration(sourceType);
        if (forceConversionChance) {
            if (sourceType == InfectionSourceType.PLAYER) {
                McaVampirismCompat.debugLog(
                        "[BITE] infectious skill detected source=PLAYER chance=1.0 duration={}",
                        durationTicks
                );
            } else {
                McaVampirismCompat.debugLog(
                        "[BITE] forced sanguinare source={} chance=1.0 duration={}",
                        sourceType.logName(),
                        durationTicks
                );
            }
        } else if (!rollConversionChance(source, target, sourceType)) {
            return false;
        }

        McaVampirismCompat.debugLog(
                "[BITE] applying configured sanguinare source={} duration={}",
                sourceType.logName(),
                durationTicks
        );
        target.addEffect(new MobEffectInstance(com.guilh.mca_vampirism_compat.util.VampirismEffectCompat.sanguinare(), durationTicks), source);
        UUID sourceUuid = source == null ? null : source.getUUID();
        if (target instanceof VillagerEntityMCA villager) {
            McaVampireStateService.markInfected(villager, sourceUuid, target.level().getGameTime());
            if (source instanceof Player player && sourceType == InfectionSourceType.PLAYER) {
                McaVampireBiteService.recordPlayerConversionBite(player, villager);
                if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                    McaAdvancementService.award(serverPlayer, McaCompatAdvancements.VAMPIRE_INFECTION);
                }
            }
        }
        McaVillageVampireThreatService.recordVampireThreat(source, target, sourceType.logName());
        McaVampirismCompat.debugLog(
                "[BITE] Sanguinare applied target={} source={} sourceType={} durationTicks={}",
                target.getUUID(),
                sourceUuid,
                sourceType.logName(),
                durationTicks
        );
        return true;
    }

    public static boolean canApplyConfiguredSanguinare(LivingEntity source, LivingEntity target) {
        if (target == null || target.level().isClientSide) {
            return false;
        }
        if (McaHunterBloodService.blockHunterVampireInfection(source, target)) {
            return false;
        }
        if (target instanceof VillagerEntityMCA villager) {
            if (McaVampireCureService.isCuring(villager)) {
                logTransformationBlocked(target, "CURING");
                return false;
            }
            if (McaWerewolfStateService.isWerewolf(villager)
                    || McaWerewolfStateService.isWerewolfInfected(villager)
                    || McaWerewolfConversionService.isLupusSanguinemActive(villager)) {
                logTransformationBlocked(target, "WEREWOLF_OR_LUPUS_CONFLICT");
                return false;
            }
        }
        if (McaVampirismCompatConfig.werewolfVillagersImmuneToVampirism()
                && McaWerewolfStateService.isWerewolf(target)) {
            McaVampirismCompat.debugLog(
                    "[WEREWOLF] vampire infection blocked target={} reason=WEREWOLF_IMMUNE",
                    target.getUUID()
            );
            return false;
        }
        if (!McaVampireBiteService.canMcaVillagerReceiveInfection(target)) {
            return false;
        }
        if (McaVampireTargetUtil.isVampireTarget(target) || target.hasEffect(com.guilh.mca_vampirism_compat.util.VampirismEffectCompat.sanguinare())) {
            return false;
        }
        return ModCapabilities.get(target)
                .map(state -> !state.isInfected() && !state.isConverted())
                .orElse(true);
    }

    private static void logTransformationBlocked(LivingEntity target, String reason) {
        McaVampirismCompat.debugLog(
                "[TRANSFORMATION] blocked target={} type=VAMPIRE reason={}",
                target == null ? null : target.getUUID(),
                reason
        );
    }

    public static int getConfiguredSanguinareDuration(InfectionSourceType sourceType) {
        if (sourceType == InfectionSourceType.PLAYER) {
            return McaVampirismCompatConfig.playerSanguinareDurationTicks();
        }
        return McaVampirismCompatConfig.vampireVillagerSanguinareDurationTicks();
    }

    public static boolean rollConversionChance(LivingEntity source, LivingEntity target, InfectionSourceType sourceType) {
        double chance = McaVampirismCompatConfig.conversionChanceOnBite();
        boolean success = chance >= 1.0D || (chance > 0.0D && target.getRandom().nextDouble() < chance);
        McaVampirismCompat.debugLog(
                "[BITE] conversion roll source={} chance={} result={}",
                sourceType.logName(),
                chance,
                success ? "SUCCESS" : "FAILED"
        );
        return success;
    }

    public static InfectionSourceType sourceTypeFor(LivingEntity source) {
        if (source instanceof Player player && McaVampireFactionService.classifyPlayer(player) == PlayerFaction.VAMPIRE) {
            return InfectionSourceType.PLAYER;
        }
        if (McaVampireStateService.isVampire(source)) {
            return InfectionSourceType.MCA_VAMPIRE;
        }
        return InfectionSourceType.OTHER_VAMPIRIC;
    }

    public enum InfectionSourceType {
        PLAYER("PLAYER"),
        MCA_VAMPIRE("MCA_VAMPIRE"),
        OTHER_VAMPIRIC("OTHER_VAMPIRIC"),
        VILLAGE_CAPTURE("VILLAGE_CAPTURE");

        private final String logName;

        InfectionSourceType(String logName) {
            this.logName = logName;
        }

        public String logName() {
            return logName;
        }
    }
}
