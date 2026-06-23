package com.mickdev.necromency.Client.render;

import com.mickdev.necromency.Client.Models.NecroEntityBaseModel;
import com.mickdev.necromency.Client.Models.NecromencyChicken;
import com.mickdev.necromency.Client.NecromencyModelLayers;
import com.mickdev.necromency.Client.Util.MinionRenderState;
import com.mickdev.necromency.Client.Util.MixedVerticalAlign;
import com.mickdev.necromency.Client.Util.MobTextureResolver;
import com.mickdev.necromency.Client.Util.PartSlot;
import com.mickdev.necromency.Client.legacy.LegacyMinionModel;
import com.mickdev.necromency.Client.legacy.LegacyNecroParts;
import com.mickdev.necromency.entity.MinionEntity;
import com.mickdev.necromency.necroapi.MinionSaddleables;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * Bipède Necro, atlas zombie+poulet (hybride), ou mesh poulet Necromency quand toutes les parties sont poulet.
 */
public class MinionRenderer extends MobRenderer<MinionEntity, MinionRenderState, NecroEntityBaseModel> {

    private static final ResourceLocation RL_CHICKEN = ResourceLocation.parse("minecraft:chicken");
    private static final ResourceLocation RL_VILLAGER = ResourceLocation.parse("minecraft:villager");

    /** Quadrupèdes legacy (même famille que le cochon). Utilisé en secours si le lookup MOBS échoue. */
    private static final java.util.Set<String> QUADRUPED_MOB_PATHS = java.util.Set.of(
            "pig", "cow", "sheep", "goat", "sniffer", "armadillo");

    /**
     * Mobs au corps horizontal rendu via le vrai modèle vanilla {@code BODY_FIT} : la tête (d'un mob
     * legacy : zombie par défaut, squelette…) doit se poser à l'<b>avant</b> du corps comme le cochon,
     * pas empilée en position humanoïde. Le warden (debout) est volontairement exclu.
     */
    private static final java.util.Set<String> HEAD_FRONT_BODY_PATHS = java.util.Set.of(
            "goat", "sniffer", "armadillo", "fox", "cat", "axolotl", "turtle", "wolf");

    private final NecroEntityBaseModel standardModel;
    private final NecroEntityBaseModel atlasModel;
    private final NecromencyChicken chickenModel;
    /** Un modèle par slot : le collector 1.21.10 lit la visibilité au rendu, pas à la soumission. */
    private final NecroEntityBaseModel headPassModel;
    private final NecroEntityBaseModel villagerHeadPassModel;
    private final NecroEntityBaseModel bodyPassModel;
    private final NecroEntityBaseModel armLPassModel;
    private final NecroEntityBaseModel armRPassModel;
    private final NecroEntityBaseModel legsPassModel;
    /** Géométrie poulet (UV atlas 128×64) pour les passes mixtes corps/ailes/jambes. */
    private final NecroEntityBaseModel atlasBodyPassModel;
    private final NecroEntityBaseModel atlasArmLPassModel;
    private final NecroEntityBaseModel atlasArmRPassModel;
    private final NecroEntityBaseModel atlasLegsPassModel;

    /** Modèles vanilla officiels (mobs ajoutés après la 1.12.2), bakés à la demande. */
    private final EntityModelSet modelSet;
    private final Map<ResourceLocation, VanillaMobModel> modernModels = new HashMap<>();
    /** Un morceau (slot) issu d'un modèle vanilla, pour les minions mixtes (ex. tête de piglin). */
    private final Map<String, VanillaSlotModel> modernSlotModels = new HashMap<>();

    @SuppressWarnings("unchecked")
    public MinionRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new NecroEntityBaseModel(ctx.bakeLayer(NecromencyModelLayers.MINION)), 0.5F);
        this.modelSet = ctx.getModelSet();
        this.standardModel = (NecroEntityBaseModel) this.getModel();
        this.atlasModel = new NecroEntityBaseModel(ctx.bakeLayer(NecromencyModelLayers.MINION_ATLAS));
        this.chickenModel = new NecromencyChicken(ctx.bakeLayer(NecromencyModelLayers.MINION_CHICKEN));
        this.headPassModel = new NecroEntityBaseModel(ctx.bakeLayer(NecromencyModelLayers.MINION));
        this.villagerHeadPassModel = new NecroEntityBaseModel(ctx.bakeLayer(NecromencyModelLayers.MINION_VILLAGER_HEAD));
        this.bodyPassModel = new NecroEntityBaseModel(ctx.bakeLayer(NecromencyModelLayers.MINION));
        this.armLPassModel = new NecroEntityBaseModel(ctx.bakeLayer(NecromencyModelLayers.MINION));
        this.armRPassModel = new NecroEntityBaseModel(ctx.bakeLayer(NecromencyModelLayers.MINION));
        this.legsPassModel = new NecroEntityBaseModel(ctx.bakeLayer(NecromencyModelLayers.MINION));
        this.atlasBodyPassModel = new NecroEntityBaseModel(ctx.bakeLayer(NecromencyModelLayers.MINION_ATLAS));
        this.atlasArmLPassModel = new NecroEntityBaseModel(ctx.bakeLayer(NecromencyModelLayers.MINION_ATLAS));
        this.atlasArmRPassModel = new NecroEntityBaseModel(ctx.bakeLayer(NecromencyModelLayers.MINION_ATLAS));
        this.atlasLegsPassModel = new NecroEntityBaseModel(ctx.bakeLayer(NecromencyModelLayers.MINION_ATLAS));
        setVisibility(headPassModel, true, false, false, false, false);
        setVisibility(villagerHeadPassModel, true, false, false, false, false);
        setVisibility(bodyPassModel, false, true, false, false, false);
        setVisibility(armLPassModel, false, false, true, false, false);
        setVisibility(armRPassModel, false, false, false, true, false);
        setVisibility(legsPassModel, false, false, false, false, true);
        setVisibility(atlasBodyPassModel, false, true, false, false, false);
        setVisibility(atlasArmLPassModel, false, false, true, false, false);
        setVisibility(atlasArmRPassModel, false, false, false, true, false);
        setVisibility(atlasLegsPassModel, false, false, false, false, true);
        setVisibility(atlasModel, false, true, true, true, true);
    }

    @Override
    public MinionRenderState createRenderState() {
        return new MinionRenderState();
    }

    @Override
    public void extractRenderState(MinionEntity entity, MinionRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        HumanoidMobRenderer.extractHumanoidRenderState(entity, state, partialTick, this.itemModelResolver);

        state.headType = LegacyNecroParts.normalize(entity.getHeadId());
        state.bodyType = LegacyNecroParts.normalize(entity.getBodyId());
        state.armLType = LegacyNecroParts.normalize(entity.getArmLId());
        state.armRType = LegacyNecroParts.normalize(entity.getArmRId());
        state.legsType = LegacyNecroParts.normalize(entity.getLegsId());
        state.setDefaultZombieIfNull();

        if (isAllChicken(state) || usesChickenAtlas(state)) {
            state.partPoseAnchor = RL_CHICKEN;
            state.skipChickenLimbProfiles = true;
        } else if (RL_CHICKEN.equals(state.armLType) && RL_CHICKEN.equals(state.armRType) && RL_CHICKEN.equals(state.legsType)) {
            state.partPoseAnchor = RL_CHICKEN;
            state.skipChickenLimbProfiles = true;
        } else {
            state.partPoseAnchor = null;
            state.skipChickenLimbProfiles = false;
        }

        state.isUpsideDown = false;
        state.saddled = entity.isSaddled();
        state.saddleTexture = state.saddled ? MinionSaddleables.saddleTexture(entity.getBodyId()) : null;
    }

    @Override
    public ResourceLocation getTextureLocation(MinionRenderState state) {
        if (isAllChicken(state)) {
            return MobTextureResolver.resolveChickenEntityTextureForRendering();
        }
        if (usesChickenAtlas(state)) {
            MinionCompositeAtlas.ensureBuilt();
            return MinionCompositeAtlas.isReady() ? MinionCompositeAtlas.LOCATION : MobTextureResolver.MISSING;
        }
        return resolveUnifiedTexture(state);
    }

    @Override
    public void submit(MinionRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        submitInternal(state, poseStack, collector, cameraState, false);
    }

    /** Aperçu autel : ALTARRenderer a déjà scale(-1,-1,1), éviter le second flip entité. */
    public void submitForAltarPreview(MinionRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                                      CameraRenderState cameraState) {
        submitInternal(state, poseStack, collector, cameraState, true);
    }

    private void submitInternal(MinionRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                                CameraRenderState cameraState, boolean altarPreview) {
        if (altarPreview) {
            state.walkAnimationSpeed = 0.0F;
            state.walkAnimationPos = 0.0F;
            state.yRot = 0.0F;
            state.xRot = 0.0F;
        }

        poseStack.pushPose();

        float scale = state.scale > 1.0E-4F ? state.scale : 1.0F;
        poseStack.scale(scale, scale, scale);
        if (!altarPreview) {
            setupRotations(state, poseStack, state.bodyRot, scale);
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            scale(state, poseStack);
        }
        poseStack.translate(0.0F, -1.501F, 0.0F);

        int light = state.lightCoords;
        int overlay = getOverlayCoords(state, getWhiteOverlayProgress(state));
        int outline = state.outlineColor;

        if (isAllSameModernMob(state)) {
            // Mob ajouté après la 1.12.2, minion uniforme : on rend le modèle vanilla officiel.
            submitModernMob(state, poseStack, collector, light, overlay, outline);
        } else {
            // Sniffer/chèvre/armadillo/cochon : corps quadrupède legacy en priorité (même tête seule + zombie par défaut).
            ResourceLocation quadAssembly = resolveQuadrupedAssemblyMob(state);
            if (quadAssembly != null) {
                submitLegacyCompositeUniform(state, poseStack, collector, light, overlay, outline, quadAssembly);
            } else if (isFullyLegacy(state)) {
                submitLegacyComposite(state, poseStack, collector, light, overlay, outline);
            } else if (isAllChicken(state)) {
                submitChickenModel(state, poseStack, collector, light, overlay, outline, altarPreview, true);
            } else if (usesChickenAtlas(state)) {
                MinionCompositeAtlas.ensureBuilt();
                submitChickenAtlasComposite(state, poseStack, collector, light, overlay, outline);
            } else if (isQuadrupedLegacy(state.bodyType) && !prefersVanillaBody(state.bodyType)) {
                // Corps quadrupède legacy (cochon/vache/mouton) : assemblage par ancres 1.7.10 comme
                // ModelMinion.render. La tête/les bras se placent à l'AVANT du corps (ancre headPos/armPos
                // du torse), jamais empilés en position humanoïde au-dessus.
                submitLegacyComposite(state, poseStack, collector, light, overlay, outline);
            } else if (hasQuadrupedLegacyPart(state)) {
                // Secours : au moins un slot chèvre/sniffer/armadillo/cochon… → legacy, jamais humanoïde.
                submitMixedParts(state, poseStack, collector, light, overlay, outline);
            } else if (hasMixedParts(state)) {
                submitMixedParts(state, poseStack, collector, light, overlay, outline);
            } else {
                ResourceLocation texture = resolveUnifiedTexture(state);
                standardModel.setupAnim(state);
                standardModel.hat.visible = false;
                collector.submitModel(
                        standardModel,
                        state,
                        poseStack,
                        RenderType.entityCutoutNoCull(texture),
                        light,
                        overlay,
                        outline,
                        null
                );
                submitSaddleIfNeeded(state, poseStack, collector, light, overlay, outline);
            }
        }

        poseStack.popPose();
    }

    /** Minion 100 % d'un même mob « moderne » (post-1.12.2) qui possède un modèle vanilla réutilisable. */
    private static boolean isAllSameModernMob(MinionRenderState state) {
        ResourceLocation b = state.bodyType;
        if (!ModernMinionMobs.contains(b)) {
            return false;
        }
        return b.equals(state.headType)
                && b.equals(state.armLType)
                && b.equals(state.armRType)
                && b.equals(state.legsType);
    }

    @org.jetbrains.annotations.Nullable
    private VanillaMobModel getModernModel(ResourceLocation mobId) {
        if (modernModels.containsKey(mobId)) {
            return modernModels.get(mobId);
        }
        ModelLayerLocation layer = ModernMinionMobs.layer(mobId);
        VanillaMobModel model = null;
        if (layer != null) {
            try {
                model = new VanillaMobModel(modelSet.bakeLayer(layer));
            } catch (RuntimeException ignored) {
                // Layer absent du jeu : on retombe sur le rendu humanoïde générique.
            }
        }
        modernModels.put(mobId, model);
        return model;
    }

    @org.jetbrains.annotations.Nullable
    private VanillaSlotModel getModernSlotModel(ResourceLocation mobId, PartSlot slot) {
        String key = mobId + "|" + slot;
        if (modernSlotModels.containsKey(key)) {
            return modernSlotModels.get(key);
        }
        ModelLayerLocation layer = ModernMinionMobs.layer(mobId);
        VanillaSlotModel model = null;
        if (layer != null) {
            try {
                VanillaSlotModel candidate = new VanillaSlotModel(modelSet.bakeLayer(layer), slot, mobId);
                if (candidate.hasParts()) {
                    model = candidate;
                }
            } catch (RuntimeException ignored) {
                // Layer absent ou morceau introuvable -> on garde le rendu humanoïde générique.
            }
        }
        modernSlotModels.put(key, model);
        return model;
    }

    /** Rend un morceau via le vrai modèle vanilla du mob. @return false si indisponible (fallback bipède). */
    private boolean submitModernSlot(MinionRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                                     int light, int overlay, int outline, ResourceLocation mobId, PartSlot slot) {
        if (!ModernMinionMobs.contains(mobId)) {
            return false;
        }
        VanillaSlotModel model = getModernSlotModel(mobId, slot);
        if (model == null) {
            return false;
        }
        model.setupAnim(state);
        // Modèle vanilla baké -> texture vanilla pure (les UV officiels n'ont de sens qu'avec l'atlas officiel).
        ResourceLocation texture = MobTextureResolver.vanillaTextureForMobId(mobId);
        // Pas d'échelle externe ici : VanillaSlotModel dimensionne déjà chaque morceau en repère bipède
        // (le ghast, baké très grand, est ramené en interne à la taille du slot).
        collector.submitModel(
                model,
                state,
                copyPose(poseStack),
                RenderType.entityCutoutNoCull(texture),
                light,
                overlay,
                outline,
                null
        );
        return true;
    }

    private void submitModernMob(MinionRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                                 int light, int overlay, int outline) {
        VanillaMobModel model = getModernModel(state.bodyType);
        if (model == null) {
            // Sécurité : modèle indisponible -> ancien rendu plaqué sur bipède.
            ResourceLocation texture = resolveUnifiedTexture(state);
            standardModel.setupAnim(state);
            standardModel.hat.visible = false;
            collector.submitModel(standardModel, state, poseStack,
                    RenderType.entityCutoutNoCull(texture), light, overlay, outline, null);
            return;
        }
        model.setupAnim(state);
        ResourceLocation texture = MobTextureResolver.textureForMobId(state.bodyType);

        // Certains modèles vanilla sont bakés avec un facteur d'échelle énorme (ghast 4.5x,
        // happy ghast 4.0x) et un corps flottant : on les ramène à la taille d'un minion.
        PoseStack pose = poseStack;
        float fix = modernRenderScale(state.bodyType);
        boolean scaled = Math.abs(fix - 1.0F) > 1e-4F;
        if (scaled) {
            pose = copyPose(poseStack);
            pose.pushPose();
            pose.scale(fix, fix, fix);
        }

        collector.submitModel(
                model,
                state,
                pose,
                RenderType.entityCutoutNoCull(texture),
                light,
                overlay,
                outline,
                null
        );

        if (scaled) {
            pose.popPose();
        }
    }

    /** Échelle correctrice pour les modèles vanilla bakés très grands (ghasts). 1.0 = aucun ajustement. */
    private static float modernRenderScale(ResourceLocation mobId) {
        if (mobId == null) {
            return 1.0F;
        }
        return switch (mobId.getPath()) {
            case "ghast" -> 0.22F;
            case "happy_ghast" -> 0.24F;
            default -> 1.0F;
        };
    }

    /**
     * Minion 5/5 identique avec géométrie 1.7.10 (cochon, sniffer, zombie…).
     * Ne doit pas être vrai pour un mix sniffer + zombie : {@code submitLegacyComposite} ancre
     * le torse sur {@code legsType} et dessine {@code bodyType} tel quel.
     */
    private static boolean isFullyLegacy(MinionRenderState state) {
        ResourceLocation h = state.headType;
        ResourceLocation b = state.bodyType;
        ResourceLocation al = state.armLType;
        ResourceLocation ar = state.armRType;
        ResourceLocation lg = state.legsType;
        if (h == null || b == null || al == null || ar == null || lg == null) {
            return false;
        }
        if (!h.equals(b) || !b.equals(al) || !al.equals(ar) || !ar.equals(lg)) {
            return false;
        }
        return LegacyNecroParts.supports(h);
    }

    private static boolean isUniformQuadrupedLegacy(MinionRenderState state) {
        ResourceLocation b = state.bodyType;
        if (!isQuadrupedLegacy(b)) {
            return false;
        }
        return b.equals(state.headType) && b.equals(state.armLType) && b.equals(state.armRType) && b.equals(state.legsType);
    }

    private static boolean isQuadrupedLegacy(@org.jetbrains.annotations.Nullable ResourceLocation mobId) {
        mobId = LegacyNecroParts.normalize(mobId);
        if (mobId == null) {
            return false;
        }
        if (QUADRUPED_MOB_PATHS.contains(mobId.getPath())) {
            return true;
        }
        LegacyNecroParts.MobDef def = LegacyNecroParts.get(mobId);
        return def != null && def.anim() == LegacyNecroParts.Anim.QUADRUPED;
    }

    /**
     * Mobs dont le slot BODY se rend avec le <b>vrai modèle vanilla</b> (corps horizontal réel +
     * texture officielle) plutôt que la géométrie legacy cochon : chèvre, sniffer, armadillo.
     * Demande utilisateur : « comme le modèle du cochon mais avec leur propre modèle ».
     */
    private static boolean prefersVanillaBody(@org.jetbrains.annotations.Nullable ResourceLocation mobId) {
        mobId = LegacyNecroParts.normalize(mobId);
        if (mobId == null) {
            return false;
        }
        String path = mobId.getPath();
        return ("goat".equals(path) || "sniffer".equals(path) || "armadillo".equals(path))
                && ModernMinionMobs.contains(mobId);
    }

    /** Au moins un slot cochon/chèvre/sniffer/armadillo/vache/mouton. */
    private static boolean hasQuadrupedLegacyPart(MinionRenderState state) {
        return isQuadrupedLegacy(state.headType)
                || isQuadrupedLegacy(state.bodyType)
                || isQuadrupedLegacy(state.armLType)
                || isQuadrupedLegacy(state.armRType)
                || isQuadrupedLegacy(state.legsType);
    }

    /**
     * Pattes avant / arrière d'un quadrupède moderne (armadillo, sniffer, chèvre) sur un corps
     * <b>humanoïde</b> : vraies pattes vanilla replacées (LIMB_FIT / LEG_FIT). Sur un corps
     * quadrupède, on garde l'assemblage legacy d'origine.
     */
    private static boolean useVanillaQuadrupedLimbsOnHumanoidBody(MinionRenderState state,
                                                                  ResourceLocation mobId, PartSlot slot) {
        if (slot != PartSlot.ARM_L && slot != PartSlot.ARM_R && slot != PartSlot.LEGS) {
            return false;
        }
        mobId = LegacyNecroParts.normalize(mobId);
        if (mobId == null || !ModernMinionMobs.contains(mobId)) {
            return false;
        }
        String path = mobId.getPath();
        if (!"armadillo".equals(path) && !"sniffer".equals(path) && !"goat".equals(path)) {
            return false;
        }
        ResourceLocation body = LegacyNecroParts.normalize(state.bodyType);
        if (body == null) {
            return false;
        }
        // Corps quadrupède (cochon, armadillo, vache…) → rendu original du mob, pas des pattes replacées.
        return !isQuadrupedLegacy(body) && !prefersVanillaBody(body);
    }

    /**
     * Rendu legacy quadrupède (identique cochon) : {@link LegacyMinionModel} + texture entité.
     * @return {@code true} si le slot a été traité (rendu ou volontairement absent)
     */
    private boolean submitLegacyQuadrupedSlot(MinionRenderState state, PoseStack legacyPose,
                                              SubmitNodeCollector collector, int light, int overlay, int outline,
                                              ResourceLocation mobId, PartSlot slot) {
        if (!isQuadrupedLegacy(mobId)) {
            return false;
        }
        if (useVanillaQuadrupedLimbsOnHumanoidBody(state, mobId, slot)) {
            return false;
        }
        if (submitLegacyMixedSlot(state, legacyPose, collector, light, overlay, outline, mobId, slot)) {
            return true;
        }
        LegacyNecroParts.MobDef def = LegacyNecroParts.get(mobId);
        return def != null && def.builder(slot) == null;
    }

    /**
     * Assemblage quadrupède 5/5 forcé uniquement si :
     * <ul>
     *   <li>les 5 morceaux sont le même quadrupède (sniffer, cochon…), ou</li>
     *   <li>seule la <b>tête</b> est quadrupède et le reste est zombie par défaut (autel incomplet).</li>
     * </ul>
     * Un mix explicite (tête zombie + corps armadillo + bras/jambes zombie) reste en {@link #submitMixedParts}.
     */
    @org.jetbrains.annotations.Nullable
    private static ResourceLocation resolveQuadrupedAssemblyMob(MinionRenderState state) {
        ResourceLocation head = state.headType;
        ResourceLocation body = state.bodyType;
        ResourceLocation armL = state.armLType;
        ResourceLocation armR = state.armRType;
        ResourceLocation legs = state.legsType;

        if (isQuadrupedLegacy(head)
                && head.equals(body) && body.equals(armL) && armL.equals(armR) && armR.equals(legs)) {
            return head;
        }

        if (isQuadrupedLegacy(head)
                && !isQuadrupedLegacy(body)
                && !isQuadrupedLegacy(armL)
                && !isQuadrupedLegacy(armR)
                && !isQuadrupedLegacy(legs)
                && isZombiePart(body)
                && isZombiePart(armL)
                && isZombiePart(armR)
                && isZombiePart(legs)) {
            return head;
        }

        return null;
    }

    private static boolean isZombiePart(@org.jetbrains.annotations.Nullable ResourceLocation id) {
        return id != null && MobTextureResolver.ZOMBIE_ID.equals(id);
    }

    /**
     * Reproduction de {@code ModelMinion} 1.7.10 : jambes à l'origine, torse translaté de
     * {@code torsoPos} (jambes), bras/tête translatés en plus de {@code armPos}/{@code headPos}
     * (torse), texture liée par morceau.
     */
    private void submitLegacyComposite(MinionRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                                       int light, int overlay, int outline) {
        LegacyNecroParts.MobDef legsDef = LegacyNecroParts.get(state.legsType);
        LegacyNecroParts.MobDef torsoDef = LegacyNecroParts.get(state.bodyType);
        float[] torsoPos = legsDef != null ? legsDef.torsoPos() : LegacyNecroParts.BIPED_TORSO_POS;
        float[] armLPos = torsoDef != null ? torsoDef.armLeftPos() : LegacyNecroParts.BIPED_ARM_L_POS;
        float[] armRPos = torsoDef != null ? torsoDef.armRightPos() : LegacyNecroParts.BIPED_ARM_R_POS;
        float[] headPos = torsoDef != null ? torsoDef.headPos() : LegacyNecroParts.BIPED_HEAD_POS;

        submitLegacySlot(state, poseStack, collector, light, overlay, outline, PartSlot.LEGS, state.legsType, null, null);
        submitLegacySlot(state, poseStack, collector, light, overlay, outline, PartSlot.BODY, state.bodyType, torsoPos, null);
        submitLegacySlot(state, poseStack, collector, light, overlay, outline, PartSlot.ARM_L, state.armLType, torsoPos, armLPos);
        submitLegacySlot(state, poseStack, collector, light, overlay, outline, PartSlot.ARM_R, state.armRType, torsoPos, armRPos);
        submitLegacySlot(state, poseStack, collector, light, overlay, outline, PartSlot.HEAD, state.headType, torsoPos, headPos);

        // ISaddleAble 1.7.10 : le torse est retracé avec la texture de selle
        if (state.saddled && state.saddleTexture != null && torsoDef != null
                && MinionSaddleables.torsoAcceptsSaddle(state.bodyType)) {
            PoseStack pose = legacyPose(poseStack, torsoPos, null, torsoDef.scale());
            submitLegacyMinionModel(LegacyMinionModel.get(PartSlot.BODY, state.bodyType), state, pose, collector,
                    state.saddleTexture, light, overlay, outline);
        }
    }

    /** Assemblage 5/5 d'un même quadrupède legacy (slots vides comptés zombie côté gameplay). */
    private void submitLegacyCompositeUniform(MinionRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                                              int light, int overlay, int outline, ResourceLocation mob) {
        LegacyNecroParts.MobDef def = LegacyNecroParts.get(mob);
        if (def == null) {
            return;
        }
        float[] torsoPos = def.torsoPos();
        float[] armLPos = def.armLeftPos();
        float[] armRPos = def.armRightPos();
        float[] headPos = def.headPos();

        submitLegacySlot(state, poseStack, collector, light, overlay, outline, PartSlot.LEGS, mob, null, null);
        submitLegacySlot(state, poseStack, collector, light, overlay, outline, PartSlot.BODY, mob, torsoPos, null);
        submitLegacySlot(state, poseStack, collector, light, overlay, outline, PartSlot.ARM_L, mob, torsoPos, armLPos);
        submitLegacySlot(state, poseStack, collector, light, overlay, outline, PartSlot.ARM_R, mob, torsoPos, armRPos);
        submitLegacySlot(state, poseStack, collector, light, overlay, outline, PartSlot.HEAD, mob, torsoPos, headPos);

        if (state.saddled && state.saddleTexture != null && MinionSaddleables.torsoAcceptsSaddle(mob)) {
            PoseStack pose = legacyPose(poseStack, torsoPos, null, def.scale());
            submitLegacyMinionModel(LegacyMinionModel.get(PartSlot.BODY, mob), state, pose, collector,
                    state.saddleTexture, light, overlay, outline);
        }
    }

    private void submitLegacySlot(MinionRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                                  int light, int overlay, int outline, PartSlot slot, ResourceLocation mobId,
                                  float[] basePos, float[] extraPos) {
        if (mobId == null) {
            return;
        }
        // Corps chèvre/sniffer/armadillo : vrai modèle vanilla (corps horizontal réel + texture officielle).
        if (slot == PartSlot.BODY && prefersVanillaBody(mobId)
                && submitModernSlot(state, poseStack, collector, light, overlay, outline, mobId, slot)) {
            return;
        }
        LegacyNecroParts.MobDef def = LegacyNecroParts.get(mobId);
        if (def == null) {
            if (submitLegacyQuadrupedSlot(state, poseStack, collector, light, overlay, outline, mobId, slot)) {
                return;
            }
            // Morceau sans géométrie legacy (mob moderne : piglin, renard…) : garder son vrai modèle vanilla
            // plutôt qu'une passe humanoïde mal texturée.
            if (submitModernSlot(state, poseStack, collector, light, overlay, outline, mobId, slot)) {
                return;
            }
            submitPartPass(state, poseStack, collector, light, overlay, outline, mobId, slot);
            return;
        }
        if (def.builder(slot) == null) {
            return; // ex. bras de creeper/araignée : le mob n'a pas ce morceau
        }
        PoseStack pose = legacyPose(poseStack, basePos, extraPos, def.scale());
        submitLegacyMinionModel(LegacyMinionModel.get(slot, mobId), state, pose, collector,
                LegacyNecroParts.textureForSlot(slot, mobId), light, overlay, outline);
    }

    /** Soumission legacy (obligatoire cast comme le mesh poulet — sinon le collector 1.21 ignore le modèle). */
    @SuppressWarnings("unchecked")
    private static void submitLegacyMinionModel(LegacyMinionModel model, MinionRenderState state, PoseStack pose,
                                                SubmitNodeCollector collector, ResourceLocation texture,
                                                int light, int overlay, int outline) {
        model.setupAnim(state);
        collector.submitModel(
                (EntityModel<EntityRenderState>) (Object) model,
                (EntityRenderState) state,
                pose,
                RenderType.entityCutoutNoCull(texture),
                light,
                overlay,
                outline,
                null
        );
    }

    /**
     * Rend un morceau legacy <b>dans un minion mixte</b> avec sa géométrie 1.7.10 fidèle.
     *
     * <p>Indispensable pour les mobs dont la texture n'est pas 64×64 (creeper/squelette/enderman
     * en 64×32) : la passe humanoïde générique est bakée en 64×64, donc plaquer une texture 64×32
     * dessus divise les coordonnées V par deux et échantillonne la mauvaise zone (le visage du
     * creeper se retrouvait sur le torse, et ses bras inexistants tombaient en texture manquante).</p>
     *
     * @return {@code true} si le morceau a été pris en charge (rendu ou volontairement ignoré),
     *         {@code false} pour laisser la passe humanoïde générique s'en occuper.
     */
    private boolean submitLegacyMixedSlot(MinionRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                                          int light, int overlay, int outline, ResourceLocation mobId, PartSlot slot) {
        // Corps chèvre/sniffer/armadillo : vrai modèle vanilla (corps horizontal réel + texture officielle).
        if (slot == PartSlot.BODY && prefersVanillaBody(mobId)
                && submitModernSlot(state, poseStack, collector, light, overlay, outline, mobId, slot)) {
            return true;
        }
        LegacyNecroParts.MobDef def = LegacyNecroParts.get(mobId);
        if (def == null || !legacyFitsMixed(def)) {
            return false;
        }

        // Quadrupèdes (cochon, chèvre, sniffer, armadillo…) : ancres propres au mob du slot,
        // comme en assemblage 100 % legacy. Les bipèdes mixtes gardent le repère jambes/torse.
        float[] torsoPos;
        float[] armLPos;
        float[] armRPos;
        float[] headPos;
        if (def.anim() == LegacyNecroParts.Anim.QUADRUPED) {
            torsoPos = slot == PartSlot.LEGS ? null : def.torsoPos();
            armLPos = def.armLeftPos();
            armRPos = def.armRightPos();
            headPos = def.headPos();
            // Corps quadrupède seul dans un mix (ex. torse armadillo + tête zombie) : ancrer le torse
            // sur les jambes bipèdes du minion, pas en plein air.
            if (slot == PartSlot.BODY && !isQuadrupedLegacy(state.legsType)) {
                LegacyNecroParts.MobDef legsDef = LegacyNecroParts.get(state.legsType);
                float[] legTorso = legsDef != null ? legsDef.torsoPos() : LegacyNecroParts.BIPED_TORSO_POS;
                torsoPos = new float[]{def.torsoPos()[0], legTorso[1], def.torsoPos()[2]};
            }
        } else if (def.anim() == LegacyNecroParts.Anim.SPIDER) {
            // Araignée : pattes/tête/torse aux ancres 1.7.10 propres (8 pattes horizontales, pas jambes bipèdes).
            torsoPos = slot == PartSlot.LEGS ? null : def.torsoPos();
            armLPos = def.armLeftPos();
            armRPos = def.armRightPos();
            headPos = def.headPos();
        } else {
            LegacyNecroParts.MobDef legsDef = LegacyNecroParts.get(state.legsType);
            LegacyNecroParts.MobDef torsoDef = LegacyNecroParts.get(state.bodyType);
            torsoPos = legsDef != null ? legsDef.torsoPos() : LegacyNecroParts.BIPED_TORSO_POS;
            armLPos = torsoDef != null ? torsoDef.armLeftPos() : LegacyNecroParts.BIPED_ARM_L_POS;
            armRPos = torsoDef != null ? torsoDef.armRightPos() : LegacyNecroParts.BIPED_ARM_R_POS;
            headPos = torsoDef != null ? torsoDef.headPos() : LegacyNecroParts.BIPED_HEAD_POS;
        }

        float[] basePos;
        float[] extraPos;
        switch (slot) {
            case LEGS -> { basePos = null; extraPos = null; }
            case BODY -> { basePos = torsoPos; extraPos = null; }
            case ARM_L -> { basePos = torsoPos; extraPos = armLPos; }
            case ARM_R -> { basePos = torsoPos; extraPos = armRPos; }
            case HEAD -> { basePos = torsoPos; extraPos = headPos; }
            default -> { return false; }
        }

        if (def.builder(slot) == null) {
            // Le mob n'a pas ce morceau (ex. bras du creeper) : comme en 100 % legacy, on ne dessine rien
            // au lieu de retomber sur un bras humanoïde mal texturé.
            return true;
        }

        PoseStack pose = legacyPose(poseStack, basePos, extraPos, def.scale());
        submitLegacyMinionModel(LegacyMinionModel.get(slot, mobId), state, pose, collector,
                LegacyNecroParts.textureForSlot(slot, mobId), light, overlay, outline);
        return true;
    }

    /**
     * Géométrie 1.7.10 utilisable dans un minion mixte (torse vertical 64×32, ou quadrupède
     * cochon/chèvre/sniffer/armadillo/vache…).
     */
    private static boolean legacyFitsMixed(LegacyNecroParts.MobDef def) {
        return switch (def.anim()) {
            case QUADRUPED, SPIDER -> true;
            case BIPED, ENDERMAN, VILLAGER, CREEPER -> def.texW() != 64 || def.texH() != 64;
            default -> false;
        };
    }

    private static PoseStack legacyPose(PoseStack source, float[] basePos, float[] extraPos, float scale) {
        PoseStack pose = copyPose(source);
        float tx = 0, ty = 0, tz = 0;
        if (basePos != null) {
            tx += basePos[0];
            ty += basePos[1];
            tz += basePos[2];
        }
        if (extraPos != null) {
            tx += extraPos[0];
            ty += extraPos[1];
            tz += extraPos[2];
        }
        if (tx != 0 || ty != 0 || tz != 0) {
            pose.translate(tx / 16.0F, ty / 16.0F, tz / 16.0F);
        }
        if (scale != 1.0F) {
            pose.scale(scale, scale, scale);
        }
        return pose;
    }

    /** 5/5 morceaux minecraft:chicken → modèle + texture 100 % poulet. */
    private static boolean isAllChicken(MinionRenderState state) {
        return RL_CHICKEN.equals(state.headType)
                && RL_CHICKEN.equals(state.bodyType)
                && RL_CHICKEN.equals(state.armLType)
                && RL_CHICKEN.equals(state.armRType)
                && RL_CHICKEN.equals(state.legsType);
    }

    private static boolean usesChickenAtlas(MinionRenderState state) {
        if (isAllChicken(state)) {
            return false;
        }
        return RL_CHICKEN.equals(state.bodyType)
                && RL_CHICKEN.equals(state.armLType)
                && RL_CHICKEN.equals(state.armRType)
                && RL_CHICKEN.equals(state.legsType);
    }

    private static ResourceLocation resolveUnifiedTexture(MinionRenderState state) {
        ResourceLocation h = state.headType;
        ResourceLocation b = state.bodyType;
        ResourceLocation al = state.armLType;
        ResourceLocation ar = state.armRType;
        ResourceLocation lg = state.legsType;
        if (h != null && h.equals(b) && b != null && b.equals(al) && al != null && al.equals(ar) && ar != null && ar.equals(lg)) {
            return MobTextureResolver.textureForMobId(h);
        }
        ResourceLocation primary = b != null ? b : MobTextureResolver.ZOMBIE_ID;
        return MobTextureResolver.textureForMobId(primary);
    }

    private static boolean hasMixedParts(MinionRenderState state) {
        if (isAllChicken(state) || usesChickenAtlas(state)) {
            return false;
        }
        ResourceLocation h = state.headType;
        ResourceLocation b = state.bodyType;
        ResourceLocation al = state.armLType;
        ResourceLocation ar = state.armRType;
        ResourceLocation lg = state.legsType;
        if (h == null || b == null || al == null || ar == null || lg == null) {
            return false;
        }
        return !(h.equals(b) && b.equals(al) && al.equals(ar) && ar.equals(lg));
    }

    /**
     * Tête autre mob + corps/ailes/jambes poulet : squelette humanoïde (cou y=0) + UV atlas poulet.
     * Le mesh Blockbench retourné à 180° est réservé au minion 100 % poulet.
     */
    private void submitChickenAtlasComposite(MinionRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                                             int light, int overlay, int outline) {
        if (isChicken(state.headType)) {
            submitChickenHeadPass(state, poseStack, collector, light, overlay, outline);
        } else {
            submitPartPass(state, poseStack, collector, light, overlay, outline, state.headType, PartSlot.HEAD);
        }
        submitAtlasChickenLimbsPass(state, poseStack, collector, light, overlay, outline);
        submitSaddleIfNeeded(state, poseStack, collector, light, overlay, outline);
    }

    private void submitMixedParts(MinionRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                                  int light, int overlay, int outline) {
        // Quand le corps est un mob legacy au torse vertical (creeper…), sa géométrie 1.7.10 se place
        // selon le repère d'origine, dont le torse est plus bas que le torse humanoïde. La tête et les
        // bras issus d'AUTRES mobs (positionnés en repère humanoïde) doivent donc descendre d'autant
        // pour se poser sur le torse, sinon ils flottent au-dessus / sur les côtés.
        float shiftY = legacyMixedBodyYShift(state);
        PoseStack foreignPose = shiftY != 0.0F ? shiftPoseY(poseStack, shiftY) : poseStack;

        // Jambes d'abord (ancrage), puis torse, bras, tête — comme ModelMinion 1.7.10.
        submitMixedSlot(state, poseStack, poseStack, collector, light, overlay, outline, state.legsType, PartSlot.LEGS);
        submitMixedSlot(state, poseStack, poseStack, collector, light, overlay, outline, state.bodyType, PartSlot.BODY);
        submitMixedSlot(state, poseStack, foreignPose, collector, light, overlay, outline, state.armLType, PartSlot.ARM_L);
        submitMixedSlot(state, poseStack, foreignPose, collector, light, overlay, outline, state.armRType, PartSlot.ARM_R);

        // Corps vanilla horizontal (sniffer/armadillo/chèvre/renard/chat/axolotl/tortue/loup) : poser la
        // tête à l'AVANT du corps comme le cochon, pas empilée en position humanoïde au-dessus.
        if (bodyWantsHeadAtFront(state.bodyType)
                && submitHeadAtVanillaBodyFront(state, poseStack, collector, light, overlay, outline)) {
            // tête posée à l'avant du corps
        } else if (isChicken(state.headType)) {
            submitChickenHeadPass(state, foreignPose, collector, light, overlay, outline);
        } else if (!submitLegacyQuadrupedSlot(state, poseStack, collector, light, overlay, outline, state.headType, PartSlot.HEAD)
                && !submitModernSlot(state, foreignPose, collector, light, overlay, outline, state.headType, PartSlot.HEAD)
                && !submitLegacyMixedSlot(state, poseStack, collector, light, overlay, outline, state.headType, PartSlot.HEAD)) {
            submitPartPass(state, foreignPose, collector, light, overlay, outline, state.headType, PartSlot.HEAD);
        }
        submitSaddleIfNeeded(state, poseStack, collector, light, overlay, outline);
    }

    /**
     * Ancre (px, repère bipède) où poser le <b>centre</b> d'une tête pour qu'elle tombe à l'avant
     * d'un corps vanilla {@code BODY_FIT} (sniffer/armadillo/chèvre). Calculée depuis la boîte réelle
     * du torse ajusté, donc adaptée à chaque mob (le sniffer long, l'armadillo court…).
     *
     * @return {@code null} si le corps n'est pas un {@code BODY_FIT} exploitable.
     */
    /**
     * {@code true} si le corps est un modèle vanilla horizontal ({@code BODY_FIT}) sur lequel la tête
     * doit se poser à l'avant (sniffer/armadillo/chèvre/renard/chat/axolotl/tortue/loup).
     */
    private boolean bodyWantsHeadAtFront(@org.jetbrains.annotations.Nullable ResourceLocation bodyType) {
        ResourceLocation norm = LegacyNecroParts.normalize(bodyType);
        if (norm == null || !HEAD_FRONT_BODY_PATHS.contains(norm.getPath())) {
            return false;
        }
        VanillaSlotModel body = getModernSlotModel(norm, PartSlot.BODY);
        return body != null && body.isBodyFit();
    }

    @org.jetbrains.annotations.Nullable
    private float[] vanillaBodyHeadAnchor(ResourceLocation bodyType) {
        if (!bodyWantsHeadAtFront(bodyType)) {
            return null;
        }
        VanillaSlotModel body = getModernSlotModel(LegacyNecroParts.normalize(bodyType), PartSlot.BODY);
        if (body == null || !body.isBodyFit()) {
            return null;
        }
        float frontZ = body.bodyFrontZ(); // négatif = vers l'avant
        float topY = body.bodyTopY();      // plus petit = plus haut
        // Tête (boîte centrée à l'origine, demi-taille 4 px) : centre posé juste devant le torse,
        // vers le haut, en chevauchant légèrement l'avant pour rester soudée au corps.
        return new float[]{0.0F, topY + 3.0F, frontZ - 2.0F};
    }

    /**
     * Pose la tête à l'avant d'un corps vanilla (sniffer/armadillo/chèvre) avec sa géométrie legacy
     * (tête centrée à l'origine, donc placée exactement à l'ancre). Identique au cochon, sans empilage
     * humanoïde au-dessus.
     *
     * @return {@code true} si la tête a été dessinée, {@code false} pour laisser le flux normal gérer
     *         (tête sans géométrie legacy, ex. mob purement moderne).
     */
    private boolean submitHeadAtVanillaBodyFront(MinionRenderState state, PoseStack poseStack,
                                                 SubmitNodeCollector collector, int light, int overlay, int outline) {
        float[] anchor = vanillaBodyHeadAnchor(state.bodyType);
        if (anchor == null) {
            return false;
        }
        ResourceLocation headType = state.headType;
        LegacyNecroParts.MobDef def = LegacyNecroParts.get(headType);
        if (def != null && def.builder(PartSlot.HEAD) != null) {
            PoseStack pose = legacyPose(poseStack, anchor, null, def.scale());
            submitLegacyMinionModel(LegacyMinionModel.get(PartSlot.HEAD, headType), state, pose, collector,
                    LegacyNecroParts.textureForSlot(PartSlot.HEAD, headType), light, overlay, outline);
            return true;
        }
        // Tête vanilla re-rootée (renard, chat, axolotl, tortue) : on la recentre pile à l'ancre avant.
        VanillaSlotModel headModel = getModernSlotModel(headType, PartSlot.HEAD);
        if (headModel != null && headModel.isHeadFit()) {
            headModel.setHeadAnchor(anchor);
            headModel.setupAnim(state);
            collector.submitModel(
                    headModel,
                    state,
                    copyPose(poseStack),
                    RenderType.entityCutoutNoCull(MobTextureResolver.vanillaTextureForMobId(headType)),
                    light,
                    overlay,
                    outline,
                    null
            );
            headModel.setHeadAnchor(null);
            return true;
        }
        return false;
    }

    /**
     * Rend un morceau d'un minion mixte.
     *
     * @param legacyPose  repère pour la géométrie legacy (ancres 1.7.10 d'origine)
     * @param foreignPose repère pour les morceaux « étrangers » (modernes/humanoïdes), éventuellement
     *                    décalé pour s'aligner sur un torse legacy
     */
    private void submitMixedSlot(MinionRenderState state, PoseStack legacyPose, PoseStack foreignPose,
                                 SubmitNodeCollector collector, int light, int overlay, int outline,
                                 ResourceLocation mobId, PartSlot slot) {
        if (mobId == null) {
            return;
        }
        if (isChicken(mobId) && slot != PartSlot.HEAD) {
            if (allChickenLimbs(state)) {
                if (slot == PartSlot.BODY) {
                    submitAtlasChickenLimbsPass(state, foreignPose, collector, light, overlay, outline);
                }
                return;
            }
            submitPartPass(state, foreignPose, collector, light, overlay, outline, mobId, slot);
            return;
        }
        if (submitLegacyQuadrupedSlot(state, legacyPose, collector, light, overlay, outline, mobId, slot)) {
            return;
        }
        if (submitModernSlot(state, foreignPose, collector, light, overlay, outline, mobId, slot)) {
            return;
        }
        if (submitLegacyMixedSlot(state, legacyPose, collector, light, overlay, outline, mobId, slot)) {
            return;
        }
        if (isQuadrupedLegacy(mobId)) {
            return;
        }
        submitPartPass(state, foreignPose, collector, light, overlay, outline, mobId, slot);
    }

    /**
     * Décalage vertical (repère modèle, en unités monde) à appliquer aux morceaux étrangers d'un
     * minion mixte quand le corps est un mob legacy au torse vertical. Vaut 0 sinon.
     */
    private static float legacyMixedBodyYShift(MinionRenderState state) {
        // Corps vanilla BODY_FIT (chèvre/sniffer/armadillo) : centré sur le torse bipède standard,
        // donc tête/bras étrangers gardent leur position bipède normale (aucun décalage).
        if (prefersVanillaBody(state.bodyType)) {
            return 0.0F;
        }
        LegacyNecroParts.MobDef bodyDef = LegacyNecroParts.get(state.bodyType);
        if (bodyDef == null || !legacyFitsMixed(bodyDef)) {
            return 0.0F;
        }
        if (bodyDef.anim() == LegacyNecroParts.Anim.QUADRUPED) {
            float torsoY = bodyDef.torsoPos()[1];
            if (!isQuadrupedLegacy(state.legsType)) {
                LegacyNecroParts.MobDef legsDef = LegacyNecroParts.get(state.legsType);
                float[] legTorso = legsDef != null ? legsDef.torsoPos() : LegacyNecroParts.BIPED_TORSO_POS;
                torsoY = legTorso[1];
            }
            // Tête/bras bipèdes : aligner sur le dos du quadrupède (repère zombie y=-2).
            return (torsoY - LegacyNecroParts.BIPED_TORSO_POS[1]) / 16.0F;
        }
        LegacyNecroParts.MobDef legsDef = LegacyNecroParts.get(state.legsType);
        float[] torsoPos = legsDef != null ? legsDef.torsoPos() : LegacyNecroParts.BIPED_TORSO_POS;
        return torsoPos[1] / 16.0F;
    }

    private static PoseStack shiftPoseY(PoseStack source, float dy) {
        PoseStack pose = copyPose(source);
        pose.translate(0.0F, dy, 0.0F);
        return pose;
    }

    private static boolean allChickenLimbs(MinionRenderState state) {
        return isChicken(state.bodyType)
                && isChicken(state.armLType)
                && isChicken(state.armRType)
                && isChicken(state.legsType);
    }

    /** Corps + ailes + jambes poulet sur le squelette humanoïde (même repère que la tête villageois/zombie). */
    private void submitAtlasChickenLimbsPass(MinionRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                                             int light, int overlay, int outline) {
        MinionCompositeAtlas.ensureBuilt();
        atlasModel.setupAnimForMixedPass(state, null, true);
        atlasModel.hat.visible = false;
        ResourceLocation atlasTex = MinionCompositeAtlas.isReady()
                ? MinionCompositeAtlas.LOCATION
                : MobTextureResolver.MISSING;
        PoseStack atlasPose = copyPose(poseStack);
        collector.submitModel(
                atlasModel,
                state,
                atlasPose,
                RenderType.entityCutoutNoCull(atlasTex),
                light,
                overlay,
                outline,
                null
        );
    }

    private static PoseStack copyPose(PoseStack source) {
        PoseStack copy = new PoseStack();
        copy.last().set(source.last());
        return copy;
    }

    private static void applyChickenFlip(PoseStack poseStack) {
        poseStack.translate(0.0F, NecromencyChicken.FLIP_180X_Y_LIFT, 0.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
    }

    /** Tête poulet : mesh Blockbench + texture poulet (pas l'atlas zombie|poulet sur mesh humanoïde). */
    @SuppressWarnings("unchecked")
    private void submitChickenHeadPass(MinionRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                                       int light, int overlay, int outline) {
        PoseStack chickenPose = copyPose(poseStack);
        chickenPose.translate(0.0F, (MixedVerticalAlign.neckY(state) - MixedVerticalAlign.CHICKEN_MODEL_HEAD_Y) / 16.0F, 0.0F);
        chickenModel.showOnly(NecromencyChicken.BodySlot.HEAD);
        chickenModel.setupAnim(state);
        collector.submitModel(
                (EntityModel<EntityRenderState>) (Object) chickenModel,
                (EntityRenderState) state,
                chickenPose,
                RenderType.entityCutoutNoCull(MobTextureResolver.resolveChickenEntityTextureForRendering()),
                light,
                overlay,
                outline,
                null
        );
    }

    @SuppressWarnings("unchecked")
    private void submitChickenModel(MinionRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                                    int light, int overlay, int outline, boolean altarPreview, boolean allParts) {
        PoseStack chickenPose = copyPose(poseStack);
        if (!altarPreview) {
            applyChickenFlip(chickenPose);
        }
        ResourceLocation texture = MobTextureResolver.resolveChickenEntityTextureForRendering();
        if (allParts) {
            chickenModel.setAllPartsVisible();
        }
        chickenModel.setupAnim(state);
        collector.submitModel(
                (EntityModel<EntityRenderState>) (Object) chickenModel,
                (EntityRenderState) state,
                chickenPose,
                RenderType.entityCutoutNoCull(texture),
                light,
                overlay,
                outline,
                null
        );
    }

    private void submitPartPass(MinionRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                                int light, int overlay, int outline, ResourceLocation mobId, PartSlot slot) {
        if (mobId == null) {
            return;
        }
        // Dernier filet : cochon/chèvre/sniffer/armadillo → legacy quadrupède, sauf pattes vanilla sur corps humanoïde.
        if (isQuadrupedLegacy(mobId)) {
            if (submitLegacyQuadrupedSlot(state, poseStack, collector, light, overlay, outline, mobId, slot)) {
                return;
            }
            if (submitModernSlot(state, poseStack, collector, light, overlay, outline, mobId, slot)) {
                return;
            }
            return;
        }
        boolean atlasMesh = MixedVerticalAlign.useAtlasMeshFor(state, slot, mobId);
        NecroEntityBaseModel model = selectPassModel(state, mobId, slot, atlasMesh);
        submitPartPass(state, poseStack, collector, light, overlay, outline, model, mobId, slot, atlasMesh);
    }

    private NecroEntityBaseModel selectPassModel(MinionRenderState state, ResourceLocation mobId, PartSlot slot, boolean atlasMesh) {
        if (!atlasMesh) {
            return switch (slot) {
                case HEAD -> isVillagerHead(mobId) ? villagerHeadPassModel : headPassModel;
                case BODY -> bodyPassModel;
                case ARM_L -> armLPassModel;
                case ARM_R -> armRPassModel;
                case LEGS -> legsPassModel;
            };
        }
        return switch (slot) {
            case BODY -> atlasBodyPassModel;
            case ARM_L -> atlasArmLPassModel;
            case ARM_R -> atlasArmRPassModel;
            case LEGS -> atlasLegsPassModel;
            default -> headPassModel;
        };
    }

    private static boolean isChicken(ResourceLocation mobId) {
        return RL_CHICKEN.equals(mobId);
    }

    private static ResourceLocation textureForPass(ResourceLocation mobId, boolean atlasChickenMesh) {
        if (isChicken(mobId) && atlasChickenMesh) {
            MinionCompositeAtlas.ensureBuilt();
            return MinionCompositeAtlas.isReady() ? MinionCompositeAtlas.LOCATION : MobTextureResolver.MISSING;
        }
        return MobTextureResolver.textureForMobId(mobId);
    }

    private void submitPartPass(MinionRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                                int light, int overlay, int outline, NecroEntityBaseModel model, ResourceLocation mobId,
                                PartSlot slot, boolean atlasChickenMesh) {
        if (mobId == null) {
            return;
        }
        if (isQuadrupedLegacy(mobId)) {
            if (submitLegacyQuadrupedSlot(state, poseStack, collector, light, overlay, outline, mobId, slot)) {
                return;
            }
            if (submitModernSlot(state, poseStack, collector, light, overlay, outline, mobId, slot)) {
                return;
            }
            return;
        }
        model.setupAnimForMixedPass(state, slot, atlasChickenMesh);
        model.hat.visible = false;
        ResourceLocation tex = textureForPass(mobId, atlasChickenMesh);
        PoseStack partPose = copyPose(poseStack);
        collector.submitModel(
                model,
                state,
                partPose,
                RenderType.entityCutoutNoCull(tex),
                light,
                overlay,
                outline,
                null
        );
    }

    private static boolean isVillagerHead(ResourceLocation headType) {
        return RL_VILLAGER.equals(headType);
    }

    private static void setVisibility(NecroEntityBaseModel model, boolean head, boolean body, boolean armL, boolean armR, boolean legs) {
        model.head.visible = head;
        model.body.visible = body;
        model.leftArm.visible = armL;
        model.rightArm.visible = armR;
        model.leftLeg.visible = legs;
        model.rightLeg.visible = legs;
        model.hat.visible = false;
    }

    private void submitSaddleIfNeeded(MinionRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                                      int light, int overlay, int outline) {
        if (!state.saddled || state.saddleTexture == null) return;
        boolean atlasMesh = MixedVerticalAlign.useAtlasMeshFor(state, PartSlot.BODY, state.bodyType);
        NecroEntityBaseModel saddleModel = selectPassModel(state, state.bodyType, PartSlot.BODY, atlasMesh);
        saddleModel.setupAnimForMixedPass(state, PartSlot.BODY, atlasMesh);
        collector.submitModel(
                saddleModel,
                state,
                poseStack,
                RenderType.entityCutoutNoCull(state.saddleTexture),
                light,
                overlay,
                outline,
                null
        );
    }
}
