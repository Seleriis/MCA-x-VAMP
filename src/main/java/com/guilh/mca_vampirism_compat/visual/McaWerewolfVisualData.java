package com.guilh.mca_vampirism_compat.visual;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.service.McaVampireCureService;
import com.guilh.mca_vampirism_compat.trait.McaTraitBridge;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

public final class McaWerewolfVisualData {
    public static final EntityDataAccessor<Boolean> DATA_IS_MCA_WEREWOLF_VISUAL =
            SynchedEntityData.defineId(VillagerEntityMCA.class, EntityDataSerializers.BOOLEAN);

    private McaWerewolfVisualData() {
    }

    public static void defineSynchedData(VillagerEntityMCA villager) {
        villager.getEntityData().define(DATA_IS_MCA_WEREWOLF_VISUAL, false);
    }

    public static void sync(VillagerEntityMCA villager) {
        if (villager == null || villager.level().isClientSide) {
            return;
        }
        if (McaVampireCureService.isInPostCureGracePeriod(villager, McaVampireCureService.CureType.WEREWOLF)) {
            villager.getEntityData().set(DATA_IS_MCA_WEREWOLF_VISUAL, false);
            return;
        }
        boolean visual = McaVampirismCompatConfig.werewolfVillagerAppearanceEnabled()
                && !McaVampirismCompatConfig.werewolfVillagerUseFullWerewolfModel()
                && (McaTraitBridge.hasWerewolfTrait(villager)
                || villager.getCapability(ModCapabilities.VAMPIRIC_VILLAGER)
                .map(state -> state.isWerewolfConverted()
                        || state.isWerewolfAppearanceApplied()
                        || state.isWerewolfPartialForm())
                .orElse(false));
        boolean previous = villager.getEntityData().get(DATA_IS_MCA_WEREWOLF_VISUAL);
        villager.getEntityData().set(DATA_IS_MCA_WEREWOLF_VISUAL, visual);
        if (!previous && visual) {
            McaVampirismCompat.debugLog(
                    "[WEREWOLF-APPEARANCE] applied partial visual villager={} ears={} claws={}",
                    villager.getUUID(),
                    McaVampirismCompatConfig.werewolfVillagerEarsEnabled(),
                    McaVampirismCompatConfig.werewolfVillagerClawsEnabled()
            );
        }
    }

    public static boolean isWerewolfVisual(VillagerEntityMCA villager) {
        return McaVampirismCompatConfig.werewolfVillagerAppearanceEnabled()
                && !McaVampirismCompatConfig.werewolfVillagerUseFullWerewolfModel()
                && (villager.getEntityData().get(DATA_IS_MCA_WEREWOLF_VISUAL)
                || McaTraitBridge.hasWerewolfTrait(villager));
    }
}
