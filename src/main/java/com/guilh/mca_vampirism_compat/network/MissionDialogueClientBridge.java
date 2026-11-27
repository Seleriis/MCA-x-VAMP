package com.guilh.mca_vampirism_compat.network;

import net.minecraft.network.chat.Component;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** Common-side relay. Client code installs its handlers after client setup. */
public final class MissionDialogueClientBridge {
    private static BiConsumer<UUID, Component> speechHandler = (villagerId, phrase) -> {
    };
    private static Consumer<UUID> closeHandler = villagerId -> {
    };

    private MissionDialogueClientBridge() {
    }

    public static void install(BiConsumer<UUID, Component> speech, Consumer<UUID> close) {
        speechHandler = speech == null ? (villagerId, phrase) -> {
        } : speech;
        closeHandler = close == null ? villagerId -> {
        } : close;
    }

    public static void speak(MissionDialogueSpeechPayload payload) {
        if (payload != null) {
            speechHandler.accept(payload.villagerId(), payload.phrase());
        }
    }

    public static void close(MissionDialogueClosePayload payload) {
        if (payload != null) {
            closeHandler.accept(payload.villagerId());
        }
    }
}
