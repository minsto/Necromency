package com.mickdev.necromency.Client.render;

import com.mickdev.necromency.Client.Models.TeddyModel;
import com.mickdev.necromency.Client.NecromencyClientRenderers;
import com.mickdev.necromency.Client.NecromencyModelLayers;
import com.mickdev.necromency.Necromency;
import com.mickdev.necromency.entity.EntityTeddy;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;

public class TeddyRenderer extends MobRenderer<EntityTeddy, LivingEntityRenderState, TeddyModel> {

    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath(Necromency.MODID, "textures/entities/new_teddy.png");

    public TeddyRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new TeddyModel(ctx.bakeLayer(NecromencyModelLayers.TEDDY)), 0.6F);
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public void extractRenderState(EntityTeddy entity, LivingEntityRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        // Ici tu peux mettre des flags custom plus tard (ex: état “angry”, etc.)
    }

    @Override
    public ResourceLocation getTextureLocation(LivingEntityRenderState state) {
        return TEX;
    }
}