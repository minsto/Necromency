package com.mickdev.necromency.registry.Altar.Model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.EntityModel;


public class ModelAltar extends EntityModel<LivingEntityRenderState> {

   // public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("necromency", "altar"), "main");
    public final ModelPart Bases;
    public final ModelPart BoockTables;
    public final ModelPart Boocks;

    public ModelAltar(ModelPart root) {
        super(root);
        this.Bases = root.getChild("Bases");
        this.BoockTables = root.getChild("BoockTables");
        this.Boocks = root.getChild("Boocks");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition Bases = partdefinition.addOrReplaceChild("Bases", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -2.0F, -24.0F, 16.0F, 2.0F, 32.0F, new CubeDeformation(0.0F)).texOffs(0, 34)
                .addBox(-8.0F, -18.0F, -24.0F, 16.0F, 2.0F, 32.0F, new CubeDeformation(0.0F)).texOffs(0, 68).addBox(-6.0F, -16.0F, -22.0F, 12.0F, 14.0F, 28.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition BoockTables = partdefinition
                .addOrReplaceChild(
                        "BoockTables", CubeListBuilder.create().texOffs(80, 68).addBox(-13.0F, 0.0F, 13.0F, 16.0F, 2.0F, 16.0F, new CubeDeformation(0.0F)).texOffs(96, 0).addBox(-10.0F, -17.0F, 16.0F, 9.0F, 17.0F, 9.0F, new CubeDeformation(0.0F))
                                .texOffs(80, 86).addBox(-13.0F, -18.0F, 13.0F, 16.0F, 1.0F, 16.0F, new CubeDeformation(0.0F)).texOffs(96, 48).addBox(-13.0F, -20.0F, 13.0F, 16.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
                        PartPose.offset(5.0F, 22.0F, -5.0F));
        PartDefinition flatpart2_r1 = BoockTables.addOrReplaceChild("flatpart2_r1",
                CubeListBuilder.create().texOffs(96, 51).addBox(0.0F, -2.0F, -1.0F, 1.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)).texOffs(96, 51).addBox(15.0F, -2.0F, -1.0F, 1.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-13.0F, -18.0F, 14.0F, -0.1309F, 0.0F, 0.0F));
        PartDefinition Boocks = partdefinition.addOrReplaceChild("Boocks", CubeListBuilder.create(), PartPose.offset(7.0F, 4.0F, 25.0F));
        PartDefinition boockp2_r1 = Boocks.addOrReplaceChild("boockp2_r1", CubeListBuilder.create().texOffs(96, 37).addBox(-5.0F, 0.0F, -2.0F, 6.0F, 0.0F, 11.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-7.0F, 0.0F, -12.0F, 0.0F, 0.0F, 0.3054F));
        PartDefinition boockp1_r1 = Boocks.addOrReplaceChild("boockp1_r1", CubeListBuilder.create().texOffs(96, 26).addBox(-5.0F, 0.0F, -1.0F, 6.0F, 0.0F, 11.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-4.0F, -1.0F, -13.0F, 0.0F, 0.0F, -0.2618F));
        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    public void setupAnim(LivingEntityRenderState state) {
        float limbSwing = state.walkAnimationPos;
        float limbSwingAmount = state.walkAnimationSpeed;
        float ageInTicks = state.ageInTicks;
        float netHeadYaw = state.yRot;
        float headPitch = state.xRot;

    }

}
