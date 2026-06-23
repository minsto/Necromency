package com.mickdev.necromency.Client.render;

import com.mickdev.necromency.registry.block.SkullWallBlock;
import com.mickdev.necromency.registry.block.SkullWallBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/** Affiche un crâne mural collé à la plaque d'obsidienne (port {@code TileEntitySkullWall}). */
public class SkullWallRenderer implements BlockEntityRenderer<SkullWallBlockEntity, SkullWallRenderer.SkullWallState> {

    /** Décalage vers l'avant (direction où regarde le crâne), en blocs. */
    private static final float SKULL_FORWARD_OFFSET = 0.07F;

    public static final class SkullWallState extends BlockEntityRenderState {
        /** Direction où le crâne regarde (WallSkullBlock.FACING vanilla). */
        public Direction facing = Direction.NORTH;
        public SkullBlock.Type skullType = SkullBlock.Types.SKELETON;
        public int packedLight;
        public @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay;
    }

    private final Map<SkullBlock.Type, SkullModelBase> skullModels = new HashMap<>();

    public SkullWallRenderer(BlockEntityRendererProvider.Context context) {
        EntityModelSet modelSet = context.entityModelSet();
        putModel(modelSet, SkullBlock.Types.SKELETON);
        putModel(modelSet, SkullBlock.Types.WITHER_SKELETON);
        putModel(modelSet, SkullBlock.Types.ZOMBIE);
        putModel(modelSet, SkullBlock.Types.CREEPER);
        putModel(modelSet, SkullBlock.Types.DRAGON);
        putModel(modelSet, SkullBlock.Types.PIGLIN);
        putModel(modelSet, SkullBlock.Types.PLAYER);
    }

    private void putModel(EntityModelSet modelSet, SkullBlock.Type type) {
        SkullModelBase model = SkullBlockRenderer.createModel(modelSet, type);
        if (model != null) {
            skullModels.put(type, model);
        }
    }

    @Override
    public SkullWallState createRenderState() {
        return new SkullWallState();
    }

    @Override
    public void extractRenderState(SkullWallBlockEntity be, SkullWallState state, float partialTick,
                                   Vec3 camera, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTick, camera, breakProgress);
        state.facing = be.getBlockState().getValue(SkullWallBlock.FACING);
        state.skullType = skullTypeFromId(be.getSkullType());
        state.crumblingOverlay = breakProgress;
        if (be.getLevel() != null) {
            state.packedLight = LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos());
        }
    }

    @Override
    public void submit(SkullWallState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        SkullModelBase model = skullModels.get(state.skullType);
        if (model == null) {
            model = skullModels.get(SkullBlock.Types.SKELETON);
        }
        if (model == null) {
            return;
        }

        RenderType renderType = SkullBlockRenderer.getSkullRenderType(state.skullType, null);
        Direction direction = state.facing;
        float yRot = RotationSegment.convertToDegrees(RotationSegment.convertToSegment(direction.getOpposite()));
        poseStack.pushPose();
        poseStack.translate(
                direction.getStepX() * SKULL_FORWARD_OFFSET,
                0.0F,
                direction.getStepZ() * SKULL_FORWARD_OFFSET);
        SkullBlockRenderer.submitSkull(
                direction,
                yRot,
                0.0F,
                poseStack,
                collector,
                state.packedLight,
                model,
                renderType,
                0,
                state.crumblingOverlay);
        poseStack.popPose();
    }

    private static SkullBlock.Type skullTypeFromId(String id) {
        return switch (id == null ? "" : id.toLowerCase()) {
            case "zombie" -> SkullBlock.Types.ZOMBIE;
            case "creeper" -> SkullBlock.Types.CREEPER;
            case "wither", "wither_skeleton" -> SkullBlock.Types.WITHER_SKELETON;
            case "piglin" -> SkullBlock.Types.PIGLIN;
            case "dragon" -> SkullBlock.Types.DRAGON;
            case "player", "char" -> SkullBlock.Types.PLAYER;
            default -> SkullBlock.Types.SKELETON;
        };
    }
}
