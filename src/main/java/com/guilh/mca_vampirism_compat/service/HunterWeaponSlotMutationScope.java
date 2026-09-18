package com.guilh.mca_vampirism_compat.service;

/**
 * Marks the very small set of equipment writes that are owned by the compat
 * itself.  The Hunter weapon guard must reject every external writer, but it
 * still needs to equip the selected weapon, restore the original hand and
 * migrate an older saved state.  A depth counter keeps nested writes safe and
 * guarantees that a failed mutation never leaks authorization to a later tick.
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
