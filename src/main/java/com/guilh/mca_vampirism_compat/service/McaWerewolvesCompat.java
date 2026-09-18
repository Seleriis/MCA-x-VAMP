package com.guilh.mca_vampirism_compat.service;

import net.neoforged.fml.ModList;

/**
 * Single runtime gate for the optional Werewolves integration.
 *
 * <p>The MCA trait is a real, editor-visible MCA registration. Registering
 * it while Werewolves is absent lets players create an unusable faction, so
 * every entry point uses this gate rather than only the native mixin.</p>
 */
public final class McaWerewolvesCompat {
    public static final String MOD_ID = "werewolves";

    private McaWerewolvesCompat() {
    }

    public static boolean isAvailable() {
        try {
            return ModList.get().isLoaded(MOD_ID);
        } catch (RuntimeException ignored) {
            // Pure unit tests do not construct NeoForge's ModList. Treat that
            // environment exactly like an optional addon that is absent.
            return false;
        }
    }
}
