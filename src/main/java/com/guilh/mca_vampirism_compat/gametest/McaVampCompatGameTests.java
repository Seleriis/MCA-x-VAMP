package com.guilh.mca_vampirism_compat.gametest;

import com.mojang.authlib.GameProfile;
import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.VampiricVillagerState;
import com.guilh.mca_vampirism_compat.easyvillagers.EasyVillagersFactionMerchant;
import com.guilh.mca_vampirism_compat.easyvillagers.EasyVillagersFactionMerchantSession;
import com.guilh.mca_vampirism_compat.easyvillagers.EasyVillagersTradeCycleService;
import com.guilh.mca_vampirism_compat.easyvillagers.McaEvCompatBridge;
import com.guilh.mca_vampirism_compat.easyvillagers.McaEvStatePayload;
import com.guilh.mca_vampirism_compat.easyvillagers.readonly.StoredMcaFactionResolver;
import com.guilh.mca_vampirism_compat.easyvillagers.readonly.StoredMcaVillagerInfo;
import com.guilh.mca_vampirism_compat.faction.PlayerFaction;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.service.McaDefensiveVillagerService;
import com.guilh.mca_vampirism_compat.service.McaEditorTraitSyncService;
import com.guilh.mca_vampirism_compat.service.McaHunterAlignmentService;
import com.guilh.mca_vampirism_compat.service.McaHunterTradeService;
import com.guilh.mca_vampirism_compat.service.McaNativeWerewolfProtectionService;
import com.guilh.mca_vampirism_compat.service.McaSpecialVillagerCombatService;
import com.guilh.mca_vampirism_compat.service.McaSpecialVillagerType;
import com.guilh.mca_vampirism_compat.service.McaVampireStateService;
import com.guilh.mca_vampirism_compat.service.McaVampireFeedProtection;
import com.guilh.mca_vampirism_compat.service.McaWerewolfStateService;
import com.guilh.mca_vampirism_compat.service.McaWerewolvesCompat;
import com.guilh.mca_vampirism_compat.service.McaVillageEffectBounds;
import com.guilh.mca_vampirism_compat.trade.McaFactionTradeService;
import com.guilh.mca_vampirism_compat.trade.McaNativeTradeCycleSession;
import com.guilh.mca_vampirism_compat.trade.McaOfferPreparation;
import com.guilh.mca_vampirism_compat.trade.TradeCycleEligibility;
import com.guilh.mca_vampirism_compat.trait.McaTraitBridge;
import com.guilh.mca_vampirism_compat.mixin.accessor.VillagerTradeAccessor;
import de.teamlapen.vampirism.api.VampirismAPI;
import de.teamlapen.vampirism.api.entity.factions.IFaction;
import de.teamlapen.vampirism.api.entity.factions.IPlayableFaction;
import de.teamlapen.vampirism.api.entity.player.skills.ISkill;
import de.teamlapen.vampirism.api.entity.player.vampire.IVampirePlayer;
import de.teamlapen.vampirism.api.event.BloodDrinkEvent;
import de.teamlapen.vampirism.core.ModBlocks;
import de.teamlapen.vampirism.core.ModEntities;
import de.teamlapen.vampirism.entity.ExtendedCreature;
import de.teamlapen.vampirism.entity.vampire.DrinkBloodContext;
import net.conczin.mca.entity.EquipmentSet;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.ai.brain.tasks.EquipmentTask;
import net.conczin.mca.entity.ai.relationship.AgeState;
import net.conczin.mca.entity.ai.Traits;
import net.conczin.mca.registry.EntitiesMCA;
import net.conczin.mca.registry.ProfessionsMCA;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.ModList;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.lang.reflect.Field;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

@GameTestHolder(McaVampirismCompat.MOD_ID)
@PrefixGameTestTemplate(false)
@EventBusSubscriber(modid = McaVampirismCompat.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class McaVampCompatGameTests {
    private McaVampCompatGameTests() {
    }

    @SubscribeEvent
    public static void register(RegisterGameTestsEvent event) {
        event.register(McaVampCompatGameTests.class);
        event.register(McaRelationshipGameTests.class);
    }

    @GameTest(template = "gametest/empty")
    public static void adminCommandRootIsRegistered(GameTestHelper helper) {
        var root = helper.getLevel().getServer().getCommands().getDispatcher().getRoot().getChild("mca_vamp_compat");
        helper.assertTrue(
                root != null,
                "The level-2 MCA Vampirism Compat administrative command root must be registered"
        );
        helper.assertTrue(root.getCommand() != null,
                "Running the administrative root with no subcommand must show its help");
        var village = root.getChild("village");
        helper.assertTrue(village != null && village.getCommand() != null,
                "Running the village command group with no subcommand must show its help");
        try {
            var commands = helper.getLevel().getServer().getCommands();
            helper.assertTrue(commands.getDispatcher().execute(
                            "mca_vamp_compat",
                            helper.getLevel().getServer().createCommandSourceStack()) == 1,
                    "The administrative root help command must execute successfully for an OP source");
            helper.assertTrue(commands.getDispatcher().execute(
                            "mca_vamp_compat village",
                            helper.getLevel().getServer().createCommandSourceStack()) == 1,
                    "The village help command must execute successfully for an OP source");
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException exception) {
            helper.fail("The administrative help command should not reject an OP source: " + exception.getMessage());
            return;
        }
        helper.succeed();
    }

    @GameTest(template = "gametest/empty")
    public static void adminCommandsResolveMcaVillagerDisplayNames(GameTestHelper helper) {
        VillagerEntityMCA villager = EntitiesMCA.FEMALE_VILLAGER.create(helper.getLevel());
        helper.assertTrue(villager != null, "MCA female villager type must create an entity");
        String testName = "CommandTestSeleris" + villager.getUUID().toString().substring(0, 8);
        villager.setCustomName(Component.literal(testName + " Vale"));
        villager.moveTo(helper.absoluteVec(new Vec3(2.5D, 2.0D, 2.5D)));
        helper.getLevel().addFreshEntity(villager);

        try {
            var commands = helper.getLevel().getServer().getCommands();
            var source = helper.getLevel().getServer().createCommandSourceStack();
            helper.assertTrue(commands.getDispatcher().execute(
                            "mca_vamp_compat inspect \"" + testName + " Vale\"", source) == 1,
                    "inspect must resolve a loaded MCA villager by its visible name");
            helper.assertTrue(commands.getDispatcher().execute(
                            "mca_vamp_compat resync \"" + testName + " Vale\"", source) == 1,
                    "resync must resolve a loaded MCA villager by its visible name");
            villager.setCustomName(Component.literal(testName));
            helper.assertTrue(commands.getDispatcher().execute(
                            "mca_vamp_compat inspect " + testName, source) == 1,
                    "inspect must resolve an unquoted single-word MCA display name");
            helper.assertTrue(commands.getDispatcher().execute(
                            "mca_vamp_compat inspect " + villager.getUUID(), source) == 1,
                    "inspect must continue accepting a villager UUID");
            helper.assertTrue(commands.getDispatcher().execute(
                            "mca_vamp_compat resync @e[type=mca:female_villager,limit=1]", source) == 1,
                    "resync must continue accepting entity selectors");
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException exception) {
            helper.fail("Administrative commands must resolve MCA display names: " + exception.getMessage());
            return;
        }
        helper.succeed();
    }

    @GameTest(template = "gametest/empty")
    public static void mcaVillageEffectBoundsUseCompleteMcaFootprintAndFogMargin(GameTestHelper helper) {
        AABB result = McaVillageEffectBounds.fromVillageBox(
                new BoundingBox(-1000, 20, -1000, 1000, 100, 1000),
                new AABB(-10, 60, -10, 10, 120, 10),
                helper.getLevel().getMinBuildHeight(),
                helper.getLevel().getMaxBuildHeight()
        );
        helper.assertTrue(result != null, "An MCA village box must produce an effective bounds AABB");
        helper.assertTrue(result.getXsize() == 2001.0D && result.getZsize() == 2001.0D,
                "The complete MCA village footprint must not be capped around its totem");
        helper.assertTrue(result.minY == 60.0D && result.maxY == 120.0D,
                "MCA-derived bounds must preserve the native totem vertical limits");
        AABB fogArea = McaVillageEffectBounds.toArtificialFogArea(
                McaVillageEffectBounds.withHorizontalFogMargin(result, 4)
        );
        helper.assertTrue(fogArea.minX == -1004.0D && fogArea.maxX == 1004.0D
                        && fogArea.minZ == -1004.0D && fogArea.maxZ == 1004.0D,
                "A four-block visual fog margin must not change the logical MCA footprint");
        helper.succeed();
    }

    @GameTest(template = "gametest/empty")
    public static void mcaVillageBoundsChangesSelectOnlyAffectedTotems(GameTestHelper helper) {
        helper.assertTrue(McaVillageEffectBounds.isAffectedByVillageChange(8, 8, false),
                "A totem formerly associated with a village must refresh after that village shrinks");
        helper.assertTrue(McaVillageEffectBounds.isAffectedByVillageChange(-1, 8, true),
                "A totem newly contained by an expanded village must refresh immediately");
        helper.assertTrue(!McaVillageEffectBounds.isAffectedByVillageChange(7, 8, false),
                "A geometry update must not refresh unrelated totems");
        helper.succeed();
    }

    @GameTest(template = "gametest/empty")
    public static void specialistStationsAreValidProfessionPois(GameTestHelper helper) {
        assertSpecialistPoi(helper, ModBlocks.ALTAR_INSPIRATION.get(), id("vampirism", "vampire_expert"));
        assertSpecialistPoi(helper, ModBlocks.HUNTER_TABLE.get(), id("vampirism", "hunter_expert"));
        if (ModList.get().isLoaded("werewolves")) {
            assertSpecialistPoi(
                    helper,
                    BuiltInRegistries.BLOCK.get(id("werewolves", "stone_altar")),
                    id("werewolves", "werewolf_expert")
            );
        }
        helper.succeed();
    }

    @GameTest(template = "gametest/empty")
    public static void alternateSpecialistStationRequiresMatchingMcaFaction(GameTestHelper helper) {
        VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.create(helper.getLevel());
        helper.assertTrue(villager != null, "MCA male villager type must create an entity");

        BlockPos station = helper.absolutePos(new BlockPos(1, 1, 1));
        helper.getLevel().setBlockAndUpdate(station, ModBlocks.HUNTER_TABLE.get().defaultBlockState());
        villager.getBrain().setMemory(
                net.minecraft.world.entity.ai.memory.MemoryModuleType.JOB_SITE,
                GlobalPos.of(helper.getLevel().dimension(), station)
        );
        VillagerProfession hunterExpert = BuiltInRegistries.VILLAGER_PROFESSION.get(id("vampirism", "hunter_expert"));
        VillagerData originalData = villager.getVillagerData();
        villager.setVillagerData(originalData.setProfession(hunterExpert));
        helper.assertTrue(villager.getVillagerData().getProfession() != hunterExpert,
                "A non-Hunter MCA villager must not claim the Hunter Research Table profession");
        helper.assertTrue(villager.getBrain().getMemory(
                        net.minecraft.world.entity.ai.memory.MemoryModuleType.JOB_SITE).isEmpty(),
                "A rejected MCA villager must release the alternative job site for an eligible villager");

        McaTraitBridge.addHunterTrait(villager);
        villager.getBrain().setMemory(
                net.minecraft.world.entity.ai.memory.MemoryModuleType.JOB_SITE,
                GlobalPos.of(helper.getLevel().dimension(), station)
        );
        villager.setVillagerData(originalData.setProfession(hunterExpert));
        helper.assertTrue(villager.getVillagerData().getProfession() == hunterExpert,
                "A Hunter MCA villager must acquire Hunter Expert from the Hunter Research Table");
        helper.succeed();
    }

    @GameTest(template = "gametest/empty")
    public static void realHunterTraderAcceptsOnlyItsSpecialistStation(GameTestHelper helper) {
        if (!McaEvCompatBridge.isMcaEvCompatLoaded()
                || !BuiltInRegistries.BLOCK.containsKey(id("easy_villagers", "trader"))) {
            helper.succeed();
            return;
        }

        try {
            BlockPos traderPos = new BlockPos(1, 1, 1);
            BlockEntity trader = createRealHunterTrader(helper, traderPos, false);
            Player player = helper.makeMockPlayer(GameType.CREATIVE);

            ItemInteractionResult rejected = invokeRealTraderWorkstationUse(
                    helper, traderPos, player, new ItemStack(ModBlocks.ALTAR_INSPIRATION.get())
            );
            helper.assertTrue(rejected == ItemInteractionResult.CONSUME,
                    "A Hunter stored in a Trader must consume the interaction without consuming or placing the Vampire altar");
            Method getWorkstation = trader.getClass().getMethod("getWorkstation");
            helper.assertTrue(getWorkstation.invoke(trader) == net.minecraft.world.level.block.Blocks.AIR,
                    "A rejected specialist station must not replace the Trader workstation");

            invokeRealTraderWorkstationUse(
                    helper, traderPos, player, new ItemStack(ModBlocks.HUNTER_TABLE.get())
            );
            helper.assertTrue(getWorkstation.invoke(trader) == ModBlocks.HUNTER_TABLE.get(),
                    "A Hunter stored in a Trader must accept the Hunter Research Table");
            Method getWorkstationProfession = trader.getClass().getMethod("getWorkstationProfession");
            helper.assertTrue(getWorkstationProfession.invoke(trader)
                            == BuiltInRegistries.VILLAGER_PROFESSION.get(id("vampirism", "hunter_expert")),
                    "The accepted Trader station must resolve to Hunter Expert through its POI");
            helper.succeed();
        } catch (ReflectiveOperationException | RuntimeException exception) {
            helper.fail("Could not verify faction specialist workstation assignment in the real Trader: " + exception);
        }
    }

    @GameTest(template = "gametest/empty")
    public static void visualPayloadClassifiesStoredHunter(GameTestHelper helper) {
        CompoundTag payload = new CompoundTag();
        payload.putInt("Version", 2);
        payload.putBoolean("Vampire", false);
        payload.putBoolean("Hunter", false);
        payload.putBoolean("Werewolf", false);
        payload.put("State", new CompoundTag());
        CompoundTag villager = new CompoundTag();
        villager.putString("id", "mca:male_villager");
        CompoundTag traits = new CompoundTag();
        traits.putBoolean("vampirism_mca_compat:hunter", true);
        villager.put("Traits", traits);
        CompoundTag root = new CompoundTag();
        root.put(McaEvStatePayload.PAYLOAD_KEY, payload);
        root.put("villager", villager);
        ItemStack visualStack = new ItemStack(Items.VILLAGER_SPAWN_EGG);
        visualStack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));

        StoredMcaFactionResolver.StoredFactionSnapshot faction =
                StoredMcaFactionResolver.resolveFactionSnapshot(visualStack);
        helper.assertTrue(faction.isMca(), "Visual payload must identify an MCA villager");
        helper.assertTrue(faction.isHunter(), "A v2 false flag must not hide the canonical Hunter trait");
        helper.assertTrue(!faction.isVampire(), "Hunter payload must not become vampire");
        helper.assertTrue(!faction.isWerewolf(), "Hunter payload must not become werewolf");
        helper.succeed();
    }

    @GameTest(template = "gametest/empty")
    public static void realMcaEvTraderVisualSnapshotPreservesHunterTrait(GameTestHelper helper) {
        if (!McaEvCompatBridge.isMcaEvCompatLoaded()
                || !BuiltInRegistries.BLOCK.containsKey(id("easy_villagers", "trader"))) {
            helper.succeed();
            return;
        }

        try {
            VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.create(helper.getLevel());
            helper.assertTrue(villager != null, "MCA male villager type must create an entity");
            McaTraitBridge.addHunterTrait(villager);

            CompoundTag villagerData = new CompoundTag();
            villager.saveWithoutId(villagerData);
            villagerData.putString("id", "mca:male_villager");

            Class<?> compatLogic = Class.forName("com.guilh.mcaevcompat.CompatLogic");
            Method createStack = compatLogic.getMethod(
                    "createMcaVillagerStack",
                    CompoundTag.class,
                    CompoundTag.class
            );
            ItemStack visualStack = (ItemStack) createStack.invoke(
                    null,
                    villagerData,
                    new CompoundTag()
            );
            helper.assertTrue(!visualStack.isEmpty(), "mca-ev-compat must create a real 1.21.1 villager stack");
            addLegacyFalseFactionPayload(visualStack);

            BlockPos traderPos = new BlockPos(1, 1, 1);
            Block trader = BuiltInRegistries.BLOCK.get(id("easy_villagers", "trader"));
            helper.setBlock(traderPos, trader);
            BlockEntity blockEntity = helper.getBlockEntity(traderPos);
            Method setVillager = blockEntity.getClass().getMethod("setVillager", ItemStack.class);
            setVillager.invoke(blockEntity, visualStack);

            StoredMcaVillagerInfo info = StoredMcaFactionResolver.resolve(blockEntity);
            helper.assertTrue(info.resolved(), "The real Trader block must expose its stored villager");
            helper.assertTrue("MCA_EV_VISUAL".equals(info.stackSource()),
                    "Faction classification must use mca-ev-compat's visual snapshot");
            helper.assertTrue(info.isHunter(),
                    "The canonical Hunter trait must survive the sanitized Trader entity and v2 false payload");
            helper.assertTrue(!info.isVampire(), "The Hunter Trader must not be classified as Vampire");
            helper.assertTrue(!info.isWerewolf(), "The Hunter Trader must not be classified as Werewolf");
            helper.succeed();
        } catch (ReflectiveOperationException exception) {
            helper.fail("Could not exercise the real Easy Villagers Trader integration: " + exception);
        }
    }

    @GameTest(template = "gametest/empty")
    public static void realHunterTraderBlocksVampireAndDiscountsHunter(GameTestHelper helper) {
        if (!McaEvCompatBridge.isMcaEvCompatLoaded()
                || !BuiltInRegistries.BLOCK.containsKey(id("easy_villagers", "trader"))) {
            helper.succeed();
            return;
        }

        try {
            BlockEntity trader = createRealHunterTrader(helper, new BlockPos(1, 1, 1), true);

            Player vampire = helper.makeMockPlayer(GameType.CREATIVE);
            setPlayerFaction(vampire, id("vampirism", "vampire"));
            boolean vampireInteractionHandled = invokeRealTraderBlockOpenGui(
                    helper,
                    trader,
                    vampire,
                    new BlockPos(1, 1, 1)
            );
            helper.assertTrue(vampireInteractionHandled,
                    "The real TraderBlock.openGUI hook must consume a blocked Vampire interaction");
            helper.assertTrue(vampire.containerMenu == vampire.inventoryMenu,
                    "A Vampire must not receive a trading menu from a stored Hunter");

            ServerPlayer hunter = new ServerPlayer(
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    new GameProfile(UUID.randomUUID(), "test-hunter-trader"),
                    ClientInformation.createDefault()
            );
            StoredMcaVillagerInfo info = StoredMcaFactionResolver.resolve(trader);
            EasyVillagersFactionMerchantSession session =
                    EasyVillagersFactionMerchantSession.createForHunter(
                            hunter,
                            info,
                            PlayerFaction.HUNTER
                    );
            helper.assertTrue(session.offers().size() == 1,
                    "The wrapper must expose the real stored villager offer");
            helper.assertTrue(session.offers().getFirst().getCostA().getCount() == 17,
                    "The default 15% Hunter discount must reduce 20 emeralds to 17");
            helper.assertTrue(session.isCycleTradesAvailable(),
                    "A fresh stored Hunter must expose Easy Villagers' Cycle Trades action");
            EasyVillagersFactionMerchant merchant = new EasyVillagersFactionMerchant(session);
            merchant.setTradingPlayer(hunter);
            MerchantMenu merchantMenu = new MerchantMenu(1, hunter.getInventory(), merchant);
            merchantMenu.setSelectionHint(0);
            merchantMenu.getSlot(0).set(new ItemStack(Items.EMERALD, 17));
            merchantMenu.slotsChanged(hunter.getInventory());
            helper.assertTrue(merchantMenu.getSlot(2).hasItem(),
                    "The discounted Hunter offer must produce a trade result");
            ItemStack quickMovedTrade = merchantMenu.quickMoveStack(hunter, 2);
            helper.assertTrue(!quickMovedTrade.isEmpty(),
                    "Shift-clicking the result must complete without casting the wrapper to Entity");
            helper.assertTrue(session.offers().getFirst().getUses() == 1,
                    "The completed wrapper trade must update the real stored offer");
            helper.assertTrue(!session.isCycleTradesAvailable(),
                    "Cycle Trades must be disabled immediately after the first completed trade");
            session.close("GameTest completed");

            StoredMcaVillagerInfo reopenedInfo = StoredMcaFactionResolver.resolve(trader);
            EasyVillagersFactionMerchantSession reopenedSession =
                    EasyVillagersFactionMerchantSession.createForHunter(
                            hunter,
                            reopenedInfo,
                            PlayerFaction.HUNTER
                    );
            helper.assertTrue(!reopenedSession.isCycleTradesAvailable(),
                    "Cycle Trades must remain disabled after closing and reopening the stored Trader");
            helper.assertTrue(reopenedSession.offers().getFirst().getUses() == 1,
                    "The reopened Trader must preserve the completed offer use");
            reopenedSession.close("GameTest reopened-session check completed");
            helper.succeed();
        } catch (ReflectiveOperationException | RuntimeException exception) {
            helper.fail("Could not exercise the real Hunter Trader policy: " + exception);
        }
    }

    @GameTest(template = "gametest/empty")
    public static void cycleTradesMessageRecoversMissingNativeMcaSession(GameTestHelper helper) {
        if (!ModList.get().isLoaded("easy_villagers")) {
            helper.succeed();
            return;
        }

        VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.create(helper.getLevel());
        helper.assertTrue(villager != null, "MCA male villager type must create an entity");
        villager.setVillagerData(villager.getVillagerData()
                .setProfession(VillagerProfession.FARMER)
                .setLevel(2));
        villager.setVillagerXp(10);
        villager.getOffers().add(new MerchantOffer(
                new ItemCost(Items.EMERALD, 20),
                new ItemStack(Items.BREAD),
                12,
                1,
                0.05F
        ));

        ServerPlayer player = new ServerPlayer(
                helper.getLevel().getServer(),
                helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "test-native-cycle-recovery"),
                ClientInformation.createDefault()
        );
        player.containerMenu = new MerchantMenu(1, player.getInventory(), villager);

        helper.assertTrue(!McaNativeTradeCycleSession.hasActiveSessionFor(player, villager),
                "The recovery regression must begin without a native MCA Cycle Trades session");
        helper.assertTrue(EasyVillagersTradeCycleService.handleCycleTrades(player),
                "The server packet dispatcher must recover a real MCA merchant session");
        helper.assertTrue(McaNativeTradeCycleSession.hasActiveSessionFor(player, villager),
                "The recovered real MCA merchant must remain owned by the compat session");

        McaNativeTradeCycleSession.closeForPlayer(player.getUUID(), "GameTest packet recovery complete");
        player.containerMenu = player.inventoryMenu;
        helper.succeed();
    }

    @GameTest(template = "gametest/empty")
    public static void realMcaDirectTradeEntryBlocksVampireForHunter(GameTestHelper helper) {
        VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.create(helper.getLevel());
        helper.assertTrue(villager != null, "MCA male villager type must create an entity");
        McaTraitBridge.addHunterTrait(villager);
        villager.getOffers().add(new MerchantOffer(
                new ItemCost(Items.EMERALD, 20),
                new ItemStack(Items.BREAD),
                12,
                1,
                0.05F
        ));
        helper.assertTrue(McaHunterTradeService.isHunterTradeVillager(villager),
                "The live MCA villager must be classified as a Hunter before trading");

        Player vampire = helper.makeMockPlayer(GameType.CREATIVE);
        setPlayerFaction(vampire, id("vampirism", "vampire"));
        ((VillagerTradeAccessor) (Object) villager).mcaVampCompat$startTrading(vampire);

        helper.assertTrue(vampire.containerMenu == vampire.inventoryMenu,
                "Vanilla Villager.startTrading must not bypass Hunter refusal for a Vampire");
        helper.succeed();
    }

    @GameTest(template = "gametest/empty")
    public static void realMcaHunterShiftTradeUsesNativeOffer(GameTestHelper helper) {
        VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.create(helper.getLevel());
        helper.assertTrue(villager != null, "MCA male villager type must create an entity");
        McaTraitBridge.addHunterTrait(villager);
        MerchantOffer offer = new MerchantOffer(
                new ItemCost(Items.EMERALD, 20),
                new ItemStack(Items.BREAD, 8),
                12,
                1,
                0.05F
        );
        villager.getOffers().add(offer);
        int nativeSpecialPrice = offer.getSpecialPriceDiff();

        ServerPlayer hunter = new ServerPlayer(
                helper.getLevel().getServer(),
                helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "test-native-hunter-trade"),
                ClientInformation.createDefault()
        );
        setPlayerFaction(hunter, id("vampirism", "hunter"));
        // GameTest mock players have no packet connection, so opening MCA's
        // screen would fail while sending its client menu packet. Build the
        // same native MerchantMenu after applying the production session.
        villager.setTradingPlayer(hunter);
        McaFactionTradeService.prepareNativeMcaTrade(hunter, villager);
        MerchantMenu merchantMenu = new MerchantMenu(1, hunter.getInventory(), villager);
        helper.assertTrue(merchantMenu.getOffers().getFirst() == offer,
                "The native MCA menu must transact with the persistent offer instance");
        helper.assertTrue(merchantMenu.getOffers().getFirst().getCostA().getCount() == 17,
                "The native Hunter menu must still apply the 15% faction discount");

        merchantMenu.setSelectionHint(0);
        merchantMenu.getSlot(0).set(new ItemStack(Items.EMERALD, 17));
        merchantMenu.slotsChanged(hunter.getInventory());
        helper.assertTrue(merchantMenu.getSlot(2).hasItem(),
                "The discounted native offer must produce a result");
        ItemStack shifted = merchantMenu.quickMoveStack(hunter, 2);
        helper.assertTrue(!shifted.isEmpty(), "Shift-clicking a native faction trade must complete");
        helper.assertTrue(merchantMenu.getSlot(0).getItem().isEmpty(),
                "A completed trade must consume its exact input before the menu is closed");
        helper.assertTrue(offer.getUses() == 1,
                "The persistent MCA offer must receive exactly one use");
        helper.assertTrue(countInventory(hunter, Items.BREAD) == 8,
                "Shift-clicking must grant exactly the offered stackable output");

        merchantMenu.removed(hunter);
        helper.assertTrue(countInventory(hunter, Items.EMERALD) == 0,
                "Closing the menu must not return payment from a completed native faction trade");
        helper.assertTrue(offer.getSpecialPriceDiff() == nativeSpecialPrice,
                "Closing the native menu must restore only the temporary faction price overlay");
        helper.succeed();
    }

    @GameTest(template = "gametest/empty")
    public static void mcaOfferPreparationReappliesHeroOfTheVillage(GameTestHelper helper) {
        VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.create(helper.getLevel());
        helper.assertTrue(villager != null, "MCA male villager type must create an entity");
        MerchantOffer offer = new MerchantOffer(
                new ItemCost(Items.EMERALD, 20),
                new ItemStack(Items.BREAD),
                12,
                1,
                0.05F
        );
        villager.getOffers().add(offer);

        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        player.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 200, 0));

        helper.assertTrue(McaOfferPreparation.prepare(villager, player),
                "Cycle Trades must invoke MCA's native offer preparation");
        helper.assertTrue(offer.getSpecialPriceDiff() == -6,
                "Hero of the Village must still apply MCA's native discount after a reroll");
        helper.succeed();
    }

    @GameTest(template = "gametest/empty")
    public static void mcaEvCaptureRoundTripKeepsCycleTradesForUntradedProfessionals(GameTestHelper helper) {
        if (!McaEvCompatBridge.isMcaEvCompatLoaded()
                || !BuiltInRegistries.ITEM.containsKey(id("easy_villagers", "villager"))) {
            helper.succeed();
            return;
        }

        try {
            VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.create(helper.getLevel());
            helper.assertTrue(villager != null, "MCA male villager type must create an entity");
            villager.setVillagerData(villager.getVillagerData().setProfession(ProfessionsMCA.GUARD));
            villager.setVillagerXp(8);
            // This is a manually assigned Hunter guard. Unlike a temporary
            // Hunter-village assignment it must survive being moved outside
            // the village area.
            McaTraitBridge.addHunterTrait(villager);
            villager.getOffers().add(new MerchantOffer(
                    new ItemCost(Items.EMERALD, 4),
                    new ItemStack(Items.BREAD),
                    12,
                    1,
                    0.05F
            ));

            Class<?> compatLogic = Class.forName("com.guilh.mcaevcompat.CompatLogic");
            Method save = compatLogic.getMethod(
                    "saveMcaVillagerToStack",
                    ItemStack.class,
                    Villager.class,
                    long.class
            );
            Method restore = compatLogic.getMethod(
                    "createStoredMcaVillagerEntity",
                    net.minecraft.world.level.Level.class,
                    ItemStack.class
            );

            ItemStack untradedStack = new ItemStack(BuiltInRegistries.ITEM.get(id("easy_villagers", "villager")));
            boolean savedUntraded = (Boolean) save.invoke(
                    null,
                    untradedStack,
                    villager,
                    helper.getLevel().getGameTime()
            );
            helper.assertTrue(savedUntraded, "mca-ev-compat must serialize a professional MCA villager");
            Villager restoredUntraded = (Villager) restore.invoke(null, helper.getLevel(), untradedStack);
            helper.assertTrue(restoredUntraded != null, "mca-ev-compat must restore the captured MCA villager");
            // The real capture round-trip may normalize profession XP. Set a
            // deliberately high value after restoration to verify that the
            // Cycle Trades policy no longer treats XP as a completed trade.
            restoredUntraded.setVillagerXp(8);
            helper.assertTrue(TradeCycleEligibility.isAvailable(
                            restoredUntraded,
                            restoredUntraded.getOffers(),
                            false
                    ),
                    "An untraded professional villager must retain Cycle Trades after capture and placement");

            TradeCycleEligibility.lockAfterTrade(restoredUntraded);
            ItemStack tradedStack = new ItemStack(BuiltInRegistries.ITEM.get(id("easy_villagers", "villager")));
            boolean savedTraded = (Boolean) save.invoke(
                    null,
                    tradedStack,
                    restoredUntraded,
                    helper.getLevel().getGameTime()
            );
            helper.assertTrue(savedTraded, "mca-ev-compat must serialize the post-trade lock");
            Villager restoredTraded = (Villager) restore.invoke(null, helper.getLevel(), tradedStack);
            helper.assertTrue(restoredTraded != null, "mca-ev-compat must restore the locked MCA villager");
            helper.assertTrue(restoredTraded.getPersistentData().getBoolean("McaVampCompatTradeCycleLocked"),
                    "The permanent Cycle Trades lock must survive capture and placement");
            helper.assertTrue(!TradeCycleEligibility.isAvailable(
                            restoredTraded,
                            restoredTraded.getOffers(),
                            false
                    ),
                    "A villager that has traded must remain unable to reroll after capture and placement");

            Method spawn = compatLogic.getDeclaredMethod(
                    "spawnStoredMcaVillager",
                    ServerLevel.class,
                    ItemStack.class,
                    BlockPos.class,
                    float.class,
                    String.class
            );
            spawn.setAccessible(true);
            BlockPos placementPos = helper.absolutePos(new BlockPos(5, 2, 5));
            boolean placed = (Boolean) spawn.invoke(
                    null,
                    helper.getLevel(),
                    untradedStack,
                    placementPos,
                    0.0F,
                    "gametest"
            );
            helper.assertTrue(placed, "mca-ev-compat must place the captured MCA guard");
            // MCA x EV creates a fresh entity UUID when it places the stored
            // villager. UUID identity is owned by that mod; this integration
            // must prove that its own faction and Cycle Trades payloads made
            // the round-trip without requiring a third-party UUID policy.
            VillagerEntityMCA placedGuard = helper.getLevel()
                    .getEntitiesOfClass(VillagerEntityMCA.class, new AABB(placementPos).inflate(2.0D))
                    .stream()
                    .findFirst()
                    .orElse(null);
            helper.assertTrue(placedGuard != null, "mca-ev-compat must spawn an MCA guard at the placement position");
            helper.assertTrue(McaTraitBridge.hasHunterTrait(placedGuard),
                    "Actual item placement must restore the Hunter trait before MCA initializes the villager");
            helper.assertTrue(McaHunterTradeService.isHunterTradeVillager(placedGuard),
                    "The placed guard must still open the faction-aware Hunter trade session");
            helper.assertTrue(TradeCycleEligibility.isAvailable(placedGuard, placedGuard.getOffers(), false),
                    "An untraded placed Hunter guard must keep Cycle Trades available");
            helper.succeed();
        } catch (ReflectiveOperationException | RuntimeException exception) {
            helper.fail("Could not verify the mca-ev-compat Cycle Trades capture round-trip: " + exception);
        }
    }

    @GameTest(template = "gametest/empty")
    public static void hunterWeaponSelectionSurvivesMcaEquipmentRefresh(GameTestHelper helper) {
        try {
            VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.create(helper.getLevel());
            helper.assertTrue(villager != null, "MCA male villager type must create an entity");
            villager.setAgeState(AgeState.ADULT);
            villager.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
            ModCapabilities.get(villager).orElseThrow().setHunterVillageApplied(true);
            McaDefensiveVillagerService.reconcileHunterWeapon(villager);

            ItemStack weapon = villager.getItemInHand(InteractionHand.MAIN_HAND).copy();
            ResourceLocation weaponId = BuiltInRegistries.ITEM.getKey(weapon.getItem());
            helper.assertTrue(id("vampirism", "pitchfork").equals(weaponId)
                            || id("vampirism", "stake").equals(weaponId),
                    "A valid Hunter must receive a Vampirism pitchfork or stake");
            String selection = ModCapabilities.get(villager).orElseThrow().getHunterWeaponSelection();
            helper.assertTrue(!selection.isEmpty(), "The Hunter weapon selection must be persisted");
            helper.assertTrue(ModCapabilities.get(villager).orElseThrow().isOriginalMainHandItemSaved(),
                    "Equipping the pitchfork must preserve the original main hand before MCA refreshes equipment");

            // This is the same MCA behavior that clears a hand during an equipment refresh.
            EquipmentTask task = new EquipmentTask(ignored -> true, ignored -> EquipmentSet.GUARD_0);
            Method start = EquipmentTask.class.getDeclaredMethod(
                    "start",
                    ServerLevel.class,
                    VillagerEntityMCA.class,
                    long.class
            );
            start.setAccessible(true);
            start.invoke(task, helper.getLevel(), villager, helper.getLevel().getGameTime());

            helper.assertTrue(ItemStack.isSameItemSameComponents(
                            villager.getItemInHand(InteractionHand.MAIN_HAND), weapon),
                    "EquipmentTask must not reroll or remove a valid Hunter weapon");
            helper.assertTrue(selection.equals(ModCapabilities.get(villager).orElseThrow().getHunterWeaponSelection()),
                    "Equipment refresh must keep the persisted Hunter weapon selection");
            helper.assertTrue(ModCapabilities.get(villager).orElseThrow().isOriginalMainHandItemSaved(),
                    "Equipment refresh must not replace or clear the preserved original hand");

            // Invoke MCA's real recovery entry point. Previous releases
            // allowed its inventory food handoff and then re-equipped the
            // pitchfork later, producing an empty-hand packet in between.
            // The reserved Hunter hand must now remain untouched while the
            // same real inventory food is consumed through MCA's eat routine.
            villager.setHealth(Math.max(1.0F, villager.getMaxHealth() - 5.0F));
            villager.getInventory().addItem(new ItemStack(Items.BREAD));
            int breadBefore = countItem(villager, Items.BREAD);
            float healthBeforeRecovery = villager.getHealth();
            Method startRecovery = VillagerEntityMCA.class.getDeclaredMethod("startRecoveryFoodUse");
            startRecovery.setAccessible(true);
            boolean startedRecovery = (Boolean) startRecovery.invoke(villager);
            helper.assertTrue(startedRecovery,
                    "A Hunter with real MCA inventory food must consume that food without using the weapon hand");
            helper.assertTrue(ItemStack.isSameItemSameComponents(
                            villager.getItemInHand(InteractionHand.MAIN_HAND), weapon),
                    "MCA recovery must not move food into a persistent Hunter weapon hand");
            helper.assertTrue(countItem(villager, Items.BREAD) == breadBefore - 1,
                    "Hunter recovery must consume exactly one real inventory food item");
            float expectedRecoveryHealth = Math.min(villager.getMaxHealth(), healthBeforeRecovery + 5.0F);
            helper.assertTrue(Math.abs(villager.getHealth() - expectedRecoveryHealth) < 0.01F,
                    "MCA's own food-eat routine must apply exactly one food recovery");
            Field recoveryFoodUseActive = VillagerEntityMCA.class.getDeclaredField("recoveryFoodUseActive");
            recoveryFoodUseActive.setAccessible(true);
            helper.assertTrue(!recoveryFoodUseActive.getBoolean(villager),
                    "Direct Hunter recovery must not schedule MCA's stale hand-restoration lifecycle");
            Method tickRecovery = VillagerEntityMCA.class.getDeclaredMethod("tickRecoveryFoodUse");
            tickRecovery.setAccessible(true);
            tickRecovery.invoke(villager);
            helper.assertTrue(Math.abs(villager.getHealth() - expectedRecoveryHealth) < 0.01F,
                    "A following MCA recovery tick must not apply a second Hunter recovery");
            helper.assertTrue(ItemStack.isSameItemSameComponents(
                            villager.getItemInHand(InteractionHand.MAIN_HAND), weapon),
                    "The selected Hunter weapon must stay equipped after recovery");

            // A real Hunter can also enter the temporary defensive mode.  That mode must
            // never take ownership of, restore, or reroll the persistent Hunter weapon.
            VampiricVillagerState hunterState = ModCapabilities.get(villager).orElseThrow();
            hunterState.enterDefensiveMode(helper.getLevel().getGameTime(), 1_000L, "gametest", null);
            helper.assertTrue(!McaDefensiveVillagerService.isTemporaryDefenseOnly(villager, hunterState),
                    "A real Hunter must not be treated as a temporary-defense-only weapon owner");
            McaDefensiveVillagerService.reconcileHunterWeapon(villager);
            helper.assertTrue(ItemStack.isSameItemSameComponents(
                            villager.getItemInHand(InteractionHand.MAIN_HAND), weapon),
                    "Temporary defense must not replace a real Hunter weapon");
            McaDefensiveVillagerService.clearDefense(villager, "gametest");
            helper.assertTrue(ItemStack.isSameItemSameComponents(
                            villager.getItemInHand(InteractionHand.MAIN_HAND), weapon),
                    "Leaving temporary defense must not remove a real Hunter weapon");
            helper.succeed();
        } catch (ReflectiveOperationException | RuntimeException exception) {
            helper.fail("Could not verify Hunter pitchfork recovery after MCA equipment refresh: " + exception);
        }
    }

    @GameTest(template = "gametest/empty")
    public static void hunterWeaponSurvivesAllEquipmentWrites(GameTestHelper helper) {
        VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.create(helper.getLevel());
        helper.assertTrue(villager != null, "MCA male villager type must create an entity");
        villager.setAgeState(AgeState.ADULT);
        InteractionHand weaponHand = villager.getDominantHand();
        InteractionHand opposingHand = weaponHand == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;
        villager.setItemInHand(weaponHand, new ItemStack(Items.BREAD));
        villager.setItemInHand(opposingHand, new ItemStack(Items.STICK));
        VampiricVillagerState state = ModCapabilities.get(villager).orElseThrow();
        state.setHunterVillageApplied(true);
        state.setHunterWeaponSelection("stake");

        McaDefensiveVillagerService.reconcileHunterWeapon(villager);
        helper.assertTrue(id("vampirism", "stake").equals(
                        BuiltInRegistries.ITEM.getKey(villager.getItemInHand(weaponHand).getItem())),
                "A Hunter must hold the selected weapon in MCA's combat hand");

        // Emulate a hand item left by an older MCA recovery cycle. The
        // compat-owned scope is the only legal way to cross the reservation;
        // reconciliation must put that real item back into MCA inventory and
        // restore the weapon without an empty-hand transition.
        McaDefensiveVillagerService.runWithAuthorizedHunterWeaponMutation(
                () -> villager.setItemInHand(weaponHand, new ItemStack(Items.CARROT))
        );
        McaDefensiveVillagerService.reconcileHunterWeapon(villager);
        boolean migratedRecoveryItem = false;
        for (int inventorySlot = 0; inventorySlot < villager.getInventory().getContainerSize(); inventorySlot++) {
            if (villager.getInventory().getItem(inventorySlot).is(Items.CARROT)) {
                migratedRecoveryItem = true;
                break;
            }
        }
        helper.assertTrue(migratedRecoveryItem,
                "A legacy recovery item in the reserved hand must return to MCA inventory");

        ItemStack weapon = villager.getItemInHand(weaponHand).copy();
        villager.setItemInHand(weaponHand, ItemStack.EMPTY);
        helper.assertTrue(ItemStack.isSameItemSameComponents(villager.getItemInHand(weaponHand), weapon),
                "A persistent Hunter weapon must reject direct slot clears");
        EquipmentSlot weaponSlot = weaponHand == InteractionHand.MAIN_HAND
                ? EquipmentSlot.MAINHAND
                : EquipmentSlot.OFFHAND;
        villager.setItemSlot(weaponSlot, new ItemStack(Items.BREAD));
        helper.assertTrue(ItemStack.isSameItemSameComponents(villager.getItemInHand(weaponHand), weapon),
                "A direct Mob#setItemSlot food write must not displace a persistent Hunter weapon");
        ItemStack changedWeapon = weapon.copy();
        if (changedWeapon.isDamageableItem()) {
            changedWeapon.setDamageValue(Math.min(1, changedWeapon.getMaxDamage()));
        }
        villager.setItemSlot(weaponSlot, changedWeapon);
        helper.assertTrue(villager.getItemInHand(weaponHand).is(changedWeapon.getItem()),
                "A same-item durability/component update must remain allowed for the reserved weapon");
        ItemStack expectedWeaponAfterRuntimeUpdate = villager.getItemInHand(weaponHand).copy();
        try {
            EquipmentTask task = new EquipmentTask(ignored -> true, ignored -> EquipmentSet.GUARD_0);
            Method start = EquipmentTask.class.getDeclaredMethod(
                    "start",
                    ServerLevel.class,
                    VillagerEntityMCA.class,
                    long.class
            );
            start.setAccessible(true);
            start.invoke(task, helper.getLevel(), villager, helper.getLevel().getGameTime());
        } catch (ReflectiveOperationException exception) {
            helper.fail("Could not execute the MCA NeoForge equipment refresh: " + exception);
            return;
        }
        helper.assertTrue(ItemStack.isSameItemSameComponents(
                        villager.getItemInHand(weaponHand), expectedWeaponAfterRuntimeUpdate),
                "MCA equipment refresh must not replace the persistent Hunter weapon after a runtime update");

        state.setHunterVillageApplied(false);
        McaDefensiveVillagerService.reconcileHunterWeapon(villager);
        helper.assertTrue(villager.getItemInHand(weaponHand).is(Items.BREAD),
                "Removing Hunter ownership must restore the original dominant-hand item");
        helper.succeed();
    }

    @GameTest(template = "gametest/empty")
    public static void hunterWeaponSurvivesMcaEvInventoryViewerWrites(GameTestHelper helper) {
        if (!McaEvCompatBridge.isMcaEvCompatLoaded()
                || !BuiltInRegistries.BLOCK.containsKey(id("easy_villagers", "inventory_viewer"))
                || !BuiltInRegistries.ITEM.containsKey(id("easy_villagers", "villager"))) {
            helper.succeed();
            return;
        }

        try {
            VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.create(helper.getLevel());
            helper.assertTrue(villager != null, "MCA male villager type must create an entity");
            villager.setAgeState(AgeState.ADULT);
            InteractionHand weaponHand = villager.getDominantHand();
            EquipmentSlot weaponSlot = weaponHand == InteractionHand.MAIN_HAND
                    ? EquipmentSlot.MAINHAND
                    : EquipmentSlot.OFFHAND;
            ModCapabilities.get(villager).orElseThrow().setHunterVillageApplied(true);
            ModCapabilities.get(villager).orElseThrow().setHunterWeaponSelection("stake");
            McaDefensiveVillagerService.reconcileHunterWeapon(villager);
            ItemStack weapon = villager.getItemBySlot(weaponSlot).copy();

            Class<?> compatLogic = Class.forName("com.guilh.mcaevcompat.CompatLogic");
            Method save = compatLogic.getMethod(
                    "saveMcaVillagerToStack",
                    ItemStack.class,
                    Villager.class,
                    long.class
            );
            ItemStack storedVillager = new ItemStack(BuiltInRegistries.ITEM.get(id("easy_villagers", "villager")));
            boolean saved = (Boolean) save.invoke(null, storedVillager, villager, helper.getLevel().getGameTime());
            helper.assertTrue(saved, "MCA x Easy Villagers must serialize the persistent Hunter");

            BlockPos viewerPos = new BlockPos(7, 1, 1);
            helper.setBlock(viewerPos, BuiltInRegistries.BLOCK.get(id("easy_villagers", "inventory_viewer")));
            BlockEntity viewer = helper.getBlockEntity(viewerPos);
            helper.assertTrue(viewer != null, "The Inventory Viewer block must create its block entity");
            viewer.getClass().getMethod("setVillager", ItemStack.class).invoke(viewer, storedVillager);
            Container inventory = createMcaEvInventoryViewerContainer(viewer);
            int weaponContainerSlot = weaponHand == InteractionHand.MAIN_HAND ? 31 : 32;

            helper.assertTrue(ItemStack.isSameItemSameComponents(inventory.getItem(weaponContainerSlot), weapon),
                    "The stored Hunter must expose its selected weapon through the real Inventory Viewer backing");
            inventory.setItem(weaponContainerSlot, new ItemStack(Items.BREAD));
            helper.assertTrue(ItemStack.isSameItemSameComponents(inventory.getItem(weaponContainerSlot), weapon),
                    "Inventory Viewer replacement must not displace the persistent Hunter weapon");
            helper.assertTrue(inventory.removeItem(weaponContainerSlot, 1).isEmpty(),
                    "Inventory Viewer removal must be read-only for the persistent Hunter weapon");
            helper.assertTrue(ItemStack.isSameItemSameComponents(inventory.getItem(weaponContainerSlot), weapon),
                    "Inventory Viewer removal must leave the persistent Hunter weapon equipped");

            inventory.clearContent();
            helper.assertTrue(ItemStack.isSameItemSameComponents(inventory.getItem(weaponContainerSlot), weapon),
                    "Inventory Viewer cleanup must preserve the persistent Hunter weapon");
            boolean backingExposed = false;
            for (int slot = 0; slot < 27; slot++) {
                if (ItemStack.isSameItemSameComponents(inventory.getItem(slot), weapon)) {
                    backingExposed = true;
                    break;
                }
            }
            helper.assertTrue(!backingExposed,
                    "The Inventory Viewer must not expose the Hunter weapon's mirrored backing as a second item");
            Field storedVillagerField = inventory.getClass().getDeclaredField("villager");
            storedVillagerField.setAccessible(true);
            VillagerEntityMCA viewerVillager = (VillagerEntityMCA) storedVillagerField.get(inventory);
            int physicalBackings = countItem(viewerVillager, weapon.getItem());
            helper.assertTrue(physicalBackings == 1,
                    "The Viewer must retain exactly one hidden backing unit for the persistent Hunter weapon");
            helper.succeed();
        } catch (ReflectiveOperationException | RuntimeException exception) {
            helper.fail("Could not verify MCA x Easy Villagers Hunter Inventory Viewer protection: " + exception);
        }
    }

    @GameTest(template = "gametest/empty")
    public static void mcaEvConverterConsumesIngredientsWhenMcaConversionCompletes(GameTestHelper helper) {
        if (!McaEvCompatBridge.isMcaEvCompatLoaded()
                || !BuiltInRegistries.BLOCK.containsKey(id("easy_villagers", "converter"))
                || !BuiltInRegistries.ITEM.containsKey(id("easy_villagers", "villager"))) {
            helper.succeed();
            return;
        }

        try {
            VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.create(helper.getLevel());
            helper.assertTrue(villager != null, "MCA male villager type must create an entity");
            CompoundTag villagerData = new CompoundTag();
            villager.saveWithoutId(villagerData);
            villagerData.putString("id", "mca:male_villager");

            Class<?> compatLogic = Class.forName("com.guilh.mcaevcompat.CompatLogic");
            Method createStack = compatLogic.getMethod(
                    "createMcaVillagerStack",
                    CompoundTag.class,
                    CompoundTag.class
            );
            ItemStack storedVillager = (ItemStack) createStack.invoke(null, villagerData, new CompoundTag());
            helper.assertTrue(!storedVillager.isEmpty(),
                    "MCA x Easy Villagers must create a stored MCA villager for Converter testing");

            BlockPos converterPos = new BlockPos(9, 1, 1);
            helper.setBlock(converterPos, BuiltInRegistries.BLOCK.get(id("easy_villagers", "converter")));
            BlockEntity converter = helper.getBlockEntity(converterPos);
            helper.assertTrue(converter != null, "The Converter block must create its block entity");
            Container input = (Container) converter.getClass().getMethod("getInputInventory").invoke(converter);
            Container output = (Container) converter.getClass().getMethod("getOutputInventory").invoke(converter);
            ItemStack weaknessPotion = PotionContents.createItemStack(Items.POTION, Potions.WEAKNESS);
            input.setItem(0, storedVillager);
            input.setItem(1, new ItemStack(Items.GOLDEN_APPLE, 2));
            input.setItem(2, weaknessPotion);

            Method tickServer = converter.getClass().getMethod("tickServer");
            tickServer.invoke(converter);
            String evVersion=ModList.get().getModContainerById("mca_ev_compat").orElseThrow().getModInfo().getVersion().toString();
            boolean legacyFinishPayment=evVersion.equals("2.1.0");
            helper.assertTrue(input.getItem(1).is(Items.GOLDEN_APPLE) && input.getItem(1).getCount()==(legacyFinishPayment?2:1),
                    "Start must preserve the installed EV version's payment timing");
            helper.assertTrue(legacyFinishPayment?!input.getItem(2).isEmpty():input.getItem(2).isEmpty(),
                    "Potion reservation must match the installed EV version");
            Field timer = findField(converter.getClass(), "timer");
            Class<?> converterType = Class.forName("de.maxhenkel.easyvillagers.blocks.tileentity.ConverterTileentity");
            long finalizeTime = ((Number) converterType.getMethod("getFinalizeTime").invoke(null)).longValue();
            timer.setLong(converter, finalizeTime);
            tickServer.invoke(converter);

            helper.assertTrue(input.getItem(1).is(Items.GOLDEN_APPLE) && input.getItem(1).getCount() == 1,
                    "Finalization must not charge a second golden apple");
            helper.assertTrue(input.getItem(2).isEmpty(),
                    "Finalization must not charge a second weakness potion");
            helper.assertTrue(!output.getItem(0).isEmpty(),
                    "A completed MCA Converter must commit the converted MCA villager to output");
            helper.succeed();
        } catch (ReflectiveOperationException | RuntimeException exception) {
            helper.fail("Could not verify MCA Converter ingredient consumption: " + exception);
        }
    }

    @GameTest(template = "gametest/empty")
    public static void mcaEvBreederBlocksInfertileAndStoresCanonicalMcaChild(GameTestHelper helper) {
        if (!McaEvCompatBridge.isMcaEvCompatLoaded()
                || !BuiltInRegistries.BLOCK.containsKey(id("easy_villagers", "breeder"))
                || !BuiltInRegistries.ITEM.containsKey(id("easy_villagers", "villager"))) {
            helper.succeed();
            return;
        }

        try {
            Class<?> compatLogic = Class.forName("com.guilh.mcaevcompat.CompatLogic");
            Method saveStack = compatLogic.getMethod(
                    "saveMcaVillagerToStack",
                    ItemStack.class,
                    Villager.class,
                    long.class
            );
            Method copyStoredData = compatLogic.getMethod(
                    "copyStoredMcaVillagerData", ItemStack.class);
            Class<?> blockCompat = Class.forName("com.guilh.mcaevcompat.EasyVillagersBlockCompat");
            Method tryBreed = blockCompat.getMethod(
                    "tryBreedMcaChild",
                    BlockEntity.class,
                    ItemStack.class,
                    ItemStack.class,
                    NonNullList.class,
                    NonNullList.class
            );

            VillagerEntityMCA infertileParent = EntitiesMCA.FEMALE_VILLAGER.create(helper.getLevel());
            VillagerEntityMCA fertileParent = EntitiesMCA.MALE_VILLAGER.create(helper.getLevel());
            helper.assertTrue(infertileParent != null && fertileParent != null,
                    "MCA parent entity types must create entities");
            infertileParent.getTraits().addTrait(Traits.INFERTILE);
            ItemStack infertileStack = saveMcaEvStack(saveStack, infertileParent, helper.getLevel().getGameTime());
            ItemStack fertileStack = saveMcaEvStack(saveStack, fertileParent, helper.getLevel().getGameTime());
            ItemStack infertileBefore = infertileStack.copy();
            ItemStack fertileBefore = fertileStack.copy();

            BlockPos breederPos = new BlockPos(11, 1, 1);
            helper.setBlock(breederPos, BuiltInRegistries.BLOCK.get(id("easy_villagers", "breeder")));
            BlockEntity breeder = helper.getBlockEntity(breederPos);
            helper.assertTrue(breeder != null, "The Breeder block must create its block entity");
            NonNullList<ItemStack> blockedFood = NonNullList.withSize(1, new ItemStack(Items.BREAD, 6));
            NonNullList<ItemStack> blockedOutput = NonNullList.withSize(1, ItemStack.EMPTY);
            Object blocked = tryBreed.invoke(
                    null, breeder, infertileStack, fertileStack, blockedFood, blockedOutput);
            helper.assertTrue("CANCEL".equals(String.valueOf(blocked)),
                    "An MCA parent with Infertile must cancel the real Easy Villagers breeder");
            helper.assertTrue(blockedOutput.get(0).isEmpty(),
                    "A cancelled infertile breeding attempt must not create a child");
            helper.assertTrue(blockedFood.get(0).is(Items.BREAD) && blockedFood.get(0).getCount() == 6,
                    "A cancelled infertile breeding attempt must not consume food");
            helper.assertTrue(ItemStack.isSameItemSameComponents(infertileStack, infertileBefore)
                            && ItemStack.isSameItemSameComponents(fertileStack, fertileBefore),
                    "The breeder must not mutate either stored MCA parent while checking fertility");

            VillagerEntityMCA fertileMother = EntitiesMCA.FEMALE_VILLAGER.create(helper.getLevel());
            VillagerEntityMCA fertileFather = EntitiesMCA.MALE_VILLAGER.create(helper.getLevel());
            helper.assertTrue(fertileMother != null && fertileFather != null,
                    "MCA fertile parent entity types must create entities");
            ItemStack motherStack = saveMcaEvStack(saveStack, fertileMother, helper.getLevel().getGameTime());
            ItemStack fatherStack = saveMcaEvStack(saveStack, fertileFather, helper.getLevel().getGameTime());
            NonNullList<ItemStack> fertileFood = NonNullList.withSize(1, new ItemStack(Items.BREAD, 6));
            NonNullList<ItemStack> fertileOutput = NonNullList.withSize(1, ItemStack.EMPTY);
            Object success = tryBreed.invoke(
                    null, breeder, motherStack, fatherStack, fertileFood, fertileOutput);
            helper.assertTrue("SUCCESS".equals(String.valueOf(success)),
                    "Two fertile MCA parents must produce a child through MCA's native breeding flow");
            ItemStack childStack = fertileOutput.get(0);
            helper.assertTrue(!childStack.isEmpty(),
                    "The successful Breeder output must contain the MCA child snapshot");
            CompoundTag childData = (CompoundTag) copyStoredData.invoke(null, childStack);
            helper.assertTrue(childData != null
                            && childData.getString("id").startsWith("mca:")
                            && childData.getInt("Age") < 0
                            && childData.contains("Gender", Tag.TAG_ANY_NUMERIC)
                            && childData.contains("Traits", Tag.TAG_COMPOUND),
                    "The Breeder must retain MCA's canonical baby identity, age, genetics/traits payload and family-producing flow");
            helper.succeed();
        } catch (ReflectiveOperationException | RuntimeException exception) {
            Throwable cause = exception instanceof InvocationTargetException invocation
                    && invocation.getCause() != null ? invocation.getCause() : exception;
            helper.fail("Could not verify MCA x Easy Villagers Breeder fertility and child snapshot: " + cause);
        }
    }

    @GameTest(template = "gametest/empty")
    public static void hunterStakeUsesBoundedNativeMeleeAttack(GameTestHelper helper) {
        VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.create(helper.getLevel());
        LivingEntity target = EntityType.ZOMBIE.create(helper.getLevel());
        helper.assertTrue(villager != null && target != null, "Hunter and target entities must be available");
        villager.setAgeState(AgeState.ADULT);
        ModCapabilities.get(villager).orElseThrow().setHunterVillageApplied(true);
        ModCapabilities.get(villager).orElseThrow().setHunterWeaponSelection("stake");
        McaDefensiveVillagerService.reconcileHunterWeapon(villager);
        helper.assertTrue(id("vampirism", "stake").equals(BuiltInRegistries.ITEM.getKey(villager.getMainHandItem().getItem())),
                "A persisted stake selection must equip the Vampirism stake");

        villager.moveTo(helper.absolutePos(new BlockPos(2, 2, 2)), 0.0F, 0.0F);
        target.moveTo(helper.absolutePos(new BlockPos(3, 2, 2)), 0.0F, 0.0F);
        helper.getLevel().addFreshEntity(villager);
        helper.getLevel().addFreshEntity(target);
        double attackBefore = villager.getAttribute(Attributes.ATTACK_DAMAGE).getValue();
        float healthBefore = target.getHealth();

        helper.assertTrue(McaSpecialVillagerCombatService.performMeleeAttack(
                        villager, target, McaSpecialVillagerType.HUNTER),
                "A Hunter stake attack must use the native Mob attack flow");
        float dealt = healthBefore - target.getHealth();
        float configuredDamage = (float) McaVampirismCompatConfig.hunterVillagerAttackDamage();
        helper.assertTrue(Math.abs(dealt - configuredDamage) <= 0.1F,
                "The native Hunter attack must use the configured base damage, not pitchfork player damage (expected "
                        + configuredDamage + ", got " + dealt + ")");
        helper.assertTrue(Math.abs(villager.getAttribute(Attributes.ATTACK_DAMAGE).getValue() - attackBefore) < 0.0001D,
                "The temporary native-attack modifier must be removed after the strike");
        helper.succeed();
    }

    @GameTest(template = "gametest/empty")
    public static void occultBookRecipeAndAdvancementAreRegistered(GameTestHelper helper) {
        ResourceLocation bookId = id(McaVampirismCompat.MOD_ID, "book_occult_arts");
        ItemStack book = new ItemStack(BuiltInRegistries.ITEM.get(bookId));
        helper.assertTrue(bookId.equals(BuiltInRegistries.ITEM.getKey(book.getItem())),
                "The Book of Occult Arts item must be registered");
        helper.assertTrue(book.getMaxStackSize() == 1, "The Book of Occult Arts must not stack");
        helper.assertTrue(helper.getLevel().getServer().getRecipeManager().byKey(bookId).isPresent(),
                "The Book of Occult Arts recipe must load");
        helper.assertTrue(helper.getLevel().getServer().getAdvancements().get(
                        id(McaVampirismCompat.MOD_ID, "discover_occult_village")) != null,
                "The occult village discovery advancement must load");
        helper.succeed();
    }

    @GameTest(template = "gametest/empty")
    public static void inheritedWerewolfStateSurvivesSerialization(GameTestHelper helper) {
        VampiricVillagerState inherited = new VampiricVillagerState();
        inherited.markInheritedWerewolf(helper.getLevel().getGameTime());
        inherited.markFactionInheritanceProcessed();

        CompoundTag serialized = inherited.serializeNBT(helper.getLevel().registryAccess());
        VampiricVillagerState restored = new VampiricVillagerState();
        restored.deserializeNBT(helper.getLevel().registryAccess(), serialized);

        helper.assertTrue(restored.isWerewolfConverted(), "Inherited child must remain converted");
        helper.assertTrue(restored.isWerewolfAppearanceApplied(), "Inherited appearance marker must persist");
        helper.assertTrue(restored.isWerewolfPartialForm(), "Inherited partial-form marker must persist");
        helper.assertTrue(restored.isFactionInheritanceProcessed(), "Inheritance decision must remain processed");
        helper.succeed();
    }

    @GameTest(template = "gametest/empty")
    public static void editorFactionTraitsApplyAndRemoveCompleteStates(GameTestHelper helper) {
        VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.create(helper.getLevel());
        helper.assertTrue(villager != null, "MCA male villager type must create an entity");
        villager.setAgeState(AgeState.ADULT);

        McaTraitBridge.addVampireTrait(villager);
        McaEditorTraitSyncService.reconcileEditedTraits(villager);
        helper.assertTrue(McaVampireStateService.isVampire(villager),
                "Adding the Vampire trait in the editor must convert the villager");

        McaTraitBridge.removeVampireTrait(villager);
        McaEditorTraitSyncService.reconcileEditedTraits(villager);
        helper.assertTrue(!McaVampireStateService.isVampire(villager),
                "Removing the Vampire trait in the editor must clear the conversion");

        McaTraitBridge.addHunterTrait(villager);
        McaEditorTraitSyncService.reconcileEditedTraits(villager);
        helper.assertTrue(McaHunterAlignmentService.isMcaHunterVillager(villager),
                "Adding the Hunter trait in the editor must align the villager as a Hunter");

        McaTraitBridge.removeHunterTrait(villager);
        McaEditorTraitSyncService.reconcileEditedTraits(villager);
        helper.assertTrue(!McaHunterAlignmentService.isMcaHunterVillager(villager),
                "Removing the Hunter trait in the editor must clear Hunter alignment");

        if (McaWerewolvesCompat.isAvailable()) {
            McaTraitBridge.addWerewolfTrait(villager);
            McaEditorTraitSyncService.reconcileEditedTraits(villager);
            helper.assertTrue(McaWerewolfStateService.isWerewolf(villager),
                    "Adding the Werewolf trait in the editor must convert the villager");

            McaTraitBridge.removeWerewolfTrait(villager);
            McaEditorTraitSyncService.reconcileEditedTraits(villager);
            helper.assertTrue(!McaWerewolfStateService.isWerewolf(villager),
                    "Removing the Werewolf trait in the editor must clear the conversion");
        } else {
            McaTraitBridge.addWerewolfTrait(villager);
            helper.assertTrue(!McaTraitBridge.hasWerewolfTrait(villager)
                            && !McaWerewolfStateService.isWerewolf(villager),
                    "Werewolf must not be assignable when the optional Werewolves addon is absent");
        }
        helper.succeed();
    }

    @GameTest(template = "gametest/empty")
    public static void ordinaryVampiresRemainBlockedFromMcaVampireBlood(GameTestHelper helper) {
        Player source = helper.makeMockPlayer(GameType.CREATIVE);
        VillagerEntityMCA villager = createVampireVillager(helper);
        helper.assertTrue(McaVampireFeedProtection.shouldBlockDrain(source, villager),
                "The MCA Vampire protection must still block a non-Bloodknight source");
        helper.succeed();
    }

    @GameTest(template = "gametest/empty")
    public static void bloodknightFeedsEligibleMcaAndNativeVampires(GameTestHelper helper) {
        if (!ModList.get().isLoaded("bloodlines")) {
            helper.succeed();
            return;
        }

        try {
            ServerPlayer bloodknight = createConnectedGameTestPlayer(helper, "test-bloodknight-drink");
            setPlayerFaction(bloodknight, id("vampirism", "vampire"));
            setBloodlinesBloodknight(bloodknight, 4);
            var vampire = VampirismAPI.getVampirePlayer(bloodknight)
                    .orElseThrow(() -> new IllegalStateException("Bloodknight must have a VampirePlayer"));

            VillagerEntityMCA mcaTarget = createVampireVillager(helper);
            mcaTarget.setHealth(mcaTarget.getMaxHealth() * 0.25F);
            helper.assertTrue(!McaVampireFeedProtection.shouldBlockDrain(bloodknight, mcaTarget),
                    "A ranked Bloodknight must bypass MCA vampire protection at Bloodlines' default 25% threshold");
            var mcaCreature = ExtendedCreature.getSafe(mcaTarget)
                    .orElseThrow(() -> new IllegalStateException("MCA target must expose ExtendedCreature"));
            boolean canBeBitten = mcaCreature.canBeBitten(vampire);
            helper.assertTrue(canBeBitten,
                    "The ExtendedCreature bite path must allow an eligible Bloodknight (blood="
                            + mcaCreature.getBlood() + ", maxBlood=" + mcaCreature.getMaxBlood() + ')');

            mcaTarget.setHealth(mcaTarget.getMaxHealth() * 0.26F);
            helper.assertTrue(McaVampireFeedProtection.shouldBlockDrain(bloodknight, mcaTarget),
                    "A Bloodknight above the configured health threshold must remain blocked from MCA vampire blood");

            mcaTarget.setHealth(mcaTarget.getMaxHealth() * 0.25F);
            ISkill<?> feedingFrenzy = getBloodlinesSkill("BLOODKNIGHT_FEEDING_FRENZY_1");
            enableBloodlinesSkill(vampire, feedingFrenzy);
            helper.assertTrue(vampire.getSkillHandler().isSkillEnabled(feedingFrenzy),
                    "The Bloodknight Feeding Frenzy skill must be enabled for the native event test");

            BloodDrinkEvent.PlayerDrinkBloodEvent mcaDrink = postBloodDrink(vampire, mcaTarget);
            helper.assertTrue(mcaDrink.getAmount() > 10,
                    "An MCA Vampire must receive Bloodlines' native vampire-source blood bonus");
            helper.assertTrue(mcaDrink.getSaturation() > 1.0F,
                    "An MCA Vampire must receive Bloodlines' native vampire-source saturation bonus");
            var bloodFrenzy = BuiltInRegistries.MOB_EFFECT.getHolder(id("bloodlines", "blood_frenzy"));
            helper.assertTrue(bloodFrenzy.isPresent() && bloodknight.hasEffect(bloodFrenzy.get()),
                    "Drinking from an MCA Vampire must grant native Bloodknight Blood Frenzy when enabled");
            LivingEntity nativeMobTarget = ModEntities.VAMPIRE.get().create(helper.getLevel());
            helper.assertTrue(nativeMobTarget != null,
                    "A native Vampirism vampire must be available for the Bloodknight parity test");
            BloodDrinkEvent.PlayerDrinkBloodEvent nativeMobDrink = postBloodDrink(vampire, nativeMobTarget);
            helper.assertTrue(mcaDrink.getAmount() == nativeMobDrink.getAmount(),
                    "MCA and native vampire mobs must receive the same Bloodknight blood multiplier");
            helper.assertTrue(Float.compare(mcaDrink.getSaturation(), nativeMobDrink.getSaturation()) == 0,
                    "MCA and native vampire mobs must receive the same Bloodknight saturation multiplier");

            Player nativeTarget = helper.makeMockPlayer(GameType.SURVIVAL);
            setPlayerFaction(nativeTarget, id("vampirism", "vampire"));
            nativeTarget.setHealth(nativeTarget.getMaxHealth() * 0.25F);
            var nativeVampire = VampirismAPI.getVampirePlayer(nativeTarget)
                    .orElseThrow(() -> new IllegalStateException("Native target must have a VampirePlayer"));
            helper.assertTrue(nativeVampire.canBeBitten(vampire),
                    "The compat must no longer cancel Bloodlines' native vampire-player bite decision");
        } catch (ReflectiveOperationException | RuntimeException exception) {
            helper.fail("Bloodlines Bloodknight integration must initialize safely: " + exception.getMessage());
            return;
        }
        helper.succeed();
    }

    @GameTest(template = "gametest/empty")
    public static void nativeWerewolfTransformationCannotReplaceMcaVillagers(GameTestHelper helper) {
        if (!ModList.get().isLoaded("werewolves")) {
            helper.succeed();
            return;
        }

        VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.create(helper.getLevel());
        helper.assertTrue(villager != null, "MCA male villager type must create an entity");
        villager.setAgeState(AgeState.ADULT);
        villager.setCustomName(Component.literal("Seleris"));
        VillagerData originalData = villager.getVillagerData().setProfession(ProfessionsMCA.GUARD);
        villager.setVillagerData(originalData);
        villager.getOffers().add(new MerchantOffer(
                new ItemCost(Items.EMERALD, 4),
                new ItemStack(Items.BREAD),
                12,
                1,
                0.05F
        ));
        UUID originalUuid = villager.getUUID();
        Component originalName = villager.getCustomName();
        int originalOfferCount = villager.getOffers().size();
        villager.moveTo(helper.absoluteVec(new Vec3(2.5D, 2.0D, 2.5D)));
        helper.getLevel().addFreshEntity(villager);

        try {
            Method markNativeWerewolf = villager.getClass().getMethod("setWerewolfFaction", boolean.class);
            markNativeWerewolf.invoke(villager, true);
            helper.assertTrue(!McaWerewolfStateService.isWerewolf(villager),
                    "A native Werewolves mark must not create compat werewolf state");
            helper.assertTrue(!McaTraitBridge.hasWerewolfTrait(villager),
                    "A native Werewolves mark must not add the MCA Werewolf trait");

            CompoundTag legacyData = new CompoundTag();
            CompoundTag nativeWerewolvesData = new CompoundTag();
            nativeWerewolvesData.putBoolean("werewolf", true);
            legacyData.put("werewolves", nativeWerewolvesData);
            Field nativeMarker = findNativeWerewolfMarker(villager);
            nativeMarker.setBoolean(villager, true);
            McaNativeWerewolfProtectionService.clearLegacyNativeMarker(villager, legacyData);
            helper.assertTrue(!legacyData.contains("werewolves"),
                    "A legacy native Werewolves marker must be removed from MCA villager data");
            helper.assertTrue(!nativeMarker.getBoolean(villager),
                    "A legacy native Werewolves marker must be cleared from the live MCA villager");

            Class<?> transformType = Class.forName("de.teamlapen.werewolves.api.entities.werewolf.TransformType");
            Object fullMoon = transformType.getField("FULL_MOON").get(null);
            Method transform = villager.getClass().getMethod("transformToWerewolf", transformType);
            Object result = transform.invoke(villager, fullMoon);

            helper.assertTrue(result == villager,
                    "Native full-moon transformation must leave the original MCA villager in place");
            // GameTest entity registration is deferred until the next tick,
            // so querying the level UUID index here is timing-sensitive. The
            // identity contract is the original live object, its UUID and its
            // non-removed state immediately after the native call.
            helper.assertTrue(!villager.isRemoved() && originalUuid.equals(villager.getUUID()),
                    "Native transformation must not remove or replace the MCA villager UUID");
            helper.assertTrue(originalName.equals(villager.getCustomName()),
                    "Native transformation must preserve the MCA villager name");
            helper.assertTrue(originalData.equals(villager.getVillagerData()),
                    "Native transformation must preserve the MCA villager profession");
            helper.assertTrue(villager.getOffers().size() == originalOfferCount,
                    "Native transformation must preserve MCA villager offers");
            helper.assertTrue(!McaWerewolfStateService.isWerewolf(villager)
                            && !McaTraitBridge.hasWerewolfTrait(villager),
                    "Native transformation must not grant compat werewolf state or traits");
            helper.succeed();
        } catch (ReflectiveOperationException | RuntimeException exception) {
            helper.fail("Could not verify MCA identity protection from native Werewolves transformation: " + exception);
        }
    }

    private static void addLegacyFalseFactionPayload(ItemStack stack) {
        CompoundTag payload = new CompoundTag();
        payload.putInt("Version", 2);
        payload.putBoolean("Vampire", false);
        payload.putBoolean("Hunter", false);
        payload.putBoolean("Werewolf", false);
        payload.put("State", new CompoundTag());

        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        root.put(McaEvStatePayload.PAYLOAD_KEY, payload);
        CustomData.set(DataComponents.CUSTOM_DATA, stack, root);
    }

    private static ItemStack saveMcaEvStack(
            Method saveStack,
            VillagerEntityMCA villager,
            long gameTime
    ) throws ReflectiveOperationException {
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(id("easy_villagers", "villager")));
        boolean saved = (Boolean) saveStack.invoke(null, stack, villager, gameTime);
        if (!saved) {
            throw new IllegalStateException("MCA x Easy Villagers could not create a stored MCA parent snapshot");
        }
        return stack;
    }

    private static Container createMcaEvInventoryViewerContainer(BlockEntity viewer)
            throws ReflectiveOperationException {
        Class<?> inventoryClass = Class.forName("com.guilh.mcaevcompat.inventory.McaStoredInventory");
        for (Constructor<?> constructor : inventoryClass.getConstructors()) {
            Class<?>[] parameters = constructor.getParameterTypes();
            if (parameters.length == 1 && parameters[0].isAssignableFrom(viewer.getClass())) {
                return (Container) constructor.newInstance(viewer);
            }
        }
        throw new NoSuchMethodException("McaStoredInventory InventoryViewer constructor");
    }

    private static Field findNativeWerewolfMarker(VillagerEntityMCA villager) throws NoSuchFieldException {
        for (Class<?> type = villager.getClass(); type != null; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (field.getType() != boolean.class
                        || (!"werewolf".equals(field.getName()) && !field.getName().endsWith("$werewolf"))) {
                    continue;
                }
                field.setAccessible(true);
                return field;
            }
        }
        throw new NoSuchFieldException("Werewolves native villager marker");
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                Field field = current.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                // The concrete MCA x Easy Villagers tile entity subclasses its
                // common Converter implementation, so keep walking upward.
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static BlockEntity createRealHunterTrader(
            GameTestHelper helper,
            BlockPos traderPos,
            boolean addOffer
    ) throws ReflectiveOperationException {
        VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.create(helper.getLevel());
        if (villager == null) {
            throw new IllegalStateException("MCA male villager type did not create an entity");
        }
        McaTraitBridge.addHunterTrait(villager);
        if (addOffer) {
            villager.getOffers().add(new MerchantOffer(
                    new ItemCost(Items.EMERALD, 20),
                    new ItemStack(Items.BREAD),
                    12,
                    1,
                    0.05F
            ));
        }

        CompoundTag villagerData = new CompoundTag();
        villager.saveWithoutId(villagerData);
        villagerData.putString("id", "mca:male_villager");
        Class<?> compatLogic = Class.forName("com.guilh.mcaevcompat.CompatLogic");
        Method createStack = compatLogic.getMethod(
                "createMcaVillagerStack",
                CompoundTag.class,
                CompoundTag.class
        );
        ItemStack visualStack = (ItemStack) createStack.invoke(
                null,
                villagerData,
                new CompoundTag()
        );
        addLegacyFalseFactionPayload(visualStack);

        helper.setBlock(traderPos, BuiltInRegistries.BLOCK.get(id("easy_villagers", "trader")));
        BlockEntity blockEntity = helper.getBlockEntity(traderPos);
        Method setVillager = blockEntity.getClass().getMethod("setVillager", ItemStack.class);
        setVillager.invoke(blockEntity, visualStack);
        return blockEntity;
    }

    private static boolean invokeRealTraderBlockOpenGui(
            GameTestHelper helper,
            BlockEntity trader,
            Player player,
            BlockPos traderPos
    ) throws ReflectiveOperationException {
        Block traderBlock = helper.getBlockState(traderPos).getBlock();
        Class<?> traderTileentityBase =
                Class.forName("de.maxhenkel.easyvillagers.blocks.tileentity.TraderTileentityBase");
        Method openGui = traderBlock.getClass().getDeclaredMethod(
                "openGUI",
                traderTileentityBase,
                Player.class,
                net.minecraft.world.level.Level.class,
                BlockPos.class
        );
        openGui.setAccessible(true);
        Object result = openGui.invoke(traderBlock, trader, player, helper.getLevel(), traderPos);
        return result instanceof Boolean value && value;
    }

    private static ItemInteractionResult invokeRealTraderWorkstationUse(
            GameTestHelper helper,
            BlockPos traderPos,
            Player player,
            ItemStack workstation
    ) throws ReflectiveOperationException {
        Class<?> traderBlockBase = Class.forName("de.maxhenkel.easyvillagers.blocks.TraderBlockBase");
        Method useItemOn = traderBlockBase.getDeclaredMethod(
                "useItemOn",
                ItemStack.class,
                net.minecraft.world.level.block.state.BlockState.class,
                net.minecraft.world.level.Level.class,
                BlockPos.class,
                Player.class,
                InteractionHand.class,
                BlockHitResult.class
        );
        useItemOn.setAccessible(true);
        BlockPos absolutePos = helper.absolutePos(traderPos);
        return (ItemInteractionResult) useItemOn.invoke(
                helper.getBlockState(traderPos).getBlock(),
                workstation,
                helper.getBlockState(traderPos),
                helper.getLevel(),
                absolutePos,
                player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(absolutePos), net.minecraft.core.Direction.UP, absolutePos, false)
        );
    }

    private static void assertSpecialistPoi(
            GameTestHelper helper,
            Block workstation,
            ResourceLocation professionId
    ) {
        helper.assertTrue(workstation != net.minecraft.world.level.block.Blocks.AIR,
                "The specialist workstation block must be registered: " + professionId);
        VillagerProfession profession = BuiltInRegistries.VILLAGER_PROFESSION.get(professionId);
        var poi = PoiTypes.forState(workstation.defaultBlockState());
        helper.assertTrue(poi.isPresent(),
                "The specialist workstation must expose a POI: " + professionId);
        helper.assertTrue(profession.heldJobSite().test(poi.get()),
                "The specialist profession must accept its new workstation POI: " + professionId);
    }

    private static int countInventory(Player player, net.minecraft.world.item.Item item) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static int countItem(VillagerEntityMCA villager, net.minecraft.world.item.Item item) {
        int count = 0;
        for (int slot = 0; slot < villager.getInventory().getContainerSize(); slot++) {
            ItemStack stack = villager.getInventory().getItem(slot);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void setPlayerFaction(Player player, ResourceLocation factionId) {
        IFaction<?> faction = VampirismAPI.factionRegistry().getFactionByID(factionId);
        if (!(faction instanceof IPlayableFaction<?> playableFaction)) {
            throw new IllegalStateException("Missing playable faction " + factionId);
        }
        if (!VampirismAPI.factionPlayerHandler(player).setFactionAndLevel(playableFaction, 1)) {
            throw new IllegalStateException("Could not set player faction " + factionId);
        }
    }

    private static VillagerEntityMCA createVampireVillager(GameTestHelper helper) {
        VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.create(helper.getLevel());
        if (villager == null) {
            throw new IllegalStateException("MCA male villager type must create an entity");
        }
        villager.setAgeState(AgeState.ADULT);
        McaTraitBridge.addVampireTrait(villager);
        McaEditorTraitSyncService.reconcileEditedTraits(villager);
        if (!McaVampireStateService.isVampire(villager)) {
            throw new IllegalStateException("Vampire trait must convert MCA villager for bite test");
        }
        return villager;
    }

    private static BloodDrinkEvent.PlayerDrinkBloodEvent postBloodDrink(IVampirePlayer vampire, LivingEntity target) {
        BloodDrinkEvent.PlayerDrinkBloodEvent event = new BloodDrinkEvent.PlayerDrinkBloodEvent(
                vampire,
                10,
                1.0F,
                false,
                new DrinkBloodContext(target)
        );
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    /**
     * Bloodlines sends an effect packet during this event. A GameTest has no
     * client connection, so suppress only that packet callback while retaining
     * the native effect state, modifiers and advancement processing.
     */
    private static ServerPlayer createConnectedGameTestPlayer(GameTestHelper helper, String name) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), name);
        return new ServerPlayer(
                helper.getLevel().getServer(),
                helper.getLevel(),
                profile,
                ClientInformation.createDefault()
        ) {
            @Override
            protected void onEffectAdded(
                    MobEffectInstance effect,
                    net.minecraft.world.entity.Entity source
            ) {
                // The effect has already been added; only its client packet is omitted.
            }
        };
    }

    private static ISkill<?> getBloodlinesSkill(String fieldName) throws ReflectiveOperationException {
        ClassLoader loader = McaVampCompatGameTests.class.getClassLoader();
        Class<?> skillsClass = Class.forName(
                "com.thedrofdoctoring.bloodlines.skills.BloodlineSkills", true, loader
        );
        Object deferredSkill = skillsClass.getField(fieldName).get(null);
        return (ISkill<?>) deferredSkill.getClass().getMethod("get").invoke(deferredSkill);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void enableBloodlinesSkill(IVampirePlayer vampire, ISkill<?> skill) {
        vampire.getSkillHandler().enableSkill((ISkill) skill, true);
    }

    private static void setBloodlinesBloodknight(Player player, int rank) throws ReflectiveOperationException {
        ClassLoader loader = McaVampCompatGameTests.class.getClassLoader();
        Class<?> managerClass = Class.forName(
                "com.thedrofdoctoring.bloodlines.capabilities.bloodlines.BloodlineManager", true, loader
        );
        Class<?> registryClass = Class.forName(
                "com.thedrofdoctoring.bloodlines.core.bloodline.BloodlineRegistry", true, loader
        );
        Class<?> bloodlineClass = Class.forName(
                "com.thedrofdoctoring.bloodlines.capabilities.bloodlines.IBloodline", true, loader
        );
        Object manager = managerClass.getMethod("get", Player.class).invoke(null, player);
        Object deferredBloodknight = registryClass.getField("BLOODLINE_BLOODKNIGHT").get(null);
        Object bloodknight = deferredBloodknight.getClass().getMethod("get").invoke(deferredBloodknight);
        managerClass.getMethod("setBloodline", bloodlineClass).invoke(manager, bloodknight);
        managerClass.getMethod("setRank", int.class).invoke(manager, rank);
    }

    private static ResourceLocation id(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }
}
