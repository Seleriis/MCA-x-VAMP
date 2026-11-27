package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.easyvillagers.LegacyEmptyItemStackSanitizer;
import com.guilh.mca_vampirism_compat.easyvillagers.McaEvStatePayload;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Cleans old empty-item data in mca-ev-compat's copy before MCA creates its
 * preview entity. This is also the path used by client block renderers.
 */
@Pseudo
@Mixin(targets = "com.guilh.mcaevcompat.CompatLogic", priority = 2000, remap = false)
public abstract class McaEvCompatLegacyItemSanitizerMixin {
    @Inject(method = "sanitizeStoredVillagerData", at = @At("RETURN"), require = 0, remap = false)
    private static void mcaVampirismCompat$sanitizeLegacyItemCompounds(
            CompoundTag data,
            CallbackInfoReturnable<CompoundTag> cir
    ) {
        LegacyEmptyItemStackSanitizer.sanitize(cir.getReturnValue());
    }

    /**
     * createStoredMcaVillagerEntity hydrates an MCA entity through this
     * method, including the entities made solely for client block previews.
     */
    @Inject(method = "prepareMcaHydrationData", at = @At("RETURN"), require = 0, remap = false)
    private static void mcaVampirismCompat$sanitizeHydrationData(
            CompoundTag data,
            String entityType,
            CallbackInfoReturnable<CompoundTag> cir
    ) {
        LegacyEmptyItemStackSanitizer.sanitize(cir.getReturnValue());
    }

    @Inject(method = "sanitizeStoredMcaVillagerStack", at = @At("RETURN"), require = 0, remap = false)
    private static void mcaVampirismCompat$sanitizeOwnLegacyPayload(
            ItemStack stack,
            CallbackInfoReturnable<ItemStack> cir
    ) {
        McaEvStatePayload.sanitizeLegacyStatePayload(cir.getReturnValue());
    }
}
