package com.guilh.mca_vampirism_compat.mixin.accessor;

import forge.net.conczin.mca.server.world.data.Village;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Read-only access to the server world that owns an MCA village. */
@Mixin(value = Village.class, remap = false)
public interface VillageAccessor {
    @Accessor("world")
    ServerLevel mcaVampCompat$getWorld();
}
