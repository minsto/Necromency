package com.mickdev.necromency.registry.Altar.Renderer;

import com.mickdev.necromency.Client.NecromencyModelLayers;
import com.mickdev.necromency.Client.render.AltarMinionPreview;
import com.mickdev.necromency.registry.Altar.Block.ALTARBlock;
import com.mickdev.necromency.registry.Altar.Block.ALTARBlockEntity;
import com.mickdev.necromency.registry.Altar.Model.ModelAltar;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(modid = "necromency", value = Dist.CLIENT)
public class ALTARRenderer implements BlockEntityRenderer<ALTARBlockEntity, ALTARRenderer.AltarState> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.parse("necromency:textures/entities/altar.png");

    private final CustomHierarchicalModel model;

    public ALTARRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new CustomHierarchicalModel(context.bakeLayer(NecromencyModelLayers.LAYER_LOCATION));
    }

    // --- RenderState custom ---
    public static final class AltarState extends BlockEntityRenderState {
        public Direction facing = Direction.NORTH;

        /** lumière du monde (block+sky). */
        public int packedLight = LightTexture.FULL_BRIGHT; // fallback

        public final LivingEntityRenderState living = new LivingEntityRenderState();

        public @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay = null;

        public BlockPos blockPos = BlockPos.ZERO;
        public float partialTick;
    }

    @Override
    public AltarState createRenderState() {
        return new AltarState();
    }

    @Override
    public void extractRenderState(
            ALTARBlockEntity be,
            AltarState state,
            float partialTick,
            Vec3 cameraPosition,
            @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress
    ) {
        // remplit les champs "de base" du state (version 1.21.x)
        BlockEntityRenderer.super.extractRenderState(be, state, partialTick, cameraPosition, breakProgress);

        state.facing = be.getBlockState().getValue(ALTARBlock.FACING);
        state.crumblingOverlay = breakProgress;

        state.blockPos = be.getBlockPos();
        state.partialTick = partialTick;

        if (be.getLevel() != null) {
            state.packedLight = LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos());
            int ticks = (int) be.getLevel().getGameTime();
            state.living.ageInTicks = ticks + partialTick;
            AltarMinionPreview.getOrUpdate(be);
        }
    }

    @Override
    public void submit(AltarState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();

        // mêmes transforms que ton ancien render()
        poseStack.scale(-1F, -1F, 1F);
        poseStack.translate(-0.5D, -0.5D, 0.5D);

        switch (state.facing) {
            case EAST  -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
            case WEST  -> poseStack.mulPose(Axis.YP.rotationDegrees(-90));
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
            default -> {}
        }

        poseStack.translate(0D, -1D, 0D);

        // animation du model
        model.setupFromState(state.living);

        // ✅ RenderType :
        // - si ta texture a uniquement alpha 0/255 => cutout (recommandé)
        // - si ta texture a des alpha partiels (semi-transparence) => entityTranslucent
        RenderType rt = RenderType.entityCutoutNoCull(TEXTURE);
        // RenderType rt = RenderType.entityTranslucent(TEXTURE);

        // rendu via le collector (API 1.21.x)
        collector.submitModel(
                model,
                state.living,
                poseStack,
                rt,
                state.packedLight,
                OverlayTexture.NO_OVERLAY, // ✅ IMPORTANT : sinon tu peux avoir un tint rouge
                0,                          // outlineColor (0 = none)
                state.crumblingOverlay
        );

        var level = net.minecraft.client.Minecraft.getInstance().level;
        if (level != null) {
            var blockEntity = level.getBlockEntity(state.blockPos);
            if (blockEntity instanceof ALTARBlockEntity altar && AltarMinionPreview.hasAnyPart(altar)) {
                AltarMinionPreview.render(
                        altar,
                        poseStack,
                        collector,
                        camera,
                        state.packedLight,
                        state.partialTick
                );
            }
        }

        poseStack.popPose();
    }

    // --- Petit wrapper sur ton ModelAltar ---
    private static final class CustomHierarchicalModel extends ModelAltar {
        public CustomHierarchicalModel(ModelPart root) {
            super(root);
        }

        public void setupFromState(LivingEntityRenderState state) {
            this.root().getAllParts().forEach(ModelPart::resetPose);
            super.setupAnim(state);
        }
    }
}