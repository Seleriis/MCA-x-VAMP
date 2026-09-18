package com.guilh.mca_vampirism_compat.client;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import forge.net.mca.client.model.VillagerEntityModelMCA;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.relationship.Gender;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(
        modid = McaVampirismCompat.MOD_ID,
        bus = EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public final class McaSpecialVillagerLayerRegistration {
    private McaSpecialVillagerLayerRegistration() {
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        addLayer(event, Gender.MALE.getVillagerType());
        addLayer(event, Gender.FEMALE.getVillagerType());
        addLayer(event, Gender.NEUTRAL.getVillagerType());
        addLayer(event, Gender.UNASSIGNED.getVillagerType());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void addLayer(EntityRenderersEvent.AddLayers event, EntityType<VillagerEntityMCA> entityType) {
        EntityRenderer<? extends VillagerEntityMCA> renderer = event.getRenderer(entityType);
        if (renderer instanceof LivingEntityRenderer livingRenderer) {
            RenderLayerParent<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>> parent =
                    (RenderLayerParent<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>>) livingRenderer;
            livingRenderer.addLayer(new LayerMcaVampireEyes(parent));
            livingRenderer.addLayer(new LayerMcaWerewolfParts(parent));
            McaVampirismCompat.debugLog(
                    "[WEREWOLF-APPEARANCE] layer registered renderer={} entityType={}",
                    livingRenderer.getClass().getName(),
                    entityType
            );
        }
    }
}
