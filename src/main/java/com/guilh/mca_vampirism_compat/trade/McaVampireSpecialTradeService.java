package com.guilh.mca_vampirism_compat.trade;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import de.teamlapen.vampirism.entity.villager.Trades;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Brings Vampirism's converted-villager offer pool to MCA vampires without
 * replacing their entity or their MCA-generated trades.
 *
 * <p>Vampirism itself rolls one listing from {@link Trades#converted_trades}
 * with a one-in-three chance when its converted villager generates offers.
 * MCA villagers intentionally stay {@code VillagerEntityMCA}, so this service
 * mirrors that narrow offer-generation step after MCA has produced the live
 * offer list. The outcome is persisted to prevent a new roll every time a
 * player opens the merchant screen.</p>
 */
public final class McaVampireSpecialTradeService {
    private static final String ROLL_COMPLETED_KEY = "McaVampCompatVampireSpecialTradeRollCompleted";
    private static final String GENERATED_KEY = "McaVampCompatVampireSpecialTradeGenerated";
    private static final ResourceLocation HUMAN_HEART = new ResourceLocation("vampirism", "human_heart");
    private static final ResourceLocation BLOOD_BOTTLE = new ResourceLocation("vampirism", "blood_bottle");
    private static final int NATIVE_ROLL_BOUND = 3;
    /**
     * MCA's lazy {@code getOffers} hook normally owns the first-list roll.
     * Cycle Trades rebuilds that list deliberately, so defer the hook and run
     * one explicit post-rebuild roll instead. The state is thread-local because
     * merchant generation occurs synchronously on the server thread.
     */
    private static final ThreadLocal<Set<java.util.UUID>> DEFERRED_AUTOMATIC_ROLLS =
            ThreadLocal.withInitial(HashSet::new);

    private McaVampireSpecialTradeService() {
    }

    /**
     * Performs the native one-in-three converted-villager trade roll once for
     * the current generated offer list. This method is server-only.
     *
     * @return whether the live offer list contains a Vampirism converted-villager offer afterwards
     */
    public static boolean ensureForVampire(Villager villager) {
        if (villager == null || villager.level().isClientSide) {
            return false;
        }

        return ensureForVampire(villager, villager.getOffers(), villager.getRandom());
    }

    /**
     * Applies the roll to an offer list that MCA has just generated. Keeping
     * the list as an argument lets the shared {@code AbstractVillager#getOffers}
     * hook call this method without recursively asking the villager for its
     * offers again.
     */
    public static boolean ensureForVampire(Villager villager, MerchantOffers offers) {
        return ensureForVampire(villager, offers, villager == null ? null : villager.getRandom());
    }

    /**
     * Applies a single native-compatible roll using the supplied server random
     * source. Cycle Trades passes the active player's random source rather
     * than relying on an Easy Villagers snapshot entity's detached RNG.
     */
    static boolean ensureForVampire(Villager villager, MerchantOffers offers, RandomSource random) {
        if (villager == null || villager.level().isClientSide || offers == null) {
            return false;
        }

        if (offers.isEmpty()) {
            // Match Vampirism: an unemployed villager has no list to extend.
            // Do not consume the roll before MCA has generated professional offers.
            return false;
        }

        CompoundTag data = villager.getPersistentData();
        if (containsConvertedVampirismOffer(offers)) {
            data.putBoolean(ROLL_COMPLETED_KEY, true);
            // Repair older/current-list state that retained the offer but did
            // not retain its companion result marker. This makes the two
            // markers describe the same current offer list and lets the Cycle
            // Trades recovery test distinguish a stale native reroll from a
            // fresh compat reroll.
            data.putBoolean(GENERATED_KEY, true);
            return true;
        }
        if (data.getBoolean(ROLL_COMPLETED_KEY)) {
            return false;
        }

        if (random == null) {
            return false;
        }

        data.putBoolean(ROLL_COMPLETED_KEY, true);
        int nativeRoll = random.nextInt(NATIVE_ROLL_BOUND);
        if (!shouldAddForNativeRoll(nativeRoll)) {
            data.putBoolean(GENERATED_KEY, false);
            McaVampirismCompat.debugLog(
                    "[TRADE] Vampirism converted offer roll skipped villager={} roll={} reason=NATIVE_ONE_IN_THREE",
                    villager.getUUID(),
                    nativeRoll
            );
            return false;
        }

        if (Trades.converted_trades.length == 0) {
            data.putBoolean(GENERATED_KEY, false);
            return false;
        }

        // ItemListing#getOffer is the public vanilla API behind the protected
        // AbstractVillager helper used by Vampirism. Calling it directly keeps
        // the same random selection without a brittle inherited-method mixin.
        int offersBefore = offers.size();
        MerchantOffer specialOffer = Trades.converted_trades[
                random.nextInt(Trades.converted_trades.length)
        ].getOffer(villager, random);
        if (specialOffer != null) {
            offers.add(specialOffer);
        }

        boolean added = offers.size() > offersBefore && containsConvertedVampirismOffer(offers);
        data.putBoolean(GENERATED_KEY, added);
        McaVampirismCompat.debugLog(
                "[TRADE] Vampirism converted offer roll villager={} roll={} added={} offersBefore={} offersAfter={}",
                villager.getUUID(),
                nativeRoll,
                added,
                offersBefore,
                offers.size()
        );
        return added;
    }

    /** Clears the one-roll marker immediately before the compat rebuilds offers. */
    public static void resetForOfferReroll(Villager villager) {
        if (villager == null || villager.level().isClientSide) {
            return;
        }
        CompoundTag data = villager.getPersistentData();
        data.remove(ROLL_COMPLETED_KEY);
        data.remove(GENERATED_KEY);
    }

    /**
     * Rebuilds one professional offer list and rolls the converted-villager
     * offer exactly once for that new list.  This is shared by normal MCA
     * merchant menus and Easy Villagers' stored Trader bridge.
     *
     * <p>The supplier must clear and rebuild the real merchant offer list. A
     * failed roll is still a completed native one-in-three attempt, whereas a
     * later Cycle Trades action starts a fresh attempt.</p>
     */
    public static MerchantOffers rebuildForCycle(
            Villager villager,
            Supplier<MerchantOffers> offerRebuilder,
            RandomSource rollRandom,
            String source
    ) {
        if (villager == null || offerRebuilder == null) {
            return null;
        }

        resetForOfferReroll(villager);
        MerchantOffers offers;
        deferAutomaticRoll(villager);
        try {
            offers = offerRebuilder.get();
        } finally {
            resumeAutomaticRoll(villager);
        }

        boolean added = ensureForVampire(villager, offers, rollRandom);
        boolean completed = villager.getPersistentData().getBoolean(ROLL_COMPLETED_KEY);
        boolean generated = villager.getPersistentData().getBoolean(GENERATED_KEY);
        McaVampirismCompat.debugLog(
                "[TRADE] Vampirism Cycle Trades reroll source={} villager={} rebuiltOffers={} rollCompleted={} added={} generated={}",
                source,
                villager.getUUID(),
                offers == null ? -1 : offers.size(),
                completed,
                added,
                generated
        );
        return offers;
    }

    /**
     * Restores a Trader's ordinary offers only when its restored vanilla
     * {@link Villager} cannot regenerate MCA's serialized professional list.
     * Easy Villagers represents a stored MCA resident as a vanilla villager,
     * so clearing that temporary entity's list can legitimately produce an
     * empty list even though the Trader has valid persisted offers. The old
     * converted-villager offer is deliberately excluded: it belongs solely to
     * the preceding list and the current list must receive its own roll.
     *
     * @return whether at least one ordinary offer was restored
     */
    public static boolean restoreOrdinaryOffersIfEmpty(
            MerchantOffers rebuiltOffers,
            MerchantOffers previousOffers
    ) {
        if (rebuiltOffers == null || !rebuiltOffers.isEmpty()
                || previousOffers == null || previousOffers.isEmpty()) {
            return false;
        }

        int restored = 0;
        for (MerchantOffer offer : previousOffers) {
            if (offer != null && !isConvertedVampirismOffer(offer)) {
                rebuiltOffers.add(offer);
                restored++;
            }
        }
        return restored > 0;
    }

    /** True when a dedicated Cycle Trades rebuild owns the current offer roll. */
    public static boolean isAutomaticRollDeferred(Villager villager) {
        return villager != null && DEFERRED_AUTOMATIC_ROLLS.get().contains(villager.getUUID());
    }

    /**
     * Removes only a special offer previously created by this service when an
     * MCA vampire is cured or its Vampire trait is explicitly removed.
     */
    public static void removeForNoLongerVampire(Villager villager) {
        if (villager == null || villager.level().isClientSide) {
            return;
        }
        CompoundTag data = villager.getPersistentData();
        if (data.getBoolean(GENERATED_KEY)) {
            villager.getOffers().removeIf(McaVampireSpecialTradeService::isConvertedVampirismOffer);
        }
        data.remove(ROLL_COMPLETED_KEY);
        data.remove(GENERATED_KEY);
    }

    static boolean shouldAddForNativeRoll(int nativeRoll) {
        return nativeRoll == 0;
    }

    static boolean containsConvertedVampirismOffer(MerchantOffers offers) {
        return offers != null && offers.stream().anyMatch(McaVampireSpecialTradeService::isConvertedVampirismOffer);
    }

    /** Exposes whether the current generated list has consumed its special-offer roll. */
    public static boolean isRollCompleted(Villager villager) {
        return villager != null && villager.getPersistentData().getBoolean(ROLL_COMPLETED_KEY);
    }

    /** Exposes the persisted result of the most recent current-list special-offer roll. */
    public static boolean wasGenerated(Villager villager) {
        return villager != null && villager.getPersistentData().getBoolean(GENERATED_KEY);
    }

    private static void deferAutomaticRoll(Villager villager) {
        DEFERRED_AUTOMATIC_ROLLS.get().add(villager.getUUID());
    }

    private static void resumeAutomaticRoll(Villager villager) {
        Set<java.util.UUID> deferred = DEFERRED_AUTOMATIC_ROLLS.get();
        deferred.remove(villager.getUUID());
        if (deferred.isEmpty()) {
            DEFERRED_AUTOMATIC_ROLLS.remove();
        }
    }

    private static boolean isConvertedVampirismOffer(MerchantOffer offer) {
        if (offer == null) {
            return false;
        }
        return isConvertedTradeItem(offer.getBaseCostA()) || isConvertedTradeItem(offer.getResult());
    }

    private static boolean isConvertedTradeItem(ItemStack stack) {
        return stack != null
                && !stack.isEmpty()
                && isConvertedTradeItem(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    private static boolean isConvertedTradeItem(ResourceLocation itemId) {
        return HUMAN_HEART.equals(itemId) || BLOOD_BOTTLE.equals(itemId);
    }
}
