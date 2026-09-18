package com.guilh.mca_vampirism_compat.easyvillagers.readonly;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.easyvillagers.McaEvCompatBridge;
import com.guilh.mca_vampirism_compat.easyvillagers.McaEvStatePayload;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.Optional;

public final class StoredMcaFactionResolver {
    private static final String TRADER_TILEENTITY_BASE = "de.maxhenkel.easyvillagers.blocks.tileentity.TraderTileentityBase";
    private static final String AUTO_TRADER_TILEENTITY = "de.maxhenkel.easyvillagers.blocks.tileentity.AutoTraderTileentity";
    private static final String VILLAGER_TAG = "villager";
    private static final String MCA_COMPAT_TAG = "mca_easy_villagers_compat";
    private static final String ENTITY_TYPE_KEY = "EntityType";
    private static final String MCA_MALE = "mca:male_villager";
    private static final String MCA_FEMALE = "mca:female_villager";
    private static final String VAMPIRE_TRAIT_ID = "vampirism_mca_compat:vampire";
    private static final String HUNTER_TRAIT_ID = "vampirism_mca_compat:hunter";
    private static final String WEREWOLF_TRAIT_ID = "vampirism_mca_compat:werewolf";
    private static final String MOD_ID_MARKER = "mca_vampirism_compat";
    private static final String TRAIT_NAMESPACE_MARKER = "vampirism_mca_compat";
    private static final String LEGACY_STATE_ATTACHMENT_ID = "mca_vampirism_compat:vampiric_villager";
    private static final int MAX_RECURSION_DEPTH = 12;
    private static final Map<String, Method> REFLECTED_METHODS = new HashMap<>();
    private static final Map<String, Field> REFLECTED_FIELDS = new HashMap<>();

    private StoredMcaFactionResolver() {
    }

    public static boolean isEasyVillagersDirectTrader(BlockEntity blockEntity) {
        if (blockEntity == null || isClassOrSuperclass(blockEntity.getClass(), AUTO_TRADER_TILEENTITY)) {
            return false;
        }
        return isClassOrSuperclass(blockEntity.getClass(), TRADER_TILEENTITY_BASE);
    }

    public static StoredMcaVillagerInfo resolve(BlockEntity blockEntity) {
        if (!isEasyVillagersDirectTrader(blockEntity)) {
            return StoredMcaVillagerInfo.unresolved(blockEntity);
        }

        StackSelection stackSelection = getFactionStack(blockEntity);
        ItemStack storedStack = stackSelection.stack();
        Villager villager = getStoredVillagerEntity(blockEntity);
        if (storedStack.isEmpty() || villager == null) {
            return StoredMcaVillagerInfo.unresolved(blockEntity);
        }

        StoredFactionSnapshot faction = resolveFactionSnapshot(storedStack, villager);
        CompoundTag villagerNbt = faction.villagerNbt();
        ResourceLocation entityType = safeResourceLocation(faction.entityTypeId());
        logNbtDiagnostics(
                blockEntity,
                storedStack,
                villagerNbt,
                faction.entityTypeId(),
                faction.vampireDetection(),
                faction.hunterDetection(),
                faction.werewolfDetection()
        );
        MerchantOffers offers = villager.getOffers();

        return new StoredMcaVillagerInfo(
                true,
                faction.isMca(),
                faction.isVampire(),
                faction.isHunter(),
                faction.isWerewolf(),
                faction.vampireSource(),
                faction.hunterSource(),
                faction.werewolfSource(),
                stackSelection.source(),
                entityType,
                villagerNbt == null ? null : villagerNbt.copy(),
                storedStack.copy(),
                villager,
                offers,
                blockEntity
        );
    }

    /**
     * Reads the original MCA snapshot owned by mca-ev-compat. Direct Trader
     * blocks expose it through McaVisualVillagerHolder after mixin application.
     * The returned stack is never modified by this compatibility layer.
     */
    static StackSelection getFactionStack(BlockEntity blockEntity) {
        ItemStack storedStack = getStoredStack(blockEntity);
        ItemStack visualStack = invokeStackMethod(blockEntity, "mcaEvCompat$getVisualVillagerStack");
        return selectPreferredFactionStack(visualStack, storedStack);
    }

    static StackSelection selectPreferredFactionStack(ItemStack visualStack, ItemStack storedStack) {
        if (visualStack != null && !visualStack.isEmpty()) {
            return new StackSelection(visualStack, "MCA_EV_VISUAL");
        }
        if (storedStack != null && !storedStack.isEmpty()) {
            return new StackSelection(storedStack, "EASY_VILLAGERS_STORED");
        }
        return new StackSelection(ItemStack.EMPTY, "NONE");
    }

    /**
     * Breeder parent fields passed to EasyVillagersBlockCompat are sanitized.
     * mca-ev-compat keeps the authoritative MCA snapshots in mixin-added
     * visual fields, so read those fields without mutating them.
     */
    public static ItemStack getBreederParentStack(BlockEntity blockEntity, int parentIndex, ItemStack fallback) {
        if (parentIndex != 1 && parentIndex != 2) {
            return fallback == null ? ItemStack.EMPTY : fallback;
        }
        ItemStack visualStack = readStackField(blockEntity, "mcaEvCompat$visualVillager" + parentIndex);
        return selectPreferredFactionStack(visualStack, fallback).stack();
    }

    public static StoredFactionSnapshot resolveFactionSnapshot(ItemStack stack) {
        return resolveFactionSnapshot(stack, null);
    }

    private static StoredFactionSnapshot resolveFactionSnapshot(ItemStack stack, Villager villager) {
        CompoundTag villagerNbt = getVillagerData(stack);
        String entityTypeId = resolveEntityType(stack, villagerNbt);
        Optional<McaEvStatePayload.FactionSnapshot> explicitPayload = McaEvStatePayload.readFaction(stack);
        VampireDetection storedVampire = detectVampireData(stack, villagerNbt);
        HunterDetection storedHunter = detectHunterData(stack, villagerNbt, villager, entityTypeId);
        WerewolfDetection storedWerewolf = detectWerewolfData(stack, villagerNbt);
        boolean isMca = isMcaVillagerType(entityTypeId)
                || explicitPayload.isPresent()
                || storedVampire.isVampire()
                || storedHunter.isHunter()
                || storedWerewolf.isWerewolf();

        // Payload flags are positive evidence, not tri-state authority. In
        // particular, false values written by v1/v2 must not hide canonical
        // traits preserved in the complete visual snapshot.
        VampireDetection vampireDetection = isMca
                ? combineVampireEvidence(explicitPayload, storedVampire)
                : VampireDetection.none();
        HunterDetection hunterDetection = isMca
                ? combineHunterEvidence(explicitPayload, storedHunter)
                : HunterDetection.none();
        WerewolfDetection werewolfDetection = isMca
                ? combineWerewolfEvidence(explicitPayload, storedWerewolf)
                : WerewolfDetection.none();

        // Resolve mutual exclusion only after collecting every positive signal.
        boolean isVampire = vampireDetection.isVampire();
        boolean isHunter = !isVampire && hunterDetection.isHunter();
        boolean isWerewolf = !isVampire && !isHunter && werewolfDetection.isWerewolf();
        return new StoredFactionSnapshot(
                isMca,
                isVampire,
                isHunter,
                isWerewolf,
                vampireDetection,
                hunterDetection,
                werewolfDetection,
                entityTypeId,
                villagerNbt == null ? null : villagerNbt.copy()
        );
    }

    private static VampireDetection combineVampireEvidence(
            Optional<McaEvStatePayload.FactionSnapshot> payload,
            VampireDetection storedDetection) {
        String source = positivePayloadSource(payload, McaEvStatePayload.FactionSnapshot::vampire);
        if (storedDetection.isVampire()) {
            source = mergeSources(source, storedDetection.source());
        }
        return source.isEmpty() ? VampireDetection.none() : new VampireDetection(true, source);
    }

    private static HunterDetection combineHunterEvidence(
            Optional<McaEvStatePayload.FactionSnapshot> payload,
            HunterDetection storedDetection) {
        String source = positivePayloadSource(payload, McaEvStatePayload.FactionSnapshot::hunter);
        if (storedDetection.isHunter()) {
            source = mergeSources(source, storedDetection.source());
        }
        return source.isEmpty() ? HunterDetection.none() : new HunterDetection(true, source);
    }

    private static WerewolfDetection combineWerewolfEvidence(
            Optional<McaEvStatePayload.FactionSnapshot> payload,
            WerewolfDetection storedDetection) {
        String source = positivePayloadSource(payload, McaEvStatePayload.FactionSnapshot::werewolf);
        if (storedDetection.isWerewolf()) {
            source = mergeSources(source, storedDetection.source());
        }
        return source.isEmpty() ? WerewolfDetection.none() : new WerewolfDetection(true, source);
    }

    private static String positivePayloadSource(
            Optional<McaEvStatePayload.FactionSnapshot> payload,
            java.util.function.Predicate<McaEvStatePayload.FactionSnapshot> classifier) {
        return payload.filter(classifier)
                .map(value -> "PAYLOAD_V" + value.version())
                .orElse("");
    }

    private static String mergeSources(String first, String second) {
        if (first == null || first.isEmpty()) {
            return second == null ? "" : second;
        }
        if (second == null || second.isEmpty() || first.equals(second)) {
            return first;
        }
        return first + "+" + second;
    }

    static ItemStack getStoredStack(BlockEntity blockEntity) {
        try {
            Method getVillager = findMethod(blockEntity.getClass(), "getVillager");
            Object result = getVillager.invoke(blockEntity);
            return result instanceof ItemStack stack ? stack : ItemStack.EMPTY;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            McaVampirismCompat.debugLog(
                    "[EASY-VILLAGERS] failed to read stored villager stack block={} error={}",
                    blockEntity.getClass().getName(),
                    exception.toString()
            );
            return ItemStack.EMPTY;
        }
    }

    private static ItemStack invokeStackMethod(BlockEntity blockEntity, String methodName) {
        if (blockEntity == null) {
            return ItemStack.EMPTY;
        }
        try {
            Method method = findMethod(blockEntity.getClass(), methodName);
            Object result = method.invoke(blockEntity);
            return result instanceof ItemStack stack ? stack : ItemStack.EMPTY;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            McaVampirismCompat.debugLog(
                    "[EASY-VILLAGERS] visual stack method unavailable block={} method={} error={}",
                    blockEntity.getClass().getName(),
                    methodName,
                    exception.toString()
            );
            return ItemStack.EMPTY;
        }
    }

    private static ItemStack readStackField(BlockEntity blockEntity, String fieldName) {
        if (blockEntity == null) {
            return ItemStack.EMPTY;
        }
        try {
            Field field = findField(blockEntity.getClass(), fieldName);
            Object result = field.get(blockEntity);
            return result instanceof ItemStack stack ? stack : ItemStack.EMPTY;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            McaVampirismCompat.debugLog(
                    "[EASY-VILLAGERS] visual stack field unavailable block={} field={} error={}",
                    blockEntity.getClass().getName(),
                    fieldName,
                    exception.toString()
            );
            return ItemStack.EMPTY;
        }
    }

    static Villager getStoredVillagerEntity(BlockEntity blockEntity) {
        try {
            Method getVillagerEntity = findMethod(blockEntity.getClass(), "getVillagerEntity");
            Object result = getVillagerEntity.invoke(blockEntity);
            return result instanceof Villager villager ? villager : null;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            McaVampirismCompat.debugLog(
                    "[EASY-VILLAGERS] failed to read stored villager entity block={} error={}",
                    blockEntity.getClass().getName(),
                    exception.toString()
            );
            return null;
        }
    }

    static VampireDetection detectVampireData(ItemStack storedStack, CompoundTag villagerNbt) {
        CompoundTag stateTag = McaEvStatePayload.readState(storedStack).orElse(null);
        CompoundTag rootTag = customFactionEvidenceData(storedStack);
        if (containsCapabilityMarker(stateTag, "payload.state", 0, null)
                || containsTraitMarker(stateTag, "payload.state", 0, null)
                || containsVampireMarker(stateTag, "payload.state", 0, null)) {
            return new VampireDetection(true, "STATE");
        }
        if (containsCapabilityMarker(villagerNbt, "villager", 0, null)
                || containsCapabilityMarker(rootTag, "item", 0, null)) {
            return new VampireDetection(true, "CAPABILITY");
        }
        if (containsTraitMarker(villagerNbt, "villager", 0, null)
                || containsTraitMarker(rootTag, "item", 0, null)) {
            return new VampireDetection(true, "TRAIT");
        }
        if (containsVampireMarker(villagerNbt, "villager", 0, null)
                || containsVampireMarker(rootTag, "item", 0, null)) {
            return new VampireDetection(true, "RECURSIVE");
        }
        return VampireDetection.none();
    }

    static HunterDetection detectHunterData(ItemStack storedStack, CompoundTag villagerNbt, Villager villager, String entityTypeId) {
        CompoundTag stateTag = McaEvStatePayload.readState(storedStack).orElse(null);
        CompoundTag rootTag = customFactionEvidenceData(storedStack);
        if (containsHunterCapabilityMarker(stateTag, "payload.state", 0, null)
                || containsHunterTraitMarker(stateTag, "payload.state", 0, null)
                || containsHunterFlagMarker(stateTag, "payload.state", 0, null)) {
            return new HunterDetection(true, "STATE");
        }
        if (containsHunterCapabilityMarker(villagerNbt, "villager", 0, null)
                || containsHunterCapabilityMarker(rootTag, "item", 0, null)) {
            return new HunterDetection(true, "CAPABILITY");
        }
        if (containsHunterTraitMarker(villagerNbt, "villager", 0, null)
                || containsHunterTraitMarker(rootTag, "item", 0, null)) {
            return new HunterDetection(true, "TRAIT");
        }
        if (containsHunterFlagMarker(villagerNbt, "villager", 0, null)
                || containsHunterFlagMarker(rootTag, "item", 0, null)) {
            return new HunterDetection(true, "FLAGS");
        }
        String profession = professionId(villager);
        if (isStrongHunterProfessionMarker(profession)) {
            McaVampirismCompat.debugLog(
                    "[EASY-VILLAGERS] stored hunter candidate reason=PROFESSION_HUNTER_MASTER profession={}",
                    profession
            );
            return new HunterDetection(true, "PROFESSION_HUNTER_MASTER");
        }
        if (isStrongHunterProfessionMarker(entityTypeId)
                || containsHunterProfessionMarker(villagerNbt, "villager", 0, null)
                || containsHunterProfessionMarker(rootTag, "item", 0, null)) {
            McaVampirismCompat.debugLog(
                    "[EASY-VILLAGERS] stored hunter candidate reason=NBT_HUNTER_MASTER entityType={}",
                    entityTypeId
            );
            return new HunterDetection(true, "NBT_HUNTER_MASTER");
        }
        return HunterDetection.none();
    }

    static WerewolfDetection detectWerewolfData(ItemStack storedStack, CompoundTag villagerNbt) {
        CompoundTag stateTag = McaEvStatePayload.readState(storedStack).orElse(null);
        CompoundTag rootTag = customFactionEvidenceData(storedStack);
        if (containsWerewolfCapabilityMarker(stateTag, "payload.state", 0, null)
                || containsWerewolfTraitMarker(stateTag, "payload.state", 0, null)
                || containsWerewolfMarker(stateTag, "payload.state", 0, null)) {
            return new WerewolfDetection(true, "STATE");
        }
        if (containsWerewolfCapabilityMarker(villagerNbt, "villager", 0, null)
                || containsWerewolfCapabilityMarker(rootTag, "item", 0, null)) {
            return new WerewolfDetection(true, "CAPABILITY");
        }
        if (containsWerewolfTraitMarker(villagerNbt, "villager", 0, null)
                || containsWerewolfTraitMarker(rootTag, "item", 0, null)) {
            return new WerewolfDetection(true, "TRAIT");
        }
        if (containsWerewolfMarker(villagerNbt, "villager", 0, null)
                || containsWerewolfMarker(rootTag, "item", 0, null)) {
            return new WerewolfDetection(true, "RECURSIVE");
        }
        return WerewolfDetection.none();
    }

    static CompoundTag getVillagerData(ItemStack stack) {
        CompoundTag completeMcaData = McaEvCompatBridge.copyStoredMcaVillagerDataReadOnly(stack);
        if (completeMcaData != null) {
            return completeMcaData.copy();
        }
        CompoundTag root = customData(stack);
        return root.contains(VILLAGER_TAG, Tag.TAG_COMPOUND)
                ? root.getCompound(VILLAGER_TAG).copy()
                : null;
    }

    static String resolveEntityType(ItemStack stack, CompoundTag villagerNbt) {
        String delegatedTypeId = McaEvCompatBridge.getStoredMcaEntityTypeIdReadOnly(stack);
        if (delegatedTypeId != null && !delegatedTypeId.isBlank()) {
            return delegatedTypeId;
        }
        if (villagerNbt != null && villagerNbt.contains("id")) {
            return villagerNbt.getString("id");
        }
        CompoundTag root = customData(stack);
        CompoundTag compatTag = root.contains(MCA_COMPAT_TAG, Tag.TAG_COMPOUND)
                ? root.getCompound(MCA_COMPAT_TAG)
                : null;
        if (compatTag != null && compatTag.contains(ENTITY_TYPE_KEY)) {
            return compatTag.getString(ENTITY_TYPE_KEY);
        }
        return "";
    }

    static boolean isMcaVillagerType(String entityTypeId) {
        return MCA_MALE.equals(entityTypeId) || MCA_FEMALE.equals(entityTypeId);
    }

    private static boolean containsCapabilityMarker(Tag tag, String path, int depth, List<String> candidates) {
        return containsEvidence(tag, path, depth, candidates, (node, nodePath) -> {
            if (!isLegacyStatePayload(node, nodePath)) {
                return StoredMcaNbtEvidenceScanner.ScanResult.none();
            }
            CompoundTag capabilityTag = (CompoundTag) node;
            return new StoredMcaNbtEvidenceScanner.ScanResult(
                    capabilityTag.getBoolean("Converted") || capabilityTag.getBoolean("converted")
                            || capabilityTag.getBoolean("VampireAppearanceApplied"),
                    true
            );
        });
    }

    private static boolean containsTraitMarker(Tag tag, String path, int depth, List<String> candidates) {
        return containsTraitEvidence(tag, path, depth, candidates, VAMPIRE_TRAIT_ID, "vampire");
    }

    private static boolean containsHunterCapabilityMarker(Tag tag, String path, int depth, List<String> candidates) {
        return containsEvidence(tag, path, depth, candidates, (node, nodePath) -> {
            if (!isLegacyStatePayload(node, nodePath)) {
                return StoredMcaNbtEvidenceScanner.ScanResult.none();
            }
            CompoundTag capabilityTag = (CompoundTag) node;
            return new StoredMcaNbtEvidenceScanner.ScanResult(
                    capabilityTag.getBoolean("HunterAligned")
                            || capabilityTag.getBoolean("HunterTraitApplied")
                            || capabilityTag.getBoolean("HunterVillageApplied"),
                    true
            );
        });
    }

    private static boolean containsHunterTraitMarker(Tag tag, String path, int depth, List<String> candidates) {
        return containsTraitEvidence(tag, path, depth, candidates, HUNTER_TRAIT_ID, "hunter");
    }

    private static boolean containsHunterFlagMarker(Tag tag, String path, int depth, List<String> candidates) {
        return containsEvidence(tag, path, depth, candidates, (node, nodePath) -> {
            String key = keyFromPath(nodePath).toLowerCase(Locale.ROOT);
            String value = node.getAsString().toLowerCase(Locale.ROOT);
            return isExplicitTrueHunterFlag(key, value)
                    ? StoredMcaNbtEvidenceScanner.ScanResult.match()
                    : StoredMcaNbtEvidenceScanner.ScanResult.none();
        });
    }

    private static boolean containsHunterProfessionMarker(Tag tag, String path, int depth, List<String> candidates) {
        return containsEvidence(tag, path, depth, candidates, (node, nodePath) -> {
            String lowerValue = node.getAsString().toLowerCase(Locale.ROOT);
            String lowerPath = nodePath.toLowerCase(Locale.ROOT);
            return isStrongHunterProfessionMarker(lowerValue) || isStrongHunterProfessionMarker(lowerPath)
                    ? StoredMcaNbtEvidenceScanner.ScanResult.match()
                    : StoredMcaNbtEvidenceScanner.ScanResult.none();
        });
    }

    private static boolean containsWerewolfCapabilityMarker(Tag tag, String path, int depth, List<String> candidates) {
        return containsEvidence(tag, path, depth, candidates, (node, nodePath) -> {
            if (!isLegacyStatePayload(node, nodePath)) {
                return StoredMcaNbtEvidenceScanner.ScanResult.none();
            }
            CompoundTag capabilityTag = (CompoundTag) node;
            return new StoredMcaNbtEvidenceScanner.ScanResult(
                    capabilityTag.getBoolean("WerewolfConverted")
                            || capabilityTag.getBoolean("WerewolfAppearanceApplied")
                            || capabilityTag.getBoolean("WerewolfPartialForm"),
                    true
            );
        });
    }

    private static boolean containsWerewolfTraitMarker(Tag tag, String path, int depth, List<String> candidates) {
        return containsTraitEvidence(tag, path, depth, candidates, WEREWOLF_TRAIT_ID, "werewolf");
    }

    private static boolean containsEvidence(
            Tag tag,
            String path,
            int depth,
            List<String> candidates,
            StoredMcaNbtEvidenceScanner.EvidenceRule rule
    ) {
        return StoredMcaNbtEvidenceScanner.contains(
                tag,
                path,
                depth,
                MAX_RECURSION_DEPTH,
                candidates,
                (candidatePath, candidateTag) -> addCandidate(candidates, candidatePath, candidateTag),
                rule
        );
    }

    private static boolean containsTraitEvidence(
            Tag tag,
            String path,
            int depth,
            List<String> candidates,
            String traitId,
            String traitName
    ) {
        return containsEvidence(tag, path, depth, candidates, (node, nodePath) -> {
            String lowerValue = node.getAsString().toLowerCase(Locale.ROOT);
            boolean scalarTrait = isScalarTag(node) && (lowerValue.contains(traitId)
                    || lowerValue.contains(TRAIT_NAMESPACE_MARKER) && lowerValue.contains(traitName));
            boolean canonicalTrait = isPositiveCanonicalTraitEntry(
                    keyFromPath(nodePath),
                    node,
                    traitId,
                    traitName
            );
            return scalarTrait || canonicalTrait
                    ? StoredMcaNbtEvidenceScanner.ScanResult.match()
                    : StoredMcaNbtEvidenceScanner.ScanResult.none();
        });
    }

    private static boolean isLegacyStatePayload(Tag tag, String path) {
        return tag instanceof CompoundTag && LEGACY_STATE_ATTACHMENT_ID.equals(keyFromPath(path));
    }

    private static String keyFromPath(String path) {
        int separator = path.lastIndexOf('.');
        return separator >= 0 ? path.substring(separator + 1) : path;
    }

    private static boolean containsWerewolfMarker(Tag tag, String path, int depth, List<String> candidates) {
        if (tag == null || depth > MAX_RECURSION_DEPTH) {
            return false;
        }
        String lowerValue = tag.getAsString().toLowerCase(Locale.ROOT);
        String lowerPath = path.toLowerCase(Locale.ROOT);
        if (lowerPath.contains("werewolf") || lowerPath.contains("lupus") || lowerValue.contains("werewolf") || lowerValue.contains("lupus")) {
            addCandidate(candidates, path, tag);
        }
        if (isScalarTag(tag) && (lowerValue.contains(WEREWOLF_TRAIT_ID)
                || lowerValue.contains(TRAIT_NAMESPACE_MARKER) && lowerValue.contains("werewolf"))) {
            return true;
        }
        if (tag instanceof CompoundTag compoundTag) {
            for (String key : compoundTag.getAllKeys()) {
                Tag child = compoundTag.get(key);
                String lowerKey = key.toLowerCase(Locale.ROOT);
                String childValue = child == null ? "" : child.getAsString().toLowerCase(Locale.ROOT);
                if (isExplicitTrueWerewolfFlag(lowerKey, childValue)) {
                    addCandidate(candidates, path + "." + key, child);
                    return true;
                }
                if (containsWerewolfMarker(child, path + "." + key, depth + 1, candidates)) {
                    return true;
                }
            }
        } else if (tag instanceof ListTag listTag) {
            for (int i = 0; i < listTag.size(); i++) {
                if (containsWerewolfMarker(listTag.get(i), path + "[" + i + "]", depth + 1, candidates)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean containsVampireMarker(Tag tag, String path, int depth, List<String> candidates) {
        if (tag == null || depth > MAX_RECURSION_DEPTH) {
            return false;
        }

        String value = tag.getAsString();
        String lowerValue = value.toLowerCase(Locale.ROOT);
        String lowerPath = path.toLowerCase(Locale.ROOT);
        if (isInterestingCandidate(lowerPath, lowerValue)) {
            addCandidate(candidates, path, tag);
        }
        if (isScalarTag(tag) && (lowerValue.contains(VAMPIRE_TRAIT_ID)
                || lowerValue.contains(TRAIT_NAMESPACE_MARKER) && lowerValue.contains("vampire"))) {
            return true;
        }

        if (tag instanceof CompoundTag compoundTag) {
            for (String key : compoundTag.getAllKeys()) {
                Tag child = compoundTag.get(key);
                String childPath = path + "." + key;
                String lowerKey = key.toLowerCase(Locale.ROOT);
                String childValue = child == null ? "" : child.getAsString().toLowerCase(Locale.ROOT);
                if (isInterestingCandidate(lowerKey, childValue) || isInterestingCandidate(childPath.toLowerCase(Locale.ROOT), childValue)) {
                    addCandidate(candidates, childPath, child);
                }

                if (isExplicitTrueVampireFlag(lowerKey, childValue)) {
                    return true;
                }
                if (LEGACY_STATE_ATTACHMENT_ID.equals(key) && child instanceof CompoundTag capabilityTag) {
                    if (capabilityTag.getBoolean("Converted") || capabilityTag.getBoolean("converted")
                            || capabilityTag.getBoolean("VampireAppearanceApplied")) {
                        return true;
                    }
                }
                if (containsVampireMarker(child, childPath, depth + 1, candidates)) {
                    return true;
                }
            }
            return false;
        }

        if (tag instanceof ListTag listTag) {
            for (int i = 0; i < listTag.size(); i++) {
                if (containsVampireMarker(listTag.get(i), path + "[" + i + "]", depth + 1, candidates)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isExplicitTrueVampireFlag(String lowerKey, String lowerValue) {
        boolean vampireKey = lowerKey.equals("converted")
                || lowerKey.equals("vampire")
                || lowerKey.equals("isvampire")
                || lowerKey.equals("mcavampire")
                || lowerKey.equals("vampireconverted")
                || lowerKey.contains("vampireappearanceapplied");
        return vampireKey && isTrueLike(lowerValue);
    }

    private static boolean isExplicitTrueHunterFlag(String lowerKey, String lowerValue) {
        boolean hunterKey = lowerKey.equals("hunteraligned")
                || lowerKey.equals("huntertraitapplied")
                || lowerKey.equals("huntervillageapplied")
                || lowerKey.contains("hunteraligned")
                || lowerKey.contains("huntertraitapplied")
                || lowerKey.contains("huntervillage");
        return hunterKey && isTrueLike(lowerValue);
    }

    private static boolean isExplicitTrueWerewolfFlag(String lowerKey, String lowerValue) {
        boolean werewolfKey = lowerKey.equals("werewolfconverted")
                || lowerKey.equals("werewolfappearanceapplied")
                || lowerKey.equals("werewolfpartialform")
                || lowerKey.equals("werewolftraitapplied")
                || lowerKey.contains("werewolfconverted")
                || lowerKey.contains("werewolfappearance")
                || lowerKey.contains("werewolfpartial");
        return werewolfKey && isTrueLike(lowerValue);
    }

    private static boolean isStrongHunterProfessionMarker(String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.toLowerCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
        return normalized.contains("hunter_master")
                || normalized.contains("hunter_trainer")
                || normalized.contains("hunter_expert")
                || normalized.contains("advanced_hunter")
                || normalized.contains("vampirism:hunter");
    }

    private static String professionId(Villager villager) {
        if (villager == null) {
            return "";
        }
        ResourceLocation key = BuiltInRegistries.VILLAGER_PROFESSION.getKey(villager.getVillagerData().getProfession());
        return key == null ? "" : key.toString();
    }

    private static boolean isTrueLike(String lowerValue) {
        return "true".equals(lowerValue)
                || "1".equals(lowerValue)
                || "1b".equals(lowerValue)
                || "1s".equals(lowerValue)
                || "1i".equals(lowerValue);
    }

    /**
     * MCA 7.7 stores traits as boolean entries whose keys are the trait ids,
     * for example {@code Traits:{"vampirism_mca_compat:hunter":1b}}. Reading
     * scalar values alone therefore misses the canonical trait entirely.
     */
    private static boolean isPositiveCanonicalTraitEntry(
            String key, Tag value, String canonicalTraitId, String factionName) {
        if (key == null || value == null || !isScalarTag(value)) {
            return false;
        }
        String lowerKey = key.toLowerCase(Locale.ROOT);
        boolean canonicalKey = lowerKey.equals(canonicalTraitId)
                || lowerKey.contains(TRAIT_NAMESPACE_MARKER)
                && lowerKey.contains(factionName);
        return canonicalKey && isTrueLike(value.getAsString().toLowerCase(Locale.ROOT));
    }

    private static boolean isScalarTag(Tag tag) {
        return !(tag instanceof CompoundTag) && !(tag instanceof ListTag);
    }

    private static boolean isInterestingCandidate(String lowerPathOrKey, String lowerValue) {
        return lowerPathOrKey.contains("vampire")
                || lowerPathOrKey.contains("vampir")
                || lowerPathOrKey.contains("converted")
                || lowerPathOrKey.contains("trait")
                || lowerPathOrKey.contains("hunter")
                || lowerPathOrKey.contains("werewolf")
                || lowerPathOrKey.contains("lupus")
                || lowerPathOrKey.contains("aggressive")
                || lowerPathOrKey.contains("master")
                || lowerPathOrKey.contains("faction")
                || lowerPathOrKey.contains("profession")
                || lowerPathOrKey.contains("village")
                || lowerPathOrKey.contains(MOD_ID_MARKER)
                || lowerPathOrKey.contains(TRAIT_NAMESPACE_MARKER)
                || lowerValue.contains("vampire")
                || lowerValue.contains("vampir")
                || lowerValue.contains("converted")
                || lowerValue.contains("trait")
                || lowerValue.contains("hunter")
                || lowerValue.contains("werewolf")
                || lowerValue.contains("lupus")
                || lowerValue.contains("aggressive")
                || lowerValue.contains("master")
                || lowerValue.contains("faction")
                || lowerValue.contains("profession")
                || lowerValue.contains("village")
                || lowerValue.contains(MOD_ID_MARKER)
                || lowerValue.contains(TRAIT_NAMESPACE_MARKER);
    }

    private static void addCandidate(List<String> candidates, String path, Tag tag) {
        if (candidates == null || candidates.size() >= 32) {
            return;
        }
        candidates.add(path + "=" + summarizeTag(tag));
    }

    private static String summarizeTag(Tag tag) {
        if (tag == null) {
            return "null";
        }
        String value;
        if (tag instanceof CompoundTag compoundTag) {
            value = "Compound" + compoundTag.getAllKeys();
        } else if (tag instanceof ListTag listTag) {
            value = "List(size=" + listTag.size() + ")";
        } else {
            value = tag.getAsString();
        }
        return value.length() > 180 ? value.substring(0, 180) + "..." : value;
    }

    private static void logNbtDiagnostics(BlockEntity blockEntity, ItemStack storedStack, CompoundTag villagerNbt,
                                          String entityTypeId, VampireDetection vampireDetection,
                                          HunterDetection hunterDetection, WerewolfDetection werewolfDetection) {
        if (!McaVampirismCompatConfig.enableVerboseNbtDebugLogs()) {
            return;
        }
        CompoundTag rootTag = customData(storedStack);
        List<String> candidates = new ArrayList<>();
        containsCapabilityMarker(villagerNbt, "villager", 0, candidates);
        containsCapabilityMarker(rootTag, "item", 0, candidates);
        containsTraitMarker(villagerNbt, "villager", 0, candidates);
        containsTraitMarker(rootTag, "item", 0, candidates);
        containsVampireMarker(villagerNbt, "villager", 0, candidates);
        containsVampireMarker(rootTag, "item", 0, candidates);
        containsHunterCapabilityMarker(villagerNbt, "villager", 0, candidates);
        containsHunterCapabilityMarker(rootTag, "item", 0, candidates);
        containsHunterTraitMarker(villagerNbt, "villager", 0, candidates);
        containsHunterTraitMarker(rootTag, "item", 0, candidates);
        containsHunterFlagMarker(villagerNbt, "villager", 0, candidates);
        containsHunterFlagMarker(rootTag, "item", 0, candidates);
        containsHunterProfessionMarker(villagerNbt, "villager", 0, candidates);
        containsHunterProfessionMarker(rootTag, "item", 0, candidates);
        containsWerewolfCapabilityMarker(villagerNbt, "villager", 0, candidates);
        containsWerewolfCapabilityMarker(rootTag, "item", 0, candidates);
        containsWerewolfTraitMarker(villagerNbt, "villager", 0, candidates);
        containsWerewolfTraitMarker(rootTag, "item", 0, candidates);
        containsWerewolfMarker(villagerNbt, "villager", 0, candidates);
        containsWerewolfMarker(rootTag, "item", 0, candidates);

        McaVampirismCompat.debugLog(
                "[EASY-VILLAGERS][NBT] block={} item={} id={} isVampire={} isHunter={} isWerewolf={}",
                blockEntity.getClass().getName(),
                storedStack.getItem(),
                entityTypeId,
                vampireDetection.isVampire(),
                hunterDetection.isHunter(),
                werewolfDetection.isWerewolf()
        );
        McaVampirismCompat.debugLog(
                "[EASY-VILLAGERS][NBT] rootKeys={} hasVillager={} hasLowerVillager={} hasCompatTag={}",
                keys(rootTag),
                rootTag != null && rootTag.contains("Villager"),
                rootTag != null && rootTag.contains(VILLAGER_TAG),
                rootTag != null && rootTag.contains(MCA_COMPAT_TAG)
        );
        McaVampirismCompat.debugLog(
                "[EASY-VILLAGERS][NBT] villagerKeys={} forgeCapsKeys={} traitsKeys={}",
                keys(villagerNbt),
                keys(childCompound(villagerNbt, "ForgeCaps")),
                traitKeys(villagerNbt)
        );
        McaVampirismCompat.debugLog(
                "[EASY-VILLAGERS][NBT] vampireCandidates={}",
                candidates
        );
    }

    private static CompoundTag childCompound(CompoundTag tag, String key) {
        if (tag == null || !tag.contains(key) || !(tag.get(key) instanceof CompoundTag child)) {
            return null;
        }
        return child;
    }

    private static String keys(CompoundTag tag) {
        if (tag == null) {
            return "[]";
        }
        Set<String> keys = tag.getAllKeys();
        if (keys.isEmpty()) {
            return "[]";
        }
        StringJoiner joiner = new StringJoiner(",", "[", "]");
        for (String key : keys) {
            joiner.add(key);
        }
        return joiner.toString();
    }

    private static String traitKeys(CompoundTag tag) {
        if (tag == null) {
            return "[]";
        }
        List<String> paths = new ArrayList<>();
        collectTraitPaths(tag, "villager", 0, paths);
        return paths.toString();
    }

    private static void collectTraitPaths(Tag tag, String path, int depth, List<String> paths) {
        if (tag == null || depth > MAX_RECURSION_DEPTH || paths.size() >= 16) {
            return;
        }
        if (path.toLowerCase(Locale.ROOT).contains("trait")) {
            paths.add(path + "=" + summarizeTag(tag));
        }
        if (tag instanceof CompoundTag compoundTag) {
            for (String key : compoundTag.getAllKeys()) {
                collectTraitPaths(compoundTag.get(key), path + "." + key, depth + 1, paths);
            }
        } else if (tag instanceof ListTag listTag) {
            for (int i = 0; i < listTag.size(); i++) {
                collectTraitPaths(listTag.get(i), path + "[" + i + "]", depth + 1, paths);
            }
        }
    }

    private static ResourceLocation safeResourceLocation(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return ResourceLocation.parse(value);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static CompoundTag customData(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return new CompoundTag();
        }
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private static CompoundTag customFactionEvidenceData(ItemStack stack) {
        CompoundTag root = customData(stack);
        // The versioned payload is inspected explicitly. Removing it from this
        // defensive copy prevents one flag from being counted twice by the
        // recursive legacy readers while leaving the owner stack untouched.
        root.remove(McaEvStatePayload.PAYLOAD_KEY);
        return root;
    }

    private static Method findMethod(Class<?> type, String name) throws NoSuchMethodException {
        String key = type.getName() + "#" + name;
        Method cached = REFLECTED_METHODS.get(key);
        if (cached != null) {
            return cached;
        }
        Class<?> cursor = type;
        while (cursor != null) {
            try {
                Method method = cursor.getDeclaredMethod(name);
                method.setAccessible(true);
                REFLECTED_METHODS.put(key, method);
                return method;
            } catch (NoSuchMethodException ignored) {
                cursor = cursor.getSuperclass();
            }
        }
        throw new NoSuchMethodException(name);
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        String key = type.getName() + "#" + name;
        Field cached = REFLECTED_FIELDS.get(key);
        if (cached != null) {
            return cached;
        }
        Class<?> cursor = type;
        while (cursor != null) {
            try {
                Field field = cursor.getDeclaredField(name);
                field.setAccessible(true);
                REFLECTED_FIELDS.put(key, field);
                return field;
            } catch (NoSuchFieldException ignored) {
                cursor = cursor.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static boolean isClassOrSuperclass(Class<?> type, String className) {
        Class<?> cursor = type;
        while (cursor != null) {
            if (className.equals(cursor.getName())) {
                return true;
            }
            cursor = cursor.getSuperclass();
        }
        return false;
    }

    record StackSelection(ItemStack stack, String source) {
    }

    public record StoredFactionSnapshot(
            boolean isMca,
            boolean isVampire,
            boolean isHunter,
            boolean isWerewolf,
            VampireDetection vampireDetection,
            HunterDetection hunterDetection,
            WerewolfDetection werewolfDetection,
            String entityTypeId,
            CompoundTag villagerNbt
    ) {
        public String vampireSource() {
            return vampireDetection.source();
        }

        public String hunterSource() {
            return hunterDetection.source();
        }

        public String werewolfSource() {
            return werewolfDetection.source();
        }
    }

    public record VampireDetection(boolean isVampire, String source) {
        private static VampireDetection none() {
            return new VampireDetection(false, "NONE");
        }
    }

    public record HunterDetection(boolean isHunter, String source) {
        private static HunterDetection none() {
            return new HunterDetection(false, "NONE");
        }
    }

    public record WerewolfDetection(boolean isWerewolf, String source) {
        private static WerewolfDetection none() {
            return new WerewolfDetection(false, "NONE");
        }
    }
}
