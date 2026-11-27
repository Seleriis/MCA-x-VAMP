package com.guilh.mca_vampirism_compat.registry;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.item.OccultArtsBookItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class ModItems {
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, McaVampirismCompat.MOD_ID);
    private static final ResourceKey<CreativeModeTab> MCA_TAB = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            new ResourceLocation("mca", "mca_tab")
    );

    public static final RegistryObject<OccultArtsBookItem> BOOK_OCCULT_ARTS =
            ITEMS.register(
                    "book_occult_arts",
                    () -> new OccultArtsBookItem(new Item.Properties().stacksTo(1))
            );

    private ModItems() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener(ModItems::addToCreativeTab);
    }

    private static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (!event.getTabKey().equals(MCA_TAB)) {
            return;
        }
        // Forge 1.20.1's tab event exposes the vanilla Output API only;
        // positional insertion was added later.  Accepting the book keeps it
        // discoverable in MCA's tab without relying on a newer registry UI.
        event.accept(BOOK_OCCULT_ARTS);
    }
}
