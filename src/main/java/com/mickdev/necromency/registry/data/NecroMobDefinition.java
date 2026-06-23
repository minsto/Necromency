package com.mickdev.necromency.registry.data;

import net.minecraft.world.item.crafting.Ingredient;

/**
 * Définition d'un mob pour les recettes du swing (machine à coudre 4×4).
 */
public record NecroMobDefinition(
        String mobId,
        Ingredient headEssence,
        Ingredient torsoEssence,
        Ingredient armEssence,
        Ingredient legEssence,
        boolean hasHead,
        boolean hasTorso,
        boolean hasArms,
        boolean hasLegs
) {
    public static NecroMobDefinition uniform(String mobId, Ingredient essence) {
        return new NecroMobDefinition(mobId, essence, essence, essence, essence, true, true, true, true);
    }

    public static NecroMobDefinition uniform(String mobId, Ingredient essence, boolean arms) {
        return new NecroMobDefinition(mobId, essence, essence, essence, essence, true, true, arms, true);
    }

    /** Tête seule (loup, Isaac, etc.). */
    public static NecroMobDefinition headOnly(String mobId, Ingredient headEssence) {
        return new NecroMobDefinition(mobId, headEssence, headEssence, headEssence, headEssence, true, false, false, false);
    }
}
