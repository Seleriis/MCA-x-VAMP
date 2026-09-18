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
import com.guilh.mca_vampirism_compat.event.VampiricVillagerEvents;
import com.guilh.mca_vampirism_compat.faction.PlayerFaction;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.service.McaDefensiveVillagerService;
import com.guilh.mca_vampirism_compat.service.McaBloodValueRepairService;
import com.guilh.mca_vampirism_compat.service.McaEditorTraitSyncService;
import com.guilh.mca_vampirism_compat.service.McaHunterAlignmentService;
import com.guilh.mca_vampirism_compat.service.McaHunterTradeService;
import com.guilh.mca_vampirism_compat.service.McaNativeWerewolfProtectionService;
import com.guilh.mca_vampirism_compat.service.McaSpecialVillagerCombatService;
import com.guilh.mca_vampirism_compat.service.McaSpecialVillagerType;
import com.guilh.mca_vampirism_compat.service.McaSpecialVillagerRuntimeService;
import com.guilh.mca_vampirism_compat.service.McaVampireStateService;
import com.guilh.mca_vampirism_compat.service.McaWerewolfStateService;
import com.guilh.mca_vampirism_compat.service.McaWerewolvesCompat;
import com.guilh.mca_vampirism_compat.service.McaVillageEffectBounds;
import com.guilh.mca_vampirism_compat.trade.McaFactionTradeService;
import com.guilh.mca_vampirism_compat.trade.McaFactionTradeSession;
import com.guilh.mca_vampirism_compat.trade.McaNativeTradeCycleSession;
import com.guilh.mca_vampirism_compat.trade.McaOfferPreparation;
import com.guilh.mca_vampirism_compat.trade.McaVampireSpecialTradeService;
import com.guilh.mca_vampirism_compat.trade.MerchantMenuTradeAccess;
import com.guilh.mca_vampirism_compat.trade.TradeCycleEligibility;
import com.guilh.mca_vampirism_compat.trait.McaTraitBridge;
import de.teamlapen.vampirism.api.VampirismAPI;
import de.teamlapen.vampirism.api.entity.factions.IFaction;
import de.teamlapen.vampirism.api.entity.factions.IPlayableFaction;
import de.teamlapen.vampirism.api.entity.player.vampire.IVampirePlayer;
import de.teamlapen.vampirism.core.ModBlocks;
import de.teamlapen.vampirism.entity.ExtendedCreature;
import de.teamlapen.vampirism.entity.player.vampire.VampirePlayer;
import forge.net.mca.entity.EquipmentSet;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.Traits;
import forge.net.mca.entity.ai.brain.tasks.EquipmentTask;
import forge.net.mca.entity.ai.relationship.AgeState;
import forge.net.mca.entity.interaction.VillagerCommandHandler;
import forge.net.mca.ProfessionsMCA;
import forge.net.mca.entity.EntitiesMCA;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

@GameTestHolder(McaVampirismCompat.MOD_ID)
@PrefixGameTestTemplate(false)
@Mod.EventBusSubscriber(modid = McaVampirismCompat.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
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
        VillagerEntityMCA villager = EntitiesMCA.FEMALE_VILLAGER.get().create(helper.getLevel());
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
    public static void vampirePlayerCanSelectHumanMcaVillagerAsBloodSource(GameTestHelper helper) {
        VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.get().create(helper.getLevel());
        helper.assertTrue(villager != null, "MCA male villager type must create an entity");
        villager.moveTo(helper.absoluteVec(new Vec3(2.5D, 2.0D, 2.5D)));
        helper.getLevel().addFreshEntity(villager);

        Player player = helper.makeMockPlayer();
        setPlayerFaction(player, id("vampirism", "vampire"));
        VampirePlayer vampire = VampirePlayer.get(player);

        helper.assertTrue(
                ExtendedCreature.getSafe(villager).isPresent(),
                "Every MCA villager must expose Vampirism's ExtendedCreature capability"
        );
        helper.assertTrue(
                !McaVampireStateService.isVampire(villager),
                "A freshly created MCA villager must remain a human blood source"
        );
        helper.assertTrue(
                McaBloodValueRepairService.prepareMcaBloodSourceForBite(villager),
                "A human MCA villager without Vampirism Integrations must receive the fallback blood source"
        );
        helper.assertTrue(
                ExtendedCreature.getSafe(villager)
                        .map(creature -> creature.getBlood() == 15 && creature.getMaxBlood() == 15)
                        .orElse(false),
                "The fallback must initialize the native ExtendedCreature blood values"
        );
        helper.assertTrue(
                vampire.determineBiteType(villager) == IVampirePlayer.BITE_TYPE.SUCK_BLOOD_CREATURE,
                "A Vampire player must select the native creature-bite flow for a human MCA villager"
        );
        helper.succeed();
    }

    @GameTest(template = "gametest/empty")
    public static void alternateSpecialistStationRequiresMatchingMcaFaction(GameTestHelper helper) {
        VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.get().create(helper.getLevel());
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
            Method getWorkstation = trader.getClass().getMethod("getWorkstation");
            Method setWorkstation = trader.getClass().getMethod("setWorkstation", Block.class);
            // Easy Villagers 1.20.1 routes both player and automation updates
            // through this tile-entity method.  Its 1.21-only useItemOn hook
            // does not exist in the supported optional dependency.
            setWorkstation.invoke(trader, ModBlocks.ALTAR_INSPIRATION.get());
            helper.assertTrue(getWorkstation.invoke(trader) == net.minecraft.world.level.block.Blocks.AIR,
                    "A Hunter stored in a Trader must reject the Vampire specialist workstation");

            setWorkstation.invoke(trader, ModBlocks.HUNTER_TABLE.get());
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
        visualStack.getOrCreateTag().merge(root);

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
            VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.get().create(helper.getLevel());
            helper.assertTrue(villager != null, "MCA male villager type must create an entity");
            McaTraitBridge.addHunterTrait(villager);

            Class<?> compatLogic = Class.forName("com.guilh.mcaevcompat.CompatLogic");
            Method createStack = compatLogic.getMethod(
                    "createMcaVillagerStack",
                    Villager.class,
                    long.class
            );
            ItemStack visualStack = (ItemStack) createStack.invoke(
                    null,
                    villager,
                    helper.getLevel().getGameTime()
            );
            helper.assertTrue(!visualStack.isEmpty(), "mca-ev-compat must create a real 1.20.1 villager stack");
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

            Player vampire = helper.makeMockPlayer();
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
                    new GameProfile(UUID.randomUUID(), "test-hunter-trader")
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
            helper.assertTrue(session.offers().get(0).getCostA().getCount() == 17,
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
            helper.assertTrue(session.offers().get(0).getUses() == 1,
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
            helper.assertTrue(reopenedSession.offers().get(0).getUses() == 1,
                    "The reopened Trader must preserve the completed offer use");
            reopenedSession.close("GameTest reopened-session check completed");
            helper.succeed();
        } catch (ReflectiveOperationException | RuntimeException exception) {
            helper.fail("Could not exercise the real Hunter Trader policy: " + exception);
        }
    }

    @GameTest(template = "gametest/empty")
    public static void storedVampireTraderCycleTradesRerollsSpecialOffers(GameTestHelper helper) {
        if (!McaEvCompatBridge.isMcaEvCompatLoaded()
                || !BuiltInRegistries.BLOCK.containsKey(id("easy_villagers", "trader"))) {
            helper.succeed();
            return;
        }

        try {
            BlockEntity trader = createRealVampireTrader(helper, new BlockPos(1, 1, 1));
            StoredMcaVillagerInfo info = StoredMcaFactionResolver.resolve(trader);
            helper.assertTrue(info.resolved() && info.isVampire(),
                    "The stored MCA villager must remain Vampire before Cycle Trades rerolls");

            Villager storedVillager = info.villagerEntity();
            Supplier<MerchantOffers> rebuildOffers = () -> {
                MerchantOffers previousOffers = storedVillager.getOffers();
                storedVillager.setOffers(null);
                MerchantOffers rebuilt = storedVillager.getOffers();
                boolean restored = McaVampireSpecialTradeService.restoreOrdinaryOffersIfEmpty(
                        rebuilt,
                        previousOffers
                );
                helper.assertTrue(restored,
                        "A stored MCA Trader whose temporary vanilla entity cannot regenerate offers must retain its ordinary persisted offers");
                return rebuilt;
            };

            var firstSuccess = McaVampireSpecialTradeService.rebuildForCycle(
                    storedVillager,
                    rebuildOffers,
                    randomWhoseFirstNativeRollIs(0),
                    "GAMETEST_STORED_TRADER_SUCCESS_1"
            );
            helper.assertTrue(hasConvertedVampirismOffer(firstSuccess),
                    "A successful Cycle Trades roll must add one Vampirism converted-villager offer");
            helper.assertTrue(countConvertedVampirismOffers(firstSuccess) == 1,
                    "A Cycle Trades list must not duplicate the special Vampirism offer");

            var nativeMiss = McaVampireSpecialTradeService.rebuildForCycle(
                    storedVillager,
                    rebuildOffers,
                    randomWhoseFirstNativeRollIs(1),
                    "GAMETEST_STORED_TRADER_MISS"
            );
            helper.assertTrue(!hasConvertedVampirismOffer(nativeMiss),
                    "A native failed one-in-three roll may omit the special offer from that new list");

            var secondSuccess = McaVampireSpecialTradeService.rebuildForCycle(
                    storedVillager,
                    rebuildOffers,
                    randomWhoseFirstNativeRollIs(0),
                    "GAMETEST_STORED_TRADER_SUCCESS_2"
            );
            helper.assertTrue(hasConvertedVampirismOffer(secondSuccess),
                    "A later Cycle Trades reroll must remain eligible after a failed prior roll");
            helper.assertTrue(countConvertedVampirismOffers(secondSuccess) == 1,
                    "A later successful reroll must still contain exactly one special offer");

            Method refresh = trader.getClass().getMethod("getVillager");
            refresh.invoke(trader);
            trader.setChanged();
            StoredMcaVillagerInfo reopenedInfo = StoredMcaFactionResolver.resolve(trader);
            helper.assertTrue(hasConvertedVampirismOffer(reopenedInfo.offers()),
                    "The successful stored-Trader reroll must persist after the Trader is resolved again");

            TradeCycleEligibility.lockAfterTrade(storedVillager);
            helper.assertTrue(!TradeCycleEligibility.isAvailable(storedVillager, secondSuccess, false),
                    "Cycle Trades must remain locked after the first completed stored-villager trade");
            helper.succeed();
        } catch (ReflectiveOperationException | RuntimeException exception) {
            helper.fail("Could not verify stored Vampire Trader Cycle Trades rerolls: " + exception);
        }
    }

    /**
     * Exercises the server packet dispatcher. Wrapper merchants are not
     * vanilla Villagers, so the native Easy Villagers handler would return
     * immediately; the compat packet hook must consume the active session.
     */
    @GameTest(template = "gametest/empty")
    public static void storedVampireTraderMessageDispatchesCycleSession(GameTestHelper helper) {
        if (!McaEvCompatBridge.isMcaEvCompatLoaded()
                || !BuiltInRegistries.BLOCK.containsKey(id("easy_villagers", "trader"))) {
            helper.succeed();
            return;
        }

        try {
            BlockEntity trader = createRealVampireTrader(helper, new BlockPos(1, 1, 1));
            ServerPlayer player = new ServerPlayer(
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    new GameProfile(UUID.randomUUID(), "test-vampire-trader-cycle")
            );
            StoredMcaVillagerInfo info = StoredMcaFactionResolver.resolve(trader);
            EasyVillagersFactionMerchantSession session =
                    EasyVillagersFactionMerchantSession.create(player, info, PlayerFaction.VAMPIRE);
            EasyVillagersFactionMerchant merchant = new EasyVillagersFactionMerchant(session);
            merchant.setTradingPlayer(player);
            MerchantMenu menu = new MerchantMenu(1, player.getInventory(), merchant);
            player.containerMenu = menu;

            McaVampireSpecialTradeService.resetForOfferReroll(info.villagerEntity());
            helper.assertTrue(
                    EasyVillagersTradeCycleService.handleCycleTrades(player, "GAMETEST_MESSAGE"),
                    "The server packet dispatcher must consume a stored Vampire Trader session"
            );

            helper.assertTrue(McaVampireSpecialTradeService.isRollCompleted(info.villagerEntity()),
                    "The server packet dispatcher must dispatch a stored Vampire Trader session before Easy Villagers returns for its wrapper merchant");
            helper.assertTrue(session.isCycleTradesAvailable(),
                    "An untraded stored Vampire Trader must remain eligible after a GUI-event cycle");
            session.close("GameTest GUI cycle dispatch completed");
            player.containerMenu = player.inventoryMenu;
            helper.succeed();
        } catch (ReflectiveOperationException | RuntimeException exception) {
            helper.fail("Could not verify stored Vampire Trader packet cycle dispatch: " + exception);
        }
    }

    @GameTest(template = "gametest/empty")
    public static void normalVampireMessageRecoversMissingFactionSession(GameTestHelper helper) {
        if (!ModList.get().isLoaded("easy_villagers")) {
            // The base profile deliberately verifies the no-op optional-mod path.
            helper.succeed();
            return;
        }
        helper.assertTrue(McaEvCompatBridge.canRecoverEasyVillagersCycleTrades(),
                "The Easy Villagers Cycle Trades recovery boundary must be available for the Easy profile");
        VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.get().create(helper.getLevel());
        helper.assertTrue(villager != null, "MCA male villager type must create an entity");
        // Use a real professional MCA merchant. An unemployed MCA resident has
        // no offer list to rebuild, and Vampirism deliberately does not consume
        // its one-in-three roll for that non-trading case.
        villager.setVillagerData(villager.getVillagerData()
                .setProfession(VillagerProfession.FARMER)
                .setLevel(2));
        villager.setVillagerXp(10);
        McaTraitBridge.addVampireTrait(villager);
        McaVampireStateService.markConverted(villager, null);
        villager.getOffers().add(new MerchantOffer(
                new ItemStack(Items.EMERALD, 20),
                new ItemStack(Items.BREAD),
                12,
                1,
                0.05F
        ));

        ServerPlayer player = new ServerPlayer(
                helper.getLevel().getServer(),
                helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "test-normal-vampire-cycle")
        );
        player.containerMenu = new MerchantMenu(1, player.getInventory(), villager);

        // Reproduce the field failure exactly: the prior list has already
        // consumed a successful roll, but there is no session map entry when
        // Easy Villagers receives the Cycle Trades click.
        MerchantOffers staleOffers = McaVampireSpecialTradeService.rebuildForCycle(
                villager,
                villager::getOffers,
                randomWhoseFirstNativeRollIs(0),
                "GAMETEST_STALE_MCA_VAMPIRE_LIST"
        );
        helper.assertTrue(hasConvertedVampirismOffer(staleOffers),
                "The stale MCA list must contain its former special offer");
        helper.assertTrue(McaVampireSpecialTradeService.isRollCompleted(villager)
                        && McaVampireSpecialTradeService.wasGenerated(villager),
                "The stale list must retain the marker which used to trap native Easy Villagers rerolls");
        helper.assertTrue(!McaFactionTradeSession.hasActiveSessionFor(player, villager),
                "The regression must start without a pre-created MCA faction session");
        helper.assertTrue(!EasyVillagersTradeCycleService.handleExistingCycleTrades(player, "GAMETEST_MESSAGE"),
                "The regression must start without an active Cycle Trades session");

        // The C2S boundary must recover this MCA menu before Easy Villagers
        // clears the list without resetting the special-offer marker.
        helper.assertTrue(EasyVillagersTradeCycleService.handleCycleTrades(player, "GAMETEST_MESSAGE"),
                "The server packet dispatcher must recover an MCA Vampire menu");
        helper.assertTrue(McaFactionTradeSession.hasActiveSessionFor(player, villager),
                "The server packet dispatcher must recover a faction session for a live MCA Vampire menu");
        helper.assertTrue(McaVampireSpecialTradeService.isRollCompleted(villager),
                "A normal MCA Vampire Cycle Trades action must run exactly one fresh special-offer roll");
        helper.assertTrue(
                McaVampireSpecialTradeService.wasGenerated(villager)
                        == hasConvertedVampirismOffer(villager.getOffers()),
                "The recovered reroll must replace the stale special-offer result instead of keeping its old marker"
        );
        helper.assertTrue(countConvertedVampirismOffers(villager.getOffers()) <= 1,
                "A recovered MCA reroll must not duplicate the special Vampirism offer");
        McaFactionTradeSession.closeForPlayer(player.getUUID(), "GameTest recovered normal cycle dispatch completed");
        player.containerMenu = player.inventoryMenu;
        helper.succeed();
    }

    @GameTest(template = "gametest/empty")
    public static void normalMcaMessageRecoversMissingNativeSession(GameTestHelper helper) {
        if (!ModList.get().isLoaded("easy_villagers")) {
            // The base profile deliberately verifies the no-op optional-mod path.
            helper.succeed();
            return;
        }
        helper.assertTrue(McaEvCompatBridge.canRecoverEasyVillagersCycleTrades(),
                "The Easy Villagers Cycle Trades recovery boundary must be available for the Easy profile");
        VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.get().create(helper.getLevel());
        helper.assertTrue(villager != null, "MCA male villager type must create an entity");
        villager.setVillagerData(villager.getVillagerData()
                .setProfession(VillagerProfession.FARMER)
                .setLevel(2));
        villager.setVillagerXp(10);
        villager.getOffers().add(new MerchantOffer(
                new ItemStack(Items.EMERALD, 20),
                new ItemStack(Items.BREAD),
                12,
                1,
                0.05F
        ));

        ServerPlayer player = new ServerPlayer(
                helper.getLevel().getServer(),
                helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "test-normal-mca-native-cycle")
        );
        player.containerMenu = new MerchantMenu(1, player.getInventory(), villager);
        helper.assertTrue(!McaNativeTradeCycleSession.hasActiveSessionFor(player, villager),
                "The native MCA recovery regression must begin without a session");

        helper.assertTrue(EasyVillagersTradeCycleService.handleCycleTrades(player, "GAMETEST_MESSAGE"),
                "The server packet dispatcher must recover an ordinary MCA menu");
        helper.assertTrue(McaNativeTradeCycleSession.hasActiveSessionFor(player, villager),
                "The server packet dispatcher must recover an ordinary MCA native Cycle Trades session");
        helper.assertTrue(!villager.getOffers().isEmpty(),
                "The recovered ordinary MCA menu must retain a generated offer list");
        McaNativeTradeCycleSession.closeForPlayer(player.getUUID(), "GameTest recovered native MCA session");
        player.containerMenu = player.inventoryMenu;
        helper.succeed();
    }

    @GameTest(template = "gametest/empty")
    public static void merchantMenuCapturesMerchantWithoutPrivateFieldShadow(GameTestHelper helper) {
        VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.get().create(helper.getLevel());
        helper.assertTrue(villager != null, "MCA male villager type must create an entity");
        villager.getOffers().add(new MerchantOffer(
                new ItemStack(Items.EMERALD, 5),
                new ItemStack(Items.BREAD),
                12,
                1,
                0.05F
        ));

        ServerPlayer player = new ServerPlayer(
                helper.getLevel().getServer(),
                helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "test-merchant-menu-capture")
        );
        MerchantMenu menu = new MerchantMenu(1, player.getInventory(), villager);
        player.containerMenu = menu;

        helper.assertTrue(menu instanceof MerchantMenuTradeAccess,
                "MerchantMenu must expose the merchant captured from its constructor");
        helper.assertTrue(((MerchantMenuTradeAccess) menu).mcaVampCompat$getTrader() == villager,
                "The constructor capture must identify the exact MCA merchant without a private field shadow");

        McaFactionTradeSession.open(player, villager, PlayerFaction.VAMPIRE);
        helper.assertTrue(McaFactionTradeSession.hasActiveSessionFor(player, villager),
                "The regression must begin with a native MCA session to close");
        VampiricVillagerEvents.onMerchantMenuClosed(new PlayerContainerEvent.Close(player, menu));
        helper.assertTrue(!McaFactionTradeSession.hasActiveSessionFor(player, villager),
                "The Forge menu-close event must restore and close the MCA session without MerchantMenu.removed injection");
        player.containerMenu = player.inventoryMenu;
        helper.succeed();
    }

    @GameTest(template = "gametest/empty")
    public static void cycleDispatcherLeavesVanillaMerchantToEasyVillagers(GameTestHelper helper) {
        Villager villager = new Villager(net.minecraft.world.entity.EntityType.VILLAGER, helper.getLevel());
        villager.setVillagerData(villager.getVillagerData()
                .setProfession(VillagerProfession.FARMER)
                .setLevel(2));
        villager.getOffers().add(new MerchantOffer(
                new ItemStack(Items.EMERALD, 20),
                new ItemStack(Items.BREAD),
                12,
                1,
                0.05F
        ));

        ServerPlayer player = new ServerPlayer(
                helper.getLevel().getServer(),
                helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "test-vanilla-cycle-fallback")
        );
        player.containerMenu = new MerchantMenu(1, player.getInventory(), villager);

        helper.assertTrue(!EasyVillagersTradeCycleService.handleCycleTrades(player, "GAMETEST_VANILLA"),
                "A merchant with no compat session must remain under Easy Villagers' native Cycle Trades handler");
        player.containerMenu = player.inventoryMenu;
        helper.succeed();
    }

    @GameTest(template = "gametest/empty")
    public static void realMcaDirectTradeEntryBlocksVampireForHunter(GameTestHelper helper) {
        VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.get().create(helper.getLevel());
        helper.assertTrue(villager != null, "MCA male villager type must create an entity");
        McaTraitBridge.addHunterTrait(villager);
        villager.getOffers().add(new MerchantOffer(
                new ItemStack(Items.EMERALD, 20),
                new ItemStack(Items.BREAD),
                12,
                1,
                0.05F
        ));
        helper.assertTrue(McaHunterTradeService.isHunterTradeVillager(villager),
                "The live MCA villager must be classified as a Hunter before trading");

        Player vampire = helper.makeMockPlayer();
        setPlayerFaction(vampire, id("vampirism", "vampire"));
        helper.assertTrue(
                McaFactionTradeService.onMobInteractHead(vampire, villager, InteractionHand.MAIN_HAND)
                        == net.minecraft.world.InteractionResult.CONSUME,
                "The empty-hand MCA interaction gate must reject a Vampire before the merchant helper runs"
        );
        villager.beginTradeWith(vampire);

        helper.assertTrue(vampire.containerMenu == vampire.inventoryMenu,
                "MCA's direct trade entry must not bypass Hunter refusal for a Vampire");
        helper.succeed();
    }

    @GameTest(template = "gametest/empty")
    public static void mcaDialogueTradeCommandBlocksVampireForHunter(GameTestHelper helper) {
        VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.get().create(helper.getLevel());
        helper.assertTrue(villager != null, "MCA male villager type must create an entity");
        McaTraitBridge.addHunterTrait(villager);
        helper.assertTrue(McaHunterTradeService.isHunterTradeVillager(villager),
                "The dialogue target must be a Hunter before its trade command is handled");

        ServerPlayer vampire = new ServerPlayer(
                helper.getLevel().getServer(),
                helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "test-dialogue-vampire-trade")
        );
        setPlayerFaction(vampire, id("vampirism", "vampire"));

        boolean handled = new VillagerCommandHandler(villager).handle(vampire, "trade");

        helper.assertTrue(handled,
                "The MCA dialogue trade command must be consumed when a Hunter refuses a Vampire");
        helper.assertTrue(vampire.containerMenu == vampire.inventoryMenu,
                "The dialogue invoker must not open a merchant menu for an enemy faction");
        helper.succeed();
    }

    @GameTest(template = "gametest/empty")
    public static void realMcaHunterShiftTradeUsesNativeOffer(GameTestHelper helper) {
        VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.get().create(helper.getLevel());
        helper.assertTrue(villager != null, "MCA male villager type must create an entity");
        McaTraitBridge.addHunterTrait(villager);
        MerchantOffer offer = new MerchantOffer(
                new ItemStack(Items.EMERALD, 20),
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
                new GameProfile(UUID.randomUUID(), "test-native-hunter-trade")
        );
        setPlayerFaction(hunter, id("vampirism", "hunter"));
        // GameTest mock players have no packet connection, so opening MCA's
        // screen would fail while sending its client menu packet. Build the
        // same native MerchantMenu after applying the production session.
        villager.setTradingPlayer(hunter);
        McaFactionTradeService.prepareNativeMcaTrade(hunter, villager);
        MerchantMenu merchantMenu = new MerchantMenu(1, hunter.getInventory(), villager);
        helper.assertTrue(merchantMenu.getOffers().get(0) == offer,
                "The native MCA menu must transact with the persistent offer instance");
        helper.assertTrue(merchantMenu.getOffers().get(0).getCostA().getCount() == 17,
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
        VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.get().create(helper.getLevel());
        helper.assertTrue(villager != null, "MCA male villager type must create an entity");
        MerchantOffer offer = new MerchantOffer(
                new ItemStack(Items.EMERALD, 20),
                new ItemStack(Items.BREAD),
                12,
                1,
                0.05F
        );
        villager.getOffers().add(offer);

        Player player = helper.makeMockPlayer();
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
            VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.get().create(helper.getLevel());
            helper.assertTrue(villager != null, "MCA male villager type must create an entity");
            villager.setVillagerData(villager.getVillagerData().setProfession(ProfessionsMCA.GUARD.get()));
            villager.setVillagerXp(8);
            // This is a manually assigned Hunter guard. Unlike a temporary
            // Hunter-village assignment it must survive being moved outside
            // the village area.
            McaTraitBridge.addHunterTrait(villager);
            villager.getOffers().add(new MerchantOffer(
                    new ItemStack(Items.EMERALD, 4),
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
            boolean placed = (Boolean) spawn.invoke(
                    null,
                    helper.getLevel(),
                    untradedStack,
                    helper.absolutePos(new BlockPos(5, 2, 5)),
                    0.0F,
                    "gametest"
            );
            helper.assertTrue(placed, "mca-ev-compat must place the captured MCA guard");
            VillagerEntityMCA placedGuard = (VillagerEntityMCA) helper.getLevel().getEntity(villager.getUUID());
            helper.assertTrue(placedGuard != null, "The placed guard must retain its stored UUID");
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
    public static void mcaEvBreederBlocksInfertileAndStoresCanonicalMcaChild(GameTestHelper helper) {
        if (!McaEvCompatBridge.isMcaEvCompatLoaded()
                || !BuiltInRegistries.BLOCK.containsKey(id("easy_villagers", "breeder"))
                || !BuiltInRegistries.ITEM.containsKey(id("easy_villagers", "villager"))) {
            helper.succeed();
            return;
        }

        try {
            Class<?> compatLogic = Class.forName("com.guilh.mcaevcompat.CompatLogic");
            Method createStack = compatLogic.getMethod(
                    "createMcaVillagerStack", Villager.class, long.class);
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

            VillagerEntityMCA infertileParent = EntitiesMCA.FEMALE_VILLAGER.get().create(helper.getLevel());
            VillagerEntityMCA fertileParent = EntitiesMCA.MALE_VILLAGER.get().create(helper.getLevel());
            helper.assertTrue(infertileParent != null && fertileParent != null,
                    "MCA parent entity types must create entities");
            ItemStack infertileStack = (ItemStack) createStack.invoke(
                    null, infertileParent, helper.getLevel().getGameTime());
            ItemStack fertileStack = (ItemStack) createStack.invoke(
                    null, fertileParent, helper.getLevel().getGameTime());
            // Infertile is data-driven in MCA 7.6.x rather than a public Java constant.
            // Set the same canonical stored value that the editor persists and that the
            // Breeder receives, so the test does not rely on a version-specific field.
            CompoundTag infertileData = (CompoundTag) copyStoredData.invoke(null, infertileStack);
            helper.assertTrue(infertileData != null, "A stored MCA parent must expose its canonical data");
            CompoundTag infertileTraits = infertileData.contains("Traits", Tag.TAG_COMPOUND)
                    ? infertileData.getCompound("Traits")
                    : new CompoundTag();
            infertileTraits.putBoolean("mca:infertile", true);
            infertileData.put("Traits", infertileTraits);
            // MCA x Easy Villagers 1.0.2 owns this exact snapshot under "villager".
            infertileStack.getOrCreateTag().put("villager", infertileData);
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
            helper.assertTrue(ItemStack.isSameItemSameTags(infertileStack, infertileBefore)
                            && ItemStack.isSameItemSameTags(fertileStack, fertileBefore),
                    "The breeder must not mutate either stored MCA parent while checking fertility");

            VillagerEntityMCA fertileMother = EntitiesMCA.FEMALE_VILLAGER.get().create(helper.getLevel());
            VillagerEntityMCA fertileFather = EntitiesMCA.MALE_VILLAGER.get().create(helper.getLevel());
            helper.assertTrue(fertileMother != null && fertileFather != null,
                    "MCA fertile parent entity types must create entities");
            ItemStack motherStack = (ItemStack) createStack.invoke(
                    null, fertileMother, helper.getLevel().getGameTime());
            ItemStack fatherStack = (ItemStack) createStack.invoke(
                    null, fertileFather, helper.getLevel().getGameTime());
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
            boolean hasGender = childData != null && (childData.contains("Gender", Tag.TAG_ANY_NUMERIC)
                    || childData.contains("gender", Tag.TAG_ANY_NUMERIC));
            boolean hasTraits = childData != null && (childData.contains("Traits", Tag.TAG_COMPOUND)
                    || childData.contains("traits", Tag.TAG_COMPOUND));
            helper.assertTrue(childData != null
                            && childData.getString("id").startsWith("mca:")
                            && childData.getInt("Age") < 0
                            && hasGender
                            && hasTraits,
                    "The Breeder must retain MCA's canonical baby identity, age, genetics/traits payload and family-producing flow");
            helper.succeed();
        } catch (ReflectiveOperationException | RuntimeException exception) {
            helper.fail("Could not verify MCA x Easy Villagers Breeder fertility and child snapshot: " + exception);
        }
    }

    @GameTest(template = "gametest/empty")
    public static void hunterWeaponSelectionSurvivesMcaEquipmentRefresh(GameTestHelper helper) {
        try {
            VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.get().create(helper.getLevel());
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
            Method run = EquipmentTask.class.getDeclaredMethod(
                    "run",
                    ServerLevel.class,
                    VillagerEntityMCA.class,
                    long.class
            );
            run.setAccessible(true);
            run.invoke(task, helper.getLevel(), villager, helper.getLevel().getGameTime());

            helper.assertTrue(ItemStack.isSameItemSameTags(
                            villager.getItemInHand(InteractionHand.MAIN_HAND), weapon),
                    "EquipmentTask must not reroll or remove a valid Hunter weapon");
            helper.assertTrue(selection.equals(ModCapabilities.get(villager).orElseThrow().getHunterWeaponSelection()),
                    "Equipment refresh must keep the persisted Hunter weapon selection");
            helper.assertTrue(ModCapabilities.get(villager).orElseThrow().isOriginalMainHandItemSaved(),
                    "Equipment refresh must not replace or clear the preserved original hand");

            // Exercise the real MCA recovery boundary. MCA 7.6 normally moves
            // this food to the dominant hand; the Hunter hook must consume it
            // directly from inventory and leave the selected weapon untouched.
            villager.getInventory().setItem(0, new ItemStack(Items.BREAD, 2));
            villager.setHealth(Math.max(1.0F, villager.getMaxHealth() - 6.0F));
            float healthBeforeRecovery = villager.getHealth();
            Method findAndEquipToMain = VillagerEntityMCA.class.getDeclaredMethod(
                    "findAndEquipToMain",
                    Predicate.class
            );
            findAndEquipToMain.setAccessible(true);
            boolean recovered = (Boolean) findAndEquipToMain.invoke(
                    villager,
                    (Predicate<ItemStack>) ItemStack::isEdible
            );
            helper.assertTrue(recovered,
                    "A persistent Hunter must handle MCA recovery food without moving it to the weapon hand");
            helper.assertTrue(ItemStack.isSameItemSameTags(
                            villager.getItemInHand(InteractionHand.MAIN_HAND), weapon),
                    "Recovery must keep the selected Hunter weapon continuously equipped");
            helper.assertTrue(villager.getInventory().getItem(0).getCount() == 1,
                    "Recovery must consume exactly one real inventory food item");
            helper.assertTrue(villager.getHealth() > healthBeforeRecovery,
                    "MCA's direct recovery must apply the normal nutrition-based healing exactly once");

            // A real Hunter can also enter the temporary defensive mode.  That mode must
            // never take ownership of, restore, or reroll the persistent Hunter weapon.
            VampiricVillagerState hunterState = ModCapabilities.get(villager).orElseThrow();
            hunterState.enterDefensiveMode(helper.getLevel().getGameTime(), 1_000L, "gametest", null);
            helper.assertTrue(!McaDefensiveVillagerService.isTemporaryDefenseOnly(villager, hunterState),
                    "A real Hunter must not be treated as a temporary-defense-only weapon owner");
            McaDefensiveVillagerService.reconcileHunterWeapon(villager);
            helper.assertTrue(ItemStack.isSameItemSameTags(
                            villager.getItemInHand(InteractionHand.MAIN_HAND), weapon),
                    "Temporary defense must not replace a real Hunter weapon");
            McaDefensiveVillagerService.clearDefense(villager, "gametest");
            helper.assertTrue(ItemStack.isSameItemSameTags(
                            villager.getItemInHand(InteractionHand.MAIN_HAND), weapon),
                    "Leaving temporary defense must not remove a real Hunter weapon");
            helper.succeed();
        } catch (ReflectiveOperationException | RuntimeException exception) {
            helper.fail("Could not verify Hunter pitchfork recovery after MCA equipment refresh: " + exception);
        }
    }

    @GameTest(template = "gametest/empty")
    public static void hunterWeaponSurvivesAllEquipmentWrites(GameTestHelper helper) {
        VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.get().create(helper.getLevel());
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

        ItemStack weapon = villager.getItemInHand(weaponHand).copy();
        villager.setItemInHand(weaponHand, ItemStack.EMPTY);
        helper.assertTrue(ItemStack.isSameItemSameTags(villager.getItemInHand(weaponHand), weapon),
                "A persistent Hunter weapon must reject direct slot clears");
        villager.setItemSlot(villager.getDominantSlot(), new ItemStack(Items.BREAD));
        helper.assertTrue(ItemStack.isSameItemSameTags(villager.getItemInHand(weaponHand), weapon),
                "Direct Mob#setItemSlot food writes must not displace a persistent Hunter weapon");
        villager.setItemInHand(weaponHand, new ItemStack(Items.IRON_AXE));
        helper.assertTrue(ItemStack.isSameItemSameTags(villager.getItemInHand(weaponHand), weapon),
                "Direct non-food hand writes must not displace a persistent Hunter weapon");

        ItemStack sameWeaponWithRuntimeState = weapon.copy();
        if (sameWeaponWithRuntimeState.isDamageableItem()) {
            sameWeaponWithRuntimeState.setDamageValue(1);
        }
        villager.setItemSlot(villager.getDominantSlot(), sameWeaponWithRuntimeState);
        helper.assertTrue(villager.getItemInHand(weaponHand).is(weapon.getItem()),
                "The reserved weapon must accept same-item durability/component updates without rerolling");
        ItemStack expectedWeaponAfterRuntimeUpdate = villager.getItemInHand(weaponHand).copy();
        try {
            EquipmentTask task = new EquipmentTask(ignored -> true, ignored -> EquipmentSet.GUARD_0);
            Method run = EquipmentTask.class.getDeclaredMethod(
                    "run",
                    ServerLevel.class,
                    VillagerEntityMCA.class,
                    long.class
            );
            run.setAccessible(true);
            run.invoke(task, helper.getLevel(), villager, helper.getLevel().getGameTime());
        } catch (ReflectiveOperationException exception) {
            helper.fail("Could not execute the MCA Forge equipment refresh: " + exception);
            return;
        }
        helper.assertTrue(ItemStack.isSameItemSameTags(
                        villager.getItemInHand(weaponHand), expectedWeaponAfterRuntimeUpdate),
                "MCA equipment refresh must not replace the persistent Hunter weapon after a runtime update");

        state.setHunterVillageApplied(false);
        McaDefensiveVillagerService.reconcileHunterWeapon(villager);
        helper.assertTrue(villager.getItemInHand(weaponHand).is(Items.BREAD),
                "Removing Hunter ownership must restore the original dominant-hand item");
        helper.succeed();
    }

    @GameTest(template = "gametest/empty")
    public static void hunterStakeUsesBoundedNativeMeleeAttack(GameTestHelper helper) {
        VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.get().create(helper.getLevel());
        LivingEntity target = EntityType.ZOMBIE.create(helper.getLevel());
        helper.assertTrue(villager != null && target != null, "Hunter and target entities must be available");
        villager.setAgeState(AgeState.ADULT);
        villager.setVillagerData(villager.getVillagerData().setProfession(ProfessionsMCA.GUARD.get()));
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
    public static void leftHandedHunterUsesReservedOffhandForNativeAttack(GameTestHelper helper) {
        VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.get().create(helper.getLevel());
        LivingEntity target = EntityType.ZOMBIE.create(helper.getLevel());
        helper.assertTrue(villager != null && target != null, "Left-handed Hunter and target entities must be available");

        // Attach the real Forge capability before writing faction state. An
        // unattached test entity receives its final capability instance only
        // when it enters the level, unlike a villager in normal gameplay.
        villager.moveTo(helper.absolutePos(new BlockPos(2, 2, 2)), 0.0F, 0.0F);
        target.moveTo(helper.absolutePos(new BlockPos(3, 2, 2)), 0.0F, 0.0F);
        helper.getLevel().addFreshEntity(villager);
        helper.getLevel().addFreshEntity(target);

        villager.setAgeState(AgeState.ADULT);
        villager.getTraits().addTrait(Traits.LEFT_HANDED);
        InteractionHand weaponHand= villager.getDominantHand();
        helper.assertTrue(villager.getTraits().hasTrait(Traits.LEFT_HANDED),
                "The test villager must retain its native left-handed trait");
        villager.setVillagerData(villager.getVillagerData().setProfession(ProfessionsMCA.GUARD.get()));
        VampiricVillagerState state = ModCapabilities.get(villager).orElseThrow();
        state.setHunterVillageApplied(true);
        state.setHunterWeaponSelection("stake");
        McaDefensiveVillagerService.reconcileHunterWeapon(villager);

        ItemStack offhandWeapon = villager.getItemInHand(weaponHand).copy();
        helper.assertTrue(id("vampirism", "stake").equals(BuiltInRegistries.ITEM.getKey(offhandWeapon.getItem())),
                "A left-handed Hunter must reserve the selected weapon in dominant hand");
        helper.assertTrue(state.isOriginalMainHandItemSaved(),
                "A left-handed Hunter must preserve the item displaced from the reserved hand");
        helper.assertTrue((weaponHand==InteractionHand.OFF_HAND?"off_hand":"main_hand").equals(state.getHunterWeaponHand()),
                "The persisted Hunter weapon hand must be dominant hand for a left-handed villager");
        helper.assertTrue(McaDefensiveVillagerService.shouldUseDominantHunterWeaponForNativeAttack(villager),
                "The native attack redirect must recognize the reserved left-handed Hunter weapon");
        helper.assertTrue(villager.getItemInHand(weaponHand==InteractionHand.OFF_HAND?InteractionHand.MAIN_HAND:InteractionHand.OFF_HAND).isEmpty(),
                "The test must exercise the native attack with an empty other hand");

        // Both MCA 7.6 attack layouts read other hand while calculating weapon
        // enchantment damage: the legacy override directly, and newer builds
        // through vanilla Mob. Sharpness on the reserved offhand therefore
        // proves that the compat supplies the dominant Hunter weapon.
        EnchantmentHelper.setEnchantments(Map.of(Enchantments.SHARPNESS, 1), offhandWeapon);
        villager.setItemSlot(villager.getDominantSlot(), offhandWeapon);
        ItemStack equippedOffhandWeapon = villager.getItemInHand(weaponHand);
        helper.assertTrue(EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SHARPNESS, equippedOffhandWeapon) == 1,
                "A same-item runtime update must preserve the dominant hand weapon enchantment");
        helper.assertTrue(Math.abs(EnchantmentHelper.getDamageBonus(equippedOffhandWeapon, target.getMobType()) - 1.0F) < 0.001F,
                "Sharpness I on the reserved dominant hand weapon must contribute one native damage point");

        helper.assertTrue(McaDefensiveVillagerService.shouldUseDominantHunterWeaponForNativeAttack(villager),
                "A live Hunter must keep its reserved dominant hand weapon through the native attack");
        helper.assertTrue(Math.abs(EnchantmentHelper.getDamageBonus(
                        villager.getItemInHand(weaponHand), target.getMobType()) - 1.0F) < 0.001F,
                "Adding the Hunter to the level must not strip the reserved dominant hand weapon enchantment");
        var targetArmor = target.getAttribute(Attributes.ARMOR);
        helper.assertTrue(targetArmor != null, "The test target must expose its armor attribute");
        targetArmor.setBaseValue(0.0D);
        float healthBefore = target.getHealth();

        helper.assertTrue(McaSpecialVillagerCombatService.performMeleeAttack(
                        villager, target, McaSpecialVillagerType.HUNTER),
                "A left-handed Hunter attack must enter MCA's native Mob attack flow");
        float dealt = healthBefore - target.getHealth();
        float expectedDamage = (float) McaVampirismCompatConfig.hunterVillagerAttackDamage() + 1.0F;
        helper.assertTrue(Math.abs(dealt - expectedDamage) <= 0.1F,
                "The native left-handed attack must read Sharpness from the reserved dominant hand weapon "
                        + "(expected " + expectedDamage + ", got " + dealt + ")");
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
        helper.assertTrue(helper.getLevel().getServer().getAdvancements().getAdvancement(
                        id(McaVampirismCompat.MOD_ID, "discover_occult_village")) != null,
                "The occult village discovery advancement must load");
        helper.succeed();
    }

    @GameTest(template = "gametest/empty")
    public static void inheritedWerewolfStateSurvivesSerialization(GameTestHelper helper) {
        VampiricVillagerState inherited = new VampiricVillagerState();
        inherited.markInheritedWerewolf(helper.getLevel().getGameTime());
        inherited.markFactionInheritanceProcessed();

        CompoundTag serialized = inherited.serializeNBT();
        VampiricVillagerState restored = new VampiricVillagerState();
        restored.deserializeNBT(serialized);

        helper.assertTrue(restored.isWerewolfConverted(), "Inherited child must remain converted");
        helper.assertTrue(restored.isWerewolfAppearanceApplied(), "Inherited appearance marker must persist");
        helper.assertTrue(restored.isWerewolfPartialForm(), "Inherited partial-form marker must persist");
        helper.assertTrue(restored.isFactionInheritanceProcessed(), "Inheritance decision must remain processed");
        helper.succeed();
    }

    @GameTest(template = "gametest/empty")
    public static void editorFactionTraitsApplyAndRemoveCompleteStates(GameTestHelper helper) {
        VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.get().create(helper.getLevel());
        helper.assertTrue(villager != null, "MCA male villager type must create an entity");
        villager.setAgeState(AgeState.ADULT);

        McaTraitBridge.addVampireTrait(villager);
        McaEditorTraitSyncService.reconcileEditedTraits(villager);
        helper.assertTrue(McaVampireStateService.isVampire(villager),
                "Adding the Vampire trait in the editor must convert the villager");

        McaTraitBridge.removeVampireTrait(villager);
        if (!McaWerewolvesCompat.isAvailable()) {
            McaTraitBridge.addWerewolfTrait(villager);
            McaEditorTraitSyncService.reconcileCompletedEditorSave(villager);
            helper.assertTrue(!McaTraitBridge.hasWerewolfTrait(villager)
                            && !McaWerewolfStateService.isWerewolf(villager),
                    "Werewolf must not be assignable when the optional Werewolves addon is absent");
            helper.succeed();
            return;
        }
        McaTraitBridge.addWerewolfTrait(villager);
        // The Editor packet has loaded the former Forge capability before its
        // tail hook runs. The selected Werewolf trait must therefore win over
        // the still-present Vampire capability, and remain authoritative on
        // the normal runtime synchronization that follows.
        McaEditorTraitSyncService.reconcileCompletedEditorSave(villager);
        McaSpecialVillagerRuntimeService.synchronizeNow(villager);
        helper.assertTrue(!McaVampireStateService.isVampire(villager),
                "Editor Vampire-to-Werewolf replacement must clear old Vampire state");
        helper.assertTrue(McaWerewolfStateService.isWerewolf(villager),
                "Editor Vampire-to-Werewolf replacement must preserve the selected Werewolf state");
        helper.assertTrue(!McaTraitBridge.hasVampireTrait(villager)
                        && McaTraitBridge.hasWerewolfTrait(villager),
                "Editor trait replacement must survive the post-save runtime synchronization");

        McaTraitBridge.addHunterTrait(villager);
        McaEditorTraitSyncService.reconcileEditedTraits(villager);
        helper.assertTrue(McaHunterAlignmentService.isMcaHunterVillager(villager),
                "Adding the Hunter trait in the editor must align the villager as a Hunter");

        McaTraitBridge.removeHunterTrait(villager);
        McaEditorTraitSyncService.reconcileEditedTraits(villager);
        helper.assertTrue(!McaHunterAlignmentService.isMcaHunterVillager(villager),
                "Removing the Hunter trait in the editor must clear Hunter alignment");

        McaTraitBridge.addWerewolfTrait(villager);
        McaEditorTraitSyncService.reconcileEditedTraits(villager);
        helper.assertTrue(McaWerewolfStateService.isWerewolf(villager),
                "Adding the Werewolf trait in the editor must convert the villager");

        McaTraitBridge.removeWerewolfTrait(villager);
        McaEditorTraitSyncService.reconcileEditedTraits(villager);
        helper.assertTrue(!McaWerewolfStateService.isWerewolf(villager),
                "Removing the Werewolf trait in the editor must clear the conversion");
        helper.succeed();
    }

    @GameTest(template = "gametest/empty")
    public static void editorTraitRemovalImmediatelyDropsFactionTargets(GameTestHelper helper) {
        if (!McaWerewolvesCompat.isAvailable()) {
            helper.succeed();
            return;
        }
        VillagerEntityMCA hunter = EntitiesMCA.MALE_VILLAGER.get().create(helper.getLevel());
        VillagerEntityMCA werewolf = EntitiesMCA.FEMALE_VILLAGER.get().create(helper.getLevel());
        helper.assertTrue(hunter != null && werewolf != null, "MCA villagers must create for editor target invalidation");
        hunter.moveTo(helper.absolutePos(new BlockPos(2, 2, 2)), 0.0F, 0.0F);
        werewolf.moveTo(helper.absolutePos(new BlockPos(3, 2, 2)), 0.0F, 0.0F);
        helper.getLevel().addFreshEntity(hunter);
        helper.getLevel().addFreshEntity(werewolf);
        hunter.setAgeState(AgeState.ADULT);
        werewolf.setAgeState(AgeState.ADULT);

        McaTraitBridge.addHunterTrait(hunter);
        McaEditorTraitSyncService.reconcileEditedTraits(hunter);
        McaTraitBridge.addWerewolfTrait(werewolf);
        McaEditorTraitSyncService.reconcileEditedTraits(werewolf);
        hunter.setTarget(werewolf);
        helper.assertTrue(hunter.getTarget() == werewolf,
                "A Hunter must initially be able to target the selected Werewolf faction");

        McaTraitBridge.removeWerewolfTrait(werewolf);
        McaEditorTraitSyncService.reconcileEditedTraits(werewolf);

        helper.assertTrue(hunter.getTarget() == null,
                "Removing a faction trait in the editor must immediately clear existing faction targets");
        helper.assertTrue(McaSpecialVillagerCombatService.shouldRejectTargetAssignment(hunter, werewolf),
                "The now-human villager must be rejected as a fresh Hunter faction target");
        helper.succeed();
    }

    @GameTest(template = "gametest/empty")
    public static void nativeWerewolfTransformationCannotReplaceMcaVillagers(GameTestHelper helper) {
        if (!ModList.get().isLoaded("werewolves")) {
            helper.succeed();
            return;
        }

        VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.get().create(helper.getLevel());
        helper.assertTrue(villager != null, "MCA male villager type must create an entity");
        villager.setAgeState(AgeState.ADULT);
        villager.setCustomName(Component.literal("Seleris"));
        VillagerData originalData = villager.getVillagerData().setProfession(ProfessionsMCA.GUARD.get());
        villager.setVillagerData(originalData);
        villager.getOffers().add(new MerchantOffer(
                new ItemStack(Items.EMERALD, 4),
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
            helper.assertTrue(!villager.isRemoved() && helper.getLevel().getEntity(originalUuid) == villager,
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

        CompoundTag root = stack.getOrCreateTag();
        root.put(McaEvStatePayload.PAYLOAD_KEY, payload);
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

    private static BlockEntity createRealHunterTrader(
            GameTestHelper helper,
            BlockPos traderPos,
            boolean addOffer
    ) throws ReflectiveOperationException {
        VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.get().create(helper.getLevel());
        if (villager == null) {
            throw new IllegalStateException("MCA male villager type did not create an entity");
        }
        McaTraitBridge.addHunterTrait(villager);
        if (addOffer) {
            villager.getOffers().add(new MerchantOffer(
                    new ItemStack(Items.EMERALD, 20),
                    new ItemStack(Items.BREAD),
                    12,
                    1,
                    0.05F
            ));
        }

        Class<?> compatLogic = Class.forName("com.guilh.mcaevcompat.CompatLogic");
        Method createStack = compatLogic.getMethod(
                "createMcaVillagerStack",
                Villager.class,
                long.class
        );
        ItemStack visualStack = (ItemStack) createStack.invoke(
                null,
                villager,
                helper.getLevel().getGameTime()
        );
        addLegacyFalseFactionPayload(visualStack);

        helper.setBlock(traderPos, BuiltInRegistries.BLOCK.get(id("easy_villagers", "trader")));
        BlockEntity blockEntity = helper.getBlockEntity(traderPos);
        Method setVillager = blockEntity.getClass().getMethod("setVillager", ItemStack.class);
        setVillager.invoke(blockEntity, visualStack);
        return blockEntity;
    }

    private static BlockEntity createRealVampireTrader(
            GameTestHelper helper,
            BlockPos traderPos
    ) throws ReflectiveOperationException {
        VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.get().create(helper.getLevel());
        if (villager == null) {
            throw new IllegalStateException("MCA male villager type did not create an entity");
        }
        McaTraitBridge.addVampireTrait(villager);
        McaVampireStateService.markConverted(villager, null);
        villager.getOffers().add(new MerchantOffer(
                new ItemStack(Items.EMERALD, 20),
                new ItemStack(Items.BREAD),
                12,
                1,
                0.05F
        ));

        Class<?> compatLogic = Class.forName("com.guilh.mcaevcompat.CompatLogic");
        Method createStack = compatLogic.getMethod(
                "createMcaVillagerStack",
                Villager.class,
                long.class
        );
        ItemStack visualStack = (ItemStack) createStack.invoke(
                null,
                villager,
                helper.getLevel().getGameTime()
        );
        addLegacyFalseFactionPayload(visualStack);

        helper.setBlock(traderPos, BuiltInRegistries.BLOCK.get(id("easy_villagers", "trader")));
        BlockEntity blockEntity = helper.getBlockEntity(traderPos);
        Method setVillager = blockEntity.getClass().getMethod("setVillager", ItemStack.class);
        setVillager.invoke(blockEntity, visualStack);
        return blockEntity;
    }

    private static RandomSource randomWhoseFirstNativeRollIs(int expectedRoll) {
        for (long seed = 0L; seed < 10_000L; seed++) {
            RandomSource candidate = RandomSource.create(seed);
            if (candidate.nextInt(3) == expectedRoll) {
                return RandomSource.create(seed);
            }
        }
        throw new IllegalStateException("Could not create a deterministic native roll for " + expectedRoll);
    }

    private static boolean hasConvertedVampirismOffer(Iterable<MerchantOffer> offers) {
        return countConvertedVampirismOffers(offers) > 0;
    }

    private static int countConvertedVampirismOffers(Iterable<MerchantOffer> offers) {
        int count = 0;
        for (MerchantOffer offer : offers) {
            if (offer != null && (isConvertedVampirismItem(offer.getBaseCostA())
                    || isConvertedVampirismItem(offer.getResult()))) {
                count++;
            }
        }
        return count;
    }

    private static boolean isConvertedVampirismItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id("vampirism", "human_heart").equals(itemId)
                || id("vampirism", "blood_bottle").equals(itemId);
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

    private static void setPlayerFaction(Player player, ResourceLocation factionId) {
        IFaction<?> faction = VampirismAPI.factionRegistry().getFactionByID(factionId);
        if (!(faction instanceof IPlayableFaction<?> playableFaction)) {
            throw new IllegalStateException("Missing playable faction " + factionId);
        }
        boolean applied = VampirismAPI.getFactionPlayerHandler(player)
                .resolve()
                .map(handler -> handler.setFactionAndLevel(playableFaction, 1))
                .orElse(false);
        if (!applied) {
            throw new IllegalStateException("Could not set player faction " + factionId);
        }
    }

    private static ResourceLocation id(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }
}
