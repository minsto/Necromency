package com.mickdev.necromency.registry.Event;

import com.mickdev.necromency.Necromency;
import com.mickdev.necromency.entity.*;
import com.mickdev.necromency.registry.NecromencyEntities;
import com.mickdev.necromency.registry.init.ModEntities;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = Necromency.MODID)
public final class NecromencyCommonEvents {

    @SubscribeEvent
    public static void onAttributes(net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent e) {
        e.put(NecromencyEntities.ISAAC_BODY.get(),   EntityIsaacBody.createAttributes().build());
        e.put(NecromencyEntities.ISAAC_NORMAL.get(), EntityIsaacNormal.createAttributes().build());
        e.put(NecromencyEntities.ISAAC_BLOOD.get(),  EntityIsaacBlood.createAttributes().build());
        e.put(NecromencyEntities.ISAAC_HEAD.get(),   EntityIsaacHead.createAttributes().build());
        e.put(NecromencyEntities.TEDDY.get(), EntityTeddy.createAttributes().build());
        e.put(NecromencyEntities.NIGHTCRAWLER.get(), NightCrawlerEntity.createAttributes().build());


    }
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                ModEntities.TEAR.get(),
                ThrownItemRenderer::new

        );
        event.registerEntityRenderer(
                ModEntities.BloodTEAR.get(),
                ThrownItemRenderer::new

                );
    }
}
