package com.mickdev.necromency.registry.Swing.Recipes;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.List;

public record SwingInput(List<ItemStack> stacks) implements RecipeInput {
    @Override
    public ItemStack getItem(int slot) {
        return stacks.get(slot);
    }

    @Override
    public int size() {
        return 16;
    }
}