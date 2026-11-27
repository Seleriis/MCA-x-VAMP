package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.conczin.mca.entity.VillagerEntityMCA;

/** Entry points for player-initiated cure actions. Item validation remains owned by the public cure facade. */
final class McaCureInteractionService {
    private McaCureInteractionService() {
    }

    static boolean handleEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide || !(event.getTarget() instanceof VillagerEntityMCA villager)) {
            return false;
        }
        InteractionResult result = handleDirectInteraction(event.getEntity(), villager, event.getHand());
        if (result == null) {
            return false;
        }
        event.setCancellationResult(result);
        event.setCanceled(true);
        return true;
    }

    static InteractionResult handleDirectInteraction(Player player, VillagerEntityMCA villager, InteractionHand hand) {
        if (player == null || villager == null || villager.level().isClientSide) {
            return null;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty()) {
            return null;
        }
        if (McaVampireCureService.handleHeldCureItem(player, villager, hand, stack)) {
            return InteractionResult.SUCCESS;
        }
        if (McaVampireCureService.shouldBlockTrade(villager)) {
            player.displayClientMessage(Component.translatable(McaVampireCureService.cureInProgressMessage(villager)), true);
            return InteractionResult.SUCCESS;
        }
        return null;
    }

    static boolean handleGiftCommand(ServerPlayer player, VillagerEntityMCA villager) {
        if (player == null || villager == null || villager.level().isClientSide) {
            return false;
        }
        ItemStack stack = player.getMainHandItem();
        return !stack.isEmpty()
                && McaVampireCureService.handleHeldCureItem(player, villager, InteractionHand.MAIN_HAND, stack);
    }
}
