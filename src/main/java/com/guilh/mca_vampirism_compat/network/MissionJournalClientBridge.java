package com.guilh.mca_vampirism_compat.network;
/** No client classes are referenced on a dedicated server. */
public final class MissionJournalClientBridge {
    private static java.util.function.Consumer<MissionJournalPayload> receiver = ignored -> {};
    public static void install(java.util.function.Consumer<MissionJournalPayload> handler) { receiver = handler; }
    public static void receive(MissionJournalPayload payload) { receiver.accept(payload); }
}
