package com.mickdev.necromency.Client;

import com.mickdev.necromency.Client.Models.*;
import com.mickdev.necromency.Client.render.*;
import com.mickdev.necromency.Necromency;
import com.mickdev.necromency.necroapi.NecroAPIRemake.MinionPartRenderer;
import com.mickdev.necromency.registry.Altar.Model.ModelAltar;
import com.mickdev.necromency.registry.Altar.Renderer.ALTARRenderer;
import com.mickdev.necromency.registry.NecromencyEntities;
import com.mickdev.necromency.registry.init.NecromencyModBlockEntities;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = Necromency.MODID, value = Dist.CLIENT)
public final class NecromencyClientRenderers {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(NecromencyEntities.TEDDY.get(), TeddyRenderer::new);
        event.registerEntityRenderer(NecromencyEntities.NIGHTCRAWLER.get(), NightCrawlerRenderer::new);
        event.registerEntityRenderer(NecromencyEntities.ISAAC_NORMAL.get(), IsaacRenderer::new);
        event.registerEntityRenderer(NecromencyEntities.ISAAC_BODY.get(), IsaacBodyRenderer::new);
        event.registerEntityRenderer(NecromencyEntities.ISAAC_HEAD.get(), IsaacHeadRenderer::new);
        event.registerEntityRenderer(NecromencyEntities.ISAAC_BLOOD.get(), IsaacBloodRenderer::new);
        event.registerBlockEntityRenderer(NecromencyModBlockEntities.ALTAR.get(), ALTARRenderer::new);
        event.registerBlockEntityRenderer(NecromencyModBlockEntities.SKULL_WALL.get(), SkullWallRenderer::new);
        event.registerEntityRenderer(NecromencyEntities.MINION.get(), MinionRenderer::new);


    }

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(NecromencyModelLayers.TEDDY, TeddyModel::createBodyLayer);
        event.registerLayerDefinition(NecromencyModelLayers.LAYER_Crawler, NightCrawlerModel::createBodyLayer);
        event.registerLayerDefinition(NecromencyModelLayers.LAYER_LOCATION, ModelAltar::createBodyLayer);
        // Pour démarrer : 1 seul modèle “IsaacModel” réutilisé (normal/body/head/blood).
        // Après, tu pourras split en 4 modèles différents si tu veux.
        event.registerLayerDefinition(NecromencyModelLayers.ISAAC_NORMAL, IsaacModel::createBodyLayer);
        event.registerLayerDefinition(NecromencyModelLayers.ISAAC_BODY, IsaacBodyModel::createBodyLayer);
        event.registerLayerDefinition(NecromencyModelLayers.ISAAC_HEAD, IsaacHeadModel::createBodyLayer);
        event.registerLayerDefinition(NecromencyModelLayers.ISAAC_BLOOD, IsaacBloodModel::createBodyLayer);
        event.registerLayerDefinition(NecromencyModelLayers.MINION,NecroEntityBaseModel::createBodyLayer);
        event.registerLayerDefinition(NecromencyModelLayers.MINION_ATLAS, MinionAtlasHumanoidMesh::createBodyLayer);
        event.registerLayerDefinition(NecromencyModelLayers.MINION_CHICKEN, NecromencyChicken::createBodyLayer);
        event.registerLayerDefinition(NecromencyModelLayers.MINION_VILLAGER_HEAD, MinionVillagerHeadMesh::createBodyLayer);

    }

    public class ClientEvents {
        @SubscribeEvent
        public static void registerBER(EntityRenderersEvent.RegisterRenderers event) {

        }
    }
    private NecromencyClientRenderers() {}
}