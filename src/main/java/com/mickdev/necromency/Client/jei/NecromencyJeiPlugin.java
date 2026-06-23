package com.mickdev.necromency.Client.jei;

import com.mickdev.necromency.Necromency;
import com.mickdev.necromency.registry.Swing.Recipes.SwingClientRecipes;
import com.mickdev.necromency.registry.init.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;

/**
 * Plugin JEI de Necromency : expose les recettes de la machine à coudre (Swing).
 *
 * <p>Cette classe n'est chargée que par JEI (via l'annotation {@link JeiPlugin}) et
 * uniquement côté client ; elle ne doit être référencée par aucun autre code du mod.</p>
 */
@JeiPlugin
public class NecromencyJeiPlugin implements IModPlugin {

    private static final ResourceLocation PLUGIN_UID =
            ResourceLocation.fromNamespaceAndPath(Necromency.MODID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new SwingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(SwingRecipeCategory.RECIPE_TYPE, SwingClientRecipes.get());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(
                SwingRecipeCategory.RECIPE_TYPE, ModItems.swing_Item.get());
    }
}
