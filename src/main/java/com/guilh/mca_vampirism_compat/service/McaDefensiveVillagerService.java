package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.faction.PlayerFaction;
import com.guilh.mca_vampirism_compat.service.faction.hunter.McaHunterAggressionPolicy;
import com.guilh.mca_vampirism_compat.trait.McaTraitBridge;
import com.guilh.mca_vampirism_compat.util.McaHunterTargetUtil;
import com.guilh.mca_vampirism_compat.util.VampirismUtil;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.ai.relationship.AgeState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.Locale;
import java.util.UUID;

public final class McaDefensiveVillagerService {
    private static final double DEFENSE_ATTACK_SPEED = 1.05D;

    private McaDefensiveVillagerService() {
    }

    public static void enableDefenseFromAggressiveReplacementAttempt(Villager villager, String reason) {
        if (!(villager instanceof VillagerEntityMCA mcaVillager)) {
            return;
        }
        if (McaVampireStateService.isVampire(mcaVillager)) {
            McaVampirismCompat.debugLog(
                    "[DEFENSE] aggressive replacement blocked for MCA vampire without human defensive mode villager={} reason={}",
                    mcaVillager.getUUID(),
                    reason
            );
            McaVillageVampireThreatService.recordVampireThreat(mcaVillager, mcaVillager, reason);
            return;
        }
        if (canEnterDefensiveMode(mcaVillager)) {
            McaVampirismCompat.debugLog(
                    "[AGGRESSIVE-VILLAGERS] replacement blocked, applying safe defensive mode to original MCA villager villager={} reason={}",
                    mcaVillager.getUUID(),
                    reason
            );
            enableDefense(mcaVillager, reason, null);
            McaVillageVampireThreatService.recordVampireThreat(null, mcaVillager, reason);
        } else {
            McaVampirismCompat.debugLog(
                    "[AGGRESSIVE-VILLAGERS] replacement blocked without defensive state villager={} reason={} deny={}",
                    mcaVillager.getUUID(),
                    reason,
                    McaHunterAggressionPolicy.denyReason(mcaVillager, McaHunterAggressionPolicy.HunterSource.DEFENSIVE_MODE)
            );
        }
    }

    public static boolean canEnterDefensiveMode(VillagerEntityMCA villager) {
        if (!McaHunterAggressionPolicy.canEnterDefensiveAggressiveMode(
                villager,
                McaHunterAggressionPolicy.DefensiveReason.TEMPORARY_DEFENSE)) {
            return false;
        }
        return ModCapabilities.get(villager)
                .map(state -> !state.isInfected())
                .orElse(true);
    }

    public static void enableDefense(VillagerEntityMCA villager, String reason, UUID targetUuid) {
        if (!canEnterDefensiveMode(villager)) {
            return;
        }
        ModCapabilities.get(villager).ifPresent(state -> {
            long gameTime = villager.level().getGameTime();
            state.enterDefensiveMode(gameTime, McaVampirismCompatConfig.defensiveModeDurationTicks(), reason, targetUuid);
            syncHunterTrait(villager);
            registerDefenseGoalsIfNeeded(villager);
            reconcileHunterWeapon(villager);
            McaVampirismCompat.debugLog(
                    "[DEFENSE] defensive mode enabled villager={} until={} reason={} target={}",
                    villager.getUUID(),
                    state.getDefensiveModeUntilTick(),
                    state.getDefensiveReason(),
                    targetUuid
            );
            McaSpecialStateNormalizer.normalize(villager);
        });
    }

    public static void processDefenseTick(VillagerEntityMCA villager) {
        if (villager.level().isClientSide) {
            return;
        }
        ModCapabilities.get(villager).ifPresent(state -> {
            if (!state.isDefensiveMode()) {
                return;
            }
            long gameTime = villager.level().getGameTime();
            if (!canEnterDefensiveMode(villager) || gameTime > state.getDefensiveModeUntilTick()) {
                clearDefense(villager, gameTime > state.getDefensiveModeUntilTick() ? "expired" : "invalid");
                return;
            }
            syncHunterTrait(villager);
            registerDefenseGoalsIfNeeded(villager);
            reconcileHunterWeapon(villager);
        });
    }

    public static void clearDefense(VillagerEntityMCA villager, String reason) {
        if (villager.level().isClientSide) {
            return;
        }
        ModCapabilities.get(villager).ifPresent(state -> {
            if (!state.isDefensiveMode() && !state.isOriginalMainHandItemSaved()) {
                return;
            }
            restoreDefenseWeapon(villager);
            state.clearDefensiveMode();
            syncHunterTrait(villager);
            villager.setTarget(null);
            McaVampirismCompat.debugLog(
                    "[DEFENSE] defensive mode {} villager={}",
                    "expired".equals(reason) ? "expired" : "cleared",
                    villager.getUUID()
            );
        });
    }

    public static void syncHunterTrait(VillagerEntityMCA villager) {
        if (villager.level().isClientSide) {
            return;
        }
        ModCapabilities.get(villager).ifPresent(state -> {
            boolean canHaveHunterTrait = McaVampirismCompatConfig.defensiveModeGrantsHunterTrait()
                    && state.isDefensiveMode()
                    && canEnterDefensiveMode(villager)
                    && McaHunterAggressionPolicy.canBecomeHunterAligned(
                    villager,
                    McaHunterAggressionPolicy.HunterSource.DEFENSIVE_MODE);
            if (canHaveHunterTrait) {
                if (!McaTraitBridge.hasHunterTrait(villager)) {
                    McaTraitBridge.addHunterTrait(villager);
                    McaVampirismCompat.debugLog("[DEFENSE] hunter trait added villager={}", villager.getUUID());
                }
                state.setHunterTraitApplied(true);
                state.setHunterAligned(
                        McaVampirismCompatConfig.defensiveVillagersAreHunterAligned(),
                        state.getDefensiveModeUntilTick()
                );
                return;
            }

            if (state.isHunterTraitApplied() && McaVampirismCompatConfig.defensiveVillagersHunterTraitIsTemporary()) {
                if (McaTraitBridge.hasHunterTrait(villager)) {
                    McaTraitBridge.removeHunterTrait(villager);
                    McaVampirismCompat.debugLog("[DEFENSE] hunter trait removed villager={}", villager.getUUID());
                }
                state.setHunterTraitApplied(false);
            }
            if (!state.isHunterVillageApplied() && !McaTraitBridge.hasHunterTrait(villager)) {
                state.setHunterAligned(false, Long.MIN_VALUE);
            }
        });
    }

    public static boolean isDefensiveVillager(Mob mob) {
        if (!(mob instanceof VillagerEntityMCA villager)
                || McaVampireStateService.isVampire(villager)
                || McaWerewolfStateService.isWerewolf(villager)) {
            return false;
        }
        return ModCapabilities.get(villager)
                .map(state -> state.isDefensiveMode()
                        && McaHunterAggressionPolicy.canEnterDefensiveAggressiveMode(
                        villager,
                        McaHunterAggressionPolicy.DefensiveReason.TEMPORARY_DEFENSE))
                .orElse(false);
    }

    public static boolean isValidDefensiveTarget(Mob defender, LivingEntity target) {
        if (defender instanceof VillagerEntityMCA hunter) {
            return McaHunterTargetUtil.isValidHunterTarget(hunter, target);
        }
        if (!isDefensiveVillager(defender) || target == null || target == defender || !target.isAlive()) {
            return false;
        }
        if (McaVampirismCompatConfig.defensiveVillagersAttackOnlyIfThreatNearby()
                && !hasRecentThreat((VillagerEntityMCA) defender)) {
            return false;
        }
        if (target instanceof Player player) {
            return McaVampirismCompatConfig.defensiveVillagersCanAttackVampirePlayers()
                    && McaVampireFactionService.classifyPlayer(player) == PlayerFaction.VAMPIRE;
        }
        if (target instanceof VillagerEntityMCA mcaTarget && McaVampireStateService.isVampire(mcaTarget)) {
            if (McaVampirismCompatConfig.defensiveVillagersWarnInsteadOfAttackMcaVampires()) {
                return false;
            }
            return McaVampirismCompatConfig.defensiveVillagersCanAttackMcaVampireVillagers();
        }
        return VampirismUtil.isVampiric(target);
    }

    private static boolean hasRecentThreat(VillagerEntityMCA villager) {
        long gameTime = villager.level().getGameTime();
        return ModCapabilities.get(villager)
                .map(state -> gameTime - state.getLastVampireThreatTick() <= McaVampirismCompatConfig.vampireThreatWindowTicks())
                .orElse(false);
    }

    private static void registerDefenseGoalsIfNeeded(VillagerEntityMCA villager) {
        ModCapabilities.get(villager).ifPresent(state -> {
            if (state.areDefensiveGoalsAdded()) {
                return;
            }
            McaSpecialVillagerCombatService.registerGoalsIfNeeded(villager);
        });
    }

    public static void equipDefenseWeaponIfNeeded(VillagerEntityMCA villager) {
        reconcileHunterWeapon(villager);
    }

    /**
     * Maintains the temporary weapon from one authoritative state snapshot.
     * The former independent equip and restore paths could race during MCA
     * state normalization, making a pitchfork appear for one sync and vanish
     * on the next. Reconciliation always makes one final decision instead.
     */
    public static void reconcileHunterWeapon(VillagerEntityMCA villager) {
        if (villager == null || villager.level().isClientSide) {
            return;
        }
        // MCA temporarily puts food in the main hand while a villager recovers.
        // Do not replace that item with the pitchfork before MCA finishes its use.
        if (villager.isUsingRecoveryFood()) {
            return;
        }
        ModCapabilities.get(villager).ifPresent(state -> {
            McaHunterAggressionPolicy.HunterSource source = resolveWeaponSource(villager, state);
            if (source == null) {
                restoreDefenseWeapon(villager);
                return;
            }
            equipHunterWeaponIfAllowed(villager, source);
        });
    }

    /**
     * Reasserts a previously established Hunter weapon after MCA's equipment
     * behavior has updated the dominant hand. This path intentionally never
     * restores a hand or captures a new original item: the regular lifecycle
     * reconciliation remains responsible for those state transitions.
     */
    public static void reconcileHunterWeaponAfterMcaEquipmentTask(VillagerEntityMCA villager) {
        if (villager == null || villager.level().isClientSide || villager.isUsingRecoveryFood()) {
            return;
        }
        ModCapabilities.get(villager).ifPresent(state -> {
            if (!state.isOriginalMainHandItemSaved()) {
                return;
            }
            McaHunterAggressionPolicy.HunterSource source = resolveWeaponSource(villager, state);
            if (source != null) {
                equipHunterWeaponIfAllowed(villager, source);
            }
        });
    }

    private static McaHunterAggressionPolicy.HunterSource resolveWeaponSource(
            VillagerEntityMCA villager,
            com.guilh.mca_vampirism_compat.VampiricVillagerState state
    ) {
        if (McaVampireStateService.isVampire(villager) || McaWerewolfStateService.isWerewolf(villager)) {
            return null;
        }
        if (state.isDefensiveMode()
                && McaHunterAggressionPolicy.canEnterDefensiveAggressiveMode(
                villager,
                McaHunterAggressionPolicy.DefensiveReason.TEMPORARY_DEFENSE)) {
            return McaHunterAggressionPolicy.HunterSource.DEFENSIVE_MODE;
        }
        if (state.isHunterVillageApplied()) {
            return McaHunterAggressionPolicy.HunterSource.HUNTER_CAPTURE_FINISHED;
        }
        if (McaVillageFactionControlService.shouldVillagerBeHunterByVillageControl(villager)) {
            return McaHunterAggressionPolicy.HunterSource.HUNTER_VILLAGE;
        }
        if (McaTraitBridge.hasHunterTrait(villager)
                && McaHunterAggressionPolicy.isManualHunterAllowed(villager)) {
            return McaHunterAggressionPolicy.HunterSource.MANUAL_TRAIT;
        }
        return null;
    }

    public static void equipHunterWeaponIfAllowed(VillagerEntityMCA villager, McaHunterAggressionPolicy.HunterSource source) {
        if (!McaHunterAggressionPolicy.canEquipHunterWeapon(villager, source)) {
            return;
        }
        ModCapabilities.get(villager).ifPresent(state -> {
            if (!state.isOriginalMainHandItemSaved()) {
                state.saveOriginalMainHandItem(villager.getItemInHand(InteractionHand.MAIN_HAND));
            }
            ItemStack weapon = resolveDefenseWeapon();
            if (weapon.isEmpty()) {
                return;
            }
            ItemStack current = villager.getItemInHand(InteractionHand.MAIN_HAND);
            if (!ItemStack.isSameItemSameComponents(current, weapon)) {
                villager.setItemInHand(InteractionHand.MAIN_HAND, weapon);
                if (!state.isDefensiveWeaponEquippedLogged()) {
                    state.markDefensiveWeaponEquippedLogged();
                    McaVampirismCompat.debugLog(
                            "[DEFENSE] weapon equipped villager={} item={}",
                            villager.getUUID(),
                            BuiltInRegistries.ITEM.getKey(weapon.getItem())
                    );
                }
            }
        });
    }

    public static void restoreDefenseWeapon(VillagerEntityMCA villager) {
        ModCapabilities.get(villager).ifPresent(state -> {
            if (!state.isOriginalMainHandItemSaved()) {
                return;
            }
            villager.setItemInHand(InteractionHand.MAIN_HAND, state.getOriginalMainHandItem());
            state.clearOriginalMainHandItem();
            state.clearDefensiveWeaponEquippedLogged();
            McaVampirismCompat.debugLog("[DEFENSE] weapon restored villager={}", villager.getUUID());
        });
    }

    private static ItemStack resolveDefenseWeapon() {
        String configured = McaVampirismCompatConfig.defensiveVillagerWeaponType();
        Item configuredItem = lookupConfiguredItem(configured);
        if (configuredItem != Items.AIR) {
            return new ItemStack(configuredItem);
        }
        Item fallback = firstExistingItem("vampirism:pitchfork", "vampirism_integrations:pitchfork", "minecraft:iron_hoe", "minecraft:iron_axe");
        return fallback == Items.AIR ? ItemStack.EMPTY : new ItemStack(fallback);
    }

    private static Item lookupConfiguredItem(String configured) {
        if (configured == null || configured.isBlank()) {
            return Items.AIR;
        }
        String normalized = configured.toLowerCase(Locale.ROOT).trim();
        if (!normalized.contains(":")) {
            if ("pitchfork".equals(normalized)) {
                return firstExistingItem("vampirism:pitchfork", "vampirism_integrations:pitchfork");
            }
            normalized = "minecraft:" + normalized;
        }
        return lookupItem(normalized);
    }

    private static Item firstExistingItem(String... ids) {
        for (String id : ids) {
            Item item = lookupItem(id);
            if (item != Items.AIR) {
                return item;
            }
        }
        return Items.AIR;
    }

    private static Item lookupItem(String id) {
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
        return item == null ? Items.AIR : item;
    }
}
