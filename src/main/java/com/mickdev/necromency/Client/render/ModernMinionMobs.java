package com.mickdev.necromency.Client.render;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Mobs vanilla ajoutés après la 1.12.2 : ils n'ont pas de géométrie 1.7.10 dans
 * {@link com.mickdev.necromency.Client.legacy.LegacyNecroParts}. Pour eux on réutilise
 * directement le {@code LayerDefinition} vanilla (modèle officiel du jeu) au lieu de plaquer
 * la texture sur un bipède zombie. Les mobs présents en 1.12.2 ne sont volontairement pas listés.
 */
public final class ModernMinionMobs {

    private static final Map<ResourceLocation, ModelLayerLocation> LAYERS = new HashMap<>();

    static {
        put("copper_golem", ModelLayers.COPPER_GOLEM);
        put("axolotl", ModelLayers.AXOLOTL);
        put("turtle", ModelLayers.TURTLE);
        put("fox", ModelLayers.FOX);
        put("cat", ModelLayers.CAT);
        put("ghast", ModelLayers.GHAST);
        put("happy_ghast", ModelLayers.HAPPY_GHAST);
        put("piglin", ModelLayers.PIGLIN);
        put("warden", ModelLayers.WARDEN);
        put("wolf", ModelLayers.WOLF);
        put("pillager", ModelLayers.PILLAGER);
        put("wandering_trader", ModelLayers.WANDERING_TRADER);
        put("illusioner", ModelLayers.ILLUSIONER);
        // Corps quadrupède rendu via le vrai modèle vanilla (UV/texture officiels), pas la géométrie
        // legacy cochon : voir VanillaSlotModel#BODY_FIT_MOBS et MinionRenderer#prefersVanillaBody.
        put("goat", ModelLayers.GOAT);
        put("sniffer", ModelLayers.SNIFFER);
        put("armadillo", ModelLayers.ARMADILLO);
    }

    private ModernMinionMobs() {}

    private static void put(String path, ModelLayerLocation layer) {
        LAYERS.put(ResourceLocation.withDefaultNamespace(path), layer);
    }

    public static boolean contains(@Nullable ResourceLocation id) {
        return id != null && LAYERS.containsKey(id);
    }

    @Nullable
    public static ModelLayerLocation layer(@Nullable ResourceLocation id) {
        return id == null ? null : LAYERS.get(id);
    }
}
