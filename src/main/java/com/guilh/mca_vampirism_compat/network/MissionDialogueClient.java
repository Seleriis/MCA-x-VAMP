package com.guilh.mca_vampirism_compat.network;

import com.guilh.mca_vampirism_compat.mixin.client.InteractScreenAccessor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.conczin.mca.client.gui.InteractScreen;
import net.conczin.mca.entity.VillagerLike;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

/** Physical-client handler kept out of the dedicated-server class path. */
@OnlyIn(Dist.CLIENT)
public final class MissionDialogueClient {
    private MissionDialogueClient() {
    }

    public static void speak(UUID villagerId, Component phrase) {
        Minecraft minecraft = Minecraft.getInstance();
        if (villagerId == null || phrase == null || minecraft.player == null
                || !(minecraft.screen instanceof InteractScreen screen)) {
            return;
        }
        VillagerLike<?> villager = ((InteractScreenAccessor) (Object) screen).mcaVampCompat$getVillager();
        if (villager instanceof Entity entity && villagerId.equals(entity.getUUID())) {
            villager.sendChatMessage(phrase, minecraft.player);
        }
    }

    public static void closeIfStillTalkingTo(UUID villagerId) {
        if (villagerId == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.screen instanceof InteractScreen screen)
                || !(((InteractScreenAccessor) (Object) screen).mcaVampCompat$getVillager() instanceof Entity villager)
                || !villagerId.equals(villager.getUUID())) {
            return;
        }
        minecraft.setScreen(null);
    }
}
