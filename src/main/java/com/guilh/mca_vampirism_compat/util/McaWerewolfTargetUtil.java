package com.guilh.mca_vampirism_compat.util;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.faction.PlayerFaction;
import com.guilh.mca_vampirism_compat.faction.PlayerFactionContext;
import com.guilh.mca_vampirism_compat.service.McaHunterAlignmentService;
import com.guilh.mca_vampirism_compat.service.McaVampireCureService;
import com.guilh.mca_vampirism_compat.service.McaVampireFactionService;
import com.guilh.mca_vampirism_compat.service.McaVampireStateService;
import com.guilh.mca_vampirism_compat.service.McaWerewolfStateService;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.relationship.AgeState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class McaWerewolfTargetUtil {
    private McaWerewolfTargetUtil() {
    }

    public static boolean isValidWerewolfTarget(VillagerEntityMCA werewolf, LivingEntity target) {
        return targetReason(werewolf, target) != TargetReason.NONE;
    }

    public static TargetReason targetReason(VillagerEntityMCA werewolf, LivingEntity target) {
        if (werewolf == null
                || target == null
                || target == werewolf
                || !target.isAlive()
                || !McaWerewolfStateService.isWerewolf(werewolf)
                || McaVampireCureService.isCuring(werewolf)
                || werewolf.getAgeState() != AgeState.ADULT) {
            return TargetReason.NONE;
        }
        if (target instanceof VillagerEntityMCA targetVillager) {
            if (targetVillager.getAgeState() != AgeState.ADULT || McaVampireCureService.isCuring(targetVillager)) {
                return TargetReason.NONE;
            }
            if (McaVampireStateService.isVampire(targetVillager)) {
                return TargetReason.MCA_VAMPIRE_VILLAGER;
            }
            if (McaWerewolfStateService.isWerewolf(targetVillager)) {
                return TargetReason.NONE;
            }
            if (McaHunterAlignmentService.isMcaHunterVillager(targetVillager)) {
                return TargetReason.MCA_HUNTER_VILLAGER;
            }
        }
        if (McaVampirismCompatConfig.werewolfVillagersAttackOnlyAtNight() && werewolf.level().isDay()) {
            return TargetReason.NONE;
        }
        if (target instanceof Player player) {
            PlayerFactionContext context = McaVampireFactionService.classifyPlayerContext(player);
            PlayerFaction faction = context.effectiveFaction();
            boolean retaliation = isRetaliatingAgainst(werewolf, target);
            if (context.vampireDisguised()
                    && McaVampirismCompatConfig.werewolfVillagersRespectVampireDisguise()
                    && !retaliation) {
                logPlayerTarget(player, context, false, "DISGUISE_NOT_DETECTED");
                return TargetReason.NONE;
            }
            PlayerFaction realFaction = context.realFaction();
            if ((faction == PlayerFaction.VAMPIRE || realFaction == PlayerFaction.VAMPIRE)
                    && McaVampirismCompatConfig.werewolfVillagersAttackVampirePlayers()) {
                logPlayerTarget(player, context, true, TargetReason.VAMPIRE_PLAYER.name());
                return TargetReason.VAMPIRE_PLAYER;
            }
            if (faction == PlayerFaction.HUNTER && McaVampirismCompatConfig.werewolfVillagersAttackHunterPlayers()) {
                logPlayerTarget(player, context, true, TargetReason.HUNTER_PLAYER.name());
                return TargetReason.HUNTER_PLAYER;
            }
            logPlayerTarget(player, context, false, "FACTION_NOT_HOSTILE");
            return TargetReason.NONE;
        }
        if (McaVampireFactionService.isVampire(target) && McaVampirismCompatConfig.werewolfVillagersAttackVampireMobs()) {
            return TargetReason.VAMPIRE_MOB;
        }
        if (McaVampireFactionService.isHunter(target) && McaVampirismCompatConfig.werewolfVillagersAttackHunterNpcs()) {
            return TargetReason.HUNTER_MOB;
        }
        return TargetReason.NONE;
    }

    private static boolean isRetaliatingAgainst(VillagerEntityMCA werewolf, LivingEntity target) {
        long gameTime = werewolf.level().getGameTime();
        return werewolf.getCapability(ModCapabilities.VAMPIRIC_VILLAGER)
                .map(state -> state.isRetaliatingAgainst(target.getUUID(), gameTime))
                .orElse(false);
    }

    private static void logPlayerTarget(Player player, PlayerFactionContext context, boolean valid, String reason) {
        if (player.tickCount % 40 != 0) {
            return;
        }
        McaVampirismCompat.verboseAiLog(
                "[WEREWOLF-AI] target check target={} realFaction={} effectiveFaction={} valid={} reason={}",
                player.getName().getString(),
                context.realFaction(),
                context.effectiveFaction(),
                valid,
                reason
        );
    }

    public enum TargetReason {
        NONE,
        VAMPIRE_PLAYER,
        HUNTER_PLAYER,
        VAMPIRE_MOB,
        HUNTER_MOB,
        MCA_VAMPIRE_VILLAGER,
        MCA_HUNTER_VILLAGER
    }
}
