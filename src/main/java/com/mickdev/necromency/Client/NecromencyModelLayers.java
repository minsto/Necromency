package com.mickdev.necromency.Client;

import com.mickdev.necromency.Necromency;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public final class NecromencyModelLayers {
    public static final ModelLayerLocation TEDDY =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Necromency.MODID, "teddy"), "main");

    public static final ModelLayerLocation ISAAC_NORMAL =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Necromency.MODID, "isaac_normal"), "main");

    public static final ModelLayerLocation ISAAC_BODY =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Necromency.MODID, "isaac_body"), "main");

    public static final ModelLayerLocation ISAAC_HEAD =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Necromency.MODID, "isaac_head"), "main");

    public static final ModelLayerLocation ISAAC_BLOOD =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Necromency.MODID, "isaac_blood"), "main");

    public static final ModelLayerLocation LAYER_Crawler=
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Necromency.MODID, "modelnightcrawer"), "main");
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Necromency.MODID, "model_altar"), "main");
    public static final ModelLayerLocation MINION =
            new ModelLayerLocation(
                    ResourceLocation.fromNamespaceAndPath(Necromency.MODID, "necro_base"), "main");

    /** Humanoïde 128×64 : tête/corps zombie + UV membres sur la zone poulet de {@link com.mickdev.necromency.Client.render.MinionCompositeAtlas}. */
    public static final ModelLayerLocation MINION_ATLAS =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Necromency.MODID, "necro_minion_atlas"), "main");

    /** Poulet Blockbench ({@link com.mickdev.necromency.Client.Models.NecromencyChicken}). */
    public static final ModelLayerLocation MINION_CHICKEN =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Necromency.MODID, "necromency_chicken"), "main");

    /** Tête villageois + nez pour minions mixtes. */
    public static final ModelLayerLocation MINION_VILLAGER_HEAD =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Necromency.MODID, "necro_villager_head"), "main");

    private NecromencyModelLayers() {}
}
