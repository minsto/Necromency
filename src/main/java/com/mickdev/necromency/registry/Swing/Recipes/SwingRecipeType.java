package com.mickdev.necromency.registry.Swing.Recipes;

import com.mickdev.necromency.Necromency;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class SwingRecipeType {

    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, Necromency.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<SwingShapedRecipe>> SWING_SHAPED =
            TYPES.register("swing_shaped",
                    () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(Necromency.MODID, "swing_shaped"))
            );

    private SwingRecipeType() {}
}