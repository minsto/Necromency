package com.mickdev.necromency.Client.legacy;

import com.mickdev.necromency.Client.Util.MinionRenderState;
import com.mickdev.necromency.Client.Util.PartSlot;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Modèle d'un slot de minion (géométrie 1.7.10 d'un mob donné) + animations
 * transcrites des {@code setRotationAngles} d'origine (NecroEntityBiped,
 * Quadruped, Chicken, Spider/Squid, Villager, Enderman, Creeper).
 */
public class LegacyMinionModel extends EntityModel<MinionRenderState> {

    private static final Map<String, LegacyMinionModel> CACHE = new HashMap<>();
    private static final String MODEL_CACHE_REV = "spider-legs-original-v4";

    private final PartSlot slot;
    private final LegacyNecroParts.MobDef def;
    private final List<ModelPart> parts = new ArrayList<>();
    private final List<PartPose> initialPoses = new ArrayList<>();

    private LegacyMinionModel(ModelPart root, PartSlot slot, LegacyNecroParts.MobDef def) {
        super(root);
        this.slot = slot;
        this.def = def;
        ModelPart group = root.getChild("slot");
        for (int i = 0; group.hasChild("p" + i); i++) {
            ModelPart p = group.getChild("p" + i);
            parts.add(p);
            initialPoses.add(p.storePose());
        }
    }

    /** Modèle (avec cache) pour un slot + mob legacy. */
    public static LegacyMinionModel get(PartSlot slot, ResourceLocation mobId) {
        String key = MODEL_CACHE_REV + "|" + slot.name() + "|" + mobId;
        return CACHE.computeIfAbsent(key, k ->
                new LegacyMinionModel(LegacyNecroParts.bake(slot, mobId), slot, LegacyNecroParts.get(mobId)));
    }

    @Override
    public void setupAnim(MinionRenderState s) {
        super.setupAnim(s);
        for (int i = 0; i < parts.size(); i++) {
            parts.get(i).loadPose(initialPoses.get(i));
        }

        float limbSwing = s.walkAnimationPos;
        float limbAmount = s.walkAnimationSpeed;
        float headYawDeg = s.yRot;
        float headPitchDeg = s.xRot;

        switch (def.anim()) {
            case BIPED -> animBiped(limbSwing, limbAmount, headYawDeg, headPitchDeg);
            case ENDERMAN -> animEnderman(limbSwing, limbAmount, headYawDeg, headPitchDeg);
            case VILLAGER -> animVillager(limbSwing, limbAmount, headYawDeg, headPitchDeg);
            case QUADRUPED -> animQuadruped(limbSwing, limbAmount, headPitchDeg);
            case CHICKEN -> animChicken(limbSwing, limbAmount, headYawDeg, headPitchDeg);
            case SPIDER -> animSpider(limbSwing, limbAmount, headYawDeg, headPitchDeg);
            case CREEPER -> animCreeper(limbSwing, limbAmount, headYawDeg, headPitchDeg);
        }

        // ModelMinion 1.7.10 : balancement des deux bras pendant l'attaque
        if ((slot == PartSlot.ARM_L || slot == PartSlot.ARM_R) && s.attackTime > 0.0F) {
            float i = s.attackTime * 10.0F;
            float swing = -2.0F + 1.5F * calc(i, 10.0F);
            for (ModelPart p : parts) {
                p.xRot = swing;
            }
        }
    }

    private static float calc(float a, float b) {
        return (Math.abs(a % b - b * 0.5F) - b * 0.25F) / (b * 0.25F);
    }

    private static final float DEG = (float) Math.PI / 180F;

    private void lookAll(float yawDeg, float pitchDeg) {
        for (ModelPart p : parts) {
            p.yRot = yawDeg * DEG;
            p.xRot = pitchDeg * DEG;
        }
    }

    private void animBiped(float swing, float amount, float yaw, float pitch) {
        switch (slot) {
            case HEAD -> lookAll(yaw, pitch);
            case ARM_L -> {
                parts.get(0).xRot = Mth.cos(swing * 0.6662F) * 2.0F * amount * 0.5F;
                parts.get(0).zRot = 0.0F;
            }
            case ARM_R -> {
                parts.get(0).xRot = Mth.cos(swing * 0.6662F + (float) Math.PI) * 2.0F * amount * 0.5F;
                parts.get(0).zRot = 0.0F;
            }
            case LEGS -> {
                if (parts.size() >= 2) {
                    parts.get(0).xRot = Mth.cos(swing * 0.6662F) * 1.4F * amount;
                    parts.get(1).xRot = Mth.cos(swing * 0.6662F + (float) Math.PI) * 1.4F * amount;
                    parts.get(0).yRot = 0.0F;
                    parts.get(1).yRot = 0.0F;
                }
            }
            default -> {}
        }
    }

    private void animEnderman(float swing, float amount, float yaw, float pitch) {
        animBiped(swing, amount, yaw, pitch);
        switch (slot) {
            case LEGS -> {
                for (ModelPart p : parts) {
                    p.xRot = Mth.clamp(p.xRot * 0.5F, -0.4F, 0.4F);
                }
            }
            case ARM_L, ARM_R -> parts.get(0).xRot = Mth.clamp(parts.get(0).xRot * 0.5F, -0.4F, 0.4F);
            default -> {}
        }
    }

    private void animVillager(float swing, float amount, float yaw, float pitch) {
        switch (slot) {
            case HEAD -> lookAll(yaw, pitch);
            case ARM_L, ARM_R -> parts.get(0).xRot = -0.75F;
            case LEGS -> {
                if (parts.size() >= 2) {
                    parts.get(0).xRot = Mth.cos(swing * 0.6662F + (float) Math.PI) * 1.4F * amount * 0.5F;
                    parts.get(1).xRot = Mth.cos(swing * 0.6662F) * 1.4F * amount * 0.5F;
                }
            }
            default -> {}
        }
    }

    private void animQuadruped(float swing, float amount, float pitch) {
        switch (slot) {
            // Quirk d'origine : la tête quadrupède suit le pitch sur X ET Y
            case HEAD -> {
                for (ModelPart p : parts) {
                    p.xRot = pitch * DEG;
                    p.yRot = pitch * DEG;
                }
            }
            case BODY -> parts.get(0).xRot = (float) Math.PI / 2F;
            case ARM_L -> {
                parts.get(0).xRot = Mth.cos(swing * 0.6662F + (float) Math.PI) * 1.4F * amount;
                parts.get(0).zRot = 0.0F;
            }
            case ARM_R -> {
                parts.get(0).xRot = Mth.cos(swing * 0.6662F) * 1.4F * amount;
                parts.get(0).zRot = 0.0F;
            }
            case LEGS -> {
                if (parts.size() >= 2) {
                    parts.get(0).xRot = Mth.cos(swing * 0.6662F) * 1.4F * amount;
                    parts.get(1).xRot = Mth.cos(swing * 0.6662F + (float) Math.PI) * 1.4F * amount;
                    parts.get(0).yRot = 0.0F;
                    parts.get(1).yRot = 0.0F;
                }
            }
        }
    }

    private void animChicken(float swing, float amount, float yaw, float pitch) {
        switch (slot) {
            case HEAD -> lookAll(yaw, pitch);
            case BODY -> parts.get(0).xRot = (float) Math.PI / 2F;
            case LEGS -> {
                if (parts.size() >= 2) {
                    parts.get(0).xRot = Mth.cos(swing * 0.6662F) * 1.4F * amount;
                    parts.get(1).xRot = Mth.cos(swing * 0.6662F + (float) Math.PI) * 1.4F * amount;
                    parts.get(0).yRot = 0.0F;
                    parts.get(1).yRot = 0.0F;
                }
            }
            default -> {}
        }
    }

    /** Pattes d'araignée / tentacules de poulpe (NecroEntitySpider & Squid, identiques). */
    private void animSpider(float swing, float amount, float yaw, float pitch) {
        if (slot == PartSlot.HEAD) {
            lookAll(yaw, pitch);
            return;
        }
        if (slot != PartSlot.LEGS || parts.size() < 8) {
            return;
        }
        float quarterPi = (float) Math.PI / 4F;
        float base = 0.3926991F;

        parts.get(0).zRot = -quarterPi;
        parts.get(1).zRot = quarterPi;
        parts.get(2).zRot = -quarterPi * 0.74F;
        parts.get(3).zRot = quarterPi * 0.74F;
        parts.get(4).zRot = -quarterPi * 0.74F;
        parts.get(5).zRot = quarterPi * 0.74F;
        parts.get(6).zRot = -quarterPi;
        parts.get(7).zRot = quarterPi;

        parts.get(0).yRot = base * 2.0F;
        parts.get(1).yRot = -base * 2.0F;
        parts.get(2).yRot = base;
        parts.get(3).yRot = -base;
        parts.get(4).yRot = -base;
        parts.get(5).yRot = base;
        parts.get(6).yRot = -base * 2.0F;
        parts.get(7).yRot = base * 2.0F;

        float y1 = -(Mth.cos(swing * 0.6662F * 2.0F) * 0.4F) * amount;
        float y2 = -(Mth.cos(swing * 0.6662F * 2.0F + (float) Math.PI) * 0.4F) * amount;
        float y3 = -(Mth.cos(swing * 0.6662F * 2.0F + (float) Math.PI / 2F) * 0.4F) * amount;
        float y4 = -(Mth.cos(swing * 0.6662F * 2.0F + (float) Math.PI * 3F / 2F) * 0.4F) * amount;
        float z1 = Math.abs(Mth.sin(swing * 0.6662F) * 0.4F) * amount;
        float z2 = Math.abs(Mth.sin(swing * 0.6662F + (float) Math.PI) * 0.4F) * amount;
        float z3 = Math.abs(Mth.sin(swing * 0.6662F + (float) Math.PI / 2F) * 0.4F) * amount;
        float z4 = Math.abs(Mth.sin(swing * 0.6662F + (float) Math.PI * 3F / 2F) * 0.4F) * amount;

        parts.get(0).yRot += y1;
        parts.get(1).yRot += -y1;
        parts.get(2).yRot += y2;
        parts.get(3).yRot += -y2;
        parts.get(4).yRot += y3;
        parts.get(5).yRot += -y3;
        parts.get(6).yRot += y4;
        parts.get(7).yRot += -y4;
        parts.get(0).zRot += z1;
        parts.get(1).zRot += -z1;
        parts.get(2).zRot += z2;
        parts.get(3).zRot += -z2;
        parts.get(4).zRot += z3;
        parts.get(5).zRot += -z3;
        parts.get(6).zRot += z4;
        parts.get(7).zRot += -z4;
    }

    private void animCreeper(float swing, float amount, float yaw, float pitch) {
        switch (slot) {
            case HEAD -> lookAll(yaw, pitch);
            case LEGS -> {
                if (parts.size() >= 2) {
                    parts.get(0).xRot = Mth.cos(swing * 0.6662F) * 1.4F * amount;
                    parts.get(1).xRot = Mth.cos(swing * 0.6662F + (float) Math.PI) * 1.4F * amount;
                    parts.get(0).yRot = 0.0F;
                    parts.get(1).yRot = 0.0F;
                }
            }
            default -> {}
        }
    }
}
