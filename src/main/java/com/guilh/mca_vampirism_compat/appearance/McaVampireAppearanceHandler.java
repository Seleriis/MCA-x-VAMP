package com.guilh.mca_vampirism_compat.appearance;

import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.service.McaVampireCureService;
import com.guilh.mca_vampirism_compat.util.McaVillagerUtil;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.ai.Genetics;

public final class McaVampireAppearanceHandler {
    private static final int MCA_DEFAULT_SKIN_DYE = 0xFF000000;
    private static final int VAMPIRE_RED_EYE_DYE = 0xFFFF0000;
    private static final float MAX_EYE_BRIGHTNESS = 1.0F;
    private static final float SKIN_DYE_LIGHTEN_FACTOR = 0.14F;
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
            if (McaVampirismCompatConfig.preserveOriginalAppearanceForCure() && !state.isOriginalAppearanceSaved()) {
                state.saveOriginalAppearance(
                        genetics.getGene(Genetics.SKIN),
                        genetics.getGene(Genetics.MELANIN),
                        genetics.getGene(Genetics.HEMOGLOBIN),
                        genetics.getGene(Genetics.FACE),
                        villager.getSkin(),
                        genetics.getGene(Genetics.EYE_BRIGHTNESS),
                        villager.getEyeDye(),
                        villager.getEyeLeftDye(),
                        villager.getSkinDye()
                );
            } else if (state.isOriginalAppearanceSaved() && state.getAppearanceSnapshotVersion() < 2) {
                // Upgrade snapshots made by 1.0.3/2.0.0 before touching the new
                // native eye and skin-dye values.
                state.upgradeNativeAppearanceSnapshot(
                        genetics.getGene(Genetics.EYE_BRIGHTNESS),
                        villager.getEyeDye(),
                        villager.getEyeLeftDye(),
                        villager.getSkinDye()
                );
            }

            restoreOriginalFaceIfSaved(villager);
            applyNativeVampireEyes(villager);
            if (McaVampirismCompatConfig.paleSkinEnabled()) {
                applyOneToneLighterSkin(villager, state);
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
                state.setVampireAppearanceApplied(false);
                return;
            }

            Genetics genetics = villager.getGenetics();
            genetics.setGene(Genetics.SKIN, state.getOriginalSkinGene());
            genetics.setGene(Genetics.MELANIN, state.getOriginalMelaninGene());
            genetics.setGene(Genetics.HEMOGLOBIN, state.getOriginalHemoglobinGene());
            genetics.setGene(Genetics.FACE, state.getOriginalFaceGene());
            villager.setSkin(state.getOriginalCustomSkin());
            if (state.getAppearanceSnapshotVersion() >= 2) {
                genetics.setGene(Genetics.EYE_BRIGHTNESS, state.getOriginalEyeBrightnessGene());
                villager.setEyeDye(state.getOriginalEyeDye());
                villager.setEyeLeftDye(state.getOriginalEyeLeftDye());
                villager.setSkinDye(state.getOriginalSkinDye());
            }
            state.setVampireAppearanceApplied(false);
        });
    }

    public static boolean hasVampireAppearance(VillagerEntityMCA villager) {
        return ModCapabilities.get(villager)
                .map(state -> state.isVampireAppearanceApplied())
                .orElse(false);
    }

    private static void applyNativeVampireEyes(VillagerEntityMCA villager) {
        if (!McaVampirismCompatConfig.vampireEyesEnabled()) {
            return;
        }
        Genetics genetics = villager.getGenetics();
        villager.setEyeDye(VAMPIRE_RED_EYE_DYE);
        villager.setEyeLeftDye(VAMPIRE_RED_EYE_DYE);
        genetics.setGene(Genetics.EYE_BRIGHTNESS, MAX_EYE_BRIGHTNESS);
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

        int baseSkinDye = state.isOriginalAppearanceSaved()
                ? state.getOriginalSkinDye()
                : villager.getSkinDye();
        if (baseSkinDye != MCA_DEFAULT_SKIN_DYE) {
            villager.setSkinDye(oneToneLighterColor(baseSkinDye));
        }
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

    static int oneToneLighterColor(int argb) {
        if (argb == MCA_DEFAULT_SKIN_DYE) {
            return argb;
        }
        int alpha = argb >>> 24;
        int red = lightenChannel((argb >>> 16) & 0xFF);
        int green = lightenChannel((argb >>> 8) & 0xFF);
        int blue = lightenChannel(argb & 0xFF);
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    private static int lightenChannel(int channel) {
        return Math.min(255, Math.round(channel + (255 - channel) * SKIN_DYE_LIGHTEN_FACTOR));
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
