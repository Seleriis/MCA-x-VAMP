package com.guilh.mca_vampirism_compat.mission;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
class MissionPersistenceTest {
    private static OccultVillageMissionSavedData.MissionKey key(int x) {
        return new OccultVillageMissionSavedData.MissionKey(ResourceLocation.parse("minecraft:overworld"),new BlockPos(x,64,0));
    }
    @Test void agreedRewardAndPresentationSurviveReload() {
        var data=new OccultVillageMissionSavedData(); var player=UUID.randomUUID(); var target=UUID.randomUUID();
        var record=new OccultVillageMissionSavedData.MissionRecord(OccultVillageMissionType.VAMPIRE_AFTER_ATTACK,target,10,24010,true,4,48010,8,"Resident with a long name","Village");
        data.put(player,key(0),record); data.track(player,target); data.recordAttack(key(0),15);
        var tag=data.save(new CompoundTag(),net.minecraft.core.RegistryAccess.EMPTY);
        assertEquals(2,tag.getInt("Version"));
        var loaded=OccultVillageMissionSavedData.load(tag,net.minecraft.core.RegistryAccess.EMPTY);
        assertEquals(record,loaded.get(player,key(0)).orElseThrow()); assertEquals(target,loaded.tracked(player));
        assertTrue(loaded.attackedRecently(key(0),24014)); assertFalse(loaded.attackedRecently(key(0),24015));
    }
    @Test void missingRewardMigratesToFiveWithoutChangingDeadlineOrQuantity() {
        var data=new OccultVillageMissionSavedData(); var player=UUID.randomUUID();
        data.put(player,key(0),new OccultVillageMissionSavedData.MissionRecord(OccultVillageMissionType.HUNTER_GARLIC,UUID.randomUUID(),10,50,true,7,12345));
        var tag=data.save(new CompoundTag(),net.minecraft.core.RegistryAccess.EMPTY);
        tag.getList("Missions",10).getCompound(0).remove("Reward"); tag.remove("Version");
        var loaded=OccultVillageMissionSavedData.load(tag,net.minecraft.core.RegistryAccess.EMPTY).get(player,key(0)).orElseThrow();
        assertEquals(5,loaded.reward()); assertEquals(7,loaded.requiredCount()); assertEquals(12345,loaded.expiresAtTick());
    }
    @Test void pruningIsBoundedAndNeverDeletesActiveRequests() {
        var data=new OccultVillageMissionSavedData(); var player=UUID.randomUUID(); var target=UUID.randomUUID();
        data.put(player,key(0),new OccultVillageMissionSavedData.MissionRecord(OccultVillageMissionType.VAMPIRE_BLOOD,target,0,10,true));
        data.put(player,key(1),new OccultVillageMissionSavedData.MissionRecord(OccultVillageMissionType.VAMPIRE_FANG,UUID.randomUUID(),0,10,false));
        for(int i=2;i<10;i++) data.put(player,key(i),new OccultVillageMissionSavedData.MissionRecord(null,null,0,100,false));
        assertEquals(0,data.prune(99,64)); assertEquals(3,data.prune(100,3));
        assertEquals(2,data.activeMissions(player).size()); assertEquals(1,data.missionsFor(target).size());
        assertEquals(5,data.prune(100,64)); assertTrue(data.get(player,key(0)).isPresent()); assertTrue(data.get(player,key(1)).isPresent());
    }
    @Test void validationQueueDoesNotMultiplyWhenRequestChanges() {
        var data=new OccultVillageMissionSavedData(); var player=UUID.randomUUID();
        for(int i=0;i<10;i++) {
            data.put(player,key(0),new OccultVillageMissionSavedData.MissionRecord(OccultVillageMissionType.VAMPIRE_FANG,UUID.randomUUID(),0,100,true));
            data.put(player,key(0),new OccultVillageMissionSavedData.MissionRecord(null,null,0,100,false));
        }
        data.put(player,key(0),new OccultVillageMissionSavedData.MissionRecord(OccultVillageMissionType.VAMPIRE_FANG,UUID.randomUUID(),0,100,true));
        assertEquals(1,data.validationBatch(64).size()); assertEquals(0,data.prune(200,64));
    }
    @Test void contextualDefinitionsAndPriorityRemainStable() {
        assertEquals(4,OccultVillageMissionType.VAMPIRE_AFTER_ATTACK.requiredCount());
        assertEquals(8,OccultVillageMissionType.HUNTER_AFTER_ATTACK.defaultReward());
        assertEquals(2,OccultVillageMissionType.VAMPIRE_SPECIALIST.requiredCount());
        assertEquals(3,OccultVillageMissionType.VAMPIRE_BLOOD.defaultReward());
        assertEquals(5,OccultVillageMissionType.HUNTER_SPECIALIST.defaultReward());
        assertTrue(OccultVillageMissionType.HUNTER_URGENT_AID.priority()>OccultVillageMissionType.HUNTER_AFTER_ATTACK.priority());
        assertTrue(OccultVillageMissionType.VAMPIRE_AFTER_ATTACK.requiresFullBlood());
    }
}
