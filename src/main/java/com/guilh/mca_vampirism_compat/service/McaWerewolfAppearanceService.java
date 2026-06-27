package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.visual.McaWerewolfVisualData;
import forge.net.mca.entity.VillagerEntityMCA;

public final class McaWerewolfAppearanceService {
    private McaWerewolfAppearanceService() {
    }

    public static void applyWerewolfAppearance(VillagerEntityMCA villager) {
        if (villager == null || villager.level().isClientSide) {
            return;
        }
        if (McaVampireCureService.isCuring(villager)
                || McaVampireCureService.isInPostCureGracePeriod(villager, McaVampireCureService.CureType.WEREWOLF)) {
            return;
        }
        villager.getCapability(ModCapabilities.VAMPIRIC_VILLAGER).ifPresent(state -> {
            state.setWerewolfAppearanceApplied(true);
            state.setWerewolfPartialForm(true);
        });
        McaWerewolfVisualData.sync(villager);
    }

    public static void removeWerewolfAppearance(VillagerEntityMCA villager) {
        if (villager == null || villager.level().isClientSide) {
            return;
        }
        villager.getCapability(ModCapabilities.VAMPIRIC_VILLAGER).ifPresent(state -> {
            state.setWerewolfAppearanceApplied(false);
            state.setWerewolfPartialForm(false);
        });
        McaWerewolfVisualData.sync(villager);
    }
}
