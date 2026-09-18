package com.guilh.mca_vampirism_compat.trait;

import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.service.McaVampireStateService;
import com.guilh.mca_vampirism_compat.service.McaWerewolvesCompat;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.ai.Traits;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
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

    private McaTraitBridge() {
    }

    public static void registerTraits() {
        if (vampireTrait == null) {
            vampireTrait = registerTrait(VAMPIRE_TRAIT_ID);
        }
        // MCA exposes every registered trait in its editor. A Werewolf trait
        // without the optional addon has no valid mechanics behind it, so do
        // not register (and therefore do not offer) it in that installation.
        if (McaWerewolvesCompat.isAvailable() && werewolfTrait == null) {
            werewolfTrait = registerTrait(WEREWOLF_TRAIT_ID);
        }
        if (hunterTrait == null) {
            hunterTrait = registerTrait(HUNTER_TRAIT_ID);
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
    }

    public static void removeVampireTrait(VillagerEntityMCA villager) {
        removeTrait(villager, vampireTrait());
    }

    public static boolean hasWerewolfTrait(VillagerEntityMCA villager) {
        return McaWerewolvesCompat.isAvailable()
                && werewolfTrait() != null
                && villager.getTraits().hasTrait(werewolfTrait());
    }

    public static void addWerewolfTrait(VillagerEntityMCA villager) {
        if (McaWerewolvesCompat.isAvailable()
                && McaVampirismCompatConfig.enableMcaWerewolfTrait()
                && werewolfTrait() != null
                && !hasWerewolfTrait(villager)) {
            villager.getTraits().addTrait(werewolfTrait());
        }
    }

    public static void removeWerewolfTrait(VillagerEntityMCA villager) {
        removeTrait(villager, werewolfTrait());
    }

    public static boolean hasHunterTrait(VillagerEntityMCA villager) {
        return hunterTrait() != null && villager.getTraits().hasTrait(hunterTrait());
    }

    public static void addHunterTrait(VillagerEntityMCA villager) {
        if (hunterTrait() != null && !hasHunterTrait(villager)) {
            villager.getTraits().addTrait(hunterTrait());
        }
    }

    public static void removeHunterTrait(VillagerEntityMCA villager) {
        removeTrait(villager, hunterTrait());
    }

    public static void migrateLegacyTraitIds(VillagerEntityMCA villager) {
        // MCA 7.7.36 only accepts valid ResourceLocation trait ids. Legacy
        // upper-case keys are rewritten before MCA reads NBT; capability
        // reconciliation below remains the authority for the resulting state.
        registerTraits();
    }

    /** Rewrites 2.0.0's invalid upper-case trait keys before MCA decodes NBT. */
    public static boolean normalizeLegacyTraitNbt(CompoundTag data) {
        if (data == null) {
            return false;
        }
        boolean changed = normalizeTraitCompound(data.getCompound("Traits"));
        if (data.contains("MCAData", Tag.TAG_COMPOUND)) {
            changed |= normalizeTraitCompound(data.getCompound("MCAData").getCompound("Traits"));
        }
        return changed;
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
        ResourceLocation identifier = ResourceLocation.parse(id);
        Traits.Trait existing = Traits.get(identifier).orElse(null);
        if (existing != null) {
            return existing;
        }
        // Inheritance is handled once, by McaVampireInheritanceHandler. Registering
        // an MCA inherit chance here caused duplicate/bogus inherited faction traits.
        return Traits.register(identifier, 0.0F, 0.0F, false);
    }

    private static boolean normalizeTraitCompound(CompoundTag traits) {
        if (traits == null || traits.isEmpty()) {
            return false;
        }
        return normalizeTraitKey(traits, LEGACY_UPPER_VAMPIRE_TRAIT_ID, VAMPIRE_TRAIT_ID)
                | normalizeTraitKey(traits, LEGACY_UPPER_WEREWOLF_TRAIT_ID, WEREWOLF_TRAIT_ID)
                | normalizeTraitKey(traits, LEGACY_UPPER_HUNTER_TRAIT_ID, HUNTER_TRAIT_ID);
    }

    private static boolean normalizeTraitKey(CompoundTag traits, String legacyKey, String canonicalKey) {
        if (!traits.contains(legacyKey)) {
            return false;
        }
        if (!traits.contains(canonicalKey)) {
            Tag value = traits.get(legacyKey);
            if (value != null) {
                traits.put(canonicalKey, value.copy());
            }
        }
        traits.remove(legacyKey);
        return true;
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
