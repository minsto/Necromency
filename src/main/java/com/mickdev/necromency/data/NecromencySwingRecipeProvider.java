package com.mickdev.necromency.data;

import com.mickdev.necromency.Necromency;
import com.mickdev.necromency.registry.Swing.Recipes.SwingShapedRecipe;
import com.mickdev.necromency.registry.data.NecroMobCatalog;
import com.mickdev.necromency.registry.data.NecroMobDefinition;
import com.mickdev.necromency.registry.init.ModItems;
import com.mickdev.necromency.registry.item.MobPart.BodyPartItem;
import com.mickdev.necromency.registry.item.MobPart.BodyPartStacks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Arrays;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class NecromencySwingRecipeProvider extends RecipeProvider {

    /** Placeholder pour les slots vides (non testé si {@code emptySlots[i]}). */
    private static final Ingredient SLOT_PLACEHOLDER = Ingredient.of(Items.BARRIER);

    public NecromencySwingRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        Ingredient skin = Ingredient.of(ModItems.SKIN.get());
        Ingredient brain = Ingredient.of(ModItems.BRAINS.get());
        Ingredient heart = Ingredient.of(ModItems.HEART.get());
        Ingredient lungs = Ingredient.of(ModItems.LUNGS.get());
        Ingredient muscle = Ingredient.of(ModItems.MUSCLE.get());
        Ingredient bone = Ingredient.of(Items.BONE);
        Ingredient spiderEye = Ingredient.of(Items.SPIDER_EYE);

        var skinGrid = shapelessGrid(Ingredient.of(Items.LEATHER));
        swing(id("swing/skin_from_leather"), skinGrid.slots(), skinGrid.emptySlots(),
                new ItemStack(ModItems.SKIN.get(), 8));

        var spawnerGrid = shapelessGrid(
                Ingredient.of(Items.ROTTEN_FLESH),
                Ingredient.of(Items.ROTTEN_FLESH),
                Ingredient.of(Items.ROTTEN_FLESH),
                Ingredient.of(Items.ROTTEN_FLESH),
                Ingredient.of(Items.ROTTEN_FLESH),
                Ingredient.of(Items.GHAST_TEAR),
                Ingredient.of(Items.GHAST_TEAR),
                Ingredient.of(ModItems.JAR_OF_SOUL.get()),
                heart
        );
        swing(id("swing/necro_spawner"), spawnerGrid.slots(), spawnerGrid.emptySlots(),
                new ItemStack(ModItems.NECRO_SPAWNER.get()));

        var teddyEgg = fromPattern(
                new String[]{"LLLL", "LWWL", "LWWL", "LLLL"},
                Map.of('L', Ingredient.of(Items.LEATHER), 'W', Ingredient.of(Items.WHITE_WOOL))
        );
        swing(id("swing/teddy_spawn_egg"), teddyEgg.slots(), teddyEgg.emptySlots(),
                new ItemStack(ModItems.TEDDY_SPAWN_EGG.get()));

        for (NecroMobDefinition mob : NecroMobCatalog.all()) {
            if (mob.hasHead()) {
                registerPart(mob, BodyPartItem.PartType.HEAD,
                        headGrid(skin, brain, spiderEye, mob.headEssence()), "head");
            }
            if (mob.hasTorso()) {
                registerPart(mob, BodyPartItem.PartType.BODY,
                        torsoGrid(skin, heart, lungs, bone, mob.torsoEssence()), "body");
            }
            if (mob.hasArms()) {
                registerPart(mob, BodyPartItem.PartType.ARM_LEFT,
                        armGrid(skin, muscle, bone, mob.armEssence()), "arm_left");
                registerPart(mob, BodyPartItem.PartType.ARM_RIGHT,
                        armGrid(skin, muscle, bone, mob.armEssence()), "arm_right");
            }
            if (mob.hasLegs()) {
                registerPart(mob, BodyPartItem.PartType.LEGS,
                        legGrid(skin, muscle, bone, mob.legEssence()), "legs");
            }
        }
    }

    private record GridPattern(NonNullList<Ingredient> slots, boolean[] emptySlots) {}

    private void registerPart(NecroMobDefinition mob, BodyPartItem.PartType part,
                              GridPattern grid, String suffix) {
        String path = mob.mobId().replace(':', '_') + "_" + suffix.replace(':', '_');
        swing(id("swing/" + path), grid.slots(), grid.emptySlots(), BodyPartStacks.create(part, mob.mobId()));
    }

    private void swing(ResourceLocation recipeId, NonNullList<Ingredient> grid, boolean[] emptySlots, ItemStack result) {
        output.accept(
                ResourceKey.create(Registries.RECIPE, recipeId),
                new SwingShapedRecipe(grid, emptySlots, result),
                null
        );
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Necromency.MODID, path);
    }

    private static GridPattern shapelessGrid(Ingredient... items) {
        NonNullList<Ingredient> grid = NonNullList.withSize(16, SLOT_PLACEHOLDER);
        boolean[] empty = new boolean[16];
        Arrays.fill(empty, true);
        for (int i = 0; i < items.length && i < 16; i++) {
            grid.set(i, items[i]);
            empty[i] = false;
        }
        return new GridPattern(grid, empty);
    }

    private static GridPattern fromPattern(String[] rows, Map<Character, Ingredient> keys) {
        NonNullList<Ingredient> grid = NonNullList.withSize(16, SLOT_PLACEHOLDER);
        boolean[] empty = new boolean[16];
        Arrays.fill(empty, true);
        for (int r = 0; r < 4; r++) {
            String row = r < rows.length ? rows[r] : "    ";
            while (row.length() < 4) row += " ";
            for (int c = 0; c < 4; c++) {
                int idx = r * 4 + c;
                char ch = row.charAt(c);
                if (ch == ' ') {
                    grid.set(idx, SLOT_PLACEHOLDER);
                    empty[idx] = true;
                } else {
                    grid.set(idx, keys.get(ch));
                    empty[idx] = false;
                }
            }
        }
        return new GridPattern(grid, empty);
    }

    private static GridPattern headGrid(Ingredient skin, Ingredient brain, Ingredient eye, Ingredient essence) {
        return fromPattern(
                new String[]{"SSSS", "SBFS", "SEES", "    "},
                Map.of('S', skin, 'B', brain, 'F', essence, 'E', eye)
        );
    }

    private static GridPattern torsoGrid(Ingredient skin, Ingredient heart, Ingredient lungs, Ingredient bone, Ingredient essence) {
        return fromPattern(
                new String[]{" LL ", "BHUB", "LEEL", "BLLB"},
                Map.of('L', skin, 'B', bone, 'H', heart, 'U', lungs, 'E', essence)
        );
    }

    private static GridPattern armGrid(Ingredient skin, Ingredient muscle, Ingredient bone, Ingredient essence) {
        return fromPattern(
                new String[]{"LLLL", "BMEB", "LLLL", "    "},
                Map.of('L', skin, 'B', bone, 'M', muscle, 'E', essence)
        );
    }

    private static GridPattern legGrid(Ingredient skin, Ingredient muscle, Ingredient bone, Ingredient essence) {
        return fromPattern(
                new String[]{"LBBL", "LMML", "LEEL", "LBBL"},
                Map.of('L', skin, 'B', bone, 'M', muscle, 'E', essence)
        );
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(output, lookupProvider);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider provider, RecipeOutput output) {
            return new NecromencySwingRecipeProvider(provider, output);
        }

        @Override
        public String getName() {
            return "Necromency Swing Recipes";
        }
    }
}
