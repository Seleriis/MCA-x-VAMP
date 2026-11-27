package com.guilh.mca_vampirism_compat.item;

import com.guilh.mca_vampirism_compat.book.OccultArtsBook;
import net.conczin.mca.item.ExtendedWrittenBookItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class OccultArtsBookItem extends ExtendedWrittenBookItem {
    private static final String MAIN_HAND_MESSAGE =
            "message.mca_vamp_compat.occult_arts.main_hand";

    public OccultArtsBookItem(Item.Properties properties) {
        super(properties, OccultArtsBook.create());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable(MAIN_HAND_MESSAGE), true);
            }
            return InteractionResultHolder.fail(stack);
        }
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
            com.guilh.mca_vampirism_compat.mission.OccultVillageMissionService.openJournalBook(serverPlayer);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
