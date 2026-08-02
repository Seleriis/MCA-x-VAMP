package com.guilh.mca_vampirism_compat.trade;

import net.minecraft.world.entity.Entity;

/**
 * Identifies a menu-only merchant wrapper whose position and trade sounds
 * belong to a real entity.
 */
public interface EntityBackedMerchant {
    Entity mcaVampCompat$getTradeSoundEntity();
}
