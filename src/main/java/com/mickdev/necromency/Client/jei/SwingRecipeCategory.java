package com.mickdev.necromency.Client.jei;

import com.mickdev.necromency.Necromency;
import com.mickdev.necromency.registry.Swing.Recipes.SwingShapedRecipe;
import com.mickdev.necromency.registry.init.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Catégorie JEI pour les recettes de la machine à coudre (grille 4x4 + résultat).
 */
public class SwingRecipeCategory implements IRecipeCategory<SwingShapedRecipe> {

    public static final IRecipeType<SwingShapedRecipe> RECIPE_TYPE =
            IRecipeType.create(
                    ResourceLocation.fromNamespaceAndPath(Necromency.MODID, "swing_shaped"),
                    SwingShapedRecipe.class);

    private static final int GRID = 4;
    private static final int CELL = 18;
    private static final int PAD = 2;

    private static final int ARROW_X = PAD + GRID * CELL + 4;
    private static final int ARROW_Y = PAD + (GRID * CELL) / 2 - 8;
    private static final int OUTPUT_X = ARROW_X + 28;
    private static final int OUTPUT_Y = PAD + (GRID * CELL) / 2 - 9;

    private final Component title;
    private final IDrawable icon;
    private final IDrawableStatic arrow;

    public SwingRecipeCategory(IGuiHelper guiHelper) {
        this.title = Component.translatable("block.necromency.swing");
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModItems.swing_Item.get()));
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public IRecipeType<SwingShapedRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return OUTPUT_X + CELL + PAD;
    }

    @Override
    public int getHeight() {
        return PAD * 2 + GRID * CELL;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SwingShapedRecipe recipe, IFocusGroup focuses) {
        var slots = recipe.getSlots4x4();
        boolean[] empty = recipe.getEmptySlots();

        for (int row = 0; row < GRID; row++) {
            for (int col = 0; col < GRID; col++) {
                int i = row * GRID + col;
                if (empty[i]) continue;
                Ingredient ingredient = slots.get(i);
                if (ingredient.isEmpty()) continue;

                int x = PAD + col * CELL;
                int y = PAD + row * CELL;
                builder.addSlot(RecipeIngredientRole.INPUT, x + 1, y + 1)
                        .setStandardSlotBackground()
                        .add(ingredient);
            }
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X + 1, OUTPUT_Y + 1)
                .setOutputSlotBackground()
                .add(recipe.getResult());
    }

    @Override
    public void draw(SwingShapedRecipe recipe, IRecipeSlotsView recipeSlotsView,
                     GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, ARROW_X, ARROW_Y);
    }
}
