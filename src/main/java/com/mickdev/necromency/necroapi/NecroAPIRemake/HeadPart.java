package com.mickdev.necromency.necroapi.NecroAPIRemake;

import com.mickdev.necromency.entity.MinionEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import com.mickdev.necromency.entity.MinionEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class HeadPart implements MinionPartRenderer {

    @Override
    public void renderPart(PoseStack ps, MultiBufferSource buf, int light, MinionEntity minion, ProxyRender proxy) {

        // ✅ entité proxy (dans le même level que le minion)
        LivingEntity proxyEntity = proxy.proxyEntity(minion.level());
        if (proxyEntity == null) return;

        // ✅ modèle du renderer
        EntityModel<?> rawModel = proxy.model();
        if (!(rawModel instanceof HumanoidModel<?> hm)) return;

        ModelPart head = HumanoidParts.head(hm);
        if (head == null) return;

        // ✅ texture du proxy (via renderState)
        ResourceLocation tex = getProxyTexture(proxy.renderer(), proxyEntity);
        if (tex == null) return;

        VertexConsumer vc = buf.getBuffer(RenderType.entityCutoutNoCull(tex));

        ps.pushPose();
        head.render(ps, vc, light, OverlayTexture.NO_OVERLAY);
        ps.popPose();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static ResourceLocation getProxyTexture(LivingEntityRenderer renderer, LivingEntity entity) {
        try {
            LivingEntityRenderState st = (LivingEntityRenderState) renderer.createRenderState();
            renderer.extractRenderState(entity, st, 0.0f);
            return renderer.getTextureLocation(st);
        } catch (Throwable t) {
            return null;
        }
    }
}