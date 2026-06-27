package com.guilh.mca_vampirism_compat.visual;

import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.trait.McaTraitBridge;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.relationship.Gender;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

public final class McaVampireVisualData {
    private static final int MIN_VAMPIRISM_EYE_TEXTURE = 0;
    private static final int MAX_VAMPIRISM_EYE_TEXTURE = 15;

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
        if (villager.level().isClientSide) {
            return;
        }

        if (!McaTraitBridge.isVampireByTraitOrCapability(villager)) {
            sync(villager, false, -1);
            return;
        }

        int eyeIndex = villager.getCapability(ModCapabilities.VAMPIRIC_VILLAGER)
                .map(state -> {
                    int selectedEyeIndex = selectEyeIndex(villager);
                    if (!state.isVampireEyeIndexAssigned()
                            || !isValidEyeIndex(state.getVampireEyeIndex())
                            || state.getVampireEyeIndex() != selectedEyeIndex) {
                        state.assignVampireEyeIndex(selectedEyeIndex);
                    }
                    return state.getVampireEyeIndex();
                })
                .orElseGet(() -> fallbackEyeIndex(villager));

        sync(villager, true, clampEyeIndex(eyeIndex));
    }

    public static void syncFromCapability(VillagerEntityMCA villager) {
        if (villager.level().isClientSide) {
            return;
        }
        boolean visual = McaTraitBridge.isVampireByTraitOrCapability(villager);
        int eyeIndex = visual ? getSavedOrFallbackEyeIndex(villager) : -1;
        sync(villager, visual, eyeIndex);
    }

    public static boolean isVampireVisual(VillagerEntityMCA villager) {
        return villager.getEntityData().get(DATA_IS_MCA_VAMPIRE_VISUAL) || McaTraitBridge.hasVampireTrait(villager);
    }

    public static int getEyeIndex(VillagerEntityMCA villager) {
        int synced = villager.getEntityData().get(DATA_VAMPIRE_EYE_INDEX);
        return isValidEyeIndex(synced) ? synced : fallbackEyeIndex(villager);
    }

    private static int getSavedOrFallbackEyeIndex(VillagerEntityMCA villager) {
        int selectedEyeIndex = selectEyeIndex(villager);
        return villager.getCapability(ModCapabilities.VAMPIRIC_VILLAGER)
                .map(state -> state.isVampireEyeIndexAssigned()
                        && isValidEyeIndex(state.getVampireEyeIndex())
                        && state.getVampireEyeIndex() == selectedEyeIndex
                        ? state.getVampireEyeIndex()
                        : selectedEyeIndex)
                .orElse(selectedEyeIndex);
    }

    private static int selectEyeIndex(VillagerEntityMCA villager) {
        return fallbackEyeIndex(villager);
    }

    private static int fallbackEyeIndex(VillagerEntityMCA villager) {
        int fallback = villager.getGenetics().getGender() == Gender.FEMALE
                ? McaVampirismCompatConfig.fallbackFemaleVampireEyeIndex()
                : McaVampirismCompatConfig.fallbackMaleVampireEyeIndex();
        return clampEyeIndex(fallback);
    }

    private static void sync(VillagerEntityMCA villager, boolean visual, int eyeIndex) {
        villager.getEntityData().set(DATA_IS_MCA_VAMPIRE_VISUAL, visual);
        villager.getEntityData().set(DATA_VAMPIRE_EYE_INDEX, visual ? clampEyeIndex(eyeIndex) : -1);
    }

    private static boolean isValidEyeIndex(int eyeIndex) {
        return eyeIndex >= MIN_VAMPIRISM_EYE_TEXTURE && eyeIndex <= MAX_VAMPIRISM_EYE_TEXTURE;
    }

    private static int clampEyeIndex(int eyeIndex) {
        return Math.max(MIN_VAMPIRISM_EYE_TEXTURE, Math.min(MAX_VAMPIRISM_EYE_TEXTURE, eyeIndex));
    }
}
