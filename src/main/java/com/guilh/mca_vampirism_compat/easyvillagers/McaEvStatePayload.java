package com.guilh.mca_vampirism_compat.easyvillagers;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.VampiricVillagerState;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.inheritance.McaBabyInheritancePayload;
import com.guilh.mca_vampirism_compat.inheritance.McaVampireInheritanceHandler.InheritedFaction;
import com.guilh.mca_vampirism_compat.trait.McaTraitBridge;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.Optional;

/**
 * Versioned, non-invasive state carried alongside mca-ev-compat's proprietary
 * villager snapshot. The owner snapshot is never rewritten or normalized here.
 */
public final class McaEvStatePayload {
    public static final String PAYLOAD_KEY = "mca_vamp_compat_state";
    private static final String VERSION_KEY = "Version";
    private static final String STATE_KEY = "State";
    private static final String VAMPIRE_KEY = "Vampire";
    private static final String HUNTER_KEY = "Hunter";
    private static final String WEREWOLF_KEY = "Werewolf";
    private static final int CURRENT_VERSION = 3;

    private McaEvStatePayload() {
    }

    public static boolean write(ItemStack stack, Villager villager) {
        if (stack == null || stack.isEmpty() || !(villager instanceof VillagerEntityMCA mcaVillager)) {
            return false;
        }
        Optional<VampiricVillagerState> optionalState = ModCapabilities.get(mcaVillager);
        boolean vampireCandidate = optionalState
                .map(state -> state.isConverted() || state.isVampireAppearanceApplied())
                .orElse(false)
                || McaTraitBridge.hasVampireTrait(mcaVillager);
        boolean hunterCandidate = optionalState
                .map(state -> state.isHunterAligned()
                        || state.isHunterTraitApplied()
                        || state.isHunterVillageApplied())
                .orElse(false)
                || McaTraitBridge.hasHunterTrait(mcaVillager);
        boolean werewolfCandidate = optionalState
                .map(state -> state.isWerewolfConverted()
                        || state.isWerewolfTraitApplied()
                        || state.isWerewolfAppearanceApplied()
                        || state.isWerewolfPartialForm())
                .orElse(false)
                || McaTraitBridge.hasWerewolfTrait(mcaVillager);

        // Persist one authoritative faction while still accepting every
        // positive state/trait classifier as input.
        boolean vampire = vampireCandidate;
        boolean hunter = !vampire && hunterCandidate;
        boolean werewolf = !vampire && !hunter && werewolfCandidate;

        CompoundTag payload = new CompoundTag();
        payload.putInt(VERSION_KEY, CURRENT_VERSION);
        optionalState.ifPresent(state ->
                payload.put(STATE_KEY, state.serializeNBT(mcaVillager.level().registryAccess())));
        payload.putBoolean(VAMPIRE_KEY, vampire);
        payload.putBoolean(HUNTER_KEY, hunter);
        payload.putBoolean(WEREWOLF_KEY, werewolf);

        CompoundTag root = customData(stack);
        root.put(PAYLOAD_KEY, payload);
        CustomData.set(DataComponents.CUSTOM_DATA, stack, root);
        return true;
    }

    public static boolean restore(ItemStack stack, Villager villager) {
        if (!(villager instanceof VillagerEntityMCA mcaVillager)) {
            return false;
        }
        Optional<CompoundTag> stateTag = readState(stack);
        boolean restoredState = false;
        if (stateTag.isPresent()) {
            restoredState = ModCapabilities.get(mcaVillager).map(state -> {
                state.deserializeNBT(mcaVillager.level().registryAccess(), stateTag.get());
                McaVampirismCompat.debugLog(
                        "[EASY-VILLAGERS] restored versioned faction state villager={} payloadVersion={}",
                        mcaVillager.getUUID(),
                        readFaction(stack).map(FactionSnapshot::version).orElse(0)
                );
                return true;
            }).orElse(false);
        }
        boolean restoredInheritance = McaBabyInheritancePayload.apply(stack, mcaVillager);
        return restoredState || restoredInheritance;
    }

    public static Optional<FactionSnapshot> readFaction(ItemStack stack) {
        CompoundTag payload = payload(stack);
        if (payload == null) {
            return McaBabyInheritancePayload.read(stack).map(faction -> new FactionSnapshot(
                    CURRENT_VERSION,
                    faction == InheritedFaction.VAMPIRE,
                    false,
                    faction == InheritedFaction.WEREWOLF
            ));
        }
        return Optional.of(new FactionSnapshot(
                payload.getInt(VERSION_KEY),
                payload.getBoolean(VAMPIRE_KEY),
                payload.getBoolean(HUNTER_KEY),
                payload.getBoolean(WEREWOLF_KEY)
        ));
    }

    public static Optional<CompoundTag> readState(ItemStack stack) {
        CompoundTag payload = payload(stack);
        if (payload == null || !payload.contains(STATE_KEY, Tag.TAG_COMPOUND)) {
            return Optional.empty();
        }
        return Optional.of(payload.getCompound(STATE_KEY).copy());
    }

    private static CompoundTag payload(ItemStack stack) {
        CompoundTag root = customData(stack);
        if (!root.contains(PAYLOAD_KEY, Tag.TAG_COMPOUND)) {
            return null;
        }
        CompoundTag payload = root.getCompound(PAYLOAD_KEY);
        return payload.getInt(VERSION_KEY) > 0 ? payload : null;
    }

    private static CompoundTag customData(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return new CompoundTag();
        }
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    public record FactionSnapshot(int version, boolean vampire, boolean hunter, boolean werewolf) {
    }
}
