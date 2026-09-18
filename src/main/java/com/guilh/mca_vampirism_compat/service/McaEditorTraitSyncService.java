package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.trait.McaTraitBridge;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.world.entity.Entity;

/**
 * Makes the MCA editor's saved faction traits authoritative for the complete
 * compat state. MCA persists traits itself; the compat state, appearance,
 * attributes and equipment must be reconciled immediately afterwards.
 */
public final class McaEditorTraitSyncService {
    private McaEditorTraitSyncService() {
    }

    public static void reconcileEditedTraits(VillagerEntityMCA villager) {
        if (villager == null || villager.level().isClientSide) {
            return;
        }

        boolean vampire = McaTraitBridge.hasVampireTrait(villager);
        boolean hunter = McaTraitBridge.hasHunterTrait(villager);
        boolean werewolf = McaTraitBridge.hasWerewolfTrait(villager);

        // Factions are mutually exclusive. The order mirrors the persistent
        // payload resolver, so an accidentally edited conflicting trait set
        // always converges to one stable, saved state.
        if (vampire) {
            becomeVampire(villager);
        } else if (hunter) {
            becomeHunter(villager);
        } else if (werewolf) {
            becomeWerewolf(villager);
        } else {
            becomeHuman(villager);
        }

        McaSpecialStateNormalizer.normalize(villager);
        McaDefensiveVillagerService.reconcileHunterWeapon(villager);
        McaSpecialVillagerCombatService.invalidateTargetingEditedVillager(villager);
        McaVampirismCompat.debugLog(
                "[EDITOR] faction traits reconciled villager={} vampire={} hunter={} werewolf={}",
                villager.getUUID(),
                McaVampireStateService.isVampire(villager),
                McaHunterAlignmentService.isMcaHunterVillager(villager),
                McaWerewolfStateService.isWerewolf(villager)
        );
    }

    /**
     * Applies an Editor-book change only after MCA has loaded the complete
     * entity NBT, including Forge capabilities. The trait choices in that
     * completed Editor transaction are authoritative over the prior capability
     * state, so a saved Vampire cannot overwrite a newly selected Werewolf.
     */
    public static void reconcileCompletedEditorSave(Entity entity) {
        if (entity instanceof VillagerEntityMCA villager) {
            reconcileEditedTraits(villager);
        }
    }

    private static void becomeVampire(VillagerEntityMCA villager) {
        clearHunterState(villager, "editor selected vampire trait");
        McaWerewolfStateService.clearWerewolfState(villager, "editor selected vampire trait");
        McaVampireStateService.markConverted(villager, null);
        McaVampireNightScheduleService.process(villager);
    }

    private static void becomeHunter(VillagerEntityMCA villager) {
        McaVampireStateService.clearVampireState(villager, "editor selected hunter trait");
        McaWerewolfStateService.clearWerewolfState(villager, "editor selected hunter trait");
        ModCapabilities.get(villager).ifPresent(state -> {
            state.clearDefensiveMode();
            state.setHunterTraitApplied(false);
            state.setHunterVillageApplied(false);
            state.setHunterAligned(true, Long.MAX_VALUE);
        });
        McaTraitBridge.addHunterTrait(villager);
    }

    private static void becomeWerewolf(VillagerEntityMCA villager) {
        McaVampireStateService.clearVampireState(villager, "editor selected werewolf trait");
        clearHunterState(villager, "editor selected werewolf trait");
        McaWerewolfStateService.markConverted(villager);
    }

    private static void becomeHuman(VillagerEntityMCA villager) {
        McaVampireStateService.clearVampireState(villager, "editor removed faction traits");
        clearHunterState(villager, "editor removed faction traits");
        McaWerewolfStateService.clearWerewolfState(villager, "editor removed faction traits");
    }

    private static void clearHunterState(VillagerEntityMCA villager, String reason) {
        McaDefensiveVillagerService.clearDefense(villager, reason);
        ModCapabilities.get(villager).ifPresent(state -> {
            state.clearDefensiveMode();
            state.setHunterTraitApplied(false);
            state.setHunterVillageApplied(false);
            state.setHunterAligned(false, Long.MIN_VALUE);
        });
        McaTraitBridge.removeHunterTrait(villager);
        McaDefensiveVillagerService.restoreDefenseWeapon(villager);
        villager.setTarget(null);
    }
}
