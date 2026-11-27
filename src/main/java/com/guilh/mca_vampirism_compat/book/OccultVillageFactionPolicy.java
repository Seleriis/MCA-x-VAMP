package com.guilh.mca_vampirism_compat.book;

import java.util.Set;

/**
 * Pure faction-id policy kept separate from the optional Werewolves API.
 */
public final class OccultVillageFactionPolicy {
    private static final Set<String> SUPPORTED_FACTIONS = Set.of(
            "vampirism:vampire",
            "vampirism:hunter",
            "werewolves:werewolf"
    );

    private OccultVillageFactionPolicy() {
    }

    public static boolean isSupportedFactionId(String factionId) {
        return factionId != null && SUPPORTED_FACTIONS.contains(factionId);
    }

    public static boolean isActiveVillageTotem(
            boolean complete,
            boolean insideVillage,
            boolean disabled,
            int villagePoiCount
    ) {
        return complete && insideVillage && !disabled && villagePoiCount > 0;
    }
}
