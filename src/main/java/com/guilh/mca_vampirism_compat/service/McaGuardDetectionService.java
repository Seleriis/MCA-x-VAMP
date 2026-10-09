package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import forge.net.conczin.mca.entity.VillagerEntityMCA;

public final class McaGuardDetectionService {
    private static boolean loggedGuardAccessorFailure;

    private McaGuardDetectionService() {
    }

    public static boolean isMcaGuard(VillagerEntityMCA villager) {
        if (villager == null) {
            return false;
        }
        try {
            return villager.isGuard();
        } catch (RuntimeException | LinkageError exception) {
            if (!loggedGuardAccessorFailure) {
                loggedGuardAccessorFailure = true;
                McaVampirismCompat.debugLog(
                        "[GUARD-DEFENSE] MCA guard accessor unavailable error={}",
                        exception.toString()
                );
            }
            return false;
        }
    }
}
