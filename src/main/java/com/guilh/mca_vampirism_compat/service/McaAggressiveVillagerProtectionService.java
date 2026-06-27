package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.service.faction.hunter.McaHunterAggressionPolicy;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.relationship.Gender;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.fml.ModList;

public final class McaAggressiveVillagerProtectionService {
    private static boolean detectedModsLogged;

    private McaAggressiveVillagerProtectionService() {
    }

    public static void logDetectedMods() {
        if (detectedModsLogged) {
            return;
        }
        detectedModsLogged = true;

        logDetectedMod("vampirism");
        logDetectedMod("vampirism_integrations");
    }

    public static boolean shouldCancelMakeAggressive(Villager oldVillager) {
        return shouldCancelReplacement(oldVillager, "VampirismVillageEvent.MakeAggressive", null);
    }

    public static boolean shouldCancelReplacement(Villager oldVillager, String source, String newTypeOverride) {
        if (oldVillager == null || oldVillager.level().isClientSide) {
            return false;
        }

        boolean isMca = oldVillager instanceof VillagerEntityMCA;
        String originalType = String.valueOf(oldVillager.getType());
        String newType = newTypeOverride != null ? newTypeOverride
                : isMca ? predictedAggressiveType((VillagerEntityMCA) oldVillager) : "vampirism:villager_angry";
        McaVampirismCompat.debugLog(
                "[AGGRESSIVE-VILLAGERS] replacement attempt source={} target={} originalType={} newType={}",
                source,
                oldVillager.getUUID(),
                originalType,
                newType
        );

        if (!isMca) {
            McaVampirismCompat.debugLog(
                    "[AGGRESSIVE-VILLAGERS] replacement allowed reason=NOT_MCA_VILLAGER target={}",
                    oldVillager.getUUID()
            );
            return false;
        }

        VillagerEntityMCA mcaVillager = (VillagerEntityMCA) oldVillager;
        if (McaVampirismCompatConfig.preventAggressiveVillagerReplacementForAllMcaVillagers()) {
            McaVampirismCompat.debugLog(
                    "[AGGRESSIVE-VILLAGERS] replacement blocked reason=ALL_MCA_PRESERVE_ENTITY target={}",
                    mcaVillager.getUUID()
            );
            handleBlockedMcaReplacement(mcaVillager, source);
            return true;
        }

        boolean isVampire = McaVampireStateService.isVampire(mcaVillager);
        if (isVampire && McaVampirismCompatConfig.preventAggressiveVillagerReplacementForMcaVampires()) {
            McaVampirismCompat.debugLog(
                    "[AGGRESSIVE-VILLAGERS] replacement blocked reason=MCA_VAMPIRE_PRESERVE_ENTITY target={}",
                    mcaVillager.getUUID()
            );
            handleBlockedMcaReplacement(mcaVillager, source);
            return true;
        }

        McaVampirismCompat.debugLog(
                "[AGGRESSIVE-VILLAGERS] replacement allowed reason=NOT_MCA_VAMPIRE target={} isVampire={}",
                mcaVillager.getUUID(),
                isVampire
        );
        return false;
    }

    private static void logDetectedMod(String modId) {
        if (ModList.get().isLoaded(modId)) {
            McaVampirismCompat.debugLog("[AGGRESSIVE-VILLAGERS] mod detected id={}", modId);
        }
    }

    public static String predictedMcaAggressiveType(VillagerEntityMCA villager) {
        return predictedAggressiveType(villager);
    }

    private static void handleBlockedMcaReplacement(VillagerEntityMCA villager, String source) {
        if (McaVampireStateService.isVampire(villager)) {
            McaVampirismCompat.debugLog(
                    "[AGGRESSIVE-VILLAGERS] replacement blocked without hunter state reason=MCA_VAMPIRE target={}",
                    villager.getUUID()
            );
            return;
        }
        boolean hunterVillage = McaHunterAggressionPolicy.canApplyMakeAggressiveAsHunter(
                villager,
                McaHunterAggressionPolicy.CaptureContext.HUNTER_VILLAGE
        );
        if (hunterVillage) {
            McaVillageFactionControlService.applyHunterVillageState(villager, McaHunterAggressionPolicy.HunterSource.MAKE_AGGRESSIVE);
            McaSpecialVillagerCombatService.registerGoalsIfNeeded(villager);
            McaVampirismCompat.debugLog(
                    "[AGGRESSIVE-VILLAGERS] replacement blocked, applying hunter village state target={} source={}",
                    villager.getUUID(),
                    source
            );
            return;
        }
        if (McaVampirismCompatConfig.hunterVillagersOnlyFromHunterControlledVillages()) {
            McaVampirismCompat.debugLog(
                    "[AGGRESSIVE-VILLAGERS] replacement blocked without hunter state reason=NOT_HUNTER_CONTROLLED_VILLAGE target={} source={}",
                    villager.getUUID(),
                    source
            );
            return;
        }
        if (McaHunterAggressionPolicy.canEnterDefensiveAggressiveMode(villager, McaHunterAggressionPolicy.DefensiveReason.MAKE_AGGRESSIVE)) {
            McaDefensiveVillagerService.enableDefenseFromAggressiveReplacementAttempt(villager, source);
            return;
        }
        McaVampirismCompat.debugLog(
                "[AGGRESSIVE-VILLAGERS] replacement blocked without defensive state reason=POLICY_DENIED target={} source={}",
                villager.getUUID(),
                source
        );
    }

    private static String predictedAggressiveType(VillagerEntityMCA villager) {
        Gender gender = villager.getGenetics().getGender();
        String id = gender == Gender.FEMALE ? "female_villager_mca_angry" : "male_villager_mca_angry";
        return "vampirism_integrations:" + id;
    }
}
