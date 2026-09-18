package com.guilh.mca_vampirism_compat.visual;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.trait.McaTraitBridge;
import forge.net.mca.entity.VillagerEntityMCA;

public final class McaWerewolfVisualData {
    private McaWerewolfVisualData() {
    }

    /** MCA 7.6 calls this during entity data setup; werewolf parts use traits only. */
    public static void defineSynchedData(VillagerEntityMCA villager) {
        // No additional synchronized fields are needed in the Forge 1.20.1 renderer.
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
