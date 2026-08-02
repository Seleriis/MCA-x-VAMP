package com.guilh.mca_vampirism_compat.easyvillagers;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.easyvillagers.readonly.StoredMcaFactionResolver;
import com.guilh.mca_vampirism_compat.easyvillagers.readonly.StoredMcaVillagerInfo;
import com.guilh.mca_vampirism_compat.faction.PlayerFaction;
import com.guilh.mca_vampirism_compat.service.McaHunterTradeService;
import com.guilh.mca_vampirism_compat.service.McaTradeContextService;
import com.guilh.mca_vampirism_compat.service.McaVampireFactionService;
import com.guilh.mca_vampirism_compat.trade.McaFactionTradeService;
import com.guilh.mca_vampirism_compat.service.McaWerewolfTradeService;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class EasyVillagersBlockTradeService {
    private EasyVillagersBlockTradeService() {
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        Player player = event.getEntity();
        BlockPos blockPos = event.getPos();
        BlockEntity blockEntity = event.getLevel().getBlockEntity(blockPos);
        McaEvCompatBridge.logDelegationStatusOnce();
        if (!McaEvCompatBridge.canApplyFactionRulesToStoredVillager()) {
            return;
        }
        if (!StoredMcaFactionResolver.isEasyVillagersDirectTrader(blockEntity)) {
            return;
        }

        ItemStack heldStack = player.getItemInHand(event.getHand());
        if (shouldLetEasyVillagersHandleHeldItem(heldStack)) {
            McaVampirismCompat.debugLog(
                    "[EASY-VILLAGERS] block interaction passed through for held item item={} player={} pos={}",
                    heldStack.getItem(),
                    player.getName().getString(),
                    blockPos
            );
            return;
        }

        if (player.isShiftKeyDown() || player.isCrouching()) {
            McaVampirismCompat.debugLog(
                    "[EASY-VILLAGERS] block pickup/removal ignored because player is sneaking player={} block={} pos={}",
                    player.getName().getString(),
                    blockEntity.getClass().getName(),
                    blockPos
            );
            return;
        }
        McaVampirismCompat.debugLog(
                "[EASY-VILLAGERS] block trade attempt block={} pos={} player={}",
                blockEntity.getClass().getName(),
                blockPos,
                player.getName().getString()
        );

        StoredMcaVillagerInfo info = StoredMcaFactionResolver.resolve(blockEntity);
        if (!info.resolved()) {
            McaVampirismCompat.debugLog("[EASY-VILLAGERS] skipped reason=UNRESOLVED_STORED_VILLAGER");
            return;
        }

        McaVampirismCompat.debugLog(
                "[EASY-VILLAGERS] stored villager faction resolved readOnly=true stackSource={} type={} isMca={} isVampire={} isHunter={} isWerewolf={} vampireSource={} hunterSource={} werewolfSource={}",
                info.stackSource(),
                info.entityType(),
                info.isMca(),
                info.isVampire(),
                info.isHunter(),
                info.isWerewolf(),
                info.vampireSource(),
                info.hunterSource(),
                info.werewolfSource()
        );

        if ((!info.isMca() && !info.isHunter() && !info.isWerewolf())
                || (!info.isVampire() && !info.isHunter() && !info.isWerewolf())) {
            return;
        }
        boolean hunterTrade = !info.isVampire() && info.isHunter();
        boolean werewolfTrade = !info.isVampire() && !hunterTrade && info.isWerewolf();
        PlayerFaction faction = hunterTrade
                ? McaHunterTradeService.tradeFaction(player)
                : werewolfTrade
                ? McaWerewolfTradeService.tradeFaction(player)
                : McaVampireFactionService.tradeFaction(player);
        McaVampireFactionService.debugClassification(player, faction, "EASY_VILLAGERS_BLOCK_TRADE");
        if (shouldBlockTrade(player, faction, hunterTrade, werewolfTrade)) {
            player.displayClientMessage(Component.translatable(messageFor(player, faction, hunterTrade, werewolfTrade)), true);
            logBlockedTrade(player, faction, hunterTrade, werewolfTrade);
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        if (info.offers().isEmpty()) {
            McaVampirismCompat.debugLog(
                    "[EASY-VILLAGERS] wrapper skipped reason=NO_OFFERS player={} pos={}",
                    player.getName().getString(),
                    blockPos
            );
            return;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        McaTradeContextService.markEasyVillagersBlockTrade(serverPlayer, blockPos);
        EasyVillagersFactionMerchantSession session =
                hunterTrade
                        ? EasyVillagersFactionMerchantSession.createForHunter(serverPlayer, info, faction)
                        : werewolfTrade
                        ? EasyVillagersFactionMerchantSession.createForWerewolf(serverPlayer, info, faction)
                        : EasyVillagersFactionMerchantSession.create(serverPlayer, info, faction);
        new EasyVillagersFactionMerchant(session).open();
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    public static void closeSession(Player player) {
        if (player != null) {
            EasyVillagersFactionMerchantSession.closeForPlayer(player.getUUID(), "player logout");
        }
    }

    /**
     * Authoritative entry point used by the Easy Villagers tile entity mixin.
     * Returning true means the original Easy Villagers GUI must not be opened.
     */
    public static boolean tryOpenDirectTrader(BlockEntity blockEntity, Player player) {
        if (blockEntity == null || player == null || player.level().isClientSide
                || !McaEvCompatBridge.canApplyFactionRulesToStoredVillager()
                || !StoredMcaFactionResolver.isEasyVillagersDirectTrader(blockEntity)) {
            return false;
        }
        StoredMcaVillagerInfo info = StoredMcaFactionResolver.resolve(blockEntity);
        if (!info.resolved() || (!info.isVampire() && !info.isHunter() && !info.isWerewolf())) {
            return false;
        }
        McaVampirismCompat.debugLog(
                "[EASY-VILLAGERS] direct trader faction stackSource={} isMca={} vampire={} hunter={} werewolf={} vampireSource={} hunterSource={} werewolfSource={}",
                info.stackSource(),
                info.isMca(),
                info.isVampire(),
                info.isHunter(),
                info.isWerewolf(),
                info.vampireSource(),
                info.hunterSource(),
                info.werewolfSource()
        );

        boolean hunterTrade = !info.isVampire() && info.isHunter();
        boolean werewolfTrade = !info.isVampire() && !hunterTrade && info.isWerewolf();
        PlayerFaction faction = hunterTrade
                ? McaHunterTradeService.tradeFaction(player)
                : werewolfTrade
                ? McaWerewolfTradeService.tradeFaction(player)
                : McaVampireFactionService.tradeFaction(player);
        McaVampireFactionService.debugClassification(player, faction, "EASY_VILLAGERS_DIRECT_TRADER");

        if (shouldBlockTrade(player, faction, hunterTrade, werewolfTrade)) {
            player.displayClientMessage(Component.translatable(
                    messageFor(player, faction, hunterTrade, werewolfTrade)), true);
            logBlockedTrade(player, faction, hunterTrade, werewolfTrade);
            return true;
        }
        if (info.offers().isEmpty()) {
            McaVampirismCompat.debugLog(
                    "[EASY-VILLAGERS] direct wrapper skipped reason=NO_OFFERS player={} pos={} storedFaction={}",
                    player.getName().getString(),
                    blockEntity.getBlockPos(),
                    hunterTrade ? "HUNTER" : werewolfTrade ? "WEREWOLF" : "VAMPIRE"
            );
            return false;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return true;
        }

        McaTradeContextService.markEasyVillagersBlockTrade(serverPlayer, blockEntity.getBlockPos());
        EasyVillagersFactionMerchantSession session = hunterTrade
                ? EasyVillagersFactionMerchantSession.createForHunter(serverPlayer, info, faction)
                : werewolfTrade
                ? EasyVillagersFactionMerchantSession.createForWerewolf(serverPlayer, info, faction)
                : EasyVillagersFactionMerchantSession.create(serverPlayer, info, faction);
        new EasyVillagersFactionMerchant(session).open();
        McaVampirismCompat.debugLog(
                "[EASY-VILLAGERS] original trader GUI replaced player={} pos={} storedFaction={}",
                player.getName().getString(),
                blockEntity.getBlockPos(),
                hunterTrade ? "HUNTER" : werewolfTrade ? "WEREWOLF" : "VAMPIRE"
        );
        return true;
    }

    private static boolean shouldBlockTrade(Player player, PlayerFaction faction, boolean hunterTrade, boolean werewolfTrade) {
        if (hunterTrade) {
            return McaHunterTradeService.shouldBlockTrade(player);
        }
        if (werewolfTrade) {
            return McaWerewolfTradeService.shouldBlockTrade(player);
        }
        if (McaVampirismCompatConfig.vampireVillagersTradeOnlyWithVampires() && faction != PlayerFaction.VAMPIRE) {
            return true;
        }
        if (faction == PlayerFaction.HUNTER) {
            return McaVampirismCompatConfig.blockHunterTrading();
        }
        return faction == PlayerFaction.TRANSFORMED_WEREWOLF
                && McaVampirismCompatConfig.blockWerewolfTrading();
    }

    private static String messageFor(Player player, PlayerFaction faction, boolean hunterTrade, boolean werewolfTrade) {
        if (hunterTrade) {
            return McaHunterTradeService.messageFor(player);
        }
        if (werewolfTrade) {
            return McaWerewolfTradeService.messageFor(player);
        }
        if (McaVampirismCompatConfig.vampireVillagersTradeOnlyWithVampires() && faction != PlayerFaction.VAMPIRE) {
            return McaFactionTradeService.ONLY_VAMPIRES;
        }
        return switch (faction) {
            case HUNTER -> McaFactionTradeService.HUNTER_BLOCKED;
            case TRANSFORMED_WEREWOLF -> McaFactionTradeService.WEREWOLF_BLOCKED;
            case WEREWOLF -> McaFactionTradeService.HUMAN_MARKUP_ACTIVE;
            case VAMPIRE, HUMAN_DISGUISED, HUMAN, UNKNOWN -> McaFactionTradeService.HUMAN_MARKUP_ACTIVE;
        };
    }

    private static void logBlockedTrade(Player player, PlayerFaction faction, boolean hunterTrade, boolean werewolfTrade) {
        if (hunterTrade) {
            McaHunterTradeService.TradeDecision decision = McaHunterTradeService.decide(player);
            McaVampirismCompat.debugLog(
                    "[HUNTER-TRADE] trade blocked faction={} reason=HUNTER_VILLAGER_REFUSES_{}",
                    decision.priceFaction(),
                    decision.reason()
            );
            return;
        }
        if (werewolfTrade) {
            McaWerewolfTradeService.TradeDecision decision = McaWerewolfTradeService.decide(player);
            McaVampirismCompat.debugLog(
                    "[WEREWOLF-TRADE] trade blocked faction={} reason=WEREWOLF_VILLAGER_REFUSES_{}",
                    decision.priceFaction(),
                    decision.reason()
            );
            return;
        }
        McaVampirismCompat.debugLog(
                "[TRADE] blocked reason={}",
                faction
        );
    }

    private static boolean shouldLetEasyVillagersHandleHeldItem(ItemStack heldStack) {
        if (heldStack.isEmpty()) {
            return false;
        }
        if (heldStack.getItem() instanceof BlockItem) {
            return true;
        }
        return heldStack.getItem().getClass().getName().equals("de.maxhenkel.easyvillagers.items.VillagerItem");
    }
}
