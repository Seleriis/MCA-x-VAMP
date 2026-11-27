package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.util.McaVillagerUtil;
import net.minecraft.world.entity.LivingEntity;

/** Centralizes the MCA-vampire blood-source policy across all Vampirism paths. */
public final class McaVampireFeedProtection {
    private McaVampireFeedProtection() {
    }

    public static boolean shouldBlockDrain(LivingEntity source, LivingEntity target) {
        if (target == null
                || !McaVampirismCompatConfig.preventVampiresBitingMcaVampires()
                || !McaVillagerUtil.isMcaVampire(target)) {
            return false;
        }
        boolean bloodknightAllowed = BloodlinesBloodknightBridge.canFeedVampire(source, target);
        if (bloodknightAllowed) {
            McaVampirismCompat.debugLog(
                    "[BLOODLINES] Bloodknight feed allowed source={} target={}",
                    source == null ? null : source.getUUID(),
                    target.getUUID()
            );
        }
        return !bloodknightAllowed;
    }

    public static boolean isMcaVampireTarget(LivingEntity target) {
        return target != null && McaVillagerUtil.isMcaVampire(target);
    }

    /**
     * Keeps Bloodlines' native vampire classification intact while allowing its
     * Bloodknight drinking handler to recognize the MCA Vampire trait locally.
     */
    public static boolean isBloodlinesVampireFeedTarget(boolean nativeVampire, LivingEntity target) {
        return isBloodlinesVampireFeedTarget(nativeVampire, isMcaVampireTarget(target));
    }

    static boolean isBloodlinesVampireFeedTarget(boolean nativeVampire, boolean mcaVampire) {
        return nativeVampire || mcaVampire;
    }
}
