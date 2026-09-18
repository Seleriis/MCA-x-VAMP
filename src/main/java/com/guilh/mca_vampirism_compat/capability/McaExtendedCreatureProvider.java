package com.guilh.mca_vampirism_compat.capability;

import de.teamlapen.vampirism.api.VampirismAPI;
import de.teamlapen.vampirism.api.entity.IExtendedCreatureVampirism;
import de.teamlapen.vampirism.entity.ExtendedCreature;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import java.util.function.Function;

/**
 * Supplies Vampirism's native creature capability for MCA villagers.
 *
 * <p>Vampirism only creates this capability for entities known to its own
 * entity registry. MCA villagers are intentionally not registered there when
 * Vampirism Integrations is absent, which otherwise leaves them ineligible
 * before the regular blood-value fallback can run. This provider mirrors
 * Vampirism's own {@code ExtendedCreature#createNewCapability} behavior and
 * still honors a custom constructor registered by another integration.</p>
 */
public final class McaExtendedCreatureProvider implements ICapabilitySerializable<CompoundTag> {
    public static final ResourceLocation ID = new ResourceLocation(
            "mca_vamp_compat",
            "vampirism_extended_creature"
    );

    private final IExtendedCreatureVampirism creature;
    private final LazyOptional<IExtendedCreatureVampirism> optional;

    public McaExtendedCreatureProvider(VillagerEntityMCA villager) {
        Function<VillagerEntityMCA, IExtendedCreatureVampirism> customConstructor =
                VampirismAPI.entityRegistry().getCustomExtendedCreatureConstructor(villager);
        this.creature = customConstructor == null
                ? new ExtendedCreature(villager)
                : customConstructor.apply(villager);
        this.optional = LazyOptional.of(() -> creature);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
        return ExtendedCreature.CAP.orEmpty(capability, optional);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        creature.saveData(tag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        creature.loadData(tag);
    }

    public void invalidate() {
        optional.invalidate();
    }
}
