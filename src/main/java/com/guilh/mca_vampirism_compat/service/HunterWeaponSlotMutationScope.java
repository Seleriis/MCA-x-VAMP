package com.guilh.mca_vampirism_compat.service;

/**
 * Marks the very small set of equipment writes owned by the compat itself.
 * Persistent Hunter weapon ownership rejects every external writer, while
 * this scope permits only equip, restore and legacy-state migration writes.
 */
public final class HunterWeaponSlotMutationScope {
    private static final ThreadLocal<Integer> AUTHORIZED_DEPTH = ThreadLocal.withInitial(() -> 0);

    private HunterWeaponSlotMutationScope() {
    }

    public static void run(Runnable action) {
        int previousDepth = AUTHORIZED_DEPTH.get();
        AUTHORIZED_DEPTH.set(previousDepth + 1);
        try {
            action.run();
        } finally {
            if (previousDepth == 0) {
                AUTHORIZED_DEPTH.remove();
            } else {
                AUTHORIZED_DEPTH.set(previousDepth);
            }
        }
    }

    public static boolean isAuthorized() {
        return AUTHORIZED_DEPTH.get() > 0;
    }
}
