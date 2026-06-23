package com.mickdev.necromency.registry.Swing.Recipes;


import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class SwingShapedRecipe implements Recipe<SwingInput> {

    /** 16 slots (4x4). {@link #emptySlots} indique les cases qui doivent rester vides. */
    private final NonNullList<Ingredient> slots; // size = 16
    private final boolean[] emptySlots;
    private final ItemStack result;

    public SwingShapedRecipe(NonNullList<Ingredient> slots, ItemStack result) {
        this(slots, new boolean[16], result);
    }

    public SwingShapedRecipe(NonNullList<Ingredient> slots, boolean[] emptySlots, ItemStack result) {
        this.slots = slots;
        this.emptySlots = emptySlots;
        this.result = result;
    }

    public boolean[] getEmptySlots() {
        return emptySlots;
    }

    public NonNullList<Ingredient> getSlots4x4() {
        return slots;
    }

    public ItemStack getResult() {
        return result;
    }

    @Override
    public boolean matches(SwingInput input, Level level) {
        if (input.size() < 16) return false;
        for (int i = 0; i < 16; i++) {
            ItemStack stack = input.getItem(i);
            if (emptySlots[i]) {
                if (!stack.isEmpty()) return false;
            } else if (!slots.get(i).test(stack)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(SwingInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public RecipeSerializer<? extends Recipe<SwingInput>> getSerializer() {
        return SwingRecipeSerializer.SWING_SHAPED.get();
    }

    @Override
    public RecipeType<? extends Recipe<SwingInput>> getType() {
        return SwingRecipeType.SWING_SHAPED.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return null;
    }


    @Override
    public boolean isSpecial() {
        return true;
    }

    // ===== Slot wrapper =====
    public static final class Slot {
        private final @Nullable net.minecraft.world.item.crafting.Ingredient ingredient; // null = slot vide

        public Slot(@Nullable net.minecraft.world.item.crafting.Ingredient ingredient) {
            this.ingredient = ingredient;
        }

        public boolean isEmptySlot() {
            return ingredient == null;
        }

        public boolean test(ItemStack stack) {
            return ingredient != null && ingredient.test(stack);
        }

        public @Nullable net.minecraft.world.item.crafting.Ingredient ingredient() {
            return ingredient;
        }

        public static Slot empty() {
            return new Slot(null);
        }

        public static Slot of(net.minecraft.world.item.crafting.Ingredient ing) {
            return new Slot(ing);
        }
    }
}