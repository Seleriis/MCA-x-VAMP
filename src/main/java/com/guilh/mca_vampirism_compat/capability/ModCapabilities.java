package com.guilh.mca_vampirism_compat.capability;

import com.guilh.mca_vampirism_compat.VampiricVillagerState;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

import java.util.Optional;

public final class ModCapabilities {
    public static final Capability<VampiricVillagerState> VAMPIRIC_VILLAGER =
            CapabilityManager.get(new CapabilityToken<>() {
            });

    private ModCapabilities() {
    }

    public static Optional<VampiricVillagerState> get(Entity entity) {
        return entity == null
                ? Optional.empty()
                : entity.getCapability(VAMPIRIC_VILLAGER).resolve();
    }
}
