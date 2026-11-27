package com.guilh.mca_vampirism_compat.mission;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OccultVillageMissionTypeTest {
    @Test
    void missionDefinitionsMatchThePlayerFacingRequests() {
        assertEquals(3, OccultVillageMissionType.VAMPIRE_BLOOD.requiredCount());
        assertEquals("vampirism:blood_bottle", OccultVillageMissionType.VAMPIRE_BLOOD.requiredItem().toString());
        assertEquals(1, OccultVillageMissionType.VAMPIRE_FANG.requiredCount());
        assertEquals("vampirism:vampire_fang", OccultVillageMissionType.VAMPIRE_FANG.requiredItem().toString());
        assertEquals(8, OccultVillageMissionType.HUNTER_GARLIC.requiredCount());
        assertEquals("vampirism:item_garlic", OccultVillageMissionType.HUNTER_GARLIC.requiredItem().toString());
        assertEquals(1, OccultVillageMissionType.HUNTER_GARLIC_INJECTION.requiredCount());
        assertEquals("vampirism:injection_garlic", OccultVillageMissionType.HUNTER_GARLIC_INJECTION.requiredItem().toString());
        assertEquals(8, OccultVillageMissionType.WEREWOLF_MUTTON.requiredCount());
        assertEquals("minecraft:mutton", OccultVillageMissionType.WEREWOLF_MUTTON.requiredItem().toString());
    }

    @Test
    void variableSupplyQuantitiesStayWithinTheirIntendedRanges() {
        RandomSource random = RandomSource.create(7L);
        for (int index = 0; index < 64; index++) {
            int blood = OccultVillageMissionType.VAMPIRE_BLOOD.randomRequiredCount(random);
            int garlic = OccultVillageMissionType.HUNTER_GARLIC.randomRequiredCount(random);
            int mutton = OccultVillageMissionType.WEREWOLF_MUTTON.randomRequiredCount(random);
            assertTrue(blood >= 2 && blood <= 4);
            assertTrue(garlic >= 4 && garlic <= 10);
            assertTrue(mutton >= 4 && mutton <= 10);
        }
        assertEquals(1, OccultVillageMissionType.VAMPIRE_FANG.randomRequiredCount(random));
        assertEquals(1, OccultVillageMissionType.HUNTER_GARLIC_INJECTION.randomRequiredCount(random));
    }

    @Test
    void urgentAidHasNoItemAndOptionalSupportIsLimitedToWerewolves() {
        assertTrue(OccultVillageMissionType.HUNTER_URGENT_AID.isUrgentAid());
        assertNull(OccultVillageMissionType.HUNTER_URGENT_AID.requiredItem());
        assertTrue(OccultVillageMissionType.WEREWOLF_MUTTON.requiresWerewolves());
        assertFalse(OccultVillageMissionType.VAMPIRE_BLOOD.requiresWerewolves());
    }

    @Test
    void bloodRequestsAcceptOnlyActuallyFullBloodBottles() {
        assertTrue(OccultVillageMissionService.hasRequiredBloodAmount(9));
        assertFalse(OccultVillageMissionService.hasRequiredBloodAmount(8));
        assertFalse(OccultVillageMissionService.hasRequiredBloodAmount(0));
    }

    @Test
    void onlyPreConversationPendingRecordsAreMigratedToTheNewDialogueFlow() {
        OccultVillageMissionSavedData.MissionRecord legacyPending = new OccultVillageMissionSavedData.MissionRecord(
                OccultVillageMissionType.VAMPIRE_BLOOD, UUID.randomUUID(), 10L, 20L, false, 3, 30L
        );
        OccultVillageMissionSavedData.MissionRecord conversationPending = new OccultVillageMissionSavedData.MissionRecord(
                OccultVillageMissionType.VAMPIRE_BLOOD, UUID.randomUUID(), 10L, 20L, false, 3, -1L
        );
        OccultVillageMissionSavedData.MissionRecord accepted = new OccultVillageMissionSavedData.MissionRecord(
                OccultVillageMissionType.VAMPIRE_BLOOD, UUID.randomUUID(), 10L, 20L, true, 3, 30L
        );

        assertTrue(legacyPending.isLegacyPending());
        assertFalse(conversationPending.isLegacyPending());
        assertFalse(accepted.isLegacyPending());
    }

    @Test
    void activeAndCooldownMissionRecordsRoundTripWithoutVillagerNbt() {
        UUID player = UUID.fromString("8128201f-1c80-4fcf-864a-c614ef51ab59");
        UUID target = UUID.fromString("fb105a86-72cc-47e7-bac3-99a31ddbf285");
        OccultVillageMissionSavedData.MissionKey key = new OccultVillageMissionSavedData.MissionKey(
                new ResourceLocation("minecraft", "overworld"), new BlockPos(18, 70, -12)
        );
        OccultVillageMissionSavedData data = new OccultVillageMissionSavedData();
        data.put(player, key, new OccultVillageMissionSavedData.MissionRecord(
                OccultVillageMissionType.HUNTER_GARLIC, target, 120L, 24_120L, false, 6, 48_120L
        ));

        CompoundTag encoded = data.save(new CompoundTag());
        OccultVillageMissionSavedData restored = OccultVillageMissionSavedData.load(encoded);
        OccultVillageMissionSavedData.MissionRecord active = restored.get(player, key).orElseThrow();
        assertTrue(active.isActive());
        assertTrue(active.isPending());
        assertEquals(OccultVillageMissionType.HUNTER_GARLIC, active.type());
        assertEquals(target, active.target());
        assertEquals(24_120L, active.nextAvailableTick());
        assertEquals(6, active.requiredCount());
        assertEquals(48_120L, active.expiresAtTick());
        assertFalse(active.isExpired(48_119L));
        assertTrue(active.isExpired(48_120L));

        restored.put(player, key, new OccultVillageMissionSavedData.MissionRecord(null, null, 120L, 24_120L, false));
        OccultVillageMissionSavedData cooledDown = OccultVillageMissionSavedData.load(
                restored.save(new CompoundTag())
        );
        assertFalse(cooledDown.get(player, key).orElseThrow().isActive());
        assertEquals(24_120L, cooledDown.get(player, key).orElseThrow().nextAvailableTick());
    }

    @Test
    void legacyActiveRecordsRemainAcceptedAfterTheInvitationUpgrade() {
        UUID player = UUID.fromString("8128201f-1c80-4fcf-864a-c614ef51ab59");
        UUID target = UUID.fromString("fb105a86-72cc-47e7-bac3-99a31ddbf285");
        CompoundTag root = new CompoundTag();
        CompoundTag entry = new CompoundTag();
        entry.putUUID("Player", player);
        entry.putString("Dimension", "minecraft:overworld");
        entry.putLong("Totem", new BlockPos(18, 70, -12).asLong());
        entry.putString("Type", OccultVillageMissionType.VAMPIRE_BLOOD.name());
        entry.putUUID("Target", target);
        net.minecraft.nbt.ListTag entries = new net.minecraft.nbt.ListTag();
        entries.add(entry);
        root.put("Missions", entries);

        OccultVillageMissionSavedData data = OccultVillageMissionSavedData.load(root);
        OccultVillageMissionSavedData.MissionKey key = new OccultVillageMissionSavedData.MissionKey(
                new ResourceLocation("minecraft", "overworld"), new BlockPos(18, 70, -12)
        );
        assertTrue(data.get(player, key).orElseThrow().isAccepted());
        assertEquals(3, data.get(player, key).orElseThrow().requiredCount(),
                "Pre-variable blood missions must retain their historical request of three bottles");
        assertFalse(data.get(player, key).orElseThrow().hasDeadline(),
                "Existing accepted missions must not expire immediately after upgrading");
    }
}
