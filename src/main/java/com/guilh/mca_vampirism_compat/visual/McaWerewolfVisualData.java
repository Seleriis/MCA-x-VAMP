package com.guilh.mca_vampirism_compat.visual;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.trait.McaTraitBridge;
import net.conczin.mca.entity.VillagerEntityMCA;

public final class McaWerewolfVisualData {
    private McaWerewolfVisualData() {
    }

    public static void sync(VillagerEntityMCA villager) {
        if (villager != null && !villager.level().isClientSide && isWerewolfVisual(villager)) {
            McaVampirismCompat.debugLog(
                    "[WEREWOLF-APPEARANCE] native trait visual active villager={} ears={} claws={}",
                    villager.getUUID(),
                    McaVampirismCompatConfig.werewolfVillagerEarsEnabled(),
                    McaVampirismCompatConfig.werewolfVillagerClawsEnabled()
            );
        }
    }

    public static boolean isWerewolfVisual(VillagerEntityMCA villager) {
        return McaVampirismCompatConfig.werewolfVillagerAppearanceEnabled()
                && McaTraitBridge.hasWerewolfTrait(villager);
    }
}
