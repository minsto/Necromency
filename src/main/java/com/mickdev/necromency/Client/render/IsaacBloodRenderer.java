package com.mickdev.necromency.Client.render;

import com.mickdev.necromency.Client.Models.IsaacBloodModel;
import com.mickdev.necromency.Client.NecromencyClientRenderers;
import com.mickdev.necromency.Client.NecromencyModelLayers;
import com.mickdev.necromency.Necromency;
import com.mickdev.necromency.entity.EntityIsaacBlood;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;

public class IsaacBloodRenderer extends MobRenderer<EntityIsaacBlood, LivingEntityRenderState, IsaacBloodModel> {

    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath(Necromency.MODID, "textures/entities/isaac_blood.png");

    public IsaacBloodRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new IsaacBloodModel(ctx.bakeLayer(NecromencyModelLayers.ISAAC_BLOOD)), 0.5F);
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