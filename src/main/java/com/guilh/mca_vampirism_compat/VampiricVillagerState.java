package com.guilh.mca_vampirism_compat;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.Optional;
import java.util.UUID;

public class VampiricVillagerState implements INBTSerializable<CompoundTag> {
    private boolean infected;
    private boolean converted;
    private boolean sanguinareActive;
    private long infectionStartTick = -1L;
    private long lastBiteTick = Long.MIN_VALUE;
    private long lastGrowthTick = -1L;
    private long lastBittenByMcaVampireTick = Long.MIN_VALUE;
    private long lastVampirePlayerBiteTick = Long.MIN_VALUE;
    private long lastAggressedByTick = Long.MIN_VALUE;
    private long retaliationUntilTick = Long.MIN_VALUE;
    private long defensiveModeUntilTick = Long.MIN_VALUE;
    private long lastVampireThreatTick = Long.MIN_VALUE;
    private int vampireThreatCount;
    private boolean aiGoalsAdded;
    private boolean defensiveGoalsAdded;
    private boolean hunterAiGoalsAdded;
    private boolean werewolfAiGoalsAdded;
    private boolean defensiveMode;
    private boolean originalMainHandItemSaved;
    private boolean defensiveWeaponEquippedLogged;
    private boolean hunterTraitApplied;
    private boolean hunterAligned;
    private boolean hunterVillageApplied;
    private boolean werewolfConverted;
    private boolean werewolfInfected;
    private boolean werewolfAppearanceApplied;
    private boolean werewolfTraitApplied;
    private boolean werewolfPartialForm;
    private boolean factionInheritanceProcessed;
    private UUID source;
    private UUID lastAggressorUuid;
    private UUID lastVampirePlayerBiteUuid;
    private UUID defensiveTargetUuid;
    private long hunterAlignmentUntilTick = Long.MIN_VALUE;
    private boolean originalAppearanceSaved;
    private boolean vampireAppearanceApplied;
    private boolean vampireEyeIndexAssigned;
    private boolean vampireNightScheduleApplied;
    private boolean wasBittenByVampirePlayer;
    private boolean biteWasConversionCause;
    private boolean curingVampire;
    private boolean curingWerewolf;
    private boolean cureHadWeakness;
    private boolean cureHadGoldenApple;
    private boolean cureHadGarlicInjection;
    private boolean vampireCureHadWeakness;
    private boolean vampireCureHadGoldenApple;
    private boolean vampireCureHadGarlicInjection;
    private boolean werewolfCureHadWeakness;
    private boolean werewolfCureHadGoldenApple;
    private boolean werewolfCureHadGarlicInjection;
    private int vampireEyeIndex = -1;
    private int cureStage;
    private float originalSkinGene;
    private float originalMelaninGene;
    private float originalHemoglobinGene;
    private float originalFaceGene;
    private float originalEyeBrightnessGene;
    private int originalEyeDye = -1;
    private int originalEyeLeftDye = -1;
    private int originalSkinDye = 0xFF000000;
    private int appearanceSnapshotVersion;
    private long cureStartedTick = -1L;
    private long cureFinishTick = -1L;
    private long werewolfCureStartedTick = -1L;
    private long werewolfCureFinishTick = -1L;
    private long cureIngredientExpireTick = Long.MIN_VALUE;
    private long vampireCureIngredientExpireTick = Long.MIN_VALUE;
    private long werewolfCureIngredientExpireTick = Long.MIN_VALUE;
    private long werewolfInfectionStartTick = -1L;
    private long werewolfInfectionFinishTick = -1L;
    private long werewolfLastTransformationTick = Long.MIN_VALUE;
    private long werewolfBiteCooldownUntilTick = Long.MIN_VALUE;
    private long lastCureCompletedTick = Long.MIN_VALUE;
    private String originalCustomSkin = "";
    private String originalMcaSchedule = "";
    private String lastAppliedVampireSchedule = "";
    private String defensiveReason = "";
    private String lastCureType = "";
    private String hunterWeaponSelection = "";
    // Optional migration marker. Older saves intentionally omit it, which
    // means their saved item belongs to the historical vanilla main hand.
    private String hunterWeaponHand = "";
    private ItemStack originalMainHandItem = ItemStack.EMPTY;
    private UUID cureInitiatorUuid;
    private UUID werewolfCureInitiatorUuid;
    private UUID werewolfSourceUuid;
    private transient boolean movementSpeedModifierApplied;
    private transient String movementSpeedModifierType = "";
    private transient double movementSpeedModifierMultiplier = Double.NaN;

    public boolean isInfected() {
        return infected;
    }

    public void setInfected(boolean infected) {
        this.infected = infected;
    }

    public boolean isConverted() {
        return converted;
    }

    public void setConverted(boolean converted) {
        this.converted = converted;
    }

    public boolean wasSanguinareActive() {
        return sanguinareActive;
    }

    public void setSanguinareActive(boolean sanguinareActive) {
        this.sanguinareActive = sanguinareActive;
    }

    public boolean isMovementSpeedModifierApplied(String type, double multiplier) {
        return movementSpeedModifierApplied
                && movementSpeedModifierType.equals(type)
                && Double.compare(movementSpeedModifierMultiplier, multiplier) == 0;
    }

    public void markMovementSpeedModifierApplied(String type, double multiplier) {
        this.movementSpeedModifierApplied = true;
        this.movementSpeedModifierType = type == null ? "" : type;
        this.movementSpeedModifierMultiplier = multiplier;
    }

    public void clearMovementSpeedModifierApplied() {
        this.movementSpeedModifierApplied = false;
        this.movementSpeedModifierType = "";
        this.movementSpeedModifierMultiplier = Double.NaN;
    }

    public long getInfectionStartTick() {
        return infectionStartTick;
    }

    public void setInfectionStartTick(long infectionStartTick) {
        this.infectionStartTick = infectionStartTick;
    }

    public Optional<UUID> getSource() {
        return Optional.ofNullable(source);
    }

    public void setSource(UUID source) {
        this.source = source;
    }

    public void startInfection(long gameTime, UUID source) {
        this.infected = true;
        this.converted = false;
        this.sanguinareActive = true;
        this.infectionStartTick = gameTime;
        this.source = source;
    }

    public void finishConversion() {
        this.infected = false;
        this.converted = true;
        this.sanguinareActive = false;
    }

    public void markInheritedVampire() {
        this.infected = false;
        this.converted = true;
        this.sanguinareActive = false;
        this.infectionStartTick = -1L;
        this.source = null;
    }

    public void markInheritedWerewolf(long gameTime) {
        clearPendingInfection();
        finishWerewolfConversion(gameTime);
        this.werewolfAppearanceApplied = true;
        this.werewolfTraitApplied = true;
        this.werewolfPartialForm = true;
    }

    public boolean isFactionInheritanceProcessed() {
        return factionInheritanceProcessed;
    }

    public void markFactionInheritanceProcessed() {
        this.factionInheritanceProcessed = true;
    }

    public void clearInfection() {
        this.infected = false;
        this.sanguinareActive = false;
    }

    public void clearPendingInfection() {
        this.infected = false;
        this.sanguinareActive = false;
        this.infectionStartTick = -1L;
        this.source = null;
        this.lastBittenByMcaVampireTick = Long.MIN_VALUE;
        this.lastVampirePlayerBiteTick = Long.MIN_VALUE;
        this.lastVampirePlayerBiteUuid = null;
        this.wasBittenByVampirePlayer = false;
        this.biteWasConversionCause = false;
    }

    public void clearVampirismAfterCure() {
        clearPendingInfection();
        this.converted = false;
        this.vampireAppearanceApplied = false;
        this.curingVampire = false;
        this.cureStartedTick = -1L;
        this.cureFinishTick = -1L;
        this.cureStage = 0;
        this.cureInitiatorUuid = null;
        clearCureIngredientProgress(false);
    }

    public void markCureCompleted(long gameTime, String cureType) {
        this.lastCureCompletedTick = gameTime;
        this.lastCureType = cureType == null ? "" : cureType;
    }

    public boolean wasCureRecentlyCompleted(long gameTime, String cureType, long windowTicks) {
        if (lastCureCompletedTick == Long.MIN_VALUE) {
            return false;
        }
        if (cureType != null && !cureType.equals(lastCureType)) {
            return false;
        }
        return gameTime - lastCureCompletedTick >= 0L && gameTime - lastCureCompletedTick <= Math.max(1L, windowTicks);
    }

    public void keepConsistent(boolean hasSanguinare) {
        if (converted) {
            infected = false;
            sanguinareActive = false;
            return;
        }

        if (!hasSanguinare) {
            clearInfection();
        }
    }

    public boolean canBite(long gameTime, long cooldownTicks) {
        return lastBiteTick == Long.MIN_VALUE || gameTime - lastBiteTick >= cooldownTicks;
    }

    public void markBite(long gameTime) {
        this.lastBiteTick = gameTime;
    }

    public boolean wasRecentlyBitten(long gameTime, long cooldownTicks) {
        return lastBittenByMcaVampireTick != Long.MIN_VALUE && gameTime - lastBittenByMcaVampireTick < cooldownTicks;
    }

    public void markBittenByMcaVampire(long gameTime) {
        this.lastBittenByMcaVampireTick = gameTime;
    }

    public void markVampirePlayerConversionBite(UUID playerUuid, long gameTime) {
        this.lastVampirePlayerBiteUuid = playerUuid;
        this.lastVampirePlayerBiteTick = gameTime;
        this.wasBittenByVampirePlayer = true;
        this.biteWasConversionCause = true;
    }

    public Optional<UUID> getLastVampirePlayerBiteUuid() {
        return Optional.ofNullable(lastVampirePlayerBiteUuid);
    }

    public long getLastVampirePlayerBiteTick() {
        return lastVampirePlayerBiteTick;
    }

    public boolean wasBittenByVampirePlayer() {
        return wasBittenByVampirePlayer;
    }

    public boolean isBiteWasConversionCause() {
        return biteWasConversionCause;
    }

    public void markRetaliation(UUID aggressorUuid, long gameTime, long memoryTicks) {
        this.lastAggressorUuid = aggressorUuid;
        this.lastAggressedByTick = gameTime;
        this.retaliationUntilTick = gameTime + Math.max(0L, memoryTicks);
    }

    public boolean isRetaliatingAgainst(UUID aggressorUuid, long gameTime) {
        return aggressorUuid != null
                && lastAggressorUuid != null
                && lastAggressorUuid.equals(aggressorUuid)
                && gameTime <= retaliationUntilTick;
    }

    public boolean isRetaliating(long gameTime) {
        return lastAggressorUuid != null && gameTime <= retaliationUntilTick;
    }

    public void clearRetaliationIfExpired(long gameTime) {
        if (lastAggressorUuid != null && gameTime > retaliationUntilTick) {
            clearRetaliation();
        }
    }

    public void clearRetaliation() {
        lastAggressorUuid = null;
        lastAggressedByTick = Long.MIN_VALUE;
        retaliationUntilTick = Long.MIN_VALUE;
    }

    public boolean canProcessGrowth(long gameTime, long intervalTicks) {
        return lastGrowthTick < 0L || gameTime - lastGrowthTick >= intervalTicks;
    }

    public void markGrowthTick(long gameTime) {
        this.lastGrowthTick = gameTime;
    }

    public boolean areAiGoalsAdded() {
        return aiGoalsAdded;
    }

    public void markAiGoalsAdded() {
        this.aiGoalsAdded = true;
    }

    public boolean areDefensiveGoalsAdded() {
        return defensiveGoalsAdded;
    }

    public void markDefensiveGoalsAdded() {
        this.defensiveGoalsAdded = true;
    }

    public boolean areHunterAiGoalsAdded() {
        return hunterAiGoalsAdded;
    }

    public void markHunterAiGoalsAdded() {
        this.hunterAiGoalsAdded = true;
    }

    public boolean areWerewolfAiGoalsAdded() {
        return werewolfAiGoalsAdded;
    }

    public void markWerewolfAiGoalsAdded() {
        this.werewolfAiGoalsAdded = true;
    }

    public boolean isDefensiveMode() {
        return defensiveMode;
    }

    public long getDefensiveModeUntilTick() {
        return defensiveModeUntilTick;
    }

    public String getDefensiveReason() {
        return defensiveReason;
    }

    public Optional<UUID> getDefensiveTargetUuid() {
        return Optional.ofNullable(defensiveTargetUuid);
    }

    public long getLastVampireThreatTick() {
        return lastVampireThreatTick;
    }

    public int getVampireThreatCount() {
        return vampireThreatCount;
    }

    public int recordVampireThreat(long gameTime, long windowTicks) {
        if (lastVampireThreatTick == Long.MIN_VALUE || gameTime - lastVampireThreatTick > windowTicks) {
            vampireThreatCount = 0;
        }
        lastVampireThreatTick = gameTime;
        vampireThreatCount++;
        return vampireThreatCount;
    }

    public void enterDefensiveMode(long gameTime, long durationTicks, String reason, UUID targetUuid) {
        defensiveMode = true;
        defensiveModeUntilTick = gameTime + Math.max(1L, durationTicks);
        defensiveReason = reason == null ? "" : reason;
        defensiveTargetUuid = targetUuid;
        lastVampireThreatTick = gameTime;
    }

    public void clearDefensiveMode() {
        defensiveMode = false;
        defensiveModeUntilTick = Long.MIN_VALUE;
        defensiveReason = "";
        defensiveTargetUuid = null;
    }

    public boolean isDefensiveWeaponEquippedLogged() {
        return defensiveWeaponEquippedLogged;
    }

    public void markDefensiveWeaponEquippedLogged() {
        defensiveWeaponEquippedLogged = true;
    }

    public void clearDefensiveWeaponEquippedLogged() {
        defensiveWeaponEquippedLogged = false;
    }

    public boolean isHunterTraitApplied() {
        return hunterTraitApplied;
    }

    public void setHunterTraitApplied(boolean hunterTraitApplied) {
        this.hunterTraitApplied = hunterTraitApplied;
    }

    public boolean isHunterAligned() {
        return hunterAligned;
    }

    public void setHunterAligned(boolean hunterAligned, long untilTick) {
        this.hunterAligned = hunterAligned;
        this.hunterAlignmentUntilTick = hunterAligned ? untilTick : Long.MIN_VALUE;
    }

    public long getHunterAlignmentUntilTick() {
        return hunterAlignmentUntilTick;
    }

    public boolean isHunterVillageApplied() {
        return hunterVillageApplied;
    }

    public void setHunterVillageApplied(boolean hunterVillageApplied) {
        this.hunterVillageApplied = hunterVillageApplied;
    }

    public boolean isWerewolfConverted() {
        return werewolfConverted;
    }

    public void setWerewolfConverted(boolean werewolfConverted) {
        this.werewolfConverted = werewolfConverted;
    }

    public boolean isWerewolfInfected() {
        return werewolfInfected;
    }

    public boolean isWerewolfAppearanceApplied() {
        return werewolfAppearanceApplied;
    }

    public void setWerewolfAppearanceApplied(boolean werewolfAppearanceApplied) {
        this.werewolfAppearanceApplied = werewolfAppearanceApplied;
    }

    public boolean isWerewolfTraitApplied() {
        return werewolfTraitApplied;
    }

    public void setWerewolfTraitApplied(boolean werewolfTraitApplied) {
        this.werewolfTraitApplied = werewolfTraitApplied;
    }

    public boolean isWerewolfPartialForm() {
        return werewolfPartialForm;
    }

    public void setWerewolfPartialForm(boolean werewolfPartialForm) {
        this.werewolfPartialForm = werewolfPartialForm;
    }

    public long getWerewolfInfectionStartTick() {
        return werewolfInfectionStartTick;
    }

    public long getWerewolfInfectionFinishTick() {
        return werewolfInfectionFinishTick;
    }

    public long getWerewolfLastTransformationTick() {
        return werewolfLastTransformationTick;
    }

    public long getWerewolfBiteCooldownUntilTick() {
        return werewolfBiteCooldownUntilTick;
    }

    public void setWerewolfBiteCooldownUntilTick(long werewolfBiteCooldownUntilTick) {
        this.werewolfBiteCooldownUntilTick = werewolfBiteCooldownUntilTick;
    }

    public Optional<UUID> getWerewolfSourceUuid() {
        return Optional.ofNullable(werewolfSourceUuid);
    }

    public void startWerewolfInfection(long gameTime, long durationTicks, UUID sourceUuid) {
        werewolfInfected = true;
        werewolfConverted = false;
        werewolfInfectionStartTick = gameTime;
        werewolfInfectionFinishTick = gameTime + Math.max(1L, durationTicks);
        werewolfSourceUuid = sourceUuid;
    }

    public void finishWerewolfConversion(long gameTime) {
        werewolfInfected = false;
        werewolfConverted = true;
        werewolfAppearanceApplied = true;
        werewolfTraitApplied = true;
        werewolfPartialForm = true;
        werewolfLastTransformationTick = gameTime;
        werewolfInfectionStartTick = -1L;
        werewolfInfectionFinishTick = -1L;
    }

    public void clearWerewolfInfection() {
        werewolfInfected = false;
        werewolfInfectionStartTick = -1L;
        werewolfInfectionFinishTick = -1L;
        werewolfSourceUuid = null;
    }

    public void clearWerewolfState() {
        clearWerewolfInfection();
        werewolfConverted = false;
        werewolfAppearanceApplied = false;
        werewolfTraitApplied = false;
        werewolfPartialForm = false;
        werewolfLastTransformationTick = Long.MIN_VALUE;
        werewolfBiteCooldownUntilTick = Long.MIN_VALUE;
    }

    public boolean isOriginalMainHandItemSaved() {
        return originalMainHandItemSaved;
    }

    public ItemStack getOriginalMainHandItem() {
        return originalMainHandItem.copy();
    }

    public void saveOriginalMainHandItem(ItemStack stack) {
        if (originalMainHandItemSaved) {
            return;
        }
        originalMainHandItem = stack == null ? ItemStack.EMPTY : stack.copy();
        originalMainHandItemSaved = true;
    }

    public void clearOriginalMainHandItem() {
        originalMainHandItem = ItemStack.EMPTY;
        originalMainHandItemSaved = false;
    }

    /**
     * The real Hunter's weapon is chosen once and kept through MCA equipment
     * refreshes, Easy Villagers capture and world reloads. An empty value is
     * intentionally kept for old saves until their first reconciliation.
     */
    public String getHunterWeaponSelection() {
        return hunterWeaponSelection;
    }

    public void setHunterWeaponSelection(String selection) {
        hunterWeaponSelection = selection == null ? "" : selection;
    }

    public void clearHunterWeaponSelection() {
        hunterWeaponSelection = "";
    }

    /**
     * The MCA combat hand that owns the temporary Hunter weapon. This stays
     * separate from the legacy OriginalMainHandItem key so existing saves can
     * be migrated without changing their payload shape.
     */
    public String getHunterWeaponHand() {
        return hunterWeaponHand;
    }

    public void setHunterWeaponHand(String hand) {
        hunterWeaponHand = hand == null ? "" : hand;
    }

    public void clearHunterWeaponHand() {
        hunterWeaponHand = "";
    }

    public boolean isOriginalAppearanceSaved() {
        return originalAppearanceSaved;
    }

    public void saveOriginalAppearance(float skinGene, float melaninGene, float hemoglobinGene, float faceGene, String customSkin) {
        saveOriginalAppearance(
                skinGene,
                melaninGene,
                hemoglobinGene,
                faceGene,
                customSkin,
                0.0F,
                -1,
                -1,
                0xFF000000
        );
    }

    public void saveOriginalAppearance(float skinGene, float melaninGene, float hemoglobinGene, float faceGene,
                                       String customSkin, float eyeBrightnessGene, int eyeDye,
                                       int eyeLeftDye, int skinDye) {
        if (originalAppearanceSaved) {
            return;
        }
        this.originalSkinGene = skinGene;
        this.originalMelaninGene = melaninGene;
        this.originalHemoglobinGene = hemoglobinGene;
        this.originalFaceGene = faceGene;
        this.originalCustomSkin = customSkin == null ? "" : customSkin;
        this.originalEyeBrightnessGene = eyeBrightnessGene;
        this.originalEyeDye = eyeDye;
        this.originalEyeLeftDye = eyeLeftDye;
        this.originalSkinDye = skinDye;
        this.appearanceSnapshotVersion = 2;
        this.originalAppearanceSaved = true;
    }

    public void upgradeNativeAppearanceSnapshot(float eyeBrightnessGene, int eyeDye, int eyeLeftDye, int skinDye) {
        if (!originalAppearanceSaved || appearanceSnapshotVersion >= 2) {
            return;
        }
        this.originalEyeBrightnessGene = eyeBrightnessGene;
        this.originalEyeDye = eyeDye;
        this.originalEyeLeftDye = eyeLeftDye;
        this.originalSkinDye = skinDye;
        this.appearanceSnapshotVersion = 2;
    }

    public boolean isVampireAppearanceApplied() {
        return vampireAppearanceApplied;
    }

    public boolean isVampireNightScheduleApplied() {
        return vampireNightScheduleApplied;
    }

    public String getOriginalMcaSchedule() {
        return originalMcaSchedule;
    }

    public String getLastAppliedVampireSchedule() {
        return lastAppliedVampireSchedule;
    }

    public void beginVampireNightSchedule(String originalSchedule, String appliedSchedule) {
        vampireNightScheduleApplied = true;
        originalMcaSchedule = originalSchedule == null ? "" : originalSchedule;
        lastAppliedVampireSchedule = appliedSchedule == null ? "" : appliedSchedule;
    }

    public void updateOriginalMcaSchedule(String originalSchedule) {
        originalMcaSchedule = originalSchedule == null ? "" : originalSchedule;
    }

    public void updateLastAppliedVampireSchedule(String appliedSchedule) {
        lastAppliedVampireSchedule = appliedSchedule == null ? "" : appliedSchedule;
    }

    public void clearVampireNightSchedule() {
        vampireNightScheduleApplied = false;
        originalMcaSchedule = "";
        lastAppliedVampireSchedule = "";
    }

    public void setVampireAppearanceApplied(boolean vampireAppearanceApplied) {
        this.vampireAppearanceApplied = vampireAppearanceApplied;
    }

    public boolean isVampireEyeIndexAssigned() {
        return vampireEyeIndexAssigned;
    }

    public int getVampireEyeIndex() {
        return vampireEyeIndex;
    }

    public void assignVampireEyeIndex(int vampireEyeIndex) {
        this.vampireEyeIndex = vampireEyeIndex;
        this.vampireEyeIndexAssigned = true;
    }

    public boolean isCuringVampire() {
        return curingVampire;
    }

    public boolean isCuringWerewolf() {
        return curingWerewolf;
    }

    public long getCureStartedTick() {
        return cureStartedTick;
    }

    public long getCureFinishTick() {
        return cureFinishTick;
    }

    public int getCureStage() {
        return cureStage;
    }

    public Optional<UUID> getCureInitiatorUuid() {
        return Optional.ofNullable(cureInitiatorUuid);
    }

    public long getWerewolfCureStartedTick() {
        return werewolfCureStartedTick;
    }

    public long getWerewolfCureFinishTick() {
        return werewolfCureFinishTick;
    }

    public Optional<UUID> getWerewolfCureInitiatorUuid() {
        return Optional.ofNullable(werewolfCureInitiatorUuid);
    }

    public boolean hasCureWeakness(long gameTime) {
        return hasCureWeakness(gameTime, false);
    }

    public boolean hasCureWeakness(long gameTime, boolean werewolf) {
        clearExpiredCureIngredientProgress(gameTime);
        return werewolf ? werewolfCureHadWeakness : vampireCureHadWeakness;
    }

    public boolean hasCureGoldenApple(long gameTime) {
        return hasCureGoldenApple(gameTime, false);
    }

    public boolean hasCureGoldenApple(long gameTime, boolean werewolf) {
        clearExpiredCureIngredientProgress(gameTime);
        return werewolf ? werewolfCureHadGoldenApple : vampireCureHadGoldenApple;
    }

    public boolean hasCureGarlicInjection(long gameTime) {
        return hasCureGarlicInjection(gameTime, false);
    }

    public boolean hasCureGarlicInjection(long gameTime, boolean werewolf) {
        clearExpiredCureIngredientProgress(gameTime);
        return werewolf ? werewolfCureHadGarlicInjection : vampireCureHadGarlicInjection;
    }

    public boolean hasAnyCureIngredientProgress(long gameTime, boolean werewolf) {
        clearExpiredCureIngredientProgress(gameTime);
        return werewolf
                ? werewolfCureHadWeakness || werewolfCureHadGoldenApple || werewolfCureHadGarlicInjection
                : vampireCureHadWeakness || vampireCureHadGoldenApple || vampireCureHadGarlicInjection;
    }

    public void markCureWeakness(long gameTime, long progressTicks, UUID initiatorUuid) {
        markCureWeakness(gameTime, progressTicks, initiatorUuid, false);
    }

    public void markCureWeakness(long gameTime, long progressTicks, UUID initiatorUuid, boolean werewolf) {
        if (werewolf) {
            werewolfCureHadWeakness = true;
        } else {
            vampireCureHadWeakness = true;
        }
        updateLegacyCureProgressMirror();
        cureStage = Math.max(cureStage, 1);
        updateCureIngredientProgress(gameTime, progressTicks, initiatorUuid, werewolf);
    }

    public void markCureGoldenApple(long gameTime, long progressTicks, UUID initiatorUuid) {
        markCureGoldenApple(gameTime, progressTicks, initiatorUuid, false);
    }

    public void markCureGoldenApple(long gameTime, long progressTicks, UUID initiatorUuid, boolean werewolf) {
        if (werewolf) {
            werewolfCureHadGoldenApple = true;
        } else {
            vampireCureHadGoldenApple = true;
        }
        updateLegacyCureProgressMirror();
        cureStage = Math.max(cureStage, 2);
        updateCureIngredientProgress(gameTime, progressTicks, initiatorUuid, werewolf);
    }

    public void markCureGarlicInjection(long gameTime, long progressTicks, UUID initiatorUuid) {
        markCureGarlicInjection(gameTime, progressTicks, initiatorUuid, false);
    }

    public void markCureGarlicInjection(long gameTime, long progressTicks, UUID initiatorUuid, boolean werewolf) {
        if (werewolf) {
            werewolfCureHadGarlicInjection = true;
        } else {
            vampireCureHadGarlicInjection = true;
        }
        updateLegacyCureProgressMirror();
        cureStage = Math.max(cureStage, 2);
        updateCureIngredientProgress(gameTime, progressTicks, initiatorUuid, werewolf);
    }

    public void startVampireCure(long gameTime, long durationTicks, UUID initiatorUuid) {
        curingVampire = true;
        curingWerewolf = false;
        cureStartedTick = gameTime;
        cureFinishTick = gameTime + Math.max(1L, durationTicks);
        cureStage = 3;
        cureInitiatorUuid = initiatorUuid;
        werewolfCureStartedTick = -1L;
        werewolfCureFinishTick = -1L;
        werewolfCureInitiatorUuid = null;
        clearCureIngredientProgress(false);
        clearCureIngredientProgress(true);
    }

    public void startWerewolfCure(long gameTime, long durationTicks, UUID initiatorUuid) {
        curingWerewolf = true;
        curingVampire = false;
        werewolfCureStartedTick = gameTime;
        werewolfCureFinishTick = gameTime + Math.max(1L, durationTicks);
        cureStage = 3;
        werewolfCureInitiatorUuid = initiatorUuid;
        cureStartedTick = -1L;
        cureFinishTick = -1L;
        cureInitiatorUuid = null;
        clearCureIngredientProgress(false);
        clearCureIngredientProgress(true);
    }

    public void cancelVampireCure() {
        curingVampire = false;
        cureStartedTick = -1L;
        cureFinishTick = -1L;
        cureStage = 0;
        cureInitiatorUuid = null;
        clearCureIngredientProgress(false);
    }

    public void cancelWerewolfCure() {
        curingWerewolf = false;
        werewolfCureStartedTick = -1L;
        werewolfCureFinishTick = -1L;
        cureStage = 0;
        werewolfCureInitiatorUuid = null;
        clearCureIngredientProgress(true);
    }

    public void clearWerewolfAfterCure() {
        clearWerewolfState();
        curingWerewolf = false;
        werewolfCureStartedTick = -1L;
        werewolfCureFinishTick = -1L;
        werewolfCureInitiatorUuid = null;
        cureStage = 0;
        clearCureIngredientProgress(true);
    }

    public void clearExpiredCureIngredientProgress(long gameTime) {
        if (!curingVampire && vampireCureIngredientExpireTick != Long.MIN_VALUE && gameTime > vampireCureIngredientExpireTick) {
            clearCureIngredientProgress(false);
        }
        if (!curingWerewolf && werewolfCureIngredientExpireTick != Long.MIN_VALUE && gameTime > werewolfCureIngredientExpireTick) {
            clearCureIngredientProgress(true);
        }
    }

    private void updateCureIngredientProgress(long gameTime, long progressTicks, UUID initiatorUuid, boolean werewolf) {
        long expireTick = gameTime + Math.max(1L, progressTicks);
        if (werewolf) {
            werewolfCureIngredientExpireTick = expireTick;
            werewolfCureInitiatorUuid = initiatorUuid;
        } else {
            vampireCureIngredientExpireTick = expireTick;
            cureInitiatorUuid = initiatorUuid;
        }
        updateLegacyCureProgressMirror();
    }

    private void clearCureIngredientProgress(boolean werewolf) {
        if (werewolf) {
            werewolfCureHadWeakness = false;
            werewolfCureHadGoldenApple = false;
            werewolfCureHadGarlicInjection = false;
            werewolfCureIngredientExpireTick = Long.MIN_VALUE;
        } else {
            vampireCureHadWeakness = false;
            vampireCureHadGoldenApple = false;
            vampireCureHadGarlicInjection = false;
            vampireCureIngredientExpireTick = Long.MIN_VALUE;
        }
        updateLegacyCureProgressMirror();
        if (!curingVampire && !curingWerewolf) {
            cureStage = 0;
        }
    }

    private void updateLegacyCureProgressMirror() {
        cureHadWeakness = vampireCureHadWeakness || werewolfCureHadWeakness;
        cureHadGoldenApple = vampireCureHadGoldenApple || werewolfCureHadGoldenApple;
        cureHadGarlicInjection = vampireCureHadGarlicInjection || werewolfCureHadGarlicInjection;
        cureIngredientExpireTick = Math.max(vampireCureIngredientExpireTick, werewolfCureIngredientExpireTick);
    }

    public float getOriginalSkinGene() {
        return originalSkinGene;
    }

    public float getOriginalMelaninGene() {
        return originalMelaninGene;
    }

    public float getOriginalHemoglobinGene() {
        return originalHemoglobinGene;
    }

    public float getOriginalFaceGene() {
        return originalFaceGene;
    }

    public float getOriginalEyeBrightnessGene() {
        return originalEyeBrightnessGene;
    }

    public int getOriginalEyeDye() {
        return originalEyeDye;
    }

    public int getOriginalEyeLeftDye() {
        return originalEyeLeftDye;
    }

    public int getOriginalSkinDye() {
        return originalSkinDye;
    }

    public int getAppearanceSnapshotVersion() {
        return appearanceSnapshotVersion;
    }

    public String getOriginalCustomSkin() {
        return originalCustomSkin;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Infected", infected);
        tag.putBoolean("Converted", converted);
        tag.putBoolean("SanguinareActive", sanguinareActive);
        tag.putLong("InfectionStartTick", infectionStartTick);
        tag.putLong("LastBiteTick", lastBiteTick);
        tag.putLong("LastGrowthTick", lastGrowthTick);
        tag.putLong("LastBittenByMcaVampireTick", lastBittenByMcaVampireTick);
        tag.putLong("LastVampirePlayerBiteTick", lastVampirePlayerBiteTick);
        tag.putLong("LastAggressedByTick", lastAggressedByTick);
        tag.putLong("RetaliationUntilTick", retaliationUntilTick);
        tag.putLong("DefensiveModeUntilTick", defensiveModeUntilTick);
        tag.putLong("LastVampireThreatTick", lastVampireThreatTick);
        tag.putInt("VampireThreatCount", vampireThreatCount);
        tag.putBoolean("DefensiveMode", defensiveMode);
        tag.putBoolean("OriginalMainHandItemSaved", originalMainHandItemSaved);
        tag.putBoolean("DefensiveWeaponEquippedLogged", defensiveWeaponEquippedLogged);
        tag.putBoolean("HunterTraitApplied", hunterTraitApplied);
        tag.putBoolean("HunterAligned", hunterAligned);
        tag.putBoolean("HunterVillageApplied", hunterVillageApplied);
        tag.putBoolean("WerewolfConverted", werewolfConverted);
        tag.putBoolean("WerewolfInfected", werewolfInfected);
        tag.putBoolean("WerewolfAppearanceApplied", werewolfAppearanceApplied);
        tag.putBoolean("WerewolfTraitApplied", werewolfTraitApplied);
        tag.putBoolean("WerewolfPartialForm", werewolfPartialForm);
        tag.putBoolean("FactionInheritanceProcessed", factionInheritanceProcessed);
        tag.putLong("HunterAlignmentUntilTick", hunterAlignmentUntilTick);
        tag.putBoolean("WasBittenByVampirePlayer", wasBittenByVampirePlayer);
        tag.putBoolean("BiteWasConversionCause", biteWasConversionCause);
        tag.putBoolean("OriginalAppearanceSaved", originalAppearanceSaved);
        tag.putBoolean("VampireAppearanceApplied", vampireAppearanceApplied);
        tag.putBoolean("VampireEyeIndexAssigned", vampireEyeIndexAssigned);
        tag.putBoolean("VampireNightScheduleApplied", vampireNightScheduleApplied);
        tag.putBoolean("CuringVampire", curingVampire);
        tag.putBoolean("CuringWerewolf", curingWerewolf);
        tag.putBoolean("CureHadWeakness", cureHadWeakness);
        tag.putBoolean("CureHadGoldenApple", cureHadGoldenApple);
        tag.putBoolean("CureHadGarlicInjection", cureHadGarlicInjection);
        tag.putBoolean("VampireCureHadWeakness", vampireCureHadWeakness);
        tag.putBoolean("VampireCureHadGoldenApple", vampireCureHadGoldenApple);
        tag.putBoolean("VampireCureHadGarlicInjection", vampireCureHadGarlicInjection);
        tag.putBoolean("WerewolfCureHadWeakness", werewolfCureHadWeakness);
        tag.putBoolean("WerewolfCureHadGoldenApple", werewolfCureHadGoldenApple);
        tag.putBoolean("WerewolfCureHadGarlicInjection", werewolfCureHadGarlicInjection);
        tag.putInt("VampireEyeIndex", vampireEyeIndex);
        tag.putInt("CureStage", cureStage);
        tag.putFloat("OriginalSkinGene", originalSkinGene);
        tag.putFloat("OriginalMelaninGene", originalMelaninGene);
        tag.putFloat("OriginalHemoglobinGene", originalHemoglobinGene);
        tag.putFloat("OriginalFaceGene", originalFaceGene);
        tag.putFloat("OriginalEyeBrightnessGene", originalEyeBrightnessGene);
        tag.putInt("OriginalEyeDye", originalEyeDye);
        tag.putInt("OriginalEyeLeftDye", originalEyeLeftDye);
        tag.putInt("OriginalSkinDye", originalSkinDye);
        tag.putInt("AppearanceSnapshotVersion", appearanceSnapshotVersion);
        tag.putLong("CureStartedTick", cureStartedTick);
        tag.putLong("CureFinishTick", cureFinishTick);
        tag.putLong("WerewolfCureStartedTick", werewolfCureStartedTick);
        tag.putLong("WerewolfCureFinishTick", werewolfCureFinishTick);
        tag.putLong("CureIngredientExpireTick", cureIngredientExpireTick);
        tag.putLong("VampireCureIngredientExpireTick", vampireCureIngredientExpireTick);
        tag.putLong("WerewolfCureIngredientExpireTick", werewolfCureIngredientExpireTick);
        tag.putLong("WerewolfInfectionStartTick", werewolfInfectionStartTick);
        tag.putLong("WerewolfInfectionFinishTick", werewolfInfectionFinishTick);
        tag.putLong("WerewolfLastTransformationTick", werewolfLastTransformationTick);
        tag.putLong("WerewolfBiteCooldownUntilTick", werewolfBiteCooldownUntilTick);
        tag.putLong("LastCureCompletedTick", lastCureCompletedTick);
        tag.putString("OriginalCustomSkin", originalCustomSkin);
        tag.putString("OriginalMcaSchedule", originalMcaSchedule);
        tag.putString("LastAppliedVampireSchedule", lastAppliedVampireSchedule);
        tag.putString("DefensiveReason", defensiveReason);
        tag.putString("LastCureType", lastCureType);
        if (!hunterWeaponSelection.isEmpty()) {
            tag.putString("HunterWeaponSelection", hunterWeaponSelection);
        }
        if (!hunterWeaponHand.isEmpty()) {
            tag.putString("HunterWeaponHand", hunterWeaponHand);
        }
        if (source != null) {
            tag.putUUID("Source", source);
        }
        if (lastAggressorUuid != null) {
            tag.putUUID("LastAggressorUuid", lastAggressorUuid);
        }
        if (lastVampirePlayerBiteUuid != null) {
            tag.putUUID("LastVampirePlayerBiteUuid", lastVampirePlayerBiteUuid);
        }
        if (defensiveTargetUuid != null) {
            tag.putUUID("DefensiveTargetUuid", defensiveTargetUuid);
        }
        if (cureInitiatorUuid != null) {
            tag.putUUID("CureInitiatorUuid", cureInitiatorUuid);
        }
        if (werewolfCureInitiatorUuid != null) {
            tag.putUUID("WerewolfCureInitiatorUuid", werewolfCureInitiatorUuid);
        }
        if (werewolfSourceUuid != null) {
            tag.putUUID("WerewolfSourceUuid", werewolfSourceUuid);
        }
        /*
         * An empty hand is a meaningful saved value: defensive villagers must
         * put their temporary weapon away again. ItemStack.save, however,
         * rejects ItemStack.EMPTY in 1.21.1, so preserve the boolean marker and
         * omit only the optional item payload.
         */
        VampiricVillagerStateNbtCodec.writeOriginalMainHand(
                tag,
                originalMainHandItemSaved,
                originalMainHandItem,
                registries
        );
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag tag) {
        infected = tag.getBoolean("Infected");
        converted = tag.getBoolean("Converted");
        sanguinareActive = tag.getBoolean("SanguinareActive");
        infectionStartTick = tag.contains("InfectionStartTick") ? tag.getLong("InfectionStartTick") : -1L;
        lastBiteTick = tag.contains("LastBiteTick") ? tag.getLong("LastBiteTick") : Long.MIN_VALUE;
        lastGrowthTick = tag.contains("LastGrowthTick") ? tag.getLong("LastGrowthTick") : -1L;
        lastBittenByMcaVampireTick = tag.contains("LastBittenByMcaVampireTick")
                ? tag.getLong("LastBittenByMcaVampireTick")
                : Long.MIN_VALUE;
        lastVampirePlayerBiteTick = tag.contains("LastVampirePlayerBiteTick")
                ? tag.getLong("LastVampirePlayerBiteTick")
                : Long.MIN_VALUE;
        lastAggressedByTick = tag.contains("LastAggressedByTick") ? tag.getLong("LastAggressedByTick") : Long.MIN_VALUE;
        retaliationUntilTick = tag.contains("RetaliationUntilTick") ? tag.getLong("RetaliationUntilTick") : Long.MIN_VALUE;
        defensiveModeUntilTick = tag.contains("DefensiveModeUntilTick") ? tag.getLong("DefensiveModeUntilTick") : Long.MIN_VALUE;
        lastVampireThreatTick = tag.contains("LastVampireThreatTick") ? tag.getLong("LastVampireThreatTick") : Long.MIN_VALUE;
        vampireThreatCount = tag.contains("VampireThreatCount") ? tag.getInt("VampireThreatCount") : 0;
        defensiveMode = tag.getBoolean("DefensiveMode");
        originalMainHandItemSaved = tag.getBoolean("OriginalMainHandItemSaved");
        defensiveWeaponEquippedLogged = tag.getBoolean("DefensiveWeaponEquippedLogged");
        hunterTraitApplied = tag.getBoolean("HunterTraitApplied");
        hunterAligned = tag.getBoolean("HunterAligned");
        hunterVillageApplied = tag.getBoolean("HunterVillageApplied");
        werewolfConverted = tag.getBoolean("WerewolfConverted");
        werewolfInfected = tag.getBoolean("WerewolfInfected");
        werewolfAppearanceApplied = tag.getBoolean("WerewolfAppearanceApplied");
        werewolfTraitApplied = tag.getBoolean("WerewolfTraitApplied");
        werewolfPartialForm = tag.getBoolean("WerewolfPartialForm");
        factionInheritanceProcessed = tag.getBoolean("FactionInheritanceProcessed");
        hunterAlignmentUntilTick = tag.contains("HunterAlignmentUntilTick") ? tag.getLong("HunterAlignmentUntilTick") : Long.MIN_VALUE;
        wasBittenByVampirePlayer = tag.getBoolean("WasBittenByVampirePlayer");
        biteWasConversionCause = tag.getBoolean("BiteWasConversionCause");
        originalAppearanceSaved = tag.getBoolean("OriginalAppearanceSaved");
        vampireAppearanceApplied = tag.getBoolean("VampireAppearanceApplied");
        vampireEyeIndexAssigned = tag.getBoolean("VampireEyeIndexAssigned");
        vampireNightScheduleApplied = tag.getBoolean("VampireNightScheduleApplied");
        curingVampire = tag.getBoolean("CuringVampire");
        curingWerewolf = tag.getBoolean("CuringWerewolf");
        boolean legacyCureHadWeakness = tag.getBoolean("CureHadWeakness");
        boolean legacyCureHadGoldenApple = tag.getBoolean("CureHadGoldenApple");
        boolean legacyCureHadGarlicInjection = tag.getBoolean("CureHadGarlicInjection");
        vampireCureHadWeakness = tag.contains("VampireCureHadWeakness") ? tag.getBoolean("VampireCureHadWeakness") : legacyCureHadWeakness;
        vampireCureHadGoldenApple = tag.contains("VampireCureHadGoldenApple") ? tag.getBoolean("VampireCureHadGoldenApple") : legacyCureHadGoldenApple;
        vampireCureHadGarlicInjection = tag.contains("VampireCureHadGarlicInjection") ? tag.getBoolean("VampireCureHadGarlicInjection") : legacyCureHadGarlicInjection;
        werewolfCureHadWeakness = tag.contains("WerewolfCureHadWeakness") ? tag.getBoolean("WerewolfCureHadWeakness") : legacyCureHadWeakness;
        werewolfCureHadGoldenApple = tag.contains("WerewolfCureHadGoldenApple") ? tag.getBoolean("WerewolfCureHadGoldenApple") : legacyCureHadGoldenApple;
        werewolfCureHadGarlicInjection = tag.contains("WerewolfCureHadGarlicInjection") ? tag.getBoolean("WerewolfCureHadGarlicInjection") : legacyCureHadGarlicInjection;
        vampireEyeIndex = tag.contains("VampireEyeIndex") ? tag.getInt("VampireEyeIndex") : -1;
        cureStage = tag.contains("CureStage") ? tag.getInt("CureStage") : 0;
        originalSkinGene = tag.contains("OriginalSkinGene") ? tag.getFloat("OriginalSkinGene") : 0.0F;
        originalMelaninGene = tag.contains("OriginalMelaninGene") ? tag.getFloat("OriginalMelaninGene") : 0.0F;
        originalHemoglobinGene = tag.contains("OriginalHemoglobinGene") ? tag.getFloat("OriginalHemoglobinGene") : 0.0F;
        originalFaceGene = tag.contains("OriginalFaceGene") ? tag.getFloat("OriginalFaceGene") : 0.0F;
        originalEyeBrightnessGene = tag.contains("OriginalEyeBrightnessGene") ? tag.getFloat("OriginalEyeBrightnessGene") : 0.0F;
        originalEyeDye = tag.contains("OriginalEyeDye") ? tag.getInt("OriginalEyeDye") : -1;
        originalEyeLeftDye = tag.contains("OriginalEyeLeftDye") ? tag.getInt("OriginalEyeLeftDye") : -1;
        originalSkinDye = tag.contains("OriginalSkinDye") ? tag.getInt("OriginalSkinDye") : 0xFF000000;
        appearanceSnapshotVersion = tag.contains("AppearanceSnapshotVersion")
                ? tag.getInt("AppearanceSnapshotVersion")
                : originalAppearanceSaved ? 1 : 0;
        cureStartedTick = tag.contains("CureStartedTick") ? tag.getLong("CureStartedTick") : -1L;
        cureFinishTick = tag.contains("CureFinishTick") ? tag.getLong("CureFinishTick") : -1L;
        werewolfCureStartedTick = tag.contains("WerewolfCureStartedTick") ? tag.getLong("WerewolfCureStartedTick") : -1L;
        werewolfCureFinishTick = tag.contains("WerewolfCureFinishTick") ? tag.getLong("WerewolfCureFinishTick") : -1L;
        long legacyCureIngredientExpireTick = tag.contains("CureIngredientExpireTick") ? tag.getLong("CureIngredientExpireTick") : Long.MIN_VALUE;
        vampireCureIngredientExpireTick = tag.contains("VampireCureIngredientExpireTick")
                ? tag.getLong("VampireCureIngredientExpireTick")
                : legacyCureIngredientExpireTick;
        werewolfCureIngredientExpireTick = tag.contains("WerewolfCureIngredientExpireTick")
                ? tag.getLong("WerewolfCureIngredientExpireTick")
                : legacyCureIngredientExpireTick;
        werewolfInfectionStartTick = tag.contains("WerewolfInfectionStartTick") ? tag.getLong("WerewolfInfectionStartTick") : -1L;
        werewolfInfectionFinishTick = tag.contains("WerewolfInfectionFinishTick") ? tag.getLong("WerewolfInfectionFinishTick") : -1L;
        werewolfLastTransformationTick = tag.contains("WerewolfLastTransformationTick") ? tag.getLong("WerewolfLastTransformationTick") : Long.MIN_VALUE;
        werewolfBiteCooldownUntilTick = tag.contains("WerewolfBiteCooldownUntilTick") ? tag.getLong("WerewolfBiteCooldownUntilTick") : Long.MIN_VALUE;
        lastCureCompletedTick = tag.contains("LastCureCompletedTick") ? tag.getLong("LastCureCompletedTick") : Long.MIN_VALUE;
        originalCustomSkin = tag.contains("OriginalCustomSkin") ? tag.getString("OriginalCustomSkin") : "";
        originalMcaSchedule = tag.contains("OriginalMcaSchedule") ? tag.getString("OriginalMcaSchedule") : "";
        lastAppliedVampireSchedule = tag.contains("LastAppliedVampireSchedule")
                ? tag.getString("LastAppliedVampireSchedule")
                : "";
        defensiveReason = tag.contains("DefensiveReason") ? tag.getString("DefensiveReason") : "";
        lastCureType = tag.contains("LastCureType") ? tag.getString("LastCureType") : "";
        hunterWeaponSelection = tag.contains("HunterWeaponSelection") ? tag.getString("HunterWeaponSelection") : "";
        hunterWeaponHand = tag.contains("HunterWeaponHand") ? tag.getString("HunterWeaponHand") : "";
        source = tag.hasUUID("Source") ? tag.getUUID("Source") : null;
        lastAggressorUuid = tag.hasUUID("LastAggressorUuid") ? tag.getUUID("LastAggressorUuid") : null;
        lastVampirePlayerBiteUuid = tag.hasUUID("LastVampirePlayerBiteUuid")
                ? tag.getUUID("LastVampirePlayerBiteUuid")
                : null;
        defensiveTargetUuid = tag.hasUUID("DefensiveTargetUuid") ? tag.getUUID("DefensiveTargetUuid") : null;
        cureInitiatorUuid = tag.hasUUID("CureInitiatorUuid") ? tag.getUUID("CureInitiatorUuid") : null;
        werewolfCureInitiatorUuid = tag.hasUUID("WerewolfCureInitiatorUuid") ? tag.getUUID("WerewolfCureInitiatorUuid") : null;
        werewolfSourceUuid = tag.hasUUID("WerewolfSourceUuid") ? tag.getUUID("WerewolfSourceUuid") : null;
        originalMainHandItem = VampiricVillagerStateNbtCodec.readOriginalMainHand(registries, tag);
        aiGoalsAdded = false;
        defensiveGoalsAdded = false;
        hunterAiGoalsAdded = false;
        werewolfAiGoalsAdded = false;

        if (converted) {
            infected = false;
            sanguinareActive = false;
            defensiveMode = false;
            hunterTraitApplied = false;
            hunterAligned = false;
            hunterVillageApplied = false;
        }
        if (werewolfConverted) {
            werewolfInfected = false;
            werewolfAppearanceApplied = true;
            werewolfPartialForm = true;
        }
        if (curingWerewolf && curingVampire) {
            curingWerewolf = false;
            werewolfCureStartedTick = -1L;
            werewolfCureFinishTick = -1L;
            werewolfCureInitiatorUuid = null;
        }
        updateLegacyCureProgressMirror();
    }
}
