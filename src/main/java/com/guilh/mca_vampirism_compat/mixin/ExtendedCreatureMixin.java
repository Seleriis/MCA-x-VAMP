package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.ai.McaVampireAi;
import com.guilh.mca_vampirism_compat.attribute.McaVampireAttributeHandler;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.faction.PlayerFaction;
import com.guilh.mca_vampirism_compat.service.McaBloodValueRepairService;
import com.guilh.mca_vampirism_compat.service.McaHunterBloodService;
import com.guilh.mca_vampirism_compat.service.McaVampireFactionService;
import com.guilh.mca_vampirism_compat.service.McaVampireInfectionService;
import com.guilh.mca_vampirism_compat.service.McaVampireStateService;
import com.guilh.mca_vampirism_compat.trade.McaFactionTradeService;
import com.guilh.mca_vampirism_compat.service.McaWerewolfStateService;
import com.guilh.mca_vampirism_compat.service.VampireSkillIntegrationService;
import com.guilh.mca_vampirism_compat.util.McaVillagerUtil;
import de.teamlapen.vampirism.api.entity.IExtendedCreatureVampirism;
import de.teamlapen.vampirism.api.entity.convertible.IConvertedCreature;
import de.teamlapen.vampirism.api.entity.vampire.IVampire;
import de.teamlapen.vampirism.entity.ExtendedCreature;
import forge.net.conczin.mca.entity.VillagerEntityMCA;
import forge.net.conczin.mca.entity.ai.relationship.AgeState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ExtendedCreature.class, remap = false)
public abstract class ExtendedCreatureMixin {
    @Shadow
    @Final
    private PathfinderMob entity;

    @Inject(method = "makeVampire", at = @At("HEAD"), cancellable = true)
    private void mcaVampirismCompat$keepMcaVillagerEntity(CallbackInfoReturnable<IConvertedCreature<?>> cir) {
        if (!McaVillagerUtil.isMcaVillager(entity)) {
            return;
        }
        McaVampirismCompat.debugLog(
                "[STATE] intercepting Vampirism conversion entity={} type={}",
                entity.getUUID(),
                entity.getType()
        );
        if (McaHunterBloodService.isHunterVampirismImmune(entity)) {
            McaHunterBloodService.clearHunterInfectionState(entity);
            McaVampirismCompat.debugLog(
                    "[HUNTER-BLOOD] makeVampire canceled target={} reason=MCA_HUNTER_IMMUNE",
                    entity.getUUID()
            );
            cir.setReturnValue(null);
            return;
        }
        if (McaVampirismCompatConfig.werewolfVillagersImmuneToVampirism()
                && McaWerewolfStateService.isWerewolf(entity)) {
            McaVampirismCompat.debugLog(
                    "[WEREWOLF] makeVampire canceled target={} reason=WEREWOLF_IMMUNE",
                    entity.getUUID()
            );
            cir.setReturnValue(null);
            return;
        }
        if (!canConvertMcaVillager()) {
            ModCapabilities.get(entity).ifPresent(state -> state.clearInfection());
            McaVampirismCompat.debugLog("[STATE] conversion canceled by config/age entity={}", entity.getUUID());
            cir.setReturnValue(null);
            return;
        }

        if (entity instanceof VillagerEntityMCA villager) {
            McaVampireStateService.markConverted(villager, null);
            ModCapabilities.get(villager).ifPresent(state ->
                    state.getLastVampirePlayerBiteUuid().ifPresent(playerUuid -> {
                        if (villager.level() instanceof ServerLevel serverLevel) {
                            ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(playerUuid);
                            if (player != null) {
                                McaFactionTradeService.clearConversionBitePenaltyIfSafe(player, villager);
                            }
                        }
                    }));
        } else {
            McaVampireAi.registerGoalsIfNeeded(entity);
            McaVampireAttributeHandler.applyVampireAttributes(entity, true);
        }
        // makeVampire normally returns null when no converted entity is produced; the caller must tolerate it.
        cir.setReturnValue(null);
    }

    @Inject(method = "canBeBitten", at = @At("HEAD"), cancellable = true)
    private void mcaVampirismCompat$blockMcaVampireBloodDrain(IVampire vampire, CallbackInfoReturnable<Boolean> cir) {
        if (McaVampirismCompatConfig.preventVampiresBitingMcaVampires() && McaVillagerUtil.isMcaVampire(entity)) {
            if (entity.tickCount % 100 == 0) {
                McaVampirismCompat.debugLog("[BITE] ExtendedCreature.canBeBitten blocked entity={}", entity.getUUID());
            }
            cir.setReturnValue(false);
            return;
        }
        McaBloodValueRepairService.repairMcaBloodValue(entity, (IExtendedCreatureVampirism) (Object) this);
    }

    @Inject(method = "onBite", at = @At("HEAD"), cancellable = true)
    private void mcaVampirismCompat$preventMcaVampireBloodLoss(IVampire vampire, CallbackInfoReturnable<Integer> cir) {
        if (McaVampirismCompatConfig.preventVampiresBitingMcaVampires() && McaVillagerUtil.isMcaVampire(entity)) {
            if (entity.tickCount % 100 == 0) {
                McaVampirismCompat.debugLog("[BITE] ExtendedCreature.onBite blocked entity={}", entity.getUUID());
            }
            cir.setReturnValue(0);
            return;
        }
        McaBloodValueRepairService.repairMcaBloodValue(entity, (IExtendedCreatureVampirism) (Object) this);
    }

    @Inject(method = "canBeInfected", at = @At("HEAD"), cancellable = true)
    private void mcaVampirismCompat$canApplyConfiguredMcaSanguinare(IVampire vampire, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity source = VampireSkillIntegrationService.representingEntity(vampire);
        if (McaHunterBloodService.blockHunterVampireInfection(source, entity)) {
            cir.setReturnValue(false);
            return;
        }
        if (McaVampirismCompatConfig.werewolfVillagersImmuneToVampirism()
                && McaWerewolfStateService.isWerewolf(entity)) {
            cir.setReturnValue(false);
            return;
        }
        if (shouldHandlePlayerInfectAction(source)) {
            cir.setReturnValue(McaVampireInfectionService.canApplyConfiguredSanguinare(source, entity));
        }
    }

    @Inject(method = "tryInfect", at = @At("HEAD"), cancellable = true)
    private void mcaVampirismCompat$applyConfiguredMcaSanguinare(IVampire vampire, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity source = VampireSkillIntegrationService.representingEntity(vampire);
        if (McaHunterBloodService.blockHunterVampireInfection(source, entity)) {
            cir.setReturnValue(false);
            return;
        }
        if (McaVampirismCompatConfig.werewolfVillagersImmuneToVampirism()
                && McaWerewolfStateService.isWerewolf(entity)) {
            cir.setReturnValue(false);
            return;
        }
        if (!shouldHandlePlayerInfectAction(source) || !McaVampirismCompatConfig.infectiousBiteStillUsesConfiguredDuration()) {
            return;
        }
        cir.setReturnValue(McaVampireInfectionService.tryApplyConfiguredSanguinare(
                source,
                entity,
                McaVampireInfectionService.InfectionSourceType.PLAYER,
                McaVampirismCompatConfig.infectiousBiteOverridesConversionChance()
        ));
    }

    private boolean canConvertMcaVillager() {
        if (!McaVampirismCompatConfig.allowMcaVillagerInfection()) {
            return false;
        }
        return McaVampirismCompatConfig.allowChildInfection()
                || !(entity instanceof VillagerEntityMCA villager)
                || villager.getAgeState() == AgeState.ADULT;
    }

    private boolean shouldHandlePlayerInfectAction(LivingEntity source) {
        return McaVillagerUtil.isMcaVillager(entity)
                && source instanceof Player player
                && McaVampireFactionService.classifyPlayer(player) == PlayerFaction.VAMPIRE
                && (McaVampirismCompatConfig.infectiousBiteOverridesConversionChance()
                || McaVampirismCompatConfig.infectiousBiteStillUsesConfiguredDuration());
    }
}
