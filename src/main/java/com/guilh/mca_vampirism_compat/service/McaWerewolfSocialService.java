package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.faction.PlayerFaction;
import com.guilh.mca_vampirism_compat.faction.PlayerFactionContext;
import com.guilh.mca_vampirism_compat.social.McaSocialActionType;
import com.guilh.mca_vampirism_compat.util.McaCompatMessages;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.server.level.ServerPlayer;

public final class McaWerewolfSocialService {
    private McaWerewolfSocialService() {
    }

    public static boolean isWerewolfSocialVillager(VillagerEntityMCA villager) {
        return villager != null
                && !McaVampireStateService.isVampire(villager)
                && !McaHunterSocialService.isHunterSocialVillager(villager)
                && McaWerewolfStateService.isWerewolf(villager);
    }

    public static SocialDecision decide(ServerPlayer player, McaSocialActionType actionType) {
        PlayerFactionContext context = McaVampireFactionService.classifyPlayerContext(player);
        if (context.vampireDisguised()) {
            if (McaVampirismCompatConfig.werewolfVillagerDisguisedVampireUsesHumanSocialRules()
                    && (!actionType.isRomanticOrMarriage()
                    || McaVampirismCompatConfig.werewolfVillagerDisguisedVampireCanRomance())) {
                return new SocialDecision(
                        false,
                        null,
                        PlayerFaction.HUMAN_DISGUISED,
                        McaVampirismCompatConfig.werewolfVillagerHumanSocialPenalty(),
                        "DISGUISED_AS_HUMAN",
                        true
                );
            }
            return vampireDecision(actionType);
        }

        if (context.werewolf()) {
            PlayerFaction faction = context.transformedWerewolf() ? PlayerFaction.TRANSFORMED_WEREWOLF : PlayerFaction.WEREWOLF;
            return new SocialDecision(
                    false,
                    null,
                    faction,
                    McaVampirismCompatConfig.werewolfVillagerWerewolfSocialPenalty(),
                    faction.name(),
                    false
            );
        }

        return switch (context.realFaction()) {
            case VAMPIRE -> vampireDecision(actionType);
            case HUNTER -> hunterDecision(actionType);
            case WEREWOLF, TRANSFORMED_WEREWOLF -> new SocialDecision(
                    false,
                    null,
                    context.realFaction(),
                    McaVampirismCompatConfig.werewolfVillagerWerewolfSocialPenalty(),
                    context.realFaction().name(),
                    false
            );
            case HUMAN_DISGUISED, HUMAN, UNKNOWN -> new SocialDecision(
                    false,
                    null,
                    context.realFaction(),
                    McaVampirismCompatConfig.werewolfVillagerHumanSocialPenalty(),
                    "HUMAN",
                    false
            );
        };
    }

    public static double getSocialPenalty(ServerPlayer player) {
        return decide(player, McaSocialActionType.GENERIC_INTERACTION).penalty();
    }

    public static String penaltyMessageKey(PlayerFaction faction) {
        return switch (faction) {
            case VAMPIRE -> McaCompatMessages.WEREWOLF_SOCIAL_PENALTY_VAMPIRE;
            case HUNTER -> McaCompatMessages.WEREWOLF_SOCIAL_PENALTY_HUNTER;
            case WEREWOLF, TRANSFORMED_WEREWOLF, HUMAN_DISGUISED, HUMAN, UNKNOWN -> null;
        };
    }

    private static SocialDecision vampireDecision(McaSocialActionType actionType) {
        boolean blocked = actionType.isRomanticOrMarriage()
                && McaVampirismCompatConfig.werewolfVillagersBlockVampireRomance();
        return new SocialDecision(
                blocked,
                blocked ? vampireBlockMessage(actionType) : null,
                PlayerFaction.VAMPIRE,
                McaVampirismCompatConfig.werewolfVillagerVampireSocialPenalty(),
                blocked ? "VAMPIRE_ROMANCE_BLOCKED" : "VAMPIRE",
                false
        );
    }

    private static SocialDecision hunterDecision(McaSocialActionType actionType) {
        boolean blocked = actionType.isRomanticOrMarriage()
                && McaVampirismCompatConfig.werewolfVillagersBlockHunterRomance();
        return new SocialDecision(
                blocked,
                blocked ? hunterBlockMessage(actionType) : null,
                PlayerFaction.HUNTER,
                McaVampirismCompatConfig.werewolfVillagerHunterSocialPenalty(),
                blocked ? "HUNTER_ROMANCE_BLOCKED" : "HUNTER",
                false
        );
    }

    private static String vampireBlockMessage(McaSocialActionType actionType) {
        return actionType.isMarriageLike()
                ? McaCompatMessages.WEREWOLF_SOCIAL_MARRIAGE_BLOCKED_VAMPIRE
                : McaCompatMessages.WEREWOLF_SOCIAL_BLOCKED_VAMPIRE;
    }

    private static String hunterBlockMessage(McaSocialActionType actionType) {
        return actionType.isMarriageLike()
                ? McaCompatMessages.WEREWOLF_SOCIAL_MARRIAGE_BLOCKED_HUNTER
                : McaCompatMessages.WEREWOLF_SOCIAL_BLOCKED_HUNTER;
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
