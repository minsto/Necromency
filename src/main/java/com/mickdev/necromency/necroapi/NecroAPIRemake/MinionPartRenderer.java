package com.mickdev.necromency.necroapi.NecroAPIRemake;



import com.mickdev.necromency.entity.MinionEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.model.geom.ModelPart;
import com.mojang.blaze3d.vertex.PoseStack;

public interface MinionPartRenderer {
    void renderPart(PoseStack poseStack,
                    MultiBufferSource buffer,
                    int packedLight,
                    MinionEntity minion,
                    ProxyRender proxy);
}

