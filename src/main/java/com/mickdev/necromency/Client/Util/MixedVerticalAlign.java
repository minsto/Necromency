package com.mickdev.necromency.Client.Util;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.ResourceLocation;

/**
 * Calage vertical des morceaux humanoïde quand les jambes poulet sont en PartProfiles
 * sur le mesh vanilla (pas le composite atlas poulet complet).
 */
public final class MixedVerticalAlign {

    private static final ResourceLocation RL_CHICKEN = ResourceLocation.parse("minecraft:chicken");

    /** Pivot tête du mesh Blockbench {@link com.mickdev.necromency.Client.Models.NecromencyChicken}. */
    public static final float CHICKEN_MODEL_HEAD_Y = 10.0F;

    /** Relèvement quand jambes poulet PartProfiles sur squelette humanoïde zombie. */
    private static final float HUMANOID_CHICKEN_LEG_LIFT = 6.0F;

    private MixedVerticalAlign() {}

    /** Cou humanoïde (y=0) — atlas poulet utilise aussi y=0 depuis la refonte mesh. */
    public static float neckY(MinionRenderState state) {
        return 0.0F;
    }

    public static void apply(HumanoidModel<?> model, MinionRenderState state, PartSlot slot) {
        if (slot == PartSlot.LEGS || usesChickenAtlas(state) || !isChicken(state.legsType)) {
            return;
        }
        float lift = HUMANOID_CHICKEN_LEG_LIFT;
        switch (slot) {
            case HEAD -> model.head.y += lift;
            case BODY -> model.body.y += lift;
            case ARM_L -> model.leftArm.y += lift;
            case ARM_R -> model.rightArm.y += lift;
            default -> {}
        }
    }

    public static boolean needsHumanoidLift(MinionRenderState state) {
        return !usesChickenAtlas(state) && isChicken(state.legsType);
    }

    /** Corps + ailes + jambes poulet (atlas), tête d'un autre mob. */
    public static boolean usesChickenAtlas(MinionRenderState state) {
        if (RL_CHICKEN.equals(state.headType)
                && RL_CHICKEN.equals(state.bodyType)
                && RL_CHICKEN.equals(state.armLType)
                && RL_CHICKEN.equals(state.armRType)
                && RL_CHICKEN.equals(state.legsType)) {
            return false;
        }
        return RL_CHICKEN.equals(state.bodyType)
                && RL_CHICKEN.equals(state.armLType)
                && RL_CHICKEN.equals(state.armRType)
                && RL_CHICKEN.equals(state.legsType);
    }

    public static boolean useAtlasMeshFor(MinionRenderState state, PartSlot slot, ResourceLocation mobId) {
        if (!isChicken(mobId) || slot == PartSlot.HEAD) {
            return false;
        }
        if (usesChickenAtlas(state)) {
            return true;
        }
        return slot == PartSlot.BODY || slot == PartSlot.ARM_L || slot == PartSlot.ARM_R || slot == PartSlot.LEGS;
    }

    private static boolean isChicken(ResourceLocation id) {
        return RL_CHICKEN.equals(id);
    }
}
