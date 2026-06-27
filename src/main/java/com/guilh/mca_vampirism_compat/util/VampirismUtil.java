package com.guilh.mca_vampirism_compat.util;

import de.teamlapen.vampirism.util.Helper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class VampirismUtil {
    private static final double BITE_RANGE_SQR = 16.0D;

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

    public static boolean isCloseLivingAttack(Entity attacker, LivingEntity target) {
        if (!(attacker instanceof LivingEntity)) {
            return false;
        }
        Vec3 attackerPos = attacker.position();
        Vec3 targetPos = target.position();
        return attackerPos.distanceToSqr(targetPos) <= BITE_RANGE_SQR;
    }
}
