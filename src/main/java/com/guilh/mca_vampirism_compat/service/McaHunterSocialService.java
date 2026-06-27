package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.faction.PlayerFaction;
import com.guilh.mca_vampirism_compat.faction.PlayerFactionContext;
import com.guilh.mca_vampirism_compat.social.McaSocialActionType;
import com.guilh.mca_vampirism_compat.util.McaCompatMessages;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.server.level.ServerPlayer;

public final class McaHunterSocialService {
    private McaHunterSocialService() {
    }

    public static boolean isHunterSocialVillager(VillagerEntityMCA villager) {
        return villager != null
                && !McaVampireStateService.isVampire(villager)
                && McaHunterAlignmentService.isMcaHunterVillager(villager);
    }

    public static SocialDecision decide(ServerPlayer player, McaSocialActionType actionType) {
        PlayerFactionContext context = McaVampireFactionService.classifyPlayerContext(player);
        if (context.vampireDisguised()) {
            if (McaVampirismCompatConfig.hunterVillagerDisguisedVampireUsesHumanSocialRules()
                    && (!actionType.isRomanticOrMarriage()
                    || McaVampirismCompatConfig.hunterVillagerDisguisedVampireCanRomance())) {
                return new SocialDecision(
                        false,
                        null,
                        PlayerFaction.HUMAN_DISGUISED,
                        McaVampirismCompatConfig.hunterVillagerHumanSocialPenalty(),
                        "DISGUISED_AS_HUMAN",
                        true
                );
            }
            return vampireDecision(actionType);
        }

        return switch (context.realFaction()) {
            case HUNTER -> new SocialDecision(
                    false,
                    null,
                    PlayerFaction.HUNTER,
                    McaVampirismCompatConfig.hunterVillagerHunterSocialPenalty(),
                    "HUNTER",
                    false
            );
            case VAMPIRE -> vampireDecision(actionType);
            case TRANSFORMED_WEREWOLF -> werewolfDecision(actionType);
            case WEREWOLF, HUMAN_DISGUISED, HUMAN, UNKNOWN -> new SocialDecision(
                    false,
                    null,
                    context.realFaction(),
                    McaVampirismCompatConfig.hunterVillagerHumanSocialPenalty(),
                    "HUMAN",
                    false
            );
        };
    }

    public static double getSocialPenalty(ServerPlayer player) {
        return decide(player, McaSocialActionType.GENERIC_INTERACTION).penalty();
    }

    private static SocialDecision vampireDecision(McaSocialActionType actionType) {
        boolean blocked = actionType.isRomanticOrMarriage()
                && McaVampirismCompatConfig.hunterVillagersBlockVampireRomance();
        return new SocialDecision(
                blocked,
                blocked ? vampireBlockMessage(actionType) : null,
                PlayerFaction.VAMPIRE,
                McaVampirismCompatConfig.hunterVillagerVampireSocialPenalty(),
                blocked ? "VAMPIRE_ROMANCE_BLOCKED" : "VAMPIRE",
                false
        );
    }

    private static SocialDecision werewolfDecision(McaSocialActionType actionType) {
        boolean blocked = actionType.isRomanticOrMarriage()
                && McaVampirismCompatConfig.hunterVillagersBlockTransformedWerewolfRomance();
        return new SocialDecision(
                blocked,
                blocked ? werewolfBlockMessage(actionType) : null,
                PlayerFaction.TRANSFORMED_WEREWOLF,
                McaVampirismCompatConfig.hunterVillagerWerewolfSocialPenalty(),
                blocked ? "TRANSFORMED_WEREWOLF_ROMANCE_BLOCKED" : "TRANSFORMED_WEREWOLF",
                false
        );
    }

    private static String vampireBlockMessage(McaSocialActionType actionType) {
        return actionType.isMarriageLike()
                ? McaCompatMessages.HUNTER_SOCIAL_MARRIAGE_BLOCKED_VAMPIRE
                : McaCompatMessages.HUNTER_SOCIAL_BLOCKED_VAMPIRE;
    }

    private static String werewolfBlockMessage(McaSocialActionType actionType) {
        return actionType.isMarriageLike()
                ? McaCompatMessages.HUNTER_SOCIAL_MARRIAGE_BLOCKED_WEREWOLF
                : McaCompatMessages.HUNTER_SOCIAL_BLOCKED_WEREWOLF;
    }

    public static String penaltyMessageKey(PlayerFaction faction) {
        return switch (faction) {
            case VAMPIRE -> McaCompatMessages.HUNTER_SOCIAL_PENALTY_VAMPIRE;
            case TRANSFORMED_WEREWOLF -> McaCompatMessages.HUNTER_SOCIAL_PENALTY_WEREWOLF;
            case HUNTER, WEREWOLF, HUMAN_DISGUISED, HUMAN, UNKNOWN -> null;
        };
    }

    public record SocialDecision(
            boolean blocked,
            String messageKey,
            PlayerFaction faction,
            double penalty,
            String reason,
            boolean disguisedHumanRules
    ) {
        public SocialDecision withPenalty(double penalty) {
            return new SocialDecision(blocked, messageKey, faction, penalty, reason, disguisedHumanRules);
        }
    }
}
