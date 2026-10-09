package com.guilh.mca_vampirism_compat.util;

import de.teamlapen.vampirism.core.ModEffects;
import net.minecraft.world.effect.MobEffect;

/**
 * Exposes Vampirism effects in the native Forge 1.20.1 representation.
 * LivingEntity's effect APIs take a MobEffect directly in this version; do not
 * wrap the value in a 1.21 Holder.
 */
public final class VampirismEffectCompat {
    private VampirismEffectCompat() {
    }

    public static MobEffect sanguinare() {
        return ModEffects.SANGUINARE.get();
    }
}
