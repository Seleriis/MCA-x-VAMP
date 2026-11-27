package com.guilh.mca_vampirism_compat.mixin.accessor;

import de.teamlapen.vampirism.blockentity.TotemBlockEntity;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

/**
 * Read-only access to the state Vampirism itself uses to decide whether a
 * faction totem represents a complete, active village.
 */
@Mixin(value = TotemBlockEntity.class, remap = false)
public interface TotemBlockEntityAccessor {
    @Accessor("isComplete")
    boolean mcaVampCompat$isComplete();

    @Accessor("isInsideVillage")
    boolean mcaVampCompat$isInsideVillage();

    @Accessor("isDisabled")
    boolean mcaVampCompat$isDisabled();

    @Accessor("village")
    Set<PoiRecord> mcaVampCompat$getVillagePois();
}
