package com.guilh.mca_vampirism_compat.visual;

import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.trait.McaTraitBridge;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

/**
 * Synced render data for MCA 7.6, whose face renderer has no native eye-color
 * accessor. The selected Vampirism eye texture is stable per villager and is
 * also persisted in the compat capability.
 */
public final class McaVampireVisualData {
    private static final int MIN_EYE_TEXTURE = 0;
    private static final int MAX_EYE_TEXTURE = 15;

    public static final EntityDataAccessor<Integer> DATA_VAMPIRE_EYE_INDEX =
            SynchedEntityData.defineId(VillagerEntityMCA.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean> DATA_IS_MCA_VAMPIRE_VISUAL =
            SynchedEntityData.defineId(VillagerEntityMCA.class, EntityDataSerializers.BOOLEAN);

    private McaVampireVisualData() {
    }

    public static void defineSynchedData(VillagerEntityMCA villager) {
        villager.getEntityData().define(DATA_VAMPIRE_EYE_INDEX, -1);
        villager.getEntityData().define(DATA_IS_MCA_VAMPIRE_VISUAL, false);
    }

    public static void ensureEyeIndex(VillagerEntityMCA villager) {
        if (villager == null || villager.level().isClientSide) {
            return;
        }
        if (!McaTraitBridge.isVampireByTraitOrCapability(villager)) {
            clear(villager);
            return;
        }
        int eyeIndex = ModCapabilities.get(villager)
                .map(state -> {
                    if (!state.isVampireEyeIndexAssigned()
                            || !isValidEyeIndex(state.getVampireEyeIndex())) {
                        state.assignVampireEyeIndex(defaultEyeIndex(villager));
                    }
                    return state.getVampireEyeIndex();
                })
                .orElseGet(() -> defaultEyeIndex(villager));
        sync(villager, true, eyeIndex);
    }

    public static void clear(VillagerEntityMCA villager) {
        if (villager != null && !villager.level().isClientSide) {
            sync(villager, false, -1);
        }
    }

    public static boolean isVampireVisual(VillagerEntityMCA villager) {
        if (villager == null) {
            return false;
        }
        try {
            return villager.getEntityData().get(DATA_IS_MCA_VAMPIRE_VISUAL)
                    || McaTraitBridge.hasVampireTrait(villager);
        } catch (RuntimeException ignored) {
            return McaTraitBridge.hasVampireTrait(villager);
        }
    }

    public static int getEyeIndex(VillagerEntityMCA villager) {
        if (villager == null) {
            return MIN_EYE_TEXTURE;
        }
        try {
            int synced = villager.getEntityData().get(DATA_VAMPIRE_EYE_INDEX);
            return isValidEyeIndex(synced) ? synced : defaultEyeIndex(villager);
        } catch (RuntimeException ignored) {
            return defaultEyeIndex(villager);
        }
    }

    private static void sync(VillagerEntityMCA villager, boolean visual, int eyeIndex) {
        villager.getEntityData().set(DATA_IS_MCA_VAMPIRE_VISUAL, visual);
        villager.getEntityData().set(DATA_VAMPIRE_EYE_INDEX, visual ? clampEyeIndex(eyeIndex) : -1);
    }

    private static int defaultEyeIndex(VillagerEntityMCA villager) {
        return clampEyeIndex(Math.floorMod(villager.getUUID().hashCode(), MAX_EYE_TEXTURE + 1));
    }

    private static boolean isValidEyeIndex(int eyeIndex) {
        return eyeIndex >= MIN_EYE_TEXTURE && eyeIndex <= MAX_EYE_TEXTURE;
    }

    private static int clampEyeIndex(int eyeIndex) {
        return Math.max(MIN_EYE_TEXTURE, Math.min(MAX_EYE_TEXTURE, eyeIndex));
    }
}
