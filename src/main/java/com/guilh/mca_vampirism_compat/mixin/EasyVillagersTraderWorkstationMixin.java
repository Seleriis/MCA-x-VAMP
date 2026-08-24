package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.easyvillagers.readonly.StoredMcaFactionResolver;
import com.guilh.mca_vampirism_compat.easyvillagers.readonly.StoredMcaVillagerInfo;
import com.guilh.mca_vampirism_compat.service.McaSpecialistProfessionService;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Easy Villagers assigns a Trader's profession from the POI represented by the
 * block held by the player. Gate only the three faction-specialist stations
 * before its native code consumes the workstation item.
 */
@Pseudo
@Mixin(targets = "de.maxhenkel.easyvillagers.blocks.TraderBlockBase", priority = 2000, remap = false)
public abstract class EasyVillagersTraderWorkstationMixin {
    @Inject(
            method = "useItemOn(Lnet/minecraft/world/item/ItemStack;"
                    + "Lnet/minecraft/world/level/block/state/BlockState;"
                    + "Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;"
                    + "Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;"
                    + "Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/ItemInteractionResult;",
            at = @At("HEAD"),
            cancellable = true,
            require = 1,
            remap = false
    )
    private void mcaVampirismCompat$rejectWrongFactionSpecialistWorkstation(
            ItemStack heldStack,
            BlockState state,
            Level level,
            BlockPos position,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult,
            CallbackInfoReturnable<ItemInteractionResult> cir
    ) {
        if (!(heldStack.getItem() instanceof BlockItem blockItem)) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(position);
        if (!StoredMcaFactionResolver.isEasyVillagersDirectTrader(blockEntity)) {
            return;
        }
        // Easy Villagers keeps stored offers server-only. Do not let the
        // client predict its workstation insertion for our three stations:
        // it would briefly consume/insert the block before the server could
        // refuse it, and FAIL would permit a BlockItem placement fallback.
        if (level.isClientSide) {
            if (McaSpecialistProfessionService.isSpecialistWorkstation(blockItem.getBlock())) {
                cir.setReturnValue(ItemInteractionResult.SUCCESS);
            }
            return;
        }
        StoredMcaVillagerInfo info = StoredMcaFactionResolver.resolve(blockEntity);
        if (McaSpecialistProfessionService.rejectIncompatibleTraderWorkstation(
                info, blockItem.getBlock(), player
        )) {
            // The block was not consumed. CONSUME only stops Minecraft from
            // falling through to BlockItem.useOn and placing it in the world.
            cir.setReturnValue(ItemInteractionResult.CONSUME);
        }
    }

    @Inject(
            method = "useItemOn(Lnet/minecraft/world/item/ItemStack;"
                    + "Lnet/minecraft/world/level/block/state/BlockState;"
                    + "Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;"
                    + "Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;"
                    + "Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/ItemInteractionResult;",
            at = @At("RETURN"),
            require = 1,
            remap = false
    )
    private void mcaVampirismCompat$awardSuccessfulSpecialistWorkstation(
            ItemStack heldStack,
            BlockState state,
            Level level,
            BlockPos position,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult,
            CallbackInfoReturnable<ItemInteractionResult> cir
    ) {
        if (level.isClientSide || !(heldStack.getItem() instanceof BlockItem blockItem)) {
            return;
        }
        McaSpecialistProfessionService.awardTraderSpecialistIfApplied(
                StoredMcaFactionResolver.resolve(level.getBlockEntity(position)), blockItem.getBlock(), player);
    }
}
