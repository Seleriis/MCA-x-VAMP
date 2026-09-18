package com.guilh.mca_vampirism_compat.mixin.client;

import forge.net.mca.client.gui.InteractScreen;
import forge.net.mca.entity.VillagerLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Gives the close payload a target check before it closes a client screen. */
@Mixin(InteractScreen.class)
public interface InteractScreenAccessor {
    // InteractScreen belongs to MCA's separately remapped jar, so this field
    // deliberately keeps its published runtime name instead of asking Forge's
    // vanilla refmap to resolve it.
    @Accessor(value = "villager", remap = false)
    VillagerLike<?> mcaVampCompat$getVillager();
}
