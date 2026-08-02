package com.guilh.mca_vampirism_compat.visual;

import com.guilh.mca_vampirism_compat.trait.McaTraitBridge;
import net.conczin.mca.entity.VillagerEntityMCA;

public final class McaVampireVisualData {
    private McaVampireVisualData() {
    }

    public static boolean isVampireVisual(VillagerEntityMCA villager) {
        return villager != null && McaTraitBridge.hasVampireTrait(villager);
    }
}
