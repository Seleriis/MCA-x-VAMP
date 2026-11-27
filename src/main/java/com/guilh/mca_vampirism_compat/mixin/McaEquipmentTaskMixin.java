package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.service.McaDefensiveVillagerService;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.ai.brain.tasks.EquipmentTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MCA owns the visual equipment of its villagers and may clear the dominant
 * hand while refreshing a profession's equipment set. Re-equipping at the
 * tail still produced two network updates (empty, then weapon), which made
 * the weapon visibly flicker. Preserve a persistent real Hunter hand before
 * MCA mutates it instead.
 */
@Mixin(value = EquipmentTask.class, remap = false)
public abstract class McaEquipmentTaskMixin {
    @Inject(
            method = "start(Lnet/minecraft/server/level/ServerLevel;Lnet/conczin/mca/entity/VillagerEntityMCA;J)V",
            at = @At("HEAD")
    )
    private void mcaVampirismCompat$prepareHunterWeaponBeforeEquipmentRefresh(
            ServerLevel level,
            VillagerEntityMCA villager,
            long gameTime,
            CallbackInfo ci
    ) {
        McaDefensiveVillagerService.prepareHunterWeaponForMcaEquipmentTask(villager);
    }

    @Inject(
            method = "equipBestWeapon(Lnet/conczin/mca/entity/VillagerEntityMCA;Lnet/minecraft/world/item/Item;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void mcaVampirismCompat$keepHunterWeaponDuringMcaMeleeEquip(
            VillagerEntityMCA villager,
            Item requestedItem,
            CallbackInfo ci
    ) {
        if (McaDefensiveVillagerService.shouldPreserveHunterWeaponDuringMcaEquipmentTask(villager)) {
            ci.cancel();
        }
    }

    @Inject(
            method = "equipBestRanged(Lnet/conczin/mca/entity/VillagerEntityMCA;Lnet/minecraft/world/item/Item;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void mcaVampirismCompat$keepHunterWeaponDuringMcaRangedEquip(
            VillagerEntityMCA villager,
            Item requestedItem,
            CallbackInfo ci
    ) {
        if (McaDefensiveVillagerService.shouldPreserveHunterWeaponDuringMcaEquipmentTask(villager)) {
            ci.cancel();
        }
    }

    @Redirect(
            method = "start(Lnet/minecraft/server/level/ServerLevel;Lnet/conczin/mca/entity/VillagerEntityMCA;J)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/conczin/mca/entity/VillagerEntityMCA;setItemInHand(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)V"
            )
    )
    private void mcaVampirismCompat$keepHunterHandDuringMcaClear(
            VillagerEntityMCA villager,
            InteractionHand hand,
            ItemStack stack
    ) {
        if (hand == villager.getDominantHand()
                && McaDefensiveVillagerService.shouldPreserveHunterWeaponDuringMcaEquipmentTask(villager)) {
            return;
        }
        villager.setItemInHand(hand, stack);
    }

    @Redirect(
            method = "start(Lnet/minecraft/server/level/ServerLevel;Lnet/conczin/mca/entity/VillagerEntityMCA;J)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/conczin/mca/entity/VillagerEntityMCA;setItemSlot(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;)V"
            )
    )
    private void mcaVampirismCompat$keepHunterSlotDuringMcaClear(
            VillagerEntityMCA villager,
            EquipmentSlot slot,
            ItemStack stack
    ) {
        if (slot == villager.getDominantSlot()
                && McaDefensiveVillagerService.shouldPreserveHunterWeaponDuringMcaEquipmentTask(villager)) {
            return;
        }
        villager.setItemSlot(slot, stack);
    }
}
