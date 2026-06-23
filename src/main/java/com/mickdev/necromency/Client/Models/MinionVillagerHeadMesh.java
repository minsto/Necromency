package com.mickdev.necromency.Client.Models;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.PartPose;

/** Humanoïde avec tête + nez villageois (UV {@code villager.png}). */
public final class MinionVillagerHeadMesh {

    private MinionVillagerHeadMesh() {}

    public static LayerDefinition createBodyLayer() {
        CubeDeformation def = CubeDeformation.NONE;
        MeshDefinition mesh = HumanoidModel.createMesh(def, 0.0F);
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild(
                "head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, def),
                PartPose.ZERO
        );
        head.addOrReplaceChild(
                "nose",
                CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, -1.0F, -6.0F, 2.0F, 4.0F, 2.0F, def),
                PartPose.offset(0.0F, -2.0F, 0.0F)
        );

        return LayerDefinition.create(mesh, 64, 64);
    }
}
