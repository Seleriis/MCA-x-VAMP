package com.guilh.mca_vampirism_compat.easyvillagers;

import com.guilh.mca_vampirism_compat.inheritance.McaBabyInheritancePayload;
import com.guilh.mca_vampirism_compat.inheritance.McaVampireInheritanceHandler.InheritedFaction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.Bootstrap;
import net.neoforged.fml.loading.LoadingModList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class McaEvStatePayloadTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        if (LoadingModList.get() == null) {
            LoadingModList.of(List.of(), List.of(), List.of(), List.of(), Map.of());
        }
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void explicitPayloadHasPriorityReadyFactionFlags() {
        CompoundTag state = new CompoundTag();
        state.putBoolean("Converted", true);
        CompoundTag payload = new CompoundTag();
        payload.putInt("Version", 2);
        payload.putBoolean("Vampire", true);
        payload.putBoolean("Hunter", false);
        payload.putBoolean("Werewolf", false);
        payload.put("State", state);
        CompoundTag root = new CompoundTag();
        root.put(McaEvStatePayload.PAYLOAD_KEY, payload);
        ItemStack stack = new ItemStack(Items.VILLAGER_SPAWN_EGG);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));

        McaEvStatePayload.FactionSnapshot snapshot = McaEvStatePayload.readFaction(stack).orElseThrow();

        assertEquals(2, snapshot.version());
        assertTrue(snapshot.vampire());
        assertFalse(snapshot.hunter());
        assertEquals(state, McaEvStatePayload.readState(stack).orElseThrow());
    }

    @Test
    void inheritedBabyPayloadIsVisibleToStoredFactionResolver() {
        ItemStack stack = new ItemStack(Items.VILLAGER_SPAWN_EGG);
        McaBabyInheritancePayload.write(stack, InheritedFaction.WEREWOLF);

        McaEvStatePayload.FactionSnapshot snapshot = McaEvStatePayload.readFaction(stack).orElseThrow();

        assertFalse(snapshot.vampire());
        assertFalse(snapshot.hunter());
        assertTrue(snapshot.werewolf());
    }
}
