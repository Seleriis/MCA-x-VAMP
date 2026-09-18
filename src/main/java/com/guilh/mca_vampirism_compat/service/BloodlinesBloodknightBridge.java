package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import de.teamlapen.vampirism.api.VampirismAPI;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Optional Bloodlines integration kept reflective so the compat has no hard
 * runtime dependency on Bloodlines. The reflected BloodlineManager contract
 * is shared by the supported 1.21-2.0.7 and 1.21-3.0.9 releases.
 */
public final class BloodlinesBloodknightBridge {
    private static final String BLOODLINES_MOD_ID = "bloodlines";
    private static final ResourceLocation BLOODKNIGHT_ID = ResourceLocation.fromNamespaceAndPath(
            BLOODLINES_MOD_ID, "bloodknight"
    );
    private static final String MANAGER_CLASS =
            "com.thedrofdoctoring.bloodlines.capabilities.bloodlines.BloodlineManager";
    private static final String SKILLS_CLASS =
            "com.thedrofdoctoring.bloodlines.skills.BloodlineSkills";
    private static final String VAMPIRE_PLAYER_CLASS =
            "de.teamlapen.vampirism.entity.player.vampire.VampirePlayer";
    private static final String VAMPIRISM_SKILL_CLASS =
            "de.teamlapen.vampirism.api.entity.player.skills.ISkill";
    private static final String COMMON_CONFIG_CLASS =
            "com.thedrofdoctoring.bloodlines.config.CommonConfig";

    private static volatile Handles resolvedHandles;
    private static volatile boolean resolutionFailureLogged;

    private BloodlinesBloodknightBridge() {
    }

    /**
     * Returns true only when the attacking player is a ranked Bloodknight and
     * the target is within Bloodlines' current health threshold.
     */
    public static boolean canFeedVampire(LivingEntity source, LivingEntity target) {
        if (!(source instanceof Player player) || target == null || !isBloodlinesLoaded()) {
            return false;
        }
        try {
            Handles handles = handles();
            Object manager = handles.getManager.invoke(null, player);
            Object bloodlineId = handles.getBloodlineId.invoke(manager);
            int rank = ((Number) handles.getRank.invoke(manager)).intValue();
            boolean feignedMercy = hasFeignedMercy(handles, player);
            double normalLimit = readDouble(handles, handles.normalHealthLimit.get(null));
            double mercyLimit = readDouble(handles, handles.feignedMercyHealthLimit.get(null));
            return isEligibleBloodknightState(
                    bloodlineId == null ? null : bloodlineId.toString(),
                    rank,
                    feignedMercy,
                    target.getHealth(),
                    target.getMaxHealth(),
                    normalLimit,
                    mercyLimit
            );
        } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            logResolutionFailure(exception);
            return false;
        }
    }

    static boolean isEligibleBloodknightState(
            String bloodlineId,
            int rank,
            boolean feignedMercy,
            double currentHealth,
            double maxHealth,
            double normalHealthLimit,
            double feignedMercyHealthLimit
    ) {
        if (!BLOODKNIGHT_ID.toString().equals(bloodlineId) || rank <= 0 || maxHealth <= 0.0D) {
            return false;
        }
        double configuredLimit = feignedMercy ? feignedMercyHealthLimit : normalHealthLimit;
        if (!Double.isFinite(configuredLimit) || !Double.isFinite(currentHealth)) {
            return false;
        }
        double limit = Math.clamp(configuredLimit, 0.0D, 1.0D);
        return currentHealth / maxHealth <= limit;
    }

    private static boolean hasFeignedMercy(Handles handles, Player player) throws ReflectiveOperationException {
        Object vampirePlayer = VampirismAPI.getVampirePlayer(player).orElse(null);
        if (vampirePlayer == null) {
            return false;
        }
        Object skillHandler = handles.getVampireSkillHandler.invoke(vampirePlayer);
        Object deferredSkill = handles.feignedMercySkill.get(null);
        Object skill = handles.deferredGet.invoke(deferredSkill);
        return Boolean.TRUE.equals(handles.isSkillEnabled.invoke(skillHandler, skill));
    }

    private static double readDouble(Handles handles, Object configValue) throws ReflectiveOperationException {
        Object value = handles.configGet.invoke(configValue);
        return value instanceof Number number ? number.doubleValue() : Double.NaN;
    }

    private static Handles handles() throws ReflectiveOperationException {
        Handles cached = resolvedHandles;
        if (cached != null) {
            return cached;
        }
        synchronized (BloodlinesBloodknightBridge.class) {
            if (resolvedHandles == null) {
                ClassLoader loader = BloodlinesBloodknightBridge.class.getClassLoader();
                Class<?> managerClass = Class.forName(MANAGER_CLASS, true, loader);
                Class<?> skillsClass = Class.forName(SKILLS_CLASS, true, loader);
                Class<?> vampirePlayerClass = Class.forName(VAMPIRE_PLAYER_CLASS, true, loader);
                Class<?> vampirismSkillClass = Class.forName(VAMPIRISM_SKILL_CLASS, true, loader);
                Class<?> configClass = Class.forName(COMMON_CONFIG_CLASS, true, loader);
                Method getManager = managerClass.getMethod("get", Player.class);
                Method getBloodlineId = managerClass.getMethod("getBloodlineId");
                Method getRank = managerClass.getMethod("getRank");
                Method getVampireSkillHandler = vampirePlayerClass.getMethod("getSkillHandler");
                Method isSkillEnabled = getVampireSkillHandler.getReturnType()
                        .getMethod("isSkillEnabled", vampirismSkillClass);
                Field feignedMercySkill = skillsClass.getField("BLOODKNIGHT_FEIGNED_MERCY");
                Method deferredGet = feignedMercySkill.getType().getMethod("get");
                Field normalHealthLimit = configClass.getField("bloodknightVampireMinHealthFeedingPercentage");
                Field feignedMercyHealthLimit = configClass.getField("bloodknightFeignedVampireMaxHealthFeedingAmount");
                Method configGet = normalHealthLimit.getType().getMethod("get");
                resolvedHandles = new Handles(
                        getManager,
                        getBloodlineId,
                        getRank,
                        getVampireSkillHandler,
                        isSkillEnabled,
                        feignedMercySkill,
                        deferredGet,
                        normalHealthLimit,
                        feignedMercyHealthLimit,
                        configGet
                );
            }
            return resolvedHandles;
        }
    }

    private static boolean isBloodlinesLoaded() {
        try {
            ModList modList = ModList.get();
            return modList != null && modList.isLoaded(BLOODLINES_MOD_ID);
        } catch (RuntimeException | LinkageError exception) {
            return false;
        }
    }

    private static void logResolutionFailure(Throwable exception) {
        if (!resolutionFailureLogged) {
            resolutionFailureLogged = true;
            McaVampirismCompat.debugLog(
                    "[BLOODLINES] Bloodknight bridge unavailable; MCA vampire feeding stays protected: {}",
                    exception.toString()
            );
        }
    }

    private record Handles(
            Method getManager,
            Method getBloodlineId,
            Method getRank,
            Method getVampireSkillHandler,
            Method isSkillEnabled,
            Field feignedMercySkill,
            Method deferredGet,
            Field normalHealthLimit,
            Field feignedMercyHealthLimit,
            Method configGet
    ) {
    }
}
