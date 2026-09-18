package com.guilh.mca_vampirism_compat.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class McaVampirismCompatConfig {
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.IntValue PLAYER_SANGUINARE_DURATION_TICKS;
    private static final ModConfigSpec.IntValue VAMPIRE_VILLAGER_SANGUINARE_DURATION_TICKS;
    private static final ModConfigSpec.BooleanValue ALLOW_MCA_VILLAGER_INFECTION;
    private static final ModConfigSpec.BooleanValue ALLOW_CHILD_INFECTION;

    private static final ModConfigSpec.IntValue BITE_COOLDOWN_TICKS;
    private static final ModConfigSpec.DoubleValue NIGHT_ATTACK_CHANCE;
    private static final ModConfigSpec.DoubleValue CONVERSION_CHANCE_ON_BITE;
    private static final ModConfigSpec.BooleanValue VAMPIRE_VILLAGERS_AVOID_SUN;
    private static final ModConfigSpec.BooleanValue VAMPIRE_VILLAGERS_BURN_IN_SUN;
    private static final ModConfigSpec.BooleanValue VAMPIRE_VILLAGERS_USE_NIGHT_SCHEDULE;
    private static final ModConfigSpec.BooleanValue NON_LETHAL_BITE_AGAINST_VILLAGERS;
    private static final ModConfigSpec.DoubleValue VILLAGER_BITE_DAMAGE;
    private static final ModConfigSpec.IntValue RECENTLY_BITTEN_TARGET_COOLDOWN_TICKS;
    private static final ModConfigSpec.BooleanValue BLOOD_DRAIN_STOPS_COMBAT;
    private static final ModConfigSpec.BooleanValue PREVENT_VAMPIRES_BITING_MCA_VAMPIRES;
    private static final ModConfigSpec.BooleanValue SHOW_ALREADY_VAMPIRE_BITE_MESSAGE;
    private static final ModConfigSpec.BooleanValue VAMPIRE_VILLAGERS_INFECT_HUMANS_AT_NIGHT;
    private static final ModConfigSpec.BooleanValue VAMPIRE_VILLAGERS_USE_NON_LETHAL_BITE_ON_HUMANS;
    private static final ModConfigSpec.IntValue RETALIATION_MEMORY_TICKS;
    private static final ModConfigSpec.BooleanValue ATTACK_ONLY_TRANSFORMED_WEREWOLVES;

    private static final ModConfigSpec.DoubleValue MAX_HEALTH_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue ATTACK_DAMAGE_BONUS;
    private static final ModConfigSpec.DoubleValue ARMOR_BONUS;
    private static final ModConfigSpec.DoubleValue FOLLOW_RANGE_BONUS;
    private static final ModConfigSpec.BooleanValue APPLY_COMBAT_ATTRIBUTES_ONLY_TO_ADULTS;
    private static final ModConfigSpec.DoubleValue VAMPIRE_VILLAGER_MOVEMENT_SPEED_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue HUNTER_VILLAGER_MOVEMENT_SPEED_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue WEREWOLF_VILLAGER_MOVEMENT_SPEED_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue DEFENSIVE_VILLAGER_MOVEMENT_SPEED_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue CURING_VILLAGER_MOVEMENT_SPEED_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue SPECIAL_VILLAGER_MAX_NAVIGATION_SPEED;
    private static final ModConfigSpec.DoubleValue SPECIAL_VILLAGER_MELEE_GOAL_SPEED;
    private static final ModConfigSpec.DoubleValue SPECIAL_VILLAGER_CHASE_GOAL_SPEED;

    private static final ModConfigSpec.DoubleValue HUMAN_SOCIAL_PENALTY;
    private static final ModConfigSpec.DoubleValue WEREWOLF_SOCIAL_PENALTY;
    private static final ModConfigSpec.DoubleValue HUNTER_SOCIAL_PENALTY;
    private static final ModConfigSpec.BooleanValue BLOCK_WEREWOLF_INTERACTIONS;
    private static final ModConfigSpec.BooleanValue BLOCK_HUNTER_INTERACTIONS;

    private static final ModConfigSpec.BooleanValue MONSTERS_IGNORE_MCA_VAMPIRE_VILLAGERS;
    private static final ModConfigSpec.BooleanValue CANCEL_MONSTER_DAMAGE_TO_MCA_VAMPIRE_VILLAGERS;
    private static final ModConfigSpec.BooleanValue SUPPRESS_MCA_VAMPIRE_VILLAGER_ZOMBIE_PANIC;
    private static final ModConfigSpec.BooleanValue SUPPRESS_MCA_VAMPIRE_VILLAGER_FEAR_DIALOGUE;
    private static final ModConfigSpec.BooleanValue MONSTERS_IGNORE_SPECIAL_MCA_VILLAGERS;
    private static final ModConfigSpec.BooleanValue SPECIAL_MCA_VILLAGERS_IGNORE_COMMON_MONSTERS;
    private static final ModConfigSpec.BooleanValue SUPPRESS_SPECIAL_MCA_VILLAGER_MONSTER_PANIC;
    private static final ModConfigSpec.BooleanValue SUPPRESS_SPECIAL_MCA_VILLAGER_FEAR_DIALOGUE;
    private static final ModConfigSpec.BooleanValue CANCEL_MONSTER_DAMAGE_TO_SPECIAL_MCA_VILLAGERS;
    private static final ModConfigSpec.BooleanValue INFECTED_MCA_VILLAGERS_COUNT_AS_SPECIAL_FOR_MONSTER_IGNORE;
    private static final ModConfigSpec.BooleanValue SPECIAL_MCA_GUARDS_DEFEND_AGAINST_HOSTILE_MOBS;
    private static final ModConfigSpec.BooleanValue SPECIAL_MCA_GUARDS_STILL_SUPPRESS_PANIC;
    private static final ModConfigSpec.BooleanValue SPECIAL_MCA_GUARDS_ATTACK_CREEPERS;

    private static final ModConfigSpec.BooleanValue PREVENT_AGGRESSIVE_VILLAGER_REPLACEMENT_FOR_MCA_VAMPIRES;
    private static final ModConfigSpec.BooleanValue PREVENT_AGGRESSIVE_VILLAGER_REPLACEMENT_FOR_ALL_MCA_VILLAGERS;

    private static final ModConfigSpec.BooleanValue ENABLE_MCA_VILLAGER_DEFENSE_AGAINST_VAMPIRES;
    private static final ModConfigSpec.BooleanValue ENABLE_TEMPORARY_DEFENSIVE_MODE;
    private static final ModConfigSpec.BooleanValue DEFENSIVE_MODE_ONLY_IN_HUNTER_VILLAGES;
    private static final ModConfigSpec.IntValue VAMPIRE_THREAT_ALERT_RADIUS;
    private static final ModConfigSpec.IntValue VAMPIRE_THREAT_WINDOW_TICKS;
    private static final ModConfigSpec.IntValue VAMPIRE_THREAT_COUNT_TO_TRIGGER_DEFENSE;
    private static final ModConfigSpec.IntValue DEFENSIVE_MODE_DURATION_TICKS;
    private static final ModConfigSpec.BooleanValue DEFENSIVE_VILLAGERS_CAN_ATTACK_VAMPIRE_PLAYERS;
    private static final ModConfigSpec.BooleanValue DEFENSIVE_VILLAGERS_CAN_ATTACK_MCA_VAMPIRE_VILLAGERS;
    private static final ModConfigSpec.BooleanValue DEFENSIVE_VILLAGERS_WARN_INSTEAD_OF_ATTACK_MCA_VAMPIRES;
    private static final ModConfigSpec.BooleanValue DEFENSIVE_VILLAGERS_ATTACK_ONLY_IF_THREAT_NEARBY;
    private static final ModConfigSpec.BooleanValue DEFENSIVE_MODE_GRANTS_HUNTER_TRAIT;
    private static final ModConfigSpec.BooleanValue DEFENSIVE_MODE_USES_PITCHFORK;
    private static final ModConfigSpec.ConfigValue<String> DEFENSIVE_VILLAGER_WEAPON_TYPE;
    private static final ModConfigSpec.BooleanValue DEFENSIVE_VILLAGERS_HUNTER_TRAIT_IS_TEMPORARY;
    private static final ModConfigSpec.BooleanValue DEFENSIVE_VILLAGERS_ARE_HUNTER_ALIGNED;
    private static final ModConfigSpec.BooleanValue DEFENSIVE_VILLAGERS_COUNT_AS_HUNTERS_FOR_VAMPIRE_AI;

    private static final ModConfigSpec.DoubleValue HUMAN_TRADE_PRICE_MULTIPLIER;
    private static final ModConfigSpec.BooleanValue BLOCK_WEREWOLF_TRADING;
    private static final ModConfigSpec.BooleanValue BLOCK_HUNTER_TRADING;
    private static final ModConfigSpec.BooleanValue VAMPIRE_VILLAGERS_TRADE_ONLY_WITH_VAMPIRES;
    private static final ModConfigSpec.BooleanValue CLEAR_VAMPIRE_BITE_REPUTATION_PENALTY_ON_CONVERSION;
    private static final ModConfigSpec.BooleanValue NEUTRALIZE_CONVERSION_BITE_PENALTY_FOR_VAMPIRES;
    private static final ModConfigSpec.DoubleValue VAMPIRE_PLAYER_TRADE_DISCOUNT;
    private static final ModConfigSpec.DoubleValue HUNTER_TRADE_PRICE_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue WEREWOLF_TRADE_PRICE_MULTIPLIER;
    private static final ModConfigSpec.BooleanValue VAMPIRE_VILLAGERS_TREAT_VAMPIRES_AS_ALLIES;
    private static final ModConfigSpec.BooleanValue VAMPIRE_VILLAGERS_TREAT_HUNTERS_AS_ENEMIES;
    private static final ModConfigSpec.BooleanValue VAMPIRE_VILLAGERS_TREAT_TRANSFORMED_WEREWOLVES_AS_ENEMIES;

    private static final ModConfigSpec.BooleanValue RESPECT_VAMPIRE_DISGUISE_FOR_MCA_INTERACTIONS;
    private static final ModConfigSpec.BooleanValue VAMPIRE_DISGUISE_SUPPRESSES_VAMPIRE_TRADE_DISCOUNT;
    private static final ModConfigSpec.BooleanValue VAMPIRE_DISGUISE_USES_HUMAN_SOCIAL_PENALTY;
    private static final ModConfigSpec.BooleanValue INFECTIOUS_BITE_OVERRIDES_CONVERSION_CHANCE;
    private static final ModConfigSpec.BooleanValue INFECTIOUS_BITE_STILL_USES_CONFIGURED_DURATION;

    private static final ModConfigSpec.BooleanValue HUNTER_VILLAGERS_IMMUNE_TO_VAMPIRISM;
    private static final ModConfigSpec.BooleanValue HUNTER_VILLAGERS_HAVE_POISONOUS_BLOOD;
    private static final ModConfigSpec.BooleanValue HUNTER_VILLAGERS_APPLY_TOXICANT_TO_VAMPIRE_BITERS;
    private static final ModConfigSpec.IntValue HUNTER_VILLAGER_TOXICANT_DURATION_TICKS;
    private static final ModConfigSpec.IntValue HUNTER_VILLAGER_TOXICANT_AMPLIFIER;
    private static final ModConfigSpec.BooleanValue HUNTER_VILLAGER_FALLBACK_TO_VANILLA_POISON;
    private static final ModConfigSpec.BooleanValue HUNTER_VILLAGERS_IMMUNE_TO_WEREWOLF_TRANSFORMATION;
    private static final ModConfigSpec.BooleanValue HUNTER_VILLAGERS_ATTACK_VAMPIRE_PLAYERS;
    private static final ModConfigSpec.BooleanValue HUNTER_VILLAGERS_ATTACK_MCA_VAMPIRE_VILLAGERS;
    private static final ModConfigSpec.BooleanValue HUNTER_VILLAGERS_ATTACK_TRANSFORMED_WEREWOLVES;
    private static final ModConfigSpec.BooleanValue HUNTER_VILLAGERS_ATTACK_VAMPIRE_MOBS;
    private static final ModConfigSpec.BooleanValue HUNTER_VILLAGERS_ATTACK_ONLY_WHILE_DEFENSIVE;
    private static final ModConfigSpec.BooleanValue HUNTER_VILLAGERS_IGNORE_VAMPIRE_DISGUISE;
    private static final ModConfigSpec.DoubleValue HUNTER_VILLAGER_ATTACK_DAMAGE;
    private static final ModConfigSpec.DoubleValue HUNTER_VILLAGER_STAKE_CHANCE;
    private static final ModConfigSpec.IntValue HUNTER_VILLAGER_ATTACK_COOLDOWN_TICKS;
    private static final ModConfigSpec.DoubleValue HUNTER_VILLAGER_FOLLOW_RANGE;
    private static final ModConfigSpec.BooleanValue HUNTER_VILLAGE_CONTROL_GRANTS_HUNTER_TRAIT;
    private static final ModConfigSpec.BooleanValue HUNTER_VILLAGERS_ONLY_FROM_HUNTER_CONTROLLED_VILLAGES;
    private static final ModConfigSpec.BooleanValue HUNTER_VILLAGER_REMOVE_TRAIT_WHEN_VILLAGE_CONTROL_LOST;
    private static final ModConfigSpec.BooleanValue HUNTER_VILLAGER_KEEP_MANUAL_HUNTER_TRAIT;
    private static final ModConfigSpec.BooleanValue HUNTER_VILLAGER_AVOID_DUPLICATE_TOXICANT;
    private static final ModConfigSpec.IntValue HUNTER_VILLAGER_MAX_CHASE_DISTANCE_FROM_VILLAGE;
    private static final ModConfigSpec.IntValue HUNTER_VILLAGER_MAX_CHASE_DISTANCE_FROM_HOME;
    private static final ModConfigSpec.IntValue HUNTER_VILLAGER_RETALIATION_MEMORY_TICKS;
    private static final ModConfigSpec.BooleanValue HUNTER_VILLAGER_PREFER_RECENT_VAMPIRE_AGGRESSORS;

    private static final ModConfigSpec.BooleanValue HUNTER_VILLAGERS_TRADE_WITH_HUNTERS;
    private static final ModConfigSpec.BooleanValue HUNTER_VILLAGERS_TRADE_WITH_HUMANS;
    private static final ModConfigSpec.BooleanValue HUNTER_VILLAGERS_BLOCK_VAMPIRE_TRADING;
    private static final ModConfigSpec.BooleanValue HUNTER_VILLAGERS_BLOCK_TRANSFORMED_WEREWOLF_TRADING;
    private static final ModConfigSpec.DoubleValue HUNTER_VILLAGER_HUNTER_DISCOUNT;
    private static final ModConfigSpec.DoubleValue HUNTER_VILLAGER_HUMAN_PRICE_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue HUNTER_VILLAGER_VAMPIRE_PRICE_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue HUNTER_VILLAGER_WEREWOLF_PRICE_MULTIPLIER;
    private static final ModConfigSpec.BooleanValue HUNTER_VILLAGER_DISGUISED_VAMPIRE_CAN_TRADE;
    private static final ModConfigSpec.BooleanValue HUNTER_VILLAGER_DISGUISED_VAMPIRE_USES_HUMAN_PRICES;

    private static final ModConfigSpec.BooleanValue HUNTER_VILLAGERS_BLOCK_VAMPIRE_ROMANCE;
    private static final ModConfigSpec.BooleanValue HUNTER_VILLAGERS_BLOCK_TRANSFORMED_WEREWOLF_ROMANCE;
    private static final ModConfigSpec.DoubleValue HUNTER_VILLAGER_VAMPIRE_SOCIAL_PENALTY;
    private static final ModConfigSpec.DoubleValue HUNTER_VILLAGER_WEREWOLF_SOCIAL_PENALTY;
    private static final ModConfigSpec.DoubleValue HUNTER_VILLAGER_HUMAN_SOCIAL_PENALTY;
    private static final ModConfigSpec.DoubleValue HUNTER_VILLAGER_HUNTER_SOCIAL_PENALTY;
    private static final ModConfigSpec.BooleanValue HUNTER_VILLAGER_DISGUISED_VAMPIRE_USES_HUMAN_SOCIAL_RULES;
    private static final ModConfigSpec.BooleanValue HUNTER_VILLAGER_DISGUISED_VAMPIRE_CAN_ROMANCE;

    private static final ModConfigSpec.BooleanValue ENABLE_MCA_VAMPIRE_VILLAGER_CURE;
    private static final ModConfigSpec.BooleanValue ALLOW_MILK_TO_TREAT_MCA_VILLAGER_SANGUINARE;
    private static final ModConfigSpec.BooleanValue MILK_TREATMENT_REMOVES_NEGATIVE_EFFECTS;
    private static final ModConfigSpec.BooleanValue MILK_TREATMENT_CONSUMES_MILK;
    private static final ModConfigSpec.BooleanValue MILK_TREATMENT_RETURNS_BUCKET;
    private static final ModConfigSpec.BooleanValue MILK_TREATMENT_ONLY_BEFORE_CONVERSION;
    private static final ModConfigSpec.BooleanValue VAMPIRE_CURE_REQUIRES_WEAKNESS;
    private static final ModConfigSpec.BooleanValue VAMPIRE_CURE_REQUIRES_GOLDEN_APPLE;
    private static final ModConfigSpec.BooleanValue VAMPIRE_CURE_REQUIRES_GARLIC_INJECTION;
    private static final ModConfigSpec.IntValue VAMPIRE_CURE_REQUIRED_WEAKNESS_DURATION_TICKS;
    private static final ModConfigSpec.BooleanValue VAMPIRE_CURE_ALLOW_ANY_WEAKNESS_DURATION;
    private static final ModConfigSpec.IntValue VAMPIRE_CURE_INGREDIENT_PROGRESS_EXPIRES_TICKS;
    private static final ModConfigSpec.IntValue VAMPIRE_CURE_DURATION_TICKS;
    private static final ModConfigSpec.BooleanValue VAMPIRE_CURE_VILLAGER_CANNOT_ATTACK;
    private static final ModConfigSpec.BooleanValue VAMPIRE_CURE_VILLAGER_CANNOT_TRADE;
    private static final ModConfigSpec.BooleanValue VAMPIRE_CURE_SHOWS_PARTICLES;
    private static final ModConfigSpec.BooleanValue VAMPIRE_CURE_APPLIES_WEAKNESS_DURING_PROCESS;
    private static final ModConfigSpec.IntValue POST_CURE_RESYNC_BLOCK_TICKS;

    private static final ModConfigSpec.BooleanValue ENABLE_EASY_VILLAGERS_FACTION_RULES;
    private static final ModConfigSpec.BooleanValue ENABLE_EASY_VILLAGERS_FACTION_PICKUP_RULES;
    private static final ModConfigSpec.BooleanValue EASY_VILLAGERS_BLOCK_PICKUP_OF_CURING_VILLAGERS;
    private static final ModConfigSpec.BooleanValue ALLOW_PICKUP_OF_INFECTED_HUMAN_VILLAGERS;
    private static final ModConfigSpec.BooleanValue SHOW_PICKUP_BLOCKED_MESSAGE;

    private static final ModConfigSpec.BooleanValue ALLOW_MCA_VILLAGER_WEREWOLF_INFECTION;
    private static final ModConfigSpec.BooleanValue ALLOW_MCA_CHILD_WEREWOLF_INFECTION;
    private static final ModConfigSpec.DoubleValue WEREWOLF_CONVERSION_CHANCE_ON_BITE;
    private static final ModConfigSpec.IntValue WEREWOLF_VILLAGER_INFECTION_DURATION_TICKS;
    private static final ModConfigSpec.BooleanValue VAMPIRE_VILLAGERS_IMMUNE_TO_WEREWOLF_TRANSFORMATION;
    private static final ModConfigSpec.BooleanValue WEREWOLF_VILLAGERS_IMMUNE_TO_VAMPIRISM;
    private static final ModConfigSpec.BooleanValue WEREWOLF_VILLAGERS_IMMUNE_TO_HUNTER_CONVERSION;

    private static final ModConfigSpec.BooleanValue WEREWOLF_VILLAGER_APPEARANCE_ENABLED;
    private static final ModConfigSpec.BooleanValue WEREWOLF_VILLAGER_EARS_ENABLED;
    private static final ModConfigSpec.BooleanValue WEREWOLF_VILLAGER_CLAWS_ENABLED;
    private static final ModConfigSpec.BooleanValue WEREWOLF_VILLAGER_APPEARANCE_DEBUG;

    private static final ModConfigSpec.BooleanValue WEREWOLF_VILLAGERS_TRADE_WITH_WEREWOLVES;
    private static final ModConfigSpec.BooleanValue WEREWOLF_VILLAGERS_TRADE_WITH_HUMANS;
    private static final ModConfigSpec.BooleanValue WEREWOLF_VILLAGERS_BLOCK_VAMPIRE_TRADING;
    private static final ModConfigSpec.BooleanValue WEREWOLF_VILLAGERS_BLOCK_HUNTER_TRADING;
    private static final ModConfigSpec.DoubleValue WEREWOLF_VILLAGER_WEREWOLF_DISCOUNT;
    private static final ModConfigSpec.DoubleValue WEREWOLF_VILLAGER_HUMAN_PRICE_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue WEREWOLF_VILLAGER_VAMPIRE_PRICE_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue WEREWOLF_VILLAGER_HUNTER_PRICE_MULTIPLIER;
    private static final ModConfigSpec.BooleanValue WEREWOLF_VILLAGER_DISGUISED_VAMPIRE_USES_HUMAN_PRICES;
    private static final ModConfigSpec.BooleanValue WEREWOLF_VILLAGER_DISGUISED_VAMPIRE_CAN_TRADE;

    private static final ModConfigSpec.BooleanValue WEREWOLF_VILLAGERS_BLOCK_VAMPIRE_ROMANCE;
    private static final ModConfigSpec.BooleanValue WEREWOLF_VILLAGERS_BLOCK_HUNTER_ROMANCE;
    private static final ModConfigSpec.DoubleValue WEREWOLF_VILLAGER_VAMPIRE_SOCIAL_PENALTY;
    private static final ModConfigSpec.DoubleValue WEREWOLF_VILLAGER_HUNTER_SOCIAL_PENALTY;
    private static final ModConfigSpec.DoubleValue WEREWOLF_VILLAGER_HUMAN_SOCIAL_PENALTY;
    private static final ModConfigSpec.DoubleValue WEREWOLF_VILLAGER_WEREWOLF_SOCIAL_PENALTY;
    private static final ModConfigSpec.BooleanValue WEREWOLF_VILLAGER_DISGUISED_VAMPIRE_USES_HUMAN_SOCIAL_RULES;
    private static final ModConfigSpec.BooleanValue WEREWOLF_VILLAGER_DISGUISED_VAMPIRE_CAN_ROMANCE;

    private static final ModConfigSpec.BooleanValue ENABLE_MCA_WEREWOLF_VILLAGER_AI;
    private static final ModConfigSpec.BooleanValue WEREWOLF_VILLAGERS_ATTACK_VAMPIRE_PLAYERS;
    private static final ModConfigSpec.BooleanValue WEREWOLF_VILLAGERS_ATTACK_HUNTER_PLAYERS;
    private static final ModConfigSpec.BooleanValue WEREWOLF_VILLAGERS_ATTACK_VAMPIRE_MOBS;
    private static final ModConfigSpec.BooleanValue WEREWOLF_VILLAGERS_ATTACK_HUNTER_NPCS;
    private static final ModConfigSpec.BooleanValue WEREWOLF_VILLAGERS_RESPECT_VAMPIRE_DISGUISE;
    private static final ModConfigSpec.BooleanValue WEREWOLF_VILLAGERS_ATTACK_ONLY_AT_NIGHT;
    private static final ModConfigSpec.DoubleValue WEREWOLF_VILLAGER_ATTACK_DAMAGE;
    private static final ModConfigSpec.IntValue WEREWOLF_VILLAGER_ATTACK_COOLDOWN_TICKS;
    private static final ModConfigSpec.DoubleValue WEREWOLF_VILLAGER_FOLLOW_RANGE;
    private static final ModConfigSpec.DoubleValue WEREWOLF_VILLAGER_MAX_CHASE_DISTANCE_FROM_HOME;
    private static final ModConfigSpec.IntValue WEREWOLF_VILLAGER_TARGET_MEMORY_TICKS;

    private static final ModConfigSpec.IntValue SPECIAL_VILLAGER_TARGET_SCAN_INTERVAL_TICKS;
    private static final ModConfigSpec.IntValue SPECIAL_VILLAGER_RETALIATION_MEMORY_TICKS;
    private static final ModConfigSpec.IntValue SPECIAL_VILLAGER_MAX_CHASE_DISTANCE;
    private static final ModConfigSpec.IntValue SPECIAL_VILLAGER_ATTACK_COOLDOWN_TICKS;
    private static final ModConfigSpec.IntValue SPECIAL_VILLAGER_COMBAT_REPATH_INTERVAL_TICKS;
    private static final ModConfigSpec.BooleanValue SPECIAL_VILLAGERS_TARGET_FACTION_NPCS;

    private static final ModConfigSpec.BooleanValue ENABLE_MCA_WEREWOLF_VILLAGER_CURE;
    private static final ModConfigSpec.BooleanValue WEREWOLF_CURE_REQUIRES_WEAKNESS;
    private static final ModConfigSpec.BooleanValue WEREWOLF_CURE_REQUIRES_GOLDEN_APPLE;
    private static final ModConfigSpec.BooleanValue WEREWOLF_CURE_REQUIRES_GARLIC_INJECTION;
    private static final ModConfigSpec.IntValue WEREWOLF_CURE_REQUIRED_WEAKNESS_DURATION_TICKS;
    private static final ModConfigSpec.BooleanValue WEREWOLF_CURE_ALLOW_ANY_WEAKNESS_DURATION;
    private static final ModConfigSpec.IntValue WEREWOLF_CURE_INGREDIENT_PROGRESS_EXPIRES_TICKS;
    private static final ModConfigSpec.IntValue WEREWOLF_CURE_DURATION_TICKS;
    private static final ModConfigSpec.BooleanValue WEREWOLF_CURE_VILLAGER_CANNOT_ATTACK;
    private static final ModConfigSpec.BooleanValue WEREWOLF_CURE_VILLAGER_CANNOT_TRADE;
    private static final ModConfigSpec.BooleanValue WEREWOLF_CURE_SHOWS_PARTICLES;
    private static final ModConfigSpec.BooleanValue WEREWOLF_CURE_APPLIES_WEAKNESS_DURING_PROCESS;

    private static final ModConfigSpec.BooleanValue ENABLE_MCA_VILLAGE_CAPTURE_INTEGRATION;
    private static final ModConfigSpec.DoubleValue MCA_HUMAN_VILLAGER_DEFENSE_STRENGTH;
    private static final ModConfigSpec.DoubleValue MCA_HUNTER_VILLAGER_DEFENSE_STRENGTH;
    private static final ModConfigSpec.DoubleValue MCA_VAMPIRE_VILLAGER_VAMPIRE_STRENGTH;
    private static final ModConfigSpec.DoubleValue MCA_INFECTED_VILLAGER_DEFENSE_STRENGTH;
    private static final ModConfigSpec.DoubleValue MCA_CURING_VILLAGER_DEFENSE_STRENGTH;
    private static final ModConfigSpec.DoubleValue MCA_CHILD_VILLAGER_DEFENSE_STRENGTH;
    private static final ModConfigSpec.IntValue MCA_VILLAGE_CAPTURE_STRENGTH_RADIUS;
    private static final ModConfigSpec.BooleanValue MCA_VILLAGE_CAPTURE_DEBUG_BREAKDOWN;
    private static final ModConfigSpec.BooleanValue MAKE_AGGRESSIVE_ONLY_CREATES_MCA_HUNTERS_IN_HUNTER_VILLAGES;
    private static final ModConfigSpec.BooleanValue VAMPIRE_VILLAGE_CAPTURE_INFECTS_MCA_VILLAGERS;
    private static final ModConfigSpec.DoubleValue VAMPIRE_VILLAGE_CAPTURE_INFECTION_CHANCE;
    private static final ModConfigSpec.BooleanValue VAMPIRE_VILLAGE_CAPTURE_INFECTS_CHILDREN;
    private static final ModConfigSpec.BooleanValue VAMPIRE_VILLAGE_CAPTURE_CAN_INFECT_HUNTER_VILLAGERS;
    private static final ModConfigSpec.BooleanValue VAMPIRE_VILLAGE_CAPTURE_ACCELERATES_EXISTING_SANGUINARE;
    private static final ModConfigSpec.DoubleValue VAMPIRE_VILLAGE_CAPTURE_ACCELERATION_MULTIPLIER;
    private static final ModConfigSpec.BooleanValue VAMPIRE_VILLAGE_CAPTURE_DOES_NOT_INSTANTLY_CONVERT_MCA_VILLAGERS;
    private static final ModConfigSpec.BooleanValue HUNTER_VILLAGE_CAPTURE_GRANTS_HUNTER_TRAIT;
    private static final ModConfigSpec.BooleanValue HUNTER_VILLAGE_CAPTURE_CURES_MCA_SANGUINARE;
    private static final ModConfigSpec.BooleanValue HUNTER_VILLAGE_CAPTURE_CURES_MCA_VAMPIRES;
    private static final ModConfigSpec.BooleanValue HUNTER_VILLAGE_CAPTURE_STARTS_CURE_FOR_MCA_VAMPIRES;
    private static final ModConfigSpec.BooleanValue WEREWOLF_VILLAGERS_RESIST_VAMPIRE_CAPTURE_INFECTION;
    private static final ModConfigSpec.BooleanValue HUNTER_VILLAGE_DOES_NOT_CONVERT_WEREWOLVES_TO_HUNTERS;

    private static final ModConfigSpec.DoubleValue TWO_VAMPIRE_PARENTS_INHERITANCE_CHANCE;
    private static final ModConfigSpec.DoubleValue ONE_VAMPIRE_PARENT_INHERITANCE_CHANCE;
    private static final ModConfigSpec.DoubleValue TWO_WEREWOLF_PARENTS_INHERITANCE_CHANCE;
    private static final ModConfigSpec.DoubleValue ONE_WEREWOLF_PARENT_INHERITANCE_CHANCE;
    private static final ModConfigSpec.DoubleValue VAMPIRE_BABY_GROWTH_MULTIPLIER;
    private static final ModConfigSpec.IntValue GROWTH_PROCESS_INTERVAL_TICKS;
    private static final ModConfigSpec.BooleanValue ALLOW_VAMPIRE_CHILDREN_GROWTH_BOOST;

    private static final ModConfigSpec.BooleanValue ENABLE_PERSISTENT_VAMPIRE_APPEARANCE;
    private static final ModConfigSpec.BooleanValue PALE_SKIN_ENABLED;
    private static final ModConfigSpec.BooleanValue VAMPIRE_EYES_ENABLED;
    private static final ModConfigSpec.BooleanValue PRESERVE_ORIGINAL_APPEARANCE_FOR_CURE;

    private static final ModConfigSpec.BooleanValue ENABLE_MCA_VAMPIRE_TRAIT;
    private static final ModConfigSpec.BooleanValue ENABLE_MCA_WEREWOLF_TRAIT;
    private static final ModConfigSpec.BooleanValue SYNC_TRAIT_AND_CAPABILITY_ON_LOAD;
    private static final ModConfigSpec.BooleanValue ENABLE_LEGACY_103_MIGRATION;
    private static final ModConfigSpec.BooleanValue TRAIT_CAN_RESTORE_CAPABILITY;
    private static final ModConfigSpec.BooleanValue VAMPIRE_TRAIT_CONTROLS_INHERITANCE;
    private static final ModConfigSpec.DoubleValue ONE_VAMPIRE_PARENT_TRAIT_INHERITANCE_CHANCE;
    private static final ModConfigSpec.DoubleValue TWO_VAMPIRE_PARENTS_TRAIT_INHERITANCE_CHANCE;

    private static final ModConfigSpec.BooleanValue RESPECT_MCA_RELATIONSHIPS_IN_COMBAT;
    private static final ModConfigSpec.BooleanValue RESPECT_MCA_PLAYER_COMMANDS;
    private static final ModConfigSpec.BooleanValue SUPPRESS_AUTONOMOUS_COMBAT_WHILE_FOLLOWING;
    private static final ModConfigSpec.BooleanValue SUPPRESS_AUTONOMOUS_COMBAT_WHILE_STAYING;
    private static final ModConfigSpec.BooleanValue ALLOW_SHORT_RETALIATION_WHILE_FOLLOWING;
    private static final ModConfigSpec.BooleanValue ALLOW_SHORT_RETALIATION_WHILE_STAYING;
    private static final ModConfigSpec.DoubleValue COMMANDED_RETALIATION_MAX_DISTANCE;
    private static final ModConfigSpec.IntValue COMMANDED_RETALIATION_TICKS;
    private static final ModConfigSpec.BooleanValue CLEAR_AUTONOMOUS_TARGET_WHEN_COMMANDED;

    private static final ModConfigSpec.BooleanValue ENABLE_FACTION_COMMAND_RESTRICTIONS;
    private static final ModConfigSpec.BooleanValue SHOW_COMMAND_BLOCKED_MESSAGE;
    private static final ModConfigSpec.BooleanValue ALLOW_SPECIAL_PLAYERS_COMMAND_HUMANS;
    private static final ModConfigSpec.BooleanValue ALLOW_COMMANDS_FOR_INFECTED_HUMANS;
    private static final ModConfigSpec.BooleanValue ALLOW_COMMANDS_FOR_CURING_VILLAGERS;
    private static final ModConfigSpec.BooleanValue BLOCK_UNKNOWN_MCA_COMMANDS_FOR_ENEMY_FACTIONS;

    private static final ModConfigSpec.BooleanValue GIVE_OCCULT_ARTS_BOOK_ON_SPECIAL_VILLAGE_DISCOVERY;
    private static final java.util.Map<com.guilh.mca_vampirism_compat.mission.OccultVillageMissionType, ModConfigSpec.IntValue> MISSION_REWARDS = new java.util.EnumMap<>(com.guilh.mca_vampirism_compat.mission.OccultVillageMissionType.class);
    private static final ModConfigSpec.BooleanValue ENABLE_OCCULT_VILLAGE_MISSIONS;
    private static final ModConfigSpec.IntValue OCCULT_VILLAGE_MISSION_COOLDOWN_TICKS;
    private static final ModConfigSpec.IntValue OCCULT_VILLAGE_MISSION_DEADLINE_TICKS;
    private static final ModConfigSpec.BooleanValue MCA_VILLAGE_EFFECTS_FOLLOW_MCA_BOUNDS;
    private static final ModConfigSpec.IntValue MCA_VILLAGE_FOG_BORDER_MARGIN_BLOCKS;
    private static final ModConfigSpec.BooleanValue ENABLE_FACTION_CONTEXTUAL_DIALOGUES;
    private static final ModConfigSpec.BooleanValue ENABLE_FACTION_GIFT_PREFERENCES;
    private static final ModConfigSpec.BooleanValue ENABLE_FACTION_RECOVERY_DIET;

    private static final ModConfigSpec.BooleanValue ENABLE_DEBUG_LOGS;
    private static final ModConfigSpec.BooleanValue ENABLE_VERBOSE_AI_DEBUG_LOGS;
    private static final ModConfigSpec.BooleanValue ENABLE_VERBOSE_NBT_DEBUG_LOGS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("vampire");
        builder.comment("Controls Sanguinare duration for MCA villagers infected by vampire players.");
        PLAYER_SANGUINARE_DURATION_TICKS = builder.defineInRange("playerSanguinareDurationTicks", 24000, 20, 24000);
        builder.comment("Controls Sanguinare duration for MCA villagers infected by MCA vampire villagers.");
        VAMPIRE_VILLAGER_SANGUINARE_DURATION_TICKS = builder.defineInRange("vampireVillagerSanguinareDurationTicks", 12000, 20, 24000);
        ALLOW_MCA_VILLAGER_INFECTION = builder.define("allowMcaVillagerInfection", true);
        ALLOW_CHILD_INFECTION = builder.define("allowChildInfection", false);
        builder.pop();

        builder.push("vampire");
        BITE_COOLDOWN_TICKS = builder.defineInRange("biteCooldownTicks", 6000, 20, 72000);
        NIGHT_ATTACK_CHANCE = builder.defineInRange("nightAttackChance", 0.18D, 0.0D, 1.0D);
        builder.comment("Chance that a valid vampire bite applies Sanguinare.");
        CONVERSION_CHANCE_ON_BITE = builder.defineInRange("conversionChanceOnBite", 0.65D, 0.0D, 1.0D);
        builder.comment("If true, adult MCA vampire villagers use MCA's night schedule: they rest during the day and work at night.");
        VAMPIRE_VILLAGERS_USE_NIGHT_SCHEDULE = builder.define("vampireVillagersUseNightSchedule", true);
        builder.comment("If true, MCA vampire villagers seek shade when exposed to daylight.");
        VAMPIRE_VILLAGERS_AVOID_SUN = builder.define("vampireVillagersAvoidSun", true);
        builder.comment("If true, MCA vampire villagers take sunlight damage when exposed.");
        VAMPIRE_VILLAGERS_BURN_IN_SUN = builder.define("vampireVillagersBurnInSun", false);
        NON_LETHAL_BITE_AGAINST_VILLAGERS = builder.define("nonLethalBiteAgainstVillagers", true);
        VILLAGER_BITE_DAMAGE = builder.defineInRange("villagerBiteDamage", 0.0D, 0.0D, 40.0D);
        RECENTLY_BITTEN_TARGET_COOLDOWN_TICKS = builder.defineInRange("recentlyBittenTargetCooldownTicks", 12000, 20, 72000);
        BLOOD_DRAIN_STOPS_COMBAT = builder.define("bloodDrainStopsCombat", true);
        McaLegacyBiteToml.defineVampireFeedingOption(builder);
        PREVENT_VAMPIRES_BITING_MCA_VAMPIRES = builder.define("preventVampiresBitingMcaVampires", true);
        SHOW_ALREADY_VAMPIRE_BITE_MESSAGE = builder.define("showAlreadyVampireBiteMessage", true);
        VAMPIRE_VILLAGERS_INFECT_HUMANS_AT_NIGHT = builder.define("vampireVillagersInfectHumansAtNight", true);
        VAMPIRE_VILLAGERS_USE_NON_LETHAL_BITE_ON_HUMANS = builder.define("vampireVillagersUseNonLethalBiteOnHumans", true);
        RETALIATION_MEMORY_TICKS = builder.defineInRange("retaliationMemoryTicks", 2400, 20, 72000);
        ATTACK_ONLY_TRANSFORMED_WEREWOLVES = builder.define("attackOnlyTransformedWerewolves", true);
        McaLegacyBiteToml.definePlayerMeleeOptions(builder);
        builder.pop();

        builder.push("balance");
        builder.comment("Release-balanced vampire attribute modifiers. Defaults keep special villagers only lightly stronger than normal MCA villagers.");
        MAX_HEALTH_MULTIPLIER = builder.defineInRange("maxHealthMultiplier", 1.2D, 1.0D, 10.0D);
        ATTACK_DAMAGE_BONUS = builder.defineInRange("attackDamageBonus", 1.0D, 0.0D, 100.0D);
        ARMOR_BONUS = builder.defineInRange("armorBonus", 2.0D, 0.0D, 30.0D);
        FOLLOW_RANGE_BONUS = builder.defineInRange("followRangeBonus", 4.0D, 0.0D, 128.0D);
        APPLY_COMBAT_ATTRIBUTES_ONLY_TO_ADULTS = builder.define("applyCombatAttributesOnlyToAdults", true);
        builder.comment("Movement speed multipliers affect normal movement. Keep these close to 1.0 so MCA routines such as work/home/follow still feel natural.");
        VAMPIRE_VILLAGER_MOVEMENT_SPEED_MULTIPLIER = builder.defineInRange("vampireVillagerMovementSpeedMultiplier", 1.0D, 0.05D, 3.0D);
        HUNTER_VILLAGER_MOVEMENT_SPEED_MULTIPLIER = builder.defineInRange("hunterVillagerMovementSpeedMultiplier", 1.0D, 0.05D, 3.0D);
        WEREWOLF_VILLAGER_MOVEMENT_SPEED_MULTIPLIER = builder.defineInRange("werewolfVillagerMovementSpeedMultiplier", 1.02D, 0.05D, 3.0D);
        DEFENSIVE_VILLAGER_MOVEMENT_SPEED_MULTIPLIER = builder.defineInRange("defensiveVillagerMovementSpeedMultiplier", 1.0D, 0.05D, 3.0D);
        CURING_VILLAGER_MOVEMENT_SPEED_MULTIPLIER = builder.defineInRange("curingVillagerMovementSpeedMultiplier", 0.35D, 0.05D, 1.0D);
        builder.comment("Combat movement speeds are used only by special combat/bite goals, not by normal MCA work/home/follow navigation. High values can make villagers look jittery.");
        SPECIAL_VILLAGER_MAX_NAVIGATION_SPEED = builder.defineInRange("specialVillagerMaxNavigationSpeed", 0.75D, 0.1D, 3.0D);
        SPECIAL_VILLAGER_MELEE_GOAL_SPEED = builder.defineInRange("specialVillagerMeleeGoalSpeed", 0.65D, 0.1D, 3.0D);
        SPECIAL_VILLAGER_CHASE_GOAL_SPEED = builder.defineInRange("specialVillagerChaseGoalSpeed", 0.7D, 0.1D, 3.0D);
        builder.pop();

        builder.push("factions");
        builder.comment("Penalty applied to positive social interactions with MCA vampire villagers when the player is human.");
        HUMAN_SOCIAL_PENALTY = builder.defineInRange("humanSocialPenalty", 0.10D, 0.0D, 1.0D);
        builder.comment("Penalty applied to positive social interactions with MCA vampire villagers when the player is a werewolf or transformed werewolf.");
        WEREWOLF_SOCIAL_PENALTY = builder.defineInRange("werewolfSocialPenalty", 0.25D, 0.0D, 1.0D);
        builder.comment("Penalty applied to positive social interactions with MCA vampire villagers when the player is a hunter.");
        HUNTER_SOCIAL_PENALTY = builder.defineInRange("hunterSocialPenalty", 0.35D, 0.0D, 1.0D);
        builder.comment("If true, transformed werewolves cannot perform romantic or marriage interactions with MCA vampire villagers.");
        BLOCK_WEREWOLF_INTERACTIONS = builder.define("blockWerewolfInteractions", true);
        builder.comment("If true, hunters cannot perform romantic or marriage interactions with MCA vampire villagers.");
        BLOCK_HUNTER_INTERACTIONS = builder.define("blockHunterInteractions", true);
        builder.pop();

        builder.push("monster_behavior");
        builder.comment("Legacy vampire-specific alias. The active monster-ignore system also has special-villager options below.");
        MONSTERS_IGNORE_MCA_VAMPIRE_VILLAGERS = builder.define("monstersIgnoreMcaVampireVillagers", true);
        builder.comment("Legacy vampire-specific alias. The active monster-damage protection also has special-villager options below.");
        CANCEL_MONSTER_DAMAGE_TO_MCA_VAMPIRE_VILLAGERS = builder.define("cancelMonsterDamageToMcaVampireVillagers", true);
        builder.comment("Legacy vampire-specific alias. The active panic suppression also has special-villager options below.");
        SUPPRESS_MCA_VAMPIRE_VILLAGER_ZOMBIE_PANIC = builder.define("suppressMcaVampireVillagerZombiePanic", true);
        builder.comment("If true, suppresses MCA fear/surprised voice reactions for vampire villagers near common monsters.");
        SUPPRESS_MCA_VAMPIRE_VILLAGER_FEAR_DIALOGUE = builder.define("suppressMcaVampireVillagerFearDialogue", true);
        builder.comment("If true, common hostile monsters ignore MCA vampire, hunter, werewolf and curing villagers. Human MCA villagers remain normal targets.");
        MONSTERS_IGNORE_SPECIAL_MCA_VILLAGERS = builder.define("monstersIgnoreSpecialMcaVillagers", true);
        builder.comment("If true, special MCA villagers clear common monster targets and do not fight/flee as normal humans.");
        SPECIAL_MCA_VILLAGERS_IGNORE_COMMON_MONSTERS = builder.define("specialMcaVillagersIgnoreCommonMonsters", true);
        builder.comment("If true, MCA vampire/hunter/werewolf/curing villagers suppress MCA panic memories caused by common monsters.");
        SUPPRESS_SPECIAL_MCA_VILLAGER_MONSTER_PANIC = builder.define("suppressSpecialMcaVillagerMonsterPanic", true);
        builder.comment("If true, suppresses MCA fear/surprised voice reactions for special MCA villagers near common monsters.");
        SUPPRESS_SPECIAL_MCA_VILLAGER_FEAR_DIALOGUE = builder.define("suppressSpecialMcaVillagerFearDialogue", true);
        builder.comment("Fallback protection: cancels damage from common hostile monsters to special MCA villagers.");
        CANCEL_MONSTER_DAMAGE_TO_SPECIAL_MCA_VILLAGERS = builder.define("cancelMonsterDamageToSpecialMcaVillagers", true);
        builder.comment("If true, Sanguinare/Lupus infected MCA villagers count as special for monster ignore before conversion. Default false: they still look human.");
        INFECTED_MCA_VILLAGERS_COUNT_AS_SPECIAL_FOR_MONSTER_IGNORE = builder.define("infectedMcaVillagersCountAsSpecialForMonsterIgnore", false);
        builder.comment("If true, MCA guards that are vampire, hunter, werewolf or defensive villagers may still defend the village against common hostile mobs.");
        SPECIAL_MCA_GUARDS_DEFEND_AGAINST_HOSTILE_MOBS = builder.define("specialMcaGuardsDefendAgainstHostileMobs", true);
        builder.comment("If true, special MCA guards can fight common hostile mobs while panic/flee/surprised behavior remains suppressed.");
        SPECIAL_MCA_GUARDS_STILL_SUPPRESS_PANIC = builder.define("specialMcaGuardsStillSuppressPanic", true);
        builder.comment("If true, special MCA guards may auto-target creepers. Default false avoids guards chasing creepers into villages.");
        SPECIAL_MCA_GUARDS_ATTACK_CREEPERS = builder.define("specialMcaGuardsAttackCreepers", false);
        builder.pop();

        builder.push("advanced");
        builder.comment("If true, MCA vampire villagers are protected from being replaced by aggressive villager entities from other mods.");
        PREVENT_AGGRESSIVE_VILLAGER_REPLACEMENT_FOR_MCA_VAMPIRES = builder.define("preventAggressiveVillagerReplacementForMcaVampires", true);
        builder.comment("If true, all MCA villagers are protected from aggressive-villager replacement. The addon replaces that dangerous swap with safe defensive mode for human MCA villagers.");
        PREVENT_AGGRESSIVE_VILLAGER_REPLACEMENT_FOR_ALL_MCA_VILLAGERS = builder.define("preventAggressiveVillagerReplacementForAllMcaVillagers", true);
        builder.pop();

        builder.push("hunter");
        builder.comment("Master gate for temporary village-defense retaliation.");
        ENABLE_MCA_VILLAGER_DEFENSE_AGAINST_VAMPIRES = builder.define("enableMcaVillagerDefenseAgainstVampires", true);
        builder.comment("If true, non-hunter MCA humans may enter temporary defensive retaliation mode. Default false: vampire threats do not create aggressive/hunter-like villagers.");
        ENABLE_TEMPORARY_DEFENSIVE_MODE = builder.define("enableTemporaryDefensiveMode", false);
        builder.comment("If true, temporary defensive/aggressive mode is allowed only inside hunter-controlled villages.");
        DEFENSIVE_MODE_ONLY_IN_HUNTER_VILLAGES = builder.define("defensiveModeOnlyInHunterVillages", true);
        VAMPIRE_THREAT_ALERT_RADIUS = builder.defineInRange("vampireThreatAlertRadius", 32, 1, 128);
        VAMPIRE_THREAT_WINDOW_TICKS = builder.defineInRange("vampireThreatWindowTicks", 12000, 20, 72000);
        VAMPIRE_THREAT_COUNT_TO_TRIGGER_DEFENSE = builder.defineInRange("vampireThreatCountToTriggerDefense", 2, 1, 64);
        DEFENSIVE_MODE_DURATION_TICKS = builder.defineInRange("defensiveModeDurationTicks", 12000, 20, 72000);
        DEFENSIVE_VILLAGERS_CAN_ATTACK_VAMPIRE_PLAYERS = builder.define("defensiveVillagersCanAttackVampirePlayers", true);
        DEFENSIVE_VILLAGERS_CAN_ATTACK_MCA_VAMPIRE_VILLAGERS = builder.define("defensiveVillagersCanAttackMcaVampireVillagers", false);
        DEFENSIVE_VILLAGERS_WARN_INSTEAD_OF_ATTACK_MCA_VAMPIRES = builder.define("defensiveVillagersWarnInsteadOfAttackMcaVampires", true);
        DEFENSIVE_VILLAGERS_ATTACK_ONLY_IF_THREAT_NEARBY = builder.define("defensiveVillagersAttackOnlyIfThreatNearby", true);
        builder.comment("If true, temporary defensive mode grants the MCA hunter trait. Default false: real hunter trait comes from hunter-controlled villages or manual traits.");
        DEFENSIVE_MODE_GRANTS_HUNTER_TRAIT = builder.define("defensiveModeGrantsHunterTrait", false);
        builder.comment("If true, temporary defensive mode can visually equip the configured pitchfork/weapon. Default false: only real hunter state equips hunter weapons.");
        DEFENSIVE_MODE_USES_PITCHFORK = builder.define("defensiveModeUsesPitchfork", false);
        DEFENSIVE_VILLAGER_WEAPON_TYPE = builder.define("defensiveVillagerWeaponType", "pitchfork");
        builder.comment("If true, the addon hunter trait is removed when defensive mode ends.");
        DEFENSIVE_VILLAGERS_HUNTER_TRAIT_IS_TEMPORARY = builder.define("defensiveVillagersHunterTraitIsTemporary", true);
        builder.comment("If true, defensive MCA villagers are treated as hunter-aligned by this addon. Default false: defensive mode is not real hunter state.");
        DEFENSIVE_VILLAGERS_ARE_HUNTER_ALIGNED = builder.define("defensiveVillagersAreHunterAligned", false);
        builder.comment("If true, MCA vampire villager AI treats defensive hunter-aligned villagers as hunter-like enemies when the attack configs allow it.");
        DEFENSIVE_VILLAGERS_COUNT_AS_HUNTERS_FOR_VAMPIRE_AI = builder.define("defensiveVillagersCountAsHuntersForVampireAI", false);
        builder.pop();

        builder.push("factions");
        builder.comment("Active. Used by the per-player wrapper trade price calculator. Real villager offers are not permanently modified.");
        HUMAN_TRADE_PRICE_MULTIPLIER = builder.defineInRange("humanTradePriceMultiplier", 1.25D, 1.0D, 10.0D);
        builder.comment("If true, transformed werewolves cannot trade with MCA vampire villagers. If false, they can trade using werewolfTradePriceMultiplier.");
        BLOCK_WEREWOLF_TRADING = builder.define("blockWerewolfTrading", true);
        builder.comment("If true, hunters cannot trade with MCA vampire villagers. If false, they can trade using hunterTradePriceMultiplier.");
        BLOCK_HUNTER_TRADING = builder.define("blockHunterTrading", true);
        VAMPIRE_VILLAGERS_TRADE_ONLY_WITH_VAMPIRES = builder.define("vampireVillagersTradeOnlyWithVampires", false);
        builder.comment("When safe API is unavailable, this is handled by the addon price bridge instead of editing MCA/vanilla reputation.");
        CLEAR_VAMPIRE_BITE_REPUTATION_PENALTY_ON_CONVERSION = builder.define("clearVampireBiteReputationPenaltyOnConversion", true);
        NEUTRALIZE_CONVERSION_BITE_PENALTY_FOR_VAMPIRES = builder.define("neutralizeConversionBitePenaltyForVampires", true);
        VAMPIRE_PLAYER_TRADE_DISCOUNT = builder.defineInRange("vampirePlayerTradeDiscount", 0.15D, 0.0D, 0.95D);
        HUNTER_TRADE_PRICE_MULTIPLIER = builder.defineInRange("hunterTradePriceMultiplier", 2.0D, 1.0D, 10.0D);
        WEREWOLF_TRADE_PRICE_MULTIPLIER = builder.defineInRange("werewolfTradePriceMultiplier", 1.75D, 1.0D, 10.0D);
        VAMPIRE_VILLAGERS_TREAT_VAMPIRES_AS_ALLIES = builder.define("vampireVillagersTreatVampiresAsAllies", true);
        VAMPIRE_VILLAGERS_TREAT_HUNTERS_AS_ENEMIES = builder.define("vampireVillagersTreatHuntersAsEnemies", true);
        VAMPIRE_VILLAGERS_TREAT_TRANSFORMED_WEREWOLVES_AS_ENEMIES = builder.define("vampireVillagersTreatTransformedWerewolvesAsEnemies", true);
        builder.pop();

        builder.push("factions");
        builder.comment("If true, MCA vampire villager commerce/social logic treats disguised vampire players as HUMAN_DISGUISED.");
        RESPECT_VAMPIRE_DISGUISE_FOR_MCA_INTERACTIONS = builder.define("respectVampireDisguiseForMcaInteractions", true);
        builder.comment("If true, disguised vampire players do not receive the vampire ally trade discount.");
        VAMPIRE_DISGUISE_SUPPRESSES_VAMPIRE_TRADE_DISCOUNT = builder.define("vampireDisguiseSuppressesVampireTradeDiscount", true);
        builder.comment("If true, disguised vampire players use the human social penalty for MCA vampire villagers.");
        VAMPIRE_DISGUISE_USES_HUMAN_SOCIAL_PENALTY = builder.define("vampireDisguiseUsesHumanSocialPenalty", true);
        builder.comment("If true, Vampirism's infect action uses 100% conversion chance for MCA villagers.");
        INFECTIOUS_BITE_OVERRIDES_CONVERSION_CHANCE = builder.define("infectiousBiteOverridesConversionChance", true);
        builder.comment("If true, Vampirism's infect action uses playerSanguinareDurationTicks for MCA villagers.");
        INFECTIOUS_BITE_STILL_USES_CONFIGURED_DURATION = builder.define("infectiousBiteStillUsesConfiguredDuration", true);
        builder.pop();

        builder.push("hunter");
        HUNTER_VILLAGERS_IMMUNE_TO_VAMPIRISM = builder.define("hunterVillagersImmuneToVampirism", true);
        HUNTER_VILLAGERS_HAVE_POISONOUS_BLOOD = builder.define("hunterVillagersHavePoisonousBlood", true);
        HUNTER_VILLAGERS_APPLY_TOXICANT_TO_VAMPIRE_BITERS = builder.define("hunterVillagersApplyToxicantToVampireBiters", true);
        HUNTER_VILLAGER_TOXICANT_DURATION_TICKS = builder.defineInRange("hunterVillagerToxicantDurationTicks", 200, 20, 72000);
        HUNTER_VILLAGER_TOXICANT_AMPLIFIER = builder.defineInRange("hunterVillagerToxicantAmplifier", 0, 0, 10);
        HUNTER_VILLAGER_FALLBACK_TO_VANILLA_POISON = builder.define("hunterVillagerFallbackToVanillaPoison", true);
        builder.comment("If true, MCA hunter-aligned villagers resist compat-controlled werewolf infection/conversion.");
        HUNTER_VILLAGERS_IMMUNE_TO_WEREWOLF_TRANSFORMATION = builder.define("hunterVillagersImmuneToWerewolfTransformation", true);
        builder.comment("If true, hunter-aligned MCA villagers target revealed vampire players. Disguised vampires remain safe unless hunterVillagersIgnoreVampireDisguise is enabled.");
        HUNTER_VILLAGERS_ATTACK_VAMPIRE_PLAYERS = builder.define("hunterVillagersAttackVampirePlayers", true);
        HUNTER_VILLAGERS_ATTACK_MCA_VAMPIRE_VILLAGERS = builder.define("hunterVillagersAttackMcaVampireVillagers", false);
        HUNTER_VILLAGERS_ATTACK_TRANSFORMED_WEREWOLVES = builder.define("hunterVillagersAttackTransformedWerewolves", true);
        HUNTER_VILLAGERS_ATTACK_VAMPIRE_MOBS = builder.define("hunterVillagersAttackVampireMobs", true);
        builder.comment("If true, hunters only acquire faction targets while defensive, in a hunter village, or retaliating. Default false lets them actively hunt revealed enemies within their normal range.");
        HUNTER_VILLAGERS_ATTACK_ONLY_WHILE_DEFENSIVE = builder.define("hunterVillagersAttackOnlyWhileDefensive", false);
        HUNTER_VILLAGERS_IGNORE_VAMPIRE_DISGUISE = builder.define("hunterVillagersIgnoreVampireDisguise", false);
        builder.comment("Base melee damage dealt by a hunter villager. Default 2.0 equals one heart before armor, resistance and other damage modifiers.");
        HUNTER_VILLAGER_ATTACK_DAMAGE = builder.defineInRange("hunterVillagerAttackDamage", 2.0D, 0.0D, 20.0D);
        builder.comment("Chance for a real Hunter villager to receive a Vampirism stake instead of a pitchfork. The chosen weapon is persisted per villager.");
        HUNTER_VILLAGER_STAKE_CHANCE = builder.defineInRange("hunterVillagerStakeChance", 0.25D, 0.0D, 1.0D);
        HUNTER_VILLAGER_ATTACK_COOLDOWN_TICKS = builder.defineInRange("hunterVillagerAttackCooldownTicks", 35, 1, 72000);
        HUNTER_VILLAGER_FOLLOW_RANGE = builder.defineInRange("hunterVillagerFollowRange", 24.0D, 1.0D, 128.0D);
        builder.comment("If true, hunter-controlled villages grant MCA villagers the addon hunter trait and hunter alignment.");
        HUNTER_VILLAGE_CONTROL_GRANTS_HUNTER_TRAIT = builder.define("hunterVillageControlGrantsHunterTrait", true);
        builder.comment("If true, MCA hunter villagers are created from hunter-controlled villages, not from generic vampire outbreak aggressive replacement attempts.");
        HUNTER_VILLAGERS_ONLY_FROM_HUNTER_CONTROLLED_VILLAGES = builder.define("hunterVillagersOnlyFromHunterControlledVillages", true);
        builder.comment("If true, hunter trait added by hunter village control is removed when that control is lost.");
        HUNTER_VILLAGER_REMOVE_TRAIT_WHEN_VILLAGE_CONTROL_LOST = builder.define("hunterVillagerRemoveTraitWhenVillageControlLost", true);
        builder.comment("If true, an existing/manual hunter trait is preserved when hunter village control is lost.");
        HUNTER_VILLAGER_KEEP_MANUAL_HUNTER_TRAIT = builder.define("hunterVillagerKeepManualHunterTrait", true);
        builder.comment("If true, poisonous hunter blood avoids repeatedly stacking/refreshing Toxicant when an equivalent effect is already active.");
        HUNTER_VILLAGER_AVOID_DUPLICATE_TOXICANT = builder.define("hunterVillagerAvoidDuplicateToxicant", true);
        HUNTER_VILLAGER_MAX_CHASE_DISTANCE_FROM_VILLAGE = builder.defineInRange("hunterVillagerMaxChaseDistanceFromVillage", 32, 8, 256);
        HUNTER_VILLAGER_MAX_CHASE_DISTANCE_FROM_HOME = builder.defineInRange("hunterVillagerMaxChaseDistanceFromHome", 24, 8, 256);
        HUNTER_VILLAGER_RETALIATION_MEMORY_TICKS = builder.defineInRange("hunterVillagerRetaliationMemoryTicks", 600, 20, 72000);
        HUNTER_VILLAGER_PREFER_RECENT_VAMPIRE_AGGRESSORS = builder.define("hunterVillagerPreferRecentVampireAggressors", true);
        builder.pop();

        builder.push("hunter");
        builder.comment("If true, MCA hunter-aligned villagers trade with hunter players and apply hunterVillagerHunterDiscount.");
        HUNTER_VILLAGERS_TRADE_WITH_HUNTERS = builder.define("hunterVillagersTradeWithHunters", true);
        builder.comment("If true, MCA hunter-aligned villagers trade with human or unknown players at human prices.");
        HUNTER_VILLAGERS_TRADE_WITH_HUMANS = builder.define("hunterVillagersTradeWithHumans", true);
        builder.comment("If true, undisguised vampire players are blocked from trading with MCA hunter-aligned villagers.");
        HUNTER_VILLAGERS_BLOCK_VAMPIRE_TRADING = builder.define("hunterVillagersBlockVampireTrading", true);
        builder.comment("If true, transformed werewolf players are blocked from trading with MCA hunter-aligned villagers.");
        HUNTER_VILLAGERS_BLOCK_TRANSFORMED_WEREWOLF_TRADING = builder.define("hunterVillagersBlockTransformedWerewolfTrading", true);
        builder.comment("Discount given to hunter players. 0.15 means hunter prices are 15 percent cheaper.");
        HUNTER_VILLAGER_HUNTER_DISCOUNT = builder.defineInRange("hunterVillagerHunterDiscount", 0.15D, 0.0D, 0.95D);
        HUNTER_VILLAGER_HUMAN_PRICE_MULTIPLIER = builder.defineInRange("hunterVillagerHumanPriceMultiplier", 1.0D, 0.05D, 10.0D);
        HUNTER_VILLAGER_VAMPIRE_PRICE_MULTIPLIER = builder.defineInRange("hunterVillagerVampirePriceMultiplier", 1.75D, 0.05D, 10.0D);
        HUNTER_VILLAGER_WEREWOLF_PRICE_MULTIPLIER = builder.defineInRange("hunterVillagerWerewolfPriceMultiplier", 1.5D, 0.05D, 10.0D);
        builder.comment("If true, disguised vampire players may trade with MCA hunter-aligned villagers as long as human trading is allowed.");
        HUNTER_VILLAGER_DISGUISED_VAMPIRE_CAN_TRADE = builder.define("hunterVillagerDisguisedVampireCanTrade", true);
        builder.comment("If true, disguised vampire players use human prices when trading with MCA hunter-aligned villagers.");
        HUNTER_VILLAGER_DISGUISED_VAMPIRE_USES_HUMAN_PRICES = builder.define("hunterVillagerDisguisedVampireUsesHumanPrices", true);
        builder.pop();

        builder.push("hunter");
        builder.comment("If true, undisguised vampire players cannot perform romantic or marriage actions with MCA hunter-aligned villagers.");
        HUNTER_VILLAGERS_BLOCK_VAMPIRE_ROMANCE = builder.define("hunterVillagersBlockVampireRomance", true);
        builder.comment("If true, transformed werewolf players cannot perform romantic or marriage actions with MCA hunter-aligned villagers.");
        HUNTER_VILLAGERS_BLOCK_TRANSFORMED_WEREWOLF_ROMANCE = builder.define("hunterVillagersBlockTransformedWerewolfRomance", true);
        HUNTER_VILLAGER_VAMPIRE_SOCIAL_PENALTY = builder.defineInRange("hunterVillagerVampireSocialPenalty", 0.50D, 0.0D, 1.0D);
        HUNTER_VILLAGER_WEREWOLF_SOCIAL_PENALTY = builder.defineInRange("hunterVillagerWerewolfSocialPenalty", 0.25D, 0.0D, 1.0D);
        HUNTER_VILLAGER_HUMAN_SOCIAL_PENALTY = builder.defineInRange("hunterVillagerHumanSocialPenalty", 0.0D, 0.0D, 1.0D);
        HUNTER_VILLAGER_HUNTER_SOCIAL_PENALTY = builder.defineInRange("hunterVillagerHunterSocialPenalty", 0.0D, 0.0D, 1.0D);
        builder.comment("If true, disguised vampire players use human social rules with MCA hunter-aligned villagers.");
        HUNTER_VILLAGER_DISGUISED_VAMPIRE_USES_HUMAN_SOCIAL_RULES = builder.define("hunterVillagerDisguisedVampireUsesHumanSocialRules", true);
        builder.comment("If true, disguised vampire players may perform romance as humans when human social rules apply.");
        HUNTER_VILLAGER_DISGUISED_VAMPIRE_CAN_ROMANCE = builder.define("hunterVillagerDisguisedVampireCanRomance", true);
        builder.pop();

        builder.push("vampire");
        builder.comment("If true, MCA vampire villagers can be safely cured without replacing their VillagerEntityMCA entity.");
        ENABLE_MCA_VAMPIRE_VILLAGER_CURE = builder.define("enableMcaVampireVillagerCure", true);
        builder.comment("If true, giving milk to an infected, not-yet-converted MCA villager removes Sanguinare and stops conversion.");
        ALLOW_MILK_TO_TREAT_MCA_VILLAGER_SANGUINARE = builder.define("allowMilkToTreatMcaVillagerSanguinare", true);
        MILK_TREATMENT_REMOVES_NEGATIVE_EFFECTS = builder.define("milkTreatmentRemovesNegativeEffects", true);
        MILK_TREATMENT_CONSUMES_MILK = builder.define("milkTreatmentConsumesMilk", true);
        MILK_TREATMENT_RETURNS_BUCKET = builder.define("milkTreatmentReturnsBucket", true);
        builder.comment("If true, milk only treats infection before conversion and does not cure converted vampire villagers.");
        MILK_TREATMENT_ONLY_BEFORE_CONVERSION = builder.define("milkTreatmentOnlyBeforeConversion", true);
        VAMPIRE_CURE_REQUIRES_WEAKNESS = builder.define("vampireCureRequiresWeakness", true);
        VAMPIRE_CURE_REQUIRES_GOLDEN_APPLE = builder.define("vampireCureRequiresGoldenApple", true);
        VAMPIRE_CURE_REQUIRES_GARLIC_INJECTION = builder.define("vampireCureRequiresGarlicInjection", true);
        builder.comment("Weakness 4:00 is 4800 ticks. Direct potion use accepts a valid long weakness potion and saves short progress.");
        VAMPIRE_CURE_REQUIRED_WEAKNESS_DURATION_TICKS = builder.defineInRange("vampireCureRequiredWeaknessDurationTicks", 4800, 20, 72000);
        VAMPIRE_CURE_ALLOW_ANY_WEAKNESS_DURATION = builder.define("vampireCureAllowAnyWeaknessDuration", false);
        VAMPIRE_CURE_INGREDIENT_PROGRESS_EXPIRES_TICKS = builder.defineInRange("vampireCureIngredientProgressExpiresTicks", 6000, 20, 72000);
        VAMPIRE_CURE_DURATION_TICKS = builder.defineInRange("vampireCureDurationTicks", 3600, 20, 72000);
        VAMPIRE_CURE_VILLAGER_CANNOT_ATTACK = builder.define("vampireCureVillagerCannotAttack", true);
        VAMPIRE_CURE_VILLAGER_CANNOT_TRADE = builder.define("vampireCureVillagerCannotTrade", true);
        VAMPIRE_CURE_SHOWS_PARTICLES = builder.define("vampireCureShowsParticles", true);
        VAMPIRE_CURE_APPLIES_WEAKNESS_DURING_PROCESS = builder.define("vampireCureAppliesWeaknessDuringProcess", true);
        builder.comment("Ticks after a completed cure where stale trait/appearance markers cannot restore vampire or werewolf state.");
        POST_CURE_RESYNC_BLOCK_TICKS = builder.defineInRange("postCureResyncBlockTicks", 400, 20, 72000);
        builder.pop();

        builder.push("easy_villagers");
        builder.comment("If true, this mod may apply vampire/hunter/werewolf faction rules to Easy Villagers surfaces.");
        ENABLE_EASY_VILLAGERS_FACTION_RULES = builder.define("enableEasyVillagersFactionRules", true);
        builder.comment("If true, this mod may deny Easy Villagers pickup for MCA villagers from enemy factions. Base capture and restore remain delegated to mca-ev-compat.");
        ENABLE_EASY_VILLAGERS_FACTION_PICKUP_RULES = builder.define("enableEasyVillagersFactionPickupRules", true);
        builder.comment("If true, MCA villagers currently being cured cannot be picked up through Easy Villagers.");
        EASY_VILLAGERS_BLOCK_PICKUP_OF_CURING_VILLAGERS = builder.define("blockPickupOfCuringVillagers", true);
        builder.comment("If true, human MCA villagers with Sanguinare/Lupus progress can still be picked up when faction rules otherwise allow human pickup.");
        ALLOW_PICKUP_OF_INFECTED_HUMAN_VILLAGERS = builder.define("allowPickupOfInfectedHumanVillagers", true);
        builder.comment("If true, players receive a short action-bar message when faction pickup rules deny pickup.");
        SHOW_PICKUP_BLOCKED_MESSAGE = builder.define("showPickupBlockedMessage", true);
        builder.pop();

        builder.push("werewolf");
        builder.comment("If true, transformed werewolf attacks can apply a compat-controlled Lupus Sanguinem infection to MCA villagers without replacing their entity.");
        ALLOW_MCA_VILLAGER_WEREWOLF_INFECTION = builder.define("allowMcaVillagerWerewolfInfection", true);
        ALLOW_MCA_CHILD_WEREWOLF_INFECTION = builder.define("allowMcaChildWerewolfInfection", false);
        WEREWOLF_CONVERSION_CHANCE_ON_BITE = builder.defineInRange("werewolfConversionChanceOnBite", 0.45D, 0.0D, 1.0D);
        WEREWOLF_VILLAGER_INFECTION_DURATION_TICKS = builder.defineInRange("werewolfVillagerInfectionDurationTicks", 24000, 20, 72000);
        builder.comment("If true, MCA vampire villagers cannot receive compat-controlled werewolf infection.");
        VAMPIRE_VILLAGERS_IMMUNE_TO_WEREWOLF_TRANSFORMATION = builder.define("vampireVillagersImmuneToWerewolfTransformation", true);
        builder.comment("If true, MCA werewolf villagers cannot receive Sanguinare or become MCA vampires.");
        WEREWOLF_VILLAGERS_IMMUNE_TO_VAMPIRISM = builder.define("werewolfVillagersImmuneToVampirism", true);
        builder.comment("If true, MCA werewolf villagers are not converted into hunter-aligned villagers by village/defensive systems.");
        WEREWOLF_VILLAGERS_IMMUNE_TO_HUNTER_CONVERSION = builder.define("werewolfVillagersImmuneToHunterConversion", true);
        ONE_WEREWOLF_PARENT_INHERITANCE_CHANCE = builder.defineInRange("oneWerewolfParentInheritanceChance", 0.45D, 0.0D, 1.0D);
        TWO_WEREWOLF_PARENTS_INHERITANCE_CHANCE = builder.defineInRange("twoWerewolfParentsInheritanceChance", 1.0D, 0.0D, 1.0D);
        builder.pop();

        builder.push("werewolf");
        WEREWOLF_VILLAGER_APPEARANCE_ENABLED = builder.define("werewolfVillagerAppearanceEnabled", true);
        WEREWOLF_VILLAGER_EARS_ENABLED = builder.define("werewolfVillagerEarsEnabled", true);
        WEREWOLF_VILLAGER_CLAWS_ENABLED = builder.define("werewolfVillagerClawsEnabled", true);
        WEREWOLF_VILLAGER_APPEARANCE_DEBUG = builder.define("werewolfVillagerAppearanceDebug", false);
        builder.pop();

        builder.push("werewolf");
        WEREWOLF_VILLAGERS_TRADE_WITH_WEREWOLVES = builder.define("werewolfVillagersTradeWithWerewolves", true);
        WEREWOLF_VILLAGERS_TRADE_WITH_HUMANS = builder.define("werewolfVillagersTradeWithHumans", true);
        WEREWOLF_VILLAGERS_BLOCK_VAMPIRE_TRADING = builder.define("werewolfVillagersBlockVampireTrading", true);
        WEREWOLF_VILLAGERS_BLOCK_HUNTER_TRADING = builder.define("werewolfVillagersBlockHunterTrading", true);
        WEREWOLF_VILLAGER_WEREWOLF_DISCOUNT = builder.defineInRange("werewolfVillagerWerewolfDiscount", 0.15D, 0.0D, 0.95D);
        WEREWOLF_VILLAGER_HUMAN_PRICE_MULTIPLIER = builder.defineInRange("werewolfVillagerHumanPriceMultiplier", 1.0D, 0.05D, 10.0D);
        WEREWOLF_VILLAGER_VAMPIRE_PRICE_MULTIPLIER = builder.defineInRange("werewolfVillagerVampirePriceMultiplier", 1.75D, 0.05D, 10.0D);
        WEREWOLF_VILLAGER_HUNTER_PRICE_MULTIPLIER = builder.defineInRange("werewolfVillagerHunterPriceMultiplier", 1.5D, 0.05D, 10.0D);
        WEREWOLF_VILLAGER_DISGUISED_VAMPIRE_USES_HUMAN_PRICES = builder.define("werewolfVillagerDisguisedVampireUsesHumanPrices", true);
        WEREWOLF_VILLAGER_DISGUISED_VAMPIRE_CAN_TRADE = builder.define("werewolfVillagerDisguisedVampireCanTrade", true);
        builder.pop();

        builder.push("werewolf");
        WEREWOLF_VILLAGERS_BLOCK_VAMPIRE_ROMANCE = builder.define("werewolfVillagersBlockVampireRomance", true);
        WEREWOLF_VILLAGERS_BLOCK_HUNTER_ROMANCE = builder.define("werewolfVillagersBlockHunterRomance", true);
        WEREWOLF_VILLAGER_VAMPIRE_SOCIAL_PENALTY = builder.defineInRange("werewolfVillagerVampireSocialPenalty", 0.50D, 0.0D, 1.0D);
        WEREWOLF_VILLAGER_HUNTER_SOCIAL_PENALTY = builder.defineInRange("werewolfVillagerHunterSocialPenalty", 0.35D, 0.0D, 1.0D);
        WEREWOLF_VILLAGER_HUMAN_SOCIAL_PENALTY = builder.defineInRange("werewolfVillagerHumanSocialPenalty", 0.0D, 0.0D, 1.0D);
        WEREWOLF_VILLAGER_WEREWOLF_SOCIAL_PENALTY = builder.defineInRange("werewolfVillagerWerewolfSocialPenalty", 0.0D, 0.0D, 1.0D);
        WEREWOLF_VILLAGER_DISGUISED_VAMPIRE_USES_HUMAN_SOCIAL_RULES = builder.define("werewolfVillagerDisguisedVampireUsesHumanSocialRules", true);
        WEREWOLF_VILLAGER_DISGUISED_VAMPIRE_CAN_ROMANCE = builder.define("werewolfVillagerDisguisedVampireCanRomance", true);
        builder.pop();

        builder.push("werewolf");
        ENABLE_MCA_WEREWOLF_VILLAGER_AI = builder.define("enableMcaWerewolfVillagerAi", true);
        WEREWOLF_VILLAGERS_ATTACK_VAMPIRE_PLAYERS = builder.define("werewolfVillagersAttackVampirePlayers", true);
        WEREWOLF_VILLAGERS_ATTACK_HUNTER_PLAYERS = builder.define("werewolfVillagersAttackHunterPlayers", true);
        WEREWOLF_VILLAGERS_ATTACK_VAMPIRE_MOBS = builder.define("werewolfVillagersAttackVampireMobs", true);
        WEREWOLF_VILLAGERS_ATTACK_HUNTER_NPCS = builder.define("werewolfVillagersAttackHunterNpcs", true);
        WEREWOLF_VILLAGERS_RESPECT_VAMPIRE_DISGUISE = builder.define("werewolfVillagersRespectVampireDisguise", true);
        WEREWOLF_VILLAGERS_ATTACK_ONLY_AT_NIGHT = builder.define("werewolfVillagersAttackOnlyAtNight", false);
        WEREWOLF_VILLAGER_ATTACK_DAMAGE = builder.defineInRange("werewolfVillagerAttackDamage", 3.0D, 0.0D, 20.0D);
        WEREWOLF_VILLAGER_ATTACK_COOLDOWN_TICKS = builder.defineInRange("werewolfVillagerAttackCooldownTicks", 35, 1, 72000);
        WEREWOLF_VILLAGER_FOLLOW_RANGE = builder.defineInRange("werewolfVillagerFollowRange", 24.0D, 1.0D, 128.0D);
        WEREWOLF_VILLAGER_MAX_CHASE_DISTANCE_FROM_HOME = builder.defineInRange("werewolfVillagerMaxChaseDistanceFromHome", 24.0D, 1.0D, 256.0D);
        WEREWOLF_VILLAGER_TARGET_MEMORY_TICKS = builder.defineInRange("werewolfVillagerTargetMemoryTicks", 200, 20, 72000);
        builder.pop();

        builder.push("balance");
        SPECIAL_VILLAGER_TARGET_SCAN_INTERVAL_TICKS = builder.defineInRange("specialVillagerTargetScanIntervalTicks", 40, 5, 72000);
        SPECIAL_VILLAGER_RETALIATION_MEMORY_TICKS = builder.defineInRange("specialVillagerRetaliationMemoryTicks", 600, 20, 72000);
        SPECIAL_VILLAGER_MAX_CHASE_DISTANCE = builder.defineInRange("specialVillagerMaxChaseDistance", 20, 4, 256);
        SPECIAL_VILLAGER_ATTACK_COOLDOWN_TICKS = builder.defineInRange("specialVillagerAttackCooldownTicks", 35, 1, 72000);
        builder.comment("Minimum ticks between combat path recalculations. Higher values reduce jitter but make chases less reactive.");
        SPECIAL_VILLAGER_COMBAT_REPATH_INTERVAL_TICKS = builder.defineInRange("specialVillagerCombatRepathIntervalTicks", 10, 1, 100);
        builder.comment("If true, MCA special villagers may automatically target non-MCA faction NPCs/mobs from Vampirism/Werewolves. If false, automatic targeting stays focused on MCA special villagers and enemy players. Retaliation against an NPC attacker is still allowed.");
        SPECIAL_VILLAGERS_TARGET_FACTION_NPCS = builder.define("specialVillagersTargetFactionNpcs", false);
        builder.pop();

        builder.push("werewolf");
        builder.comment("If true, MCA werewolf villagers can be cured with Weakness, golden apple and garlic injection.");
        ENABLE_MCA_WEREWOLF_VILLAGER_CURE = builder.define("enableMcaWerewolfVillagerCure", true);
        WEREWOLF_CURE_REQUIRES_WEAKNESS = builder.define("werewolfCureRequiresWeakness", true);
        WEREWOLF_CURE_REQUIRES_GOLDEN_APPLE = builder.define("werewolfCureRequiresGoldenApple", true);
        WEREWOLF_CURE_REQUIRES_GARLIC_INJECTION = builder.define("werewolfCureRequiresGarlicInjection", true);
        WEREWOLF_CURE_REQUIRED_WEAKNESS_DURATION_TICKS = builder.defineInRange("werewolfCureRequiredWeaknessDurationTicks", 4800, 20, 72000);
        WEREWOLF_CURE_ALLOW_ANY_WEAKNESS_DURATION = builder.define("werewolfCureAllowAnyWeaknessDuration", false);
        WEREWOLF_CURE_INGREDIENT_PROGRESS_EXPIRES_TICKS = builder.defineInRange("werewolfCureIngredientProgressExpiresTicks", 6000, 20, 72000);
        WEREWOLF_CURE_DURATION_TICKS = builder.defineInRange("werewolfCureDurationTicks", 3600, 20, 72000);
        WEREWOLF_CURE_VILLAGER_CANNOT_ATTACK = builder.define("werewolfCureVillagerCannotAttack", true);
        WEREWOLF_CURE_VILLAGER_CANNOT_TRADE = builder.define("werewolfCureVillagerCannotTrade", true);
        WEREWOLF_CURE_SHOWS_PARTICLES = builder.define("werewolfCureShowsParticles", true);
        WEREWOLF_CURE_APPLIES_WEAKNESS_DURING_PROCESS = builder.define("werewolfCureAppliesWeaknessDuringProcess", true);
        builder.pop();

        builder.push("village_capture");
        builder.comment("If true, Vampirism village capture events classify MCA villagers and apply safe MCA-specific results without replacing entities.");
        ENABLE_MCA_VILLAGE_CAPTURE_INTEGRATION = builder.define("enableMcaVillageCaptureIntegration", true);
        MCA_HUMAN_VILLAGER_DEFENSE_STRENGTH = builder.defineInRange("mcaHumanVillagerDefenseStrength", 0.5D, 0.0D, 20.0D);
        MCA_HUNTER_VILLAGER_DEFENSE_STRENGTH = builder.defineInRange("mcaHunterVillagerDefenseStrength", 2.0D, 0.0D, 20.0D);
        MCA_VAMPIRE_VILLAGER_VAMPIRE_STRENGTH = builder.defineInRange("mcaVampireVillagerVampireStrength", 2.0D, 0.0D, 20.0D);
        MCA_INFECTED_VILLAGER_DEFENSE_STRENGTH = builder.defineInRange("mcaInfectedVillagerDefenseStrength", 0.25D, 0.0D, 20.0D);
        MCA_CURING_VILLAGER_DEFENSE_STRENGTH = builder.defineInRange("mcaCuringVillagerDefenseStrength", 0.0D, 0.0D, 20.0D);
        MCA_CHILD_VILLAGER_DEFENSE_STRENGTH = builder.defineInRange("mcaChildVillagerDefenseStrength", 0.0D, 0.0D, 20.0D);
        MCA_VILLAGE_CAPTURE_STRENGTH_RADIUS = builder.defineInRange("mcaVillageCaptureStrengthRadius", 48, 8, 256);
        MCA_VILLAGE_CAPTURE_DEBUG_BREAKDOWN = builder.define("mcaVillageCaptureDebugBreakdown", false);
        builder.comment("If true, MakeAggressive creates safe MCA hunter state only in hunter-controlled/captured village contexts.");
        MAKE_AGGRESSIVE_ONLY_CREATES_MCA_HUNTERS_IN_HUNTER_VILLAGES = builder.define("makeAggressiveOnlyCreatesMcaHuntersInHunterVillages", true);
        builder.comment("Vampire village capture can infect MCA humans with Sanguinare, but never grants vampire trait or converts instantly.");
        VAMPIRE_VILLAGE_CAPTURE_INFECTS_MCA_VILLAGERS = builder.define("vampireVillageCaptureInfectsMcaVillagers", true);
        VAMPIRE_VILLAGE_CAPTURE_INFECTION_CHANCE = builder.defineInRange("vampireVillageCaptureInfectionChance", 0.35D, 0.0D, 1.0D);
        VAMPIRE_VILLAGE_CAPTURE_INFECTS_CHILDREN = builder.define("vampireVillageCaptureInfectsChildren", false);
        VAMPIRE_VILLAGE_CAPTURE_CAN_INFECT_HUNTER_VILLAGERS = builder.define("vampireVillageCaptureCanInfectHunterVillagers", false);
        VAMPIRE_VILLAGE_CAPTURE_ACCELERATES_EXISTING_SANGUINARE = builder.define("vampireVillageCaptureAcceleratesExistingSanguinare", true);
        VAMPIRE_VILLAGE_CAPTURE_ACCELERATION_MULTIPLIER = builder.defineInRange("vampireVillageCaptureAccelerationMultiplier", 0.5D, 0.05D, 10.0D);
        VAMPIRE_VILLAGE_CAPTURE_DOES_NOT_INSTANTLY_CONVERT_MCA_VILLAGERS = builder.define("vampireVillageCaptureDoesNotInstantlyConvertMcaVillagers", true);
        builder.comment("Hunter capture grants hunter state to MCA humans and can cleanse Sanguinare. Converted MCA vampires are not cured by default.");
        HUNTER_VILLAGE_CAPTURE_GRANTS_HUNTER_TRAIT = builder.define("hunterVillageCaptureGrantsHunterTrait", true);
        HUNTER_VILLAGE_CAPTURE_CURES_MCA_SANGUINARE = builder.define("hunterVillageCaptureCuresMcaSanguinare", true);
        HUNTER_VILLAGE_CAPTURE_CURES_MCA_VAMPIRES = builder.define("hunterVillageCaptureCuresMcaVampires", false);
        HUNTER_VILLAGE_CAPTURE_STARTS_CURE_FOR_MCA_VAMPIRES = builder.define("hunterVillageCaptureStartsCureForMcaVampires", false);
        builder.comment("If true, werewolf-aligned MCA villagers resist vampire village capture infection.");
        WEREWOLF_VILLAGERS_RESIST_VAMPIRE_CAPTURE_INFECTION = builder.define("werewolfVillagersResistVampireCaptureInfection", true);
        builder.comment("If true, hunter villages do not convert werewolf-aligned MCA villagers to hunters.");
        HUNTER_VILLAGE_DOES_NOT_CONVERT_WEREWOLVES_TO_HUNTERS = builder.define("hunterVillageDoesNotConvertWerewolvesToHunters", true);
        builder.pop();

        builder.push("vampire");
        TWO_VAMPIRE_PARENTS_INHERITANCE_CHANCE = builder.defineInRange("twoVampireParentsInheritanceChance", 1.0D, 0.0D, 1.0D);
        ONE_VAMPIRE_PARENT_INHERITANCE_CHANCE = builder.defineInRange("oneVampireParentInheritanceChance", 0.45D, 0.0D, 1.0D);
        VAMPIRE_BABY_GROWTH_MULTIPLIER = builder.defineInRange("vampireBabyGrowthMultiplier", 1.5D, 1.0D, 10.0D);
        GROWTH_PROCESS_INTERVAL_TICKS = builder.defineInRange("growthProcessIntervalTicks", 1200, 20, 24000);
        ALLOW_VAMPIRE_CHILDREN_GROWTH_BOOST = builder.define("allowVampireChildrenGrowthBoost", true);
        builder.pop();

        builder.push("vampire");
        ENABLE_PERSISTENT_VAMPIRE_APPEARANCE = builder.define("enablePersistentVampireAppearance", true);
        PALE_SKIN_ENABLED = builder.define("paleSkinEnabled", true);
        PRESERVE_ORIGINAL_APPEARANCE_FOR_CURE = builder.define("preserveOriginalAppearanceForCure", true);
        builder.comment("Master toggle for client-side vampire eye visuals on MCA vampire villagers.");
        VAMPIRE_EYES_ENABLED = builder.define("vampireEyesEnabled", true);
        builder.pop();

        builder.push("advanced");
        ENABLE_MCA_VAMPIRE_TRAIT = builder.define("enableMcaVampireTrait", true);
        ENABLE_MCA_WEREWOLF_TRAIT = builder.define("enableMcaWerewolfTrait", true);
        SYNC_TRAIT_AND_CAPABILITY_ON_LOAD = builder.define("syncTraitAndCapabilityOnLoad", true);
        builder.comment("Migrates the Forge 1.20.1 capability payload from existing MCA villagers on first load.");
        ENABLE_LEGACY_103_MIGRATION = builder.define("enableLegacy103Migration", true);
        TRAIT_CAN_RESTORE_CAPABILITY = builder.define("traitCanRestoreCapability", true);
        VAMPIRE_TRAIT_CONTROLS_INHERITANCE = builder.define("vampireTraitControlsInheritance", true);
        ONE_VAMPIRE_PARENT_TRAIT_INHERITANCE_CHANCE = builder.defineInRange("oneVampireParentTraitInheritanceChance", 0.45D, 0.0D, 1.0D);
        TWO_VAMPIRE_PARENTS_TRAIT_INHERITANCE_CHANCE = builder.defineInRange("twoVampireParentsTraitInheritanceChance", 1.0D, 0.0D, 1.0D);
        builder.pop();

        builder.push("mca_commands");
        builder.comment("If true, special vampire/hunter/werewolf AI reads MCA movement commands and avoids overriding explicit player orders.");
        builder.comment("Protect MCA spouses, parents, children, siblings and player friends from automatic combat. Confirmed aggression permits retaliation.");
        RESPECT_MCA_RELATIONSHIPS_IN_COMBAT = builder.define("respectMcaRelationshipsInCombat", false);
        RESPECT_MCA_PLAYER_COMMANDS = builder.define("respectMcaPlayerCommands", true);
        builder.comment("If true, special villagers do not start autonomous combat while following a player through MCA commands.");
        SUPPRESS_AUTONOMOUS_COMBAT_WHILE_FOLLOWING = builder.define("suppressAutonomousCombatWhileFollowing", true);
        builder.comment("If true, special villagers do not start autonomous combat while staying through MCA commands.");
        SUPPRESS_AUTONOMOUS_COMBAT_WHILE_STAYING = builder.define("suppressAutonomousCombatWhileStaying", true);
        builder.comment("If true, a following special villager may briefly retaliate against a nearby attacker.");
        ALLOW_SHORT_RETALIATION_WHILE_FOLLOWING = builder.define("allowShortRetaliationWhileFollowing", true);
        builder.comment("If true, a staying special villager may briefly retaliate against a nearby attacker.");
        ALLOW_SHORT_RETALIATION_WHILE_STAYING = builder.define("allowShortRetaliationWhileStaying", false);
        COMMANDED_RETALIATION_MAX_DISTANCE = builder.defineInRange("commandedRetaliationMaxDistance", 4.0D, 0.0D, 64.0D);
        COMMANDED_RETALIATION_TICKS = builder.defineInRange("commandedRetaliationTicks", 100, 1, 72000);
        CLEAR_AUTONOMOUS_TARGET_WHEN_COMMANDED = builder.define("clearAutonomousTargetWhenCommanded", true);
        builder.pop();

        builder.push("mca_commands");
        builder.comment("If true, MCA movement/work/home commands are accepted only from compatible factions.");
        ENABLE_FACTION_COMMAND_RESTRICTIONS = builder.define("enableFactionCommandRestrictions", true);
        SHOW_COMMAND_BLOCKED_MESSAGE = builder.define("showCommandBlockedMessage", true);
        builder.comment("If true, vampire/hunter/werewolf players may still command normal human MCA villagers.");
        ALLOW_SPECIAL_PLAYERS_COMMAND_HUMANS = builder.define("allowSpecialPlayersCommandHumans", true);
        ALLOW_COMMANDS_FOR_INFECTED_HUMANS = builder.define("allowCommandsForInfectedHumans", true);
        ALLOW_COMMANDS_FOR_CURING_VILLAGERS = builder.define("allowCommandsForCuringVillagers", false);
        BLOCK_UNKNOWN_MCA_COMMANDS_FOR_ENEMY_FACTIONS = builder.define("blockUnknownMcaCommandsForEnemyFactions", true);
        builder.pop();

        builder.push("guide_book");
        builder.comment("Gives the Book of Occult Arts once when a player first enters an active vampire, hunter or werewolf-controlled village.");
        GIVE_OCCULT_ARTS_BOOK_ON_SPECIAL_VILLAGE_DISCOVERY =
                builder.define("giveOccultArtsBookOnSpecialVillageDiscovery", true);
        builder.pop();

        builder.push("village_missions");
        builder.push("rewards");
        for (var type : com.guilh.mca_vampirism_compat.mission.OccultVillageMissionType.values()) {
            MISSION_REWARDS.put(type, builder.defineInRange(type.name().toLowerCase(java.util.Locale.ROOT), type.defaultReward(), 0, 100));
        }
        builder.pop();
        builder.comment("If true, eligible residents of special villages can offer supplies or urgent aid through MCA conversations.");
        ENABLE_OCCULT_VILLAGE_MISSIONS = builder.define("enableOccultVillageMissions", true);
        builder.comment("Minimum time between village requests for the same player and totem. 24000 ticks is one Minecraft day.");
        OCCULT_VILLAGE_MISSION_COOLDOWN_TICKS = builder.defineInRange(
                "occultVillageMissionCooldownTicks", 24000, 1200, 240000
        );
        builder.comment("Time allowed to finish an accepted village request. 48000 ticks is two Minecraft days. Expired or canceled requests begin a new cooldown.");
        OCCULT_VILLAGE_MISSION_DEADLINE_TICKS = builder.defineInRange(
                "occultVillageMissionDeadlineTicks", 48000, 1200, 480000
        );
        builder.pop();

        builder.push("mcaVillageEffects");
        builder.comment("If true, vampire fog and compat-owned special-village effects follow the editable MCA village bounds when the totem is inside that village. Vampirism capture, raids and spawning stay native.");
        MCA_VILLAGE_EFFECTS_FOLLOW_MCA_BOUNDS = builder.define("mcaVillageEffectsFollowMcaBounds", true);
        builder.comment("Horizontal fog-only margin beyond an MCA village border. 0 ends fog at the final MCA village block; Hunter control, book discovery and outlines remain on the exact MCA border.");
        MCA_VILLAGE_FOG_BORDER_MARGIN_BLOCKS = builder.defineInRange("mcaVillageFogBorderMarginBlocks", 4, 0, 64);
        builder.pop();

        builder.push("dialogues");
        builder.comment("If true, short faction-themed lines accompany existing MCA conversations, faction refusals and cure milestones. These lines never change gameplay outcomes.");
        ENABLE_FACTION_CONTEXTUAL_DIALOGUES = builder.define("enableFactionContextualDialogues", true);
        builder.pop();

        builder.push("gifts");
        builder.comment("If true, Vampire, Hunter and Werewolf villagers use their faction-specific gift tastes. Gifts still use MCA's native saturation and relationship rules.");
        ENABLE_FACTION_GIFT_PREFERENCES = builder.define("enableFactionGiftPreferences", true);
        builder.comment("If true, Vampire and Werewolf villagers use their faction-specific recovery diets. Humans and Hunters keep MCA's normal recovery food.");
        ENABLE_FACTION_RECOVERY_DIET = builder.define("enableFactionRecoveryDiet", true);
        builder.pop();

        builder.push("debug");
        builder.comment("Enables compact diagnostic logs for trade, faction, bite, state and Easy Villagers integration.");
        ENABLE_DEBUG_LOGS = builder.define("enableDebugLogs", false);
        builder.comment("Very verbose AI target-candidate diagnostics. Normal debug logs still include selected/dropped targets, attacks and retaliation.");
        ENABLE_VERBOSE_AI_DEBUG_LOGS = builder.define("enableVerboseAiDebugLogs", false);
        builder.comment("Deep Easy Villagers stored-villager NBT diagnostics. Very verbose; only enable when diagnosing NBT persistence issues.");
        ENABLE_VERBOSE_NBT_DEBUG_LOGS = builder.define("enableVerboseNbtDebugLogs", false);
        builder.pop();

        SPEC = builder.build();
    }

    private McaVampirismCompatConfig() {
    }

    public static int playerSanguinareDurationTicks() { return PLAYER_SANGUINARE_DURATION_TICKS.get(); }
    public static int vampireVillagerSanguinareDurationTicks() { return VAMPIRE_VILLAGER_SANGUINARE_DURATION_TICKS.get(); }
    public static boolean allowMcaVillagerInfection() { return ALLOW_MCA_VILLAGER_INFECTION.get(); }
    public static boolean allowChildInfection() { return ALLOW_CHILD_INFECTION.get(); }
    public static long biteCooldownTicks() { return BITE_COOLDOWN_TICKS.get(); }
    public static double nightAttackChance() { return NIGHT_ATTACK_CHANCE.get(); }
    public static double conversionChanceOnBite() { return CONVERSION_CHANCE_ON_BITE.get(); }
    public static boolean vampireVillagersUseNightSchedule() { return VAMPIRE_VILLAGERS_USE_NIGHT_SCHEDULE.get(); }
    public static boolean vampireVillagersAvoidSun() { return VAMPIRE_VILLAGERS_AVOID_SUN.get(); }
    public static boolean vampireVillagersBurnInSun() { return VAMPIRE_VILLAGERS_BURN_IN_SUN.get(); }
    public static boolean nonLethalBiteAgainstVillagers() { return NON_LETHAL_BITE_AGAINST_VILLAGERS.get(); }
    public static double villagerBiteDamage() { return VILLAGER_BITE_DAMAGE.get(); }
    public static long recentlyBittenTargetCooldownTicks() { return RECENTLY_BITTEN_TARGET_COOLDOWN_TICKS.get(); }
    public static boolean bloodDrainStopsCombat() { return BLOOD_DRAIN_STOPS_COMBAT.get(); }
    public static boolean preventVampiresBitingMcaVampires() { return PREVENT_VAMPIRES_BITING_MCA_VAMPIRES.get(); }
    public static boolean showAlreadyVampireBiteMessage() { return SHOW_ALREADY_VAMPIRE_BITE_MESSAGE.get(); }
    public static boolean vampireVillagersInfectHumansAtNight() { return VAMPIRE_VILLAGERS_INFECT_HUMANS_AT_NIGHT.get(); }
    public static boolean vampireVillagersUseNonLethalBiteOnHumans() { return VAMPIRE_VILLAGERS_USE_NON_LETHAL_BITE_ON_HUMANS.get(); }
    public static long retaliationMemoryTicks() { return RETALIATION_MEMORY_TICKS.get(); }
    public static boolean attackOnlyTransformedWerewolves() { return ATTACK_ONLY_TRANSFORMED_WEREWOLVES.get(); }
    public static double maxHealthMultiplier() { return MAX_HEALTH_MULTIPLIER.get(); }
    public static double attackDamageBonus() { return ATTACK_DAMAGE_BONUS.get(); }
    public static double armorBonus() { return ARMOR_BONUS.get(); }
    public static double followRangeBonus() { return FOLLOW_RANGE_BONUS.get(); }
    public static boolean applyCombatAttributesOnlyToAdults() { return APPLY_COMBAT_ATTRIBUTES_ONLY_TO_ADULTS.get(); }
    public static double vampireVillagerMovementSpeedMultiplier() { return VAMPIRE_VILLAGER_MOVEMENT_SPEED_MULTIPLIER.get(); }
    public static double hunterVillagerMovementSpeedMultiplier() { return HUNTER_VILLAGER_MOVEMENT_SPEED_MULTIPLIER.get(); }
    public static double werewolfVillagerMovementSpeedMultiplier() { return WEREWOLF_VILLAGER_MOVEMENT_SPEED_MULTIPLIER.get(); }
    public static double defensiveVillagerMovementSpeedMultiplier() { return DEFENSIVE_VILLAGER_MOVEMENT_SPEED_MULTIPLIER.get(); }
    public static double curingVillagerMovementSpeedMultiplier() { return CURING_VILLAGER_MOVEMENT_SPEED_MULTIPLIER.get(); }
    public static double specialVillagerMaxNavigationSpeed() { return SPECIAL_VILLAGER_MAX_NAVIGATION_SPEED.get(); }
    public static double specialVillagerMeleeGoalSpeed() { return SPECIAL_VILLAGER_MELEE_GOAL_SPEED.get(); }
    public static double specialVillagerChaseGoalSpeed() { return SPECIAL_VILLAGER_CHASE_GOAL_SPEED.get(); }
    public static double humanSocialPenalty() { return HUMAN_SOCIAL_PENALTY.get(); }
    public static double werewolfSocialPenalty() { return WEREWOLF_SOCIAL_PENALTY.get(); }
    public static double hunterSocialPenalty() { return HUNTER_SOCIAL_PENALTY.get(); }
    public static boolean blockWerewolfInteractions() { return BLOCK_WEREWOLF_INTERACTIONS.get(); }
    public static boolean blockHunterInteractions() { return BLOCK_HUNTER_INTERACTIONS.get(); }
    public static boolean monstersIgnoreMcaVampireVillagers() { return MONSTERS_IGNORE_MCA_VAMPIRE_VILLAGERS.get(); }
    public static boolean cancelMonsterDamageToMcaVampireVillagers() { return CANCEL_MONSTER_DAMAGE_TO_MCA_VAMPIRE_VILLAGERS.get(); }
    public static boolean suppressMcaVampireVillagerZombiePanic() { return SUPPRESS_MCA_VAMPIRE_VILLAGER_ZOMBIE_PANIC.get(); }
    public static boolean suppressMcaVampireVillagerFearDialogue() { return SUPPRESS_MCA_VAMPIRE_VILLAGER_FEAR_DIALOGUE.get(); }
    public static boolean monstersIgnoreSpecialMcaVillagers() { return MONSTERS_IGNORE_SPECIAL_MCA_VILLAGERS.get(); }
    public static boolean specialMcaVillagersIgnoreCommonMonsters() { return SPECIAL_MCA_VILLAGERS_IGNORE_COMMON_MONSTERS.get(); }
    public static boolean suppressSpecialMcaVillagerMonsterPanic() { return SUPPRESS_SPECIAL_MCA_VILLAGER_MONSTER_PANIC.get(); }
    public static boolean suppressSpecialMcaVillagerFearDialogue() { return SUPPRESS_SPECIAL_MCA_VILLAGER_FEAR_DIALOGUE.get(); }
    public static boolean cancelMonsterDamageToSpecialMcaVillagers() { return CANCEL_MONSTER_DAMAGE_TO_SPECIAL_MCA_VILLAGERS.get(); }
    public static boolean infectedMcaVillagersCountAsSpecialForMonsterIgnore() { return INFECTED_MCA_VILLAGERS_COUNT_AS_SPECIAL_FOR_MONSTER_IGNORE.get(); }
    public static boolean specialMcaGuardsDefendAgainstHostileMobs() { return SPECIAL_MCA_GUARDS_DEFEND_AGAINST_HOSTILE_MOBS.get(); }
    public static boolean specialMcaGuardsStillSuppressPanic() { return SPECIAL_MCA_GUARDS_STILL_SUPPRESS_PANIC.get(); }
    public static boolean specialMcaGuardsAttackCreepers() { return SPECIAL_MCA_GUARDS_ATTACK_CREEPERS.get(); }
    public static boolean preventAggressiveVillagerReplacementForMcaVampires() { return PREVENT_AGGRESSIVE_VILLAGER_REPLACEMENT_FOR_MCA_VAMPIRES.get(); }
    public static boolean preventAggressiveVillagerReplacementForAllMcaVillagers() { return PREVENT_AGGRESSIVE_VILLAGER_REPLACEMENT_FOR_ALL_MCA_VILLAGERS.get(); }
    public static boolean enableMcaVillagerDefenseAgainstVampires() { return ENABLE_MCA_VILLAGER_DEFENSE_AGAINST_VAMPIRES.get(); }
    public static boolean enableTemporaryDefensiveMode() { return ENABLE_TEMPORARY_DEFENSIVE_MODE.get(); }
    public static boolean defensiveModeOnlyInHunterVillages() { return DEFENSIVE_MODE_ONLY_IN_HUNTER_VILLAGES.get(); }
    public static int vampireThreatAlertRadius() { return VAMPIRE_THREAT_ALERT_RADIUS.get(); }
    public static int vampireThreatWindowTicks() { return VAMPIRE_THREAT_WINDOW_TICKS.get(); }
    public static int vampireThreatCountToTriggerDefense() { return VAMPIRE_THREAT_COUNT_TO_TRIGGER_DEFENSE.get(); }
    public static int defensiveModeDurationTicks() { return DEFENSIVE_MODE_DURATION_TICKS.get(); }
    public static boolean defensiveVillagersCanAttackVampirePlayers() { return DEFENSIVE_VILLAGERS_CAN_ATTACK_VAMPIRE_PLAYERS.get(); }
    public static boolean defensiveVillagersCanAttackMcaVampireVillagers() { return DEFENSIVE_VILLAGERS_CAN_ATTACK_MCA_VAMPIRE_VILLAGERS.get(); }
    public static boolean defensiveVillagersWarnInsteadOfAttackMcaVampires() { return DEFENSIVE_VILLAGERS_WARN_INSTEAD_OF_ATTACK_MCA_VAMPIRES.get(); }
    public static boolean defensiveVillagersAttackOnlyIfThreatNearby() { return DEFENSIVE_VILLAGERS_ATTACK_ONLY_IF_THREAT_NEARBY.get(); }
    public static boolean defensiveModeGrantsHunterTrait() { return DEFENSIVE_MODE_GRANTS_HUNTER_TRAIT.get(); }
    public static boolean defensiveModeUsesPitchfork() { return DEFENSIVE_MODE_USES_PITCHFORK.get(); }
    public static String defensiveVillagerWeaponType() { return DEFENSIVE_VILLAGER_WEAPON_TYPE.get(); }
    public static boolean defensiveVillagersHunterTraitIsTemporary() { return DEFENSIVE_VILLAGERS_HUNTER_TRAIT_IS_TEMPORARY.get(); }
    public static boolean defensiveVillagersAreHunterAligned() { return DEFENSIVE_VILLAGERS_ARE_HUNTER_ALIGNED.get(); }
    public static boolean defensiveVillagersCountAsHuntersForVampireAi() { return DEFENSIVE_VILLAGERS_COUNT_AS_HUNTERS_FOR_VAMPIRE_AI.get(); }
    public static double humanTradePriceMultiplier() { return HUMAN_TRADE_PRICE_MULTIPLIER.get(); }
    public static boolean blockWerewolfTrading() { return BLOCK_WEREWOLF_TRADING.get(); }
    public static boolean blockHunterTrading() { return BLOCK_HUNTER_TRADING.get(); }
    public static boolean vampireVillagersTradeOnlyWithVampires() { return VAMPIRE_VILLAGERS_TRADE_ONLY_WITH_VAMPIRES.get(); }
    public static boolean clearVampireBiteReputationPenaltyOnConversion() { return CLEAR_VAMPIRE_BITE_REPUTATION_PENALTY_ON_CONVERSION.get(); }
    public static boolean neutralizeConversionBitePenaltyForVampires() { return NEUTRALIZE_CONVERSION_BITE_PENALTY_FOR_VAMPIRES.get(); }
    public static double vampirePlayerTradeDiscount() { return VAMPIRE_PLAYER_TRADE_DISCOUNT.get(); }
    public static double hunterTradePriceMultiplier() { return HUNTER_TRADE_PRICE_MULTIPLIER.get(); }
    public static double werewolfTradePriceMultiplier() { return WEREWOLF_TRADE_PRICE_MULTIPLIER.get(); }
    public static boolean vampireVillagersTreatVampiresAsAllies() { return VAMPIRE_VILLAGERS_TREAT_VAMPIRES_AS_ALLIES.get(); }
    public static boolean vampireVillagersTreatHuntersAsEnemies() { return VAMPIRE_VILLAGERS_TREAT_HUNTERS_AS_ENEMIES.get(); }
    public static boolean vampireVillagersTreatTransformedWerewolvesAsEnemies() { return VAMPIRE_VILLAGERS_TREAT_TRANSFORMED_WEREWOLVES_AS_ENEMIES.get(); }
    public static boolean respectVampireDisguiseForMcaInteractions() { return RESPECT_VAMPIRE_DISGUISE_FOR_MCA_INTERACTIONS.get(); }
    public static boolean vampireDisguiseSuppressesVampireTradeDiscount() { return VAMPIRE_DISGUISE_SUPPRESSES_VAMPIRE_TRADE_DISCOUNT.get(); }
    public static boolean vampireDisguiseUsesHumanSocialPenalty() { return VAMPIRE_DISGUISE_USES_HUMAN_SOCIAL_PENALTY.get(); }
    public static boolean infectiousBiteOverridesConversionChance() { return INFECTIOUS_BITE_OVERRIDES_CONVERSION_CHANCE.get(); }
    public static boolean infectiousBiteStillUsesConfiguredDuration() { return INFECTIOUS_BITE_STILL_USES_CONFIGURED_DURATION.get(); }
    public static boolean hunterVillagersImmuneToVampirism() { return HUNTER_VILLAGERS_IMMUNE_TO_VAMPIRISM.get(); }
    public static boolean hunterVillagersHavePoisonousBlood() { return HUNTER_VILLAGERS_HAVE_POISONOUS_BLOOD.get(); }
    public static boolean hunterVillagersApplyToxicantToVampireBiters() { return HUNTER_VILLAGERS_APPLY_TOXICANT_TO_VAMPIRE_BITERS.get(); }
    public static int hunterVillagerToxicantDurationTicks() { return HUNTER_VILLAGER_TOXICANT_DURATION_TICKS.get(); }
    public static int hunterVillagerToxicantAmplifier() { return HUNTER_VILLAGER_TOXICANT_AMPLIFIER.get(); }
    public static boolean hunterVillagerFallbackToVanillaPoison() { return HUNTER_VILLAGER_FALLBACK_TO_VANILLA_POISON.get(); }
    public static boolean hunterVillagersImmuneToWerewolfTransformation() { return HUNTER_VILLAGERS_IMMUNE_TO_WEREWOLF_TRANSFORMATION.get(); }
    public static boolean hunterVillagersAttackVampirePlayers() { return HUNTER_VILLAGERS_ATTACK_VAMPIRE_PLAYERS.get(); }
    public static boolean hunterVillagersAttackMcaVampireVillagers() { return HUNTER_VILLAGERS_ATTACK_MCA_VAMPIRE_VILLAGERS.get(); }
    public static boolean hunterVillagersAttackTransformedWerewolves() { return HUNTER_VILLAGERS_ATTACK_TRANSFORMED_WEREWOLVES.get(); }
    public static boolean hunterVillagersAttackVampireMobs() { return HUNTER_VILLAGERS_ATTACK_VAMPIRE_MOBS.get(); }
    public static boolean hunterVillagersAttackOnlyWhileDefensive() { return HUNTER_VILLAGERS_ATTACK_ONLY_WHILE_DEFENSIVE.get(); }
    public static boolean hunterVillagersIgnoreVampireDisguise() { return HUNTER_VILLAGERS_IGNORE_VAMPIRE_DISGUISE.get(); }
    public static double hunterVillagerAttackDamage() { return HUNTER_VILLAGER_ATTACK_DAMAGE.get(); }
    public static double hunterVillagerStakeChance() { return HUNTER_VILLAGER_STAKE_CHANCE.get(); }
    public static int hunterVillagerAttackCooldownTicks() { return HUNTER_VILLAGER_ATTACK_COOLDOWN_TICKS.get(); }
    public static double hunterVillagerFollowRange() { return HUNTER_VILLAGER_FOLLOW_RANGE.get(); }
    public static boolean hunterVillageControlGrantsHunterTrait() { return HUNTER_VILLAGE_CONTROL_GRANTS_HUNTER_TRAIT.get(); }
    public static boolean hunterVillagersOnlyFromHunterControlledVillages() { return HUNTER_VILLAGERS_ONLY_FROM_HUNTER_CONTROLLED_VILLAGES.get(); }
    public static boolean hunterVillagerRemoveTraitWhenVillageControlLost() { return HUNTER_VILLAGER_REMOVE_TRAIT_WHEN_VILLAGE_CONTROL_LOST.get(); }
    public static boolean hunterVillagerKeepManualHunterTrait() { return HUNTER_VILLAGER_KEEP_MANUAL_HUNTER_TRAIT.get(); }
    public static boolean hunterVillagerAvoidDuplicateToxicant() { return HUNTER_VILLAGER_AVOID_DUPLICATE_TOXICANT.get(); }
    public static int hunterVillagerMaxChaseDistanceFromVillage() { return HUNTER_VILLAGER_MAX_CHASE_DISTANCE_FROM_VILLAGE.get(); }
    public static int hunterVillagerMaxChaseDistanceFromHome() { return HUNTER_VILLAGER_MAX_CHASE_DISTANCE_FROM_HOME.get(); }
    public static int hunterVillagerRetaliationMemoryTicks() { return HUNTER_VILLAGER_RETALIATION_MEMORY_TICKS.get(); }
    public static boolean hunterVillagerPreferRecentVampireAggressors() { return HUNTER_VILLAGER_PREFER_RECENT_VAMPIRE_AGGRESSORS.get(); }
    public static boolean hunterVillagersTradeWithHunters() { return HUNTER_VILLAGERS_TRADE_WITH_HUNTERS.get(); }
    public static boolean hunterVillagersTradeWithHumans() { return HUNTER_VILLAGERS_TRADE_WITH_HUMANS.get(); }
    public static boolean hunterVillagersBlockVampireTrading() { return HUNTER_VILLAGERS_BLOCK_VAMPIRE_TRADING.get(); }
    public static boolean hunterVillagersBlockTransformedWerewolfTrading() { return HUNTER_VILLAGERS_BLOCK_TRANSFORMED_WEREWOLF_TRADING.get(); }
    public static double hunterVillagerHunterDiscount() { return HUNTER_VILLAGER_HUNTER_DISCOUNT.get(); }
    public static double hunterVillagerHumanPriceMultiplier() { return HUNTER_VILLAGER_HUMAN_PRICE_MULTIPLIER.get(); }
    public static double hunterVillagerVampirePriceMultiplier() { return HUNTER_VILLAGER_VAMPIRE_PRICE_MULTIPLIER.get(); }
    public static double hunterVillagerWerewolfPriceMultiplier() { return HUNTER_VILLAGER_WEREWOLF_PRICE_MULTIPLIER.get(); }
    public static boolean hunterVillagerDisguisedVampireCanTrade() { return HUNTER_VILLAGER_DISGUISED_VAMPIRE_CAN_TRADE.get(); }
    public static boolean hunterVillagerDisguisedVampireUsesHumanPrices() { return HUNTER_VILLAGER_DISGUISED_VAMPIRE_USES_HUMAN_PRICES.get(); }
    public static boolean hunterVillagersBlockVampireRomance() { return HUNTER_VILLAGERS_BLOCK_VAMPIRE_ROMANCE.get(); }
    public static boolean hunterVillagersBlockTransformedWerewolfRomance() { return HUNTER_VILLAGERS_BLOCK_TRANSFORMED_WEREWOLF_ROMANCE.get(); }
    public static double hunterVillagerVampireSocialPenalty() { return HUNTER_VILLAGER_VAMPIRE_SOCIAL_PENALTY.get(); }
    public static double hunterVillagerWerewolfSocialPenalty() { return HUNTER_VILLAGER_WEREWOLF_SOCIAL_PENALTY.get(); }
    public static double hunterVillagerHumanSocialPenalty() { return HUNTER_VILLAGER_HUMAN_SOCIAL_PENALTY.get(); }
    public static double hunterVillagerHunterSocialPenalty() { return HUNTER_VILLAGER_HUNTER_SOCIAL_PENALTY.get(); }
    public static boolean hunterVillagerDisguisedVampireUsesHumanSocialRules() { return HUNTER_VILLAGER_DISGUISED_VAMPIRE_USES_HUMAN_SOCIAL_RULES.get(); }
    public static boolean hunterVillagerDisguisedVampireCanRomance() { return HUNTER_VILLAGER_DISGUISED_VAMPIRE_CAN_ROMANCE.get(); }
    public static boolean enableMcaVampireVillagerCure() { return ENABLE_MCA_VAMPIRE_VILLAGER_CURE.get(); }
    public static boolean allowMilkToTreatMcaVillagerSanguinare() { return ALLOW_MILK_TO_TREAT_MCA_VILLAGER_SANGUINARE.get(); }
    public static boolean milkTreatmentRemovesNegativeEffects() { return MILK_TREATMENT_REMOVES_NEGATIVE_EFFECTS.get(); }
    public static boolean milkTreatmentConsumesMilk() { return MILK_TREATMENT_CONSUMES_MILK.get(); }
    public static boolean milkTreatmentReturnsBucket() { return MILK_TREATMENT_RETURNS_BUCKET.get(); }
    public static boolean milkTreatmentOnlyBeforeConversion() { return MILK_TREATMENT_ONLY_BEFORE_CONVERSION.get(); }
    public static boolean mcaVillageEffectsFollowMcaBounds() { return MCA_VILLAGE_EFFECTS_FOLLOW_MCA_BOUNDS.get(); }
    public static int mcaVillageFogBorderMarginBlocks() { return MCA_VILLAGE_FOG_BORDER_MARGIN_BLOCKS.get(); }
    public static int missionReward(com.guilh.mca_vampirism_compat.mission.OccultVillageMissionType type) { return MISSION_REWARDS.get(type).get(); }
    public static boolean enableOccultVillageMissions() { return ENABLE_OCCULT_VILLAGE_MISSIONS.get(); }
    public static int occultVillageMissionCooldownTicks() { return OCCULT_VILLAGE_MISSION_COOLDOWN_TICKS.get(); }
    public static int occultVillageMissionDeadlineTicks() { return OCCULT_VILLAGE_MISSION_DEADLINE_TICKS.get(); }
    public static boolean enableFactionContextualDialogues() { return ENABLE_FACTION_CONTEXTUAL_DIALOGUES.get(); }
    public static boolean enableFactionGiftPreferences() { return ENABLE_FACTION_GIFT_PREFERENCES.get(); }
    public static boolean enableFactionRecoveryDiet() { return ENABLE_FACTION_RECOVERY_DIET.get(); }
    public static boolean vampireCureRequiresWeakness() { return VAMPIRE_CURE_REQUIRES_WEAKNESS.get(); }
    public static boolean vampireCureRequiresGoldenApple() { return VAMPIRE_CURE_REQUIRES_GOLDEN_APPLE.get(); }
    public static boolean vampireCureRequiresGarlicInjection() { return VAMPIRE_CURE_REQUIRES_GARLIC_INJECTION.get(); }
    public static int vampireCureRequiredWeaknessDurationTicks() { return VAMPIRE_CURE_REQUIRED_WEAKNESS_DURATION_TICKS.get(); }
    public static boolean vampireCureAllowAnyWeaknessDuration() { return VAMPIRE_CURE_ALLOW_ANY_WEAKNESS_DURATION.get(); }
    public static int vampireCureIngredientProgressExpiresTicks() { return VAMPIRE_CURE_INGREDIENT_PROGRESS_EXPIRES_TICKS.get(); }
    public static int vampireCureDurationTicks() { return VAMPIRE_CURE_DURATION_TICKS.get(); }
    public static boolean vampireCureVillagerCannotAttack() { return VAMPIRE_CURE_VILLAGER_CANNOT_ATTACK.get(); }
    public static boolean vampireCureVillagerCannotTrade() { return VAMPIRE_CURE_VILLAGER_CANNOT_TRADE.get(); }
    public static boolean vampireCureShowsParticles() { return VAMPIRE_CURE_SHOWS_PARTICLES.get(); }
    public static boolean vampireCureAppliesWeaknessDuringProcess() { return VAMPIRE_CURE_APPLIES_WEAKNESS_DURING_PROCESS.get(); }
    public static int postCureResyncBlockTicks() { return POST_CURE_RESYNC_BLOCK_TICKS.get(); }
    public static boolean enableEasyVillagersFactionRules() { return ENABLE_EASY_VILLAGERS_FACTION_RULES.get(); }
    public static boolean enableEasyVillagersFactionPickupRules() { return ENABLE_EASY_VILLAGERS_FACTION_PICKUP_RULES.get(); }
    public static boolean easyVillagersBlockPickupOfCuringVillagers() { return EASY_VILLAGERS_BLOCK_PICKUP_OF_CURING_VILLAGERS.get(); }
    public static boolean allowPickupOfInfectedHumanVillagers() { return ALLOW_PICKUP_OF_INFECTED_HUMAN_VILLAGERS.get(); }
    public static boolean showPickupBlockedMessage() { return SHOW_PICKUP_BLOCKED_MESSAGE.get(); }
    public static boolean allowMcaVillagerWerewolfInfection() { return ALLOW_MCA_VILLAGER_WEREWOLF_INFECTION.get(); }
    public static boolean allowMcaChildWerewolfInfection() { return ALLOW_MCA_CHILD_WEREWOLF_INFECTION.get(); }
    public static double werewolfConversionChanceOnBite() { return WEREWOLF_CONVERSION_CHANCE_ON_BITE.get(); }
    public static int werewolfVillagerInfectionDurationTicks() { return WEREWOLF_VILLAGER_INFECTION_DURATION_TICKS.get(); }
    public static boolean vampireVillagersImmuneToWerewolfTransformation() { return VAMPIRE_VILLAGERS_IMMUNE_TO_WEREWOLF_TRANSFORMATION.get(); }
    public static boolean werewolfVillagersImmuneToVampirism() { return WEREWOLF_VILLAGERS_IMMUNE_TO_VAMPIRISM.get(); }
    public static boolean werewolfVillagersImmuneToHunterConversion() { return WEREWOLF_VILLAGERS_IMMUNE_TO_HUNTER_CONVERSION.get(); }
    public static boolean werewolfVillagerAppearanceEnabled() { return WEREWOLF_VILLAGER_APPEARANCE_ENABLED.get(); }
    public static boolean werewolfVillagerEarsEnabled() { return WEREWOLF_VILLAGER_EARS_ENABLED.get(); }
    public static boolean werewolfVillagerClawsEnabled() { return WEREWOLF_VILLAGER_CLAWS_ENABLED.get(); }
    public static boolean werewolfVillagerAppearanceDebug() { return WEREWOLF_VILLAGER_APPEARANCE_DEBUG.get(); }
    public static boolean werewolfVillagersTradeWithWerewolves() { return WEREWOLF_VILLAGERS_TRADE_WITH_WEREWOLVES.get(); }
    public static boolean werewolfVillagersTradeWithHumans() { return WEREWOLF_VILLAGERS_TRADE_WITH_HUMANS.get(); }
    public static boolean werewolfVillagersBlockVampireTrading() { return WEREWOLF_VILLAGERS_BLOCK_VAMPIRE_TRADING.get(); }
    public static boolean werewolfVillagersBlockHunterTrading() { return WEREWOLF_VILLAGERS_BLOCK_HUNTER_TRADING.get(); }
    public static double werewolfVillagerWerewolfDiscount() { return WEREWOLF_VILLAGER_WEREWOLF_DISCOUNT.get(); }
    public static double werewolfVillagerHumanPriceMultiplier() { return WEREWOLF_VILLAGER_HUMAN_PRICE_MULTIPLIER.get(); }
    public static double werewolfVillagerVampirePriceMultiplier() { return WEREWOLF_VILLAGER_VAMPIRE_PRICE_MULTIPLIER.get(); }
    public static double werewolfVillagerHunterPriceMultiplier() { return WEREWOLF_VILLAGER_HUNTER_PRICE_MULTIPLIER.get(); }
    public static boolean werewolfVillagerDisguisedVampireUsesHumanPrices() { return WEREWOLF_VILLAGER_DISGUISED_VAMPIRE_USES_HUMAN_PRICES.get(); }
    public static boolean werewolfVillagerDisguisedVampireCanTrade() { return WEREWOLF_VILLAGER_DISGUISED_VAMPIRE_CAN_TRADE.get(); }
    public static boolean werewolfVillagersBlockVampireRomance() { return WEREWOLF_VILLAGERS_BLOCK_VAMPIRE_ROMANCE.get(); }
    public static boolean werewolfVillagersBlockHunterRomance() { return WEREWOLF_VILLAGERS_BLOCK_HUNTER_ROMANCE.get(); }
    public static double werewolfVillagerVampireSocialPenalty() { return WEREWOLF_VILLAGER_VAMPIRE_SOCIAL_PENALTY.get(); }
    public static double werewolfVillagerHunterSocialPenalty() { return WEREWOLF_VILLAGER_HUNTER_SOCIAL_PENALTY.get(); }
    public static double werewolfVillagerHumanSocialPenalty() { return WEREWOLF_VILLAGER_HUMAN_SOCIAL_PENALTY.get(); }
    public static double werewolfVillagerWerewolfSocialPenalty() { return WEREWOLF_VILLAGER_WEREWOLF_SOCIAL_PENALTY.get(); }
    public static boolean werewolfVillagerDisguisedVampireUsesHumanSocialRules() { return WEREWOLF_VILLAGER_DISGUISED_VAMPIRE_USES_HUMAN_SOCIAL_RULES.get(); }
    public static boolean werewolfVillagerDisguisedVampireCanRomance() { return WEREWOLF_VILLAGER_DISGUISED_VAMPIRE_CAN_ROMANCE.get(); }
    public static boolean enableMcaWerewolfVillagerAi() { return ENABLE_MCA_WEREWOLF_VILLAGER_AI.get(); }
    public static boolean werewolfVillagersAttackVampirePlayers() { return WEREWOLF_VILLAGERS_ATTACK_VAMPIRE_PLAYERS.get(); }
    public static boolean werewolfVillagersAttackHunterPlayers() { return WEREWOLF_VILLAGERS_ATTACK_HUNTER_PLAYERS.get(); }
    public static boolean werewolfVillagersAttackVampireMobs() { return WEREWOLF_VILLAGERS_ATTACK_VAMPIRE_MOBS.get(); }
    public static boolean werewolfVillagersAttackHunterNpcs() { return WEREWOLF_VILLAGERS_ATTACK_HUNTER_NPCS.get(); }
    public static boolean werewolfVillagersRespectVampireDisguise() { return WEREWOLF_VILLAGERS_RESPECT_VAMPIRE_DISGUISE.get(); }
    public static boolean werewolfVillagersAttackOnlyAtNight() { return WEREWOLF_VILLAGERS_ATTACK_ONLY_AT_NIGHT.get(); }
    public static double werewolfVillagerAttackDamage() { return WEREWOLF_VILLAGER_ATTACK_DAMAGE.get(); }
    public static int werewolfVillagerAttackCooldownTicks() { return WEREWOLF_VILLAGER_ATTACK_COOLDOWN_TICKS.get(); }
    public static double werewolfVillagerFollowRange() { return WEREWOLF_VILLAGER_FOLLOW_RANGE.get(); }
    public static double werewolfVillagerMaxChaseDistanceFromHome() { return WEREWOLF_VILLAGER_MAX_CHASE_DISTANCE_FROM_HOME.get(); }
    public static int werewolfVillagerTargetMemoryTicks() { return WEREWOLF_VILLAGER_TARGET_MEMORY_TICKS.get(); }
    public static int specialVillagerTargetScanIntervalTicks() { return SPECIAL_VILLAGER_TARGET_SCAN_INTERVAL_TICKS.get(); }
    public static int specialVillagerRetaliationMemoryTicks() { return SPECIAL_VILLAGER_RETALIATION_MEMORY_TICKS.get(); }
    public static int specialVillagerMaxChaseDistance() { return SPECIAL_VILLAGER_MAX_CHASE_DISTANCE.get(); }
    public static int specialVillagerAttackCooldownTicks() { return SPECIAL_VILLAGER_ATTACK_COOLDOWN_TICKS.get(); }
    public static int specialVillagerCombatRepathIntervalTicks() { return SPECIAL_VILLAGER_COMBAT_REPATH_INTERVAL_TICKS.get(); }
    public static boolean specialVillagersTargetFactionNpcs() { return SPECIAL_VILLAGERS_TARGET_FACTION_NPCS.get(); }
    public static boolean enableMcaWerewolfVillagerCure() { return ENABLE_MCA_WEREWOLF_VILLAGER_CURE.get(); }
    public static boolean werewolfCureRequiresWeakness() { return WEREWOLF_CURE_REQUIRES_WEAKNESS.get(); }
    public static boolean werewolfCureRequiresGoldenApple() { return WEREWOLF_CURE_REQUIRES_GOLDEN_APPLE.get(); }
    public static boolean werewolfCureRequiresGarlicInjection() { return WEREWOLF_CURE_REQUIRES_GARLIC_INJECTION.get(); }
    public static int werewolfCureRequiredWeaknessDurationTicks() { return WEREWOLF_CURE_REQUIRED_WEAKNESS_DURATION_TICKS.get(); }
    public static boolean werewolfCureAllowAnyWeaknessDuration() { return WEREWOLF_CURE_ALLOW_ANY_WEAKNESS_DURATION.get(); }
    public static int werewolfCureIngredientProgressExpiresTicks() { return WEREWOLF_CURE_INGREDIENT_PROGRESS_EXPIRES_TICKS.get(); }
    public static int werewolfCureDurationTicks() { return WEREWOLF_CURE_DURATION_TICKS.get(); }
    public static boolean werewolfCureVillagerCannotAttack() { return WEREWOLF_CURE_VILLAGER_CANNOT_ATTACK.get(); }
    public static boolean werewolfCureVillagerCannotTrade() { return WEREWOLF_CURE_VILLAGER_CANNOT_TRADE.get(); }
    public static boolean werewolfCureShowsParticles() { return WEREWOLF_CURE_SHOWS_PARTICLES.get(); }
    public static boolean werewolfCureAppliesWeaknessDuringProcess() { return WEREWOLF_CURE_APPLIES_WEAKNESS_DURING_PROCESS.get(); }
    public static boolean enableMcaVillageCaptureIntegration() { return ENABLE_MCA_VILLAGE_CAPTURE_INTEGRATION.get(); }
    public static double mcaHumanVillagerDefenseStrength() { return MCA_HUMAN_VILLAGER_DEFENSE_STRENGTH.get(); }
    public static double mcaHunterVillagerDefenseStrength() { return MCA_HUNTER_VILLAGER_DEFENSE_STRENGTH.get(); }
    public static double mcaVampireVillagerVampireStrength() { return MCA_VAMPIRE_VILLAGER_VAMPIRE_STRENGTH.get(); }
    public static double mcaInfectedVillagerDefenseStrength() { return MCA_INFECTED_VILLAGER_DEFENSE_STRENGTH.get(); }
    public static double mcaCuringVillagerDefenseStrength() { return MCA_CURING_VILLAGER_DEFENSE_STRENGTH.get(); }
    public static double mcaChildVillagerDefenseStrength() { return MCA_CHILD_VILLAGER_DEFENSE_STRENGTH.get(); }
    public static int mcaVillageCaptureStrengthRadius() { return MCA_VILLAGE_CAPTURE_STRENGTH_RADIUS.get(); }
    public static boolean mcaVillageCaptureDebugBreakdown() { return MCA_VILLAGE_CAPTURE_DEBUG_BREAKDOWN.get(); }
    public static boolean makeAggressiveOnlyCreatesMcaHuntersInHunterVillages() { return MAKE_AGGRESSIVE_ONLY_CREATES_MCA_HUNTERS_IN_HUNTER_VILLAGES.get(); }
    public static boolean vampireVillageCaptureInfectsMcaVillagers() { return VAMPIRE_VILLAGE_CAPTURE_INFECTS_MCA_VILLAGERS.get(); }
    public static double vampireVillageCaptureInfectionChance() { return VAMPIRE_VILLAGE_CAPTURE_INFECTION_CHANCE.get(); }
    public static boolean vampireVillageCaptureInfectsChildren() { return VAMPIRE_VILLAGE_CAPTURE_INFECTS_CHILDREN.get(); }
    public static boolean vampireVillageCaptureCanInfectHunterVillagers() { return VAMPIRE_VILLAGE_CAPTURE_CAN_INFECT_HUNTER_VILLAGERS.get(); }
    public static boolean vampireVillageCaptureAcceleratesExistingSanguinare() { return VAMPIRE_VILLAGE_CAPTURE_ACCELERATES_EXISTING_SANGUINARE.get(); }
    public static double vampireVillageCaptureAccelerationMultiplier() { return VAMPIRE_VILLAGE_CAPTURE_ACCELERATION_MULTIPLIER.get(); }
    public static boolean vampireVillageCaptureDoesNotInstantlyConvertMcaVillagers() { return VAMPIRE_VILLAGE_CAPTURE_DOES_NOT_INSTANTLY_CONVERT_MCA_VILLAGERS.get(); }
    public static boolean hunterVillageCaptureGrantsHunterTrait() { return HUNTER_VILLAGE_CAPTURE_GRANTS_HUNTER_TRAIT.get(); }
    public static boolean hunterVillageCaptureCuresMcaSanguinare() { return HUNTER_VILLAGE_CAPTURE_CURES_MCA_SANGUINARE.get(); }
    public static boolean hunterVillageCaptureCuresMcaVampires() { return HUNTER_VILLAGE_CAPTURE_CURES_MCA_VAMPIRES.get(); }
    public static boolean hunterVillageCaptureStartsCureForMcaVampires() { return HUNTER_VILLAGE_CAPTURE_STARTS_CURE_FOR_MCA_VAMPIRES.get(); }
    public static boolean werewolfVillagersResistVampireCaptureInfection() { return WEREWOLF_VILLAGERS_RESIST_VAMPIRE_CAPTURE_INFECTION.get(); }
    public static boolean hunterVillageDoesNotConvertWerewolvesToHunters() { return HUNTER_VILLAGE_DOES_NOT_CONVERT_WEREWOLVES_TO_HUNTERS.get(); }
    public static double twoVampireParentsInheritanceChance() { return TWO_VAMPIRE_PARENTS_INHERITANCE_CHANCE.get(); }
    public static double oneVampireParentInheritanceChance() { return ONE_VAMPIRE_PARENT_INHERITANCE_CHANCE.get(); }
    public static double twoWerewolfParentsInheritanceChance() { return TWO_WEREWOLF_PARENTS_INHERITANCE_CHANCE.get(); }
    public static double oneWerewolfParentInheritanceChance() { return ONE_WEREWOLF_PARENT_INHERITANCE_CHANCE.get(); }
    public static double vampireBabyGrowthMultiplier() { return VAMPIRE_BABY_GROWTH_MULTIPLIER.get(); }
    public static long growthProcessIntervalTicks() { return GROWTH_PROCESS_INTERVAL_TICKS.get(); }
    public static boolean allowVampireChildrenGrowthBoost() { return ALLOW_VAMPIRE_CHILDREN_GROWTH_BOOST.get(); }
    public static boolean enablePersistentVampireAppearance() { return ENABLE_PERSISTENT_VAMPIRE_APPEARANCE.get(); }
    public static boolean paleSkinEnabled() { return PALE_SKIN_ENABLED.get(); }
    public static boolean vampireEyesEnabled() { return VAMPIRE_EYES_ENABLED.get(); }
    public static boolean preserveOriginalAppearanceForCure() { return PRESERVE_ORIGINAL_APPEARANCE_FOR_CURE.get(); }
    public static boolean enableMcaVampireTrait() { return ENABLE_MCA_VAMPIRE_TRAIT.get(); }
    public static boolean enableMcaWerewolfTrait() { return ENABLE_MCA_WEREWOLF_TRAIT.get(); }
    public static boolean syncTraitAndCapabilityOnLoad() { return SYNC_TRAIT_AND_CAPABILITY_ON_LOAD.get(); }
    public static boolean enableLegacy103Migration() { return ENABLE_LEGACY_103_MIGRATION.get(); }
    public static boolean traitCanRestoreCapability() { return TRAIT_CAN_RESTORE_CAPABILITY.get(); }
    public static boolean vampireTraitControlsInheritance() { return VAMPIRE_TRAIT_CONTROLS_INHERITANCE.get(); }
    public static double oneVampireParentTraitInheritanceChance() { return ONE_VAMPIRE_PARENT_TRAIT_INHERITANCE_CHANCE.get(); }
    public static double twoVampireParentsTraitInheritanceChance() { return TWO_VAMPIRE_PARENTS_TRAIT_INHERITANCE_CHANCE.get(); }
    public static boolean respectMcaRelationshipsInCombat() { return RESPECT_MCA_RELATIONSHIPS_IN_COMBAT.get(); }
    public static boolean respectMcaPlayerCommands() { return RESPECT_MCA_PLAYER_COMMANDS.get(); }
    public static boolean suppressAutonomousCombatWhileFollowing() { return SUPPRESS_AUTONOMOUS_COMBAT_WHILE_FOLLOWING.get(); }
    public static boolean suppressAutonomousCombatWhileStaying() { return SUPPRESS_AUTONOMOUS_COMBAT_WHILE_STAYING.get(); }
    public static boolean allowShortRetaliationWhileFollowing() { return ALLOW_SHORT_RETALIATION_WHILE_FOLLOWING.get(); }
    public static boolean allowShortRetaliationWhileStaying() { return ALLOW_SHORT_RETALIATION_WHILE_STAYING.get(); }
    public static double commandedRetaliationMaxDistance() { return COMMANDED_RETALIATION_MAX_DISTANCE.get(); }
    public static int commandedRetaliationTicks() { return COMMANDED_RETALIATION_TICKS.get(); }
    public static boolean clearAutonomousTargetWhenCommanded() { return CLEAR_AUTONOMOUS_TARGET_WHEN_COMMANDED.get(); }
    public static boolean enableFactionCommandRestrictions() { return ENABLE_FACTION_COMMAND_RESTRICTIONS.get(); }
    public static boolean showCommandBlockedMessage() { return SHOW_COMMAND_BLOCKED_MESSAGE.get(); }
    public static boolean allowSpecialPlayersCommandHumans() { return ALLOW_SPECIAL_PLAYERS_COMMAND_HUMANS.get(); }
    public static boolean allowCommandsForInfectedHumans() { return ALLOW_COMMANDS_FOR_INFECTED_HUMANS.get(); }
    public static boolean allowCommandsForCuringVillagers() { return ALLOW_COMMANDS_FOR_CURING_VILLAGERS.get(); }
    public static boolean blockUnknownMcaCommandsForEnemyFactions() { return BLOCK_UNKNOWN_MCA_COMMANDS_FOR_ENEMY_FACTIONS.get(); }
    public static boolean giveOccultArtsBookOnSpecialVillageDiscovery() { return GIVE_OCCULT_ARTS_BOOK_ON_SPECIAL_VILLAGE_DISCOVERY.get(); }
    public static boolean enableDebugLogs() { return ENABLE_DEBUG_LOGS.get(); }
    public static boolean enableVerboseAiDebugLogs() { return ENABLE_DEBUG_LOGS.get() && ENABLE_VERBOSE_AI_DEBUG_LOGS.get(); }
    public static boolean enableVerboseNbtDebugLogs() { return ENABLE_VERBOSE_NBT_DEBUG_LOGS.get(); }
}
