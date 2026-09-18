package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.service.faction.hunter.McaHunterAggressionPolicy;
import com.guilh.mca_vampirism_compat.trait.McaTraitBridge;
import de.teamlapen.vampirism.api.entity.factions.IFaction;
import de.teamlapen.vampirism.api.world.ITotem;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.relationship.AgeState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Optional;

public final class McaVillageFactionControlService {
    private static final String HUNTER_FACTION_ID = "vampirism:hunter";

    private McaVillageFactionControlService() {
    }

    public static boolean isInHunterControlledVillage(ServerLevel level, BlockPos pos) {
        return findTotemNear(level, pos)
                .filter(totem -> isHunterTotemAtPosition(totem, pos))
                .isPresent();
    }

    public static boolean shouldVillagerBeHunterByVillageControl(VillagerEntityMCA villager) {
        if (!McaVampirismCompatConfig.hunterVillageControlGrantsHunterTrait()
                || villager == null
                || villager.level().isClientSide
                || villager.getAgeState() != AgeState.ADULT
                || McaVampireStateService.isVampire(villager)
                || (McaWerewolfStateService.isWerewolf(villager)
                && McaVampirismCompatConfig.hunterVillageDoesNotConvertWerewolvesToHunters())
                || !(villager.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        return isInHunterControlledVillage(serverLevel, villager.blockPosition());
    }

    public static Optional<BlockPos> getHunterVillageTotemPos(VillagerEntityMCA villager) {
        if (villager == null || villager.level().isClientSide || !(villager.level() instanceof ServerLevel serverLevel)) {
            return Optional.empty();
        }
        return findTotemNear(serverLevel, villager.blockPosition())
                .filter(totem -> isHunterTotemAtPosition(totem, villager.blockPosition()))
                .map(McaVillageFactionControlService::totemPos);
    }

    public static void syncHunterTraitFromVillageControl(VillagerEntityMCA villager) {
        if (villager == null || villager.level().isClientSide) {
            return;
        }
        if (shouldVillagerBeHunterByVillageControl(villager)) {
            applyHunterVillageState(villager);
        } else {
            removeHunterVillageState(villager);
        }
    }

    public static void applyHunterVillageState(VillagerEntityMCA villager) {
        applyHunterVillageState(villager, McaHunterAggressionPolicy.HunterSource.HUNTER_VILLAGE);
    }

    public static void applyHunterVillageState(VillagerEntityMCA villager, McaHunterAggressionPolicy.HunterSource source) {
        if (villager == null || villager.level().isClientSide || McaVampireStateService.isVampire(villager)
                || (McaWerewolfStateService.isWerewolf(villager)
                && McaVampirismCompatConfig.hunterVillageDoesNotConvertWerewolvesToHunters())) {
            return;
        }
        if (!McaHunterAggressionPolicy.canBecomeHunterAligned(villager, source)) {
            McaVampirismCompat.debugLog(
                    "[HUNTER-VILLAGE] state denied villager={} source={} reason={}",
                    villager.getUUID(),
                    source,
                    McaHunterAggressionPolicy.denyReason(villager, source)
            );
            return;
        }
        ModCapabilities.get(villager).ifPresent(state -> {
            boolean alreadyHadTrait = McaTraitBridge.hasHunterTrait(villager);
            if (McaVampirismCompatConfig.hunterVillageControlGrantsHunterTrait() && !alreadyHadTrait) {
                McaTraitBridge.addHunterTrait(villager);
                state.setHunterVillageApplied(true);
                McaVampirismCompat.debugLog(
                        "[HUNTER-VILLAGE] hunter trait added villager={} source={}",
                        villager.getUUID(),
                        source
                );
            } else if (alreadyHadTrait && !state.isHunterVillageApplied() && !McaVampirismCompatConfig.hunterVillagerKeepManualHunterTrait()) {
                state.setHunterVillageApplied(true);
            }
            state.setHunterAligned(true, Long.MAX_VALUE);
            McaDefensiveVillagerService.reconcileHunterWeapon(villager);
            McaVampirismCompat.debugLog(
                    "[HUNTER-VILLAGE] state applied villager={} source={}",
                    villager.getUUID(),
                    source
            );
            McaSpecialStateNormalizer.normalize(villager);
        });
    }

    public static void removeHunterVillageState(VillagerEntityMCA villager) {
        if (villager == null || villager.level().isClientSide) {
            return;
        }
        ModCapabilities.get(villager).ifPresent(state -> {
            boolean hadVillageState = state.isHunterVillageApplied();
            boolean removeTrait = hadVillageState && McaVampirismCompatConfig.hunterVillagerRemoveTraitWhenVillageControlLost();
            if (removeTrait && McaTraitBridge.hasHunterTrait(villager)) {
                McaTraitBridge.removeHunterTrait(villager);
                McaVampirismCompat.debugLog(
                        "[HUNTER-VILLAGE] hunter trait removed villager={} reason=VILLAGE_CONTROL_LOST",
                        villager.getUUID()
                );
            }

            if (hadVillageState) {
                state.setHunterVillageApplied(false);
            }

            boolean hasHunterTrait = McaTraitBridge.hasHunterTrait(villager);
            if (!hasHunterTrait) {
                state.setHunterAligned(false, Long.MIN_VALUE);
                villager.setTarget(null);
            }

            if (!hasHunterTrait && isStaleAggressiveReplacementDefense(state.getDefensiveReason())) {
                McaDefensiveVillagerService.clearDefense(villager, "stale aggressive replacement after hunter village lost");
                McaVampirismCompat.debugLog(
                        "[HUNTER-VILLAGE] stale defensive hunter state cleared villager={} reason=NO_HUNTER_TRAIT",
                        villager.getUUID()
                );
                return;
            }

            McaDefensiveVillagerService.reconcileHunterWeapon(villager);

            if (!hadVillageState && !hasHunterTrait && (state.isHunterAligned()
                    || state.isHunterTraitApplied()
                    || state.isOriginalMainHandItemSaved()
                    || villager.getTarget() != null)) {
                McaVampirismCompat.debugLog(
                        "[HUNTER-VILLAGE] stale hunter alignment normalized villager={} reason=NO_HUNTER_TRAIT",
                        villager.getUUID()
                );
            }
        });
    }

    public static void syncVillagersFromCaptureFinish(ITotem totem, List<Villager> villagers) {
        if (totem == null) {
            return;
        }
        if (villagers != null) {
            for (Villager villager : villagers) {
                if (villager instanceof VillagerEntityMCA mcaVillager) {
                    syncHunterTraitFromVillageControl(mcaVillager);
                }
            }
        }
        Level level = totem.getTileWorld();
        AABB area = totem.getVillageArea();
        if (level instanceof ServerLevel serverLevel && area != null) {
            for (VillagerEntityMCA villager : serverLevel.getEntitiesOfClass(VillagerEntityMCA.class, area)) {
                syncHunterTraitFromVillageControl(villager);
            }
        }
    }

    /**
     * Reconciles only loaded MCA villagers after the compat-effective bounds
     * change. Capture keeps calling the native-area method above on purpose.
     */
    public static void syncVillagersForEffectiveBoundsChange(ServerLevel level, AABB previousArea, AABB currentArea) {
        if (level == null) {
            return;
        }
        AABB affectedArea = McaVillageEffectBounds.union(previousArea, currentArea);
        if (affectedArea == null) {
            return;
        }
        for (VillagerEntityMCA villager : level.getEntitiesOfClass(VillagerEntityMCA.class, affectedArea)) {
            syncHunterTraitFromVillageControl(villager);
        }
    }

    public static boolean isHunterControlledTotem(ITotem totem) {
        return totem != null && isHunterFaction(totem.getControllingFaction());
    }

    private static Optional<ITotem> findTotemNear(ServerLevel level, BlockPos pos) {
        try {
            return McaVillageEffectBoundsService.findActiveTotemAt(level, pos)
                    .map(totem -> (ITotem) totem);
        } catch (RuntimeException | LinkageError exception) {
            McaVampirismCompat.debugLog(
                    "[HUNTER-VILLAGE] failed to query totem pos={} error={}",
                    pos,
                    exception.toString()
            );
            return Optional.empty();
        }
    }

    private static boolean isHunterTotemAtPosition(ITotem totem, BlockPos pos) {
        if (!isHunterControlledTotem(totem)) {
            return false;
        }
        AABB area = McaVillageEffectBoundsService.getEffectiveArea(totem);
        return area == null || area.contains(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
    }

    private static boolean isHunterFaction(IFaction<?> faction) {
        try {
            return faction != null && HUNTER_FACTION_ID.equals(String.valueOf(faction.getID()));
        } catch (RuntimeException | LinkageError ignored) {
            return false;
        }
    }

    private static BlockPos totemPos(ITotem totem) {
        return totem instanceof BlockEntity blockEntity ? blockEntity.getBlockPos() : BlockPos.ZERO;
    }

    private static boolean isStaleAggressiveReplacementDefense(String reason) {
        if (reason == null) {
            return false;
        }
        String normalized = reason.toLowerCase(java.util.Locale.ROOT);
        return normalized.contains("makeaggressive")
                || normalized.contains("aggressive")
                || normalized.contains("replacement");
    }
}
