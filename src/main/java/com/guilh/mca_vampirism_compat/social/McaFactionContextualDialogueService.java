package com.guilh.mca_vampirism_compat.social;

import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.service.McaHunterAlignmentService;
import com.guilh.mca_vampirism_compat.service.McaVampireStateService;
import com.guilh.mca_vampirism_compat.service.McaWerewolfStateService;
import forge.net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Adds flavor after the authoritative social, trade and cure decisions. It
 * never returns a decision or mutates relation data, so it cannot change an
 * MCA interaction's gameplay result.
 */
public final class McaFactionContextualDialogueService {
    private static final long COOLDOWN_TICKS = 100L;
    private static final Map<DialogueCooldownKey, Long> COOLDOWNS = new HashMap<>();

    private McaFactionContextualDialogueService() {
    }

    public static void onAcceptedConversation(ServerPlayer player, VillagerEntityMCA villager,
                                              String question, String answer) {
        if (!looksLikeConversation(question, answer)) {
            return;
        }
        send(player, villager, voiceFor(villager), "greeting", true);
    }

    public static void onTradeRefused(ServerPlayer player, VillagerEntityMCA villager) {
        send(player, villager, voiceFor(villager), "trade_refused", false);
    }

    public static void onSocialRefused(ServerPlayer player, VillagerEntityMCA villager) {
        send(player, villager, voiceFor(villager), "social_refused", false);
    }

    public static void onCureStarted(ServerPlayer player, VillagerEntityMCA villager, boolean werewolf) {
        send(player, villager, werewolf ? FactionVoice.WEREWOLF : FactionVoice.VAMPIRE, "cure_started", false);
    }

    public static void onCureCompleted(ServerPlayer player, VillagerEntityMCA villager, boolean werewolf) {
        send(player, villager, werewolf ? FactionVoice.WEREWOLF : FactionVoice.VAMPIRE, "cure_completed", false);
    }

    private static void send(ServerPlayer player, VillagerEntityMCA villager, FactionVoice voice,
                             String event, boolean actionBar) {
        if (!McaVampirismCompatConfig.enableFactionContextualDialogues()
                || player == null || villager == null || voice == null || event == null) {
            return;
        }
        long gameTime = player.level().getGameTime();
        purge(gameTime);
        DialogueCooldownKey cooldownKey = new DialogueCooldownKey(player.getUUID(), villager.getUUID());
        Long nextAllowed = COOLDOWNS.get(cooldownKey);
        if (nextAllowed != null && nextAllowed > gameTime) {
            return;
        }
        COOLDOWNS.put(cooldownKey, gameTime + COOLDOWN_TICKS);
        Component line = Component.translatable(
                "mca_vamp_compat.dialogue." + voice.key + "." + event,
                villager.getName()
        );
        if (actionBar) {
            player.displayClientMessage(line, true);
        } else {
            // The existing action-bar warning remains visible; flavor follows
            // in the normal response channel instead of replacing it.
            player.sendSystemMessage(line);
        }
    }

    private static FactionVoice voiceFor(VillagerEntityMCA villager) {
        if (McaVampireStateService.isVampire(villager)) {
            return FactionVoice.VAMPIRE;
        }
        if (McaHunterAlignmentService.isMcaHunterVillager(villager)) {
            return FactionVoice.HUNTER;
        }
        if (McaWerewolfStateService.isWerewolf(villager)) {
            return FactionVoice.WEREWOLF;
        }
        return null;
    }

    private static boolean looksLikeConversation(String question, String answer) {
        String text = ((question == null ? "" : question) + " " + (answer == null ? "" : answer))
                .toLowerCase(Locale.ROOT);
        return text.contains("greet") || text.contains("chat") || text.contains("talk")
                || text.contains("hello") || text.contains("small") || text.contains("rumor")
                || text.contains("rumour") || text.contains("story");
    }

    private static void purge(long gameTime) {
        COOLDOWNS.entrySet().removeIf(entry -> entry.getValue() <= gameTime);
    }

    private enum FactionVoice {
        VAMPIRE("vampire"),
        HUNTER("hunter"),
        WEREWOLF("werewolf");

        private final String key;

        FactionVoice(String key) {
            this.key = key;
        }
    }

    private record DialogueCooldownKey(UUID playerUuid, UUID villagerUuid) {
    }
}
