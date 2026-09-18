package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.easyvillagers.McaFactionPickupPermissionService;
import com.guilh.mca_vampirism_compat.easyvillagers.McaEvStatePayload;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.guilh.mcaevcompat.CompatLogic", priority = 2000, remap = false)
public abstract class McaEvCompatCaptureMixin {
    @Inject(method = "captureMcaVillager", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private static void vampirismMcaCompat$denyFactionPickup(Villager villager, Player player, CallbackInfo ci) {
        if (McaFactionPickupPermissionService.denyPickupIfNeeded(villager, player, "McaEvCompat CompatLogic.captureMcaVillager")) {
            ci.cancel();
        }
    }

    @Inject(method = "captureMcaVillager", at = @At("RETURN"), require = 0, remap = false)
    private static void vampirismMcaCompat$reportAllowedCaptureFailure(Villager villager, Player player, CallbackInfo ci) {
        McaFactionPickupPermissionService.reportAllowedPickupFailureIfNeeded(
                villager,
                player,
                "McaEvCompat CompatLogic.captureMcaVillager"
        );
    }

    @Inject(method = "saveMcaVillagerToStack", at = @At("RETURN"), require = 0, remap = false)
    private static void vampirismMcaCompat$saveFactionPayload(ItemStack stack, Villager villager, long gameTime,
                                                               CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            McaEvStatePayload.write(stack, villager);
        }
    }

    @Inject(method = "createMcaVillagerStack(Lnet/minecraft/world/entity/npc/Villager;J)Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN"), require = 0, remap = false)
    private static void vampirismMcaCompat$saveCreatedStackPayload(Villager villager, long gameTime,
                                                                   CallbackInfoReturnable<ItemStack> cir) {
        McaEvStatePayload.write(cir.getReturnValue(), villager);
    }

    @Inject(method = "createStoredMcaVillagerEntity", at = @At("RETURN"), require = 0, remap = false)
    private static void vampirismMcaCompat$restoreFactionPayload(Level level, ItemStack stack,
                                                                 CallbackInfoReturnable<Villager> cir) {
        McaEvStatePayload.restore(stack, cir.getReturnValue());
    }

    /**
     * mca-ev-compat builds the entity used by actual item placement in its
     * private spawnStoredMcaVillager path instead of createStoredMcaVillagerEntity.
     * Restore before it joins the level so MCA's join work sees the correct
     * faction state on its first tick and before a player can open its trades.
     */
    @Redirect(
            method = "spawnStoredMcaVillager(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/BlockPos;FLjava/lang/String;)Z",
            at = @At(
                    value = "INVOKE",
                    // CompatLogic is an already-remapped optional mod class;
                    // remap=false therefore requires its production selector.
                    target = "Lnet/minecraft/server/level/ServerLevel;m_7967_(Lnet/minecraft/world/entity/Entity;)Z"
            ),
            require = 0,
            remap = false
    )
    private static boolean vampirismMcaCompat$restoreFactionPayloadBeforePlacement(
            ServerLevel level,
            Entity entity,
            ServerLevel ignoredLevel,
            ItemStack stack,
            BlockPos ignoredPos,
            float ignoredYaw,
            String ignoredAction
    ) {
        if (entity instanceof Villager villager) {
            McaEvStatePayload.restore(stack, villager);
        }
        return level.addFreshEntity(entity);
    }
}
