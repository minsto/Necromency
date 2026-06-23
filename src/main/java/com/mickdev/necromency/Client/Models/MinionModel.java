package com.mickdev.necromency.Client.Models;

import com.mickdev.necromency.Client.Util.MinionRenderState;
import com.mickdev.necromency.Client.Util.PartProfile;
import com.mickdev.necromency.Client.Util.PartProfiles;
import com.mickdev.necromency.Client.Util.PartSlot;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.util.Mth;

/**
 * Modèle de base du minion.
 *
 * Important : ce modèle est rendu plusieurs fois par MinionRenderer.
 * À chaque rendu, on affiche seulement UNE partie : tête, corps, bras gauche, etc.
 */
public class MinionModel extends HumanoidModel<MinionRenderState> {

    public enum BodySlot {
        HEAD,
        BODY,
        LEFT_ARM,
        RIGHT_ARM,
        LEFT_LEG,
        RIGHT_LEG
    }

    private final float baseHeadX, baseHeadY, baseHeadZ;
    private final float baseBodyX, baseBodyY, baseBodyZ;
    private final float baseLeftArmX, baseLeftArmY, baseLeftArmZ;
    private final float baseRightArmX, baseRightArmY, baseRightArmZ;
    private final float baseLeftLegX, baseLeftLegY, baseLeftLegZ;
    private final float baseRightLegX, baseRightLegY, baseRightLegZ;

    public MinionModel(ModelPart root) {
        super(root);

        this.baseHeadX = this.head.x;
        this.baseHeadY = this.head.y;
        this.baseHeadZ = this.head.z;

        this.baseBodyX = this.body.x;
        this.baseBodyY = this.body.y;
        this.baseBodyZ = this.body.z;

        this.baseLeftArmX = this.leftArm.x;
        this.baseLeftArmY = this.leftArm.y;
        this.baseLeftArmZ = this.leftArm.z;

        this.baseRightArmX = this.rightArm.x;
        this.baseRightArmY = this.rightArm.y;
        this.baseRightArmZ = this.rightArm.z;

        this.baseLeftLegX = this.leftLeg.x;
        this.baseLeftLegY = this.leftLeg.y;
        this.baseLeftLegZ = this.leftLeg.z;

        this.baseRightLegX = this.rightLeg.x;
        this.baseRightLegY = this.rightLeg.y;
        this.baseRightLegZ = this.rightLeg.z;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(MinionRenderState state) {
        resetPoseAndVisibility();

        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
        copyPartPose(this.hat, this.head);

        float limbSwing = state.walkAnimationPos;
        float limbSwingAmount = Mth.clamp(state.walkAnimationSpeed, 0.0F, 1.0F);

        this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + Mth.PI) * 2.0F * limbSwingAmount * 0.5F;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
        this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + Mth.PI) * 1.4F * limbSwingAmount;

        applyProfiles(state);
    }

    public void showOnly(BodySlot slot) {
        this.head.visible = slot == BodySlot.HEAD;
        this.hat.visible = false;
        this.body.visible = slot == BodySlot.BODY;
        this.leftArm.visible = slot == BodySlot.LEFT_ARM;
        this.rightArm.visible = slot == BodySlot.RIGHT_ARM;
        this.leftLeg.visible = slot == BodySlot.LEFT_LEG;
        this.rightLeg.visible = slot == BodySlot.RIGHT_LEG;
    }

    private void resetPoseAndVisibility() {
        resetPart(this.head, baseHeadX, baseHeadY, baseHeadZ);
        resetPart(this.hat, baseHeadX, baseHeadY, baseHeadZ);
        resetPart(this.body, baseBodyX, baseBodyY, baseBodyZ);
        resetPart(this.leftArm, baseLeftArmX, baseLeftArmY, baseLeftArmZ);
        resetPart(this.rightArm, baseRightArmX, baseRightArmY, baseRightArmZ);
        resetPart(this.leftLeg, baseLeftLegX, baseLeftLegY, baseLeftLegZ);
        resetPart(this.rightLeg, baseRightLegX, baseRightLegY, baseRightLegZ);

        this.head.visible = true;
        this.hat.visible = false;
        this.body.visible = true;
        this.leftArm.visible = true;
        this.rightArm.visible = true;
        this.leftLeg.visible = true;
        this.rightLeg.visible = true;
    }

    private static void copyPartPose(ModelPart to, ModelPart from) {
        to.x = from.x;
        to.y = from.y;
        to.z = from.z;
        to.xRot = from.xRot;
        to.yRot = from.yRot;
        to.zRot = from.zRot;
        to.xScale = from.xScale;
        to.yScale = from.yScale;
        to.zScale = from.zScale;
    }

    private static void resetPart(ModelPart part, float x, float y, float z) {
        part.x = x;
        part.y = y;
        part.z = z;

        part.xRot = 0.0F;
        part.yRot = 0.0F;
        part.zRot = 0.0F;

        part.xScale = 1.0F;
        part.yScale = 1.0F;
        part.zScale = 1.0F;
    }

    private void applyProfiles(MinionRenderState state) {
        applyProfile(this.head, PartProfiles.getHeadProfile(state.headType, state.partPoseAnchor));
        applyProfile(this.body, PartProfiles.get(PartSlot.BODY, state.bodyType));
        applyProfile(this.leftArm, PartProfiles.get(PartSlot.ARM_L, state.armLType));
        applyProfile(this.rightArm, PartProfiles.get(PartSlot.ARM_R, state.armRType));
        applyProfile(this.leftLeg, PartProfiles.get(PartSlot.LEGS, state.legsType));
        applyProfile(this.rightLeg, PartProfiles.get(PartSlot.LEGS, state.legsType));
    }

    private static void applyProfile(ModelPart part, PartProfile profile) {
        if (profile == null) return;

        part.x += profile.offX();
        part.y += profile.offY();
        part.z += profile.offZ();

        part.xRot += profile.rotX();
        part.yRot += profile.rotY();
        part.zRot += profile.rotZ();

        part.xScale *= profile.scale();
        part.yScale *= profile.scale();
        part.zScale *= profile.scale();
    }
}
