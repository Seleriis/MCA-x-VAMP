package com.guilh.mca_vampirism_compat;

import com.guilh.mca_vampirism_compat.capability.ModAttachments;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.book.OccultVillageBookDeliveryService;
import com.guilh.mca_vampirism_compat.event.VampiricVillagerEvents;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.gametest.McaVampCompatGameTests;
import com.guilh.mca_vampirism_compat.network.ModNetwork;
import com.guilh.mca_vampirism_compat.registry.ModItems;
import com.guilh.mca_vampirism_compat.trait.McaTraitBridge;
import com.mojang.logging.LogUtils;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

@Mod(McaVampirismCompat.MOD_ID)
public class McaVampirismCompat {
    public static final String MOD_ID = "mca_vamp_compat";
    public static final String LEGACY_DATA_NAMESPACE = "mca_vampirism_compat";
    public static final String VERSION = "2.0.12";
    public static final String BUILD_MARKER = "2.0.12-neoforge-1.21.1";
    public static final Logger LOGGER = LogUtils.getLogger();

    public McaVampirismCompat(IEventBus modBus, ModContainer modContainer) {
        LOGGER.info("[MCA Reborn x Vampirism Compat] MOD LOADED - version {}", VERSION);
        LOGGER.info("[MCA Reborn x Vampirism Compat] BUILD_MARKER = {}", BUILD_MARKER);

        ModAttachments.register(modBus);
        ModItems.register(modBus);
        modBus.addListener(this::registerCapabilities);
        modBus.addListener(McaVampCompatGameTests::register);
        modBus.addListener(ModNetwork::registerPayloads);
        modContainer.registerConfig(
                ModConfig.Type.COMMON,
                McaVampirismCompatConfig.SPEC,
                "mca_vamp_compat-common.toml");
        OccultVillageBookDeliveryService.setEnabledSupplier(
                McaVampirismCompatConfig::giveOccultArtsBookOnSpecialVillageDiscovery
        );
        OccultVillageBookDeliveryService.setBookStackSupplier(
                () -> new ItemStack(ModItems.BOOK_OCCULT_ARTS.get())
        );

        McaTraitBridge.registerTraits();

        NeoForge.EVENT_BUS.register(VampiricVillagerEvents.class);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        ModCapabilities.register(event);
    }

    public static void debugLog(String message, Object... args) {
        if (McaVampirismCompatConfig.enableDebugLogs()) {
            LOGGER.info("[MCA Reborn x Vampirism Compat Debug] " + message, args);
        }
    }

    public static void verboseAiLog(String message, Object... args) {
        if (McaVampirismCompatConfig.enableVerboseAiDebugLogs()) {
            LOGGER.info("[MCA Reborn x Vampirism Compat Debug] " + message, args);
        }
    }
}
