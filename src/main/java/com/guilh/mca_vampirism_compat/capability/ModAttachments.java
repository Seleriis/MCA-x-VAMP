package com.guilh.mca_vampirism_compat.capability;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.VampiricVillagerState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES,
                    McaVampirismCompat.LEGACY_DATA_NAMESPACE);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<VampiricVillagerState>> VAMPIRIC_VILLAGER =
            ATTACHMENTS.register("vampiric_villager",
                    () -> AttachmentType.serializable(VampiricVillagerState::new).build());

    private ModAttachments() {
    }

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
    }
}
