package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.service.faction.hunter.McaHunterAggressionPolicy;
import com.guilh.mca_vampirism_compat.trait.McaTraitBridge;
import de.teamlapen.vampirism.api.entity.factions.IFaction;
import de.teamlapen.vampirism.api.event.VampirismVillageEvent;
import de.teamlapen.vampirism.api.world.ITotem;
import de.teamlapen.vampirism.core.ModEffects;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.relationship.AgeState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class McaVillageCaptureIntegrationService {
    private static final String VAMPIRE_FACTION_ID = "vampirism:vampire";
    private static final String HUNTER_FACTION_ID = "vampirism:hunter";
    private static final Map<String, List<UUID>> PENDING_MCA_CAPTURE_VILLAGERS = new LinkedHashMap<>();

    private McaVillageCaptureIntegrationService() {
    }

    public static void onInitiateCapture(VampirismVillageEvent.InitiateCapture event) {
        if (!McaVampirismCompatConfig.enableMcaVillageCaptureIntegration()) {
            return;
        }
        McaVampirismCompat.debugLog(
                "[VILLAGE-CAPTURE] event InitiateCapture faction={} controlling={}",
                factionId(event.getCapturingFaction()),
                factionId(event.getControllingFaction())
        );
    }

    public static void onDefineRaidStrength(VampirismVillageEvent.DefineRaidStrength event) {
        if (!McaVampirismCompatConfig.enableMcaVillageCaptureIntegration()) {
            return;
        }
        StrengthBreakdown breakdown = calculateStrengthBreakdown(event.getTotem());
        if (breakdown.attack == 0.0F && breakdown.defense == 0.0F) {
            return;
        }
        event.setAttackStrength(Math.max(0.0F, event.getAttackStrength() + breakdown.attack));
        event.setDefendStrength(Math.max(0.0F, event.getDefendStrength() + breakdown.defense));
        McaVampirismCompat.debugLog(
                "[VILLAGE-CAPTURE] strength total mcaHuman={} mcaHunter={} mcaVampire={} mcaWerewolf={} mcaInfected={} mcaCuring={} mcaChild={} attackDelta={} defenseDelta={} attackTotal={} defenseTotal={}",
                breakdown.human,
                breakdown.hunter,
                breakdown.vampire,
                breakdown.werewolf,
                breakdown.infected,
                breakdown.curing,
                breakdown.child,
                breakdown.attack,
                breakdown.defense,
                event.getAttackStrength(),
                event.getDefendStrength()
        );
    }

    public static boolean onMakeAggressive(VampirismVillageEvent.MakeAggressive event) {
        if (!McaVampirismCompatConfig.enableMcaVillageCaptureIntegration()
                || !(event.getOldVillager() instanceof VillagerEntityMCA villager)
                || villager.level().isClientSide) {
            return false;
        }

        event.setCanceled(true);
        McaHunterAggressionPolicy.CaptureContext context = makeAggressiveContext(event.getTotem());
        boolean appliedHunter = false;
        if (!McaVampireStateService.isVampire(villager)
                && !(McaWerewolfStateService.isWerewolf(villager)
                && McaVampirismCompatConfig.hunterVillageDoesNotConvertWerewolvesToHunters())
                && McaHunterAggressionPolicy.canApplyMakeAggressiveAsHunter(villager, context)
                && McaVampirismCompatConfig.makeAggressiveOnlyCreatesMcaHuntersInHunterVillages()) {
            McaVillageFactionControlService.applyHunterVillageState(villager, McaHunterAggressionPolicy.HunterSource.MAKE_AGGRESSIVE);
            McaSpecialVillagerCombatService.registerGoalsIfNeeded(villager);
            appliedHunter = true;
        }

        McaVampirismCompat.debugLog(
                "[VILLAGE-CAPTURE] makeAggressive blocked replacement villager={} context={} appliedHunter={}",
                villager.getUUID(),
                context,
                appliedHunter
        );
        return true;
    }

    public static void onCaptureFinishPre(VampirismVillageEvent.VillagerCaptureFinish.Pre event) {
        if (!McaVampirismCompatConfig.enableMcaVillageCaptureIntegration()) {
            return;
        }
        List<VillagerEntityMCA> mcaVillagers = collectMcaVillagers(event.getVillager());
        if (mcaVillagers.isEmpty()) {
            return;
        }
        boolean removedFromVanillaList = removeMcaVillagersFromVanillaList(event.getVillager());
        rememberPreRemovedVillagers(event.getTotem(), event.getCapturingFaction(), event.getControllingFaction(), mcaVillagers, removedFromVanillaList);
        if (McaVampirismCompatConfig.mcaVillageCaptureDebugBreakdown()) {
            for (VillagerEntityMCA villager : mcaVillagers) {
                McaVampirismCompat.debugLog(
                        "[VILLAGE-CAPTURE] villager classified phase=PRE villager={} type={} removedFromVanillaList={}",
                        villager.getUUID(),
                        classifyMcaVillagerForCapture(villager),
                        removedFromVanillaList
                );
            }
        }
        McaVampirismCompat.debugLog(
                "[VILLAGE-CAPTURE] event CaptureFinish.Pre faction={} controlling={} mcaVillagers={} removedFromVanillaList={}",
                factionId(event.getCapturingFaction()),
                factionId(event.getControllingFaction()),
                mcaVillagers.size(),
                removedFromVanillaList
        );
    }

    public static void onCaptureFinishPost(VampirismVillageEvent.VillagerCaptureFinish.Post event) {
        if (!McaVampirismCompatConfig.enableMcaVillageCaptureIntegration()) {
            McaVillageFactionControlService.syncVillagersFromCaptureFinish(event.getTotem(), event.getVillager());
            return;
        }
        List<VillagerEntityMCA> mcaVillagers = collectCaptureFinishPostVillagers(event.getTotem(), event.getCapturingFaction(), event.getControllingFaction(), event.getVillager());
        if (mcaVillagers.isEmpty()) {
            return;
        }
        if (McaVampirismCompatConfig.mcaVillageCaptureDebugBreakdown()) {
            for (VillagerEntityMCA villager : mcaVillagers) {
                McaVampirismCompat.debugLog(
                        "[VILLAGE-CAPTURE] villager classified phase=POST villager={} type={}",
                        villager.getUUID(),
                        classifyMcaVillagerForCapture(villager)
                );
            }
        }
        McaVampirismCompat.debugLog(
                "[VILLAGE-CAPTURE] event CaptureFinish.Post faction={} controlling={} forced={} mcaVillagers={}",
                factionId(event.getCapturingFaction()),
                factionId(event.getControllingFaction()),
                event.isForced(),
                mcaVillagers.size()
        );

        if (isVampireFaction(event.getCapturingFaction())) {
            applyVampireCaptureResult(event.getTotem(), mcaVillagers);
        } else if (isHunterFaction(event.getCapturingFaction())) {
            applyHunterCaptureResult(mcaVillagers);
        } else if (isHunterFaction(event.getControllingFaction())) {
            removeHunterVillageState(mcaVillagers);
        } else {
            syncNeutralCaptureResult(mcaVillagers);
        }
    }

    private static StrengthBreakdown calculateStrengthBreakdown(ITotem totem) {
        StrengthBreakdown breakdown = new StrengthBreakdown();
        IFaction<?> capturingFaction = totem == null ? null : totem.getCapturingFaction();
        for (VillagerEntityMCA villager : collectMcaVillagers(totem, null)) {
            CaptureType type = classifyMcaVillagerForCapture(villager);
            switch (type) {
                case MCA_CHILD -> {
                    breakdown.child++;
                    addDefenseDeltaContribution(breakdown, villager, type, McaVampirismCompatConfig.mcaChildVillagerDefenseStrength());
                }
                case MCA_CURING -> {
                    breakdown.curing++;
                    addDefenseDeltaContribution(breakdown, villager, type, McaVampirismCompatConfig.mcaCuringVillagerDefenseStrength());
                }
                case MCA_VAMPIRE -> {
                    breakdown.vampire++;
                    addContextualDeltaContribution(breakdown, villager, type, isVampireFaction(capturingFaction),
                            McaVampirismCompatConfig.mcaVampireVillagerVampireStrength());
                }
                case MCA_HUNTER -> {
                    breakdown.hunter++;
                    addContextualDeltaContribution(breakdown, villager, type, isHunterFaction(capturingFaction),
                            McaVampirismCompatConfig.mcaHunterVillagerDefenseStrength());
                }
                case MCA_WEREWOLF -> {
                    breakdown.werewolf++;
                    logContribution(villager, type, 0.0D);
                }
                case MCA_INFECTED -> {
                    breakdown.infected++;
                    addDefenseDeltaContribution(breakdown, villager, type, McaVampirismCompatConfig.mcaInfectedVillagerDefenseStrength());
                }
                case MCA_HUMAN -> {
                    breakdown.human++;
                    logContribution(villager, type, 0.0D);
                }
            }
        }
        return breakdown;
    }

    public static CaptureType classifyMcaVillagerForCapture(VillagerEntityMCA villager) {
        if (McaSpecialVillagerClassifier.isCuring(villager)) {
            return CaptureType.MCA_CURING;
        }
        if (villager.getAgeState() != AgeState.ADULT) {
            return CaptureType.MCA_CHILD;
        }
        if (McaVampireStateService.isVampire(villager)) {
            return CaptureType.MCA_VAMPIRE;
        }
        if (McaHunterAlignmentService.isMcaHunterVillager(villager)) {
            return CaptureType.MCA_HUNTER;
        }
        if (McaWerewolfStateService.isWerewolf(villager)) {
            return CaptureType.MCA_WEREWOLF;
        }
        if (isInfected(villager)) {
            return CaptureType.MCA_INFECTED;
        }
        return CaptureType.MCA_HUMAN;
    }

    private static void applyVampireCaptureResult(ITotem totem, List<VillagerEntityMCA> villagers) {
        boolean hunterVillageLost = isHunterFaction(totem == null ? null : totem.getControllingFaction());
        for (VillagerEntityMCA villager : villagers) {
            CaptureType type = classifyMcaVillagerForCapture(villager);
            if (type == CaptureType.MCA_INFECTED) {
                accelerateExistingSanguinare(villager);
            } else if (shouldVampireCaptureTryInfect(villager, type)) {
                rollAndApplyVampireCaptureInfection(villager);
            } else if (type == CaptureType.MCA_HUNTER) {
                McaVampirismCompat.debugLog(
                        "[VILLAGE-CAPTURE] hunter resisted vampire capture infection villager={}",
                        villager.getUUID()
                );
            } else if (type == CaptureType.MCA_WEREWOLF) {
                McaVampirismCompat.debugLog(
                        "[VILLAGE-CAPTURE] werewolf resisted vampire capture infection villager={}",
                        villager.getUUID()
                );
            }

            if (hunterVillageLost) {
                McaVillageFactionControlService.removeHunterVillageState(villager);
            }
            logResult(villager, "VAMPIRE_CAPTURE", type);
        }
        McaVampirismCompat.debugLog(
                "[VILLAGE-CAPTURE] vampire village control does not grant vampire trait automatically"
        );
    }

    private static void applyHunterCaptureResult(List<VillagerEntityMCA> villagers) {
        for (VillagerEntityMCA villager : villagers) {
            CaptureType type = classifyMcaVillagerForCapture(villager);
            if (type == CaptureType.MCA_INFECTED && McaVampirismCompatConfig.hunterVillageCaptureCuresMcaSanguinare()) {
                clearSanguinare(villager, "HUNTER_CAPTURE");
                McaVampirismCompat.debugLog(
                        "[VILLAGE-CAPTURE] hunter capture cured sanguinare villager={}",
                        villager.getUUID()
                );
            }
            if (type == CaptureType.MCA_VAMPIRE) {
                if (McaVampirismCompatConfig.hunterVillageCaptureCuresMcaVampires()) {
                    McaVampireCureService.completeCure(villager);
                } else if (McaVampirismCompatConfig.hunterVillageCaptureStartsCureForMcaVampires()) {
                    McaVampireCureService.startVillageCaptureCure(villager);
                } else {
                    McaVampirismCompat.debugLog(
                            "[VILLAGE-CAPTURE] hunter capture left MCA vampire unchanged villager={} reason=CONFIG",
                            villager.getUUID()
                    );
                }
                logResult(villager, "HUNTER_CAPTURE", type);
                continue;
            }
            if (McaVampirismCompatConfig.hunterVillageCaptureGrantsHunterTrait()
                    && type != CaptureType.MCA_CHILD
                    && type != CaptureType.MCA_CURING
                    && !(type == CaptureType.MCA_WEREWOLF
                    && McaVampirismCompatConfig.hunterVillageDoesNotConvertWerewolvesToHunters())
                    && !McaVampireStateService.isVampire(villager)) {
                McaVillageFactionControlService.applyHunterVillageState(villager, McaHunterAggressionPolicy.HunterSource.HUNTER_CAPTURE_FINISHED);
                McaSpecialVillagerCombatService.registerGoalsIfNeeded(villager);
                McaVampirismCompat.debugLog(
                        "[VILLAGE-CAPTURE] hunter capture applied hunter state villager={}",
                        villager.getUUID()
                );
            }
            logResult(villager, "HUNTER_CAPTURE", type);
        }
    }

    private static void removeHunterVillageState(List<VillagerEntityMCA> villagers) {
        for (VillagerEntityMCA villager : villagers) {
            McaVillageFactionControlService.removeHunterVillageState(villager);
        }
    }

    private static void syncNeutralCaptureResult(List<VillagerEntityMCA> villagers) {
        for (VillagerEntityMCA villager : villagers) {
            McaVillageFactionControlService.syncHunterTraitFromVillageControl(villager);
            logResult(villager, "NEUTRAL_CAPTURE", classifyMcaVillagerForCapture(villager));
        }
    }

    private static boolean shouldVampireCaptureTryInfect(VillagerEntityMCA villager, CaptureType type) {
        if (!McaVampirismCompatConfig.vampireVillageCaptureInfectsMcaVillagers()) {
            return false;
        }
        if (McaVampirismCompatConfig.vampireVillageCaptureDoesNotInstantlyConvertMcaVillagers()
                && (type == CaptureType.MCA_VAMPIRE || type == CaptureType.MCA_CURING)) {
            return false;
        }
        if (type == CaptureType.MCA_CHILD && !McaVampirismCompatConfig.vampireVillageCaptureInfectsChildren()) {
            return false;
        }
        if (type == CaptureType.MCA_HUNTER && !McaVampirismCompatConfig.vampireVillageCaptureCanInfectHunterVillagers()) {
            return false;
        }
        if (type == CaptureType.MCA_WEREWOLF && McaVampirismCompatConfig.werewolfVillagersResistVampireCaptureInfection()) {
            return false;
        }
        return type == CaptureType.MCA_HUMAN || type == CaptureType.MCA_CHILD || type == CaptureType.MCA_HUNTER;
    }

    private static void rollAndApplyVampireCaptureInfection(VillagerEntityMCA villager) {
        if (isInfected(villager)) {
            McaVampirismCompat.debugLog(
                    "[VILLAGE-CAPTURE] sanguinare skipped villager={} reason=ALREADY_INFECTED",
                    villager.getUUID()
            );
            return;
        }
        double chance = McaVampirismCompatConfig.vampireVillageCaptureInfectionChance();
        double roll = villager.getRandom().nextDouble();
        McaVampirismCompat.debugLog(
                "[VILLAGE-CAPTURE] vampire capture infection roll villager={} roll={} chance={}",
                villager.getUUID(),
                roll,
                chance
        );
        if (roll > chance) {
            return;
        }
        boolean applied = McaVampireInfectionService.tryApplyConfiguredSanguinare(
                null,
                villager,
                McaVampireInfectionService.InfectionSourceType.VILLAGE_CAPTURE,
                true
        );
        if (applied) {
            McaVampirismCompat.debugLog(
                    "[VILLAGE-CAPTURE] sanguinare applied reason=VAMPIRE_CAPTURE duration={}",
                    McaVampireInfectionService.getConfiguredSanguinareDuration(McaVampireInfectionService.InfectionSourceType.VILLAGE_CAPTURE)
            );
        }
    }

    private static void accelerateExistingSanguinare(VillagerEntityMCA villager) {
        if (!McaVampirismCompatConfig.vampireVillageCaptureAcceleratesExistingSanguinare()) {
            return;
        }
        MobEffectInstance current = villager.getEffect(ModEffects.SANGUINARE.get());
        if (current == null) {
            return;
        }
        int currentDuration = current.getDuration();
        int newDuration = Math.max(20, (int) Math.floor(currentDuration * McaVampirismCompatConfig.vampireVillageCaptureAccelerationMultiplier()));
        if (newDuration >= currentDuration) {
            return;
        }
        villager.removeEffect(ModEffects.SANGUINARE.get());
        villager.addEffect(new MobEffectInstance(
                ModEffects.SANGUINARE.get(),
                newDuration,
                current.getAmplifier(),
                current.isAmbient(),
                current.isVisible(),
                current.showIcon()
        ));
        McaVampirismCompat.debugLog(
                "[VILLAGE-CAPTURE] sanguinare accelerated villager={} oldDuration={} newDuration={}",
                villager.getUUID(),
                currentDuration,
                newDuration
        );
    }

    private static void clearSanguinare(VillagerEntityMCA villager, String reason) {
        boolean removed = villager.removeEffect(ModEffects.SANGUINARE.get());
        villager.getCapability(ModCapabilities.VAMPIRIC_VILLAGER)
                .ifPresent(state -> state.clearPendingInfection());
        McaVampirismCompat.debugLog(
                "[VILLAGE-CAPTURE] sanguinare cleared villager={} removed={} reason={}",
                villager.getUUID(),
                removed,
                reason
        );
    }

    private static boolean isInfected(VillagerEntityMCA villager) {
        return villager.hasEffect(ModEffects.SANGUINARE.get())
                || villager.getCapability(ModCapabilities.VAMPIRIC_VILLAGER)
                .map(state -> state.isInfected())
                .orElse(false);
    }

    private static void addContextualDeltaContribution(StrengthBreakdown breakdown, VillagerEntityMCA villager,
                                                       CaptureType type, boolean supportsAttack, double configuredStrength) {
        if (supportsAttack) {
            breakdown.attack += (float) Math.max(0.0D, configuredStrength);
            logContribution(villager, type, Math.max(0.0D, configuredStrength));
            return;
        }
        double delta = configuredStrength - McaVampirismCompatConfig.mcaHumanVillagerDefenseStrength();
        breakdown.defense += (float) delta;
        logContribution(villager, type, delta);
    }

    private static void addDefenseDeltaContribution(StrengthBreakdown breakdown, VillagerEntityMCA villager,
                                                    CaptureType type, double configuredStrength) {
        double delta = configuredStrength - McaVampirismCompatConfig.mcaHumanVillagerDefenseStrength();
        breakdown.defense += (float) delta;
        logContribution(villager, type, delta);
    }

    private static void logContribution(VillagerEntityMCA villager, CaptureType type, double amount) {
        if (!McaVampirismCompatConfig.mcaVillageCaptureDebugBreakdown()) {
            return;
        }
        McaVampirismCompat.debugLog(
                "[VILLAGE-CAPTURE] strength contribution villager={} type={} delta={} baseSkipped=true",
                villager.getUUID(),
                type,
                amount
        );
    }

    private static boolean removeMcaVillagersFromVanillaList(List<Villager> villagers) {
        try {
            return villagers.removeIf(villager -> villager instanceof VillagerEntityMCA);
        } catch (UnsupportedOperationException exception) {
            McaVampirismCompat.debugLog(
                    "[VILLAGE-CAPTURE] failed to remove MCA villagers from vanilla capture list error={}",
                    exception.toString()
            );
            return false;
        }
    }

    private static List<VillagerEntityMCA> collectMcaVillagers(List<Villager> villagers) {
        List<VillagerEntityMCA> result = new ArrayList<>();
        if (villagers == null) {
            return result;
        }
        for (Villager villager : villagers) {
            if (villager instanceof VillagerEntityMCA mcaVillager) {
                result.add(mcaVillager);
            }
        }
        return result;
    }

    private static void rememberPreRemovedVillagers(ITotem totem, IFaction<?> capturingFaction, IFaction<?> controllingFaction,
                                                    List<VillagerEntityMCA> villagers, boolean removedFromVanillaList) {
        if (!removedFromVanillaList || villagers.isEmpty()) {
            return;
        }
        if (PENDING_MCA_CAPTURE_VILLAGERS.size() > 16) {
            PENDING_MCA_CAPTURE_VILLAGERS.clear();
        }
        List<UUID> ids = new ArrayList<>();
        for (VillagerEntityMCA villager : villagers) {
            ids.add(villager.getUUID());
        }
        PENDING_MCA_CAPTURE_VILLAGERS.put(captureKey(totem, capturingFaction, controllingFaction), ids);
    }

    private static List<VillagerEntityMCA> collectCaptureFinishPostVillagers(ITotem totem, IFaction<?> capturingFaction,
                                                                             IFaction<?> controllingFaction, List<Villager> eventVillagers) {
        Map<UUID, VillagerEntityMCA> villagers = new LinkedHashMap<>();
        for (VillagerEntityMCA villager : collectMcaVillagers(eventVillagers)) {
            villagers.put(villager.getUUID(), villager);
        }

        List<UUID> pendingIds = PENDING_MCA_CAPTURE_VILLAGERS.remove(captureKey(totem, capturingFaction, controllingFaction));
        if (pendingIds == null || pendingIds.isEmpty()) {
            return new ArrayList<>(villagers.values());
        }

        Level level = totem == null ? null : totem.getTileWorld();
        if (!(level instanceof ServerLevel serverLevel)) {
            return new ArrayList<>(villagers.values());
        }
        for (UUID id : pendingIds) {
            if (serverLevel.getEntity(id) instanceof VillagerEntityMCA villager) {
                villagers.put(villager.getUUID(), villager);
            }
        }
        return new ArrayList<>(villagers.values());
    }

    private static List<VillagerEntityMCA> collectMcaVillagers(ITotem totem, List<Villager> eventVillagers) {
        Map<UUID, VillagerEntityMCA> villagers = new LinkedHashMap<>();
        for (VillagerEntityMCA villager : collectMcaVillagers(eventVillagers)) {
            villagers.put(villager.getUUID(), villager);
        }
        Level level = totem == null ? null : totem.getTileWorld();
        AABB area = totemArea(totem);
        if (level instanceof ServerLevel serverLevel && area != null) {
            for (VillagerEntityMCA villager : serverLevel.getEntitiesOfClass(VillagerEntityMCA.class, area)) {
                villagers.put(villager.getUUID(), villager);
            }
        }
        return new ArrayList<>(villagers.values());
    }

    private static AABB totemArea(ITotem totem) {
        if (totem == null) {
            return null;
        }
        AABB area = totem.getVillageArea();
        if (area != null) {
            return area;
        }
        if (totem instanceof BlockEntity blockEntity) {
            BlockPos pos = blockEntity.getBlockPos();
            int radius = McaVampirismCompatConfig.mcaVillageCaptureStrengthRadius();
            return new AABB(pos).inflate(radius);
        }
        return null;
    }

    private static McaHunterAggressionPolicy.CaptureContext makeAggressiveContext(ITotem totem) {
        if (totem != null && isHunterFaction(totem.getControllingFaction())) {
            return McaHunterAggressionPolicy.CaptureContext.HUNTER_VILLAGE;
        }
        return McaHunterAggressionPolicy.CaptureContext.NON_HUNTER;
    }

    private static boolean isVampireFaction(IFaction<?> faction) {
        return VAMPIRE_FACTION_ID.equals(factionId(faction));
    }

    private static boolean isHunterFaction(IFaction<?> faction) {
        return HUNTER_FACTION_ID.equals(factionId(faction));
    }

    private static String factionId(IFaction<?> faction) {
        try {
            return faction == null ? "none" : String.valueOf(faction.getID());
        } catch (RuntimeException | LinkageError ignored) {
            return "unknown";
        }
    }

    private static String captureKey(ITotem totem, IFaction<?> capturingFaction, IFaction<?> controllingFaction) {
        Level level = totem == null ? null : totem.getTileWorld();
        String dimension = level == null ? "unknown" : level.dimension().location().toString();
        BlockPos pos = totem instanceof BlockEntity blockEntity ? blockEntity.getBlockPos() : BlockPos.ZERO;
        return dimension + "|" + pos.asLong();
    }

    private static void logResult(VillagerEntityMCA villager, String result, CaptureType beforeType) {
        McaVampirismCompat.debugLog(
                "[VILLAGE-CAPTURE] result applied villager={} result={} beforeType={} afterType={}",
                villager.getUUID(),
                result,
                beforeType,
                classifyMcaVillagerForCapture(villager)
        );
    }

    public enum CaptureType {
        MCA_VAMPIRE,
        MCA_HUNTER,
        MCA_WEREWOLF,
        MCA_INFECTED,
        MCA_HUMAN,
        MCA_CURING,
        MCA_CHILD
    }

    private static final class StrengthBreakdown {
        private int human;
        private int hunter;
        private int vampire;
        private int werewolf;
        private int infected;
        private int curing;
        private int child;
        private float attack;
        private float defense;
    }
}
