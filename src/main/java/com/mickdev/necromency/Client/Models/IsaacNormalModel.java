package com.mickdev.necromency.Client.Models;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

public class IsaacNormalModel extends EntityModel<LivingEntityRenderState> {

    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart arm;
    private final ModelPart leg;
    private final ModelPart bb_main;

    public IsaacNormalModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.arm  = root.getChild("arm");
        this.leg  = root.getChild("leg");
        this.bb_main = root.getChild("bb_main");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-3.0F, -32.0F, -1.0F, 14.0F, 16.0F, 6.0F),
                PartPose.offset(-4.0F, 24.0F, 0.0F));

        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 22)
                        .addBox(-11.0F, -25.0F, -3.0F, 10.0F, 9.0F, 10.0F),
                PartPose.offset(6.0F, 8.0F, 0.0F));

        partdefinition.addOrReplaceChild("arm", CubeListBuilder.create(),
                PartPose.offset(4.0F, -8.0F, -2.0F));

        partdefinition.addOrReplaceChild("leg", CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        partdefinition.addOrReplaceChild("bb_main",
                CubeListBuilder.create()
                        .texOffs(40, 0).addBox(-12.0F, -32.0F, -1.0F, 5.0F, 20.0F, 6.0F)
                        .texOffs(40, 26).addBox(7.0F, -32.0F, -1.0F, 5.0F, 20.0F, 6.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }


    @Override
    public void setupAnim(LivingEntityRenderState state) {
        // Head
        head.yRot = state.yRot * ((float) Math.PI / 180F);
        head.xRot = state.xRot * ((float) Math.PI / 180F);

        float swing = state.walkAnimationPos;
        float amt = state.walkAnimationSpeed;

        leg.xRot = Mth.cos(swing * 0.6662F) * 1.4F * amt;
        leg.xRot = Mth.cos(swing * 0.6662F + (float) Math.PI) * 1.4F * amt;
        arm.xRot = leg.xRot;
        arm.xRot = leg.xRot;
    }
}
