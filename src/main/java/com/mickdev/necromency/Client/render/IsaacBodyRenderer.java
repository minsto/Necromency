package com.mickdev.necromency.Client.render;

import com.mickdev.necromency.Client.Models.IsaacBodyModel;
import com.mickdev.necromency.Client.NecromencyModelLayers;
import com.mickdev.necromency.Necromency;
import com.mickdev.necromency.entity.EntityIsaacBody;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;

public class IsaacBodyRenderer extends MobRenderer<EntityIsaacBody, LivingEntityRenderState, IsaacBodyModel> {

    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath(Necromency.MODID, "textures/entities/isaac_blood.png");

    public IsaacBodyRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new IsaacBodyModel(ctx.bakeLayer(NecromencyModelLayers.ISAAC_BODY)), 0.6F);
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