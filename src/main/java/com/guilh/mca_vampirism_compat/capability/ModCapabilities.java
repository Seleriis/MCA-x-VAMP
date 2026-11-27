package com.guilh.mca_vampirism_compat.capability;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.VampiricVillagerState;
import net.conczin.mca.registry.EntitiesMCA;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.Optional;

public final class ModCapabilities {
    public static final EntityCapability<VampiricVillagerState, Void> VAMPIRIC_VILLAGER =
            EntityCapability.createVoid(
                    ResourceLocation.fromNamespaceAndPath(
                            McaVampirismCompat.LEGACY_DATA_NAMESPACE,
                            "vampiric_villager"),
                    VampiricVillagerState.class);

    private ModCapabilities() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.registerEntity(
                VAMPIRIC_VILLAGER,
                EntitiesMCA.MALE_VILLAGER,
                (entity, context) -> entity.getData(ModAttachments.VAMPIRIC_VILLAGER));
        event.registerEntity(
                VAMPIRIC_VILLAGER,
                EntitiesMCA.FEMALE_VILLAGER,
                (entity, context) -> entity.getData(ModAttachments.VAMPIRIC_VILLAGER));
    }

    public static Optional<VampiricVillagerState> get(Entity entity) {
        return entity == null
                ? Optional.empty()
                : Optional.ofNullable(entity.getCapability(VAMPIRIC_VILLAGER));
    }
}
