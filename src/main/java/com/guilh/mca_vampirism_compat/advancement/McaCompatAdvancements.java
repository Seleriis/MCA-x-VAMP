package com.guilh.mca_vampirism_compat.advancement;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import net.minecraft.resources.ResourceLocation;

/** Stable ids for player-facing compat progress. */
public final class McaCompatAdvancements {
    public static final String CRITERION = "completed";

    public static final ResourceLocation VAMPIRE_INFECTION = id("vampire_infection");
    public static final ResourceLocation VAMPIRE_CONVERSION = id("vampire_conversion");
    public static final ResourceLocation VAMPIRE_TRADE = id("vampire_trade_discount");
    public static final ResourceLocation HUNTER_BLOOD = id("hunter_blood");
    public static final ResourceLocation VAMPIRE_SPECIALIST = id("vampire_specialist");
    public static final ResourceLocation HUNTER_TRADE = id("hunter_trade_discount");
    public static final ResourceLocation HUNTER_SPECIALIST = id("hunter_specialist");
    public static final ResourceLocation WEREWOLF_INFECTION = id("werewolf_infection");
    public static final ResourceLocation WEREWOLF_CONVERSION = id("werewolf_conversion");
    public static final ResourceLocation WEREWOLF_TRADE = id("werewolf_trade_discount");
    public static final ResourceLocation WEREWOLF_SPECIALIST = id("werewolf_specialist");
    public static final ResourceLocation EARLY_INFECTION_TREATMENT = id("treat_early_infection");
    public static final ResourceLocation COMPLETE_CURE = id("complete_cure");
    public static final ResourceLocation INHERITED_BABY = id("inherited_baby");
    public static final ResourceLocation FACTION_PICKUP = id("faction_safe_carrying");
    public static final ResourceLocation VAMPIRE_VILLAGE_FAVOR = id("vampire_village_favor");
    public static final ResourceLocation HUNTER_VILLAGE_FAVOR = id("hunter_village_favor");
    public static final ResourceLocation WEREWOLF_VILLAGE_FAVOR = id("werewolf_village_favor");
    public static final ResourceLocation URGENT_HUNTER_AID = id("urgent_hunter_aid");

    private McaCompatAdvancements() {
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(McaVampirismCompat.MOD_ID, path);
    }
}
