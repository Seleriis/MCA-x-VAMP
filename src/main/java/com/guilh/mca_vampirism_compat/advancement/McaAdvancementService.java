package com.guilh.mca_vampirism_compat.advancement;

import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.faction.PlayerFaction;
import com.guilh.mca_vampirism_compat.trade.McaFactionTradePriceOverlay;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/** Awards loaded compat advancements once and safely retains offline credits. */
public final class McaAdvancementService {
    private McaAdvancementService() {
    }

    public static void award(ServerPlayer player, ResourceLocation id) {
        if (player == null || player.serverLevel().isClientSide) {
            return;
        }
        Advancement advancement = player.server.getAdvancements().getAdvancement(id);
        if (advancement != null) {
            player.getAdvancements().award(advancement, McaCompatAdvancements.CRITERION);
        }
    }

    public static void awardOrQueue(MinecraftServer server, UUID playerId, ResourceLocation id) {
        if (server == null || playerId == null || server.getAdvancements().getAdvancement(id) == null) {
            return;
        }
        ServerPlayer player = server.getPlayerList().getPlayer(playerId);
        if (player != null) {
            award(player, id);
            return;
        }
        credits(server).add(playerId, id);
    }

    public static void grantPending(ServerPlayer player) {
        if (player == null) {
            return;
        }
        for (ResourceLocation id : credits(player.server).take(player.getUUID())) {
            award(player, id);
        }
    }

    public static void awardFactionTrade(ServerPlayer player, PlayerFaction playerFaction,
                                         McaFactionTradePriceOverlay.TradeKind kind) {
        if (player == null || playerFaction == null || kind == null) {
            return;
        }
        switch (kind) {
            case VAMPIRE -> {
                if (playerFaction == PlayerFaction.VAMPIRE
                        && McaVampirismCompatConfig.vampirePlayerTradeDiscount() > 0.0D) {
                    award(player, McaCompatAdvancements.VAMPIRE_TRADE);
                }
            }
            case HUNTER -> {
                if (playerFaction == PlayerFaction.HUNTER
                        && McaVampirismCompatConfig.hunterVillagerHunterDiscount() > 0.0D) {
                    award(player, McaCompatAdvancements.HUNTER_TRADE);
                }
            }
            case WEREWOLF -> {
                if ((playerFaction == PlayerFaction.WEREWOLF || playerFaction == PlayerFaction.TRANSFORMED_WEREWOLF)
                        && McaVampirismCompatConfig.werewolfVillagerWerewolfDiscount() > 0.0D) {
                    award(player, McaCompatAdvancements.WEREWOLF_TRADE);
                }
            }
        }
    }

    private static McaAdvancementCreditSavedData credits(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                McaAdvancementCreditSavedData::load,
                McaAdvancementCreditSavedData::new,
                McaAdvancementCreditSavedData.ID
        );
    }
}
