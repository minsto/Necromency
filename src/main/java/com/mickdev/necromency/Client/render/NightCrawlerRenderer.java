package com.mickdev.necromency.Client.render;

import com.mickdev.necromency.Client.Models.NightCrawlerModel;
import com.mickdev.necromency.Client.Models.TeddyModel;
import com.mickdev.necromency.Client.NecromencyModelLayers;
import com.mickdev.necromency.Necromency;
import com.mickdev.necromency.entity.EntityTeddy;
import com.mickdev.necromency.entity.NightCrawlerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;

import com.mickdev.necromency.Necromency;

import com.mickdev.necromency.entity.NightCrawlerEntity;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class NightCrawlerRenderer extends MobRenderer<NightCrawlerEntity, LivingEntityRenderState, NightCrawlerModel> {

    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath(Necromency.MODID, "textures/entities/crawler.png");

    public NightCrawlerRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new NightCrawlerModel(ctx.bakeLayer(NecromencyModelLayers.LAYER_Crawler)), 0.6F);
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }



    @Override
    public ResourceLocation getTextureLocation(LivingEntityRenderState state) {
        return TEX;
    }
}
