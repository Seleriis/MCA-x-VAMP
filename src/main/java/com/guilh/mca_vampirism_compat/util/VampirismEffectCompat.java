package com.guilh.mca_vampirism_compat.util;

import de.teamlapen.vampirism.core.ModEffects;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;

/**
 * Bridges Vampirism's registry wrapper (which exposes the effect value) to the
 * holder-based mob-effect API introduced by Minecraft 1.21.
 */
public final class VampirismEffectCompat {
    private VampirismEffectCompat() {
    }

    public static Holder<MobEffect> sanguinare() {
        return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.SANGUINARE.get());
    }

    public static Holder<MobEffect> holder(MobEffect effect) {
        return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect);
    }
}
