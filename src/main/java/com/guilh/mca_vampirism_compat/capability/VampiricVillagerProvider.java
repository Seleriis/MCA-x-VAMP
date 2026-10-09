package com.guilh.mca_vampirism_compat.capability;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.VampiricVillagerState;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

/**
 * Forge 1.20.1 capability provider.  The resource location deliberately
 * retains the 1.0.3 namespace so existing ForgeCaps data keeps loading.
 */
public final class VampiricVillagerProvider implements ICapabilitySerializable<CompoundTag> {
    public static final ResourceLocation ID = new ResourceLocation(
            McaVampirismCompat.LEGACY_DATA_NAMESPACE,
            "vampiric_villager"
    );

    private final VampiricVillagerState state = new VampiricVillagerState();
    private final LazyOptional<VampiricVillagerState> optional = LazyOptional.of(() -> state);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
        return capability == ModCapabilities.VAMPIRIC_VILLAGER
                ? optional.cast()
                : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return state.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        state.deserializeNBT(tag);
    }

    public void invalidate() {
        optional.invalidate();
    }
}
