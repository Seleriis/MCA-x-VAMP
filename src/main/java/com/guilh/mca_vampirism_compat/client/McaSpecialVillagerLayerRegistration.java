package com.guilh.mca_vampirism_compat.client;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import net.conczin.mca.client.model.VillagerEntityModelMCA;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.ai.relationship.Gender;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(
        modid = McaVampirismCompat.MOD_ID,
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
            livingRenderer.addLayer(new LayerMcaWerewolfParts(parent));
            McaVampirismCompat.debugLog(
                    "[WEREWOLF-APPEARANCE] layer registered renderer={} entityType={}",
                    livingRenderer.getClass().getName(),
                    entityType
            );
        }
    }
}
