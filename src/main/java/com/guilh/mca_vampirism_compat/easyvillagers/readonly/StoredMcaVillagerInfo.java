package com.guilh.mca_vampirism_compat.easyvillagers.readonly;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.block.entity.BlockEntity;

public record StoredMcaVillagerInfo(
        boolean resolved,
        boolean isMca,
        boolean isVampire,
        boolean isHunter,
        boolean isWerewolf,
        String vampireSource,
        String hunterSource,
        String werewolfSource,
        String stackSource,
        ResourceLocation entityType,
        CompoundTag villagerNbt,
        ItemStack storedStack,
        Villager villagerEntity,
        MerchantOffers offers,
        BlockEntity blockEntity
) {
    public static StoredMcaVillagerInfo unresolved(BlockEntity blockEntity) {
        return new StoredMcaVillagerInfo(
                false,
                false,
                false,
                false,
                false,
                "NONE",
                "NONE",
                "NONE",
                "NONE",
                null,
                null,
                ItemStack.EMPTY,
                null,
                new MerchantOffers(),
                blockEntity
        );
    }
}
