package com.guilh.mca_vampirism_compat.registry;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.item.OccultArtsBookItem;
import net.conczin.mca.registry.ItemsMCA;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, McaVampirismCompat.MOD_ID);
    private static final ResourceKey<CreativeModeTab> MCA_TAB = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            ResourceLocation.fromNamespaceAndPath("mca", "mca_tab")
    );

    public static final DeferredHolder<Item, OccultArtsBookItem> BOOK_OCCULT_ARTS =
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
        event.insertAfter(
                new ItemStack(ItemsMCA.BOOK_CULT_ANCIENT),
                new ItemStack(BOOK_OCCULT_ARTS.get()),
                CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
        );
    }
}
