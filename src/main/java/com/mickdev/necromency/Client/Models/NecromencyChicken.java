package com.mickdev.necromency.Client.Models;

import com.mickdev.necromency.Client.Util.MinionRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * Poulet Blockbench ({@code EntytiModelpart/NecromencyChicken.bbmodel}) — mesh aligné texture vanilla poulet 64×32.
 */
public class NecromencyChicken extends EntityModel<MinionRenderState> {

    /**
     * Rehaussement monde (+Y) appliqué <strong>avant</strong> le 180° X (voir MinionRenderer) : si la
     * translation venait après le flip, le PoseStack l’appliquerait le long de l’axe Y local retourné et
     * on enfonce le modèle. Sommet des jambes 24/16 ; le corps tourné peut s’étendre un peu plus haut.
     */
    public static final float FLIP_180X_Y_LIFT = 2.0F;

    public final ModelPart head;
    public final ModelPart bill;
    public final ModelPart chin;
    public final ModelPart body;
    public final ModelPart leftWing;
    public final ModelPart rightWing;
    public final ModelPart leftLeg;
    public final ModelPart rightLeg;

    public NecromencyChicken(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.bill = root.getChild("bill");
        this.chin = root.getChild("chin");
        this.body = root.getChild("body");
        this.leftWing = root.getChild("left_wing");
        this.rightWing = root.getChild("right_wing");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
    }

    /** Slot minion ↔ morceaux poulet (export Blockbench). */
    public enum BodySlot {
        HEAD,
        BODY,
        LEFT_ARM,
        RIGHT_ARM,
        LEFT_LEG,
        RIGHT_LEG
    }

    /** N’affiche que le groupe correspondant au slot du minion (composite). */
    public void showOnly(BodySlot slot) {
        boolean head = slot == BodySlot.HEAD;
        this.head.visible = head;
        this.bill.visible = head;
        this.chin.visible = head;
        this.body.visible = slot == BodySlot.BODY;
        this.leftWing.visible = slot == BodySlot.LEFT_ARM;
        this.rightWing.visible = slot == BodySlot.RIGHT_ARM;
        this.leftLeg.visible = slot == BodySlot.LEFT_LEG;
        this.rightLeg.visible = slot == BodySlot.RIGHT_LEG;
    }

    /** Minion entièrement poulet : tout le mesh d’un coup (pas de découpe par slot). */
    public void setAllPartsVisible() {
        this.head.visible = true;
        this.bill.visible = true;
        this.chin.visible = true;
        this.body.visible = true;
        this.leftWing.visible = true;
        this.rightWing.visible = true;
        this.leftLeg.visible = true;
        this.rightLeg.visible = true;
    }

    /** Jambes poulet uniquement (composite mixte). */
    public void showLegsOnly() {
        this.head.visible = false;
        this.bill.visible = false;
        this.chin.visible = false;
        this.body.visible = false;
        this.leftWing.visible = false;
        this.rightWing.visible = false;
        this.leftLeg.visible = true;
        this.rightLeg.visible = true;
    }

    /** Composite tête autre mob + corps/ailes/jambes poulet. */
    public void setBodyLimbsVisible() {
        this.head.visible = false;
        this.bill.visible = false;
        this.chin.visible = false;
        this.body.visible = true;
        this.leftWing.visible = true;
        this.rightWing.visible = true;
        this.leftLeg.visible = true;
        this.rightLeg.visible = true;
    }

    public static LayerDefinition createBodyLayer() {
        // Aligne sur EntytiModelpart/NecromencyChicken.bbmodel (export Blockbench 5.1.3) — mêmes cubes/poses que NecromencyChicken.java dans EntytiModelpart/.
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -3.0F, -1.5F, 4.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 10.0F, -2.5F));

        partdefinition.addOrReplaceChild("bill", CubeListBuilder.create().texOffs(14, 0).addBox(-2.0F, -1.0F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 10.0F, -5.0F));

        partdefinition.addOrReplaceChild("chin", CubeListBuilder.create().texOffs(14, 4).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 12.0F, -4.0F));

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 16.999F, -0.9564F, 1.5708F, 0.0F, 0.0F));
        body.addOrReplaceChild("body_r1", CubeListBuilder.create().texOffs(0, 9).addBox(-2.0F, -3.0F, -3.0F, 6.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -0.0436F, 0.999F, -1.5272F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(24, 13).addBox(-0.5F, -2.0F, -3.0F, 1.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(3.5F, 15.0F, -4.0F));

        partdefinition.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(24, 13).addBox(-0.5F, -2.0F, -3.0F, 1.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.5F, 15.0F, -4.0F));

        partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(26, 0).addBox(-1.5F, -2.5F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5F, 21.5F, -0.5F));

        partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(26, 0).addBox(-1.5F, -2.5F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.5F, 21.5F, -0.5F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(MinionRenderState state) {
        float limbSwing = state.walkAnimationPos;
        float limbSwingAmount = state.walkAnimationSpeed;
        float ageInTicks = state.ageInTicks;

        float hx = state.xRot * (Mth.PI / 180F);
        float hy = state.yRot * (Mth.PI / 180F);
        this.head.xRot = hx;
        this.head.yRot = hy;
        this.bill.xRot = hx;
        this.bill.yRot = hy;
        this.chin.xRot = hx;
        this.chin.yRot = hy;

        float flap = (Mth.cos(ageInTicks * 2.25F) + 1F) * 0.25F;
        this.leftWing.zRot = flap;
        this.rightWing.zRot = -flap;

        float walk = limbSwingAmount * 0.8F;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * walk;
        this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F + Mth.PI) * 1.4F * walk;
    }
}
