package com.guilh.mca_vampirism_compat.service;

import net.minecraftforge.fml.ModList;

/**
 * Single runtime gate for the optional Werewolves integration.
 *
 * <p>The MCA trait is editor-visible, so it must not be registered when its
 * backing addon is absent. This also keeps old dormant data inert until the
 * addon is installed again.</p>
 */
public final class McaWerewolvesCompat {
    public static final String MOD_ID = "werewolves";

    // The mod list is fixed once Forge has built it, so the answer is cached
    // after the first lookup that actually reached it. This gate sits under
    // every per-tick faction and trait check.
    private static volatile Boolean available;

    private McaWerewolvesCompat() {
    }

    public static boolean isAvailable() {
        Boolean cached = available;
        if (cached != null) {
            return cached;
        }
        try {
            ModList modList = ModList.get();
            if (modList == null) {
                return false;
            }
            boolean loaded = modList.isLoaded(MOD_ID);
            available = loaded;
            return loaded;
        } catch (RuntimeException ignored) {
            // Unit tests have no Forge mod list; optional means absent there.
            return false;
        }
    }
}
