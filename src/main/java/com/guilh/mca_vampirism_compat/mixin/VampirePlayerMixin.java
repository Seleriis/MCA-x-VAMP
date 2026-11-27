package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.service.McaBloodValueRepairService;
import com.guilh.mca_vampirism_compat.trait.McaTraitBridge;
import com.guilh.mca_vampirism_compat.util.VampirismUtil;
import de.teamlapen.vampirism.api.entity.player.vampire.IVampirePlayer;
import de.teamlapen.vampirism.api.entity.vampire.IVampire;
import de.teamlapen.vampirism.entity.player.vampire.VampirePlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = VampirePlayer.class, remap = false)
public abstract class VampirePlayerMixin {
    @Inject(method = "determineBiteType", at = @At("HEAD"), cancellable = true)
    private void mcaVampirismCompat$blockVampireTargets(LivingEntity target,
                                                       CallbackInfoReturnable<IVampirePlayer.BITE_TYPE> cir) {
        if (shouldBlockDrainTarget(target)) {
            McaVampirismCompat.debugLog(
                    "[BITE] VampirePlayer.determineBiteType blocked target={} targetType={}",
                    target.getUUID(),
                    target.getType()
            );
            showBlockedMessage(target);
            cir.setReturnValue(IVampirePlayer.BITE_TYPE.NONE);
            return;
        }
        if (McaBloodValueRepairService.prepareMcaBloodSourceForBite(target)) {
            McaVampirismCompat.debugLog(
                    "[BITE] prepared MCA human blood source before bite selection target={} targetType={}",
                    target.getUUID(),
                    target.getType()
            );
        }
    }

    @Inject(method = "canBeBitten", at = @At("HEAD"), cancellable = true)
    private void mcaVampirismCompat$blockVampirePlayerBloodDrain(IVampire vampire,
                                                                 CallbackInfoReturnable<Boolean> cir) {
        VampirePlayer self = (VampirePlayer) (Object) this;
        if (McaVampirismCompatConfig.preventVampiresBitingVampires() && self.getLevel() > 0) {
            cir.setReturnValue(false);
        }
    }

    private boolean shouldBlockDrainTarget(LivingEntity target) {
        if (target == null) {
            return false;
        }
        if (McaTraitBridge.isVampireByTraitOrCapability(target)) {
            return McaVampirismCompatConfig.preventVampiresBitingMcaVampires()
                    || McaVampirismCompatConfig.preventVampiresBitingVampires();
        }
        return McaVampirismCompatConfig.preventVampiresBitingVampires() && VampirismUtil.isVampiric(target);
    }

    private void showBlockedMessage(LivingEntity target) {
        VampirePlayer self = (VampirePlayer) (Object) this;
        LivingEntity source = self.getRepresentingEntity();
        if (!(source instanceof Player player) || player.level().isClientSide
                || !McaVampirismCompatConfig.showAlreadyVampireBiteMessage()) {
            return;
        }
        String key = McaTraitBridge.isVampireByTraitOrCapability(target)
                ? "mca_vamp_compat.bite.already_vampire"
                : "mca_vamp_compat.bite.vampire_cannot_be_drained";
        player.displayClientMessage(Component.translatable(key), true);
    }
}
