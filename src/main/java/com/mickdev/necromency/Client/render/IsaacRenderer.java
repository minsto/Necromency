package com.mickdev.necromency.Client.render;

import com.mickdev.necromency.Client.Models.IsaacModel;
import com.mickdev.necromency.Client.NecromencyModelLayers;
import com.mickdev.necromency.Necromency;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

public class IsaacRenderer<T extends Mob> extends MobRenderer<T, LivingEntityRenderState, IsaacModel> {

    private static final ResourceLocation ISAAC_TEX =
            ResourceLocation.fromNamespaceAndPath(Necromency.MODID, "textures/entities/isaac_normal.png");

    public IsaacRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new IsaacModel(ctx.bakeLayer(NecromencyModelLayers.ISAAC_NORMAL)), 0.5F);
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public ResourceLocation getTextureLocation(LivingEntityRenderState state) {
        return ISAAC_TEX;
    }
}
