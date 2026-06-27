package com.guilh.mca_vampirism_compat.appearance;

import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.service.McaVampireCureService;
import com.guilh.mca_vampirism_compat.util.McaVillagerUtil;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.VillagerLike;
import forge.net.mca.entity.ai.Genetics;

public final class McaVampireAppearanceHandler {
    private static final float[] PALE_SKIN_GENE = {
            0.0F,
            0.2F,
            0.4F,
            0.6F,
            0.8F,
            0.8F
    };
    private static final float[] PALE_MELANIN = {
            0.08F,
            0.18F,
            0.30F,
            0.42F,
            0.56F,
            0.70F
    };
    private static final float[] PALE_HEMOGLOBIN = {
            0.14F,
            0.13F,
            0.12F,
            0.10F,
            0.08F,
            0.06F
    };

    private McaVampireAppearanceHandler() {
    }

    public static void applyVampireAppearance(VillagerEntityMCA villager) {
        if (villager.level().isClientSide || !McaVampirismCompatConfig.enablePersistentVampireAppearance()
                || !McaVillagerUtil.isMcaVampire(villager)
                || McaVampireCureService.isCuring(villager)
                || McaVampireCureService.isInPostCureGracePeriod(villager, McaVampireCureService.CureType.VAMPIRE)) {
            return;
        }

        villager.getCapability(ModCapabilities.VAMPIRIC_VILLAGER).ifPresent(state -> {
            Genetics genetics = villager.getGenetics();
            if (McaVampirismCompatConfig.preserveOriginalAppearanceForCure() && !state.isOriginalAppearanceSaved()) {
                state.saveOriginalAppearance(
                        genetics.getGene(Genetics.SKIN),
                        genetics.getGene(Genetics.MELANIN),
                        genetics.getGene(Genetics.HEMOGLOBIN),
                        genetics.getGene(Genetics.FACE),
                        villager.getTrackedValue(VillagerLike.CUSTOM_SKIN)
                );
            }

            boolean applied = false;
            restoreOriginalFaceIfSaved(villager);
            if (McaVampirismCompatConfig.paleSkinEnabled()) {
                applyPaleSkin(villager);
                applied = true;
            }
            state.setVampireAppearanceApplied(applied);
        });
    }

    public static void restoreOriginalAppearance(VillagerEntityMCA villager) {
        if (villager.level().isClientSide) {
            return;
        }

        villager.getCapability(ModCapabilities.VAMPIRIC_VILLAGER).ifPresent(state -> {
            if (!state.isOriginalAppearanceSaved()) {
                state.setVampireAppearanceApplied(false);
                return;
            }

            Genetics genetics = villager.getGenetics();
            genetics.setGene(Genetics.SKIN, state.getOriginalSkinGene());
            genetics.setGene(Genetics.MELANIN, state.getOriginalMelaninGene());
            genetics.setGene(Genetics.HEMOGLOBIN, state.getOriginalHemoglobinGene());
            genetics.setGene(Genetics.FACE, state.getOriginalFaceGene());
            villager.setTrackedValue(VillagerLike.CUSTOM_SKIN, state.getOriginalCustomSkin());
            state.setVampireAppearanceApplied(false);
        });
    }

    public static boolean hasVampireAppearance(VillagerEntityMCA villager) {
        return villager.getCapability(ModCapabilities.VAMPIRIC_VILLAGER)
                .map(state -> state.isVampireAppearanceApplied())
                .orElse(false);
    }

    private static void applyPaleSkin(VillagerEntityMCA villager) {
        Genetics genetics = villager.getGenetics();
        int tone = inferTone(genetics);
        genetics.setGene(Genetics.SKIN, PALE_SKIN_GENE[tone]);
        genetics.setGene(Genetics.MELANIN, PALE_MELANIN[tone]);
        genetics.setGene(Genetics.HEMOGLOBIN, PALE_HEMOGLOBIN[tone]);
    }

    private static void restoreOriginalFaceIfSaved(VillagerEntityMCA villager) {
        villager.getCapability(ModCapabilities.VAMPIRIC_VILLAGER).ifPresent(state -> {
            if (state.isOriginalAppearanceSaved()) {
                villager.getGenetics().setGene(Genetics.FACE, state.getOriginalFaceGene());
            }
        });
    }

    private static int inferTone(Genetics genetics) {
        float melanin = clamp(genetics.getGene(Genetics.MELANIN), 0.0F, 1.0F);
        return Math.min(PALE_MELANIN.length - 1, Math.round(melanin * (PALE_MELANIN.length - 1)));
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
