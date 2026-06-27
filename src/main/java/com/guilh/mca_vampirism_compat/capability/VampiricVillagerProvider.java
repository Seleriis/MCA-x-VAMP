package com.guilh.mca_vampirism_compat.capability;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.VampiricVillagerState;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class VampiricVillagerProvider implements ICapabilitySerializable<CompoundTag> {
    // Keep the legacy namespace for saved villager capability data when the public mod id changes.
    public static final ResourceLocation ID = new ResourceLocation(McaVampirismCompat.LEGACY_DATA_NAMESPACE, "vampiric_villager");

    private final VampiricVillagerState state = new VampiricVillagerState();
    private final LazyOptional<VampiricVillagerState> optional = LazyOptional.of(() -> state);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return cap == ModCapabilities.VAMPIRIC_VILLAGER ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return state.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        state.deserializeNBT(nbt);
    }
}
