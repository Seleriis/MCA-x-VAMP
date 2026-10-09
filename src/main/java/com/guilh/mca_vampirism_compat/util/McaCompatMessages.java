package com.guilh.mca_vampirism_compat.util;

import com.guilh.mca_vampirism_compat.faction.PlayerFaction;

public final class McaCompatMessages {
    public static final String HUMAN_PENALTY = "message.mca_vamp_compat.interaction.human_penalty";
    public static final String WEREWOLF_HOSTILITY = "message.mca_vamp_compat.interaction.werewolf_hostility";
    public static final String HUNTER_HOSTILITY = "message.mca_vamp_compat.interaction.hunter_hostility";
    public static final String VAMPIRE_FRIENDLY = "message.mca_vamp_compat.interaction.vampire_friendly";
    public static final String SOCIAL_BLOCKED_HUNTER = "mca_vamp_compat.social.blocked.hunter";
    public static final String SOCIAL_BLOCKED_WEREWOLF = "mca_vamp_compat.social.blocked.werewolf";
    public static final String SOCIAL_MARRIAGE_BLOCKED_HUNTER = "mca_vamp_compat.social.marriage_blocked.hunter";
    public static final String SOCIAL_MARRIAGE_BLOCKED_WEREWOLF = "mca_vamp_compat.social.marriage_blocked.werewolf";
    public static final String SOCIAL_PENALTY_HUMAN = "mca_vamp_compat.social.penalty.human";
    public static final String SOCIAL_PENALTY_WEREWOLF = "mca_vamp_compat.social.penalty.werewolf";
    public static final String SOCIAL_PENALTY_HUNTER = "mca_vamp_compat.social.penalty.hunter";
    public static final String HUNTER_TRADE_BLOCKED_VAMPIRE = "mca_vamp_compat.trade.hunter_blocked.vampire";
    public static final String HUNTER_TRADE_BLOCKED_WEREWOLF = "mca_vamp_compat.trade.hunter_blocked.werewolf";
    public static final String HUNTER_TRADE_BLOCKED_GENERIC = "mca_vamp_compat.trade.hunter_blocked.generic";
    public static final String HUNTER_SOCIAL_BLOCKED_VAMPIRE = "mca_vamp_compat.social.hunter_blocked.vampire";
    public static final String HUNTER_SOCIAL_BLOCKED_WEREWOLF = "mca_vamp_compat.social.hunter_blocked.werewolf";
    public static final String HUNTER_SOCIAL_MARRIAGE_BLOCKED_VAMPIRE = "mca_vamp_compat.social.hunter_marriage_blocked.vampire";
    public static final String HUNTER_SOCIAL_MARRIAGE_BLOCKED_WEREWOLF = "mca_vamp_compat.social.hunter_marriage_blocked.werewolf";
    public static final String HUNTER_SOCIAL_PENALTY_VAMPIRE = "mca_vamp_compat.social.hunter_penalty.vampire";
    public static final String HUNTER_SOCIAL_PENALTY_WEREWOLF = "mca_vamp_compat.social.hunter_penalty.werewolf";
    public static final String WEREWOLF_TRADE_BLOCKED_VAMPIRE = "mca_vamp_compat.trade.werewolf_blocked.vampire";
    public static final String WEREWOLF_TRADE_BLOCKED_HUNTER = "mca_vamp_compat.trade.werewolf_blocked.hunter";
    public static final String WEREWOLF_TRADE_BLOCKED_GENERIC = "mca_vamp_compat.trade.werewolf_blocked.generic";
    public static final String WEREWOLF_SOCIAL_BLOCKED_VAMPIRE = "mca_vamp_compat.social.werewolf_blocked.vampire";
    public static final String WEREWOLF_SOCIAL_BLOCKED_HUNTER = "mca_vamp_compat.social.werewolf_blocked.hunter";
    public static final String WEREWOLF_SOCIAL_MARRIAGE_BLOCKED_VAMPIRE = "mca_vamp_compat.social.werewolf_marriage_blocked.vampire";
    public static final String WEREWOLF_SOCIAL_MARRIAGE_BLOCKED_HUNTER = "mca_vamp_compat.social.werewolf_marriage_blocked.hunter";
    public static final String WEREWOLF_SOCIAL_PENALTY_VAMPIRE = "mca_vamp_compat.social.werewolf_penalty.vampire";
    public static final String WEREWOLF_SOCIAL_PENALTY_HUNTER = "mca_vamp_compat.social.werewolf_penalty.hunter";
    public static final String CURE_INFECTION_TREATED = "mca_vamp_compat.cure.infection_treated";
    public static final String CURE_MILK_NOT_ENOUGH = "mca_vamp_compat.cure.milk_not_enough";
    public static final String CURE_WEAKNESS_REQUIRED = "mca_vamp_compat.cure.weakness_required";
    public static final String CURE_STARTED = "mca_vamp_compat.cure.started";
    public static final String CURE_COMPLETED = "mca_vamp_compat.cure.completed";
    public static final String CURE_IN_PROGRESS = "mca_vamp_compat.cure.in_progress";
    public static final String WEREWOLF_CURE_STARTED = "mca_vamp_compat.cure.werewolf.started";
    public static final String WEREWOLF_CURE_COMPLETED = "mca_vamp_compat.cure.werewolf.completed";
    public static final String WEREWOLF_CURE_IN_PROGRESS = "mca_vamp_compat.cure.werewolf.in_progress";
    public static final String CURE_GARLIC_RELATION_LOSS = "mca_vamp_compat.cure.garlic_relation_loss";
    public static final String GIFT_REJECTED_VAMPIRE = "mca_vamp_compat.gift.rejected.vampire";
    public static final String GIFT_REJECTED_WEREWOLF = "mca_vamp_compat.gift.rejected.werewolf";
    public static final String PICKUP_BLOCKED_CURING = "mca_vamp_compat.pickup.blocked.curing";
    public static final String PICKUP_BLOCKED_ENEMY_FACTION = "mca_vamp_compat.pickup.blocked.enemy_faction";
    public static final String PICKUP_BLOCKED_PROTECTED = "mca_vamp_compat.pickup.blocked.protected";
    public static final String PICKUP_FAILED_INVENTORY_FULL = "mca_vamp_compat.pickup.failed.inventory_full";
    public static final String PICKUP_FAILED_SLEEPING = "mca_vamp_compat.pickup.failed.sleeping";
    public static final String PICKUP_FAILED_SNAPSHOT = "mca_vamp_compat.pickup.failed.snapshot";
    public static final String COMMAND_BLOCKED_ENEMY_FACTION = "mca_vamp_compat.command.blocked.enemy_faction";
    public static final String COMMAND_BLOCKED_CURING = "mca_vamp_compat.command.blocked.curing";
    public static final String COMMAND_BLOCKED_OWN_FACTION = "mca_vamp_compat.command.blocked.own_faction";
    private McaCompatMessages() {
    }

    public static String forFaction(PlayerFaction faction) {
        return switch (faction) {
            case VAMPIRE -> VAMPIRE_FRIENDLY;
            case HUNTER -> HUNTER_HOSTILITY;
            case TRANSFORMED_WEREWOLF -> WEREWOLF_HOSTILITY;
            case WEREWOLF, HUMAN_DISGUISED, HUMAN, UNKNOWN -> HUMAN_PENALTY;
        };
    }
}
