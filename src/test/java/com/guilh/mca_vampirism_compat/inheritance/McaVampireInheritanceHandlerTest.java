package com.guilh.mca_vampirism_compat.inheritance;

import com.guilh.mca_vampirism_compat.inheritance.McaVampireInheritanceHandler.InheritedFaction;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.neoforged.fml.loading.LoadingModList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class McaVampireInheritanceHandlerTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        if (LoadingModList.get() == null) {
            LoadingModList.of(List.of(), List.of(), List.of(), List.of(), Map.of());
        }
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void twoVampireParentsAlwaysProduceVampire() {
        assertEquals(InheritedFaction.VAMPIRE,
                decide(2, 0, 0.999999D, 0.0D, true));
    }

    @Test
    void twoWerewolfParentsAlwaysProduceWerewolf() {
        assertEquals(InheritedFaction.WEREWOLF,
                decide(0, 2, 0.0D, 0.999999D, true));
    }

    @Test
    void humanParentsDoNotCreateFactionTrait() {
        assertEquals(InheritedFaction.HUMAN,
                decide(0, 0, 0.0D, 0.0D, true));
    }

    @Test
    void mixedParentsNeverProduceDualFaction() {
        assertEquals(InheritedFaction.VAMPIRE,
                decide(1, 1, 0.0D, 0.0D, true));
        assertEquals(InheritedFaction.WEREWOLF,
                decide(1, 1, 0.0D, 0.0D, false));
    }

    private static InheritedFaction decide(int vampireParents, int werewolfParents,
                                           double vampireRoll, double werewolfRoll, boolean vampireWinsTie) {
        return McaVampireInheritanceHandler.decide(
                vampireParents,
                werewolfParents,
                vampireRoll,
                werewolfRoll,
                vampireWinsTie,
                0.45D,
                1.0D,
                0.45D,
                1.0D
        );
    }
}
