package com.guilh.mca_vampirism_compat.gametest;

import com.mojang.authlib.GameProfile;
import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.VampiricVillagerState;
import com.guilh.mca_vampirism_compat.easyvillagers.EasyVillagersFactionMerchant;
import com.guilh.mca_vampirism_compat.easyvillagers.EasyVillagersFactionMerchantSession;
import com.guilh.mca_vampirism_compat.easyvillagers.McaEvCompatBridge;
import com.guilh.mca_vampirism_compat.easyvillagers.McaEvStatePayload;
import com.guilh.mca_vampirism_compat.easyvillagers.readonly.StoredMcaFactionResolver;
import com.guilh.mca_vampirism_compat.easyvillagers.readonly.StoredMcaVillagerInfo;
import com.guilh.mca_vampirism_compat.faction.PlayerFaction;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.service.McaDefensiveVillagerService;
import com.guilh.mca_vampirism_compat.service.McaHunterTradeService;
import com.guilh.mca_vampirism_compat.trait.McaTraitBridge;
import de.teamlapen.vampirism.api.VampirismAPI;
import de.teamlapen.vampirism.api.entity.factions.IFaction;
import de.teamlapen.vampirism.api.entity.factions.IPlayableFaction;
import net.conczin.mca.entity.EquipmentSet;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.ai.brain.tasks.EquipmentTask;
import net.conczin.mca.entity.ai.relationship.AgeState;
import net.conczin.mca.registry.EntitiesMCA;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

@GameTestHolder(McaVampirismCompat.MOD_ID)
@PrefixGameTestTemplate(false)
public final class McaVampCompatGameTests {
    private McaVampCompatGameTests() {
    }

    public static void register(RegisterGameTestsEvent event) {
        event.register(McaVampCompatGameTests.class);
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
            helper.assertTrue(session.adjustedOffers().size() == 1,
                    "The wrapper must expose the stored villager offer");
            helper.assertTrue(session.adjustedOffers().getFirst().getCostA().getCount() == 17,
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
            helper.assertTrue(session.originalOffers().getFirst().getUses() == 1,
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
            helper.assertTrue(reopenedSession.originalOffers().getFirst().getUses() == 1,
                    "The reopened Trader must preserve the completed offer use");
            reopenedSession.close("GameTest reopened-session check completed");
            helper.succeed();
        } catch (ReflectiveOperationException | RuntimeException exception) {
            helper.fail("Could not exercise the real Hunter Trader policy: " + exception);
        }
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
        villager.startTrading(vampire);

        helper.assertTrue(vampire.containerMenu == vampire.inventoryMenu,
                "VillagerEntityMCA.startTrading must not bypass Hunter refusal for a Vampire");
        helper.succeed();
    }

    @GameTest(template = "gametest/empty")
    public static void hunterPitchforkSurvivesMcaEquipmentRefresh(GameTestHelper helper) {
        try {
            VillagerEntityMCA villager = EntitiesMCA.MALE_VILLAGER.create(helper.getLevel());
            helper.assertTrue(villager != null, "MCA male villager type must create an entity");
            villager.setAgeState(AgeState.ADULT);
            villager.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
            ModCapabilities.get(villager).orElseThrow().setHunterVillageApplied(true);
            McaDefensiveVillagerService.reconcileHunterWeapon(villager);

            ItemStack pitchfork = villager.getItemInHand(InteractionHand.MAIN_HAND).copy();
            helper.assertTrue(!pitchfork.isEmpty(), "A valid Hunter must start with its pitchfork equipped");
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
                            villager.getItemInHand(InteractionHand.MAIN_HAND), pitchfork),
                    "EquipmentTask must not leave a valid Hunter without its pitchfork for a synchronization interval");
            helper.assertTrue(ModCapabilities.get(villager).orElseThrow().isOriginalMainHandItemSaved(),
                    "Equipment refresh must not replace or clear the preserved original hand");

            Field recoveryFoodUseActive = VillagerEntityMCA.class.getDeclaredField("recoveryFoodUseActive");
            recoveryFoodUseActive.setAccessible(true);
            recoveryFoodUseActive.setBoolean(villager, true);
            ItemStack recoveryFood = new ItemStack(Items.BREAD);
            villager.setItemInHand(InteractionHand.MAIN_HAND, recoveryFood);
            McaDefensiveVillagerService.reconcileHunterWeapon(villager);
            McaDefensiveVillagerService.reconcileHunterWeaponAfterMcaEquipmentTask(villager);
            helper.assertTrue(ItemStack.isSameItemSameComponents(
                            villager.getItemInHand(InteractionHand.MAIN_HAND), recoveryFood),
                    "Hunter pitchfork reconciliation must not interrupt MCA recovery food use");

            recoveryFoodUseActive.setBoolean(villager, false);
            McaDefensiveVillagerService.reconcileHunterWeapon(villager);
            helper.assertTrue(ItemStack.isSameItemSameComponents(
                            villager.getItemInHand(InteractionHand.MAIN_HAND), pitchfork),
                    "The pitchfork must return once MCA recovery food use has finished");
            helper.succeed();
        } catch (ReflectiveOperationException | RuntimeException exception) {
            helper.fail("Could not verify Hunter pitchfork recovery after MCA equipment refresh: " + exception);
        }
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

    private static void setPlayerFaction(Player player, ResourceLocation factionId) {
        IFaction<?> faction = VampirismAPI.factionRegistry().getFactionByID(factionId);
        if (!(faction instanceof IPlayableFaction<?> playableFaction)) {
            throw new IllegalStateException("Missing playable faction " + factionId);
        }
        if (!VampirismAPI.factionPlayerHandler(player).setFactionAndLevel(playableFaction, 1)) {
            throw new IllegalStateException("Could not set player faction " + factionId);
        }
    }

    private static ResourceLocation id(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }
}
