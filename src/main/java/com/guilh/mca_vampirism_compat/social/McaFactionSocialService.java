package com.guilh.mca_vampirism_compat.social;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.faction.PlayerFaction;
import com.guilh.mca_vampirism_compat.faction.PlayerFactionContext;
import com.guilh.mca_vampirism_compat.service.McaHunterSocialService;
import com.guilh.mca_vampirism_compat.service.McaVampireCureService;
import com.guilh.mca_vampirism_compat.service.McaVampireFactionService;
import com.guilh.mca_vampirism_compat.service.McaVampireStateService;
import com.guilh.mca_vampirism_compat.service.McaWerewolfSocialService;
import com.guilh.mca_vampirism_compat.util.McaCompatMessages;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class McaFactionSocialService {
    private static final long SOCIAL_MESSAGE_COOLDOWN_TICKS = 60L;
    private static final Map<SocialMessageKey, Long> SOCIAL_MESSAGE_COOLDOWNS = new HashMap<>();

    private McaFactionSocialService() {
    }

    public static boolean handleDialogueAction(ServerPlayer player, VillagerEntityMCA villager, String question, String answer) {
        McaSocialActionType actionType = actionTypeFromDialogue(question, answer);
        if (actionType == McaSocialActionType.UNKNOWN && isMcaVampireVillager(villager)) {
            logUnknownDialogue(player, villager, question, answer);
        }
        return handleSocialAction(player, villager, actionType);
    }

    public static boolean handleCommandAction(ServerPlayer player, VillagerEntityMCA villager, String command) {
        McaSocialActionType actionType = actionTypeFromCommand(player, command);
        if (actionType == McaSocialActionType.UNKNOWN && isMcaVampireVillager(villager)) {
            logUnknownCommand(player, villager, command);
        }
        return handleSocialAction(player, villager, actionType);
    }

    public static boolean canPerformSocialAction(ServerPlayer player, VillagerEntityMCA villager, McaSocialActionType actionType) {
        return blockMessageKey(player, villager, actionType) == null;
    }

    public static double getSocialPenalty(ServerPlayer player, VillagerEntityMCA villager) {
        if (McaHunterSocialService.isHunterSocialVillager(villager)) {
            return McaHunterSocialService.getSocialPenalty(player);
        }
        if (McaWerewolfSocialService.isWerewolfSocialVillager(villager)) {
            return McaWerewolfSocialService.getSocialPenalty(player);
        }
        if (!isMcaVampireVillager(villager)) {
            return 0.0D;
        }
        PlayerFactionContext context = McaVampireFactionService.classifyPlayerContext(player);
        PlayerFaction faction = socialFaction(context);
        double penalty = switch (faction) {
            case VAMPIRE -> 0.0D;
            case HUNTER -> McaVampirismCompatConfig.hunterSocialPenalty();
            case TRANSFORMED_WEREWOLF -> McaVampirismCompatConfig.werewolfSocialPenalty();
            case WEREWOLF, HUMAN_DISGUISED, HUMAN, UNKNOWN -> McaVampirismCompatConfig.humanSocialPenalty();
        };
        if (context.vampireDisguised() && faction == PlayerFaction.HUMAN_DISGUISED) {
            McaVampirismCompat.debugLog(
                    "[SOCIAL] disguise active player={} effectiveFaction={} penalty=humanSocialPenalty",
                    player.getName().getString(),
                    faction
            );
        }
        return penalty;
    }

    public static boolean shouldBlockMarriage(ServerPlayer player, VillagerEntityMCA villager) {
        return !canPerformSocialAction(player, villager, McaSocialActionType.MARRIAGE);
    }

    public static boolean shouldBlockRomance(ServerPlayer player, VillagerEntityMCA villager) {
        return !canPerformSocialAction(player, villager, McaSocialActionType.ROMANCE);
    }

    public static boolean applySocialPenaltyIfSafe(ServerPlayer player, VillagerEntityMCA villager, McaSocialActionType actionType) {
        if (!actionType.isPositiveSocialAction()) {
            return false;
        }
        if (McaHunterSocialService.isHunterSocialVillager(villager)) {
            McaHunterSocialService.SocialDecision decision = McaHunterSocialService.decide(player, actionType);
            if (decision.penalty() <= 0.0D) {
                return false;
            }
            boolean failed = player.getRandom().nextDouble() < decision.penalty();
            if (failed) {
                String messageKey = McaHunterSocialService.penaltyMessageKey(decision.faction());
                if (messageKey != null) {
                    sendSocialMessage(player, villager, actionType, messageKey);
                }
            }
            return failed;
        }
        if (McaWerewolfSocialService.isWerewolfSocialVillager(villager)) {
            McaWerewolfSocialService.SocialDecision decision = McaWerewolfSocialService.decide(player, actionType);
            if (decision.penalty() <= 0.0D) {
                return false;
            }
            boolean failed = player.getRandom().nextDouble() < decision.penalty();
            if (failed) {
                String messageKey = McaWerewolfSocialService.penaltyMessageKey(decision.faction());
                if (messageKey != null) {
                    sendSocialMessage(player, villager, actionType, messageKey);
                }
            }
            return failed;
        }
        if (!isMcaVampireVillager(villager)) {
            return false;
        }
        double penalty = getSocialPenalty(player, villager);
        if (penalty <= 0.0D) {
            return false;
        }
        boolean failed = player.getRandom().nextDouble() < penalty;
        if (failed) {
            sendSocialMessage(player, villager, actionType, penaltyMessageKey(player));
        }
        return failed;
    }

    private static boolean handleSocialAction(ServerPlayer player, VillagerEntityMCA villager, McaSocialActionType actionType) {
        if (McaVampireCureService.shouldBlockSocialAction(villager, actionType)) {
            sendSocialMessage(player, villager, actionType, McaCompatMessages.CURE_IN_PROGRESS);
            McaVampirismCompat.debugLog(
                    "[CURE] social blocked villager={} action={} reason=CURING",
                    villager.getUUID(),
                    actionType
            );
            return true;
        }
        PlayerFactionContext context = McaVampireFactionService.classifyPlayerContext(player);
        McaVampirismCompat.debugLog(
                "[SOCIAL] decision playerWerewolfTransformed={} action={} realFaction={} effectiveFaction={}",
                context.transformedWerewolf(),
                actionType,
                context.realFaction(),
                context.effectiveFaction()
        );
        if (McaHunterSocialService.isHunterSocialVillager(villager)) {
            return handleHunterSocialAction(player, villager, actionType);
        }
        if (McaWerewolfSocialService.isWerewolfSocialVillager(villager)) {
            return handleWerewolfSocialAction(player, villager, actionType);
        }
        if (!isMcaVampireVillager(villager)) {
            return false;
        }
        PlayerFaction faction = socialFaction(context);
        String blockMessage = blockMessageKey(player, villager, actionType);
        if (blockMessage != null) {
            sendSocialMessage(player, villager, actionType, blockMessage);
            logSocial(actionType, player, villager, faction, getSocialPenalty(player, villager), "BLOCKED", blockReason(faction));
            return true;
        }
        double penalty = actionType.isPositiveSocialAction() ? getSocialPenalty(player, villager) : 0.0D;
        if (applySocialPenaltyIfSafe(player, villager, actionType)) {
            logSocial(actionType, player, villager, faction, penalty, "PENALTY_FAILED", "SOCIAL_PENALTY");
            return true;
        }
        logSocial(actionType, player, villager, faction, penalty, "ALLOWED", "OK");
        return false;
    }

    private static String blockMessageKey(ServerPlayer player, VillagerEntityMCA villager, McaSocialActionType actionType) {
        if (McaHunterSocialService.isHunterSocialVillager(villager)) {
            McaHunterSocialService.SocialDecision decision = McaHunterSocialService.decide(player, actionType);
            return decision.blocked() ? decision.messageKey() : null;
        }
        if (McaWerewolfSocialService.isWerewolfSocialVillager(villager)) {
            McaWerewolfSocialService.SocialDecision decision = McaWerewolfSocialService.decide(player, actionType);
            return decision.blocked() ? decision.messageKey() : null;
        }
        if (!isMcaVampireVillager(villager) || !actionType.isStrongPositiveAction()) {
            return null;
        }
        PlayerFaction faction = socialFaction(McaVampireFactionService.classifyPlayerContext(player));
        if (faction == PlayerFaction.HUNTER && McaVampirismCompatConfig.blockHunterInteractions()) {
            return actionType.isMarriageLike()
                    ? McaCompatMessages.SOCIAL_MARRIAGE_BLOCKED_HUNTER
                    : McaCompatMessages.SOCIAL_BLOCKED_HUNTER;
        }
        if (faction == PlayerFaction.TRANSFORMED_WEREWOLF
                && McaVampirismCompatConfig.blockWerewolfInteractions()) {
            return actionType.isMarriageLike()
                    ? McaCompatMessages.SOCIAL_MARRIAGE_BLOCKED_WEREWOLF
                    : McaCompatMessages.SOCIAL_BLOCKED_WEREWOLF;
        }
        return null;
    }

    private static boolean handleHunterSocialAction(ServerPlayer player, VillagerEntityMCA villager, McaSocialActionType actionType) {
        McaHunterSocialService.SocialDecision decision = McaHunterSocialService.decide(player, actionType);
        if (decision.disguisedHumanRules()) {
            McaVampirismCompat.debugLog(
                    "[HUNTER-SOCIAL] disguise active realFaction=VAMPIRE effectiveFaction={} result=ALLOWED",
                    decision.faction()
            );
        }
        if (decision.blocked()) {
            sendSocialMessage(player, villager, actionType, decision.messageKey());
            logHunterSocial(actionType, player, villager, decision, "BLOCKED");
            return true;
        }
        double penalty = actionType.isPositiveSocialAction() ? decision.penalty() : 0.0D;
        if (applySocialPenaltyIfSafe(player, villager, actionType)) {
            logHunterSocial(actionType, player, villager, decision.withPenalty(penalty), "FAILED_BY_PENALTY");
            return true;
        }
        logHunterSocial(actionType, player, villager, decision.withPenalty(penalty), "ALLOWED");
        return false;
    }

    private static boolean handleWerewolfSocialAction(ServerPlayer player, VillagerEntityMCA villager, McaSocialActionType actionType) {
        McaWerewolfSocialService.SocialDecision decision = McaWerewolfSocialService.decide(player, actionType);
        if (decision.disguisedHumanRules()) {
            McaVampirismCompat.debugLog(
                    "[WEREWOLF-SOCIAL] disguise active realFaction=VAMPIRE effectiveFaction={} result=ALLOWED",
                    decision.faction()
            );
        }
        if (decision.blocked()) {
            sendSocialMessage(player, villager, actionType, decision.messageKey());
            logWerewolfSocial(actionType, player, villager, decision, "BLOCKED");
            return true;
        }
        double penalty = actionType.isPositiveSocialAction() ? decision.penalty() : 0.0D;
        if (applySocialPenaltyIfSafe(player, villager, actionType)) {
            logWerewolfSocial(actionType, player, villager, decision.withPenalty(penalty), "FAILED_BY_PENALTY");
            return true;
        }
        logWerewolfSocial(actionType, player, villager, decision.withPenalty(penalty), "ALLOWED");
        return false;
    }

    private static String penaltyMessageKey(ServerPlayer player) {
        PlayerFaction faction = socialFaction(McaVampireFactionService.classifyPlayerContext(player));
        return switch (faction) {
            case HUNTER -> McaCompatMessages.SOCIAL_PENALTY_HUNTER;
            case TRANSFORMED_WEREWOLF -> McaCompatMessages.SOCIAL_PENALTY_WEREWOLF;
            case VAMPIRE -> McaCompatMessages.VAMPIRE_FRIENDLY;
            case WEREWOLF, HUMAN_DISGUISED, HUMAN, UNKNOWN -> McaCompatMessages.SOCIAL_PENALTY_HUMAN;
        };
    }

    private static String blockReason(PlayerFaction faction) {
        return switch (faction) {
            case HUNTER -> "HUNTER";
            case WEREWOLF -> "WEREWOLF_NOT_TRANSFORMED";
            case TRANSFORMED_WEREWOLF -> "TRANSFORMED_WEREWOLF";
            case VAMPIRE, HUMAN_DISGUISED, HUMAN, UNKNOWN -> "NONE";
        };
    }

    private static PlayerFaction socialFaction(PlayerFactionContext context) {
        if (context.vampireDisguised() && !McaVampirismCompatConfig.vampireDisguiseUsesHumanSocialPenalty()) {
            return context.realFaction();
        }
        return context.effectiveFaction();
    }

    private static boolean isMcaVampireVillager(VillagerEntityMCA villager) {
        return villager != null && McaVampireStateService.isVampire(villager);
    }

    private static McaSocialActionType actionTypeFromDialogue(String question, String answer) {
        String raw = raw(question, answer);
        McaSocialActionType actionType = classifyKnownSocialAction(raw);
        if (actionType != McaSocialActionType.UNKNOWN) {
            return actionType;
        }
        return looksLikeFreeformDialogue(question, answer) ? McaSocialActionType.SMALL_TALK : McaSocialActionType.UNKNOWN;
    }

    private static McaSocialActionType classifyKnownSocialAction(String raw) {
        String normalized = normalizeForClassification(raw);
        if (containsAny(normalized, "divorce")) {
            return McaSocialActionType.DIVORCE;
        }
        if (containsAny(normalized, "marry", "married", "marriage", "wedding")) {
            return McaSocialActionType.MARRIAGE;
        }
        if (containsAny(normalized, "engage", "engagement", "promise", "propose", "proposal", "ring")) {
            return McaSocialActionType.PROPOSE;
        }
        if (containsAny(normalized, "flirt", "flirty", "seduce")) {
            return McaSocialActionType.FLIRT;
        }
        if (containsAny(normalized, "kiss")) {
            return McaSocialActionType.KISS;
        }
        if (containsAny(normalized, "romance", "romantic", "hug", "procreate", "intimate")) {
            return McaSocialActionType.ROMANCE;
        }
        if (containsAny(normalized, "gift", "present")) {
            return McaSocialActionType.GIFT;
        }
        if (containsAny(normalized, "joke", "funny", "laugh")) {
            return McaSocialActionType.JOKE;
        }
        if (containsAny(normalized, "compliment", "praise")) {
            return McaSocialActionType.COMPLIMENT;
        }
        if (containsAny(normalized,
                "chat",
                "talk",
                "greet",
                "greeting",
                "hello",
                "how",
                "question",
                "answer",
                "smalltalk",
                "small",
                "rumour",
                "rumor",
                "story",
                "location")) {
            return McaSocialActionType.CHAT;
        }
        return McaSocialActionType.UNKNOWN;
    }

    private static McaSocialActionType actionTypeFromCommand(ServerPlayer player, String command) {
        String normalized = command == null ? "" : command.toLowerCase(Locale.ROOT);
        if ("gift".equals(normalized)) {
            return actionTypeFromHeldGift(player);
        }
        if ("procreate".equals(normalized)) {
            return McaSocialActionType.ROMANCE;
        }
        if (normalized.contains("divorce")) {
            return McaSocialActionType.DIVORCE;
        }
        if ("location".equals(normalized)) {
            return McaSocialActionType.GENERIC_INTERACTION;
        }
        McaSocialActionType actionType = classifyKnownSocialAction(normalized);
        return actionType == McaSocialActionType.UNKNOWN ? McaSocialActionType.UNKNOWN : actionType;
    }

    private static McaSocialActionType actionTypeFromHeldGift(ServerPlayer player) {
        Item item = player.getMainHandItem().getItem();
        String itemClassName = item.getClass().getName().toLowerCase(Locale.ROOT);
        if (itemClassName.endsWith("weddingringitem")) {
            return McaSocialActionType.MARRIAGE;
        }
        if (itemClassName.endsWith("engagementringitem") || itemClassName.endsWith("bouquetitem")) {
            return McaSocialActionType.PROPOSE;
        }
        if (itemClassName.endsWith("matchmakersringitem")) {
            return McaSocialActionType.MARRIAGE;
        }
        return McaSocialActionType.GIFT;
    }

    private static void logSocial(McaSocialActionType actionType, ServerPlayer player, VillagerEntityMCA villager,
                                  PlayerFaction faction, double penalty, String result, String reason) {
        McaVampirismCompat.debugLog(
                "[SOCIAL] action={} player={} faction={} villager={} penalty={} result={} reason={}",
                actionType,
                player.getName().getString(),
                faction,
                villager.getUUID(),
                penalty,
                result,
                reason
        );
    }

    private static void logHunterSocial(McaSocialActionType actionType, ServerPlayer player, VillagerEntityMCA villager,
                                        McaHunterSocialService.SocialDecision decision, String result) {
        McaVampirismCompat.debugLog(
                "[HUNTER-SOCIAL] action={} player={} faction={} villager={} penalty={} result={} reason={}",
                actionType,
                player.getName().getString(),
                decision.faction(),
                villager.getUUID(),
                decision.penalty(),
                result,
                decision.reason()
        );
    }

    private static void logWerewolfSocial(McaSocialActionType actionType, ServerPlayer player, VillagerEntityMCA villager,
                                          McaWerewolfSocialService.SocialDecision decision, String result) {
        McaVampirismCompat.debugLog(
                "[WEREWOLF-SOCIAL] action={} player={} faction={} villager={} penalty={} result={} reason={}",
                actionType,
                player.getName().getString(),
                decision.faction(),
                villager.getUUID(),
                decision.penalty(),
                result,
                decision.reason()
        );
    }

    private static void sendSocialMessage(ServerPlayer player, VillagerEntityMCA villager, McaSocialActionType actionType, String messageKey) {
        if (messageKey == null) {
            return;
        }
        long gameTime = player.level().getGameTime();
        purgeSocialMessageCooldowns(gameTime);
        SocialMessageKey key = new SocialMessageKey(player.getUUID(), villager.getUUID(), actionType, messageKey);
        Long nextAllowed = SOCIAL_MESSAGE_COOLDOWNS.get(key);
        if (nextAllowed != null && nextAllowed > gameTime) {
            return;
        }
        SOCIAL_MESSAGE_COOLDOWNS.put(key, gameTime + SOCIAL_MESSAGE_COOLDOWN_TICKS);
        player.displayClientMessage(Component.translatable(messageKey), true);
    }

    private static void purgeSocialMessageCooldowns(long gameTime) {
        SOCIAL_MESSAGE_COOLDOWNS.entrySet().removeIf(entry -> entry.getValue() <= gameTime);
    }

    private static String raw(String left, String right) {
        return (left == null ? "" : left) + " " + (right == null ? "" : right);
    }

    private static String normalizeForClassification(String raw) {
        return (" " + (raw == null ? "" : raw).toLowerCase(Locale.ROOT) + " ")
                .replace('_', ' ')
                .replace('-', ' ')
                .replace('.', ' ')
                .replace(':', ' ')
                .replace('/', ' ')
                .replace('?', ' ')
                .replace('!', ' ')
                .replace(',', ' ')
                .replace(';', ' ')
                .replace('\'', ' ')
                .replace('"', ' ');
    }

    private static boolean containsAny(String normalized, String... words) {
        for (String word : words) {
            if (normalized.contains(" " + word + " ")) {
                return true;
            }
        }
        return false;
    }

    private static boolean looksLikeFreeformDialogue(String question, String answer) {
        return looksLikeFreeformText(question) || looksLikeFreeformText(answer);
    }

    private static boolean looksLikeFreeformText(String raw) {
        if (raw == null) {
            return false;
        }
        String trimmed = raw.trim();
        return !trimmed.isEmpty()
                && (trimmed.contains(" ")
                || trimmed.contains("?")
                || trimmed.contains("!")
                || trimmed.contains("'")
                || trimmed.contains("*"));
    }

    private static void logUnknownDialogue(ServerPlayer player, VillagerEntityMCA villager, String question, String answer) {
        PlayerFaction faction = McaVampireFactionService.classifyPlayer(player);
        String raw = raw(question, answer);
        McaVampirismCompat.debugLog(
                "[SOCIAL-UNKNOWN] source=DIALOGUE questionId={} answerId={} questionKey={} answerKey={} dialogueId={} interactionId={} raw={} player={} faction={} villager={}",
                question,
                answer,
                question,
                answer,
                question,
                answer,
                raw,
                player.getName().getString(),
                faction,
                villager.getUUID()
        );
    }

    private static void logUnknownCommand(ServerPlayer player, VillagerEntityMCA villager, String command) {
        PlayerFaction faction = McaVampireFactionService.classifyPlayer(player);
        McaVampirismCompat.debugLog(
                "[SOCIAL-UNKNOWN] source=COMMAND command={} raw={} player={} faction={} villager={}",
                command,
                command,
                player.getName().getString(),
                faction,
                villager.getUUID()
        );
    }

    private record SocialMessageKey(UUID playerUuid, UUID villagerUuid, McaSocialActionType actionType, String messageKey) {
    }
}
