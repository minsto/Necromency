package com.mickdev.necromency.Client.Models;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.PartPose;

/**
 * Humanoïde standard (cou y=0, hanches y=12) avec UV poulet sur l'atlas 128×64.
 * Torse étroit (4 px) pour coller aux proportions poulet, jambes courtes sous y=12.
 */
public final class MinionAtlasHumanoidMesh {

    private MinionAtlasHumanoidMesh() {}

    public static LayerDefinition createBodyLayer() {
        CubeDeformation def = CubeDeformation.NONE;
        MeshDefinition mesh = HumanoidModel.createMesh(def, 0.0F);
        PartDefinition root = mesh.getRoot();

        // Jambes poulet (UV 3×5) — hauteur vanilla, pieds vers le sol (y=24).
        root.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create().texOffs(64 + 26, 0).addBox(-1.5F, 7.0F, -1.5F, 3.0F, 5.0F, 3.0F, def),
                PartPose.offset(-1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create().mirror().texOffs(64 + 26, 0).addBox(-1.5F, 7.0F, -1.5F, 3.0F, 5.0F, 3.0F, def),
                PartPose.offset(1.9F, 12.0F, 0.0F));

        // Ailes resserrées sur torse étroit (±3 au lieu de ±5).
        root.addOrReplaceChild(
                "right_arm",
                CubeListBuilder.create().texOffs(64 + 24, 13).addBox(-0.5F, -2.0F, -3.0F, 1.0F, 4.0F, 6.0F, def),
                PartPose.offset(-3.0F, 2.0F, 0.0F));
        root.addOrReplaceChild(
                "left_arm",
                CubeListBuilder.create().mirror().texOffs(64 + 24, 13).addBox(-0.5F, -2.0F, -3.0F, 1.0F, 4.0F, 6.0F, def),
                PartPose.offset(3.0F, 2.0F, 0.0F));

        // Torse poulet étroit (UV 6×8 sur le corps vanilla).
        root.addOrReplaceChild(
                "body",
                CubeListBuilder.create().texOffs(64, 9).addBox(-3.0F, 4.0F, -3.0F, 6.0F, 8.0F, 6.0F, def),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 64);
    }
}
