package com.mickdev.necromency.registry.item.MobPart;

import com.mickdev.necromency.Necromency;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Set;

/** Chemins {@code necromency:bodyparts/<mob>/<part>} — textures portées depuis 1.7.10. */
public final class BodyPartModels {

    /** Combinaisons présentes dans {@code textures/item/bodyparts/}. */
    private static final Set<String> AVAILABLE = Set.of(
            "zombie/head", "zombie/torso", "zombie/arm", "zombie/legs",
            "skeleton/head", "skeleton/torso", "skeleton/arm", "skeleton/legs",
            "creeper/torso", "creeper/legs",
            "spider/head", "spider/torso", "spider/legs",
            "cave_spider/head", "cave_spider/torso", "cave_spider/legs",
            "enderman/head", "enderman/torso", "enderman/arm", "enderman/legs",
            "witch/head", "witch/torso", "witch/arm", "witch/legs",
            "chicken/head", "chicken/torso", "chicken/arm", "chicken/legs",
            "pig/head", "pig/torso", "pig/arm", "pig/legs",
            "cow/head", "cow/torso", "cow/arm", "cow/legs",
            "sheep/head", "sheep/torso", "sheep/arm", "sheep/legs",
            "squid/head", "squid/torso", "squid/legs",
            "villager/head", "villager/torso", "villager/arm", "villager/legs",
            "wolf/head", "wolf/torso", "wolf/arm", "wolf/legs",
            "zombified_piglin/head", "zombified_piglin/torso", "zombified_piglin/arm", "zombified_piglin/legs",
            "iron_golem/head", "iron_golem/torso", "iron_golem/arm", "iron_golem/legs",
            "copper_golem/head", "copper_golem/torso", "copper_golem/arm", "copper_golem/legs",
            "axolotl/head", "axolotl/torso", "axolotl/arm", "axolotl/legs",
            "goat/head", "goat/torso", "goat/arm", "goat/legs",
            "sniffer/head", "sniffer/torso", "sniffer/arm", "sniffer/legs",
            "turtle/head", "turtle/torso", "turtle/arm", "turtle/legs",
            "fox/head", "fox/torso", "fox/arm", "fox/legs",
            "cat/head", "cat/torso", "cat/arm", "cat/legs",
            "ghast/head", "ghast/torso", "ghast/legs",
            "happy_ghast/head", "happy_ghast/torso", "happy_ghast/legs",
            "piglin/head", "piglin/torso", "piglin/arm", "piglin/legs",
            "warden/head", "warden/torso", "warden/arm", "warden/legs",
            "pillager/head", "pillager/torso", "pillager/arm", "pillager/legs",
            "wandering_trader/head", "wandering_trader/torso", "wandering_trader/arm", "wandering_trader/legs",
            "illusioner/head", "illusioner/torso", "illusioner/arm", "illusioner/legs",
            "armadillo/head", "armadillo/torso", "armadillo/arm", "armadillo/legs"
    );

    private BodyPartModels() {}

    @Nullable
    public static ResourceLocation itemModelId(String mobId, BodyPartItem.PartType part) {
        String folder = textureFolder(mobId);
        String partName = texturePart(part);
        if (folder == null || partName == null) {
            return null;
        }
        String key = folder + "/" + partName;
        if (!AVAILABLE.contains(key)) {
            return null;
        }
        return ResourceLocation.fromNamespaceAndPath(Necromency.MODID, "bodyparts/" + key);
    }

    @Nullable
    private static String textureFolder(String mobId) {
        if (mobId == null || mobId.isBlank()) {
            return null;
        }
        ResourceLocation id = ResourceLocation.tryParse(mobId);
        if (id == null) {
            return null;
        }
        if ("minecraft".equals(id.getNamespace())) {
            return id.getPath();
        }
        return switch (id.getPath()) {
            case "isaac_head", "isaac_normal", "isaac_blood", "isaac_body" -> "isaac";
            case "nightcrawler" -> "nightcrawler";
            case "teddy" -> "teddy";
            default -> id.getPath();
        };
    }

    @Nullable
    private static String texturePart(BodyPartItem.PartType part) {
        return switch (part) {
            case HEAD -> "head";
            case BODY -> "torso";
            case ARM_LEFT, ARM_RIGHT -> "arm";
            case LEGS -> "legs";
        };
    }
}
