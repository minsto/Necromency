package com.mickdev.necromency.Client.Models;

import com.mickdev.necromency.Client.Util.MinionRenderState;
import com.mickdev.necromency.Client.Util.MixedVerticalAlign;
import com.mickdev.necromency.Client.Util.PartProfile;
import com.mickdev.necromency.Client.Util.PartProfiles;
import com.mickdev.necromency.Client.Util.PartSlot;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class NecroEntityBaseModel extends HumanoidModel<MinionRenderState> {

    /** Positions de base (reset chaque frame avant profils), sinon les += des PartProfile dérivent à l’infini. */
    private final float baseHeadX, baseHeadY, baseHeadZ;
    private final float baseBodyX, baseBodyY, baseBodyZ;
    private final float baseArmLX, baseArmLY, baseArmLZ;
    private final float baseArmRX, baseArmRY, baseArmRZ;
    private final float baseLegLX, baseLegLY, baseLegLZ;
    private final float baseLegRX, baseLegRY, baseLegRZ;

    public NecroEntityBaseModel(ModelPart root) {
        super(root);
        this.baseHeadX = this.head.x;
        this.baseHeadY = this.head.y;
        this.baseHeadZ = this.head.z;
        this.baseBodyX = this.body.x;
        this.baseBodyY = this.body.y;
        this.baseBodyZ = this.body.z;
        this.baseArmLX = this.leftArm.x;
        this.baseArmLY = this.leftArm.y;
        this.baseArmLZ = this.leftArm.z;
        this.baseArmRX = this.rightArm.x;
        this.baseArmRY = this.rightArm.y;
        this.baseArmRZ = this.rightArm.z;
        this.baseLegLX = this.leftLeg.x;
        this.baseLegLY = this.leftLeg.y;
        this.baseLegLZ = this.leftLeg.z;
        this.baseLegRX = this.rightLeg.x;
        this.baseLegRY = this.rightLeg.y;
        this.baseLegRZ = this.rightLeg.z;
    }

    /** Rendu mixte sans slot connu (ex. atlas entier). */
    public void setupAnimForMixedPass(MinionRenderState s) {
        setupAnimForMixedPass(s, null, false);
    }

    public void setupAnimForMixedPass(MinionRenderState s, @javax.annotation.Nullable PartSlot visibleSlot, boolean atlasChickenMesh) {
        super.setupAnim(s);
        resetBase();
        if (visibleSlot != null && !atlasChickenMesh) {
            switch (visibleSlot) {
                case HEAD -> initHead(s);
                case BODY -> initBody(s.bodyType);
                case ARM_L -> initArmL(s.armLType);
                case ARM_R -> initArmR(s.armRType);
                case LEGS -> initLegs(s.legsType);
                default -> {}
            }
            MixedVerticalAlign.apply(this, s, visibleSlot);
        }
    }

    @Override
    public void setupAnim(MinionRenderState s) {
        super.setupAnim(s);

        resetBase();

        initHead(s);
        if (!s.skipChickenLimbProfiles) {
            initBody(s.bodyType);
            initArmL(s.armLType);
            initArmR(s.armRType);
            initLegs(s.legsType);
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();
        return LayerDefinition.create(mesh, 64, 64);
    }

    private void resetBase() {
        this.head.x = baseHeadX;
        this.head.y = baseHeadY;
        this.head.z = baseHeadZ;
        this.body.x = baseBodyX;
        this.body.y = baseBodyY;
        this.body.z = baseBodyZ;
        this.leftArm.x = baseArmLX;
        this.leftArm.y = baseArmLY;
        this.leftArm.z = baseArmLZ;
        this.rightArm.x = baseArmRX;
        this.rightArm.y = baseArmRY;
        this.rightArm.z = baseArmRZ;
        this.leftLeg.x = baseLegLX;
        this.leftLeg.y = baseLegLY;
        this.leftLeg.z = baseLegLZ;
        this.rightLeg.x = baseLegRX;
        this.rightLeg.y = baseLegRY;
        this.rightLeg.z = baseLegRZ;

        this.head.xScale = this.head.yScale = this.head.zScale = 1f;
        this.body.xScale = this.body.yScale = this.body.zScale = 1f;
        this.leftArm.xScale = this.leftArm.yScale = this.leftArm.zScale = 1f;
        this.rightArm.xScale = this.rightArm.yScale = this.rightArm.zScale = 1f;
        this.leftLeg.xScale = this.leftLeg.yScale = this.leftLeg.zScale = 1f;
        this.rightLeg.xScale = this.rightLeg.yScale = this.rightLeg.zScale = 1f;
    }

    private void initHead(MinionRenderState s) {
        PartProfile p = PartProfiles.getHeadProfile(s.headType, s.partPoseAnchor);
        applyProfile(this.head, p);
    }

    private void initLegs(ResourceLocation type) {
        PartProfile p = PartProfiles.get(PartSlot.LEGS, type);
        applyProfile(this.leftLeg, p);
        applyProfile(this.rightLeg, p);
    }

    private void initBody(ResourceLocation type) {
        PartProfile p = PartProfiles.get(PartSlot.BODY, type);
        applyProfile(this.body, p);
    }

    private void initArmL(ResourceLocation type) {
        PartProfile p = PartProfiles.get(PartSlot.ARM_L, type);
        applyProfile(this.leftArm, p);
    }

    private void initArmR(ResourceLocation type) {
        PartProfile p = PartProfiles.get(PartSlot.ARM_R, type);
        applyProfile(this.rightArm, p);
    }

    private static void applyProfile(ModelPart part, PartProfile p) {
        if (p == null) return;

        part.x += p.offX();
        part.y += p.offY();
        part.z += p.offZ();

        part.xRot += p.rotX();
        part.yRot += p.rotY();
        part.zRot += p.rotZ();

        float sc = p.scale();
        part.xScale *= sc;
        part.yScale *= sc;
        part.zScale *= sc;

        part.xRot = Mth.clamp(part.xRot, -3.0f, 3.0f);
    }
}
