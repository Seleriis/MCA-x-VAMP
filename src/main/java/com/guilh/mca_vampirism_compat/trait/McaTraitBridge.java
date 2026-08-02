package com.guilh.mca_vampirism_compat.trait;

import com.guilh.mca_vampirism_compat.ai.McaVampireAi;
import com.guilh.mca_vampirism_compat.appearance.McaVampireAppearanceHandler;
import com.guilh.mca_vampirism_compat.attribute.McaVampireAttributeHandler;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.service.McaVampireStateService;
import com.guilh.mca_vampirism_compat.visual.McaVampireVisualData;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.ai.Traits;
import net.minecraft.world.entity.Entity;

public final class McaTraitBridge {
    private static final String VAMPIRE_TRAIT_ID = "vampirism_mca_compat:vampire";
    private static final String WEREWOLF_TRAIT_ID = "vampirism_mca_compat:werewolf";
    private static final String HUNTER_TRAIT_ID = "vampirism_mca_compat:hunter";
    private static final String LEGACY_UPPER_VAMPIRE_TRAIT_ID = "VAMPIRISM_MCA_COMPAT:VAMPIRE";
    private static final String LEGACY_UPPER_WEREWOLF_TRAIT_ID = "VAMPIRISM_MCA_COMPAT:WEREWOLF";
    private static final String LEGACY_UPPER_HUNTER_TRAIT_ID = "VAMPIRISM_MCA_COMPAT:HUNTER";
    private static Traits.Trait vampireTrait;
    private static Traits.Trait werewolfTrait;
    private static Traits.Trait hunterTrait;
    private static Traits.Trait legacyUpperVampireTrait;
    private static Traits.Trait legacyUpperWerewolfTrait;
    private static Traits.Trait legacyUpperHunterTrait;

    private McaTraitBridge() {
    }

    public static void registerTraits() {
        if (vampireTrait == null) {
            vampireTrait = registerTrait(VAMPIRE_TRAIT_ID);
        }
        if (werewolfTrait == null) {
            werewolfTrait = registerTrait(WEREWOLF_TRAIT_ID);
        }
        if (hunterTrait == null) {
            hunterTrait = registerTrait(HUNTER_TRAIT_ID);
        }

        // 2.0.0 accidentally registered these ids in upper case. Keep aliases long
        // enough to read affected worlds, then migrate each villager to canonical ids.
        if (legacyUpperVampireTrait == null) {
            legacyUpperVampireTrait = registerTrait(LEGACY_UPPER_VAMPIRE_TRAIT_ID);
        }
        if (legacyUpperWerewolfTrait == null) {
            legacyUpperWerewolfTrait = registerTrait(LEGACY_UPPER_WEREWOLF_TRAIT_ID);
        }
        if (legacyUpperHunterTrait == null) {
            legacyUpperHunterTrait = registerTrait(LEGACY_UPPER_HUNTER_TRAIT_ID);
        }
    }

    public static boolean hasVampireTrait(VillagerEntityMCA villager) {
        return vampireTrait() != null && villager.getTraits().hasTrait(vampireTrait());
    }

    public static boolean hasVampireTrait(Entity entity) {
        return entity instanceof VillagerEntityMCA villager && hasVampireTrait(villager);
    }

    public static void addVampireTrait(VillagerEntityMCA villager) {
        if (McaVampirismCompatConfig.enableMcaVampireTrait() && vampireTrait() != null && !hasVampireTrait(villager)) {
            villager.getTraits().addTrait(vampireTrait());
        }
        removeTrait(villager, legacyUpperVampireTrait);
    }

    public static void removeVampireTrait(VillagerEntityMCA villager) {
        removeTrait(villager, vampireTrait());
        removeTrait(villager, legacyUpperVampireTrait);
    }

    public static boolean hasWerewolfTrait(VillagerEntityMCA villager) {
        return werewolfTrait() != null && villager.getTraits().hasTrait(werewolfTrait());
    }

    public static void addWerewolfTrait(VillagerEntityMCA villager) {
        if (McaVampirismCompatConfig.enableMcaWerewolfTrait() && werewolfTrait() != null && !hasWerewolfTrait(villager)) {
            villager.getTraits().addTrait(werewolfTrait());
        }
        removeTrait(villager, legacyUpperWerewolfTrait);
    }

    public static void removeWerewolfTrait(VillagerEntityMCA villager) {
        removeTrait(villager, werewolfTrait());
        removeTrait(villager, legacyUpperWerewolfTrait);
    }

    public static boolean hasHunterTrait(VillagerEntityMCA villager) {
        return hunterTrait() != null && villager.getTraits().hasTrait(hunterTrait());
    }

    public static void addHunterTrait(VillagerEntityMCA villager) {
        if (hunterTrait() != null && !hasHunterTrait(villager)) {
            villager.getTraits().addTrait(hunterTrait());
        }
        removeTrait(villager, legacyUpperHunterTrait);
    }

    public static void removeHunterTrait(VillagerEntityMCA villager) {
        removeTrait(villager, hunterTrait());
        removeTrait(villager, legacyUpperHunterTrait);
    }

    public static void migrateLegacyTraitIds(VillagerEntityMCA villager) {
        registerTraits();
        ModCapabilities.get(villager).ifPresent(state -> {
            boolean hadLegacyVampire = hasTrait(villager, legacyUpperVampireTrait);
            boolean hadLegacyWerewolf = hasTrait(villager, legacyUpperWerewolfTrait);
            boolean hadLegacyHunter = hasTrait(villager, legacyUpperHunterTrait);
            if (!hadLegacyVampire && !hadLegacyWerewolf && !hadLegacyHunter) {
                return;
            }

            removeTrait(villager, legacyUpperVampireTrait);
            removeTrait(villager, legacyUpperWerewolfTrait);
            removeTrait(villager, legacyUpperHunterTrait);

            // Capability state is authoritative. This deliberately drops the bogus
            // vampire trait that MCA 2.0.0 could inherit onto an otherwise human baby.
            if (hadLegacyVampire && state.isConverted()) {
                addVampireTrait(villager);
            }
            if (hadLegacyWerewolf && state.isWerewolfConverted()) {
                addWerewolfTrait(villager);
            }
            if (hadLegacyHunter && (state.isHunterAligned()
                    || state.isHunterTraitApplied()
                    || state.isHunterVillageApplied())) {
                addHunterTrait(villager);
            }
        });
    }

    public static void syncVampireTraitWithCapability(VillagerEntityMCA villager) {
        if (!McaVampirismCompatConfig.syncTraitAndCapabilityOnLoad()) {
            return;
        }
        McaVampireStateService.syncTraitAndCapability(villager);
    }

    public static void ensureVampireState(VillagerEntityMCA villager) {
        McaVampireStateService.ensureVampireState(villager);
    }

    public static boolean isVampireByTraitOrCapability(Entity entity) {
        return McaVampireStateService.isVampire(entity);
    }

    private static Traits.Trait vampireTrait() {
        registerTraits();
        return vampireTrait;
    }

    private static Traits.Trait werewolfTrait() {
        registerTraits();
        return werewolfTrait;
    }

    private static Traits.Trait hunterTrait() {
        registerTraits();
        return hunterTrait;
    }

    private static Traits.Trait registerTrait(String id) {
        Traits.Trait existing = Traits.Trait.valueOf(id);
        if (existing != null) {
            return existing;
        }
        // Inheritance is handled once, by McaVampireInheritanceHandler. Registering
        // an MCA inherit chance here caused duplicate/bogus inherited faction traits.
        return Traits.registerTrait(id, 0.0F, 0.0F, false);
    }

    private static boolean hasTrait(VillagerEntityMCA villager, Traits.Trait trait) {
        return trait != null && villager.getTraits().hasTrait(trait);
    }

    private static void removeTrait(VillagerEntityMCA villager, Traits.Trait trait) {
        if (hasTrait(villager, trait)) {
            villager.getTraits().removeTrait(trait);
        }
    }
}
