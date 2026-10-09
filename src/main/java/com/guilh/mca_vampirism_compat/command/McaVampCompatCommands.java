package com.guilh.mca_vampirism_compat.command;

import com.guilh.mca_vampirism_compat.VampiricVillagerState;
import com.guilh.mca_vampirism_compat.book.OccultVillageFactionPolicy;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.service.McaSpecialVillagerClassifier;
import com.guilh.mca_vampirism_compat.service.McaSpecialVillagerRuntimeService;
import com.guilh.mca_vampirism_compat.service.McaVillageEffectBoundsService;
import com.guilh.mca_vampirism_compat.service.McaVillageOutlineService;
import com.guilh.mca_vampirism_compat.trade.TradeCycleEligibility;
import com.guilh.mca_vampirism_compat.trait.McaTraitBridge;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import de.teamlapen.vampirism.blockentity.TotemBlockEntity;
import forge.net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** OP-only diagnostics and conservative state repair for this compat. */
public final class McaVampCompatCommands {
    private static final int OP_PERMISSION_LEVEL = 2;
    private static final int DEFAULT_OUTLINE_SECONDS = 30;
    private static final SimpleCommandExceptionType NOT_MCA_VILLAGER =
            new SimpleCommandExceptionType(Component.translatable("mca_vamp_compat.admin.error.not_mca_villager"));
    private static final DynamicCommandExceptionType MCA_VILLAGER_NOT_FOUND =
            new DynamicCommandExceptionType(name -> Component.translatable(
                    "mca_vamp_compat.admin.error.villager_not_found", name
            ));
    private static final Dynamic2CommandExceptionType MCA_VILLAGER_AMBIGUOUS =
            new Dynamic2CommandExceptionType((name, count) -> Component.translatable(
                    "mca_vamp_compat.admin.error.villager_ambiguous", name, count
            ));
    private static final SimpleCommandExceptionType NOT_ACTIVE_TOTEM =
            new SimpleCommandExceptionType(Component.translatable("mca_vamp_compat.admin.error.not_active_totem"));
    private static final SimpleCommandExceptionType PLAYER_REQUIRED =
            new SimpleCommandExceptionType(Component.translatable("mca_vamp_compat.admin.error.player_required"));

    private McaVampCompatCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("mca_vamp_compat")
                .executes(McaVampCompatCommands::showRootHelp)
                .then(Commands.literal("inspect")
                        .requires(source -> source.hasPermission(OP_PERMISSION_LEVEL))
                        .then(Commands.argument("villager", StringArgumentType.greedyString())
                                .executes(McaVampCompatCommands::inspectByReference)))
                .then(Commands.literal("resync")
                        .requires(source -> source.hasPermission(OP_PERMISSION_LEVEL))
                        .then(Commands.argument("villagers", StringArgumentType.greedyString())
                                .executes(McaVampCompatCommands::resyncByReference)))
                .then(Commands.literal("village")
                        .requires(source -> source.hasPermission(OP_PERMISSION_LEVEL))
                        .executes(McaVampCompatCommands::showVillageHelp)
                        .then(Commands.literal("info")
                                .executes(context -> villageInfo(context, null))
                                .then(Commands.argument("totem", BlockPosArgument.blockPos())
                                        .executes(context -> villageInfo(context,
                                                BlockPosArgument.getLoadedBlockPos(context, "totem")))))
                        .then(Commands.literal("outline")
                                .executes(context -> villageOutline(context, null, DEFAULT_OUTLINE_SECONDS))
                                .then(Commands.argument("totem", BlockPosArgument.blockPos())
                                        .executes(context -> villageOutline(context,
                                                BlockPosArgument.getLoadedBlockPos(context, "totem"),
                                                DEFAULT_OUTLINE_SECONDS))
                                        .then(Commands.argument("seconds", IntegerArgumentType.integer(5, 60))
                                                .executes(context -> villageOutline(context,
                                                        BlockPosArgument.getLoadedBlockPos(context, "totem"),
                                                        IntegerArgumentType.getInteger(context, "seconds"))))))));
    }

    private static int showRootHelp(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Component.translatable("mca_vamp_compat.admin.help.header"), false);
        source.sendSuccess(() -> Component.translatable("mca_vamp_compat.admin.help.inspect"), false);
        source.sendSuccess(() -> Component.translatable("mca_vamp_compat.admin.help.resync"), false);
        source.sendSuccess(() -> Component.translatable("mca_vamp_compat.admin.help.village"), false);
        return 1;
    }

    private static int showVillageHelp(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Component.translatable("mca_vamp_compat.admin.help.village_header"), false);
        source.sendSuccess(() -> Component.translatable("mca_vamp_compat.admin.help.village_info"), false);
        source.sendSuccess(() -> Component.translatable("mca_vamp_compat.admin.help.village_outline"), false);
        return 1;
    }

    private static int inspectByReference(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String reference = StringArgumentType.getString(context, "villager");
        List<VillagerEntityMCA> matches = resolveMcaVillagers(context.getSource(), reference);
        if (matches.isEmpty()) {
            throw MCA_VILLAGER_NOT_FOUND.create(reference);
        }
        if (matches.size() > 1) {
            throw MCA_VILLAGER_AMBIGUOUS.create(reference, matches.size());
        }
        return inspect(context, matches.get(0));
    }

    private static int inspect(CommandContext<CommandSourceStack> context, Entity entity) throws CommandSyntaxException {
        VillagerEntityMCA villager = requireMcaVillager(entity);
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Component.translatable("mca_vamp_compat.admin.inspect.header", villager.getName()), false);
        source.sendSuccess(() -> Component.translatable("mca_vamp_compat.admin.inspect.faction",
                McaSpecialVillagerClassifier.classify(villager).name(),
                stateOrigin(villager)), false);
        source.sendSuccess(() -> Component.translatable("mca_vamp_compat.admin.inspect.traits",
                McaTraitBridge.hasVampireTrait(villager),
                McaTraitBridge.hasHunterTrait(villager),
                McaTraitBridge.hasWerewolfTrait(villager)), false);
        source.sendSuccess(() -> Component.translatable("mca_vamp_compat.admin.inspect.profession",
                professionId(villager),
                TradeCycleEligibility.isAvailable(villager, villager.getOffers(), false)), false);
        ModCapabilities.get(villager).ifPresentOrElse(
                state -> sendStateLines(source, villager, state),
                () -> source.sendSuccess(() -> Component.translatable("mca_vamp_compat.admin.inspect.no_state"), false)
        );
        return 1;
    }

    private static void sendStateLines(CommandSourceStack source, VillagerEntityMCA villager, VampiricVillagerState state) {
        source.sendSuccess(() -> Component.translatable("mca_vamp_compat.admin.inspect.infection",
                state.isInfected(), state.isConverted(), state.isWerewolfInfected(), state.isWerewolfConverted(),
                state.getSource().map(Object::toString).orElse("-"),
                state.getWerewolfSourceUuid().map(Object::toString).orElse("-")), false);
        source.sendSuccess(() -> Component.translatable("mca_vamp_compat.admin.inspect.cure",
                state.isCuringVampire(), state.isCuringWerewolf(),
                state.getCureFinishTick(), state.getWerewolfCureFinishTick()), false);
        source.sendSuccess(() -> Component.translatable("mca_vamp_compat.admin.inspect.runtime",
                state.isVampireAppearanceApplied(), state.isWerewolfAppearanceApplied(),
                state.isVampireNightScheduleApplied(), state.getLastAppliedVampireSchedule(),
                state.isOriginalMainHandItemSaved(), villager.getMainHandItem().getHoverName()), false);
        source.sendSuccess(() -> Component.translatable("mca_vamp_compat.admin.inspect.hunter",
                state.isHunterAligned(), state.isHunterVillageApplied(), state.isDefensiveMode(),
                state.isHunterTraitApplied(), state.isFactionInheritanceProcessed()), false);
    }

    private static int resync(CommandContext<CommandSourceStack> context, Collection<? extends Entity> entities) {
        int repaired = 0;
        int skipped = 0;
        for (Entity entity : entities) {
            if (entity instanceof VillagerEntityMCA villager) {
                McaSpecialVillagerRuntimeService.synchronizeNow(villager);
                repaired++;
            } else {
                skipped++;
            }
        }
        int finalRepaired = repaired;
        int finalSkipped = skipped;
        context.getSource().sendSuccess(() -> Component.translatable(
                "mca_vamp_compat.admin.resync.result", finalRepaired, finalSkipped), false);
        return repaired;
    }

    private static int resyncByReference(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String reference = StringArgumentType.getString(context, "villagers");
        List<VillagerEntityMCA> villagers = resolveMcaVillagers(context.getSource(), reference);
        if (villagers.isEmpty()) {
            throw MCA_VILLAGER_NOT_FOUND.create(reference);
        }
        return resync(context, villagers);
    }

    private static int villageInfo(CommandContext<CommandSourceStack> context, BlockPos requestedPosition) throws CommandSyntaxException {
        TotemBlockEntity totem = resolveActiveTotem(context.getSource(), requestedPosition);
        McaVillageEffectBoundsService.VillageAreaInfo info = McaVillageEffectBoundsService.describeArea(totem)
                .orElseThrow(NOT_ACTIVE_TOTEM::create);
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Component.translatable("mca_vamp_compat.admin.village.header",
                totem.getBlockPos().toShortString(), info.factionId()), false);
        source.sendSuccess(() -> Component.translatable("mca_vamp_compat.admin.village.native",
                formatArea(info.nativeArea())), false);
        source.sendSuccess(() -> Component.translatable("mca_vamp_compat.admin.village.effective",
                formatArea(info.effectiveArea())), false);
        source.sendSuccess(() -> Component.translatable("mca_vamp_compat.admin.village.fog",
                formatFogArea(info.fogArea())), false);
        source.sendSuccess(() -> Component.translatable("mca_vamp_compat.admin.village.mca",
                info.usesMcaBounds(), info.mcaVillageId(),
                info.mcaVillageArea() == null ? "-" : formatArea(info.mcaVillageArea())), false);
        source.sendSuccess(() -> Component.translatable("mca_vamp_compat.admin.village.fog_margin",
                McaVampirismCompatConfig.mcaVillageEffectsFollowMcaBounds(),
                McaVampirismCompatConfig.mcaVillageFogBorderMarginBlocks()), false);
        return 1;
    }

    private static int villageOutline(CommandContext<CommandSourceStack> context, BlockPos requestedPosition, int seconds)
            throws CommandSyntaxException {
        ServerPlayer player = player(context.getSource());
        TotemBlockEntity totem = resolveActiveTotem(context.getSource(), requestedPosition);
        if (McaVillageEffectBoundsService.getEffectiveArea(totem) == null) {
            throw NOT_ACTIVE_TOTEM.create();
        }
        McaVillageOutlineService.start(player, totem, seconds);
        context.getSource().sendSuccess(() -> Component.translatable("mca_vamp_compat.admin.village.outline_started",
                seconds, totem.getBlockPos().toShortString()), false);
        return 1;
    }

    private static TotemBlockEntity resolveActiveTotem(CommandSourceStack source, BlockPos requestedPosition)
            throws CommandSyntaxException {
        ServerLevel level = source.getLevel();
        TotemBlockEntity totem;
        if (requestedPosition == null) {
            ServerPlayer player = player(source);
            totem = McaVillageEffectBoundsService.findActiveTotemAt(level, player.blockPosition()).orElseThrow(NOT_ACTIVE_TOTEM::create);
        } else {
            BlockEntity blockEntity = level.getBlockEntity(requestedPosition);
            if (!(blockEntity instanceof TotemBlockEntity value)) {
                throw NOT_ACTIVE_TOTEM.create();
            }
            totem = value;
        }
        if (!McaVillageEffectBoundsService.isActiveVillageTotem(totem)
                || !OccultVillageFactionPolicy.isSupportedFactionId(McaVillageEffectBoundsService.factionId(totem))) {
            throw NOT_ACTIVE_TOTEM.create();
        }
        return totem;
    }

    private static ServerPlayer player(CommandSourceStack source) throws CommandSyntaxException {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            throw PLAYER_REQUIRED.create();
        }
        return player;
    }

    private static VillagerEntityMCA requireMcaVillager(Entity entity) throws CommandSyntaxException {
        if (!(entity instanceof VillagerEntityMCA villager)) {
            throw NOT_MCA_VILLAGER.create();
        }
        return villager;
    }

    /**
     * EntityArgument treats an unprefixed word as an online player name. MCA
     * villagers are neither players nor scoreboard targets, so commands need
     * their own resolver for the display name players actually see. Selectors
     * and UUIDs remain available for unambiguous administrative use.
     */
    private static List<VillagerEntityMCA> resolveMcaVillagers(CommandSourceStack source, String reference)
            throws CommandSyntaxException {
        String value = unquoteReference(reference == null ? "" : reference.strip());
        if (value.isEmpty()) {
            return List.of();
        }
        if (value.startsWith("@")) {
            EntitySelector selector = new EntitySelectorParser(new StringReader(value), true).parse();
            List<VillagerEntityMCA> result = new ArrayList<>();
            for (Entity entity : selector.findEntities(source)) {
                if (entity instanceof VillagerEntityMCA villager && !villager.isRemoved()) {
                    result.add(villager);
                }
            }
            return result;
        }

        try {
            UUID uuid = UUID.fromString(value);
            Entity entity = source.getLevel().getEntity(uuid);
            return entity instanceof VillagerEntityMCA villager && !villager.isRemoved()
                    ? List.of(villager)
                    : List.of();
        } catch (IllegalArgumentException ignored) {
            // A normal MCA display name is the expected command input.
        }

        String normalizedReference = normalizeName(value);
        List<VillagerEntityMCA> result = new ArrayList<>();
        for (Entity entity : source.getLevel().getAllEntities()) {
            if (entity instanceof VillagerEntityMCA villager
                    && !villager.isRemoved()
                    && normalizeName(villager.getName().getString()).equals(normalizedReference)) {
                result.add(villager);
            }
        }
        return result;
    }

    /**
     * Greedy Brigadier arguments deliberately retain their original quotes.
     * Strip one matching outer pair so names such as "Seleris Vale" compare
     * to the name shown by MCA instead of including
     * the two quote characters.
     */
    private static String unquoteReference(String value) {
        if (value.length() < 2 || value.charAt(0) != '\"' || value.charAt(value.length() - 1) != '\"') {
            return value;
        }
        return value.substring(1, value.length() - 1)
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    private static String normalizeName(String value) {
        return Normalizer.normalize(value == null ? "" : value.strip(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    private static String stateOrigin(VillagerEntityMCA villager) {
        return ModCapabilities.get(villager).map(state -> {
            if (state.isConverted() || state.isWerewolfConverted() || state.isHunterAligned()) {
                return "compat_state";
            }
            if (McaTraitBridge.hasVampireTrait(villager)
                    || McaTraitBridge.hasHunterTrait(villager)
                    || McaTraitBridge.hasWerewolfTrait(villager)) {
                return "mca_trait";
            }
            return "compat_state_idle";
        }).orElse("no_compat_state");
    }

    private static String professionId(VillagerEntityMCA villager) {
        return String.valueOf(BuiltInRegistries.VILLAGER_PROFESSION.getKey(
                villager.getVillagerData().getProfession()
        ));
    }

    private static String formatArea(AABB area) {
        if (area == null) {
            return "-";
        }
        return String.format(Locale.ROOT, "[%.1f, %.1f, %.1f] -> [%.1f, %.1f, %.1f] (%.0f x %.0f)",
                area.minX, area.minY, area.minZ, area.maxX, area.maxY, area.maxZ,
                area.getXsize(), area.getZsize());
    }

    /**
     * LevelFog stores a Minecraft BoundingBox whose maximum horizontal block
     * is inclusive. This representation is therefore formatted as a block
     * range rather than as an AABB size.
     */
    private static String formatFogArea(AABB area) {
        if (area == null) {
            return "-";
        }
        return String.format(Locale.ROOT, "X %.0f..%.0f | Z %.0f..%.0f",
                area.minX, area.maxX, area.minZ, area.maxZ);
    }
}
