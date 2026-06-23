package com.mickdev.necromency.registry.Swing.Recipes;

import com.mickdev.necromency.registry.item.MobPart.BodyPartItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * Table de craft : un bras droit → bras gauche du même mob (et inversement). Fonctionne pour tous les mobs
 * (item générique {@code body_part} + NBT, ou items dédiés legacy comme {@code zombie_right_arm}).
 */
public class ArmFlipRecipe extends CustomRecipe {

    public ArmFlipRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return findSingleArm(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack arm = findSingleArm(input);
        if (arm == null) {
            return ItemStack.EMPTY;
        }
        return BodyPartItem.flipArmSide(arm);
    }

    @Override
    public RecipeSerializer<ArmFlipRecipe> getSerializer() {
        return SwingRecipeSerializer.ARM_FLIP.get();
    }

    /**
     * Une seule stack dans la grille, et c'est un bras (gauche ou droit) avec un mob connu.
     * Les bras droits produiront le bras gauche correspondant via {@link BodyPartItem#flipArmSide}.
     */
    @Nullable
    private static ItemStack findSingleArm(CraftingInput input) {
        ItemStack found = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (!found.isEmpty()) {
                return null;
            }
            if (!BodyPartItem.isRightArm(stack) && !BodyPartItem.isLeftArm(stack)) {
                return null;
            }
            if (BodyPartItem.resolveMobId(stack) == null) {
                return null;
            }
            found = stack;
        }
        return found.isEmpty() ? null : found;
    }
}
