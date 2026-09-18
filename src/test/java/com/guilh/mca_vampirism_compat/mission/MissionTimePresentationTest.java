package com.guilh.mca_vampirism_compat.mission;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MissionTimePresentationTest {
    @Test void lessThanOneDayUsesMinutesInsteadOfAlwaysSayingOneDay() {
        expect(23_999L, "minutes", "20");
        expect(6_000L, "minutes", "5");
    }
    @Test void minutesRoundUpWithoutReachingZeroWhileTimeRemains() {
        expect(1_201L, "minutes", "2");
        expect(1_200L, "minute", "1");
        expect(1L, "minute", "1");
    }
    @Test void daysUseSingularAndPluralAtTheBoundary() {
        expect(24_000L, "day", "1");
        expect(24_001L, "days", "2");
        expect(48_000L, "days", "2");
    }
    private static void expect(long ticks, String unit, String amount) {
        var text = OccultVillageMissionService.formattedRemainingTime(ticks);
        var translation = assertInstanceOf(TranslatableContents.class, text.getContents());
        assertEquals("mca_vamp_compat.village_mission.time." + unit, translation.getKey());
        assertEquals(amount, ((Component) translation.getArgs()[0]).getString());
    }
}
