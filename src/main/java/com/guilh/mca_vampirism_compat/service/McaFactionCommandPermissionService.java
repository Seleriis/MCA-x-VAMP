package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.faction.PlayerFaction;
import com.guilh.mca_vampirism_compat.faction.PlayerFactionContext;
import com.guilh.mca_vampirism_compat.util.McaCompatMessages;
import forge.net.conczin.mca.entity.VillagerEntityMCA;
import forge.net.conczin.mca.entity.ai.Chore;
import forge.net.conczin.mca.entity.ai.MoveState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class McaFactionCommandPermissionService {
    private static final long MESSAGE_COOLDOWN_TICKS = 40L;
    private static final int MAX_TRACKED_MESSAGE_COOLDOWNS = 512;
    private static final Map<String, Long> LAST_MESSAGE_TICKS = new HashMap<>();

    private McaFactionCommandPermissionService() {
    }

    public static boolean denyCommandIfNeeded(Player player, VillagerEntityMCA villager, String command) {
        McaCommandType commandType = classifyCommand(command);
        if (!commandType.isRestricted()) {
            return false;
        }
        CommandBlockReason reason = getBlockReason(player, villager, commandType);
        CommandVillagerFaction villagerFaction = commandVillagerFaction(McaSpecialVillagerClassifier.classify(villager));
        PlayerFaction playerFaction = commandPlayerFaction(player);
        McaVampirismCompat.debugLog(
                "[MCA-COMMAND-PERMISSION] reached command={} playerFaction={} villagerFaction={}",
                commandType,
                playerFaction,
                villagerFaction
        );
        if (reason == CommandBlockReason.NONE) {
            McaVampirismCompat.debugLog(
                    "[MCA-COMMAND-PERMISSION] allowed reason=SAME_OR_ALLOWED_FACTION"
            );
            return false;
        }
        maybeSendBlockedMessage(player, villager, reason);
        McaVampirismCompat.debugLog(
                "[MCA-COMMAND-PERMISSION] denied reason={}",
                reason
        );
        return true;
    }

    public static boolean canCommand(Player player, VillagerEntityMCA villager, McaCommandType commandType) {
        return getBlockReason(player, villager, commandType) == CommandBlockReason.NONE;
    }

    public static CommandBlockReason getBlockReason(Player player, VillagerEntityMCA villager, McaCommandType commandType) {
        if (player == null || villager == null || villager.level().isClientSide
                || !McaVampirismCompatConfig.enableFactionCommandRestrictions()
                || commandType == null || !commandType.isRestricted()) {
            return CommandBlockReason.NONE;
        }
        if (commandType == McaCommandType.UNKNOWN
                && !McaVampirismCompatConfig.blockUnknownMcaCommandsForEnemyFactions()) {
            return CommandBlockReason.NONE;
        }

        McaSpecialVillagerType villagerType = McaSpecialVillagerClassifier.classify(villager);
        CommandVillagerFaction villagerFaction = commandVillagerFaction(villagerType);
        if (villagerFaction == CommandVillagerFaction.CURING) {
            return McaVampirismCompatConfig.allowCommandsForCuringVillagers()
                    ? CommandBlockReason.NONE
                    : CommandBlockReason.CURING;
        }
        if (villagerFaction == CommandVillagerFaction.HUMAN_INFECTED
                && !McaVampirismCompatConfig.allowCommandsForInfectedHumans()) {
            return CommandBlockReason.ENEMY_FACTION;
        }

        PlayerFaction playerFaction = commandPlayerFaction(player);
        return isCommandAllowed(playerFaction, villagerFaction)
                ? CommandBlockReason.NONE
                : CommandBlockReason.ENEMY_FACTION;
    }

    public static McaCommandType classifyCommand(String command) {
        String base = commandBase(command);
        if (base.isEmpty()) {
            return McaCommandType.NON_CONTROL;
        }
        Optional<MoveState> moveState = MoveState.byCommand(base);
        if (moveState.isPresent()) {
            return switch (moveState.get()) {
                case FOLLOW -> McaCommandType.FOLLOW;
                case STAY -> McaCommandType.STAY;
                case MOVE -> McaCommandType.MOVE;
            };
        }
        Optional<Chore> chore = Chore.byCommand(base);
        if (chore.isPresent()) {
            return McaCommandType.WORK;
        }
        return switch (base.toLowerCase(Locale.ENGLISH)) {
            case "guard" -> McaCommandType.GUARD;
            case "sethome", "gohome" -> McaCommandType.HOME;
            case "setworkplace", "stopworking", "stay_in_village" -> McaCommandType.WORK;
            case "pick_up", "ridehorse", "hire_short", "hire_long" -> McaCommandType.OTHER_BEHAVIOR;
            case "trade", "inventory", "gift", "adopt", "procreate", "divorcepapers", "divorceconfirm",
                    "execute", "pardon", "infected", "armor", "profession", "apologize", "location", "slap" ->
                    McaCommandType.NON_CONTROL;
            default -> McaCommandType.UNKNOWN;
        };
    }

    private static boolean isCommandAllowed(PlayerFaction playerFaction, CommandVillagerFaction villagerFaction) {
        if (villagerFaction == CommandVillagerFaction.UNKNOWN) {
            return true;
        }
        if (villagerFaction == CommandVillagerFaction.HUMAN || villagerFaction == CommandVillagerFaction.HUMAN_INFECTED) {
            return playerFaction == PlayerFaction.HUMAN
                    || playerFaction == PlayerFaction.UNKNOWN
                    || McaVampirismCompatConfig.allowSpecialPlayersCommandHumans();
        }
        return switch (playerFaction) {
            case VAMPIRE -> villagerFaction == CommandVillagerFaction.VAMPIRE;
            case HUNTER -> villagerFaction == CommandVillagerFaction.HUNTER;
            case WEREWOLF, TRANSFORMED_WEREWOLF -> villagerFaction == CommandVillagerFaction.WEREWOLF;
            case HUMAN, HUMAN_DISGUISED, UNKNOWN -> false;
        };
    }

    private static PlayerFaction commandPlayerFaction(Player player) {
        if (player == null) {
            return PlayerFaction.UNKNOWN;
        }
        PlayerFactionContext context = McaVampireFactionService.classifyPlayerContext(player);
        return switch (context.realFaction()) {
            case VAMPIRE -> PlayerFaction.VAMPIRE;
            case HUNTER -> PlayerFaction.HUNTER;
            case WEREWOLF, TRANSFORMED_WEREWOLF -> PlayerFaction.WEREWOLF;
            case HUMAN_DISGUISED, HUMAN -> PlayerFaction.HUMAN;
            case UNKNOWN -> PlayerFaction.UNKNOWN;
        };
    }

    private static CommandVillagerFaction commandVillagerFaction(McaSpecialVillagerType type) {
        return switch (type) {
            case VAMPIRE -> CommandVillagerFaction.VAMPIRE;
            case HUNTER, DEFENSIVE -> CommandVillagerFaction.HUNTER;
            case WEREWOLF -> CommandVillagerFaction.WEREWOLF;
            case HUMAN_INFECTED -> CommandVillagerFaction.HUMAN_INFECTED;
            case HUMAN_NORMAL, CHILD -> CommandVillagerFaction.HUMAN;
            case CURING_VAMPIRE, CURING_WEREWOLF -> CommandVillagerFaction.CURING;
            case UNKNOWN -> CommandVillagerFaction.UNKNOWN;
        };
    }

    private static void maybeSendBlockedMessage(Player player, VillagerEntityMCA villager, CommandBlockReason reason) {
        if (player == null || villager == null || !McaVampirismCompatConfig.showCommandBlockedMessage()) {
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

    private static String messageKey(CommandBlockReason reason) {
        return switch (reason) {
            case CURING -> McaCompatMessages.COMMAND_BLOCKED_CURING;
            case ENEMY_FACTION -> McaCompatMessages.COMMAND_BLOCKED_ENEMY_FACTION;
            case OWN_FACTION_REQUIRED -> McaCompatMessages.COMMAND_BLOCKED_OWN_FACTION;
            case NONE -> McaCompatMessages.COMMAND_BLOCKED_OWN_FACTION;
        };
    }

    private static String commandBase(String command) {
        if (command == null) {
            return "";
        }
        String trimmed = command.trim();
        int separator = trimmed.indexOf('.');
        return separator < 0 ? trimmed : trimmed.substring(0, separator);
    }

    public enum McaCommandType {
        FOLLOW(true),
        STAY(true),
        MOVE(true),
        WORK(true),
        HOME(true),
        GUARD(true),
        OTHER_BEHAVIOR(true),
        UNKNOWN(true),
        NON_CONTROL(false);

        private final boolean restricted;

        McaCommandType(boolean restricted) {
            this.restricted = restricted;
        }

        public boolean isRestricted() {
            return restricted;
        }
    }

    public enum CommandBlockReason {
        NONE,
        ENEMY_FACTION,
        CURING,
        OWN_FACTION_REQUIRED
    }

    private enum CommandVillagerFaction {
        HUMAN,
        HUMAN_INFECTED,
        VAMPIRE,
        HUNTER,
        WEREWOLF,
        CURING,
        UNKNOWN
    }
}
