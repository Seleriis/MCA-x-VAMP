package com.guilh.mca_vampirism_compat.easyvillagers;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.advancement.McaAdvancementService;
import com.guilh.mca_vampirism_compat.advancement.McaCompatAdvancements;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.faction.PlayerFaction;
import com.guilh.mca_vampirism_compat.faction.PlayerFactionContext;
import com.guilh.mca_vampirism_compat.service.McaSpecialVillagerClassifier;
import com.guilh.mca_vampirism_compat.service.McaSpecialVillagerType;
import com.guilh.mca_vampirism_compat.service.McaVampireFactionService;
import com.guilh.mca_vampirism_compat.util.McaCompatMessages;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class McaFactionPickupPermissionService {
    private static final long MESSAGE_COOLDOWN_TICKS = 40L;
    private static final int MAX_TRACKED_MESSAGE_COOLDOWNS = 512;
    private static final Map<String, Long> LAST_MESSAGE_TICKS = new HashMap<>();

    private McaFactionPickupPermissionService() {
    }

    public static boolean canPickup(Player player, VillagerEntityMCA villager) {
        return getBlockReason(player, villager) == PickupBlockReason.NONE;
    }

    public static PickupBlockReason getBlockReason(Player player, VillagerEntityMCA villager) {
        if (player == null || villager == null || villager.level().isClientSide) {
            return PickupBlockReason.NONE;
        }
        if (!McaVampirismCompatConfig.enableEasyVillagersFactionRules()
                || !McaVampirismCompatConfig.enableEasyVillagersFactionPickupRules()
                || !McaEvCompatBridge.isDelegatedPickupHandledByMcaEvCompat()) {
            return PickupBlockReason.NONE;
        }

        McaSpecialVillagerType villagerType = McaSpecialVillagerClassifier.classify(villager);
        if (isCuring(villagerType)) {
            return McaVampirismCompatConfig.easyVillagersBlockPickupOfCuringVillagers()
                    ? PickupBlockReason.CURING
                    : PickupBlockReason.NONE;
        }
        if (villagerType == McaSpecialVillagerType.HUMAN_INFECTED
                && !McaVampirismCompatConfig.allowPickupOfInfectedHumanVillagers()) {
            return PickupBlockReason.PROTECTED;
        }

        PlayerFaction playerFaction = pickupPlayerFaction(player);
        PickupVillagerFaction villagerFaction = pickupVillagerFaction(villagerType);
        return isPickupAllowed(playerFaction, villagerFaction)
                ? PickupBlockReason.NONE
                : PickupBlockReason.ENEMY_FACTION;
    }

    public static boolean denyPickupIfNeeded(Villager villager, Player player) {
        return denyPickupIfNeeded(villager, player, "UNKNOWN");
    }

    public static boolean denyPickupIfNeeded(Villager villager, Player player, String source) {
        if (!(villager instanceof VillagerEntityMCA mcaVillager)) {
            return false;
        }
        McaEvCompatBridge.logDelegationStatusOnce();
        PickupBlockReason reason = getBlockReason(player, mcaVillager);
        McaSpecialVillagerType villagerType = McaSpecialVillagerClassifier.classify(mcaVillager);
        PlayerFaction playerFaction = pickupPlayerFaction(player);
        PickupVillagerFaction villagerFaction = pickupVillagerFaction(villagerType);
        McaVampirismCompat.debugLog(
                "[PICKUP-HOOK] {} reached player={} villager={}",
                source,
                player == null ? null : player.getName().getString(),
                mcaVillager.getUUID()
        );
        McaVampirismCompat.debugLog(
                "[PICKUP-HOOK] {} decision allowed={} reason={}",
                source,
                reason == PickupBlockReason.NONE,
                reason
        );
        if (reason == PickupBlockReason.NONE) {
            McaVampirismCompat.debugLog(
                    "[PICKUP] allowed source={} playerFaction={} villagerFaction={}",
                    source,
                    playerFaction,
                    villagerFaction
            );
            return false;
        }
        maybeSendBlockedMessage(player, mcaVillager, reason);
        McaVampirismCompat.debugLog(
                "[PICKUP] denied source={} playerFaction={} villagerFaction={} reason={}",
                source,
                playerFaction,
                villagerFaction,
                reason
        );
        return true;
    }

    /**
     * mca-ev-compat owns MCA snapshot creation. It deliberately leaves the
     * villager in place on a safe failure, so report that result after its
     * method returns. Faction denials are handled at HEAD and never reach here.
     */
    public static void reportAllowedPickupFailureIfNeeded(Villager villager, Player player, String source) {
        if (!(villager instanceof VillagerEntityMCA mcaVillager)
                || player == null
                || player.level().isClientSide
                || !McaVampirismCompatConfig.enableEasyVillagersFactionRules()
                || !McaVampirismCompatConfig.enableEasyVillagersFactionPickupRules()
                || !McaEvCompatBridge.isDelegatedPickupHandledByMcaEvCompat()) {
            return;
        }

        if (mcaVillager.isRemoved()) {
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer
                    && pickupVillagerFaction(McaSpecialVillagerClassifier.classify(mcaVillager)) != PickupVillagerFaction.HUMAN
                    && pickupVillagerFaction(McaSpecialVillagerClassifier.classify(mcaVillager)) != PickupVillagerFaction.UNKNOWN) {
                McaAdvancementService.award(serverPlayer, McaCompatAdvancements.FACTION_PICKUP);
            }
            return;
        }

        PickupFailure failure = pickupFailure(mcaVillager, player);
        maybeSendPickupFailureMessage(player, mcaVillager, failure);
        McaVampirismCompat.debugLog(
                "[PICKUP] allowed capture failed source={} player={} villager={} reason={}",
                source,
                player.getName().getString(),
                mcaVillager.getUUID(),
                failure
        );
    }

    private static boolean isPickupAllowed(PlayerFaction playerFaction, PickupVillagerFaction villagerFaction) {
        if (villagerFaction == PickupVillagerFaction.UNKNOWN) {
            return true;
        }
        if (villagerFaction == PickupVillagerFaction.HUMAN || villagerFaction == PickupVillagerFaction.HUMAN_INFECTED) {
            return true;
        }
        return switch (pickupPlayerFaction(playerFaction)) {
            case HUNTER -> villagerFaction == PickupVillagerFaction.HUNTER;
            case VAMPIRE -> villagerFaction == PickupVillagerFaction.VAMPIRE;
            case WEREWOLF -> villagerFaction == PickupVillagerFaction.WEREWOLF;
            case HUMAN, UNKNOWN, HUMAN_DISGUISED, TRANSFORMED_WEREWOLF -> false;
        };
    }

    private static PlayerFaction pickupPlayerFaction(Player player) {
        if (player == null) {
            return PlayerFaction.UNKNOWN;
        }
        PlayerFactionContext context = McaVampireFactionService.classifyPlayerContext(player);
        return pickupPlayerFaction(context.realFaction());
    }

    private static PlayerFaction pickupPlayerFaction(PlayerFaction faction) {
        return switch (faction) {
            case VAMPIRE -> PlayerFaction.VAMPIRE;
            case HUNTER -> PlayerFaction.HUNTER;
            case WEREWOLF, TRANSFORMED_WEREWOLF -> PlayerFaction.WEREWOLF;
            case HUMAN_DISGUISED, HUMAN -> PlayerFaction.HUMAN;
            case UNKNOWN -> PlayerFaction.UNKNOWN;
        };
    }

    private static PickupVillagerFaction pickupVillagerFaction(McaSpecialVillagerType type) {
        return switch (type) {
            case VAMPIRE -> PickupVillagerFaction.VAMPIRE;
            case HUNTER, DEFENSIVE -> PickupVillagerFaction.HUNTER;
            case WEREWOLF -> PickupVillagerFaction.WEREWOLF;
            case HUMAN_INFECTED -> PickupVillagerFaction.HUMAN_INFECTED;
            case HUMAN_NORMAL, CHILD -> PickupVillagerFaction.HUMAN;
            case CURING_VAMPIRE, CURING_WEREWOLF -> PickupVillagerFaction.CURING;
            case UNKNOWN -> PickupVillagerFaction.UNKNOWN;
        };
    }

    private static boolean isCuring(McaSpecialVillagerType type) {
        return type == McaSpecialVillagerType.CURING_VAMPIRE
                || type == McaSpecialVillagerType.CURING_WEREWOLF;
    }

    private static void maybeSendBlockedMessage(Player player, VillagerEntityMCA villager, PickupBlockReason reason) {
        if (player == null || !McaVampirismCompatConfig.showPickupBlockedMessage()) {
            return;
        }
        long gameTime = villager.level().getGameTime();
        pruneMessageCooldowns(gameTime);
        String key = player.getUUID() + "|" + villager.getUUID() + "|" + reason.name();
        Long lastTick = LAST_MESSAGE_TICKS.get(key);
        if (lastTick != null && gameTime - lastTick < MESSAGE_COOLDOWN_TICKS) {
            return;
        }
        LAST_MESSAGE_TICKS.put(key, gameTime);
        player.displayClientMessage(Component.translatable(messageKey(reason)), true);
    }

    private static PickupFailure pickupFailure(VillagerEntityMCA villager, Player player) {
        if (villager.isSleeping()) {
            return PickupFailure.SLEEPING;
        }
        if (!player.getMainHandItem().isEmpty() && player.getInventory().getFreeSlot() < 0) {
            return PickupFailure.INVENTORY_FULL;
        }
        return PickupFailure.SNAPSHOT;
    }

    private static void maybeSendPickupFailureMessage(Player player, VillagerEntityMCA villager, PickupFailure failure) {
        if (!McaVampirismCompatConfig.showPickupBlockedMessage()) {
            return;
        }
        long gameTime = villager.level().getGameTime();
        pruneMessageCooldowns(gameTime);
        String key = player.getUUID() + "|" + villager.getUUID() + "|" + failure.name();
        Long lastTick = LAST_MESSAGE_TICKS.get(key);
        if (lastTick != null && gameTime - lastTick < MESSAGE_COOLDOWN_TICKS) {
            return;
        }
        LAST_MESSAGE_TICKS.put(key, gameTime);
        player.displayClientMessage(Component.translatable(messageKey(failure)), true);
    }

    private static void pruneMessageCooldowns(long gameTime) {
        if (LAST_MESSAGE_TICKS.size() < MAX_TRACKED_MESSAGE_COOLDOWNS) {
            return;
        }
        LAST_MESSAGE_TICKS.entrySet().removeIf(
                entry -> gameTime - entry.getValue() >= MESSAGE_COOLDOWN_TICKS
        );
        if (LAST_MESSAGE_TICKS.size() >= MAX_TRACKED_MESSAGE_COOLDOWNS) {
            LAST_MESSAGE_TICKS.clear();
        }
    }

    private static String messageKey(PickupBlockReason reason) {
        return switch (reason) {
            case CURING -> McaCompatMessages.PICKUP_BLOCKED_CURING;
            case ENEMY_FACTION -> McaCompatMessages.PICKUP_BLOCKED_ENEMY_FACTION;
            case PROTECTED -> McaCompatMessages.PICKUP_BLOCKED_PROTECTED;
            case NONE -> McaCompatMessages.PICKUP_BLOCKED_PROTECTED;
        };
    }

    private static String messageKey(PickupFailure failure) {
        return switch (failure) {
            case INVENTORY_FULL -> McaCompatMessages.PICKUP_FAILED_INVENTORY_FULL;
            case SLEEPING -> McaCompatMessages.PICKUP_FAILED_SLEEPING;
            case SNAPSHOT -> McaCompatMessages.PICKUP_FAILED_SNAPSHOT;
        };
    }

    public enum PickupBlockReason {
        NONE,
        CURING,
        ENEMY_FACTION,
        PROTECTED
    }

    private enum PickupFailure {
        INVENTORY_FULL,
        SLEEPING,
        SNAPSHOT
    }

    private enum PickupVillagerFaction {
        HUMAN,
        HUMAN_INFECTED,
        VAMPIRE,
        HUNTER,
        WEREWOLF,
        CURING,
        UNKNOWN
    }
}
