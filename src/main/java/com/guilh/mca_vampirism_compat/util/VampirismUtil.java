package com.guilh.mca_vampirism_compat.util;

import de.teamlapen.vampirism.util.Helper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public final class VampirismUtil {
    private VampirismUtil() {
    }

    public static boolean isVampiric(Entity entity) {
        if (entity == null) {
            return false;
        }
        if (entity instanceof Player player) {
            return Helper.isVampire(player);
        }
        if (Helper.isVampire(entity)) {
            return true;
        }
        return McaVillagerUtil.isMcaVampire(entity);
    }

}
