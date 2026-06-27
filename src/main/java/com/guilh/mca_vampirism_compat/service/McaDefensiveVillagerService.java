package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.faction.PlayerFaction;
import com.guilh.mca_vampirism_compat.service.faction.hunter.McaHunterAggressionPolicy;
import com.guilh.mca_vampirism_compat.trait.McaTraitBridge;
import com.guilh.mca_vampirism_compat.util.McaHunterTargetUtil;
import com.guilh.mca_vampirism_compat.util.VampirismUtil;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.relationship.AgeState;
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
import net.minecraftforge.registries.ForgeRegistries;

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
        return villager.getCapability(ModCapabilities.VAMPIRIC_VILLAGER)
                .map(state -> !state.isInfected())
                .orElse(true);
    }

    public static void enableDefense(VillagerEntityMCA villager, String reason, UUID targetUuid) {
        if (!canEnterDefensiveMode(villager)) {
            return;
        }
        villager.getCapability(ModCapabilities.VAMPIRIC_VILLAGER).ifPresent(state -> {
            long gameTime = villager.level().getGameTime();
            state.enterDefensiveMode(gameTime, McaVampirismCompatConfig.defensiveModeDurationTicks(), reason, targetUuid);
            syncHunterTrait(villager);
            registerDefenseGoalsIfNeeded(villager);
            equipDefenseWeaponIfNeeded(villager);
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
        villager.getCapability(ModCapabilities.VAMPIRIC_VILLAGER).ifPresent(state -> {
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
            equipDefenseWeaponIfNeeded(villager);
        });
    }

    public static void clearDefense(VillagerEntityMCA villager, String reason) {
        if (villager.level().isClientSide) {
            return;
        }
        villager.getCapability(ModCapabilities.VAMPIRIC_VILLAGER).ifPresent(state -> {
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
        villager.getCapability(ModCapabilities.VAMPIRIC_VILLAGER).ifPresent(state -> {
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
        return villager.getCapability(ModCapabilities.VAMPIRIC_VILLAGER)
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
        return villager.getCapability(ModCapabilities.VAMPIRIC_VILLAGER)
                .map(state -> gameTime - state.getLastVampireThreatTick() <= McaVampirismCompatConfig.vampireThreatWindowTicks())
                .orElse(false);
    }

    private static void registerDefenseGoalsIfNeeded(VillagerEntityMCA villager) {
        villager.getCapability(ModCapabilities.VAMPIRIC_VILLAGER).ifPresent(state -> {
            if (state.areDefensiveGoalsAdded()) {
                return;
            }
            McaSpecialVillagerCombatService.registerGoalsIfNeeded(villager);
        });
    }

    public static void equipDefenseWeaponIfNeeded(VillagerEntityMCA villager) {
        equipHunterWeaponIfAllowed(villager, McaHunterAggressionPolicy.HunterSource.DEFENSIVE_MODE);
    }

    public static void equipHunterWeaponIfAllowed(VillagerEntityMCA villager, McaHunterAggressionPolicy.HunterSource source) {
        if (!McaHunterAggressionPolicy.canEquipHunterWeapon(villager, source)) {
            return;
        }
        villager.getCapability(ModCapabilities.VAMPIRIC_VILLAGER).ifPresent(state -> {
            if (!state.isOriginalMainHandItemSaved()) {
                state.saveOriginalMainHandItem(villager.getItemInHand(InteractionHand.MAIN_HAND));
            }
            ItemStack weapon = resolveDefenseWeapon();
            if (weapon.isEmpty()) {
                return;
            }
            ItemStack current = villager.getItemInHand(InteractionHand.MAIN_HAND);
            if (!ItemStack.isSameItemSameTags(current, weapon)) {
                villager.setItemInHand(InteractionHand.MAIN_HAND, weapon);
                if (!state.isDefensiveWeaponEquippedLogged()) {
                    state.markDefensiveWeaponEquippedLogged();
                    McaVampirismCompat.debugLog(
                            "[DEFENSE] weapon equipped villager={} item={}",
                            villager.getUUID(),
                            ForgeRegistries.ITEMS.getKey(weapon.getItem())
                    );
                }
            }
        });
    }

    public static void restoreDefenseWeapon(VillagerEntityMCA villager) {
        villager.getCapability(ModCapabilities.VAMPIRIC_VILLAGER).ifPresent(state -> {
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
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        return item == null ? Items.AIR : item;
    }
}
