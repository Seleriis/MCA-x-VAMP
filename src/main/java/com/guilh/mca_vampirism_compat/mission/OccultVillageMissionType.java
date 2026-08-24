package com.guilh.mca_vampirism_compat.mission;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

/** Immutable definitions for player-facing special-village favors. */
public enum OccultVillageMissionType {
    VAMPIRE_BLOOD("vampirism:vampire", "vampirism:blood_bottle", 2, 4, false),
    VAMPIRE_FANG("vampirism:vampire", "vampirism:vampire_fang", 1, 1, false),
    HUNTER_GARLIC("vampirism:hunter", "vampirism:garlic", 4, 10, false),
    HUNTER_GARLIC_INJECTION("vampirism:hunter", "vampirism:injection_garlic", 1, 1, false),
    HUNTER_URGENT_AID("vampirism:hunter", null, 0, 0, false),
    WEREWOLF_MUTTON("werewolves:werewolf", "minecraft:mutton", 4, 10, true);

    private final String villageFactionId;
    private final ResourceLocation requiredItem;
    private final int minimumRequiredCount;
    private final int maximumRequiredCount;
    private final boolean requiresWerewolves;

    OccultVillageMissionType(String villageFactionId, String requiredItem, int minimumRequiredCount,
                             int maximumRequiredCount,
                             boolean requiresWerewolves) {
        this.villageFactionId = villageFactionId;
        this.requiredItem = requiredItem == null ? null : ResourceLocation.parse(requiredItem);
        this.minimumRequiredCount = minimumRequiredCount;
        this.maximumRequiredCount = maximumRequiredCount;
        this.requiresWerewolves = requiresWerewolves;
    }

    public String villageFactionId() {
        return villageFactionId;
    }

    public ResourceLocation requiredItem() {
        return requiredItem;
    }

    public int requiredCount() {
        // The historical fixed value remains the default for missions saved
        // before quantities became variable.
        return switch (this) {
            case VAMPIRE_BLOOD -> 3;
            case HUNTER_GARLIC, WEREWOLF_MUTTON -> 8;
            default -> minimumRequiredCount;
        };
    }

    public int randomRequiredCount(net.minecraft.util.RandomSource random) {
        if (minimumRequiredCount == maximumRequiredCount) {
            return minimumRequiredCount;
        }
        return minimumRequiredCount + random.nextInt(maximumRequiredCount - minimumRequiredCount + 1);
    }

    public int sanitizeRequiredCount(int value) {
        if (isUrgentAid()) {
            return 0;
        }
        return value >= minimumRequiredCount && value <= maximumRequiredCount ? value : requiredCount();
    }

    public boolean isUrgentAid() {
        return this == HUNTER_URGENT_AID;
    }

    public boolean requiresWerewolves() {
        return requiresWerewolves;
    }

    public String translationKey() {
        return "mca_vamp_compat.village_mission." + name().toLowerCase(Locale.ROOT);
    }

    public ResourceLocation advancementId() {
        return ResourceLocation.fromNamespaceAndPath(
                McaVampirismCompat.MOD_ID,
                switch (this) {
                    case VAMPIRE_BLOOD, VAMPIRE_FANG -> "vampire_village_favor";
                    case HUNTER_GARLIC, HUNTER_GARLIC_INJECTION, HUNTER_URGENT_AID -> "hunter_village_favor";
                    case WEREWOLF_MUTTON -> "werewolf_village_favor";
                }
        );
    }
}
