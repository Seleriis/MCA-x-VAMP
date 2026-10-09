package com.guilh.mca_vampirism_compat.mission;

import forge.net.conczin.mca.entity.EntitiesMCA;
import forge.net.conczin.mca.entity.ai.relationship.AgeState;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import java.util.UUID;

/** Exercises the real SavedData and server lifecycle, including offline owners. */
@GameTestHolder("mca_vamp_compat")
@PrefixGameTestTemplate(false)
public final class OccultMissionLifecycleGameTests {
    private static OccultVillageMissionSavedData data(GameTestHelper helper) {
        return helper.getLevel().getServer().overworld().getDataStorage().computeIfAbsent(
                OccultVillageMissionSavedData::load,OccultVillageMissionSavedData::new,OccultVillageMissionSavedData.ID);
    }
    private static OccultVillageMissionSavedData.MissionRecord accepted(GameTestHelper helper, UUID target) {
        long now=helper.getLevel().getGameTime();
        return new OccultVillageMissionSavedData.MissionRecord(OccultVillageMissionType.HUNTER_AFTER_ATTACK,target,
                now,now+48000L,true,8,now+24000L,8,"Long-lived unloaded resident","Test village");
    }
    @GameTest(template="gametest/empty",timeoutTicks=260)
    public static void offlineUnloadedResidentSurvivesValidationAndReload(GameTestHelper helper) {
        var saved=data(helper);var owner=UUID.randomUUID();
        var villager=EntitiesMCA.MALE_VILLAGER.get().create(helper.getLevel());villager.setAgeState(AgeState.ADULT);
        villager.moveTo(helper.absolutePos(new BlockPos(2,2,2)),0,0);helper.getLevel().addFreshEntity(villager);
        var far=new BlockPos(29000000,64,29000000);
        var key=new OccultVillageMissionSavedData.MissionKey(helper.getLevel().dimension().location(),far);
        saved.put(owner,key,accepted(helper,villager.getUUID()));saved.track(owner,villager.getUUID());
        villager.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
        helper.assertTrue(!helper.getLevel().hasChunkAt(far),"The test totem chunk must start unloaded");
        helper.runAfterDelay(205,()->{
            helper.assertTrue(saved.get(owner,key).orElseThrow().isAccepted(),"An unloaded resident must not be treated as dead, even for an offline player");
            helper.assertTrue(!helper.getLevel().hasChunkAt(far),"Periodic mission validation must not load the totem chunk");
            var restored=OccultVillageMissionSavedData.load(saved.save(new CompoundTag()));
            helper.assertTrue(restored.get(owner,key).orElseThrow().equals(saved.get(owner,key).orElseThrow()),"Reward, quantities, location, name and deadline must survive SavedData reload");
            helper.assertTrue(villager.getUUID().equals(restored.tracked(owner)),"Tracked request belongs to the player and survives reload");
            helper.succeed();
        });
    }
    @GameTest(template="gametest/empty")
    public static void confirmedDeathTerminatesOfflineRequest(GameTestHelper helper) {
        var saved=data(helper);var owner=UUID.randomUUID();
        var villager=EntitiesMCA.MALE_VILLAGER.get().create(helper.getLevel());villager.setAgeState(AgeState.ADULT);
        villager.moveTo(helper.absolutePos(new BlockPos(3,2,3)),0,0);helper.getLevel().addFreshEntity(villager);
        var key=new OccultVillageMissionSavedData.MissionKey(helper.getLevel().dimension().location(),helper.absolutePos(new BlockPos(3,1,3)));
        saved.put(owner,key,accepted(helper,villager.getUUID()));saved.track(owner,villager.getUUID());
        villager.hurt(villager.damageSources().generic(),100000F);
        helper.assertTrue(!villager.isAlive(),"The test must cause confirmed native death");
        helper.assertTrue(!saved.get(owner,key).orElseThrow().isActive(),"The death event must terminate the offline owner's request");
        helper.assertTrue(saved.tracked(owner)==null && saved.missionsFor(villager.getUUID()).isEmpty(),"Death must clear tracking and the active target index");
        helper.succeed();
    }
    @GameTest(template="gametest/empty",timeoutTicks=260)
    public static void destroyedTotemTerminatesOfflineRequestAfterBoundedValidation(GameTestHelper helper) {
        var saved=data(helper);var owner=UUID.randomUUID();var relative=new BlockPos(4,2,4);
        helper.setBlock(relative,Blocks.AIR);
        var key=new OccultVillageMissionSavedData.MissionKey(helper.getLevel().dimension().location(),helper.absolutePos(relative));
        saved.put(owner,key,accepted(helper,UUID.randomUUID()));
        helper.runAfterDelay(205,()->{
            helper.assertTrue(!saved.get(owner,key).orElseThrow().isActive(),"A loaded position with no totem is confirmed destruction");
            helper.assertTrue(saved.prune(helper.getLevel().getGameTime(),64)==0,"Cancelled requests retain their cooldown");
            helper.succeed();
        });
    }
}
