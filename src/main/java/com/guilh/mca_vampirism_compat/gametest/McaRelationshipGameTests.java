package com.guilh.mca_vampirism_compat.gametest;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.service.McaRelationshipCombatPolicy;
import com.guilh.mca_vampirism_compat.service.McaSpecialVillagerCombatService;
import com.guilh.mca_vampirism_compat.service.McaEditorTraitSyncService;
import com.guilh.mca_vampirism_compat.trait.McaTraitBridge;
import com.guilh.mca_vampirism_compat.util.McaVampireTargetUtil;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import net.conczin.mca.registry.EntitiesMCA;
import net.conczin.mca.entity.ai.relationship.AgeState;
import net.conczin.mca.server.world.data.FamilyTree;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
@GameTestHolder("mca_vamp_compat")
@PrefixGameTestTemplate(false)
public final class McaRelationshipGameTests {
    private static net.neoforged.neoforge.common.ModConfigSpec.BooleanValue option() throws ReflectiveOperationException {
        var field=McaVampirismCompatConfig.class.getDeclaredField("RESPECT_MCA_RELATIONSHIPS_IN_COMBAT"); field.setAccessible(true);
        return (net.neoforged.neoforge.common.ModConfigSpec.BooleanValue)field.get(null);
    }
    @GameTest(template="gametest/empty")
    public static void parentProtectionSwitchAndConfirmedRetaliation(GameTestHelper helper) throws ReflectiveOperationException {
        var config=option(); boolean before=config.get();
        try {
            var vampire=EntitiesMCA.MALE_VILLAGER.create(helper.getLevel());
            var child=EntitiesMCA.FEMALE_VILLAGER.create(helper.getLevel());
            vampire.setAgeState(AgeState.ADULT); child.setAgeState(AgeState.ADULT);
            McaTraitBridge.addVampireTrait(vampire); McaEditorTraitSyncService.reconcileEditedTraits(vampire);
            McaTraitBridge.addHunterTrait(child); McaEditorTraitSyncService.reconcileEditedTraits(child);
            var tree=FamilyTree.get(helper.getLevel()); tree.getOrCreate(child).setFather(tree.getOrCreate(vampire));
            config.set(false);
            helper.assertTrue(!McaRelationshipCombatPolicy.blocksAutomaticAttack(vampire,child),"Disabled option preserves faction hostility");
            config.set(true);
            helper.assertTrue(McaRelationshipCombatPolicy.blocksAutomaticAttack(vampire,child),"A parent must protect its child");
            helper.assertTrue(McaRelationshipCombatPolicy.blocksAutomaticAttack(child,vampire),"A child must protect its parent");
            helper.assertTrue(!McaVampireTargetUtil.isValidTarget(vampire,child),"Faction target selection must respect family");
            vampire.setTarget(child); helper.assertTrue(vampire.getTarget()==null,"Native target changes must also be blocked");
            McaRelationshipCombatPolicy.confirmedAggression(vampire,child);
            McaSpecialVillagerCombatService.markRetaliation(vampire,child,"GAME_TEST_CONFIRMED_DAMAGE");
            helper.assertTrue(!McaRelationshipCombatPolicy.blocksAutomaticAttack(vampire,child),"Confirmed aggression permits defense");
            helper.assertTrue(McaVampireTargetUtil.isValidTarget(vampire,child),"Defense must be a valid faction target");
            child.setAgeState(AgeState.CHILD);
            helper.assertTrue(!McaRelationshipCombatPolicy.protectedRetaliation(vampire,child),"Retaliation must retain age protections");
            helper.succeed();
        } finally { config.set(before); }
    }
    @GameTest(template="gametest/empty")
    public static void friendshipUsesMcaThresholdWithoutCreatingMemories(GameTestHelper helper) throws ReflectiveOperationException {
        var config=option(); boolean before=config.get();
        try {
            config.set(true);
            var villager=EntitiesMCA.MALE_VILLAGER.create(helper.getLevel()); var player=helper.makeMockPlayer(GameType.SURVIVAL);
            int count=villager.getVillagerBrain().getMemories().size();
            helper.assertTrue(!McaRelationshipCombatPolicy.protectedRelationship(villager,player),"Unknown player must not be a friend");
            helper.assertTrue(count==villager.getVillagerBrain().getMemories().size(),"Relationship query must not create memories");
            int threshold=net.conczin.mca.Config.getInstance().heartsToBeConsideredAsFriend;
            var memory=villager.getVillagerBrain().getMemoriesForPlayer(player); memory.setHearts(threshold);
            helper.assertTrue(McaRelationshipCombatPolicy.protectedRelationship(villager,player),"MCA friendship threshold must be used");
            memory.setHearts(threshold-1);
            helper.assertTrue(!McaRelationshipCombatPolicy.protectedRelationship(villager,player),"Friendship changes must be visible immediately");
            helper.succeed();
        } finally { config.set(before); }
    }
}
