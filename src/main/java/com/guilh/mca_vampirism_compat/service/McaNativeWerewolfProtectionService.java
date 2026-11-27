package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.lang.reflect.Field;

/**
 * Keeps Werewolves' native villager marker from taking ownership of an MCA
 * villager. The native marker has no source or infection context, while its
 * transformation replaces the villager entity with a {@code BasicWerewolf}.
 * MCA faction changes must instead go through the compat's explicit bite,
 * inheritance, or editor flows.
 */
public final class McaNativeWerewolfProtectionService {
    private static final String WEREWOLVES_DATA_KEY = "werewolves";
    private static final String WEREWOLF_MARKER_KEY = "werewolf";

    private static volatile Field nativeWerewolfMarker;
    private static volatile boolean nativeMarkerLookupCompleted;
    private static volatile boolean nativeMarkerLookupFailureLogged;

    private McaNativeWerewolfProtectionService() {
    }

    /**
     * Replaces the native faction setter only on {@link VillagerEntityMCA}.
     * A native mark is deliberately ignored; it never grants a compat trait
     * or capability state.
     */
    public static void ignoreNativeFactionMark(VillagerEntityMCA villager, boolean marked) {
        if (villager == null) {
            return;
        }

        boolean markerWasSet = clearNativeMarker(villager);
        if (marked && isServerSide(villager)) {
            McaVampirismCompat.debugLog(
                    "[WEREWOLF-NATIVE] ignored native faction mark villager={} markerWasSet={} reason=NO_COMPAT_CONVERSION_CONTEXT",
                    villager.getUUID(),
                    markerWasSet
            );
        }
    }

    /**
     * Stops the native full-moon, rage and time-limited path before it can
     * replace the MCA entity. The optional mixin casts this unchanged object
     * back to WerewolfTransformable only when Werewolves is installed.
     */
    public static Object blockNativeTransformation(VillagerEntityMCA villager, Object transformType) {
        if (villager == null) {
            return null;
        }

        clearNativeMarker(villager);
        if (isServerSide(villager)) {
            McaVampirismCompat.debugLog(
                    "[WEREWOLF-NATIVE] blocked physical transformation villager={} type={} reason=MCA_IDENTITY_PRESERVED",
                    villager.getUUID(),
                    transformType
            );
        }
        return villager;
    }

    /**
     * Normalizes only the native boolean left by old saves. It never touches
     * the compat capability, MCA traits, visuals or any valid werewolf state.
     */
    public static void clearLegacyNativeMarker(VillagerEntityMCA villager, CompoundTag entityTag) {
        if (villager == null || entityTag == null) {
            return;
        }

        boolean legacyMarkerFound = false;
        if (entityTag.contains(WEREWOLVES_DATA_KEY, Tag.TAG_COMPOUND)) {
            CompoundTag werewolvesData = entityTag.getCompound(WEREWOLVES_DATA_KEY);
            legacyMarkerFound = werewolvesData.getBoolean(WEREWOLF_MARKER_KEY);
            if (legacyMarkerFound) {
                werewolvesData.remove(WEREWOLF_MARKER_KEY);
                if (werewolvesData.isEmpty()) {
                    entityTag.remove(WEREWOLVES_DATA_KEY);
                }
            }
        }

        boolean markerWasSet = legacyMarkerFound && clearNativeMarker(villager);
        if (legacyMarkerFound && isServerSide(villager)) {
            McaVampirismCompat.debugLog(
                    "[WEREWOLF-NATIVE] cleared legacy native marker villager={} markerWasSet={}",
                    villager.getUUID(),
                    markerWasSet
            );
        }
    }

    private static boolean clearNativeMarker(VillagerEntityMCA villager) {
        Field marker = findNativeMarker(villager);
        if (marker == null) {
            return false;
        }
        try {
            boolean markerWasSet = marker.getBoolean(villager);
            marker.setBoolean(villager, false);
            return markerWasSet;
        } catch (IllegalAccessException exception) {
            logNativeMarkerLookupFailure(villager, exception);
            return false;
        }
    }

    private static Field findNativeMarker(VillagerEntityMCA villager) {
        if (nativeMarkerLookupCompleted) {
            return nativeWerewolfMarker;
        }

        synchronized (McaNativeWerewolfProtectionService.class) {
            if (nativeMarkerLookupCompleted) {
                return nativeWerewolfMarker;
            }
            for (Class<?> type = villager.getClass(); type != null; type = type.getSuperclass()) {
                for (Field field : type.getDeclaredFields()) {
                    if (field.getType() != boolean.class || !isNativeWerewolfMarkerName(field.getName())) {
                        continue;
                    }
                    if (field.trySetAccessible()) {
                        nativeWerewolfMarker = field;
                        nativeMarkerLookupCompleted = true;
                        return nativeWerewolfMarker;
                    }
                }
            }
            nativeMarkerLookupCompleted = true;
            logNativeMarkerLookupFailure(villager, null);
            return null;
        }
    }

    private static boolean isNativeWerewolfMarkerName(String name) {
        return WEREWOLF_MARKER_KEY.equals(name)
                || name.endsWith("$" + WEREWOLF_MARKER_KEY);
    }

    private static void logNativeMarkerLookupFailure(VillagerEntityMCA villager, Exception exception) {
        if (nativeMarkerLookupFailureLogged || !isServerSide(villager)) {
            return;
        }
        nativeMarkerLookupFailureLogged = true;
        McaVampirismCompat.debugLog(
                "[WEREWOLF-NATIVE] could not resolve native marker villager={} error={}",
                villager.getUUID(),
                exception == null ? "field-not-found" : exception.getClass().getSimpleName()
        );
    }

    private static boolean isServerSide(VillagerEntityMCA villager) {
        return villager.level() != null && !villager.level().isClientSide;
    }
}
