package com.mickdev.necromency.Client.legacy;

import com.mickdev.necromency.Client.Util.MobTextureResolver;
import com.mickdev.necromency.Client.Util.PartSlot;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Géométrie des morceaux de minion transcrite 1:1 depuis les {@code NecroEntity*} de
 * Necromancy 1.7.10 (NecroEntityBiped / Quadruped / Spider / etc.). Chaque mob fournit,
 * par slot, ses boîtes (offsets de texture et points de rotation d'origine) ainsi que
 * les ancres d'assemblage : {@code torsoPos} (des jambes), {@code armLeftPos},
 * {@code armRightPos} et {@code headPos} (du torse), exactement comme ModelMinion 1.7.10.
 */
public final class LegacyNecroParts {

    /** Famille d'animation du mob source (logique setRotationAngles d'origine). */
    public enum Anim { BIPED, ENDERMAN, VILLAGER, QUADRUPED, CHICKEN, SPIDER, CREEPER }

    public record MobDef(
            ResourceLocation texture,
            int texW, int texH,
            float scale,
            Anim anim,
            float[] torsoPos,
            float[] armLeftPos, float[] armRightPos, float[] headPos,
            @Nullable Consumer<PartDefinition> head,
            @Nullable Consumer<PartDefinition> torso,
            @Nullable Consumer<PartDefinition> armLeft,
            @Nullable Consumer<PartDefinition> armRight,
            @Nullable Consumer<PartDefinition> legs) {

        @Nullable
        public Consumer<PartDefinition> builder(PartSlot slot) {
            return switch (slot) {
                case HEAD -> head;
                case BODY -> torso;
                case ARM_L -> armLeft;
                case ARM_R -> armRight;
                case LEGS -> legs;
            };
        }
    }

    private static final Map<ResourceLocation, MobDef> MOBS = new HashMap<>();
    /** Incrémenter si la géométrie bake change (invalidation cache dev sans redémarrage). */
    private static final String BAKE_CACHE_REV = "spider-legs-original-v4";
    private static final Map<String, ModelPart> BAKE_CACHE = new HashMap<>();

    /** Ancres bipèdes par défaut (NecroEntityBiped) pour slots inconnus. */
    public static final float[] BIPED_TORSO_POS = {-4F, -2F, -2F};
    public static final float[] BIPED_ARM_L_POS = {-4F, 0F, 2F};
    public static final float[] BIPED_ARM_R_POS = {8F, 0F, 2F};
    public static final float[] BIPED_HEAD_POS = {4F, -4F, 2F};

    private LegacyNecroParts() {}

    public static boolean supports(@Nullable ResourceLocation mobId) {
        return get(mobId) != null;
    }

    @Nullable
    public static MobDef get(@Nullable ResourceLocation mobId) {
        mobId = normalize(mobId);
        if (mobId == null) {
            return null;
        }
        MobDef def = MOBS.get(mobId);
        if (def != null) {
            return def;
        }
        ResourceLocation byPath = mc(mobId.getPath());
        if (!byPath.equals(mobId)) {
            return MOBS.get(byPath);
        }
        return null;
    }

    @Nullable
    public static ResourceLocation normalize(@Nullable ResourceLocation mobId) {
        if (mobId == null) {
            return null;
        }
        ResourceLocation byPath = mc(mobId.getPath());
        if (MOBS.containsKey(byPath)) {
            return byPath;
        }
        if ("minecraft".equals(mobId.getNamespace())) {
            return ResourceLocation.fromNamespaceAndPath("minecraft", mobId.getPath());
        }
        return mobId;
    }

    /**
     * Texture d'un slot legacy — même principe que le cochon : atlas entité du mob,
     * jamais les icônes inventaire 16×16 ({@code textures/item/bodyparts/…}).
     */
    public static ResourceLocation textureForSlot(PartSlot slot, ResourceLocation mobId) {
        MobDef def = get(mobId);
        if (def == null) {
            return MobTextureResolver.MISSING;
        }
        return MobTextureResolver.textureForMobId(mobId);
    }

    /** Bake (avec cache) du groupe d'un slot pour un mob donné. */
    public static ModelPart bake(PartSlot slot, ResourceLocation mobId) {
        String key = BAKE_CACHE_REV + "|" + slot.name() + "|" + mobId;
        return BAKE_CACHE.computeIfAbsent(key, k -> {
            MobDef def = get(mobId);
            if (def == null) {
                throw new IllegalArgumentException("Unknown legacy mob: " + mobId);
            }
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = mesh.getRoot();
            PartDefinition group = root.addOrReplaceChild("slot", CubeListBuilder.create(), PartPose.ZERO);
            Consumer<PartDefinition> builder = def.builder(slot);
            if (builder != null) {
                builder.accept(group);
            }
            return LayerDefinition.create(mesh, def.texW(), def.texH()).bakeRoot();
        });
    }

    // ------------------------------------------------------------------
    // Aides de construction
    // ------------------------------------------------------------------

    private static CubeListBuilder cube() {
        return CubeListBuilder.create();
    }

    private static void child(PartDefinition parent, String name, CubeListBuilder cubes, PartPose pose) {
        parent.addOrReplaceChild(name, cubes, pose);
    }

    // --- NecroEntityBiped (zombie, pigzombie, base squelette/golem) ---

    private static Consumer<PartDefinition> bipedHead() {
        return g -> {
            child(g, "p0", cube().texOffs(0, 0).addBox(-4, -4, -4, 8, 8, 8), PartPose.ZERO);
            child(g, "p1", cube().texOffs(32, 0).addBox(-4, -4, -4, 8, 8, 8, new CubeDeformation(0.5F)), PartPose.ZERO);
        };
    }

    private static Consumer<PartDefinition> bipedTorso() {
        return g -> child(g, "p0", cube().texOffs(16, 16).addBox(0, 0, 0, 8, 12, 4), PartPose.ZERO);
    }

    private static Consumer<PartDefinition> bipedArmLeft() {
        return g -> child(g, "p0", cube().texOffs(40, 16).mirror().addBox(0, 0, -2, 4, 12, 4), PartPose.ZERO);
    }

    private static Consumer<PartDefinition> bipedArmRight() {
        return g -> child(g, "p0", cube().texOffs(40, 16).addBox(0, 0, -2, 4, 12, 4), PartPose.ZERO);
    }

    private static Consumer<PartDefinition> bipedLegs() {
        return g -> {
            child(g, "p0", cube().texOffs(0, 16).mirror().addBox(-4, -2, -2, 4, 12, 4), PartPose.offset(0, 12, 0));
            child(g, "p1", cube().texOffs(0, 16).addBox(0, -2, -2, 4, 12, 4), PartPose.offset(0, 12, 0));
        };
    }

    private static MobDef biped(ResourceLocation texture, int texW, int texH) {
        return new MobDef(texture, texW, texH, 1.0F, Anim.BIPED,
                BIPED_TORSO_POS, BIPED_ARM_L_POS, BIPED_ARM_R_POS, BIPED_HEAD_POS,
                bipedHead(), bipedTorso(), bipedArmLeft(), bipedArmRight(), bipedLegs());
    }

    // --- NecroEntityQuadruped (cochon, mouton, vache via overrides) ---

    private static Consumer<PartDefinition> quadHead() {
        return g -> child(g, "p0", cube().texOffs(0, 0).addBox(-4, -4, -4, 8, 8, 8), PartPose.ZERO);
    }

    private static Consumer<PartDefinition> quadTorso() {
        // Rotation π/2 bakée : le torse quadrupède doit être horizontal même si setupAnim n'est pas rappelé
        // (SubmitNodeCollector 1.21). Correspond à ModelMinion 1.7.10 / animQuadruped BODY.
        return g -> child(g, "p0", cube().texOffs(28, 8).addBox(-1, -12, -12, 10, 16, 8),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, (float) (Math.PI / 2.0), 0.0F, 0.0F));
    }

    private static Consumer<PartDefinition> quadArm(boolean mirror, int size) {
        return g -> {
            CubeListBuilder b = cube().texOffs(0, 16);
            if (mirror) b.mirror();
            b.addBox(0, 0, -1, 4, size, 4);
            child(g, "p0", b, PartPose.ZERO);
        };
    }

    private static Consumer<PartDefinition> quadLegs(int size) {
        return g -> {
            child(g, "p0", cube().texOffs(0, 16).mirror().addBox(-2, 0, -2, 4, size, 4),
                    PartPose.offset(-3, 22 - size, 3));
            child(g, "p1", cube().texOffs(0, 16).addBox(-2, 0, -2, 4, size, 4),
                    PartPose.offset(3, 22 - size, 3));
        };
    }

    private static MobDef quadruped(ResourceLocation texture, int texW, int texH, int size,
                                    @Nullable Consumer<PartDefinition> headOverride) {
        return new MobDef(texture, texW, texH, 1.0F, Anim.QUADRUPED,
                new float[]{-4F, 4F, 0F},
                new float[]{-1F, 12F, -10F}, new float[]{5F, 12F, -10F}, new float[]{4F, 12 - size, -14F},
                headOverride != null ? headOverride : quadHead(),
                quadTorso(), quadArm(true, size), quadArm(false, size), quadLegs(size));
    }

    // --- NecroEntitySpider / Squid : 8 pattes (18,0) ---

    private static Consumer<PartDefinition> eightLegs() {
        return g -> {
            float[][] points = {
                    {-4, 15, 2}, {4, 15, 2},
                    {-4, 15, 1}, {4, 15, 1},
                    {-4, 15, 0}, {4, 15, 0},
                    {-4, 15, -1}, {4, 15, -1}
            };
            for (int i = 0; i < 8; i++) {
                boolean left = (i % 2) == 0;
                CubeListBuilder b = cube().texOffs(18, 0);
                if (left) {
                    b.addBox(-15, -1, -1, 16, 2, 2);
                } else {
                    b.addBox(-1, -1, -1, 16, 2, 2);
                }
                child(g, "p" + i, b, PartPose.offset(points[i][0], points[i][1], points[i][2]));
            }
        };
    }

    private static Consumer<PartDefinition> spiderLegs() {
        // NecroEntitySpider.initLegs 1.7.10 : 8 pattes horizontales avec rotationPoint par patte.
        return g -> {
            float[][] rotPoints = {
                    {-4, 15, 2}, {4, 15, 2},
                    {-4, 15, 1}, {4, 15, 1},
                    {-4, 15, 0}, {4, 15, 0},
                    {-4, 15, -1}, {4, 15, -1}
            };
            for (int i = 0; i < 8; i++) {
                boolean left = (i % 2) == 0;
                CubeListBuilder b = cube().texOffs(18, 0);
                if (left) {
                    b.addBox(-15, -1, -1, 16, 2, 2);
                } else {
                    b.addBox(-1, -1, -1, 16, 2, 2);
                }
                float[] rp = rotPoints[i];
                child(g, "p" + i, b, PartPose.offset(rp[0], rp[1], rp[2]));
            }
        };
    }

    private static Consumer<PartDefinition> spiderTorso() {
        // NecroEntitySpider.initTorso : corps + « cou » (p1=neck, p0=body dans l'ordre de rendu original).
        return g -> {
            child(g, "p0", cube().texOffs(0, 12).addBox(-1, 4, 0, 10, 8, 12), PartPose.ZERO);
            child(g, "p1", cube().texOffs(0, 0).addBox(1, 5, -6, 6, 6, 6), PartPose.ZERO);
        };
    }

    private static MobDef spider(ResourceLocation texture, float scale) {
        return new MobDef(texture, 64, 32, scale, Anim.SPIDER,
                new float[]{-4F, 6F, 3F},
                new float[]{-1F, 10F, -6F}, new float[]{5F, 10F, -6F}, new float[]{4F, 8F, -7F},
                g -> child(g, "p0", cube().texOffs(32, 4).addBox(-4, -4, -6, 8, 8, 8), PartPose.ZERO),
                spiderTorso(),
                null, null,
                spiderLegs());
    }

    // ------------------------------------------------------------------
    // Registre (ids modernes -> définitions 1.7.10)
    // ------------------------------------------------------------------

    private static ResourceLocation mc(String path) {
        return ResourceLocation.fromNamespaceAndPath("minecraft", path);
    }

    private static ResourceLocation mcTex(String path) {
        return ResourceLocation.withDefaultNamespace("textures/entity/" + path);
    }

    static {
        // Zombie (NecroEntityZombie : bipède pur, texture 64x64)
        MOBS.put(mc("zombie"), biped(mcTex("zombie/zombie.png"), 64, 64));

        // Pigman zombifié (NecroEntityPigZombie : bipède, 64x64)
        MOBS.put(mc("zombified_piglin"), biped(mcTex("piglin/zombified_piglin.png"), 64, 64));

        // Squelette (NecroEntitySkeleton : bras/jambes fins)
        MOBS.put(mc("skeleton"), new MobDef(mcTex("skeleton/skeleton.png"), 64, 32, 1.0F, Anim.BIPED,
                BIPED_TORSO_POS, BIPED_ARM_L_POS, BIPED_ARM_R_POS, BIPED_HEAD_POS,
                bipedHead(), bipedTorso(),
                g -> child(g, "p0", cube().texOffs(40, 16).mirror().addBox(2, 0, -1, 2, 12, 2), PartPose.ZERO),
                g -> child(g, "p0", cube().texOffs(40, 16).addBox(0, 0, -1, 2, 12, 2), PartPose.ZERO),
                g -> {
                    child(g, "p0", cube().texOffs(0, 16).mirror().addBox(-3, -2, -1, 2, 12, 2), PartPose.offset(0, 12, 0));
                    child(g, "p1", cube().texOffs(0, 16).addBox(1, -2, -1, 2, 12, 2), PartPose.offset(0, 12, 0));
                }));

        // Enderman (NecroEntityEnderman : membres 2x30x2, torse haut)
        MOBS.put(mc("enderman"), new MobDef(mcTex("enderman/enderman.png"), 64, 32, 1.0F, Anim.ENDERMAN,
                new float[]{-4F, -18F, 0F}, BIPED_ARM_L_POS, BIPED_ARM_R_POS, BIPED_HEAD_POS,
                g -> {
                    child(g, "p0", cube().texOffs(0, 0).addBox(-4, -7, -4, 8, 8, 8), PartPose.ZERO);
                    child(g, "p1", cube().texOffs(0, 16).addBox(-4, -3, -4, 8, 8, 8, new CubeDeformation(-0.5F)), PartPose.ZERO);
                },
                g -> child(g, "p0", cube().texOffs(32, 16).addBox(0, 0, 0, 8, 12, 4), PartPose.ZERO),
                g -> child(g, "p0", cube().texOffs(56, 0).mirror().addBox(2, 0, -1, 2, 30, 2), PartPose.ZERO),
                g -> child(g, "p0", cube().texOffs(56, 0).addBox(0, 0, -1, 2, 30, 2), PartPose.ZERO),
                g -> {
                    child(g, "p0", cube().texOffs(56, 0).mirror().addBox(-1, -4, 1, 2, 30, 2), PartPose.offset(2, -2, 0));
                    child(g, "p1", cube().texOffs(56, 0).addBox(-1, -4, 1, 2, 30, 2), PartPose.offset(-2, -2, 0));
                }));

        // Creeper (NecroEntityCreeper : 4 jambes, pas de bras)
        MOBS.put(mc("creeper"), new MobDef(mcTex("creeper/creeper.png"), 64, 32, 1.0F, Anim.CREEPER,
                new float[]{-4F, 4F, -2F}, BIPED_ARM_L_POS, BIPED_ARM_R_POS, BIPED_HEAD_POS,
                g -> child(g, "p0", cube().texOffs(0, 0).addBox(-4, -4, -4, 8, 8, 8), PartPose.ZERO),
                bipedTorso(),
                null, null,
                g -> {
                    child(g, "p0", cube().texOffs(0, 16).addBox(0, 16, 2, 4, 6, 4), PartPose.ZERO);
                    child(g, "p1", cube().texOffs(0, 16).addBox(-4, 16, 2, 4, 6, 4), PartPose.ZERO);
                    child(g, "p2", cube().texOffs(0, 16).addBox(-4, 16, -6, 4, 6, 4), PartPose.ZERO);
                    child(g, "p3", cube().texOffs(0, 16).addBox(0, 16, -6, 4, 6, 4), PartPose.ZERO);
                }));

        // Araignées (NecroEntitySpider / CaveSpider à 70 %)
        MOBS.put(mc("spider"), spider(mcTex("spider/spider.png"), 1.0F));
        MOBS.put(mc("cave_spider"), spider(mcTex("spider/cave_spider.png"), 0.7F));

        // Poulpe (NecroEntitySquid : corps massif + 8 tentacules)
        MOBS.put(mc("squid"), new MobDef(mcTex("squid/squid.png"), 64, 32, 1.0F, Anim.SPIDER,
                new float[]{-4F, 6F, 3F},
                new float[]{-6F, -4F, 0F}, new float[]{10F, -4F, 0F}, new float[]{4F, -8F, 0F},
                g -> child(g, "p0", cube().texOffs(0, 0).addBox(-6, -20, -6, 12, 16, 12), PartPose.offset(0, 8, 0)),
                g -> child(g, "p0", cube().texOffs(0, 0).addBox(-2, -12, -5, 12, 16, 12), PartPose.offset(0, 8, 0)),
                null, null,
                eightLegs()));

        // Villageois (NecroEntityVillager : robe, nez, bras croisés)
        MOBS.put(mc("villager"), new MobDef(mcTex("villager/villager.png"), 64, 64, 1.0F, Anim.VILLAGER,
                new float[]{-4F, 0F, -2F},
                new float[]{-4F, 0F, 0F}, new float[]{8F, 0F, 0F}, new float[]{4F, -4F, 2F},
                villagerHead(false),
                villagerTorso(),
                g -> child(g, "p0", cube().texOffs(44, 22)
                                .addBox(0, -2, -2, 4, 8, 4)
                                .addBox(4, 2, -2, 4, 4, 4),
                        PartPose.offset(0, 2, 0)),
                g -> child(g, "p0", cube().texOffs(44, 22)
                                .addBox(0, -2, -2, 4, 8, 4)
                                .addBox(-4, 2, -2, 4, 4, 4),
                        PartPose.offset(0, 2, 0)),
                villagerLegs()));

        // Sorcière (NecroEntityWitch : villageois + chapeau, texture 64x128)
        MOBS.put(mc("witch"), new MobDef(mcTex("witch.png"), 64, 128, 1.0F, Anim.VILLAGER,
                new float[]{-4F, 0F, -2F},
                new float[]{-4F, 0F, 0F}, new float[]{8F, 0F, 0F}, new float[]{4F, -4F, 2F},
                villagerHead(true),
                villagerTorso(),
                g -> child(g, "p0", cube().texOffs(44, 22)
                                .addBox(0, -2, -2, 4, 8, 4)
                                .addBox(4, 2, -2, 4, 4, 4),
                        PartPose.offset(0, 2, 0)),
                g -> child(g, "p0", cube().texOffs(44, 22)
                                .addBox(0, -2, -2, 4, 8, 4)
                                .addBox(-4, 2, -2, 4, 4, 4),
                        PartPose.offset(0, 2, 0)),
                villagerLegs()));

        // Cochon (NecroEntityPig : quadrupède taille 6 + groin)
        MOBS.put(mc("pig"), quadruped(mcTex("pig/temperate_pig.png"), 64, 64, 6,
                g -> {
                    child(g, "p0", cube().texOffs(0, 0).addBox(-4, -4, -4, 8, 8, 8), PartPose.ZERO);
                    child(g, "p1", cube().texOffs(16, 16).addBox(-2, 0, -5, 4, 3, 1), PartPose.ZERO);
                }));

        // Chèvre, sniffer, armadillo : NecroEntityQuadruped (même UV/géométrie que le cochon) + texture entité.
        MOBS.put(mc("goat"), quadruped(
                ResourceLocation.fromNamespaceAndPath("necromency", "textures/entity/goat/goat.png"), 64, 64, 6, null));
        MOBS.put(mc("sniffer"), quadruped(
                ResourceLocation.fromNamespaceAndPath("necromency", "textures/entity/sniffer/sniffer.png"), 192, 192, 8, null));
        MOBS.put(mc("armadillo"), quadruped(mcTex("armadillo.png"), 64, 32, 6, null));

        // Mouton (NecroEntitySheep : tête 6x6x8, torse 8x16x6, ancres basses)
        MOBS.put(mc("sheep"), new MobDef(mcTex("sheep/sheep.png"), 64, 32, 1.0F, Anim.QUADRUPED,
                new float[]{-4F, 4F, 0F},
                new float[]{-1F, 6F, -10F}, new float[]{5F, 6F, -10F}, new float[]{4F, 0F, -14F},
                g -> child(g, "p0", cube().texOffs(0, 0).addBox(-4, -4, -4, 6, 6, 8), PartPose.ZERO),
                g -> child(g, "p0", cube().texOffs(28, 8).addBox(0, -10, -6, 8, 16, 6), PartPose.ZERO),
                quadArm(true, 12), quadArm(false, 12), quadLegs(12)));

        // Vache (NecroEntityCow : cornes, pis, jambes écartées)
        MOBS.put(mc("cow"), new MobDef(mcTex("cow/temperate_cow.png"), 64, 64, 1.0F, Anim.QUADRUPED,
                new float[]{-4F, -2F, 0F},
                new float[]{-1F, 12F, -10F}, new float[]{5F, 12F, -10F}, new float[]{4F, 4F, -14F},
                g -> child(g, "p0", cube()
                                .texOffs(0, 0).addBox(-4, -4, -4, 8, 8, 6)
                                .texOffs(22, 0).addBox(-5, -5, -4, 1, 3, 1)
                                .texOffs(22, 0).addBox(4, -5, -4, 1, 3, 1),
                        PartPose.ZERO),
                g -> child(g, "p0", cube()
                                .texOffs(18, 4).addBox(-2, -12, -12, 12, 18, 10)
                                .texOffs(52, 0).addBox(2, 2, -13, 4, 6, 1),
                        PartPose.ZERO),
                quadArm(true, 12), quadArm(false, 12),
                g -> {
                    child(g, "p0", cube().texOffs(0, 16).mirror().addBox(-2, 0, -2, 4, 12, 4), PartPose.offset(-4, 10, 2));
                    child(g, "p1", cube().texOffs(0, 16).addBox(-2, 0, -2, 4, 12, 4), PartPose.offset(4, 10, 2));
                }));

        // Poulet (NecroEntityChicken : bec, barbillon, ailes, pattes fines)
        MOBS.put(mc("chicken"), new MobDef(mcTex("chicken/temperate_chicken.png"), 64, 32, 1.0F, Anim.CHICKEN,
                new float[]{-3F, 8F, 0F},
                new float[]{-3F, 6F, 2F}, new float[]{7F, 6F, 2F}, new float[]{4F, 4F, -2F},
                g -> {
                    child(g, "p0", cube().texOffs(0, 0).addBox(-2, -2, -2, 4, 6, 3), PartPose.ZERO);
                    child(g, "p1", cube().texOffs(14, 0).addBox(-2, 0, -4, 4, 2, 2), PartPose.ZERO);
                    child(g, "p2", cube().texOffs(14, 4).addBox(-1, 2, -3, 2, 2, 2), PartPose.ZERO);
                },
                g -> child(g, "p0", cube().texOffs(0, 9).addBox(1, -2, -12, 6, 8, 6), PartPose.ZERO),
                g -> child(g, "p0", cube().texOffs(24, 13).addBox(3, 0, -3, 1, 4, 6), PartPose.ZERO),
                g -> child(g, "p0", cube().texOffs(24, 13).addBox(0, 0, -3, 1, 4, 6), PartPose.ZERO),
                g -> {
                    child(g, "p0", cube().texOffs(26, 0).addBox(0.5F, -1, -1, 3, 5, 3), PartPose.offset(0, 19, 0));
                    child(g, "p1", cube().texOffs(26, 0).addBox(-1.5F, -1, -1, 3, 5, 3), PartPose.offset(0, 19, 0));
                }));

        // Golem de fer (NecroEntityIronGolem : 128x128)
        MOBS.put(mc("iron_golem"), new MobDef(mcTex("iron_golem/iron_golem.png"), 128, 128, 1.0F, Anim.BIPED,
                new float[]{-4F, -4F, 0F},
                new float[]{-9F, 0F, 0F}, new float[]{13F, 0F, 0F}, new float[]{8F, -7F, 2F},
                g -> child(g, "p0", cube()
                                .texOffs(0, 0).addBox(-4, -6, -5.5F, 8, 10, 8)
                                .texOffs(24, 0).addBox(-1, 1, -7.5F, 2, 4, 2),
                        PartPose.offset(0, 0, -2)),
                g -> child(g, "p0", cube()
                                .texOffs(0, 40).addBox(-5, 4, -6, 18, 12, 11)
                                .texOffs(0, 70).addBox(-0.5F, 16, -3, 9, 5, 6, new CubeDeformation(0.5F)),
                        PartPose.offset(0, -7, 0)),
                g -> child(g, "p0", cube().texOffs(60, 58).mirror().addBox(0, 2, -3, 4, 30, 6), PartPose.offset(0, -7, 0)),
                g -> child(g, "p0", cube().texOffs(60, 21).addBox(0, 2, -3, 4, 30, 6), PartPose.offset(0, -7, 0)),
                g -> {
                    child(g, "p0", cube().texOffs(37, 0).addBox(-3.5F, -3, -3, 6, 16, 5), PartPose.offset(-4, 11, 0));
                    child(g, "p1", cube().texOffs(60, 0).mirror().addBox(-3.5F, -3, -3, 6, 16, 5), PartPose.offset(5, 11, 0));
                }));

        // Loup (NecroEntityWolf : tête seulement)
        MOBS.put(mc("wolf"), new MobDef(mcTex("wolf/wolf.png"), 64, 32, 1.0F, Anim.BIPED,
                BIPED_TORSO_POS, BIPED_ARM_L_POS, BIPED_ARM_R_POS, BIPED_HEAD_POS,
                g -> child(g, "p0", cube()
                                .texOffs(0, 0).addBox(-2, -2.5F, 2, 6, 6, 4)
                                .texOffs(16, 14).addBox(-2, -4.5F, 4, 2, 2, 1)
                                .texOffs(16, 14).addBox(2, -4.5F, 4, 2, 2, 1)
                                .texOffs(0, 10).addBox(-0.5F, 0.5F, -1, 3, 3, 4),
                        PartPose.offset(-1, 0, -3)),
                null, null, null, null));
    }

    private static Consumer<PartDefinition> villagerHead(boolean witch) {
        return g -> {
            PartDefinition head = g.addOrReplaceChild("p0",
                    cube().texOffs(0, 0).addBox(-4, -6, -4, 8, 10, 8), PartPose.ZERO);
            PartDefinition nose = g.addOrReplaceChild("p1",
                    cube().texOffs(24, 0).addBox(-1, 3, -6, 2, 4, 2), PartPose.offset(0, -2, 0));
            if (witch) {
                nose.addOrReplaceChild("mole",
                        cube().texOffs(0, 0).addBox(0, 7, -6.75F, 1, 1, 1, new CubeDeformation(-0.25F)),
                        PartPose.offset(0, -2, 0));
                PartDefinition hat = head.addOrReplaceChild("hat",
                        cube().texOffs(0, 64).addBox(0, 0, 0, 10, 2, 10),
                        PartPose.offset(-5, -6.03125F, -5));
                PartDefinition hat2 = hat.addOrReplaceChild("hat2",
                        cube().texOffs(0, 76).addBox(0, 0, 0, 7, 4, 7),
                        PartPose.offsetAndRotation(1.75F, -4, 2, -0.05235988F, 0, 0.02617994F));
                PartDefinition hat3 = hat2.addOrReplaceChild("hat3",
                        cube().texOffs(0, 87).addBox(0, 0, 0, 4, 4, 4),
                        PartPose.offsetAndRotation(1.75F, -4, 2, -0.10471976F, 0, 0.05235988F));
                hat3.addOrReplaceChild("hat4",
                        cube().texOffs(0, 95).addBox(0, 0, 0, 1, 2, 1, new CubeDeformation(0.25F)),
                        PartPose.offsetAndRotation(1.75F, -2, 2, -0.20943952F, 0, 0.10471976F));
            }
        };
    }

    private static Consumer<PartDefinition> villagerTorso() {
        return g -> {
            child(g, "p0", cube().texOffs(16, 20).addBox(0, 0, -1, 8, 12, 6), PartPose.ZERO);
            child(g, "p1", cube().texOffs(0, 38).addBox(0, 0, -1, 8, 18, 6, new CubeDeformation(0.5F)), PartPose.ZERO);
        };
    }

    private static Consumer<PartDefinition> villagerLegs() {
        return g -> {
            child(g, "p0", cube().texOffs(0, 22).addBox(-2, 0, -2, 4, 12, 4), PartPose.offset(-2, 12, 0));
            child(g, "p1", cube().texOffs(0, 22).mirror().addBox(-2, 0, -2, 4, 12, 4), PartPose.offset(2, 12, 0));
        };
    }
}
