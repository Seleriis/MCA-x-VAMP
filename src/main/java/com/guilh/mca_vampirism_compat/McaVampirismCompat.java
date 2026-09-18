package com.guilh.mca_vampirism_compat;

import com.guilh.mca_vampirism_compat.book.OccultVillageBookDeliveryService;
import com.guilh.mca_vampirism_compat.event.VampiricVillagerEvents;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.network.ModNetwork;
import com.guilh.mca_vampirism_compat.registry.ModItems;
import com.guilh.mca_vampirism_compat.registry.ModSpecialistWorkstations;
import com.guilh.mca_vampirism_compat.service.McaVillageEffectBoundsService;
import com.guilh.mca_vampirism_compat.trait.McaTraitBridge;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(McaVampirismCompat.MOD_ID)
public class McaVampirismCompat {
    public static final String MOD_ID = "mca_vamp_compat";
    public static final String LEGACY_DATA_NAMESPACE = "mca_vampirism_compat";
    public static final String VERSION = "1.2.3";
    public static final String BUILD_MARKER = "1.2.3-forge-1.20.1-mca-7.6.16+7.6.28-beta.10-vampirism-1.10.15+1.10.16";
    public static final Logger LOGGER = LogUtils.getLogger();

    public McaVampirismCompat() {
        LOGGER.info("[MCA Reborn x Vampirism Compat] MOD LOADED - version {}", VERSION);
        LOGGER.info("[MCA Reborn x Vampirism Compat] BUILD_MARKER = {}", BUILD_MARKER);

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.register(modBus);
        ModSpecialistWorkstations.register(modBus);
        modBus.addListener(this::registerCapabilities);
        modBus.addListener(this::onCommonConfigReloading);
        ModLoadingContext.get().registerConfig(
                ModConfig.Type.COMMON,
                McaVampirismCompatConfig.SPEC,
                "mca_vamp_compat-common.toml");
        ModNetwork.register();
        OccultVillageBookDeliveryService.setEnabledSupplier(
                McaVampirismCompatConfig::giveOccultArtsBookOnSpecialVillageDiscovery
        );
        OccultVillageBookDeliveryService.setBookStackSupplier(
                () -> new ItemStack(ModItems.BOOK_OCCULT_ARTS.get())
        );

        McaTraitBridge.registerTraits();
        com.guilh.mca_vampirism_compat.easyvillagers.McaEvTooltipIntegration.register();

        MinecraftForge.EVENT_BUS.register(VampiricVillagerEvents.class);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(VampiricVillagerState.class);
    }

    private void onCommonConfigReloading(ModConfigEvent.Reloading event) {
        if (event.getConfig().getType() == ModConfig.Type.COMMON) {
            McaVillageEffectBoundsService.refreshTrackedTotems();
        }
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
