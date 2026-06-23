package com.mickdev.necromency.necroapi.NecroAPIRemake;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;

public final class HumanoidParts {
    private HumanoidParts() {}

    public static ModelPart head(HumanoidModel<?> m) { return m.head; }
    public static ModelPart body(HumanoidModel<?> m) { return m.body; }
    public static ModelPart leftArm(HumanoidModel<?> m) { return m.leftArm; }
    public static ModelPart rightArm(HumanoidModel<?> m) { return m.rightArm; }
    public static ModelPart leftLeg(HumanoidModel<?> m) { return m.leftLeg; }
    public static ModelPart rightLeg(HumanoidModel<?> m) { return m.rightLeg; }
}
