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

    private McaWerewolvesCompat() {
    }

    public static boolean isAvailable() {
        try {
            return ModList.get().isLoaded(MOD_ID);
        } catch (RuntimeException ignored) {
            // Unit tests have no Forge mod list; optional means absent there.
            return false;
        }
    }
}
