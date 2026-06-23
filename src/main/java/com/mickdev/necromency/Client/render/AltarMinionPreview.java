package com.mickdev.necromency.Client.render;

import com.mickdev.necromency.Client.Util.MinionRenderState;
import com.mickdev.necromency.entity.MinionEntity;
import com.mickdev.necromency.registry.Altar.Block.ALTARBlockEntity;
import com.mickdev.necromency.registry.NecromencyEntities;
import com.mickdev.necromency.registry.item.MobPart.BodyPartItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import java.util.HashMap;
import java.util.Map;

/** Aperçu client du minion sur l'autel (1.12 {@code TileEntityAltar#getPreviewEntity}) — classe client uniquement. */
public final class AltarMinionPreview {

    private static final Map<BlockPos, MinionEntity> CACHE = new HashMap<>();
    private static final ResourceLocation ZOMBIE = ResourceLocation.parse("minecraft:zombie");

    private AltarMinionPreview() {}

    public static boolean hasAnyPart(ALTARBlockEntity altar) {
        for (int i = 1; i <= 5; i++) {
            if (!altar.getItem(i).isEmpty()) return true;
        }
        return false;
    }

    public static MinionEntity getOrUpdate(ALTARBlockEntity altar) {
        Level level = altar.getLevel();
        if (level == null) return null;

        BlockPos pos = altar.getBlockPos();
        MinionEntity entity = CACHE.computeIfAbsent(pos, p -> {
            MinionEntity m = new MinionEntity(NecromencyEntities.MINION.get(), level);
            m.setNoAi(true);
            m.setSilent(true);
            m.setInvulnerable(true);
            return m;
        });

        syncParts(altar, entity);
        entity.setPos(pos.getX() + 0.5, pos.getY() + 0.05, pos.getZ() + 0.5);
        entity.tickCount = (int) level.getGameTime();
        return entity;
    }

    public static void remove(BlockPos pos) {
        MinionEntity removed = CACHE.remove(pos);
        if (removed != null) {
            removed.discard();
        }
    }

    private static void syncParts(ALTARBlockEntity altar, MinionEntity entity) {
        entity.setHeadId(mobIdFromSlot(altar.getItem(5)));
        entity.setBodyId(mobIdFromSlot(altar.getItem(2)));
        entity.setLegsId(mobIdFromSlot(altar.getItem(1)));
        entity.setArmRId(mobIdFromSlot(altar.getItem(3)));
        entity.setArmLId(mobIdFromSlot(altar.getItem(4)));
        entity.refreshDimensions();
    }

    private static ResourceLocation mobIdFromSlot(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof BodyPartItem)) {
            return ZOMBIE;
        }
        String id = BodyPartItem.resolveMobId(stack);
        if (id == null || id.isBlank()) return ZOMBIE;
        try {
            return ResourceLocation.parse(id);
        } catch (Exception e) {
            return ZOMBIE;
        }
    }

    /**
     * Le {@code poseStack} arrive déjà retourné (scale -1,-1,1) et orienté par la rotation de
     * l'autel (ALTARRenderer), origine au centre-haut du bloc. Le minion est couché sur le dos
     * sur le plateau (y monde ≈ 1.125), flottant légèrement au-dessus, aligné sur l'autel.
     */
    public static void render(
            ALTARBlockEntity altar,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState camera,
            int packedLight,
            float partialTick) {
        if (!hasAnyPart(altar)) return;

        MinionEntity preview = getOrUpdate(altar);
        if (preview == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (!(mc.getEntityRenderDispatcher().getRenderer(preview) instanceof MinionRenderer minionRenderer)) return;

        MinionRenderState state = minionRenderer.createRenderState();
        minionRenderer.extractRenderState(preview, state, partialTick);

        poseStack.pushPose();

        float bob = 0.04F * (float) Math.sin((preview.tickCount + partialTick) * 0.12F);
        // Y local pointe vers le bas : abaisser l'origine = poser le corps juste au-dessus du plateau
        poseStack.translate(0.0F, 0.175F - bob, 0.0F);
        // Couché sur le dos, visage vers le ciel
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        // Recentrer le corps sur la table (le pivot du modèle est aux pieds)
        poseStack.translate(0.0F, 1.25F, 0.0F);

        state.lightCoords = packedLight;
        minionRenderer.submitForAltarPreview(state, poseStack, collector, camera);

        poseStack.popPose();
    }
}
