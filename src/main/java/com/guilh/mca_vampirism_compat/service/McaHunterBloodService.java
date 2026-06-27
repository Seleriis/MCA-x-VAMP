package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.util.McaHunterTargetUtil;
import de.teamlapen.vampirism.core.ModEffects;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class McaHunterBloodService {
    private static final ResourceLocation VAMPIRISM_POISON_ID = new ResourceLocation("vampirism", "poison");
    private static final ResourceLocation VANILLA_POISON_ID = new ResourceLocation("minecraft", "poison");
    private static final long BLOCK_LOG_COOLDOWN_TICKS = 100L;
    private static final Map<UUID, Long> LAST_BLOCK_LOG_TICKS = new ConcurrentHashMap<>();

    private McaHunterBloodService() {
    }

    public static boolean isPoisonousBloodTarget(LivingEntity target) {
        return McaVampirismCompatConfig.hunterVillagersHavePoisonousBlood()
                && McaHunterAlignmentService.isMcaHunterVillager(target);
    }

    public static boolean isHunterVampirismImmune(LivingEntity target) {
        return McaVampirismCompatConfig.hunterVillagersImmuneToVampirism()
                && McaHunterAlignmentService.isMcaHunterVillager(target);
    }

    public static boolean shouldApplyToxicantToBiter(LivingEntity biter, LivingEntity target) {
        return biter != null
                && !biter.level().isClientSide
                && McaVampirismCompatConfig.hunterVillagersApplyToxicantToVampireBiters()
                && isPoisonousBloodTarget(target)
                && McaVampireFactionService.isVampire(biter);
    }

    public static boolean applyToxicantToBiter(LivingEntity biter, LivingEntity target) {
        if (!shouldApplyToxicantToBiter(biter, target)) {
            return false;
        }

        MobEffect effect = resolveToxicantEffect();
        if (effect == null) {
            return false;
        }

        int durationTicks = McaVampirismCompatConfig.hunterVillagerToxicantDurationTicks();
        int amplifier = McaVampirismCompatConfig.hunterVillagerToxicantAmplifier();
        MobEffectInstance existing = biter.getEffect(effect);
        if (McaVampirismCompatConfig.hunterVillagerAvoidDuplicateToxicant()
                && existing != null && existing.getDuration() > Math.max(20, durationTicks - 20)
                && existing.getAmplifier() >= amplifier) {
            return false;
        }

        ResourceLocation effectId = ForgeRegistries.MOB_EFFECTS.getKey(effect);
        McaVampirismCompat.debugLog(
                "[HUNTER-BLOOD] poisonous blood target={} reason=MCA_HUNTER_ALIGNED",
                target == null ? null : target.getUUID()
        );
        if (VANILLA_POISON_ID.equals(effectId)) {
            McaVampirismCompat.debugLog("[HUNTER-BLOOD] toxicant fallback used effect=minecraft:poison");
        }

        biter.addEffect(new MobEffectInstance(effect, durationTicks, amplifier), target);
        McaVampirismCompat.debugLog(
                "[HUNTER-BLOOD] toxicant applied by compat effect={} biter={} target={} duration={} amplifier={}",
                effectId,
                biter.getUUID(),
                target == null ? null : target.getUUID(),
                durationTicks,
                amplifier
        );
        return true;
    }

    public static boolean blockHunterVampireInfection(LivingEntity source, LivingEntity target) {
        if (!isHunterVampirismImmune(target)) {
            return false;
        }
        clearHunterInfectionState(target);
        logBlockedInfection(target);
        applyToxicantToBiter(source, target);
        if (target instanceof VillagerEntityMCA hunter && source != null) {
            McaHunterTargetUtil.markRetaliation(hunter, source);
        }
        return true;
    }

    public static void clearHunterInfectionState(LivingEntity target) {
        if (target == null || target.level().isClientSide || !isHunterVampirismImmune(target)) {
            return;
        }
        target.removeEffect(ModEffects.SANGUINARE.get());
        target.getCapability(ModCapabilities.VAMPIRIC_VILLAGER).ifPresent(state -> state.clearInfection());
    }

    private static MobEffect resolveToxicantEffect() {
        try {
            return ModEffects.POISON.get();
        } catch (RuntimeException | LinkageError ignored) {
            MobEffect vampirismPoison = ForgeRegistries.MOB_EFFECTS.getValue(VAMPIRISM_POISON_ID);
            if (vampirismPoison != null) {
                return vampirismPoison;
            }
            return McaVampirismCompatConfig.hunterVillagerFallbackToVanillaPoison() ? MobEffects.POISON : null;
        }
    }

    private static void logBlockedInfection(LivingEntity target) {
        if (target == null) {
            return;
        }
        long gameTime = target.level().getGameTime();
        Long lastLog = LAST_BLOCK_LOG_TICKS.get(target.getUUID());
        if (lastLog != null && gameTime - lastLog < BLOCK_LOG_COOLDOWN_TICKS) {
            return;
        }
        LAST_BLOCK_LOG_TICKS.put(target.getUUID(), gameTime);
        McaVampirismCompat.debugLog(
                "[HUNTER-BLOOD] vampire infection blocked target={} reason=MCA_HUNTER_IMMUNE",
                target.getUUID()
        );
        McaVampirismCompat.debugLog(
                "[HUNTER-BLOOD] vampire infection blocked target={} reason=POISONOUS_HUNTER_BLOOD",
                target.getUUID()
        );
    }
}
