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
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.Locale;
import java.util.UUID;
import java.util.function.Predicate;

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
            if (gameTime > state.getDefensiveModeUntilTick()) {
                clearDefense(villager, "expired");
                return;
            }
            if (!McaRuntimeTickScheduler.isScheduled(
                    gameTime,
                    villager.getId(),
                    McaRuntimeTickScheduler.DEFENSIVE_MAINTENANCE_INTERVAL_TICKS
            )) {
                return;
            }
            if (!canEnterDefensiveMode(villager)) {
                clearDefense(villager, "invalid");
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
            // A real Hunter can enter temporary defensive mode as well. Its
            // persistent weapon must not be restored/removed when that short
            // mode expires; only a defense-only villager puts a hand back.
            boolean preserveRealHunterWeapon = hasRealHunterSource(villager, state);
            if (!preserveRealHunterWeapon) {
                restoreDefenseWeapon(villager);
            }
            state.clearDefensiveMode();
            syncHunterTrait(villager);
            if (preserveRealHunterWeapon) {
                reconcileHunterWeapon(villager);
            }
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
     * Runs before MCA's own equipment behavior. MCA normally clears or replaces
     * the dominant hand and the old tail-only reconciliation then sends a
     * second equipment update. Preparing the Hunter first lets the mixin keep
     * the already-correct stack in place, so clients never see an empty hand
     * between the two updates.
     */
    public static void prepareHunterWeaponForMcaEquipmentTask(VillagerEntityMCA villager) {
        reconcileHunterWeapon(villager);
    }

    /**
     * True only for a persistent, real Hunter weapon. Temporary defensive
     * equipment keeps MCA's normal ownership rules. Once a real Hunter owns
     * its weapon hand, no MCA activity is allowed to borrow that hand: food,
     * work tasks and integrations must use their own storage instead.
     */
    public static boolean shouldPreserveHunterWeaponDuringMcaEquipmentTask(VillagerEntityMCA villager) {
        if (villager == null || villager.level().isClientSide) {
            return false;
        }
        return ModCapabilities.get(villager)
                .map(state -> state.isOriginalMainHandItemSaved()
                        && managedHunterWeaponHand(state) == villager.getDominantHand()
                        && isPersistentHunterWeaponSource(resolveWeaponSource(villager, state)))
                .orElse(false);
    }

    /**
     * Last-line protection for the reserved real-Hunter weapon. Vanilla's
     * {@code setItemInHand} eventually delegates to {@code Mob#setItemSlot},
     * but MCA and optional integrations also invoke the slot writer directly.
     * Guarding the slot is therefore the one common boundary that prevents an
     * empty-hand packet followed by a later reconciliation packet.
     */
    public static boolean shouldRejectPersistentHunterWeaponSlotMutation(
            Mob entity,
            EquipmentSlot slot,
            ItemStack replacement
    ) {
        if (!(entity instanceof VillagerEntityMCA villager)
                || slot == null
                || villager.level().isClientSide
                || HunterWeaponSlotMutationScope.isAuthorized()
                || slot != villager.getDominantSlot()
                || !shouldPreserveHunterWeaponDuringMcaEquipmentTask(villager)) {
            return false;
        }
        return ModCapabilities.get(villager).map(state -> {
            McaHunterAggressionPolicy.HunterSource source = resolveWeaponSource(villager, state);
            ItemStack expectedWeapon = source == null ? ItemStack.EMPTY : resolveHunterWeapon(villager, state, source);
            return !expectedWeapon.isEmpty()
                    && !isSameWeaponItem(replacement, expectedWeapon);
        }).orElse(false);
    }

    /**
     * Kept for compatibility with callers that still think in hands. New
     * writers must use the slot variant above so direct {@code setItemSlot}
     * calls receive the same protection.
     */
    public static boolean shouldRejectPersistentHunterWeaponMutation(
            VillagerEntityMCA villager,
            InteractionHand hand,
            ItemStack replacement
    ) {
        return villager != null
                && hand != null
                && shouldRejectPersistentHunterWeaponSlotMutation(
                villager,
                villager.getSlotForHand(hand),
                replacement
        );
    }

    /**
     * MCA's normal recovery implementation first transfers a food stack into
     * the dominant hand and only consumes it on the next recovery interval.
     * That transfer is exactly what used to displace a Hunter weapon. Consume
     * one real inventory item now, return any container result safely, and
     * report success so MCA does not execute the hand-transfer path.
     */
    public static boolean consumePersistentHunterRecoveryFood(
            VillagerEntityMCA villager,
            Predicate<ItemStack> foodPredicate
    ) {
        if (villager == null
                || villager.level().isClientSide
                || foodPredicate == null
                || !shouldPreserveHunterWeaponDuringMcaEquipmentTask(villager)) {
            return false;
        }

        SimpleContainer inventory = villager.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (candidate.isEmpty() || !foodPredicate.test(candidate)) {
                continue;
            }

            ItemStack consumed = inventory.removeItem(slot, 1);
            if (consumed.isEmpty()) {
                continue;
            }
            // This is MCA's exact recovery operation (VillagerEntityMCA.eat),
            // including nutrition-based healing, sounds and item effects.
            ItemStack result = villager.eat(villager.level(), consumed);
            returnRecoveryResultToInventory(villager, inventory, result);
            inventory.setChanged();
            McaVampirismCompat.debugLog(
                    "[HUNTER_WEAPON] recovery consumed-from-inventory villager={} item={} remaining={}",
                    villager.getUUID(),
                    BuiltInRegistries.ITEM.getKey(consumed.getItem()),
                    inventory.getItem(slot).getCount()
            );
            return true;
        }
        return false;
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
        ModCapabilities.get(villager).ifPresent(state -> {
            McaHunterAggressionPolicy.HunterSource source = resolveWeaponSource(villager, state);
            if (source == null) {
                restoreDefenseWeapon(villager);
                state.clearHunterWeaponSelection();
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
        if (villager == null || villager.level().isClientSide) {
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
        if (state.isDefensiveMode()
                && McaHunterAggressionPolicy.canEnterDefensiveAggressiveMode(
                villager,
                McaHunterAggressionPolicy.DefensiveReason.TEMPORARY_DEFENSE)) {
            return McaHunterAggressionPolicy.HunterSource.DEFENSIVE_MODE;
        }
        return null;
    }

    private static boolean isPersistentHunterWeaponSource(McaHunterAggressionPolicy.HunterSource source) {
        return source != null && source != McaHunterAggressionPolicy.HunterSource.DEFENSIVE_MODE;
    }

    /**
     * Returns whether the current defense state is the only reason a weapon
     * could be equipped. Real Hunter sources always win over a temporary
     * defensive reaction so their persisted weapon never flickers.
     */
    public static boolean isTemporaryDefenseOnly(
            VillagerEntityMCA villager,
            com.guilh.mca_vampirism_compat.VampiricVillagerState state
    ) {
        return state != null && state.isDefensiveMode() && !hasRealHunterSource(villager, state);
    }

    private static boolean hasRealHunterSource(
            VillagerEntityMCA villager,
            com.guilh.mca_vampirism_compat.VampiricVillagerState state
    ) {
        return state.isHunterVillageApplied()
                || McaVillageFactionControlService.shouldVillagerBeHunterByVillageControl(villager)
                || (McaTraitBridge.hasHunterTrait(villager)
                && McaHunterAggressionPolicy.isManualHunterAllowed(villager));
    }

    public static void equipHunterWeaponIfAllowed(VillagerEntityMCA villager, McaHunterAggressionPolicy.HunterSource source) {
        if (!McaHunterAggressionPolicy.canEquipHunterWeapon(villager, source)) {
            return;
        }
        ModCapabilities.get(villager).ifPresent(state -> {
            InteractionHand weaponHand = prepareManagedHunterWeaponHand(villager, state);
            ItemStack weapon = resolveHunterWeapon(villager, state, source);
            if (weapon.isEmpty()) {
                return;
            }
            ItemStack current = villager.getItemInHand(weaponHand);
            // The reserved identity is the item itself, not a freshly-created
            // stack's NBT. Damage, enchantments and mod-added data must be
            // allowed to evolve without reconstructing the weapon every tick.
            if (!isSameWeaponItem(current, weapon)) {
                preserveDisplacedHunterHandItem(villager, state, weapon);
                setItemInHandAsHunterWeaponOwner(villager, weaponHand, weapon);
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
            ItemStack expectedHunterWeapon = resolvePersistentHunterWeapon(villager, state);
            preserveDisplacedHunterHandItem(villager, state, expectedHunterWeapon);
            setItemInHandAsHunterWeaponOwner(
                    villager,
                    managedHunterWeaponHand(state),
                    state.getOriginalMainHandItem()
            );
            state.clearOriginalMainHandItem();
            state.clearHunterWeaponHand();
            state.clearDefensiveWeaponEquippedLogged();
            McaVampirismCompat.debugLog("[DEFENSE] weapon restored villager={}", villager.getUUID());
        });
    }

    /**
     * MCA guards strike with their trait-selected dominant hand, not always
     * Minecraft's MAIN_HAND. Keeping the selected hand in compat state makes
     * the equipment redirect, visible item and native attack use one owner.
     *
     * Old releases always equipped MAIN_HAND. When a left-handed Hunter from
     * such a save is first reconciled, restore that old main-hand snapshot,
     * capture the real dominant hand and move the selected weapon once. This
     * prevents item loss and fixes the previously empty attacking hand.
     */
    private static InteractionHand prepareManagedHunterWeaponHand(
            VillagerEntityMCA villager,
            com.guilh.mca_vampirism_compat.VampiricVillagerState state
    ) {
        InteractionHand desiredHand = villager.getDominantHand();
        String savedHand = state.getHunterWeaponHand();
        if (savedHand.isEmpty()) {
            if (state.isOriginalMainHandItemSaved() && desiredHand != InteractionHand.MAIN_HAND) {
                ItemStack legacyWeapon = villager.getItemInHand(InteractionHand.MAIN_HAND).copy();
                setItemInHandAsHunterWeaponOwner(
                        villager,
                        InteractionHand.MAIN_HAND,
                        state.getOriginalMainHandItem()
                );
                state.clearOriginalMainHandItem();
                state.saveOriginalMainHandItem(villager.getItemInHand(desiredHand));
                if (!legacyWeapon.isEmpty()) {
                    setItemInHandAsHunterWeaponOwner(villager, desiredHand, legacyWeapon);
                }
            } else if (!state.isOriginalMainHandItemSaved()) {
                state.saveOriginalMainHandItem(villager.getItemInHand(desiredHand));
            }
            state.setHunterWeaponHand(serializeHunterWeaponHand(desiredHand));
            return desiredHand;
        }

        InteractionHand previousHand = managedHunterWeaponHand(state);
        if (previousHand != desiredHand) {
            if (state.isOriginalMainHandItemSaved()) {
                setItemInHandAsHunterWeaponOwner(villager, previousHand, state.getOriginalMainHandItem());
                state.clearOriginalMainHandItem();
            }
            state.saveOriginalMainHandItem(villager.getItemInHand(desiredHand));
            state.setHunterWeaponHand(serializeHunterWeaponHand(desiredHand));
        }
        return desiredHand;
    }

    private static InteractionHand managedHunterWeaponHand(
            com.guilh.mca_vampirism_compat.VampiricVillagerState state
    ) {
        return "off_hand".equals(state.getHunterWeaponHand())
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;
    }

    /**
     * The MCA 7.6 attack implementation always asks for {@code getMainHandItem}
     * when calculating enchantment bonuses. A left-handed MCA villager keeps
     * the real Hunter weapon in OFF_HAND, so the targeted attack mixin uses
     * this predicate before substituting the dominant hand.
     */
    public static boolean shouldUseDominantHunterWeaponForNativeAttack(VillagerEntityMCA villager) {
        return shouldPreserveHunterWeaponDuringMcaEquipmentTask(villager);
    }

    private static boolean isSameWeaponItem(ItemStack stack, ItemStack weapon) {
        return stack != null
                && weapon != null
                && !stack.isEmpty()
                && !weapon.isEmpty()
                && stack.is(weapon.getItem());
    }

    private static void setItemInHandAsHunterWeaponOwner(
            VillagerEntityMCA villager,
            InteractionHand hand,
            ItemStack stack
    ) {
        HunterWeaponSlotMutationScope.run(() -> villager.setItemInHand(hand, stack));
    }

    /**
     * Older releases could leave recovery food or task equipment in the
     * reserved hand. Preserve that real displaced item before putting the
     * selected Hunter weapon back, unless it is the original snapshot or the
     * compat-managed weapon itself. This makes the migration lossless without
     * duplicating the virtual Hunter item on trait removal.
     */
    private static void preserveDisplacedHunterHandItem(
            VillagerEntityMCA villager,
            com.guilh.mca_vampirism_compat.VampiricVillagerState state,
            ItemStack expectedHunterWeapon
    ) {
        ItemStack current = villager.getItemInHand(managedHunterWeaponHand(state));
        if (current.isEmpty()
                || ItemStack.isSameItemSameTags(current, state.getOriginalMainHandItem())
                || isSameWeaponItem(current, expectedHunterWeapon)
                || isLikelyManagedHunterWeapon(current)) {
            return;
        }
        ItemStack remainder = villager.getInventory().addItem(current.copy());
        if (!remainder.isEmpty()) {
            villager.spawnAtLocation(remainder);
        }
        villager.getInventory().setChanged();
        McaVampirismCompat.debugLog(
                "[HUNTER_WEAPON] migrated displaced-hand item villager={} item={} droppedRemainder={}",
                villager.getUUID(),
                BuiltInRegistries.ITEM.getKey(current.getItem()),
                !remainder.isEmpty()
        );
    }

    private static void returnRecoveryResultToInventory(
            VillagerEntityMCA villager,
            SimpleContainer inventory,
            ItemStack result
    ) {
        if (result == null || result.isEmpty()) {
            return;
        }
        ItemStack remainder = inventory.addItem(result.copy());
        if (!remainder.isEmpty()) {
            villager.spawnAtLocation(remainder);
            McaVampirismCompat.debugLog(
                    "[HUNTER_WEAPON] recovery container dropped after full inventory villager={} item={}",
                    villager.getUUID(),
                    BuiltInRegistries.ITEM.getKey(remainder.getItem())
            );
        }
    }

    private static ItemStack resolvePersistentHunterWeapon(
            VillagerEntityMCA villager,
            com.guilh.mca_vampirism_compat.VampiricVillagerState state
    ) {
        Item explicitOverride = resolveExplicitHunterWeaponOverride();
        if (explicitOverride != Items.AIR) {
            return new ItemStack(explicitOverride);
        }
        HunterWeaponSelection selection = HunterWeaponSelection.fromSerializedName(state.getHunterWeaponSelection());
        if (selection == HunterWeaponSelection.UNASSIGNED) {
            return ItemStack.EMPTY;
        }
        Item weapon = selection == HunterWeaponSelection.STAKE
                ? firstExistingItem("vampirism:stake", "vampirism:pitchfork", "vampirism_integrations:pitchfork", "minecraft:iron_hoe", "minecraft:iron_axe")
                : firstExistingItem("vampirism:pitchfork", "vampirism_integrations:pitchfork", "vampirism:stake", "minecraft:iron_hoe", "minecraft:iron_axe");
        return weapon == Items.AIR ? ItemStack.EMPTY : new ItemStack(weapon);
    }

    private static boolean isLikelyManagedHunterWeapon(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        return item == lookupItem("vampirism:pitchfork")
                || item == lookupItem("vampirism_integrations:pitchfork")
                || item == lookupItem("vampirism:stake");
    }

    private static String serializeHunterWeaponHand(InteractionHand hand) {
        return hand == InteractionHand.OFF_HAND ? "off_hand" : "main_hand";
    }

    private static ItemStack resolveHunterWeapon(
            VillagerEntityMCA villager,
            com.guilh.mca_vampirism_compat.VampiricVillagerState state,
            McaHunterAggressionPolicy.HunterSource source
    ) {
        // Temporary defense retains its existing opt-in, configured single
        // weapon behavior. Only real Hunters participate in the 75/25 pool.
        if (source == McaHunterAggressionPolicy.HunterSource.DEFENSIVE_MODE) {
            return resolveConfiguredDefenseWeapon();
        }

        Item explicitOverride = resolveExplicitHunterWeaponOverride();
        if (explicitOverride != Items.AIR) {
            return new ItemStack(explicitOverride);
        }

        HunterWeaponSelection selection = HunterWeaponSelection.fromSerializedName(state.getHunterWeaponSelection());
        if (selection == HunterWeaponSelection.UNASSIGNED) {
            selection = HunterWeaponSelection.roll(villager.getRandom().nextDouble(),
                    McaVampirismCompatConfig.hunterVillagerStakeChance());
            state.setHunterWeaponSelection(selection.serializedName());
        }

        Item weapon = selection == HunterWeaponSelection.STAKE
                ? firstExistingItem("vampirism:stake", "vampirism:pitchfork", "vampirism_integrations:pitchfork", "minecraft:iron_hoe", "minecraft:iron_axe")
                : firstExistingItem("vampirism:pitchfork", "vampirism_integrations:pitchfork", "vampirism:stake", "minecraft:iron_hoe", "minecraft:iron_axe");
        return weapon == Items.AIR ? ItemStack.EMPTY : new ItemStack(weapon);
    }

    private static Item resolveExplicitHunterWeaponOverride() {
        String configured = McaVampirismCompatConfig.defensiveVillagerWeaponType();
        if (isDefaultHunterWeaponConfiguration(configured)) {
            return Items.AIR;
        }
        return lookupConfiguredItem(configured);
    }

    private static boolean isDefaultHunterWeaponConfiguration(String configured) {
        if (configured == null || configured.isBlank()) {
            return true;
        }
        String normalized = configured.toLowerCase(Locale.ROOT).trim();
        return "pitchfork".equals(normalized)
                || "vampirism:pitchfork".equals(normalized)
                || "vampirism_integrations:pitchfork".equals(normalized);
    }

    private static ItemStack resolveConfiguredDefenseWeapon() {
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
        Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(id));
        return item == null ? Items.AIR : item;
    }
}
