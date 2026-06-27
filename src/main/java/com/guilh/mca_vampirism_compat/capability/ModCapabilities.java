package com.guilh.mca_vampirism_compat.capability;

import com.guilh.mca_vampirism_compat.VampiricVillagerState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public final class ModCapabilities {
    public static final Capability<VampiricVillagerState> VAMPIRIC_VILLAGER =
            CapabilityManager.get(new CapabilityToken<>() {
            });

    private ModCapabilities() {
    }
}
