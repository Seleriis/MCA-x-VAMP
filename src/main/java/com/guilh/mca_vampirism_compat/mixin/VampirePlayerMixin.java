package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.service.McaVampireFeedProtection;
import de.teamlapen.vampirism.api.entity.player.vampire.IVampirePlayer;
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
        VampirePlayer self = (VampirePlayer) (Object) this;
        if (McaVampireFeedProtection.shouldBlockDrain(self.getRepresentingEntity(), target)) {
            McaVampirismCompat.debugLog(
                    "[BITE] VampirePlayer.determineBiteType blocked target={} targetType={}",
                    target.getUUID(),
                    target.getType()
            );
            showBlockedMessage(target);
            cir.setReturnValue(IVampirePlayer.BITE_TYPE.NONE);
        }
    }

    private void showBlockedMessage(LivingEntity target) {
        VampirePlayer self = (VampirePlayer) (Object) this;
        LivingEntity source = self.getRepresentingEntity();
        if (!(source instanceof Player player) || player.level().isClientSide
                || !McaVampirismCompatConfig.showAlreadyVampireBiteMessage()) {
            return;
        }
        player.displayClientMessage(Component.translatable("mca_vamp_compat.bite.already_vampire"), true);
    }
}
