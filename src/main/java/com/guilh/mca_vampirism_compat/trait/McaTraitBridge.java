package com.guilh.mca_vampirism_compat.trait;

import com.guilh.mca_vampirism_compat.ai.McaVampireAi;
import com.guilh.mca_vampirism_compat.appearance.McaVampireAppearanceHandler;
import com.guilh.mca_vampirism_compat.attribute.McaVampireAttributeHandler;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.service.McaVampireStateService;
import com.guilh.mca_vampirism_compat.visual.McaVampireVisualData;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.Traits;
import net.minecraft.world.entity.Entity;

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

    public static void registerTraits() {
        if (vampireTrait == null) {
            vampireTrait = registerTrait(VAMPIRE_TRAIT_ID, 1.0F);
        }
        if (werewolfTrait == null) {
            werewolfTrait = registerTrait(WEREWOLF_TRAIT_ID, 0.0F);
        }
        if (hunterTrait == null) {
            hunterTrait = registerTrait(HUNTER_TRAIT_ID, 0.0F);
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
        if (vampireTrait() != null && hasVampireTrait(villager)) {
            villager.getTraits().removeTrait(vampireTrait());
        }
    }

    public static boolean hasWerewolfTrait(VillagerEntityMCA villager) {
        return werewolfTrait() != null && villager.getTraits().hasTrait(werewolfTrait());
    }

    public static void addWerewolfTrait(VillagerEntityMCA villager) {
        if (McaVampirismCompatConfig.enableMcaWerewolfTrait() && werewolfTrait() != null && !hasWerewolfTrait(villager)) {
            villager.getTraits().addTrait(werewolfTrait());
        }
    }

    public static void removeWerewolfTrait(VillagerEntityMCA villager) {
        if (werewolfTrait() != null && hasWerewolfTrait(villager)) {
            villager.getTraits().removeTrait(werewolfTrait());
        }
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
        if (hunterTrait() != null && hasHunterTrait(villager)) {
            villager.getTraits().removeTrait(hunterTrait());
        }
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

    private static Traits.Trait registerTrait(String id, float inheritChance) {
        String registryId = id.toUpperCase(Locale.ROOT);
        Traits.Trait existing = Traits.Trait.valueOf(id);
        if (existing != Traits.UNKNOWN) {
            return existing;
        }
        return Traits.registerTrait(registryId, 0.0F, inheritChance, false);
    }
}
