package com.guilh.mca_vampirism_compat.mission;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.advancement.McaAdvancementService;
import com.guilh.mca_vampirism_compat.advancement.McaCompatAdvancements;
import com.guilh.mca_vampirism_compat.book.OccultVillageFactionPolicy;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.faction.PlayerFaction;
import com.guilh.mca_vampirism_compat.faction.PlayerFactionContext;
import com.guilh.mca_vampirism_compat.network.MissionDialogueClosePayload;
import com.guilh.mca_vampirism_compat.network.MissionDialogueSpeechPayload;
import com.guilh.mca_vampirism_compat.network.MissionHighlightPayload;
import com.guilh.mca_vampirism_compat.network.ModNetwork;
import com.guilh.mca_vampirism_compat.service.McaHunterAlignmentService;
import com.guilh.mca_vampirism_compat.service.McaVampireFactionService;
import com.guilh.mca_vampirism_compat.service.McaVampireStateService;
import com.guilh.mca_vampirism_compat.service.McaVillageEffectBoundsService;
import com.guilh.mca_vampirism_compat.service.McaWerewolfStateService;
import com.guilh.mca_vampirism_compat.util.VampirismEffectCompat;
import de.teamlapen.vampirism.blockentity.TotemBlockEntity;
import de.teamlapen.vampirism.fluids.BloodHelper;
import de.teamlapen.vampirism.items.BloodBottleItem;
import forge.net.conczin.mca.entity.VillagerEntityMCA;
import forge.net.conczin.mca.entity.ai.relationship.AgeState;
import forge.net.conczin.mca.cobalt.network.NetworkHandler;
import forge.net.conczin.mca.network.s2c.InteractionDialogueResponse;
import forge.net.conczin.mca.resources.Dialogues;
import forge.net.conczin.mca.resources.data.dialogue.Question;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.ModList;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Owns contextual supply and aid requests without storing anything on MCA
 * villagers. The sole persistent state is a per-player, per-totem record.
 */
public final class OccultVillageMissionService {
    public static final int HEART_REWARD = 5;
    private static final int CHECK_INTERVAL_TICKS = 40;
    private static final String MAIN_DIALOGUE = "main";
    private static final String OFFER_ANSWER = "occult_favor_offer";
    private static final String STATUS_ANSWER = "occult_favor_status";
    private static final String PENDING_DIALOGUE = "occult_favor_pending";
    private static final String ACTIVE_DIALOGUE = "occult_favor_active";
    private static final String ACCEPT_ANSWER = "accept";
    private static final String DECLINE_ANSWER = "decline";
    private static final String PROGRESS_ANSWER = "progress";
    private static final String CANCEL_ANSWER = "cancel";

    private static final java.util.Map<ServerPlayer, JournalSession> JOURNALS = new java.util.WeakHashMap<>();
    private static final java.security.SecureRandom JOURNAL_RANDOM = new java.security.SecureRandom();
    private static final class JournalSession {
        final long token = JOURNAL_RANDOM.nextLong();
        long lastRequest = Long.MIN_VALUE / 2;
    }
    public static void openJournalBook(ServerPlayer player) {
        JournalSession session = new JournalSession();
        JOURNALS.put(player, session);
        sendJournalPage(player, session, 0, true);
    }
    public static void journalRequest(ServerPlayer player, com.guilh.mca_vampirism_compat.network.MissionJournalRequest request) {
        JournalSession session = JOURNALS.get(player);
        long now = player.server.overworld().getGameTime();
        if (session == null || session.token != request.session() || !player.isAlive()
                || player.containerMenu != player.inventoryMenu || request.page() < 0 || request.page() > 100000
                || !(player.getMainHandItem().getItem() instanceof com.guilh.mca_vampirism_compat.item.OccultArtsBookItem)) return;
        if (now - session.lastRequest < (request.select() ? 4 : 20)) return;
        session.lastRequest = now;
        if (request.select()) selectTrackedMission(player, request.target());
        sendJournalPage(player, session, request.page(), false);
    }
    private static void sendJournalPage(ServerPlayer player, JournalSession session, int requestedPage, boolean open) {
        OccultVillageMissionSavedData saved = data(player.server);
        long now = player.server.overworld().getGameTime();
        expirePastMissions(player, saved, now);
        var entries = saved.activeMissions(player.getUUID()).stream().filter(entry -> entry.record().isAccepted())
                .sorted(java.util.Comparator.comparingLong((OccultVillageMissionSavedData.MissionEntry entry) -> entry.record().createdTick())
                        .thenComparing(entry -> entry.key().dimension().toString()).thenComparingLong(entry -> entry.key().totemPosition().asLong())).toList();
        int page = Math.max(0, Math.min(requestedPage, entries.size()-1));
        java.util.List<Component> lines = new java.util.ArrayList<>();
        UUID targetId = null;
        if (!entries.isEmpty()) {
            var entry = entries.get(page); var record = entry.record(); targetId = record.target();
            ServerLevel level = player.server.getLevel(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, entry.key().dimension()));
            Entity target = level == null ? null : level.getEntity(record.target());
            Component name = target == null ? (record.targetName().isBlank() ? Component.translatable("mca_vamp_compat.journal.unknown") : Component.literal(record.targetName())) : target.getName();
            lines.add(Component.translatable("mca_vamp_compat.journal.requester", name));
            if (target == null) lines.add(Component.translatable("mca_vamp_compat.journal.unavailable"));
            var pos = entry.key().totemPosition();
            lines.add(Component.translatable("mca_vamp_compat.journal.location", pos.getX(), pos.getY(), pos.getZ()));
            if (!record.villageName().isBlank()) lines.add(Component.literal(record.villageName()));
            lines.add(Component.translatable("mca_vamp_compat.journal.dimension", entry.key().dimension().toString()));
            lines.add(record.type().isUrgentAid() ? Component.translatable("mca_vamp_compat.journal.treatment")
                    : Component.translatable("mca_vamp_compat.journal.delivery", record.requiredCount(), requiredItemName(record.type(), item(record.type()))));
            lines.add(Component.translatable("mca_vamp_compat.village_mission.reward", record.reward()));
            lines.add(Component.translatable("mca_vamp_compat.journal.deadline", record.hasDeadline()
                    ? Component.translatable("mca_vamp_compat.journal.minutes", Math.max(0L, (record.expiresAtTick()-now+1199L)/1200L))
                    : Component.translatable("mca_vamp_compat.village_mission.time.none")));
        } else lines.add(Component.translatable("mca_vamp_compat.journal.empty"));
        com.guilh.mca_vampirism_compat.network.ModNetwork.sendToPlayer(player, new com.guilh.mca_vampirism_compat.network.MissionJournalPayload(session.token, open, page, entries.size(), targetId,
                targetId != null && targetId.equals(saved.tracked(player.getUUID())), java.util.List.copyOf(lines)));
    }

    private OccultVillageMissionService() {
    }

    public static void onPlayerTick(ServerPlayer player) {
        if (player == null || player.isSpectator() || player.tickCount % CHECK_INTERVAL_TICKS != 0
                || !McaVampirismCompatConfig.enableOccultVillageMissions()
                || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        OccultVillageMissionSavedData data = data(player.server);
        long now = level.getGameTime();
        expirePastMissions(player, data, now);

        Optional<TotemBlockEntity> totem = McaVillageEffectBoundsService.findActiveTotemAt(level, player.blockPosition());
        if (totem.isEmpty()) {
            return;
        }

        OccultVillageMissionSavedData.MissionKey key = key(level, totem.get().getBlockPos());
        Optional<OccultVillageMissionSavedData.MissionRecord> existing = data.get(player.getUUID(), key);
        if (existing.isPresent() && existing.get().isActive()) {
            if (expireMissionIfNeeded(player, data, key, existing.get(), now)) {
                return;
            }
            if (isMissionValid(player, level, totem.get(), existing.get(), true)) {
                if (existing.get().isAccepted() && existing.get().target().equals(data.tracked(player.getUUID()))) {
                    syncHighlight(player, existing.get().target(), true);
                }
            } else {
                abandon(data, player, key, existing.get(), now, "invalid during village check");
            }
        }
    }

    /**
     * Handles only a matching active supply request. Other MCA gifts return
     * false and remain entirely under MCA's native relationship code.
     */
    public static boolean handleGiftCommand(ServerPlayer player, VillagerEntityMCA villager) {
        if (player == null || villager == null || !McaVampirismCompatConfig.enableOccultVillageMissions()
                || !(player.level() instanceof ServerLevel level)) {
            return false;
        }
        Optional<TotemBlockEntity> totem = McaVillageEffectBoundsService.findActiveTotemAt(level, villager.blockPosition());
        if (totem.isEmpty()) {
            return false;
        }
        OccultVillageMissionSavedData.MissionKey key = key(level, totem.get().getBlockPos());
        OccultVillageMissionSavedData data = data(player.server);
        Optional<OccultVillageMissionSavedData.MissionRecord> active = data.get(player.getUUID(), key)
                .filter(OccultVillageMissionSavedData.MissionRecord::isActive);
        if (active.isEmpty() || !villager.getUUID().equals(active.get().target())) {
            return false;
        }

        OccultVillageMissionSavedData.MissionRecord mission = active.get();
        if (expireMissionIfNeeded(player, data, key, mission, level.getGameTime())) {
            return false;
        }
        if (!isMissionValid(player, level, totem.get(), mission, true)) {
            abandon(data, player, key, mission, level.getGameTime(), "invalid during gift");
            return false;
        }
        if (!mission.isAccepted()) {
            return false;
        }
        if (mission.type().isUrgentAid()) {
            return false;
        }

        ItemStack stack = player.getMainHandItem();
        Item required = item(mission.type());
        if (required == null || !stack.is(required)) {
            return false;
        }
        if (countRequiredItems(player, mission.type(), required) < mission.requiredCount()) {
            player.displayClientMessage(Component.translatable(
                "mca_vamp_compat.village_mission.not_enough",
                highlightedQuantity(mission.requiredCount()), requiredItemName(mission.type(), required)
            ), true);
            return true;
        }

        consumeRequiredItems(player, mission.type(), required, mission.requiredCount());
        player.getInventory().setChanged();
        completeSupplyMission(player, villager, key, mission);
        return true;
    }

    /** Called only after the existing milk treatment has cleared the infection. */
    public static void onMilkTreatment(ServerPlayer player, VillagerEntityMCA villager) {
        if (player == null || villager == null || !McaVampirismCompatConfig.enableOccultVillageMissions()
                || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        Optional<TotemBlockEntity> totem = McaVillageEffectBoundsService.findActiveTotemAt(level, villager.blockPosition());
        if (totem.isEmpty()) {
            return;
        }
        OccultVillageMissionSavedData.MissionKey key = key(level, totem.get().getBlockPos());
        OccultVillageMissionSavedData data = data(player.server);
        Optional<OccultVillageMissionSavedData.MissionRecord> active = data.get(player.getUUID(), key)
                .filter(record -> record.isActive() && record.type().isUrgentAid()
                    && record.isAccepted() && villager.getUUID().equals(record.target()));
        if (active.isEmpty() || expireMissionIfNeeded(player, data, key, active.get(), level.getGameTime())
                || !isMissionValid(player, level, totem.get(), active.get(), false)) {
            return;
        }
        rewardAndComplete(player, villager, key, active.get());
        McaAdvancementService.award(player, McaCompatAdvancements.URGENT_HUNTER_AID);
    }

    /**
     * Filters the MCA main conversation so special-village favors are only
     * offered by a valid resident. The server repeats the same validation
     * when an answer is selected; this filter is purely the visible affordance.
     */
    public static List<String> filterDialogueAnswers(String question, List<String> answers,
                                                     ServerPlayer player, VillagerEntityMCA villager) {
        if (!MAIN_DIALOGUE.equals(question) || answers == null) {
            return answers;
        }
        List<String> filtered = new java.util.ArrayList<>(answers);
        filtered.remove(OFFER_ANSWER);
        filtered.remove(STATUS_ANSWER);
        dialogueContext(player, villager).ifPresent(context -> {
            if (context.record() != null && context.record().isActive()) {
                // An unanswered request created by the old automatic system
                // may point at a resident the player cannot identify. Let the
                // first compatible resident take it over through the normal
                // offer entry instead of silently blocking this whole village.
                filtered.add(context.legacyPendingProxy() ? OFFER_ANSWER : STATUS_ANSWER);
            } else if (context.offerType() != null) {
                filtered.add(OFFER_ANSWER);
            }
        });
        return filtered;
    }

    /** Handles the three mission dialogue pages before MCA runs their JSON fallback actions. */
    public static boolean handleDialogueAnswer(ServerPlayer player, VillagerEntityMCA villager,
                                                String question, String answer) {
        if (player == null || villager == null || question == null || answer == null) {
            return false;
        }
        if (MAIN_DIALOGUE.equals(question) && (OFFER_ANSWER.equals(answer) || STATUS_ANSWER.equals(answer))) {
            return openMissionDialogue(player, villager, OFFER_ANSWER.equals(answer));
        }
        if (PENDING_DIALOGUE.equals(question)) {
            return switch (answer) {
                case ACCEPT_ANSWER -> acceptFromDialogue(player, villager);
                case DECLINE_ANSWER -> declineFromDialogue(player, villager);
                default -> true;
            };
        }
        if (ACTIVE_DIALOGUE.equals(question)) {
            return switch (answer) {
                case PROGRESS_ANSWER -> showActiveDialogue(player, villager);
                case CANCEL_ANSWER -> cancelFromDialogue(player, villager);
                default -> true;
            };
        }
        return false;
    }

    private static boolean openMissionDialogue(ServerPlayer player, VillagerEntityMCA villager, boolean creatingOffer) {
        Optional<DialogueContext> optionalContext = dialogueContext(player, villager);
        if (optionalContext.isEmpty()) {
            return true;
        }
        DialogueContext context = optionalContext.get();
        if (context.record() != null && context.record().isActive()) {
            OccultVillageMissionSavedData.MissionRecord record = context.record();
            if (context.legacyPendingProxy()) {
                record = reassignLegacyPendingOffer(player, context, villager);
            }
            if (record.isPending()) {
                showDialogue(player, villager, PENDING_DIALOGUE, offerNarration(record));
            } else {
                showActiveDialogue(player, villager);
            }
            return true;
        }
        if (!creatingOffer || context.offerType() == null) {
            return true;
        }

        long now = context.level().getGameTime();
        OccultVillageMissionSavedData.MissionRecord offer = new OccultVillageMissionSavedData.MissionRecord(
                context.offerType(), villager.getUUID(), now,
                now + McaVampirismCompatConfig.occultVillageMissionCooldownTicks(), false,
                context.offerType().randomRequiredCount(player.getRandom()), -1L,
                McaVampirismCompatConfig.missionReward(context.offerType()), villager.getName().getString(), ""
        );
        data(player.server).put(player.getUUID(), context.key(), offer);
        showDialogue(player, villager, PENDING_DIALOGUE, offerNarration(offer));
        McaVampirismCompat.debugLog(
                "[VILLAGE-MISSION] offered by conversation player={} totem={} type={} target={}",
                player.getUUID(), context.totem().getBlockPos(), offer.type(), villager.getUUID()
        );
        return true;
    }

    private static boolean acceptFromDialogue(ServerPlayer player, VillagerEntityMCA villager) {
        Optional<DialogueContext> optionalContext = dialogueContext(player, villager)
                .filter(context -> context.record() != null && context.record().isPending());
        if (optionalContext.isEmpty()) {
            return true;
        }
        DialogueContext context = optionalContext.get();
        long now = context.level().getGameTime();
        OccultVillageMissionSavedData.MissionRecord accepted = new OccultVillageMissionSavedData.MissionRecord(
                context.record().type(), context.record().target(), context.record().createdTick(),
                context.record().nextAvailableTick(), true, context.record().requiredCount(),
                now + McaVampirismCompatConfig.occultVillageMissionDeadlineTicks(),
                context.record().reward(), villager.getName().getString(), context.record().villageName()
        );
        data(player.server).put(player.getUUID(), context.key(), accepted);
        selectTrackedMission(player, villager.getUUID());
        sendChatAndClose(player, villager, acceptedNarration(accepted, now));
        return true;
    }

    private static boolean declineFromDialogue(ServerPlayer player, VillagerEntityMCA villager) {
        Optional<DialogueContext> optionalContext = dialogueContext(player, villager)
                .filter(context -> context.record() != null && context.record().isPending());
        if (optionalContext.isEmpty()) {
            return true;
        }
        DialogueContext context = optionalContext.get();
        abandon(data(player.server), player, context.key(), context.record(), context.level().getGameTime(),
                "declined in dialogue");
        sendChatAndClose(player, villager,
                Component.translatable("mca_vamp_compat.village_mission.dialogue.declined"));
        return true;
    }

    private static boolean showActiveDialogue(ServerPlayer player, VillagerEntityMCA villager) {
        Optional<DialogueContext> optionalContext = dialogueContext(player, villager)
                .filter(context -> context.record() != null && context.record().isAccepted());
        if (optionalContext.isEmpty()) {
            return true;
        }
        DialogueContext context = optionalContext.get();
        if (expireMissionIfNeeded(player, data(player.server), context.key(), context.record(),
                context.level().getGameTime())) {
            sendChatAndClose(player, villager,
                    Component.translatable("mca_vamp_compat.village_mission.expired"));
            return true;
        }
        sendChatAndClose(player, villager,
                activeNarration(context.record(), context.level().getGameTime()));
        return true;
    }

    private static boolean cancelFromDialogue(ServerPlayer player, VillagerEntityMCA villager) {
        Optional<DialogueContext> optionalContext = dialogueContext(player, villager)
                .filter(context -> context.record() != null && context.record().isAccepted());
        if (optionalContext.isEmpty()) {
            return true;
        }
        DialogueContext context = optionalContext.get();
        abandon(data(player.server), player, context.key(), context.record(), context.level().getGameTime(),
                "canceled in dialogue");
        sendChatAndClose(player, villager,
                Component.translatable("mca_vamp_compat.village_mission.dialogue.canceled"));
        return true;
    }

    private static void completeSupplyMission(ServerPlayer player, VillagerEntityMCA villager,
                                              OccultVillageMissionSavedData.MissionKey key,
                                              OccultVillageMissionSavedData.MissionRecord mission) {
        rewardAndComplete(player, villager, key, mission);
    }

    private static void rewardAndComplete(ServerPlayer player, VillagerEntityMCA villager,
                                          OccultVillageMissionSavedData.MissionKey key,
                                          OccultVillageMissionSavedData.MissionRecord mission) {
        OccultVillageMissionSavedData data = data(player.server);
        data.put(player.getUUID(), key, new OccultVillageMissionSavedData.MissionRecord(
                null, null, mission.createdTick(), mission.nextAvailableTick(), false, 0
        ));
        villager.getVillagerBrain().rewardHearts(player, mission.reward());
        syncHighlight(player, villager.getUUID(), false);
        McaAdvancementService.award(player, mission.type().advancementId());
        player.displayClientMessage(Component.translatable(
                "mca_vamp_compat.village_mission.completed", mission.reward()
        ), true);
        McaVampirismCompat.debugLog(
                "[VILLAGE-MISSION] completed player={} totem={} type={} target={} hearts={}",
                player.getUUID(), key.totemPosition(), mission.type(), villager.getUUID(), mission.reward()
        );
    }

    private static void abandon(OccultVillageMissionSavedData data, ServerPlayer player,
                                OccultVillageMissionSavedData.MissionKey key,
                                OccultVillageMissionSavedData.MissionRecord mission, long now, String reason) {
        data.put(player.getUUID(), key, new OccultVillageMissionSavedData.MissionRecord(
                null, null, mission.createdTick(), nextAvailableAfterCancellation(mission, now), false, 0
        ));
        syncHighlight(player, mission.target(), false);
        McaVampirismCompat.debugLog(
                "[VILLAGE-MISSION] canceled player={} totem={} type={} target={} reason={}",
                player.getUUID(), key.totemPosition(), mission.type(), mission.target(), reason
        );
    }

    private static void expirePastMissions(ServerPlayer player, OccultVillageMissionSavedData data, long now) {
        for (OccultVillageMissionSavedData.MissionEntry entry : data.activeMissions(player.getUUID())) {
            expireMissionIfNeeded(player, data, entry.key(), entry.record(), now);
        }
    }

    private static boolean expireMissionIfNeeded(ServerPlayer player, OccultVillageMissionSavedData data,
                                                  OccultVillageMissionSavedData.MissionKey key,
                                                  OccultVillageMissionSavedData.MissionRecord mission, long now) {
        // Pending requests deliberately have no deadline. Some earlier versions
        // wrote one before acceptance; those records become untimed offers here.
        if (!mission.isAccepted() || !mission.isExpired(now)) {
            return false;
        }
        abandon(data, player, key, mission, now, "deadline elapsed");
        player.displayClientMessage(Component.translatable("mca_vamp_compat.village_mission.expired"), true);
        return true;
    }

    private static long nextAvailableAfterCancellation(OccultVillageMissionSavedData.MissionRecord mission, long now) {
        return Math.max(mission.nextAvailableTick(), now + McaVampirismCompatConfig.occultVillageMissionCooldownTicks());
    }

    private static Optional<OccultVillageMissionType> selectTypeForVillager(ServerPlayer player, ServerLevel level,
                                                                              TotemBlockEntity totem,
                                                                              VillagerEntityMCA villager) {
        String faction = McaVillageEffectBoundsService.factionId(totem);
        if (villager == null || !isEligibleTotem(level, player, totem)
                || !contains(McaVillageEffectBoundsService.getEffectiveArea(totem), villager.blockPosition())) {
            return Optional.empty();
        }
        List<OccultVillageMissionType> available = new java.util.ArrayList<>();
        for (OccultVillageMissionType type : OccultVillageMissionType.values()) {
            if (!faction.equals(type.villageFactionId()) || !isTypeAvailable(type) || !isCandidate(villager, type)) {
                continue;
            }
            if (type.isAfterAttack() && !data(player.server).attackedRecently(key(level, totem.getBlockPos()), level.getGameTime())) continue;
            available.add(type);
        }
        if (available.isEmpty()) {
            return Optional.empty();
        }
        int priority = available.stream().mapToInt(OccultVillageMissionType::priority).max().orElse(0);
        available.removeIf(type -> type.priority() != priority);
        return Optional.of(available.get(player.getRandom().nextInt(available.size())));
    }

    private static Optional<DialogueContext> dialogueContext(ServerPlayer player, VillagerEntityMCA villager) {
        if (player == null || villager == null || !McaVampirismCompatConfig.enableOccultVillageMissions()
                || !(player.level() instanceof ServerLevel level) || villager.level() != level) {
            return Optional.empty();
        }
        return McaVillageEffectBoundsService.findActiveTotemAt(level, villager.blockPosition()).flatMap(totem -> {
            if (!isEligibleTotem(level, player, totem)) {
                return Optional.empty();
            }
            OccultVillageMissionSavedData.MissionKey key = key(level, totem.getBlockPos());
            OccultVillageMissionSavedData.MissionRecord record = data(player.server).get(player.getUUID(), key).orElse(null);
            long now = level.getGameTime();
            if (record != null && record.isActive()) {
                if (!record.target().equals(villager.getUUID())) {
                    if (record.isLegacyPending() && isCandidate(villager, record.type())) {
                        return Optional.of(new DialogueContext(level, totem, key, record, null, true));
                    }
                    return Optional.empty();
                }
                if (expireMissionIfNeeded(player, data(player.server), key, record, now)) {
                    return Optional.empty();
                }
                if (!isMissionValid(player, level, totem, record, true)) {
                    abandon(data(player.server), player, key, record, now, "invalid during dialogue");
                    return Optional.empty();
                }
                return Optional.of(new DialogueContext(level, totem, key, record, null, false));
            }
            if (record != null && now < record.nextAvailableTick()) {
                return Optional.empty();
            }
            return selectTypeForVillager(player, level, totem, villager)
                    .map(type -> new DialogueContext(level, totem, key, null, type, false));
        });
    }

    /**
     * Converts only pre-conversation pending records. Their requested type and
     * amount survive, but the resident and deadline are rebuilt when the
     * player explicitly opens the request with an eligible resident.
     */
    private static OccultVillageMissionSavedData.MissionRecord reassignLegacyPendingOffer(
            ServerPlayer player, DialogueContext context, VillagerEntityMCA villager) {
        OccultVillageMissionSavedData.MissionRecord migrated = new OccultVillageMissionSavedData.MissionRecord(
                context.record().type(), villager.getUUID(), context.record().createdTick(),
                context.record().nextAvailableTick(), false, context.record().requiredCount(), -1L,
                context.record().reward(), villager.getName().getString(), context.record().villageName()
        );
        data(player.server).put(player.getUUID(), context.key(), migrated);
        McaVampirismCompat.debugLog(
                "[VILLAGE-MISSION] reassigned legacy pending offer player={} totem={} type={} from={} to={}",
                player.getUUID(), context.totem().getBlockPos(), migrated.type(), context.record().target(), villager.getUUID()
        );
        return migrated;
    }

    private static boolean isMissionValid(ServerPlayer player, ServerLevel level, TotemBlockEntity totem,
                                          OccultVillageMissionSavedData.MissionRecord mission,
                                          boolean requireCurrentTargetState) {
        if (!mission.isActive() || !isTypeAvailable(mission.type()) || !isEligibleTotem(level, player, totem)
                || !mission.type().villageFactionId().equals(McaVillageEffectBoundsService.factionId(totem))) {
            return false;
        }
        AABB area = McaVillageEffectBoundsService.getEffectiveArea(totem);
        if (area == null || !contains(area, player.blockPosition())) {
            return false;
        }
        // Mission records already own the resident UUID. Looking up every MCA
        // villager in the village AABB made active missions scale with the
        // whole village footprint, which is especially costly now that MCA
        // villages deliberately have no artificial horizontal size limit.
        Entity entity = level.getEntity(mission.target());
        // A UUID lookup never loads a chunk. Absence is not evidence of death.
        if (entity == null) return true;
        if (!(entity instanceof VillagerEntityMCA target)
                || !target.isAlive() || target.isRemoved()
                || !contains(area, target.blockPosition())) {
            return false;
        }
        return !requireCurrentTargetState || isCandidate(target, mission.type());
    }

    private static boolean isCandidate(VillagerEntityMCA villager, OccultVillageMissionType type) {
        if (villager == null || villager.isRemoved() || !villager.isAlive()
                || villager.getAgeState() != AgeState.ADULT) {
            return false;
        }
        if (type.isSpecialist()) {
            String expected = type.villageFactionId().replace(":vampire", ":vampire_expert").replace(":hunter", ":hunter_expert").replace(":werewolf", ":werewolf_expert");
            if (!expected.equals(BuiltInRegistries.VILLAGER_PROFESSION.getKey(villager.getVillagerData().getProfession()).toString())) return false;
        }
        return switch (type) {
            case VAMPIRE_BLOOD, VAMPIRE_FANG, VAMPIRE_AFTER_ATTACK, VAMPIRE_SPECIALIST -> McaVampireStateService.isVampire(villager);
            case HUNTER_GARLIC, HUNTER_GARLIC_INJECTION, HUNTER_AFTER_ATTACK, HUNTER_SPECIALIST -> McaHunterAlignmentService.isMcaHunterVillager(villager);
            case WEREWOLF_MUTTON, WEREWOLF_AFTER_ATTACK, WEREWOLF_SPECIALIST -> McaWerewolfStateService.isWerewolf(villager);
            case HUNTER_URGENT_AID -> !McaVampireStateService.isVampire(villager)
                    && !McaWerewolfStateService.isWerewolf(villager)
                    && !McaWerewolfStateService.isWerewolfInfected(villager)
                    && (villager.hasEffect(VampirismEffectCompat.sanguinare())
                    || ModCapabilities.get(villager).map(state -> state.isInfected()).orElse(false));
        };
    }

    private static boolean isEligibleTotem(ServerLevel level, ServerPlayer player, TotemBlockEntity totem) {
        if (totem == null || totem.isRemoved() || totem.getLevel() != level
                || !McaVillageEffectBoundsService.isActiveVillageTotem(totem)) {
            return false;
        }
        String faction = McaVillageEffectBoundsService.factionId(totem);
        if (!OccultVillageFactionPolicy.isSupportedFactionId(faction) || !isPlayerEligible(player, faction)) {
            return false;
        }
        AABB area = McaVillageEffectBoundsService.getEffectiveArea(totem);
        return area != null && area.getSize() > 0.0D && contains(area, player.blockPosition());
    }

    private static boolean isPlayerEligible(ServerPlayer player, String villageFaction) {
        PlayerFactionContext context = McaVampireFactionService.classifyPlayerContext(player);
        PlayerFaction faction = context.effectiveFaction();
        return switch (villageFaction) {
            case "vampirism:vampire" -> faction == PlayerFaction.VAMPIRE || isHuman(faction);
            case "vampirism:hunter" -> faction == PlayerFaction.HUNTER || isHuman(faction);
            case "werewolves:werewolf" -> faction == PlayerFaction.WEREWOLF
                    || faction == PlayerFaction.TRANSFORMED_WEREWOLF || isHuman(faction);
            default -> false;
        };
    }

    private static boolean isHuman(PlayerFaction faction) {
        return faction == PlayerFaction.HUMAN || faction == PlayerFaction.HUMAN_DISGUISED;
    }

    private static boolean isTypeAvailable(OccultVillageMissionType type) {
        if (type.requiresWerewolves() && !ModList.get().isLoaded("werewolves")) {
            return false;
        }
        return type.requiredItem() == null || BuiltInRegistries.ITEM.containsKey(type.requiredItem());
    }

    private static Item item(OccultVillageMissionType type) {
        return type.requiredItem() == null ? null : BuiltInRegistries.ITEM.getOptional(type.requiredItem()).orElse(null);
    }

    private static Component offerNarration(OccultVillageMissionSavedData.MissionRecord mission) {
        Item required = item(mission.type());
        Component itemName = mission.type().isUrgentAid() || required == null
                ? Component.empty() : requiredItemName(mission.type(), required);
        return Component.translatable(mission.type().translationKey() + ".request",
                highlightedQuantity(mission.requiredCount()), itemName)
                .append(Component.literal(" ")).append(Component.translatable("mca_vamp_compat.village_mission.reward", mission.reward()));
    }

    private static Component acceptedNarration(OccultVillageMissionSavedData.MissionRecord mission, long now) {
        return missionNarration(mission, now, ".accepted");
    }

    private static Component activeNarration(OccultVillageMissionSavedData.MissionRecord mission, long now) {
        return missionNarration(mission, now, ".active");
    }

    private static Component missionNarration(OccultVillageMissionSavedData.MissionRecord mission, long now,
                                               String suffix) {
        Item required = item(mission.type());
        Component remainingTime = mission.hasDeadline()
                ? Component.translatable("mca_vamp_compat.village_mission.time.remaining",
                        formattedRemainingTime(Math.max(0L, mission.expiresAtTick() - now)))
                : Component.translatable("mca_vamp_compat.village_mission.time.unlimited");
        if (mission.type().isUrgentAid()) {
            return Component.translatable(mission.type().translationKey() + suffix, remainingTime);
        }
        Component itemName = required == null ? Component.empty() : requiredItemName(mission.type(), required);
        return Component.translatable(mission.type().translationKey() + suffix,
                highlightedQuantity(mission.requiredCount()), itemName, remainingTime);
    }

    // Presentation only: the accepted deadline is neither rounded nor changed.
    static Component formattedRemainingTime(long remainingTicks) {
        boolean days = remainingTicks >= 24_000L;
        long unit = days ? 24_000L : 1_200L;
        long ticks = Math.max(0L, remainingTicks);
        long amount = Math.max(1L, ticks / unit + (ticks % unit == 0L ? 0L : 1L));
        String suffix = days ? (amount == 1L ? "day" : "days") : (amount == 1L ? "minute" : "minutes");
        return Component.translatable("mca_vamp_compat.village_mission.time." + suffix,
                highlightedQuantity(amount));
    }

    private static void showDialogue(ServerPlayer player, VillagerEntityMCA villager,
                                      String questionId, Component phrase) {
        Question question = Dialogues.getInstance().getQuestion(questionId);
        if (question == null) {
            McaVampirismCompat.LOGGER.warn("[VILLAGE-MISSION] Missing MCA dialogue question {}", questionId);
            return;
        }
        NetworkHandler.sendToPlayer(new InteractionDialogueResponse(question, player, villager), player);
        sendChatPhrase(player, villager, phrase);
    }

    private static void sendChatAndClose(ServerPlayer player, VillagerEntityMCA villager, Component phrase) {
        if (player == null || villager == null) {
            return;
        }
        sendChatPhrase(player, villager, phrase);
        ModNetwork.sendToPlayer(player, new MissionDialogueClosePayload(villager.getUUID()));
    }

    private static void sendChatPhrase(ServerPlayer player, VillagerEntityMCA villager, Component phrase) {
        if (player != null && villager != null && phrase != null) {
            ModNetwork.sendToPlayer(player, new MissionDialogueSpeechPayload(villager.getUUID(), phrase));
        }
    }

    private static Component highlightedQuantity(long amount) {
        return Component.literal(Long.toString(amount)).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD);
    }

    private static Component requiredItemName(OccultVillageMissionType type, Item required) {
        if (type.requiresFullBlood()) {
            return Component.translatable("mca_vamp_compat.village_mission.vampire_blood.item");
        }
        return required == null ? Component.empty() : new ItemStack(required).getHoverName();
    }

    private static int countRequiredItems(ServerPlayer player, OccultVillageMissionType type, Item required) {
        int count = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack candidate = player.getInventory().getItem(slot);
            if (isRequiredItem(type, required, candidate)) {
                count += candidate.getCount();
            }
        }
        return count;
    }

    private static void consumeRequiredItems(ServerPlayer player, OccultVillageMissionType type, Item required, int amount) {
        int remaining = amount;
        for (int slot = 0; slot < player.getInventory().getContainerSize() && remaining > 0; slot++) {
            ItemStack candidate = player.getInventory().getItem(slot);
            if (!isRequiredItem(type, required, candidate)) {
                continue;
            }
            int consumed = Math.min(remaining, candidate.getCount());
            candidate.shrink(consumed);
            remaining -= consumed;
        }
    }

    private static boolean isRequiredItem(OccultVillageMissionType type, Item required, ItemStack stack) {
        if (stack == null || !stack.is(required)) {
            return false;
        }
        if (!type.requiresFullBlood()) {
            return true;
        }
        return hasRequiredBloodAmount(BloodHelper.getBlood(stack));
    }

    static boolean hasRequiredBloodAmount(int bloodAmount) {
        return bloodAmount >= BloodBottleItem.AMOUNT;
    }

    public static void selectTrackedMission(ServerPlayer player, UUID target) {
        OccultVillageMissionSavedData saved = data(player.server);
        if (target != null && saved.activeMissions(player.getUUID()).stream().noneMatch(entry -> entry.record().isAccepted() && target.equals(entry.record().target()))) return;
        UUID previous = saved.tracked(player.getUUID());
        if (previous != null) syncHighlight(player, previous, false);
        saved.track(player.getUUID(), target);
        if (target != null) syncHighlight(player, target, true);
    }

    /** All world queries run on the server thread and explicitly avoid loading chunks. */
    public static void serverTick(MinecraftServer server) {
        long now = server.overworld().getGameTime();
        if (now % 200L != 0L) return;
        OccultVillageMissionSavedData saved = data(server);
        saved.prune(now, 64);
        for (var owner : saved.validationBatch(32)) {
            var record = saved.get(owner.player(), owner.key()).orElse(null);
            if (record == null || !record.isActive()) continue;
            ServerLevel level = server.getLevel(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, owner.key().dimension()));
            boolean invalid = record.isExpired(now);
            if (!invalid && level != null && level.hasChunkAt(owner.key().totemPosition())) {
                BlockEntity block = level.getBlockEntity(owner.key().totemPosition());
                invalid = !(block instanceof TotemBlockEntity totem)
                        || !McaVillageEffectBoundsService.isActiveVillageTotem(totem)
                        || !record.type().villageFactionId().equals(McaVillageEffectBoundsService.factionId(totem));
            }
            if (invalid) terminate(server, saved, owner, record, now);
        }
    }

    private static void terminate(MinecraftServer server, OccultVillageMissionSavedData saved,
                                  OccultVillageMissionSavedData.OwnerKey owner,
                                  OccultVillageMissionSavedData.MissionRecord record, long now) {
        saved.put(owner.player(), owner.key(), new OccultVillageMissionSavedData.MissionRecord(
                null, null, record.createdTick(), nextAvailableAfterCancellation(record, now), false));
        ServerPlayer player = server.getPlayerList().getPlayer(owner.player());
        if (player != null) syncHighlight(player, record.target(), false);
    }

    public static void confirmedDeath(VillagerEntityMCA villager) {
        if (!(villager.level() instanceof ServerLevel level)) return;
        OccultVillageMissionSavedData saved = data(level.getServer());
        for (var owner : saved.missionsFor(villager.getUUID())) {
            saved.get(owner.player(), owner.key()).ifPresent(record -> terminate(level.getServer(), saved, owner, record, level.getGameTime()));
        }
    }

    public static void recordAttack(ServerLevel level, BlockPos totem) {
        data(level.getServer()).recordAttack(key(level, totem), level.getGameTime());
    }

    public static void confirmedDamage(VillagerEntityMCA resident, net.minecraft.world.entity.LivingEntity attacker) {
        if (!(resident.level() instanceof ServerLevel level) || attacker instanceof net.minecraft.world.entity.player.Player) return;
        String faction = npcFaction(attacker);
        if (faction.isEmpty()) return;
        McaVillageEffectBoundsService.findActiveTotemAt(level, resident.blockPosition()).ifPresent(totem -> {
            String village = McaVillageEffectBoundsService.factionId(totem);
            if (OccultVillageFactionPolicy.isSupportedFactionId(village) && !village.equals(faction)) recordAttack(level, totem.getBlockPos());
        });
    }

    private static String npcFaction(net.minecraft.world.entity.LivingEntity entity) {
        if (com.guilh.mca_vampirism_compat.util.VampirismUtil.isVampiric(entity)) return "vampirism:vampire";
        if (McaVampireFactionService.isHunter(entity)) return "vampirism:hunter";
        if (McaVampireFactionService.isWerewolf(entity)) return "werewolves:werewolf";
        return "";
    }

    private static void syncHighlight(ServerPlayer player, UUID villager, boolean highlighted) {
        if (player != null && villager != null) {
            ModNetwork.sendToPlayer(player, new MissionHighlightPayload(villager, highlighted));
        }
    }

    private static boolean contains(AABB area, BlockPos position) {
        return area != null && position != null
                && area.contains(position.getX() + 0.5D, position.getY() + 0.5D, position.getZ() + 0.5D);
    }

    private static OccultVillageMissionSavedData.MissionKey key(ServerLevel level, BlockPos totemPosition) {
        return new OccultVillageMissionSavedData.MissionKey(level.dimension().location(), totemPosition.immutable());
    }

    private static OccultVillageMissionSavedData data(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                OccultVillageMissionSavedData::load,
                OccultVillageMissionSavedData::new,
                OccultVillageMissionSavedData.ID
        );
    }

    private record DialogueContext(ServerLevel level, TotemBlockEntity totem,
                                   OccultVillageMissionSavedData.MissionKey key,
                                   OccultVillageMissionSavedData.MissionRecord record,
                                   OccultVillageMissionType offerType,
                                   boolean legacyPendingProxy) {
    }
}
