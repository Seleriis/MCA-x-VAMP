package com.guilh.mca_vampirism_compat.inheritance;

import com.guilh.mca_vampirism_compat.inheritance.McaVampireInheritanceHandler.InheritedFaction;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.Optional;

public final class McaBabyInheritancePayload {
    public static final String PAYLOAD_KEY = "mca_vamp_compat_baby_inheritance";
    private static final int CURRENT_VERSION = 1;

    private McaBabyInheritancePayload() {
    }

    public static void decideAndWrite(ItemStack stack, Entity parentA, Entity parentB) {
        if (stack == null || stack.isEmpty() || parentA == null || parentB == null) {
            return;
        }
        RandomSource random = parentA.getRandom();
        write(stack, McaVampireInheritanceHandler.decide(parentA, parentB, random));
    }

    public static void decideAndWrite(ItemStack stack, int vampireParents, int werewolfParents, RandomSource random) {
        if (stack == null || stack.isEmpty() || random == null) {
            return;
        }
        write(stack, McaVampireInheritanceHandler.decide(
                vampireParents,
                werewolfParents,
                random.nextDouble(),
                random.nextDouble(),
                random.nextBoolean()
        ));
    }

    public static void write(ItemStack stack, InheritedFaction faction) {
        CompoundTag payload = new CompoundTag();
        payload.putInt("Version", CURRENT_VERSION);
        payload.putString("Faction", faction.name());
        CompoundTag root = customData(stack);
        root.put(PAYLOAD_KEY, payload);
        CustomData.set(DataComponents.CUSTOM_DATA, stack, root);
    }

    public static Optional<InheritedFaction> read(ItemStack stack) {
        CompoundTag root = customData(stack);
        if (!root.contains(PAYLOAD_KEY, Tag.TAG_COMPOUND)) {
            return Optional.empty();
        }
        CompoundTag payload = root.getCompound(PAYLOAD_KEY);
        if (payload.getInt("Version") <= 0) {
            return Optional.empty();
        }
        try {
            return Optional.of(InheritedFaction.valueOf(payload.getString("Faction")));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    public static boolean apply(ItemStack stack, VillagerEntityMCA child) {
        Optional<InheritedFaction> decision = read(stack);
        decision.ifPresent(faction -> McaVampireInheritanceHandler.applyDecision(child, faction));
        return decision.isPresent();
    }

    private static CompoundTag customData(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return new CompoundTag();
        }
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }
}
