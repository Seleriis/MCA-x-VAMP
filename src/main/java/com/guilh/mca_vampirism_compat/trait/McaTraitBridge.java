package com.guilh.mca_vampirism_compat.trait;

import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.service.McaVampireStateService;
import com.guilh.mca_vampirism_compat.service.McaWerewolvesCompat;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.Traits;
import net.minecraft.world.entity.Entity;

import java.util.List;
import java.util.Locale;

public final class McaTraitBridge {
    private static final String VAMPIRE_TRAIT_ID = "vampirism_mca_compat:vampire";
    private static final String WEREWOLF_TRAIT_ID = "vampirism_mca_compat:werewolf";
    private static final String HUNTER_TRAIT_ID = "vampirism_mca_compat:hunter";
    private static Traits.Trait vampireTrait;
    private static Traits.Trait werewolfTrait;
    private static Traits.Trait hunterTrait;

    private McaTraitBridge() {
    }

    /**
     * Stable trait ids retained from the 1.20.1 save format.  The actual MCA
     * registry is touched only during mod loading; this list is intentionally
     * pure so migration checks do not need a running Minecraft registry.
     */
    public static List<String> legacyTraitIds() {
        return List.of(VAMPIRE_TRAIT_ID, WEREWOLF_TRAIT_ID, HUNTER_TRAIT_ID);
    }

    public static void registerTraits() {
        if (vampireTrait == null) {
            vampireTrait = registerTrait(VAMPIRE_TRAIT_ID);
        }
        // MCA exposes registered traits in the editor. Do not offer a
        // Werewolf faction when its optional backing addon is absent.
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

    /**
     * MCA 7.6 stores the legacy upper-case keys used by the 1.0.3 release.
     * They are deliberately left untouched so a 1.20.1 world upgrades in
     * place rather than losing its existing faction traits.
     */
    public static boolean normalizeLegacyTraitNbt(net.minecraft.nbt.CompoundTag data) {
        return false;
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
        String registryId = id.toUpperCase(Locale.ROOT);
        Traits.Trait existing = Traits.Trait.valueOf(id);
        if (existing != Traits.UNKNOWN) {
            return existing;
        }
        // Inheritance is handled once, by McaVampireInheritanceHandler. Registering
        // an MCA inherit chance here caused duplicate/bogus inherited faction traits.
        return Traits.registerTrait(registryId, 0.0F, 0.0F, false);
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
