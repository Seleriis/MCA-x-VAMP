package com.guilh.mca_vampirism_compat.mixin.client;

import net.conczin.mca.client.gui.InteractScreen;
import net.conczin.mca.entity.VillagerLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Gives the close payload a target check before it closes a client screen. */
@Mixin(InteractScreen.class)
public interface InteractScreenAccessor {
    @Accessor("villager")
    VillagerLike<?> mcaVampCompat$getVillager();
}
