package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.service.McaDefensiveVillagerService;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MCA x EV's Inventory Viewer keeps visible equipment backed by a real MCA
 * inventory unit. Cancelling only Mob#setItemSlot is too late: the Viewer may
 * already have released that backing. Reject edits at its public container
 * boundary so the reserved Hunter weapon and its backing remain one atomic
 * pair until the Hunter state ends.
 */
@Pseudo
@Mixin(targets = "com.guilh.mcaevcompat.inventory.McaStoredInventory", priority = 2000, remap = false)
public abstract class McaEvCompatHunterInventoryMixin {
    private static final int MCA_INVENTORY_SIZE = 27;
    private static final int MAIN_HAND_SLOT = MCA_INVENTORY_SIZE + 4;
    private static final int OFF_HAND_SLOT = MCA_INVENTORY_SIZE + 5;
    private static final int NO_BACKING_SLOT = -1;
    private static final EquipmentSlot[] EQUIPMENT = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
            EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND
    };

    @Shadow @Final private VillagerEntityMCA villager;
    @Shadow @Final private int[] equipmentBackings;
    @Shadow public abstract void setChanged();

    /**
     * MCA x EV represents equipped gear twice while the Viewer is open: once
     * in the visible equipment slot and once as its one-item inventory
     * backing. The latter is an accounting token, not a second player item.
     * Hiding it at the Container read boundary prevents click/drag paths from
     * first copying it to the cursor and only then reaching our mutation
     * guards. The backing itself stays in the MCA inventory for MCA x EV's
     * serializer and is reconciled by the original implementation.
     */
    @Inject(method = "getItem", at = @At("HEAD"), cancellable = true, require = 0)
    private void mcaVampirismCompat$hideReservedHunterWeaponBacking(
            int slot,
            CallbackInfoReturnable<ItemStack> cir
    ) {
        if (isReservedBackingSlot(slot)) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    @Inject(method = "canSetEquipment", at = @At("HEAD"), cancellable = true, require = 0)
    private void mcaVampirismCompat$makeReservedHunterWeaponReadOnly(
            int containerSlot,
            ItemStack stack,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (isReservedEquipmentSlot(containerSlot)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "setItem", at = @At("HEAD"), cancellable = true, require = 0)
    private void mcaVampirismCompat$preventReservedHunterWeaponReplacement(
            int slot,
            ItemStack stack,
            CallbackInfo ci
    ) {
        if (isReservedViewerSlot(slot)) {
            ci.cancel();
        }
    }

    /**
     * Inventory Viewer calls this private implementation directly after it
     * has chosen an equipment slot. Intercept here as well as at setItem so a
     * future menu or item-handler path cannot release the backing first.
     */
    @Inject(method = "setEquipment", at = @At("HEAD"), cancellable = true, require = 0)
    private void mcaVampirismCompat$preventReservedHunterWeaponBackingRelease(
            int equipmentIndex,
            ItemStack requested,
            CallbackInfo ci
    ) {
        if (isReservedEquipmentIndex(equipmentIndex)) {
            ci.cancel();
        }
    }

    @Inject(method = "removeItem", at = @At("HEAD"), cancellable = true, require = 0)
    private void mcaVampirismCompat$preventReservedHunterWeaponRemoval(
            int slot,
            int amount,
            CallbackInfoReturnable<ItemStack> cir
    ) {
        if (isReservedViewerSlot(slot)) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    @Inject(method = "removeItemNoUpdate", at = @At("HEAD"), cancellable = true, require = 0)
    private void mcaVampirismCompat$preventReservedHunterWeaponNoUpdateRemoval(
            int slot,
            CallbackInfoReturnable<ItemStack> cir
    ) {
        if (isReservedViewerSlot(slot)) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    /**
     * Keep only the reserved equipment/backing pair while honoring a real
     * Container#clearContent request for all other Viewer contents. This is
     * deliberately selective: normal cleanup behavior stays intact and the
     * subsequent setChanged reconciliation still sees one valid backing unit.
     */
    @Inject(method = "clearContent", at = @At("HEAD"), cancellable = true, require = 0)
    private void mcaVampirismCompat$preserveReservedHunterWeaponDuringClear(CallbackInfo ci) {
        int reservedIndex = reservedEquipmentIndex();
        if (reservedIndex < 0) {
            return;
        }
        McaDefensiveVillagerService.reconcileHunterWeapon(villager);
        reservedIndex = reservedEquipmentIndex();
        if (reservedIndex < 0) {
            return;
        }
        ItemStack weapon = villager.getItemBySlot(EQUIPMENT[reservedIndex]).copyWithCount(1);
        int backing = equipmentBackings[reservedIndex];
        for (int equipmentIndex = 0; equipmentIndex < EQUIPMENT.length; equipmentIndex++) {
            if (equipmentIndex == reservedIndex) {
                continue;
            }
            equipmentBackings[equipmentIndex] = NO_BACKING_SLOT;
            villager.setItemSlot(EQUIPMENT[equipmentIndex], ItemStack.EMPTY);
        }
        villager.getInventory().clearContent();
        if (!weapon.isEmpty()) {
            int safeBacking = backing >= 0 && backing < MCA_INVENTORY_SIZE ? backing : 0;
            villager.getInventory().setItem(safeBacking, weapon.copy());
            equipmentBackings[reservedIndex] = safeBacking;
        } else {
            equipmentBackings[reservedIndex] = NO_BACKING_SLOT;
        }
        setChanged();
        ci.cancel();
    }

    private boolean isReservedEquipmentSlot(int containerSlot) {
        if (containerSlot == MAIN_HAND_SLOT) {
            return McaDefensiveVillagerService.isPersistentHunterWeaponReserved(villager, EquipmentSlot.MAINHAND);
        }
        if (containerSlot == OFF_HAND_SLOT) {
            return McaDefensiveVillagerService.isPersistentHunterWeaponReserved(villager, EquipmentSlot.OFFHAND);
        }
        return false;
    }

    private boolean isReservedViewerSlot(int containerSlot) {
        return isReservedEquipmentSlot(containerSlot) || isReservedBackingSlot(containerSlot);
    }

    private boolean isReservedBackingSlot(int containerSlot) {
        if (containerSlot < 0 || containerSlot >= MCA_INVENTORY_SIZE) {
            return false;
        }
        int reservedIndex = reservedEquipmentIndex();
        return reservedIndex >= 0 && equipmentBackings[reservedIndex] == containerSlot;
    }

    private boolean isReservedEquipmentIndex(int equipmentIndex) {
        return equipmentIndex >= 0
                && equipmentIndex < EQUIPMENT.length
                && McaDefensiveVillagerService.isPersistentHunterWeaponReserved(
                villager,
                EQUIPMENT[equipmentIndex]
        );
    }

    private int reservedEquipmentIndex() {
        if (isReservedEquipmentIndex(4)) {
            return 4;
        }
        return isReservedEquipmentIndex(5) ? 5 : -1;
    }
}
