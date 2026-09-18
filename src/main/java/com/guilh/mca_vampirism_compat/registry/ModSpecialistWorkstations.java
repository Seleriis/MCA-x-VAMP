package com.guilh.mca_vampirism_compat.registry;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import de.teamlapen.vampirism.core.ModBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;

/**
 * Alternative job sites for the faction-specialist professions. The original
 * faction totems stay registered by Vampirism and Werewolves themselves.
 */
public final class ModSpecialistWorkstations {
    public static final ResourceLocation VAMPIRE_SPECIALIST_ALTAR_ID =
            new ResourceLocation(McaVampirismCompat.MOD_ID, "vampire_specialist_altar");
    public static final ResourceLocation HUNTER_SPECIALIST_TABLE_ID =
            new ResourceLocation(McaVampirismCompat.MOD_ID, "hunter_specialist_table");
    public static final ResourceLocation WEREWOLF_SPECIALIST_ALTAR_ID =
            new ResourceLocation(McaVampirismCompat.MOD_ID, "werewolf_specialist_altar");

    private static final String WEREWOLVES_MOD_ID = "werewolves";
    private static final ResourceLocation WEREWOLF_STONE_ALTAR =
            new ResourceLocation(WEREWOLVES_MOD_ID, "stone_altar");
    private static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(ForgeRegistries.POI_TYPES, McaVampirismCompat.MOD_ID);
    private static boolean registered;

    private ModSpecialistWorkstations() {
    }

    public static void register(IEventBus modBus) {
        if (registered) {
            return;
        }
        registered = true;
        POI_TYPES.register("vampire_specialist_altar",
                () -> createPoi(ModBlocks.ALTAR_INSPIRATION.get()));
        POI_TYPES.register("hunter_specialist_table",
                () -> createPoi(ModBlocks.HUNTER_TABLE.get()));
        if (isWerewolvesLoaded()) {
            POI_TYPES.register("werewolf_specialist_altar", ModSpecialistWorkstations::createWerewolfPoi);
        }
        POI_TYPES.register(modBus);
    }

    private static PoiType createWerewolfPoi() {
        Block altar = ForgeRegistries.BLOCKS.getValue(WEREWOLF_STONE_ALTAR);
        if (altar == null || altar.defaultBlockState().isAir()) {
            throw new IllegalStateException("Werewolves is loaded without its stone altar block");
        }
        return createPoi(altar);
    }

    private static PoiType createPoi(Block block) {
        Set<BlockState> states = Set.copyOf(block.getStateDefinition().getPossibleStates());
        return new PoiType(states, 1, 1);
    }

    private static boolean isWerewolvesLoaded() {
        ModList modList = ModList.get();
        return modList != null && modList.isLoaded(WEREWOLVES_MOD_ID);
    }
}
