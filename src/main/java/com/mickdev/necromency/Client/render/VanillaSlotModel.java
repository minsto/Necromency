package com.mickdev.necromency.Client.render;

import com.mickdev.necromency.Client.Util.MinionRenderState;
import com.mickdev.necromency.Client.Util.PartSlot;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Affiche UN seul morceau (tête, torse, bras, jambes) à partir du vrai modèle vanilla d'un mob
 * (ex. tête de piglin avec ses oreilles).
 *
 * <p>Trois stratégies selon le mob :</p>
 * <ul>
 *   <li><b>STANDARD</b> (humanoïdes type piglin) : on masque les autres morceaux via
 *       {@link ModelPart#skipDraw} ; chaque partie reste à sa position native, qui coïncide avec
 *       le squelette bipède.</li>
 *   <li><b>HEAD_FIT</b> (quadrupèdes/aquatiques : tortue, renard, axolotl, chat, sniffer, warden) :
 *       leur partie {@code head} est placée à l'emplacement natif du mob (bas/avant) et parfois
 *       démesurée. On « re-root » sur la tête (supprime les transforms parentes) puis on la
 *       recentre et la redimensionne à l'ancre bipède.</li>
 *   <li><b>GHAST</b> : pas de tête ni de membres, juste un cube-face « body » baké très grand. On
 *       le réutilise, redimensionné et placé à l'ancre du slot.</li>
 * </ul>
 *
 * <p>Important : {@link EntityModel#setupAnim} réinitialise les poses chaque frame, donc tous les
 * réglages géométriques sont (ré)appliqués dans {@link #setupAnim} après l'appel parent. Seul le
 * masquage {@code skipDraw} (non réinitialisé) est figé à la construction.</p>
 */
public final class VanillaSlotModel extends EntityModel<MinionRenderState> {

    private static final float DEG = (float) Math.PI / 180.0F;

    /** Hauteur (px) où accrocher le haut du bras-patte, proche de l'épaule bipède. */
    private static final float SHOULDER_TOP_Y = 1.0F;

    private enum Mode { STANDARD, GHAST, HEAD_FIT, LIMB_FIT, BODY_FIT, LEG_FIT }

    /**
     * Mobs dont le « body » natif n'est pas humanoïde (warden géant, carapace de tortue à plat…).
     * Leur torse doit être re-rooté puis redimensionné/recentré pour tenir dans le torse du minion.
     * Les vrais humanoïdes (piglin, pillager, illusioner, trader) ne sont PAS listés : leur « body »
     * Les quadrupèdes (cochon, chèvre, sniffer, armadillo…) passent par {@link com.mickdev.necromency.Client.legacy.LegacyNecroParts}.
     */
    private static final java.util.Set<String> BODY_FIT_MOBS = java.util.Set.of(
            "turtle", "fox", "cat", "axolotl", "warden", "wolf",
            // Quadrupèdes : corps horizontal réel (comme le cochon) + texture vanilla, plutôt que la
            // géométrie legacy cochon plaquée d'une texture custom mal alignée.
            "goat", "sniffer", "armadillo");

    /**
     * Mobs dont le slot BODY utilise le torse humanoïde texturé (cochon/vache/chèvre/sniffer/armadillo
     * sont gérés par {@link com.mickdev.necromency.Client.legacy.LegacyNecroParts} quadrupède).
     */
    private static final java.util.Set<String> BODY_VANILLA_FALLBACK = java.util.Set.of();

    /**
     * Mobs dont le slot LEGS utilise jambes bipèdes texturées plutôt que pattes natives replacées.
     */
    private static final java.util.Set<String> LEGS_VANILLA_FALLBACK = java.util.Set.of(
            "fox", "cat", "axolotl", "warden",
            "ghast", "happy_ghast");

    /**
     * Réglage d'une vraie patte arrière utilisée comme jambe.
     * @param scale    échelle uniforme (proportions natives préservées)
     * @param standUp  {@code true} pour une nageoire plate (tortue) : redressée verticalement.
     */
    private record LegFit(float scale, boolean standUp) {}

    /**
     * Mobs rendus avec leurs <b>vraies pattes vanilla</b> replacées aux ancres bipèdes.
     */
    private static final Map<String, LegFit> LEG_FITS = Map.of(
            "turtle", new LegFit(1.0F, true),
            "armadillo", new LegFit(4.0F, false),
            "sniffer", new LegFit(0.65F, false),
            "goat", new LegFit(1.1F, false)
    );

    /** Recentrage/redimensionnement d'une tête de mob non humanoïde vers l'ancre bipède (px). */
    private record HeadFit(float scale, float x, float y) {}

    /**
     * Réglage d'une patte avant de quadrupède utilisée comme bras (échelle, décalage X/Y px).
     * {@code scale <= 0} = auto-échelle : la patte est ajustée à ~11 px de long par boîte englobante
     * (utile pour les nageoires plates type tortue, trop petites avec une échelle fixe).
     */
    private record LimbFit(float scale, float x, float y) {}

    /**
     * Réglages par mob pour les bras (= patte avant native). Le pivot de la patte est ramené à
     * l'ancre d'épaule bipède (±5, 2, 0) ; l'échelle ajuste sa longueur à celle d'un bras. Calés
     * à l'œil, à affiner selon le rendu. Un mob absent de la table (ou sans {@code *_front_leg})
     * retombe sur la passe humanoïde texturée.
     */
    private static final Map<String, LimbFit> LIMB_FITS = Map.of(
            "fox", new LimbFit(2.00F, 0.0F, 0.0F),
            "cat", new LimbFit(1.90F, 0.0F, 0.0F),
            "turtle", new LimbFit(0.0F, 0.0F, 0.0F), // 0 = auto (nageoire plate)
            "axolotl", new LimbFit(1.40F, 0.0F, 0.0F),
            "armadillo", new LimbFit(0.0F, 0.0F, 0.0F),
            "sniffer", new LimbFit(0.0F, 0.0F, 0.0F),
            "goat", new LimbFit(0.0F, 0.0F, 0.0F)
    );

    /**
     * Réglages par mob (échelle, décalage X, décalage Y du pivot tête). Calés sur la géométrie
     * vanilla pour que la tête tombe sur les épaules en regardant vers l'avant. Le repère est
     * celui de la tête bipède (pivot à 0, cube au-dessus -> Y négatif = vers le haut).
     */
    private static final Map<String, HeadFit> HEAD_FITS = Map.of(
            "turtle", new HeadFit(1.40F, 0.0F, -6.1F),
            "fox", new HeadFit(1.17F, -1.17F, -5.17F),
            "axolotl", new HeadFit(1.40F, 0.0F, -3.3F),
            "cat", new HeadFit(1.50F, 0.0F, -4.0F),
            "warden", new HeadFit(0.50F, 0.0F, 0.0F)
    );

    /** Tentacules du ghast / happy ghast (toujours masqués en mode morceau). */
    private static final List<String> GHAST_TENTACLES = List.of(
            "tentacle0", "tentacle1", "tentacle2", "tentacle3", "tentacle4",
            "tentacle5", "tentacle6", "tentacle7", "tentacle8");

    /** Membres/tête susceptibles d'être imbriqués sous "body" (copper golem, axolotl…) : à re-masquer pour le slot BODY. */
    private static final List<String> LIMB_AND_HEAD_PARTS = List.of(
            "head",
            "tail",
            "left_arm", "right_arm",
            "left_leg", "right_leg",
            "left_front_leg", "right_front_leg",
            "left_hind_leg", "right_hind_leg",
            "left_mid_leg", "right_mid_leg");

    private final Mode mode;
    @Nullable
    private final ModelPart head;       // cible de la rotation du regard
    @Nullable
    private final ModelPart ghastBody;
    @Nullable
    private final HeadFit headFit;
    @Nullable
    private final LimbFit limbFit;
    /** Centre horizontal cible du cube-bras (repère bipède : +6 = bras gauche, -6 = bras droit). */
    private final float limbCenterX;
    private final float ghastScale;
    private final float ghastX;
    private final float ghastY;
    /** Transform auto-calculée du corps non humanoïde (mode BODY_FIT). */
    private float bodyFitScale;
    private float bodyFitX;
    private float bodyFitY;
    private float bodyFitZ;
    /** Avant (z mini, vers l'avant) et haut (y mini) du torse une fois ajusté — pour ancrer une tête. */
    private boolean bodyFitComputed;
    private float bodyFitFrontZ;
    private float bodyFitTopY;
    /** Vraies pattes arrière utilisées comme jambes (mode LEG_FIT). */
    @Nullable
    private ModelPart legLeft;
    @Nullable
    private ModelPart legRight;
    private float legFitScale;
    private boolean legStandUp;
    private boolean hasParts;
    /** Ancre (px, repère bipède) où poser le centre de la tête en mode HEAD_FIT (pour un corps horizontal). */
    @Nullable
    private float[] headAnchorPx;

    public VanillaSlotModel(ModelPart bakedRoot, PartSlot slot, ResourceLocation mobId) {
        super(chooseRoot(bakedRoot, slot, mobId));

        String path = mobId != null ? mobId.getPath() : "";

        // Slots laissés au tato de base (humanoïde texturé) : LEGS des mobs non humanoïdes/ghast,
        // et BODY des mobs où l'on n'échange que la tête (armadillo). hasParts=false -> l'appelant
        // retombe sur la passe humanoïde.
        boolean legsFallback = slot == PartSlot.LEGS && LEGS_VANILLA_FALLBACK.contains(path);
        boolean bodyFallback = slot == PartSlot.BODY && BODY_VANILLA_FALLBACK.contains(path);
        if (legsFallback || bodyFallback) {
            this.mode = Mode.STANDARD;
            this.headFit = null;
            this.limbFit = null;
            this.limbCenterX = 0.0F;
            this.head = null;
            this.ghastBody = null;
            this.ghastScale = 0.0F;
            this.ghastX = 0.0F;
            this.ghastY = 0.0F;
            this.hasParts = false;
            return;
        }

        HeadFit fit = slot == PartSlot.HEAD ? HEAD_FITS.get(path) : null;

        // HEAD_FIT : on a re-rooté sur la partie "head" -> on affiche tout son sous-arbre.
        if (fit != null && this.root != bakedRoot) {
            this.mode = Mode.HEAD_FIT;
            this.headFit = fit;
            this.limbFit = null;
            this.limbCenterX = 0.0F;
            this.head = this.root;
            this.ghastBody = null;
            this.ghastScale = 0.0F;
            this.ghastX = 0.0F;
            this.ghastY = 0.0F;
            for (ModelPart part : this.root.getAllParts()) {
                part.visible = true;
                part.skipDraw = false;
            }
            this.hasParts = true;
            return;
        }

        // LIMB_FIT : on a re-rooté sur une patte avant -> on l'affiche en entier, placée à l'épaule.
        LimbFit limb = (slot == PartSlot.ARM_L || slot == PartSlot.ARM_R) ? LIMB_FITS.get(path) : null;
        if (limb != null && this.root != bakedRoot) {
            this.mode = Mode.LIMB_FIT;
            this.headFit = null;
            this.limbFit = limb;
            this.limbCenterX = slot == PartSlot.ARM_L ? 6.0F : -6.0F;
            this.head = null;
            this.ghastBody = null;
            this.ghastScale = 0.0F;
            this.ghastX = 0.0F;
            this.ghastY = 0.0F;
            for (ModelPart part : this.root.getAllParts()) {
                part.visible = true;
                part.skipDraw = false;
            }
            this.hasParts = true;
            return;
        }

        // BODY_FIT : corps de mob non humanoïde (warden, tortue, armadillo, sniffer…). On a re-rooté
        // sur "body" -> on l'affiche entier, redimensionné et recentré pour tenir dans le torse.
        boolean bodyFit = slot == PartSlot.BODY && BODY_FIT_MOBS.contains(path) && this.root != bakedRoot;
        if (bodyFit) {
            this.mode = Mode.BODY_FIT;
            this.headFit = null;
            this.limbFit = null;
            this.limbCenterX = 0.0F;
            this.head = null;
            this.ghastBody = null;
            this.ghastScale = 0.0F;
            this.ghastX = 0.0F;
            this.ghastY = 0.0F;
            for (ModelPart part : this.root.getAllParts()) {
                part.visible = true;
                part.skipDraw = false;
            }
            // Le "body" englobe parfois tête/membres (warden, sniffer) : on ne garde que le torse.
            Function<String, ModelPart> bodyLookup = this.root.createPartLookup();
            for (String name : LIMB_AND_HEAD_PARTS) {
                ModelPart nested = bodyLookup.apply(name);
                if (nested != null && nested != this.root) {
                    setSubtreeDrawn(nested, false);
                }
            }
            float[] b = localCubeBounds(this.root); // cubes propres du torse uniquement
            float w = Math.max(b[3] - b[0], 1.0F);
            float h = Math.max(b[4] - b[1], 1.0F);
            // Ajuste le torse en largeur (≤9 px) et hauteur (≤12 px). On ne borne PAS la profondeur :
            // les corps allongés (sniffer, tortue…) gardent leur largeur réelle et dépassent
            // simplement devant/derrière, au lieu d'être rétrécis en un torse trop étroit.
            float s = Math.min(12.0F / h, 9.0F / w);
            float cx = (b[0] + b[3]) * 0.5F;
            float cy = (b[1] + b[4]) * 0.5F;
            float cz = (b[2] + b[5]) * 0.5F;
            this.bodyFitScale = s;
            this.bodyFitX = -cx * s;
            this.bodyFitY = 6.0F - cy * s; // centre du torse bipède (y 0..12)
            this.bodyFitZ = -cz * s;
            // Avant (z mini) et haut (y mini) du torse une fois recentré : pour poser une tête à l'avant.
            this.bodyFitFrontZ = (b[2] - cz) * s;
            this.bodyFitTopY = 6.0F + (b[1] - cy) * s;
            this.bodyFitComputed = true;
            this.hasParts = true;
            return;
        }

        // LEG_FIT : vraies pattes arrière (tortue, sniffer) replacées aux ancres bipèdes, échelle
        // uniforme (pas de déformation), pieds au sol.
        LegFit legFit = slot == PartSlot.LEGS ? LEG_FITS.get(path) : null;
        if (legFit != null) {
            Function<String, ModelPart> legLookup = bakedRoot.createPartLookup();
            ModelPart ll = firstPart(legLookup, "left_hind_leg", "left_back_leg", "left_leg");
            ModelPart rl = firstPart(legLookup, "right_hind_leg", "right_back_leg", "right_leg");
            if (ll != null && rl != null) {
                this.mode = Mode.LEG_FIT;
                this.headFit = null;
                this.limbFit = null;
                this.limbCenterX = 0.0F;
                this.head = null;
                this.ghastBody = null;
                this.ghastScale = 0.0F;
                this.ghastX = 0.0F;
                this.ghastY = 0.0F;
                for (ModelPart part : bakedRoot.getAllParts()) {
                    part.visible = true;
                    part.skipDraw = true;
                }
                setSubtreeDrawn(ll, true);
                setSubtreeDrawn(rl, true);
                this.legLeft = ll;
                this.legRight = rl;
                this.legFitScale = legFit.scale();
                this.legStandUp = legFit.standUp();
                this.hasParts = true;
                return;
            }
        }

        for (ModelPart part : bakedRoot.getAllParts()) {
            part.visible = true;
            part.skipDraw = true;
        }
        Function<String, ModelPart> lookup = bakedRoot.createPartLookup();

        if (isGhastLike(lookup)) {
            this.mode = Mode.GHAST;
            this.headFit = null;
            this.limbFit = null;
            this.limbCenterX = 0.0F;
            this.head = null;
            ModelPart body = lookup.apply("body");
            this.ghastBody = body;
            for (String tentacle : GHAST_TENTACLES) {
                ModelPart part = lookup.apply(tentacle);
                if (part != null) {
                    part.skipDraw = true;
                }
            }
            if (body != null) {
                body.skipDraw = false;
                this.hasParts = true;
            }
            float size;
            float x = 0.0F;
            float y;
            switch (slot) {
                case HEAD -> { size = 10.0F; y = -4.0F; }
                case BODY -> { size = 12.0F; y = 6.0F; }
                case ARM_L -> { size = 5.0F; x = 5.0F; y = 5.0F; }
                case ARM_R -> { size = 5.0F; x = -5.0F; y = 5.0F; }
                // LEGS du ghast : géré en amont par le fallback humanoïde (jamais atteint ici).
                default -> { size = 5.0F; y = 18.0F; }
            }
            this.ghastScale = size / 72.0F;
            this.ghastX = x;
            this.ghastY = y;
            return;
        }

        this.mode = Mode.STANDARD;
        this.headFit = null;
        this.limbFit = null;
        this.limbCenterX = 0.0F;
        this.ghastBody = null;
        this.ghastScale = 0.0F;
        this.ghastX = 0.0F;
        this.ghastY = 0.0F;
        for (String name : namesFor(slot)) {
            ModelPart part = lookup.apply(name);
            if (part != null) {
                setSubtreeDrawn(part, true);
                this.hasParts = true;
            }
        }
        // "body" englobe parfois tête/membres imbriqués (copper golem, axolotl) : on les remasque.
        if (slot == PartSlot.BODY) {
            for (String name : LIMB_AND_HEAD_PARTS) {
                ModelPart part = lookup.apply(name);
                if (part != null) {
                    setSubtreeDrawn(part, false);
                }
            }
        }
        this.head = slot == PartSlot.HEAD ? lookup.apply("head") : null;
    }

    /** Re-root sur la tête ou la patte avant des mobs non humanoïdes (supprime les transforms parents). */
    private static ModelPart chooseRoot(ModelPart bakedRoot, PartSlot slot, ResourceLocation mobId) {
        if (mobId == null) {
            return bakedRoot;
        }
        String path = mobId.getPath();
        if (slot == PartSlot.HEAD && HEAD_FITS.containsKey(path)) {
            ModelPart head = bakedRoot.createPartLookup().apply("head");
            if (head != null) {
                return head;
            }
        }
        if ((slot == PartSlot.ARM_L || slot == PartSlot.ARM_R) && LIMB_FITS.containsKey(path)) {
            String legName = slot == PartSlot.ARM_L ? "left_front_leg" : "right_front_leg";
            ModelPart leg = bakedRoot.createPartLookup().apply(legName);
            if (leg != null) {
                return leg;
            }
        }
        if (slot == PartSlot.BODY && BODY_FIT_MOBS.contains(path)) {
            ModelPart body = bakedRoot.createPartLookup().apply("body");
            if (body != null) {
                return body;
            }
        }
        return bakedRoot;
    }

    /** false : ce mob n'expose pas ce morceau (slot) -> l'appelant garde le rendu humanoïde générique. */
    public boolean hasParts() {
        return hasParts;
    }

    /** true si ce slot est un corps BODY_FIT dont l'avant/haut sont calculés (pour ancrer une tête). */
    public boolean isBodyFit() {
        return bodyFitComputed;
    }

    /** true si ce slot est une tête vanilla re-rootée (HEAD_FIT), relocalisable à l'avant d'un corps. */
    public boolean isHeadFit() {
        return mode == Mode.HEAD_FIT;
    }

    /**
     * Force (ou annule avec {@code null}) le placement du <b>centre</b> de la tête HEAD_FIT à une ancre
     * donnée (px, repère bipède), pour la poser à l'avant d'un corps horizontal. Sans effet hors HEAD_FIT.
     */
    public void setHeadAnchor(@Nullable float[] anchorPx) {
        this.headAnchorPx = anchorPx;
    }

    /** Z (px, négatif = vers l'avant) du bord avant du torse BODY_FIT recentré. */
    public float bodyFrontZ() {
        return bodyFitFrontZ;
    }

    /** Y (px, repère bipède : plus petit = plus haut) du bord supérieur du torse BODY_FIT recentré. */
    public float bodyTopY() {
        return bodyFitTopY;
    }

    /** Modèle « ghast » : pas de partie head mais un body + des tentacules. */
    private static boolean isGhastLike(Function<String, ModelPart> lookup) {
        return lookup.apply("head") == null
                && lookup.apply("body") != null
                && lookup.apply("tentacle0") != null;
    }

    /**
     * Place une vraie patte arrière à l'ancre bipède : échelle uniforme {@link #legFitScale}
     * (proportions natives préservées), recentrée en X sur {@code anchorX} et en Z sur 0, pied posé
     * au sol (bas du cube à y=24, comme les jambes du minion). {@code swing} = rotation de marche.
     */
    private void placeLeg(ModelPart leg, float anchorX, float swing) {
        float[] b = localCubeBounds(leg); // minX,minY,minZ,maxX,maxY,maxZ
        float s = legFitScale;
        float centerX = (b[0] + b[3]) * 0.5F;
        leg.xScale = s;
        leg.yScale = s;
        leg.zScale = s;
        leg.yRot = 0.0F;
        leg.zRot = 0.0F;
        if (legStandUp) {
            // Nageoire plate (tortue) : rotation -90° autour de X -> la profondeur Z (longue) devient
            // la hauteur. Après rotation, le bas du cube est à +b[5] (zmax) sous le pivot.
            leg.xRot = -((float) Math.PI / 2.0F) + swing * 0.25F;
            leg.x = anchorX - centerX * s;
            leg.y = 24.0F - b[5] * s; // bas de la nageoire redressée posé au sol
            leg.z = 0.0F;
        } else {
            // Vraie patte verticale (sniffer) : bas du cube (maxY) posé au sol.
            float centerZ = (b[2] + b[5]) * 0.5F;
            leg.xRot = swing;
            leg.x = anchorX - centerX * s;
            leg.y = 24.0F - b[4] * s;
            leg.z = -centerZ * s;
        }
    }

    /** Première partie existante parmi {@code names} (noms d'os variables selon le mob). */
    @Nullable
    private static ModelPart firstPart(Function<String, ModelPart> lookup, String... names) {
        for (String name : names) {
            ModelPart part = lookup.apply(name);
            if (part != null) {
                return part;
            }
        }
        return null;
    }

    private static void setSubtreeDrawn(ModelPart part, boolean drawn) {
        for (ModelPart sub : part.getAllParts()) {
            sub.skipDraw = !drawn;
        }
    }

    /**
     * Bornes locales {minX,minY,minZ,maxX,maxY,maxZ} (px, repère du pivot) des cubes propres de la
     * partie (hors enfants). Sert à recentrer une patte sur l'ancre du bras malgré son décalage natif.
     */
    private static float[] localCubeBounds(ModelPart part) {
        float[] b = {Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE,
                -Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};
        part.visit(new com.mojang.blaze3d.vertex.PoseStack(), (pose, path, index, cube) -> {
            if (!path.isEmpty()) {
                return; // ignorer les cubes des sous-parties
            }
            b[0] = Math.min(b[0], cube.minX);
            b[1] = Math.min(b[1], cube.minY);
            b[2] = Math.min(b[2], cube.minZ);
            b[3] = Math.max(b[3], cube.maxX);
            b[4] = Math.max(b[4], cube.maxY);
            b[5] = Math.max(b[5], cube.maxZ);
        });
        if (b[0] > b[3]) { // aucun cube -> bornes neutres
            return new float[]{0, 0, 0, 0, 0, 0};
        }
        return b;
    }

    /**
     * Noms candidats par slot. On n'accepte que les <b>vraies parties bipèdes</b>
     * ({@code left_arm}/{@code left_leg}…) : leur pivot natif coïncide avec le squelette du minion.
     *
     * <p>Les pattes de quadrupède ({@code left_front_leg}, {@code left_hind_leg}…) ne sont
     * volontairement pas listées : en mode STANDARD elles ne sont pas repositionnées et
     * s'afficheraient au mauvais endroit (bras « manquants »). Pour ces mobs, {@link #hasParts}
     * reste faux et l'appelant retombe sur la passe humanoïde (bras/jambe bipède + texture du mob),
     * ce qui garantit des membres visibles et bien placés.</p>
     */
    private static List<String> namesFor(PartSlot slot) {
        return switch (slot) {
            case HEAD -> List.of("head");
            case BODY -> List.of("body");
            case ARM_L -> List.of("left_arm");
            case ARM_R -> List.of("right_arm");
            case LEGS -> List.of("left_leg", "right_leg");
        };
    }

    @Override
    public void setupAnim(MinionRenderState state) {
        super.setupAnim(state); // réinitialise toutes les poses à leur valeur bakée

        switch (mode) {
            case HEAD_FIT -> {
                ModelPart h = this.root;
                float sc = headFit.scale();
                h.xScale = sc;
                h.yScale = sc;
                h.zScale = sc;
                h.xRot = state.xRot * DEG;
                h.yRot = state.yRot * DEG;
                h.zRot = 0.0F;
                if (headAnchorPx != null) {
                    // Pose le CENTRE réel de la tête à l'ancre (avant du corps horizontal).
                    float[] b = localCubeBounds(h);
                    float cx = (b[0] + b[3]) * 0.5F;
                    float cy = (b[1] + b[4]) * 0.5F;
                    float cz = (b[2] + b[5]) * 0.5F;
                    h.x = headAnchorPx[0] - cx * sc;
                    h.y = headAnchorPx[1] - cy * sc;
                    h.z = headAnchorPx[2] - cz * sc;
                } else {
                    h.x = headFit.x();
                    h.y = headFit.y();
                    h.z = 0.0F;
                }
            }
            case LIMB_FIT -> {
                ModelPart l = this.root;
                // Centre/haut RÉELS du cube (les pattes vanilla sont souvent décalées du pivot) :
                // on place le pivot pour que le cube tombe à l'ancre du bras et pende depuis l'épaule.
                float[] b = localCubeBounds(l); // minX,minY,minZ,maxX,maxY,maxZ
                float scale = limbFit.scale();
                if (scale <= 0.0F) { // auto : ajuste la patte à ~11 px de long, ≤4 px de large
                    float len = Math.max(b[4] - b[1], 1.0F);
                    float wide = Math.max(b[3] - b[0], 1.0F);
                    scale = Math.min(11.0F / len, 4.0F / wide);
                }
                float centerX = (b[0] + b[3]) * 0.5F;
                float centerZ = (b[2] + b[5]) * 0.5F;
                float topY = b[1];
                l.x = limbCenterX - centerX * scale + limbFit.x();
                l.y = SHOULDER_TOP_Y - topY * scale + limbFit.y();
                l.z = -centerZ * scale;
                l.xScale = scale;
                l.yScale = scale;
                l.zScale = scale;

                // Animation : balancement de marche + coup d'attaque, comme un bras bipède.
                boolean leftArm = limbCenterX > 0.0F;
                float walkSpeed = Math.min(state.walkAnimationSpeed, 1.0F);
                float phase = leftArm ? 0.0F : (float) Math.PI;
                float xRot = Mth.cos(state.walkAnimationPos * 0.6662F + phase) * walkSpeed;
                float zRot = 0.0F;
                boolean isAttackArm = leftArm
                        ? state.attackArm == HumanoidArm.LEFT
                        : state.attackArm == HumanoidArm.RIGHT;
                if (state.attackTime > 0.0F && isAttackArm) {
                    float f = 1.0F - state.attackTime;
                    f *= f;
                    f *= f;
                    f = 1.0F - f;
                    float swing = Mth.sin(f * (float) Math.PI);
                    float lift = Mth.sin(state.attackTime * (float) Math.PI) * 0.75F;
                    xRot -= swing * 1.2F + lift;
                    zRot += Mth.sin(state.attackTime * (float) Math.PI) * -0.4F;
                }
                l.xRot = xRot;
                l.yRot = 0.0F;
                l.zRot = zRot;
            }
            case LEG_FIT -> {
                if (legLeft != null && legRight != null) {
                    // On neutralise tous les transforms ancêtres (corps, os…) pour que seules les
                    // deux pattes arrière dessinées comptent ; leurs sous-parties gardent la pose bakée.
                    java.util.Set<ModelPart> legParts = new java.util.HashSet<>();
                    legParts.addAll(legLeft.getAllParts());
                    legParts.addAll(legRight.getAllParts());
                    for (ModelPart p : this.root.getAllParts()) {
                        if (legParts.contains(p)) {
                            continue;
                        }
                        p.x = 0.0F; p.y = 0.0F; p.z = 0.0F;
                        p.xRot = 0.0F; p.yRot = 0.0F; p.zRot = 0.0F;
                        p.xScale = 1.0F; p.yScale = 1.0F; p.zScale = 1.0F;
                    }
                    float walkSpeed = Math.min(state.walkAnimationSpeed, 1.0F);
                    float swing = Mth.cos(state.walkAnimationPos * 0.6662F) * 1.4F * walkSpeed;
                    placeLeg(legLeft, 2.0F, swing);
                    placeLeg(legRight, -2.0F, -swing);
                }
            }
            case BODY_FIT -> {
                ModelPart bdy = this.root;
                bdy.x = bodyFitX;
                bdy.y = bodyFitY;
                bdy.z = bodyFitZ;
                bdy.xScale = bodyFitScale;
                bdy.yScale = bodyFitScale;
                bdy.zScale = bodyFitScale;
                bdy.xRot = 0.0F;
                bdy.yRot = 0.0F;
                bdy.zRot = 0.0F;
            }
            case GHAST -> {
                if (ghastBody != null) {
                    ghastBody.x = ghastX;
                    ghastBody.y = ghastY;
                    ghastBody.z = 0.0F;
                    ghastBody.xScale = ghastScale;
                    ghastBody.yScale = ghastScale;
                    ghastBody.zScale = ghastScale;
                    ghastBody.xRot = 0.0F;
                    ghastBody.yRot = 0.0F;
                    ghastBody.zRot = 0.0F;
                }
            }
            default -> {
                if (head != null) {
                    head.yRot = state.yRot * DEG;
                    head.xRot = state.xRot * DEG;
                }
            }
        }
    }
}
