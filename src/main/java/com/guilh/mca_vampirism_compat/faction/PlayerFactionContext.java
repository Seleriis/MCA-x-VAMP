package com.guilh.mca_vampirism_compat.faction;

public record PlayerFactionContext(
        PlayerFaction realFaction,
        PlayerFaction effectiveFaction,
        boolean vampireDisguised,
        boolean werewolf,
        boolean transformedWerewolf,
        String source
) {
    public boolean effectiveVampireAlly() {
        return effectiveFaction == PlayerFaction.VAMPIRE;
    }
}
