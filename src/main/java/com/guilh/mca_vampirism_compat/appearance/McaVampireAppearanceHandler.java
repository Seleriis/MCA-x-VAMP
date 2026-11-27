package com.guilh.mca_vampirism_compat.appearance;

import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.service.McaVampireCureService;
import com.guilh.mca_vampirism_compat.util.McaVillagerUtil;
import com.guilh.mca_vampirism_compat.visual.McaVampireVisualData;
import forge.net.conczin.mca.entity.VillagerEntityMCA;
import forge.net.conczin.mca.entity.VillagerLike;
import forge.net.conczin.mca.entity.ai.Genetics;

public final class McaVampireAppearanceHandler {
    private static final float MELANIN_ONE_TONE_STEP = 0.16F;
    private static final float HEMOGLOBIN_PALE_STEP = 0.04F;

    private McaVampireAppearanceHandler() {
    }

    public static void applyVampireAppearance(VillagerEntityMCA villager) {
        if (villager.level().isClientSide || !McaVampirismCompatConfig.enablePersistentVampireAppearance()
                || !McaVillagerUtil.isMcaVampire(villager)
                || McaVampireCureService.isCuring(villager)
                || McaVampireCureService.isInPostCureGracePeriod(villager, McaVampireCureService.CureType.VAMPIRE)) {
            return;
        }

        ModCapabilities.get(villager).ifPresent(state -> {
            Genetics genetics = villager.getGenetics();
            McaNativeEyeAppearanceBridge.EyeSnapshot nativeEyes = McaNativeEyeAppearanceBridge.capture(villager);
            McaNativeSkinAppearanceBridge.SkinSnapshot nativeSkin = McaNativeSkinAppearanceBridge.capture(villager);
            if (McaVampirismCompatConfig.preserveOriginalAppearanceForCure() && !state.isOriginalAppearanceSaved()) {
                state.saveOriginalAppearance(
                        genetics.getGene(Genetics.SKIN),
                        genetics.getGene(Genetics.MELANIN),
                        genetics.getGene(Genetics.HEMOGLOBIN),
                        genetics.getGene(Genetics.FACE),
                        villager.getTrackedValue(VillagerLike.CUSTOM_SKIN),
                        0.0F,
                        nativeEyes.eyeDye(),
                        nativeEyes.eyeLeftDye(),
                        nativeSkin.skinDye()
                );
            } else {
                if (nativeEyes.nativeSupported()) {
                    // Worlds made before MCA 7.6.28 have a version-one
                    // appearance snapshot. Add the optional native values.
                    state.upgradeNativeAppearanceSnapshot(
                            0.0F,
                            nativeEyes.eyeDye(),
                            nativeEyes.eyeLeftDye(),
                            nativeSkin.skinDye()
                    );
                }
                if (nativeSkin.nativeSupported()) {
                    // Version-two snapshots used a placeholder skin value.
                    // The live dye is still the original one because older
                    // compat builds never changed it, so migrate it before
                    // clearing the dye for the vampire palette.
                    state.upgradeOriginalSkinDyeSnapshot(nativeSkin.skinDye());
                }
            }

            restoreOriginalFaceIfSaved(villager);
            McaVampireVisualData.ensureEyeIndex(villager);
            if (nativeEyes.nativeSupported()) {
                if (McaVampirismCompatConfig.vampireEyesEnabled()) {
                    McaNativeEyeAppearanceBridge.applyVampireEyes(villager);
                } else if (state.isOriginalAppearanceSaved()) {
                    McaNativeEyeAppearanceBridge.restoreOriginalEyes(
                            villager,
                            state.getOriginalEyeDye(),
                            state.getOriginalEyeLeftDye()
                    );
                } else {
                    McaNativeEyeAppearanceBridge.restoreOriginalEyes(villager, -1, -1);
                }
            }
            if (McaVampirismCompatConfig.paleSkinEnabled()) {
                applyOneToneLighterSkin(villager, state);
                if (state.hasOriginalSkinDyeSnapshot()) {
                    McaNativeSkinAppearanceBridge.clearForVampireAppearance(villager);
                }
            }
            state.setVampireAppearanceApplied(
                    McaVampirismCompatConfig.vampireEyesEnabled()
                            || McaVampirismCompatConfig.paleSkinEnabled()
            );
        });
    }

    public static void restoreOriginalAppearance(VillagerEntityMCA villager) {
        if (villager.level().isClientSide) {
            return;
        }

        ModCapabilities.get(villager).ifPresent(state -> {
            if (!state.isOriginalAppearanceSaved()) {
                // The appearance snapshot can be intentionally disabled by
                // configuration. Do not leave a native MCA 7.6.28 eye dye
                // behind when that villager stops being a vampire.
                McaNativeEyeAppearanceBridge.restoreOriginalEyes(villager, -1, -1);
                state.setVampireAppearanceApplied(false);
                McaVampireVisualData.clear(villager);
                return;
            }

            Genetics genetics = villager.getGenetics();
            genetics.setGene(Genetics.SKIN, state.getOriginalSkinGene());
            genetics.setGene(Genetics.MELANIN, state.getOriginalMelaninGene());
            genetics.setGene(Genetics.HEMOGLOBIN, state.getOriginalHemoglobinGene());
            genetics.setGene(Genetics.FACE, state.getOriginalFaceGene());
            villager.setTrackedValue(VillagerLike.CUSTOM_SKIN, state.getOriginalCustomSkin());
            if (state.hasOriginalSkinDyeSnapshot()) {
                McaNativeSkinAppearanceBridge.restoreOriginalSkinDye(
                        villager,
                        state.getOriginalSkinDye()
                );
            }
            McaNativeEyeAppearanceBridge.restoreOriginalEyes(
                    villager,
                    state.getOriginalEyeDye(),
                    state.getOriginalEyeLeftDye()
            );
            state.setVampireAppearanceApplied(false);
            McaVampireVisualData.clear(villager);
        });
    }

    public static boolean hasVampireAppearance(VillagerEntityMCA villager) {
        return ModCapabilities.get(villager)
                .map(state -> state.isVampireAppearanceApplied())
                .orElse(false);
    }

    private static void applyOneToneLighterSkin(VillagerEntityMCA villager,
                                                com.guilh.mca_vampirism_compat.VampiricVillagerState state) {
        Genetics genetics = villager.getGenetics();
        float baseSkin = state.isOriginalAppearanceSaved()
                ? state.getOriginalSkinGene()
                : genetics.getGene(Genetics.SKIN);
        float baseMelanin = state.isOriginalAppearanceSaved()
                ? state.getOriginalMelaninGene()
                : genetics.getGene(Genetics.MELANIN);
        float baseHemoglobin = state.isOriginalAppearanceSaved()
                ? state.getOriginalHemoglobinGene()
                : genetics.getGene(Genetics.HEMOGLOBIN);

        genetics.setGene(Genetics.SKIN, oneToneLighterSkinGene(baseSkin));
        genetics.setGene(Genetics.MELANIN, clamp(baseMelanin - MELANIN_ONE_TONE_STEP, 0.0F, 1.0F));
        genetics.setGene(Genetics.HEMOGLOBIN, clamp(baseHemoglobin - HEMOGLOBIN_PALE_STEP, 0.0F, 1.0F));

    }

    private static void restoreOriginalFaceIfSaved(VillagerEntityMCA villager) {
        ModCapabilities.get(villager).ifPresent(state -> {
            if (state.isOriginalAppearanceSaved()) {
                villager.getGenetics().setGene(Genetics.FACE, state.getOriginalFaceGene());
            }
        });
    }

    static float oneToneLighterSkinGene(float originalSkinGene) {
        int originalTone = Math.min(4, (int) (clamp(originalSkinGene, 0.0F, 1.0F) * 5.0F));
        int lighterTone = Math.max(0, originalTone - 1);
        return lighterTone / 5.0F;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
