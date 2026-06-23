package com.mickdev.necromency;

import net.minecraft.resources.ResourceLocation;

public final class NecromencyRefs {
    public static final String MOD_ID = "necromency";
    public static final String MOD_NAME = "Necromancy";

    public static final String TEX = "textures";
    public static final String TEX_ENTITIES = TEX + "/entities";
    public static final String TEX_GUIS = TEX + "/guis";
    public static final String TEX_ITEMS = TEX + "/items";
    public static final String TEX_MODELS = TEX + "/models";

    public static final ResourceLocation TEX_ENTITY_NECROMANCER = rl(TEX_ENTITIES + "/villagernecro.png");
    public static final ResourceLocation TEX_ENTITY_NIGHTCRAWLER = rl(TEX_ENTITIES + "/nightcrawler.png");
    public static final ResourceLocation TEX_ENTITY_TEDDY = rl(TEX_ENTITIES + "/teddy.png");
    public static final ResourceLocation TEX_ENTITY_ISAAC = rl(TEX_ENTITIES + "/isaac.png");
    public static final ResourceLocation TEX_ENTITY_ISAAC_BLOOD = rl(TEX_ENTITIES + "/isaacblood.png");

    public static final ResourceLocation TEX_MODEL_SCYTHE = rl(TEX_MODELS + "/scythe.png");
    public static final ResourceLocation TEX_MODEL_SCYTHE_BONE = rl(TEX_MODELS + "/scythebone.png");
    public static final ResourceLocation TEX_MODEL_NECRONOMICON = rl(TEX_MODELS + "/necronomicon.png");
    public static final ResourceLocation TEX_MODEL_ALTAR = rl(TEX_MODELS + "/altartexture.png");
    public static final ResourceLocation TEX_MODEL_SEWING = rl(TEX_MODELS + "/sewingtexture.png");

    public static final ResourceLocation TEX_GUI_ALTAR = rl(TEX_GUIS + "/altargui.png");
    public static final ResourceLocation TEX_GUI_SEWING = rl(TEX_GUIS + "/sewinggui.png");

    public static final ResourceLocation TEX_PARTICLES = rl(TEX + "/particles.png");

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    private NecromencyRefs() {}
}
