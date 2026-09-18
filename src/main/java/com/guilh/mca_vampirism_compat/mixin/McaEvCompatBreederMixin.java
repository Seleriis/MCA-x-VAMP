package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.easyvillagers.McaEvStatePayload;
import com.guilh.mca_vampirism_compat.easyvillagers.McaEvBreedingEligibility;
import com.guilh.mca_vampirism_compat.easyvillagers.readonly.StoredMcaFactionResolver;
import com.guilh.mca_vampirism_compat.inheritance.McaBabyInheritancePayload;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

@Pseudo
@Mixin(targets = "com.guilh.mcaevcompat.EasyVillagersBlockCompat", priority = 2000, remap = false)
public abstract class McaEvCompatBreederMixin {
    @Unique
    private static final ThreadLocal<Set<Integer>> MCA_VAMP_COMPAT_EMPTY_OUTPUTS = new ThreadLocal<>();

    @Inject(method = "tryBreedMcaChild", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private static void mcaVampirismCompat$captureEmptySlots(BlockEntity blockEntity,
                                                             ItemStack parentA,
                                                             ItemStack parentB,
                                                             NonNullList<ItemStack> food,
                                                             NonNullList<ItemStack> output,
                                                             CallbackInfoReturnable<Object> cir) {
        ItemStack parentSnapshotA = StoredMcaFactionResolver.getBreederParentStack(blockEntity, 1, parentA);
        ItemStack parentSnapshotB = StoredMcaFactionResolver.getBreederParentStack(blockEntity, 2, parentB);
        if (McaEvBreedingEligibility.isInfertileMcaParent(parentSnapshotA)
                || McaEvBreedingEligibility.isInfertileMcaParent(parentSnapshotB)) {
            Object cancel = mcaVampirismCompat$cancelBreederResult();
            if (cancel != null) {
                cir.setReturnValue(cancel);
                return;
            }
        }

        Set<Integer> emptySlots = new HashSet<>();
        for (int i = 0; i < output.size(); i++) {
            if (output.get(i).isEmpty()) {
                emptySlots.add(i);
            }
        }
        MCA_VAMP_COMPAT_EMPTY_OUTPUTS.set(emptySlots);
    }

    @Unique
    private static Object mcaVampirismCompat$cancelBreederResult() {
        try {
            Class<?> resultType = Class.forName(
                    "com.guilh.mcaevcompat.EasyVillagersBlockCompat$BreederResult",
                    false,
                    McaEvCompatBreederMixin.class.getClassLoader());
            for (Object constant : resultType.getEnumConstants()) {
                if ("CANCEL".equals(String.valueOf(constant))) {
                    return constant;
                }
            }
        } catch (ClassNotFoundException | LinkageError ignored) {
            // The target mixin itself is optional.  If an incompatible addon
            // does not expose this result enum, leave its breeder untouched.
        }
        return null;
    }

    @Inject(method = "tryBreedMcaChild", at = @At("RETURN"), require = 0, remap = false)
    private static void mcaVampirismCompat$writeChildInheritance(BlockEntity blockEntity,
                                                                 ItemStack parentA,
                                                                 ItemStack parentB,
                                                                 NonNullList<ItemStack> food,
                                                                 NonNullList<ItemStack> output,
                                                                 CallbackInfoReturnable<Object> cir) {
        Set<Integer> emptySlots = MCA_VAMP_COMPAT_EMPTY_OUTPUTS.get();
        MCA_VAMP_COMPAT_EMPTY_OUTPUTS.remove();
        Object result = cir.getReturnValue();
        if (emptySlots == null || result == null || !"SUCCESS".equals(result.toString())
                || blockEntity.getLevel() == null) {
            return;
        }
        ItemStack parentSnapshotA = StoredMcaFactionResolver.getBreederParentStack(blockEntity, 1, parentA);
        ItemStack parentSnapshotB = StoredMcaFactionResolver.getBreederParentStack(blockEntity, 2, parentB);
        int vampireParents = factionCount(parentSnapshotA, true) + factionCount(parentSnapshotB, true);
        int werewolfParents = factionCount(parentSnapshotA, false) + factionCount(parentSnapshotB, false);
        for (int slot : emptySlots) {
            ItemStack child = output.get(slot);
            if (!child.isEmpty()) {
                if (McaEvStatePayload.readState(child)
                        .map(state -> state.getBoolean("FactionInheritanceProcessed"))
                        .orElse(false)) {
                    return;
                }
                McaBabyInheritancePayload.decideAndWrite(
                        child,
                        vampireParents,
                        werewolfParents,
                        blockEntity.getLevel().getRandom()
                );
                return;
            }
        }
    }

    @Unique
    private static int factionCount(ItemStack stack, boolean vampire) {
        StoredMcaFactionResolver.StoredFactionSnapshot snapshot =
                StoredMcaFactionResolver.resolveFactionSnapshot(stack);
        return (vampire ? snapshot.isVampire() : snapshot.isWerewolf()) ? 1 : 0;
    }
}
