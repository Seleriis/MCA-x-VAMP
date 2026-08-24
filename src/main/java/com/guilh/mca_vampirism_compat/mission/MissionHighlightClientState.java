package com.guilh.mca_vampirism_compat.mission;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Lives independently in every game client. The server never toggles the
 * entity's synced glowing bit, so other players cannot see this marker.
 */
public final class MissionHighlightClientState {
    private static final Set<UUID> HIGHLIGHTED = new HashSet<>();

    private MissionHighlightClientState() {
    }

    public static void set(UUID villagerId, boolean highlighted) {
        if (villagerId == null) {
            return;
        }
        if (highlighted) {
            HIGHLIGHTED.add(villagerId);
        } else {
            HIGHLIGHTED.remove(villagerId);
        }
    }

    public static boolean isHighlighted(UUID villagerId) {
        return villagerId != null && HIGHLIGHTED.contains(villagerId);
    }

    public static void clear() {
        HIGHLIGHTED.clear();
    }
}
