package com.mickdev.necromency.Client.Util;

import com.mickdev.necromency.Client.Util.PartSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * 1.21.5+ : l’ancienne {@code entity/chicken.png} a été déplacée vers
 * {@code textures/entity/chicken/temperate_chicken.png} (variantes froid / chaud en plus).
 */
public final class MobTextureResolver {

    public static final ResourceLocation MISSING =
            ResourceLocation.parse("minecraft:textures/entity/zombie/zombie.png");

    public static final ResourceLocation ZOMBIE_ID =
            ResourceLocation.parse("minecraft:zombie");

    /** Icônes inventaire 16×16 — affichage item uniquement, pas le rendu minion. */
    private static final Set<String> BODYPART_TEXTURE_MOBS = Set.of("goat", "sniffer", "armadillo");

    /** Textures entité custom du mod (prioritaires sur vanilla pour le rendu legacy). */
    private static final java.util.Map<String, ResourceLocation> NECRO_ENTITY_TEXTURES = java.util.Map.of(
            "goat", ResourceLocation.fromNamespaceAndPath("necromency", "textures/entity/goat/goat.png"),
            "sniffer", ResourceLocation.fromNamespaceAndPath("necromency", "textures/entity/sniffer/sniffer.png")
    );

    private static final ResourceLocation[] CHICKEN_ENTITY_CANDIDATES = {
            ResourceLocation.parse("minecraft:textures/entity/chicken/temperate_chicken.png"),
            ResourceLocation.parse("minecraft:textures/entity/chicken/warm_chicken.png"),
            ResourceLocation.parse("minecraft:textures/entity/chicken/cold_chicken.png"),
            ResourceLocation.parse("minecraft:textures/entity/chicken.png"),
            ResourceLocation.parse("minecraft:textures/entity/chicken/chicken.png"),
    };

    public static ResourceLocation textureForMobId(ResourceLocation id) {
        if (id == null) return MISSING;

        String path = id.getPath();
        ResourceLocation necroOverride = NECRO_ENTITY_TEXTURES.get(path);
        if (necroOverride != null) {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.getResourceManager().getResource(necroOverride).isPresent()) {
                return necroOverride;
            }
        }
        return vanillaTextureForMobId(id);
    }

    /**
     * Texture entité <b>vanilla pure</b> (sans override custom du mod). Indispensable pour le rendu
     * via le vrai modèle vanilla baké ({@code VanillaSlotModel}/{@code VanillaMobModel}) : les UV du
     * modèle officiel n'ont de sens qu'avec l'atlas officiel — une texture custom peinte pour une
     * autre géométrie (ex. chèvre/sniffer layout cochon legacy) échantillonnerait la mauvaise zone.
     */
    public static ResourceLocation vanillaTextureForMobId(ResourceLocation id) {
        if (id == null) return MISSING;

        String ns = id.getNamespace();
        String path = id.getPath();

        // Si on te donne déjà un chemin de texture, on le renvoie direct
        if (path.startsWith("textures/") && path.endsWith(".png")) {
            return ResourceLocation.fromNamespaceAndPath(ns, path);
        }

        if (!"minecraft".equals(ns)) {
            return switch (path) {
                case "nightcrawler" -> ResourceLocation.fromNamespaceAndPath("necromency", "textures/entities/nightcrawler.png");
                case "teddy" -> ResourceLocation.fromNamespaceAndPath("necromency", "textures/entities/teddy.png");
                case "isaac_normal", "isaac_blood", "isaac_body", "isaac_head" ->
                        ResourceLocation.fromNamespaceAndPath("necromency", "textures/entities/isaac.png");
                default -> MISSING;
            };
        }

        return switch (path) {
            case "zombie" -> ResourceLocation.parse("minecraft:textures/entity/zombie/zombie.png");
            case "skeleton" -> ResourceLocation.parse("minecraft:textures/entity/skeleton/skeleton.png");
            case "creeper" -> ResourceLocation.parse("minecraft:textures/entity/creeper/creeper.png");
            case "spider" -> ResourceLocation.parse("minecraft:textures/entity/spider/spider.png");
            case "cave_spider" -> ResourceLocation.parse("minecraft:textures/entity/spider/cave_spider.png");
            case "enderman" -> ResourceLocation.parse("minecraft:textures/entity/enderman/enderman.png");
            case "witch" -> ResourceLocation.parse("minecraft:textures/entity/witch.png");
            case "villager" -> ResourceLocation.parse("minecraft:textures/entity/villager/villager.png");
            case "pig" -> ResourceLocation.parse("minecraft:textures/entity/pig/temperate_pig.png");
            case "cow" -> ResourceLocation.parse("minecraft:textures/entity/cow/temperate_cow.png");
            case "sheep" -> ResourceLocation.parse("minecraft:textures/entity/sheep/sheep.png");
            case "wolf" -> ResourceLocation.parse("minecraft:textures/entity/wolf/wolf.png");
            case "squid" -> ResourceLocation.parse("minecraft:textures/entity/squid/squid.png");
            case "zombified_piglin" -> ResourceLocation.parse("minecraft:textures/entity/piglin/zombified_piglin.png");
            case "iron_golem" -> ResourceLocation.parse("minecraft:textures/entity/iron_golem/iron_golem.png");
            case "slime" -> ResourceLocation.parse("minecraft:textures/entity/slime/slime.png");
            case "magma_cube" -> ResourceLocation.parse("minecraft:textures/entity/slime/magmacube.png");
            case "chicken" -> resolveChickenEntityTextureForRendering();
            case "copper_golem" -> ResourceLocation.parse("minecraft:textures/entity/copper_golem/copper_golem.png");
            case "axolotl" -> ResourceLocation.parse("minecraft:textures/entity/axolotl/axolotl_cyan.png");
            case "goat" -> ResourceLocation.parse("minecraft:textures/entity/goat/goat.png");
            case "sniffer" -> ResourceLocation.parse("minecraft:textures/entity/sniffer/sniffer.png");
            case "turtle" -> ResourceLocation.parse("minecraft:textures/entity/turtle/big_sea_turtle.png");
            case "fox" -> ResourceLocation.parse("minecraft:textures/entity/fox/fox.png");
            case "cat" -> ResourceLocation.parse("minecraft:textures/entity/cat/tabby.png");
            case "ghast" -> ResourceLocation.parse("minecraft:textures/entity/ghast/ghast.png");
            case "happy_ghast" -> ResourceLocation.parse("minecraft:textures/entity/ghast/happy_ghast.png");
            case "piglin" -> ResourceLocation.parse("minecraft:textures/entity/piglin/piglin.png");
            case "warden" -> ResourceLocation.parse("minecraft:textures/entity/warden/warden.png");
            case "pillager" -> ResourceLocation.parse("minecraft:textures/entity/illager/pillager.png");
            case "wandering_trader" -> ResourceLocation.parse("minecraft:textures/entity/wandering_trader.png");
            case "illusioner" -> ResourceLocation.parse("minecraft:textures/entity/illager/illusioner.png");
            case "armadillo" -> ResourceLocation.parse("minecraft:textures/entity/armadillo.png");
            default -> MISSING;
        };
    }

    /**
     * Choisit la première texture de poulet présente dans les packs (ordre 1.21+ puis secours).
     */
    @Nullable
    public static ResourceLocation firstExistingChickenEntityTexture(@Nullable ResourceManager resourceManager) {
        if (resourceManager == null) {
            return null;
        }
        for (ResourceLocation c : CHICKEN_ENTITY_CANDIDATES) {
            if (resourceManager.getResource(c).isPresent()) {
                return c;
            }
        }
        return null;
    }

    /**
     * Texture bodypart 1.7.10 ({@code textures/item/bodyparts/<mob>/<part>.png}) pour un slot de minion.
     * Utilisée pour chèvre/sniffer/armadillo dont l'atlas entité ne correspond pas à la géométrie legacy cochon.
     */
    @Nullable
    public static ResourceLocation bodypartTextureFor(@Nullable ResourceLocation mobId, PartSlot slot) {
        if (mobId == null || !"minecraft".equals(mobId.getNamespace())) {
            return null;
        }
        if (!BODYPART_TEXTURE_MOBS.contains(mobId.getPath())) {
            return null;
        }
        String part = switch (slot) {
            case HEAD -> "head";
            case BODY -> "torso";
            case ARM_L, ARM_R -> "arm";
            case LEGS -> "legs";
        };
        return ResourceLocation.fromNamespaceAndPath("necromency",
                "textures/item/bodyparts/" + mobId.getPath() + "/" + part + ".png");
    }

    /** Bodypart si le mob l'exige, sinon texture entité vanilla. */
    public static ResourceLocation textureForLegacySlot(@Nullable ResourceLocation mobId, PartSlot slot) {
        ResourceLocation bodypart = bodypartTextureFor(mobId, slot);
        if (bodypart != null) {
            return bodypart;
        }
        return textureForMobId(mobId);
    }

    public static ResourceLocation resolveChickenEntityTextureForRendering() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            ResourceLocation found = firstExistingChickenEntityTexture(mc.getResourceManager());
            if (found != null) {
                return found;
            }
        }
        return CHICKEN_ENTITY_CANDIDATES[0];
    }

    private MobTextureResolver() {}
}
