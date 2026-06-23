package com.mickdev.necromency.Client.render;

import com.mickdev.necromency.Client.Models.IsaacNormalModel;
import com.mickdev.necromency.Client.NecromencyClientRenderers;
import com.mickdev.necromency.Client.NecromencyModelLayers;
import com.mickdev.necromency.Necromency;
import com.mickdev.necromency.entity.EntityIsaacNormal;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;

public class IsaacNormalRenderer extends MobRenderer<EntityIsaacNormal, LivingEntityRenderState, IsaacNormalModel> {

    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath(Necromency.MODID, "textures/entities/isaac_normal.png");

    public IsaacNormalRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new IsaacNormalModel(ctx.bakeLayer(NecromencyModelLayers.ISAAC_NORMAL)), 0.5F);
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
