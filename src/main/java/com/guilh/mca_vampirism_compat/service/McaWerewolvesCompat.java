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

    // NeoForge's mod list is fixed after startup; cache only a completed lookup.
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
            // Pure unit tests do not construct NeoForge's ModList. Treat that
            // environment exactly like an optional addon that is absent.
            return false;
        }
    }
}
