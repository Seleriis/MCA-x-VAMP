package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.advancement.McaAdvancementService;
import com.guilh.mca_vampirism_compat.advancement.McaCompatAdvancements;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.ai.relationship.AgeState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.UUID;

public final class McaWerewolfInfectionService {
    private static final ResourceLocation LUPUS_SANGUINEM_ID =
            ResourceLocation.fromNamespaceAndPath("werewolves", "lupus_sanguinem");
    private static boolean lupusSanguinemResolved;
    private static Holder<MobEffect> lupusSanguinemEffect;

    private McaWerewolfInfectionService() {
    }

    public static void handleWerewolfAttack(LivingEntity source, LivingEntity target) {
        if (!McaWerewolvesCompat.isAvailable()
                || !(target instanceof VillagerEntityMCA villager)
                || source == null
                || target.level().isClientSide
                || !McaVampireFactionService.isWerewolf(source)
                || !McaVampireFactionService.isTransformedWerewolf(source)) {
            return;
        }
        tryApplyWerewolfInfection(source, villager, false);
    }

    public static boolean tryApplyWerewolfInfection(LivingEntity source, VillagerEntityMCA target, boolean forceChance) {
        if (!canApplyWerewolfInfection(source, target)) {
            return false;
        }
        if (!forceChance) {
            double chance = McaVampirismCompatConfig.werewolfConversionChanceOnBite();
            double roll = target.getRandom().nextDouble();
            McaVampirismCompat.debugLog(
                    "[WEREWOLF-CONVERSION] infection roll target={} source={} roll={} chance={}",
                    target.getUUID(),
                    source == null ? null : source.getUUID(),
                    roll,
                    chance
            );
            if (roll > chance) {
                return false;
            }
        }

        int duration = McaVampirismCompatConfig.werewolfVillagerInfectionDurationTicks();
        Holder<MobEffect> lupusSanguinem = lupusSanguinemEffect();
        if (lupusSanguinem != null) {
            target.addEffect(new MobEffectInstance(lupusSanguinem, duration), source);
        }
        UUID sourceUuid = source == null ? null : source.getUUID();
        ModCapabilities.get(target)
                .ifPresent(state -> state.startWerewolfInfection(target.level().getGameTime(), duration, sourceUuid));
        if (source instanceof net.minecraft.server.level.ServerPlayer player) {
            McaAdvancementService.award(player, McaCompatAdvancements.WEREWOLF_INFECTION);
        }
        McaVampirismCompat.debugLog(
                "[WEREWOLF-CONVERSION] infection applied target={} source={} duration={} effectFound={}",
                target.getUUID(),
                sourceUuid,
                duration,
                lupusSanguinem != null
        );
        return true;
    }

    public static boolean canApplyWerewolfInfection(LivingEntity source, VillagerEntityMCA target) {
        if (!McaWerewolvesCompat.isAvailable()
                || target == null
                || target.level().isClientSide
                || !McaVampirismCompatConfig.allowMcaVillagerWerewolfInfection()) {
            return false;
        }
        if (!McaVampirismCompatConfig.allowMcaChildWerewolfInfection()
                && target.getAgeState() != AgeState.ADULT) {
            logBlocked(target, "CHILD_DISABLED");
            return false;
        }
        if (McaVampireCureService.isCuring(target)) {
            logBlocked(target, "CURING");
            return false;
        }
        if (target.hasEffect(com.guilh.mca_vampirism_compat.util.VampirismEffectCompat.sanguinare())
                || ModCapabilities.get(target)
                .map(state -> state.isInfected() || state.isConverted())
                .orElse(false)) {
            logBlocked(target, "VAMPIRE_OR_SANGUINARE_CONFLICT");
            return false;
        }
        if (McaVampireStateService.isVampire(target)
                && McaVampirismCompatConfig.vampireVillagersImmuneToWerewolfTransformation()) {
            logBlocked(target, "VAMPIRE_IMMUNE");
            return false;
        }
        if (McaHunterAlignmentService.isMcaHunterVillager(target)
                && McaVampirismCompatConfig.hunterVillagersImmuneToWerewolfTransformation()) {
            logBlocked(target, "HUNTER_IMMUNE");
            return false;
        }
        if (McaWerewolfStateService.isWerewolf(target)) {
            logBlocked(target, "ALREADY_WEREWOLF");
            return false;
        }
        Holder<MobEffect> lupusSanguinem = lupusSanguinemEffect();
        if (lupusSanguinem != null && target.hasEffect(lupusSanguinem)) {
            return false;
        }
        return ModCapabilities.get(target)
                .map(state -> !state.isWerewolfInfected())
                .orElse(true);
    }

    public static void processWerewolfTick(VillagerEntityMCA villager) {
        if (!McaWerewolvesCompat.isAvailable() || villager == null || villager.level().isClientSide) {
            return;
        }
        ModCapabilities.get(villager).ifPresent(state -> {
            Holder<MobEffect> lupusSanguinem = lupusSanguinemEffect();
            MobEffectInstance effect = lupusSanguinem == null ? null : villager.getEffect(lupusSanguinem);
            if (effect != null && !state.isWerewolfInfected() && !McaWerewolfStateService.isWerewolf(villager)) {
                state.startWerewolfInfection(
                        villager.level().getGameTime(),
                        McaVampirismCompatConfig.werewolfVillagerInfectionDurationTicks(),
                        null
                );
                McaVampirismCompat.debugLog(
                        "[WEREWOLF-CONVERSION] external lupus sanguinem tracked target={} duration={}",
                        villager.getUUID(),
                        McaVampirismCompatConfig.werewolfVillagerInfectionDurationTicks()
                );
            }
            if (!state.isWerewolfInfected()) {
                return;
            }
            if (!canCompleteWerewolfConversion(villager)) {
                state.clearWerewolfInfection();
                if (lupusSanguinem != null) {
                    villager.removeEffect(lupusSanguinem);
                }
                return;
            }
            long gameTime = villager.level().getGameTime();
            if (gameTime < state.getWerewolfInfectionFinishTick()) {
                return;
            }
            if (lupusSanguinem != null) {
                villager.removeEffect(lupusSanguinem);
            }
            McaWerewolfStateService.markConverted(villager);
        });
    }

    public static boolean isLupusSanguinemActive(VillagerEntityMCA villager) {
        if (!McaWerewolvesCompat.isAvailable() || villager == null) {
            return false;
        }
        Holder<MobEffect> lupusSanguinem = lupusSanguinemEffect();
        return lupusSanguinem != null && villager.hasEffect(lupusSanguinem);
    }

    public static boolean removeLupusSanguinem(VillagerEntityMCA villager) {
        if (!McaWerewolvesCompat.isAvailable() || villager == null) {
            return false;
        }
        Holder<MobEffect> lupusSanguinem = lupusSanguinemEffect();
        return lupusSanguinem != null && villager.removeEffect(lupusSanguinem);
    }

    private static boolean canCompleteWerewolfConversion(VillagerEntityMCA villager) {
        return !McaVampireStateService.isVampire(villager)
                && !McaHunterAlignmentService.isMcaHunterVillager(villager)
                && !McaVampireCureService.isCuring(villager)
                && (McaVampirismCompatConfig.allowMcaChildWerewolfInfection()
                || villager.getAgeState() == AgeState.ADULT);
    }

    private static Holder<MobEffect> lupusSanguinemEffect() {
        if (!McaWerewolvesCompat.isAvailable()) {
            return null;
        }
        if (!lupusSanguinemResolved) {
            lupusSanguinemResolved = true;
            MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(LUPUS_SANGUINEM_ID);
            lupusSanguinemEffect = effect == null ? null : BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect);
        }
        return lupusSanguinemEffect;
    }

    private static void logBlocked(VillagerEntityMCA target, String reason) {
        McaVampirismCompat.debugLog(
                "[WEREWOLF-CONVERSION] blocked target={} reason={}",
                target.getUUID(),
                reason
        );
        McaVampirismCompat.debugLog(
                "[TRANSFORMATION] blocked target={} type=WEREWOLF reason={}",
                target.getUUID(),
                reason
        );
    }
}
