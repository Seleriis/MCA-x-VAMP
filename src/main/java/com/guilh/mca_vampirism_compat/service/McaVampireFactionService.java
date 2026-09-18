package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.faction.PlayerFaction;
import com.guilh.mca_vampirism_compat.faction.PlayerFactionContext;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import de.teamlapen.vampirism.api.VampirismAPI;
import de.teamlapen.vampirism.api.entity.factions.IFaction;
import de.teamlapen.vampirism.util.Helper;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class McaVampireFactionService {
    private static final String VAMPIRISM_VAMPIRE_FACTION_ID = "vampirism:vampire";
    private static final String VAMPIRISM_HUNTER_FACTION_ID = "vampirism:hunter";
    private static final String WEREWOLVES_FACTION_ID = "werewolves:werewolf";
    private static final String WEREWOLVES_HELPER = "de.teamlapen.werewolves.util.Helper";
    private static final String WEREWOLVES_FORM_HELPER = "de.teamlapen.werewolves.util.FormHelper";
    private static boolean werewolvesIsWerewolfMethodResolved;
    private static Method werewolvesIsWerewolfMethod;
    private static boolean werewolvesGetFormMethodResolved;
    private static Method werewolvesGetFormMethod;
    private static final Map<Class<?>, Method> WEREWOLVES_IS_TRANSFORMED_METHODS = new ConcurrentHashMap<>();

    private McaVampireFactionService() {
    }

    public static PlayerFaction classifyPlayer(Player player) {
        return classifyPlayerDetailed(player).faction();
    }

    public static PlayerFaction classifyEffectivePlayer(Player player) {
        return classifyPlayerContext(player).effectiveFaction();
    }

    public static PlayerFaction tradeFaction(Player player) {
        PlayerFactionContext context = classifyPlayerContext(player);
        if (context.vampireDisguised() && !McaVampirismCompatConfig.vampireDisguiseSuppressesVampireTradeDiscount()) {
            return context.realFaction();
        }
        return context.effectiveFaction();
    }

    public static PlayerFactionContext classifyPlayerContext(Player player) {
        ClassificationResult real = classifyPlayerDetailed(player);
        boolean disguised = player != null
                && real.faction() == PlayerFaction.VAMPIRE
                && McaVampirismCompatConfig.respectVampireDisguiseForMcaInteractions()
                && VampireSkillIntegrationService.isVampireDisguised(player);
        PlayerFaction effective = disguised ? PlayerFaction.HUMAN_DISGUISED : real.faction();
        if (disguised) {
            McaVampirismCompat.debugLog(
                    "[DISGUISE] player={} vampire=true disguised=true effectiveFaction={}",
                    player.getName().getString(),
                    effective
            );
        }
        return new PlayerFactionContext(real.faction(), effective, disguised, real.werewolf(), real.transformedWerewolf(), real.source());
    }

    private static ClassificationResult classifyPlayerDetailed(Player player) {
        if (player == null) {
            return new ClassificationResult(PlayerFaction.HUMAN, false, "NULL_PLAYER");
        }
        try {
            String currentFactionId = getCurrentFactionId(player);
            ClassificationResult classification = classifyByCurrentFaction(player, currentFactionId);
            if (classification == null) {
                classification = fallbackClassifyPlayer(player);
            }
            if (classification == null) {
                classification = new ClassificationResult(PlayerFaction.HUMAN, false, "NULL_SAFE_FALLBACK");
            }
            return classification;
        } catch (RuntimeException | LinkageError exception) {
            McaVampirismCompat.debugLog(
                    "[FACTION] classify failed safely player={} error={}",
                    player.getName().getString(),
                    exception.toString()
            );
            return new ClassificationResult(PlayerFaction.HUMAN, false, "CLASSIFY_ERROR");
        }
    }

    public static void debugClassification(Player player, PlayerFaction faction, String context) {
        if (player == null) {
            return;
        }
        try {
            String currentFactionId = getCurrentFactionId(player);
            boolean transformed = faction == PlayerFaction.TRANSFORMED_WEREWOLF
                    || (WEREWOLVES_FACTION_ID.equals(currentFactionId) && isTransformedWerewolf(player));
            McaVampirismCompat.debugLog(
                    "[FACTION] context={} player={} currentFaction={} transformed={} result={} effective={}",
                    context,
                    player.getName().getString(),
                    currentFactionId,
                    transformed,
                    faction,
                    classifyPlayerContext(player).effectiveFaction()
            );
        } catch (RuntimeException | LinkageError exception) {
            McaVampirismCompat.debugLog(
                    "[FACTION] debug classification failed safely player={} error={}",
                    player.getName().getString(),
                    exception.toString()
            );
        }
    }

    public static boolean isHunter(Entity entity) {
        try {
            if (McaHunterAlignmentService.isMcaHunterAligned(entity)) {
                return true;
            }
            if (entity instanceof Player player) {
                return classifyPlayer(player) == PlayerFaction.HUNTER;
            }
            return entity instanceof LivingEntity livingEntity && Helper.isHunter(livingEntity);
        } catch (RuntimeException | LinkageError exception) {
            McaVampirismCompat.debugLog(
                    "[FACTION] isHunter failed safely entity={} error={}",
                    entity == null ? "null" : entity.getType().toString(),
                    exception.toString()
            );
            return false;
        }
    }

    public static boolean isVampire(Entity entity) {
        try {
            if (McaVampireStateService.isVampire(entity)) {
                return true;
            }
            if (entity instanceof Player player) {
                return classifyPlayer(player) == PlayerFaction.VAMPIRE;
            }
            return entity instanceof LivingEntity livingEntity && Helper.isVampire(livingEntity);
        } catch (RuntimeException | LinkageError exception) {
            McaVampirismCompat.debugLog(
                    "[FACTION] isVampire failed safely entity={} error={}",
                    entity == null ? "null" : entity.getType().toString(),
                    exception.toString()
            );
            return false;
        }
    }

    public static boolean isWerewolf(Entity entity) {
        if (entity == null) {
            return false;
        }
        if (McaWerewolfStateService.isWerewolf(entity)) {
            return true;
        }
        try {
            Method method = werewolvesIsWerewolfMethod();
            if (method == null) {
                return false;
            }
            Object result = method.invoke(null, entity);
            return result instanceof Boolean value && value;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            return false;
        }
    }

    public static boolean isTransformedWerewolf(Entity entity) {
        if (!(entity instanceof LivingEntity livingEntity) || !isWerewolf(entity)) {
            return false;
        }
        try {
            Method getForm = werewolvesGetFormMethod();
            if (getForm == null) {
                return false;
            }
            Object form = getForm.invoke(null, livingEntity);
            if (form == null) {
                return false;
            }
            Method isTransformed = werewolvesIsTransformedMethod(form.getClass());
            Object result = isTransformed.invoke(form);
            return result instanceof Boolean value && value;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            return false;
        }
    }

    private static Method werewolvesIsWerewolfMethod() {
        if (werewolvesIsWerewolfMethodResolved) {
            return werewolvesIsWerewolfMethod;
        }
        werewolvesIsWerewolfMethodResolved = true;
        try {
            Class<?> helperClass = Class.forName(WEREWOLVES_HELPER);
            werewolvesIsWerewolfMethod = helperClass.getMethod("isWerewolf", Entity.class);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            werewolvesIsWerewolfMethod = null;
        }
        return werewolvesIsWerewolfMethod;
    }

    private static Method werewolvesGetFormMethod() {
        if (werewolvesGetFormMethodResolved) {
            return werewolvesGetFormMethod;
        }
        werewolvesGetFormMethodResolved = true;
        try {
            Class<?> formHelperClass = Class.forName(WEREWOLVES_FORM_HELPER);
            werewolvesGetFormMethod = formHelperClass.getMethod("getForm", LivingEntity.class);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            werewolvesGetFormMethod = null;
        }
        return werewolvesGetFormMethod;
    }

    private static Method werewolvesIsTransformedMethod(Class<?> formClass) throws NoSuchMethodException {
        Method cached = WEREWOLVES_IS_TRANSFORMED_METHODS.get(formClass);
        if (cached != null) {
            return cached;
        }
        Method method = formClass.getMethod("isTransformed");
        WEREWOLVES_IS_TRANSFORMED_METHODS.put(formClass, method);
        return method;
    }

    public static boolean isVampireAlly(Player player, VillagerEntityMCA villager) {
        PlayerFactionContext context = classifyPlayerContext(player);
        PlayerFaction effectiveFaction = context.vampireDisguised()
                && !McaVampirismCompatConfig.vampireDisguiseSuppressesVampireTradeDiscount()
                ? context.realFaction()
                : context.effectiveFaction();
        return McaVampireStateService.isVampire(villager)
                && McaVampirismCompatConfig.vampireVillagersTreatVampiresAsAllies()
                && effectiveFaction == PlayerFaction.VAMPIRE;
    }

    public static boolean isTradeBlocked(Player player, VillagerEntityMCA villager) {
        if (!McaVampireStateService.isVampire(villager)) {
            return false;
        }
        if (McaVampirismCompatConfig.vampireVillagersTradeOnlyWithVampires() && !isVampireAlly(player, villager)) {
            return true;
        }
        PlayerFactionContext context = classifyPlayerContext(player);
        PlayerFaction faction = tradeFaction(player);
        boolean blocked;
        if (faction == PlayerFaction.HUNTER) {
            blocked = McaVampirismCompatConfig.blockHunterTrading();
        } else {
            blocked = faction == PlayerFaction.TRANSFORMED_WEREWOLF
                    && McaVampirismCompatConfig.blockWerewolfTrading();
        }
        McaVampirismCompat.debugLog(
                "[TRADE] decision villagerType=VAMPIRE playerWerewolfTransformed={} blocked={} reason={}",
                context.transformedWerewolf(),
                blocked,
                faction == PlayerFaction.TRANSFORMED_WEREWOLF ? "TRANSFORMED_WEREWOLF" : faction
        );
        return blocked;
    }

    public static double getTradeMultiplier(Player player, VillagerEntityMCA villager) {
        if (!McaVampireStateService.isVampire(villager)) {
            return 1.0D;
        }
        PlayerFactionContext context = classifyPlayerContext(player);
        PlayerFaction tradeFaction = tradeFaction(player);
        if (context.vampireDisguised() && tradeFaction == PlayerFaction.HUMAN_DISGUISED) {
            McaVampirismCompat.debugLog("[TRADE] disguise active, vampire discount suppressed");
        }
        return switch (tradeFaction) {
            case VAMPIRE -> Math.max(0.05D, 1.0D - McaVampirismCompatConfig.vampirePlayerTradeDiscount());
            case HUNTER -> McaVampirismCompatConfig.hunterTradePriceMultiplier();
            case TRANSFORMED_WEREWOLF -> McaVampirismCompatConfig.werewolfTradePriceMultiplier();
            case WEREWOLF, HUMAN_DISGUISED, HUMAN, UNKNOWN -> McaVampirismCompatConfig.humanTradePriceMultiplier();
        };
    }

    public static float socialPenalty(PlayerFaction faction) {
        return switch (faction) {
            case VAMPIRE -> 0.0F;
            case HUMAN_DISGUISED, HUMAN, WEREWOLF, UNKNOWN -> (float) McaVampirismCompatConfig.humanSocialPenalty();
            case TRANSFORMED_WEREWOLF -> (float) McaVampirismCompatConfig.werewolfSocialPenalty();
            case HUNTER -> (float) McaVampirismCompatConfig.hunterSocialPenalty();
        };
    }

    public static int targetPriority(Player player) {
        return switch (classifyPlayerContext(player).effectiveFaction()) {
            case HUNTER -> 0;
            case TRANSFORMED_WEREWOLF -> 1;
            case WEREWOLF -> 100;
            case HUMAN_DISGUISED, HUMAN -> 2;
            case VAMPIRE, UNKNOWN -> 100;
        };
    }

    private static String getCurrentFactionId(Player player) {
        var handler = VampirismAPI.getFactionPlayerHandler(player).orElse(null);
        if (handler == null) {
            return null;
        }
        IFaction<?> currentFaction = handler.getCurrentFaction();
        return currentFaction == null ? null : describeFaction(currentFaction);
    }

    private static ClassificationResult classifyByCurrentFaction(Player player, String currentFactionId) {
        if (currentFactionId == null) {
            return null;
        }
        if (VAMPIRISM_VAMPIRE_FACTION_ID.equals(currentFactionId)) {
            return new ClassificationResult(PlayerFaction.VAMPIRE, false, "CURRENT_FACTION");
        }
        if (VAMPIRISM_HUNTER_FACTION_ID.equals(currentFactionId)) {
            return new ClassificationResult(PlayerFaction.HUNTER, false, "CURRENT_FACTION");
        }
        if (WEREWOLVES_FACTION_ID.equals(currentFactionId)) {
            boolean transformed = isTransformedWerewolf(player);
            return new ClassificationResult(
                    transformed ? PlayerFaction.TRANSFORMED_WEREWOLF : PlayerFaction.WEREWOLF,
                    true,
                    transformed,
                    "CURRENT_FACTION_PLUS_WEREWOLF_FORM"
            );
        }
        // Addons can expose their own current faction. That is not proof that
        // a player is human: Vampirism's helper and capability checks still
        // know whether the player is a vampire or hunter.
        return null;
    }

    private static ClassificationResult fallbackClassifyPlayer(Player player) {
        if (safeIsVampireHelper(player)) {
            return new ClassificationResult(PlayerFaction.VAMPIRE, false, "FALLBACK_HELPER");
        }
        if (safeIsHunterHelper(player)) {
            return new ClassificationResult(PlayerFaction.HUNTER, false, "FALLBACK_HELPER");
        }
        if (safeHasVampireLevel(player)) {
            return new ClassificationResult(PlayerFaction.VAMPIRE, false, "FALLBACK_ACTIVE_CAPABILITY");
        }
        if (safeHasHunterLevel(player)) {
            return new ClassificationResult(PlayerFaction.HUNTER, false, "FALLBACK_ACTIVE_CAPABILITY");
        }
        boolean transformed = isTransformedWerewolf(player);
        if (transformed || isWerewolf(player)) {
            return new ClassificationResult(
                    transformed ? PlayerFaction.TRANSFORMED_WEREWOLF : PlayerFaction.WEREWOLF,
                    true,
                    transformed,
                    transformed ? "FALLBACK_WEREWOLF_FORM" : "FALLBACK_WEREWOLF_HELPER"
            );
        }
        return new ClassificationResult(PlayerFaction.HUMAN, false, "NO_CURRENT_FACTION");
    }

    private static boolean safeIsVampireHelper(LivingEntity entity) {
        try {
            return Helper.isVampire(entity);
        } catch (RuntimeException | LinkageError exception) {
            McaVampirismCompat.debugLog(
                    "[FACTION] Helper.isVampire failed safely entity={} error={}",
                    entity == null ? "null" : entity.getType().toString(),
                    exception.toString()
            );
            return false;
        }
    }

    private static boolean safeIsHunterHelper(LivingEntity entity) {
        try {
            return Helper.isHunter(entity);
        } catch (RuntimeException | LinkageError exception) {
            McaVampirismCompat.debugLog(
                    "[FACTION] Helper.isHunter failed safely entity={} error={}",
                    entity == null ? "null" : entity.getType().toString(),
                    exception.toString()
            );
            return false;
        }
    }

    private static boolean safeHasVampireLevel(Player player) {
        try {
            return VampirismAPI.getVampirePlayer(player)
                    .map(vampire -> vampire.getLevel() > 0)
                    .orElse(false);
        } catch (RuntimeException | LinkageError exception) {
            McaVampirismCompat.debugLog(
                    "[FACTION] vampire capability failed safely player={} error={}",
                    player.getName().getString(),
                    exception.toString()
            );
            return false;
        }
    }

    private static boolean safeHasHunterLevel(Player player) {
        try {
            return VampirismAPI.getHunterPlayer(player)
                    .map(hunter -> hunter.getLevel() > 0)
                    .orElse(false);
        } catch (RuntimeException | LinkageError exception) {
            McaVampirismCompat.debugLog(
                    "[FACTION] hunter capability failed safely player={} error={}",
                    player.getName().getString(),
                    exception.toString()
            );
            return false;
        }
    }

    private static String describeFaction(IFaction<?> faction) {
        if (faction == null) {
            return "none";
        }
        try {
            return String.valueOf(faction.getID());
        } catch (RuntimeException ignored) {
            return faction.toString();
        }
    }

    private record ClassificationResult(PlayerFaction faction, boolean werewolf, boolean transformedWerewolf, String source) {
        private ClassificationResult(PlayerFaction faction, boolean transformedWerewolf, String source) {
            this(faction, faction == PlayerFaction.WEREWOLF || faction == PlayerFaction.TRANSFORMED_WEREWOLF,
                    transformedWerewolf, source);
        }
    }
}
