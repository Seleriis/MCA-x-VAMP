package com.guilh.mca_vampirism_compat.compat;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Guards the optional API contracts exercised by the NeoForge bridge. */
class OptionalIntegrationContractTest {
    @Test
    void cycleTradesPacketIsTheSupportedServerBoundary() throws Exception {
        assumeTrue(isClassPresent("de.maxhenkel.easyvillagers.net.MessageCycleTrades"),
                "Easy Villagers is not part of this optional validation profile");
        Class<?> message = Class.forName("de.maxhenkel.easyvillagers.net.MessageCycleTrades", false,
                getClass().getClassLoader());
        assertNotNull(message.getDeclaredMethod("executeServerSide", IPayloadContext.class));

        Class<?> guiEvents = Class.forName("de.maxhenkel.easyvillagers.events.GuiEvents", false,
                getClass().getClassLoader());
        assertNotNull(guiEvents.getDeclaredMethod("onCycleTrades", net.minecraft.server.level.ServerPlayer.class));

        Field cycleKey = Class.forName("de.maxhenkel.easyvillagers.Main", false,
                getClass().getClassLoader()).getDeclaredField("CYCLE_TRADES_KEY");
        assertTrue(cycleKey.getType().getName().equals("net.minecraft.client.KeyMapping"),
                "Easy Villagers keeps client key bindings beside GuiEvents; compat must remain on the packet boundary");
    }

    @Test
    void mcaEvSnapshotBridgeUsesTheDocumentedReadOnlySurface() throws Exception {
        assumeTrue(isClassPresent("com.guilh.mcaevcompat.CompatLogic"),
                "MCA x Easy Villagers Compat is not part of this optional validation profile");
        Class<?> compatLogic = Class.forName("com.guilh.mcaevcompat.CompatLogic", false,
                getClass().getClassLoader());
        assertNotNull(compatLogic.getMethod("copyStoredMcaVillagerData", ItemStack.class));
        assertNotNull(compatLogic.getMethod("getStoredMcaEntityTypeId", ItemStack.class));
    }

    @Test
    void productionMixinConfigurationDoesNotTargetGuiEvents() {
        String config = assertDoesNotThrow(() -> new String(
                getClass().getClassLoader().getResourceAsStream("mca_vamp_compat.mixins.json").readAllBytes()
        ));
        assertTrue(config.contains("EasyVillagersCycleTradesMessageMixin"));
        assertFalse(config.contains("EasyVillagersCycleTradesGuiEventsMixin"));
    }

    private boolean isClassPresent(String className) {
        try {
            Class.forName(className, false, getClass().getClassLoader());
            return true;
        } catch (ClassNotFoundException | LinkageError ignored) {
            return false;
        }
    }
}
