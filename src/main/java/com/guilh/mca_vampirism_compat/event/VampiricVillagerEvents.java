package com.guilh.mca_vampirism_compat.event;

import com.guilh.mca_vampirism_compat.mission.OccultVillageMissionService;
import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.advancement.McaAdvancementService;
import com.guilh.mca_vampirism_compat.advancement.McaInheritanceAdvancementService;
import com.guilh.mca_vampirism_compat.command.McaVampCompatCommands;
import com.guilh.mca_vampirism_compat.ai.McaHunterAi;
import com.guilh.mca_vampirism_compat.ai.McaVampireAi;
import com.guilh.mca_vampirism_compat.ai.McaWerewolfAi;
import com.guilh.mca_vampirism_compat.attribute.McaVampireAttributeHandler;
import com.guilh.mca_vampirism_compat.attribute.McaSpecialVillagerAttributeService;
import com.guilh.mca_vampirism_compat.faction.PlayerFaction;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.easyvillagers.EasyVillagersBlockTradeService;
import com.guilh.mca_vampirism_compat.easyvillagers.McaEvCompatBridge;
import com.guilh.mca_vampirism_compat.growth.McaVampireGrowthHandler;
import com.guilh.mca_vampirism_compat.inheritance.McaVampireInheritanceHandler;
import com.guilh.mca_vampirism_compat.service.McaTradeContextService;
import com.guilh.mca_vampirism_compat.service.McaAggressiveVillagerProtectionService;
import com.guilh.mca_vampirism_compat.service.McaDefensiveVillagerService;
import com.guilh.mca_vampirism_compat.service.McaHunterAlignmentService;
import com.guilh.mca_vampirism_compat.service.McaHunterBloodService;
import com.guilh.mca_vampirism_compat.service.McaCommandRespectService;
import com.guilh.mca_vampirism_compat.service.McaSpecialVillagerClassifier;
import com.guilh.mca_vampirism_compat.service.McaSpecialVillagerCombatService;
import com.guilh.mca_vampirism_compat.service.McaSpecialVillagerRuntimeService;
import com.guilh.mca_vampirism_compat.service.McaVampireBiteService;
import com.guilh.mca_vampirism_compat.service.McaVampireCureService;
import com.guilh.mca_vampirism_compat.service.McaVampireFeedProtection;
import com.guilh.mca_vampirism_compat.service.McaVampireFactionService;
import com.guilh.mca_vampirism_compat.monster.McaSpecialVillagerMonsterIgnoreService;
import com.guilh.mca_vampirism_compat.service.McaVampireStateService;
import com.guilh.mca_vampirism_compat.service.McaVampireSunlightHandler;
import com.guilh.mca_vampirism_compat.trade.McaFactionTradeService;
import com.guilh.mca_vampirism_compat.service.McaVillageCaptureIntegrationService;
import com.guilh.mca_vampirism_compat.service.McaVillageEffectBoundsService;
import com.guilh.mca_vampirism_compat.service.McaVillageFactionControlService;
import com.guilh.mca_vampirism_compat.service.McaVillageOutlineService;
import com.guilh.mca_vampirism_compat.service.McaWerewolfConversionService;
import com.guilh.mca_vampirism_compat.service.McaWerewolfVillagerService;
import com.guilh.mca_vampirism_compat.service.McaWerewolfStateService;
import com.guilh.mca_vampirism_compat.trade.McaFactionTradeSession;
import com.guilh.mca_vampirism_compat.trade.McaNativeTradeCycleSession;
import com.guilh.mca_vampirism_compat.trade.TradeCycleEligibility;
import com.guilh.mca_vampirism_compat.util.McaCompatMessages;
import com.guilh.mca_vampirism_compat.util.McaHunterTargetUtil;
import com.guilh.mca_vampirism_compat.util.McaVampireTargetUtil;
import com.guilh.mca_vampirism_compat.util.McaVillagerUtil;
import de.teamlapen.vampirism.api.event.BloodDrinkEvent;
import de.teamlapen.vampirism.api.event.VampirismVillageEvent;
import de.teamlapen.vampirism.core.ModEffects;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.player.TradeWithVillagerEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

public final class VampiricVillagerEvents {
    @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST)
    public static void onMissionResidentDeath(net.neoforged.neoforge.event.entity.living.LivingDeathEvent event) {
        if (event.getEntity() instanceof VillagerEntityMCA villager) OccultVillageMissionService.confirmedDeath(villager);
    }

    @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST)
    public static void onConfirmedMissionDamage(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post event) {
        if (event.getNewDamage() > 0 && event.getEntity() instanceof VillagerEntityMCA resident
                && event.getSource().getEntity() instanceof net.minecraft.world.entity.LivingEntity attacker) {
            OccultVillageMissionService.confirmedDamage(resident, attacker);
            if (McaVampirismCompatConfig.respectMcaRelationshipsInCombat()) {
                com.guilh.mca_vampirism_compat.service.McaRelationshipCombatPolicy.confirmedAggression(resident, attacker);
                McaSpecialVillagerCombatService.markRetaliation(resident, attacker, "CONFIRMED_DAMAGE");
            }
        }
    }

    @SubscribeEvent
    public static void onRelationshipTargetChange(net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent event) {
        if (com.guilh.mca_vampirism_compat.service.McaRelationshipCombatPolicy.blocksAutomaticAttack(event.getEntity(), event.getNewAboutToBeSetTarget())) event.setNewAboutToBeSetTarget(null);
    }

    private static boolean configProbeLogged;

    private VampiricVillagerEvents() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        McaVampCompatCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        OccultVillageMissionService.serverTick(event.getServer());
        McaVillageOutlineService.tick(event.getServer());
        McaInheritanceAdvancementService.observeBabyItems(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        McaVillageOutlineService.clearAll();
        McaInheritanceAdvancementService.resetTransientState();
        if (!configProbeLogged) {
            configProbeLogged = true;
            McaVampirismCompat.debugLog(
                    "[CONFIG] active gameplay values: conversionChance={} playerSanguinareDuration={} villagerSanguinareDuration={} nightAttackChance={} vampireDiscount={} humanTradeMultiplier={} hunterBlocked={} werewolfBlocked={} nightSchedule={} avoidSun={} burnInSun={} biteCooldown={} recentTargetCooldown={} monstersIgnoreMcaVampires={} cancelMonsterDamage={} suppressZombiePanic={} suppressFearDialogue={} protectAggressiveMcaVampires={} protectAllMcaFromAggressive={} defenseEnabled={} temporaryDefenseEnabled={} defenseOnlyHunterVillages={} defenseDuration={} threatRadius={} threatThreshold={} mcaVillageBounds={} mcaVillageFogMargin={}",
                    McaVampirismCompatConfig.conversionChanceOnBite(),
                    McaVampirismCompatConfig.playerSanguinareDurationTicks(),
                    McaVampirismCompatConfig.vampireVillagerSanguinareDurationTicks(),
                    McaVampirismCompatConfig.nightAttackChance(),
                    McaVampirismCompatConfig.vampirePlayerTradeDiscount(),
                    McaVampirismCompatConfig.humanTradePriceMultiplier(),
                    McaVampirismCompatConfig.blockHunterTrading(),
                    McaVampirismCompatConfig.blockWerewolfTrading(),
                    McaVampirismCompatConfig.vampireVillagersUseNightSchedule(),
                    McaVampirismCompatConfig.vampireVillagersAvoidSun(),
                    McaVampirismCompatConfig.vampireVillagersBurnInSun(),
                    McaVampirismCompatConfig.biteCooldownTicks(),
                    McaVampirismCompatConfig.recentlyBittenTargetCooldownTicks(),
                    McaVampirismCompatConfig.monstersIgnoreMcaVampireVillagers(),
                    McaVampirismCompatConfig.cancelMonsterDamageToMcaVampireVillagers(),
                    McaVampirismCompatConfig.suppressMcaVampireVillagerZombiePanic(),
                    McaVampirismCompatConfig.suppressMcaVampireVillagerFearDialogue(),
                    McaVampirismCompatConfig.preventAggressiveVillagerReplacementForMcaVampires(),
                    McaVampirismCompatConfig.preventAggressiveVillagerReplacementForAllMcaVillagers(),
                    McaVampirismCompatConfig.enableMcaVillagerDefenseAgainstVampires(),
                    McaVampirismCompatConfig.enableTemporaryDefensiveMode(),
                    McaVampirismCompatConfig.defensiveModeOnlyInHunterVillages(),
                    McaVampirismCompatConfig.defensiveModeDurationTicks(),
                    McaVampirismCompatConfig.vampireThreatAlertRadius(),
                    McaVampirismCompatConfig.vampireThreatCountToTriggerDefense(),
                    McaVampirismCompatConfig.mcaVillageEffectsFollowMcaBounds(),
                    McaVampirismCompatConfig.mcaVillageFogBorderMarginBlocks()
            );
            McaAggressiveVillagerProtectionService.logDetectedMods();
            McaEvCompatBridge.logDelegationStatusOnce();
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        McaVillageOutlineService.clearAll();
        McaVillageEffectBoundsService.clearTransientState();
        McaInheritanceAdvancementService.resetTransientState();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onVampirismMakeAggressive(VampirismVillageEvent.MakeAggressive event) {
        if (McaVillageCaptureIntegrationService.onMakeAggressive(event)) {
            return;
        }
        if (McaAggressiveVillagerProtectionService.shouldCancelMakeAggressive(event.getOldVillager())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onVampirismInitiateCapture(VampirismVillageEvent.InitiateCapture event) {
        McaVillageCaptureIntegrationService.onInitiateCapture(event);
    }

    @SubscribeEvent
    public static void onVampirismDefineRaidStrength(VampirismVillageEvent.DefineRaidStrength event) {
        McaVillageCaptureIntegrationService.onDefineRaidStrength(event);
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide && event.getEntity() instanceof LivingEntity entity) {
            if (entity instanceof VillagerEntityMCA villager) {
                McaSpecialVillagerRuntimeService.onJoin(villager);
                McaInheritanceAdvancementService.onVillagerJoined(villager);
            } else {
                McaVampireAi.registerGoalsIfNeeded(entity);
                McaVampireAttributeHandler.applyVampireAttributes(entity);
            }
        }
    }

    @SubscribeEvent
    public static void onBabySpawn(BabyEntitySpawnEvent event) {
        if (event.getChild() != null && !event.getChild().level().isClientSide) {
            McaVampireInheritanceHandler.applyInheritance(event.getChild(), event.getParentA(), event.getParentB());
            if (event.getChild() instanceof VillagerEntityMCA child) {
                McaInheritanceAdvancementService.recordNaturalBirth(child);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onVillagerCaptureFinishPre(VampirismVillageEvent.VillagerCaptureFinish.Pre event) {
        McaVillageCaptureIntegrationService.onCaptureFinishPre(event);
    }

    @SubscribeEvent
    public static void onVillagerCaptureFinish(VampirismVillageEvent.VillagerCaptureFinish.Post event) {
        if (!event.isForced() && event.getCapturingFaction() != null && event.getControllingFaction() != null
                && !event.getCapturingFaction().equals(event.getControllingFaction())
                && event.getTotem() instanceof de.teamlapen.vampirism.blockentity.TotemBlockEntity totem
                && totem.getLevel() instanceof net.minecraft.server.level.ServerLevel level) {
            OccultVillageMissionService.recordAttack(level, totem.getBlockPos());
        }
        McaVillageCaptureIntegrationService.onCaptureFinishPost(event);
    }

    @SubscribeEvent
    public static void onMcaVampireInteract(PlayerInteractEvent.EntityInteract event) {
        if (McaVampireCureService.handleEntityInteract(event)) {
            return;
        }
        if (!event.getLevel().isClientSide && event.getHand() == InteractionHand.MAIN_HAND
                && event.getTarget() instanceof VillagerEntityMCA resident
                && com.guilh.mca_vampirism_compat.service.McaRelationshipCombatPolicy.blocksAutomaticAttack(resident,event.getEntity())) {
            event.getEntity().displayClientMessage(Component.translatable("mca_vamp_compat.relationship.protected"), false);
        }
        McaFactionTradeService.onInteractionEvent(event);
        Entity target = event.getTarget();
        if (event.getLevel().isClientSide || !McaVillagerUtil.isMcaVampire(target)) {
            return;
        }
        if (target instanceof VillagerEntityMCA villager
                && event.getHand() == InteractionHand.MAIN_HAND
                && villager.hasTradeOffers()) {
            return;
        }

        PlayerFaction faction = McaVampireFactionService.classifyEffectivePlayer(event.getEntity());
        event.getEntity().displayClientMessage(Component.translatable(McaCompatMessages.forFaction(faction)), true);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        EasyVillagersBlockTradeService.onRightClickBlock(event);
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        McaFactionTradeService.closeWrappedTradeSession(event.getEntity());
        EasyVillagersBlockTradeService.closeSession(event.getEntity());
        McaTradeContextService.clearContext(event.getEntity());
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            McaVillageOutlineService.stop(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            McaAdvancementService.grantPending(player);
        }
    }

    @SubscribeEvent
    public static void onNativeMcaTrade(TradeWithVillagerEvent event) {
        if (event.getAbstractVillager() instanceof VillagerEntityMCA villager) {
            // The lock reflects a real completed offer, not MCA or
            // mca-ev-compat's professional XP. It must therefore cover human
            // MCA villagers as well as faction-aware sessions.
            TradeCycleEligibility.lockAfterTrade(villager);
            McaFactionTradeSession.onNativeTradeCompleted(
                    event.getEntity(),
                    villager,
                    event.getMerchantOffer()
            );
            McaNativeTradeCycleSession.onNativeTradeCompleted(
                    event.getEntity(),
                    villager
            );
        }
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof VillagerEntityMCA villager) {
            McaDefensiveVillagerService.clearDefense(villager, "villager left level");
            McaFactionTradeSession.closeForVillager(villager.getUUID(), "villager left level");
            McaNativeTradeCycleSession.closeForVillager(villager.getUUID(), "villager left level");
        }
    }

    @SubscribeEvent
    public static void onPlayerBloodDrink(BloodDrinkEvent.PlayerDrinkBloodEvent event) {
        onBloodDrink(event);
    }

    @SubscribeEvent
    public static void onEntityBloodDrink(BloodDrinkEvent.EntityDrinkBloodEvent event) {
        onBloodDrink(event);
    }

    private static void onBloodDrink(BloodDrinkEvent event) {
        event.getBloodSource().getEntity().ifPresent(target -> {
            LivingEntity source = event.getVampire().getRepresentingEntity();
            if (McaVampireFeedProtection.isMcaVampireTarget(target)) {
                if (!McaVampireFeedProtection.shouldBlockDrain(source, target)) {
                    return;
                }
                blockBloodDrain(event);
                if (source instanceof Player player && McaVampirismCompatConfig.showAlreadyVampireBiteMessage()) {
                    player.displayClientMessage(Component.translatable("mca_vamp_compat.bite.already_vampire"), true);
                }
                return;
            }

            if (!McaVillagerUtil.isMcaVillager(target)) {
                return;
            }

            if (McaHunterBloodService.isPoisonousBloodTarget(target)) {
                blockBloodDrain(event);
                McaHunterBloodService.blockHunterVampireInfection(source, target);
                return;
            }
            McaVampireBiteService.applySanguinareInfection(target, source);
        });
    }

    @SubscribeEvent
    public static void onVampiricAttack(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        Entity attacker = event.getSource().getEntity();
        if (McaVampireCureService.shouldBlockCuringAttack(attacker)) {
            event.setCanceled(true);
            McaVampirismCompat.debugLog(
                    "[CURE] attack blocked source={} reason=CURING",
                    attacker == null ? null : attacker.getUUID()
            );
            return;
        }
        if (!target.level().isClientSide && McaSpecialVillagerMonsterIgnoreService.shouldCancelMonsterAttack(attacker, target)) {
            if (attacker instanceof Mob mob) {
                mob.setTarget(null);
                mob.getNavigation().stop();
            }
            if (McaSpecialVillagerMonsterIgnoreService.shouldLogMonsterIgnore(attacker, target, "attack_canceled")) {
                McaVampirismCompat.debugLog(
                        "[MONSTER-IGNORE] attack canceled mob={} villagerType={}",
                        attacker == null ? null : attacker.getType(),
                        McaSpecialVillagerMonsterIgnoreService.specialVillagerTypeName(target)
                );
            }
            event.setCanceled(true);
            return;
        }
        // Player melee damage is deliberately left untouched here.  The actual
        // Vampirism bite flow emits BloodDrinkEvent and is handled above; using
        // proximity as a bite signal made every sword or Better Combat hit look
        // like blood feeding.
    }

    @SubscribeEvent
    public static void onLivingTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }
        if (entity.level().isClientSide) {
            return;
        }

        if (entity instanceof Mob mob) {
            McaSpecialVillagerMonsterIgnoreService.clearMonsterTargetIfNeeded(mob);
        }

        if (!McaVillagerUtil.isMcaVillager(entity)) {
            return;
        }

        if (entity instanceof VillagerEntityMCA villager) {
            if (com.guilh.mca_vampirism_compat.service.McaRelationshipCombatPolicy.blocksAutomaticAttack(villager, villager.getTarget())) {
                villager.setTarget(null);
                villager.getNavigation().stop();
                villager.getBrain().eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET);
            }
            McaSpecialVillagerRuntimeService.onTick(villager);
            return;
        }
        McaVampireGrowthHandler.processGrowth(entity);
        McaVampireSunlightHandler.processSunlightDamage(entity);
    }

    @SubscribeEvent
    public static void onMcaVampireHurtTarget(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        Entity source = event.getSource().getEntity();
        if (!target.level().isClientSide && McaSpecialVillagerMonsterIgnoreService.shouldCancelMonsterAttack(source, target)) {
            if (source instanceof Mob mob) {
                mob.setTarget(null);
                mob.getNavigation().stop();
            }
            if (McaSpecialVillagerMonsterIgnoreService.shouldLogMonsterIgnore(source, target, "hurt_canceled")) {
                McaVampirismCompat.debugLog(
                        "[MONSTER-IGNORE] hurt canceled mob={} villagerType={} originalAmount={}",
                        source == null ? null : source.getType(),
                        McaSpecialVillagerMonsterIgnoreService.specialVillagerTypeName(target),
                        event.getAmount()
                );
            }
            event.setCanceled(true);
            return;
        }
        if (target instanceof VillagerEntityMCA hunter
                && source instanceof LivingEntity attacker
                && McaHunterAlignmentService.isMcaHunterVillager(hunter)
                && (McaVampireFactionService.isVampire(attacker)
                || McaVampireFactionService.isTransformedWerewolf(attacker))) {
            if (!McaVampirismCompatConfig.respectMcaRelationshipsInCombat()) McaHunterTargetUtil.markRetaliation(hunter, attacker);
        }
        if (target instanceof VillagerEntityMCA specialVillager && source instanceof LivingEntity attacker) {
            if (!McaVampirismCompatConfig.respectMcaRelationshipsInCombat()) McaSpecialVillagerCombatService.markRetaliation(specialVillager, attacker, "HURT");
        }
        if (target instanceof VillagerEntityMCA villager && source instanceof LivingEntity livingSource) {
            McaWerewolfConversionService.handleWerewolfAttack(livingSource, villager);
        }
        if (McaVampireCureService.shouldBlockCuringAttack(source)) {
            event.setCanceled(true);
            McaVampirismCompat.debugLog(
                    "[CURE] attack blocked source={} reason=CURING",
                    source == null ? null : source.getUUID()
            );
            return;
        }
        if (target.level().isClientSide || !(source instanceof PathfinderMob vampire)) {
            return;
        }
        if (event.getSource().getDirectEntity() != vampire) {
            return;
        }

        boolean hunterBloodBite = McaVampireBiteService.applyHunterBloodBite(vampire, target);
        if (hunterBloodBite) {
            float damage = (float) McaVampirismCompatConfig.villagerBiteDamage();
            if (damage <= 0.0F) {
                event.setCanceled(true);
            } else {
                event.setAmount(Math.min(event.getAmount(), damage));
            }
            if (McaVampirismCompatConfig.bloodDrainStopsCombat()) {
                stopBiteCombat(vampire, target);
            }
            return;
        }

        boolean nonLethalVillagerBite = McaVampireBiteService.isNonLethalInfectionBite(vampire, target);
        if (!nonLethalVillagerBite) {
            return;
        }

        boolean biteAttempted = McaVampireBiteService.applyInfectionBite(vampire, target);
        float damage = (float) McaVampirismCompatConfig.villagerBiteDamage();
        if (damage <= 0.0F) {
            event.setCanceled(true);
        } else {
            event.setAmount(Math.min(event.getAmount(), damage));
        }

        if (biteAttempted && McaVampirismCompatConfig.bloodDrainStopsCombat()) {
            stopBiteCombat(vampire, target);
        }
    }

    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity target = event.getNewAboutToBeSetTarget();
        if (event.getEntity().level().isClientSide || !(event.getEntity() instanceof Mob mob)) {
            return;
        }
        if (!McaSpecialVillagerMonsterIgnoreService.shouldMonsterIgnore(mob, target)
                && !McaSpecialVillagerMonsterIgnoreService.shouldSpecialVillagerIgnoreMonster(mob, target)) {
            return;
        }
        boolean specialVillagerIgnoringMonster = McaSpecialVillagerMonsterIgnoreService.shouldSpecialVillagerIgnoreMonster(mob, target);
        event.setNewAboutToBeSetTarget(null);
        event.setCanceled(true);
        mob.setTarget(null);
        if (!specialVillagerIgnoringMonster
                && (!(mob instanceof VillagerEntityMCA villager) || !McaCommandRespectService.shouldPreserveMcaMovement(villager))) {
            mob.getNavigation().stop();
        }
        if (McaSpecialVillagerMonsterIgnoreService.shouldLogMonsterIgnore(mob, target, "target_canceled")) {
            McaVampirismCompat.debugLog(
                    "[MONSTER-IGNORE] target canceled mob={} target={} villagerType={}",
                    mob.getType(),
                    target == null ? null : target.getUUID(),
                    McaSpecialVillagerMonsterIgnoreService.isSpecialNonPanickingVillager(mob)
                            ? McaSpecialVillagerMonsterIgnoreService.specialVillagerTypeName(mob)
                            : McaSpecialVillagerMonsterIgnoreService.specialVillagerTypeName(target)
            );
        }
    }

    @SubscribeEvent
    public static void onMcaVampireHurtByHuman(LivingIncomingDamageEvent event) {
        if (McaVampirismCompatConfig.respectMcaRelationshipsInCombat()) return;
        LivingEntity vampire = event.getEntity();
        Entity source = event.getSource().getEntity();
        if (vampire.level().isClientSide || !(vampire instanceof VillagerEntityMCA)
                || !McaVillagerUtil.isMcaVampire(vampire) || !(source instanceof LivingEntity attacker)
                || !McaVampireTargetUtil.isHumanLikeTarget(attacker)) {
            return;
        }

        VillagerEntityMCA villager = (VillagerEntityMCA) vampire;
        if (!McaCommandRespectService.canRetaliateWhileCommanded(villager, attacker)) {
            McaCommandRespectService.logRejectedTarget(villager, attacker);
            return;
        }
        ModCapabilities.get(vampire)
                .ifPresent(state -> state.markRetaliation(
                        attacker.getUUID(),
                        vampire.level().getGameTime(),
                        McaCommandRespectService.retaliationMemoryTicksFor(
                                villager,
                                McaVampirismCompatConfig.retaliationMemoryTicks()
                        )
                ));
        if (vampire instanceof Mob mob) {
            mob.setTarget(attacker);
        }
    }

    private static void blockBloodDrain(BloodDrinkEvent event) {
        event.setAmount(0);
        event.setSaturationModifier(0.0F);
        if (event instanceof BloodDrinkEvent.PlayerDrinkBloodEvent playerEvent) {
            playerEvent.setUseRemaining(false);
        }
        if (event instanceof BloodDrinkEvent.EntityDrinkBloodEvent entityEvent) {
            entityEvent.setUseRemaining(false);
        }
    }

    private static void stopBiteCombat(PathfinderMob vampire, LivingEntity target) {
        vampire.setTarget(null);
        vampire.getNavigation().stop();
        if (target instanceof Mob mob && mob.getTarget() == vampire) {
            mob.setTarget(null);
            mob.getNavigation().stop();
        }
    }
}
