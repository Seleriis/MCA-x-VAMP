package com.guilh.mca_vampirism_compat.book;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OccultVillageFactionPolicyTest {
    @Test
    void acceptsVampireHunterAndOptionalWerewolfVillageFactions() {
        assertTrue(OccultVillageFactionPolicy.isSupportedFactionId("vampirism:vampire"));
        assertTrue(OccultVillageFactionPolicy.isSupportedFactionId("vampirism:hunter"));
        assertTrue(OccultVillageFactionPolicy.isSupportedFactionId("werewolves:werewolf"));
    }

    @Test
    void rejectsNeutralUnknownAndMalformedFactionIds() {
        assertFalse(OccultVillageFactionPolicy.isSupportedFactionId(null));
        assertFalse(OccultVillageFactionPolicy.isSupportedFactionId(""));
        assertFalse(OccultVillageFactionPolicy.isSupportedFactionId("vampirism:neutral"));
        assertFalse(OccultVillageFactionPolicy.isSupportedFactionId("werewolves:human"));
        assertFalse(OccultVillageFactionPolicy.isSupportedFactionId("VAMPIRISM:VAMPIRE"));
    }

    @Test
    void acceptsOnlyCompleteEnabledTotemsAttachedToARealVillage() {
        assertTrue(OccultVillageFactionPolicy.isActiveVillageTotem(true, true, false, 1));
        assertTrue(OccultVillageFactionPolicy.isActiveVillageTotem(true, true, false, 24));

        assertFalse(OccultVillageFactionPolicy.isActiveVillageTotem(false, true, false, 24));
        assertFalse(OccultVillageFactionPolicy.isActiveVillageTotem(true, false, false, 24));
        assertFalse(OccultVillageFactionPolicy.isActiveVillageTotem(true, true, true, 24));
        assertFalse(OccultVillageFactionPolicy.isActiveVillageTotem(true, true, false, 0));
    }
}
