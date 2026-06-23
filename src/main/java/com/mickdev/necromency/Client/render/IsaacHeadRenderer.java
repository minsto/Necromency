package com.mickdev.necromency.Client.render;

import com.mickdev.necromency.Client.Models.IsaacHeadModel;
import com.mickdev.necromency.Client.NecromencyClientRenderers;
import com.mickdev.necromency.Client.NecromencyModelLayers;
import com.mickdev.necromency.Necromency;
import com.mickdev.necromency.entity.EntityIsaacHead;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;

public class IsaacHeadRenderer extends MobRenderer<EntityIsaacHead, LivingEntityRenderState, IsaacHeadModel> {

    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath(Necromency.MODID, "textures/entities/isaac_blood.png");

    public IsaacHeadRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new IsaacHeadModel(ctx.bakeLayer(NecromencyModelLayers.ISAAC_HEAD)), 0.4F);
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