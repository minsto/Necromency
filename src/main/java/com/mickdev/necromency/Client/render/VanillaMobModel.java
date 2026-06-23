package com.mickdev.necromency.Client.render;

import com.mickdev.necromency.Client.Util.MinionRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;

import javax.annotation.Nullable;

/**
 * Enveloppe un {@link ModelPart} vanilla déjà baké (modèle officiel d'un mob 1.21) pour le
 * rendre tel quel dans le repère du minion. La géométrie et les UV viennent directement du jeu,
 * donc aucun cube n'est retranscrit à la main. Pose statique + suivi du regard si une tête existe.
 */
public final class VanillaMobModel extends EntityModel<MinionRenderState> {

    private static final float DEG = (float) Math.PI / 180.0F;

    @Nullable
    private final ModelPart head;

    public VanillaMobModel(ModelPart root) {
        super(root);
        this.head = findHead(root);
    }

    @Nullable
    private static ModelPart findHead(ModelPart root) {
        if (root.hasChild("head")) {
            return root.getChild("head");
        }
        if (root.hasChild("body") && root.getChild("body").hasChild("head")) {
            return root.getChild("body").getChild("head");
        }
        return null;
    }

    @Override
    public void setupAnim(MinionRenderState state) {
        super.setupAnim(state);
        if (head != null) {
            head.yRot = state.yRot * DEG;
            head.xRot = state.xRot * DEG;
        }
    }
}
