package com.guilh.mca_vampirism_compat.trade;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import de.teamlapen.vampirism.entity.villager.Trades;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

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
    private static final ResourceLocation HUMAN_HEART = ResourceLocation.fromNamespaceAndPath("vampirism", "human_heart");
    private static final ResourceLocation BLOOD_BOTTLE = ResourceLocation.fromNamespaceAndPath("vampirism", "blood_bottle");
    private static final int NATIVE_ROLL_BOUND = 3;

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

        return ensureForVampire(villager, villager.getOffers());
    }

    /**
     * Applies the roll to an offer list that MCA has just generated. Keeping
     * the list as an argument lets the {@code VillagerEntityMCA#getOffers}
     * hook call this method without recursively asking the villager for its
     * offers again.
     */
    public static boolean ensureForVampire(Villager villager, MerchantOffers offers) {
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
            return true;
        }
        if (data.getBoolean(ROLL_COMPLETED_KEY)) {
            return false;
        }

        data.putBoolean(ROLL_COMPLETED_KEY, true);
        if (!shouldAddForNativeRoll(villager.getRandom().nextInt(NATIVE_ROLL_BOUND))) {
            data.putBoolean(GENERATED_KEY, false);
            McaVampirismCompat.debugLog(
                    "[TRADE] Vampirism converted offer roll skipped villager={} reason=NATIVE_ONE_IN_THREE",
                    villager.getUUID()
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
                villager.getRandom().nextInt(Trades.converted_trades.length)
        ].getOffer(villager, villager.getRandom());
        if (specialOffer != null) {
            offers.add(specialOffer);
        }

        boolean added = offers.size() > offersBefore && containsConvertedVampirismOffer(offers);
        data.putBoolean(GENERATED_KEY, added);
        McaVampirismCompat.debugLog(
                "[TRADE] Vampirism converted offer roll villager={} added={} offersBefore={} offersAfter={}",
                villager.getUUID(),
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
