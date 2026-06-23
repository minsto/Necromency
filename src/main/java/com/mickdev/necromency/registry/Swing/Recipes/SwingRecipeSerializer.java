package com.mickdev.necromency.registry.Swing.Recipes;


import com.mickdev.necromency.Necromency;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import com.mickdev.necromency.Necromency;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@SuppressWarnings("deprecation")
public final class SwingRecipeSerializer {

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, Necromency.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<SwingShapedRecipe>> SWING_SHAPED =
            SERIALIZERS.register("swing_shaped", SwingShapedRecipeSerializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ArmFlipRecipe>> ARM_FLIP =
            SERIALIZERS.register("arm_flip",
                    () -> new net.minecraft.world.item.crafting.CustomRecipe.Serializer<>(ArmFlipRecipe::new));

    private SwingRecipeSerializer() {}

    public static final class SwingShapedRecipeSerializer implements RecipeSerializer<SwingShapedRecipe> {

        private record IngredientsFormat(List<Ingredient> ingredients, List<Boolean> empty, ItemStack result) {}
        private record PatternFormat(List<String> pattern, Map<String, Ingredient> key, ItemStack result) {}

        private static final Codec<List<Ingredient>> INGREDIENTS_16_CODEC =
                Ingredient.CODEC.listOf().flatXmap(list -> {
                    if (list.size() != 16) {
                        return DataResult.error(() -> "ingredients must have 16 entries (4x4)");
                    }
                    for (int i = 0; i < 16; i++) {
                        final int idx = i;
                        final Ingredient ing = list.get(i);
                        if (ing == null || ing.isEmpty()) {
                            return DataResult.error(() -> "ingredients[" + idx + "] is empty");
                        }
                    }
                    return DataResult.success(list);
                }, DataResult::success);

        private static final Codec<List<String>> PATTERN_4_CODEC =
                Codec.STRING.listOf().flatXmap(list -> {
                    if (list.size() != 4) return DataResult.error(() -> "pattern must have 4 lines");
                    for (String s : list) {
                        if (s.length() != 4) return DataResult.error(() -> "each pattern line must be exactly 4 characters");
                    }
                    return DataResult.success(list);
                }, DataResult::success);

        private static final Codec<Map<String, Ingredient>> KEY_CODEC =
                Codec.unboundedMap(Codec.STRING, Ingredient.CODEC);

        private static final Codec<IngredientsFormat> INGREDIENTS_FORMAT_CODEC =
                RecordCodecBuilder.create(inst -> inst.group(
                        INGREDIENTS_16_CODEC.fieldOf("ingredients").forGetter(IngredientsFormat::ingredients),
                        // Cases à laisser vides (true) : l'ingrédient correspondant est ignoré (placeholder).
                        Codec.BOOL.listOf().optionalFieldOf("empty", List.of()).forGetter(IngredientsFormat::empty),
                        ItemStack.CODEC.fieldOf("result").forGetter(IngredientsFormat::result)
                ).apply(inst, IngredientsFormat::new));

        private static final Codec<PatternFormat> PATTERN_FORMAT_CODEC =
                RecordCodecBuilder.create(inst -> inst.group(
                        PATTERN_4_CODEC.fieldOf("pattern").forGetter(PatternFormat::pattern),
                        KEY_CODEC.fieldOf("key").forGetter(PatternFormat::key),
                        ItemStack.CODEC.fieldOf("result").forGetter(PatternFormat::result)
                ).apply(inst, PatternFormat::new));

        private static final Codec<SwingShapedRecipe> ROOT_CODEC =
                Codec.either(INGREDIENTS_FORMAT_CODEC, PATTERN_FORMAT_CODEC).flatXmap(
                        either -> either.map(
                                f -> DataResult.success(fromIngredients(f.ingredients(), f.empty(), f.result())),
                                f -> fromPattern(f.pattern(), f.key(), f.result())
                        ),
                        recipe -> DataResult.success(Either.left(
                                new IngredientsFormat(recipe.getSlots4x4(), toBoolList(recipe.getEmptySlots()), recipe.getResult())
                        ))
                );

        private static final MapCodec<SwingShapedRecipe> CODEC = new MapCodec<>() {
            @Override
            public <T> DataResult<SwingShapedRecipe> decode(DynamicOps<T> ops, MapLike<T> input) {
                return ROOT_CODEC.parse(ops, ops.createMap(input.entries()));
            }

            @Override
            public <T> RecordBuilder<T> encode(SwingShapedRecipe value, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                return ROOT_CODEC.encodeStart(ops, value)
                        .flatMap(ops::getMap)
                        .map(mapLike -> {
                            mapLike.entries().forEach(e -> prefix.add(e.getFirst(), e.getSecond()));
                            return prefix;
                        })
                        .result()
                        .orElse(prefix);
            }

            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return Stream.of(
                        ops.createString("ingredients"),
                        ops.createString("pattern"),
                        ops.createString("key"),
                        ops.createString("result")
                );
            }
        };

        private static final StreamCodec<RegistryFriendlyByteBuf, SwingShapedRecipe> STREAM_CODEC =
                StreamCodec.of(SwingShapedRecipeSerializer::toNetwork, SwingShapedRecipeSerializer::fromNetwork);

        private static SwingShapedRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            NonNullList<Ingredient> nn = NonNullList.withSize(16, Ingredient.of(Items.STONE));
            boolean[] empty = new boolean[16];
            for (int i = 0; i < 16; i++) nn.set(i, Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
            for (int i = 0; i < 16; i++) empty[i] = buf.readBoolean();
            ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
            return new SwingShapedRecipe(nn, empty, result);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buf, SwingShapedRecipe recipe) {
            for (int i = 0; i < 16; i++) Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.getSlots4x4().get(i));
            for (int i = 0; i < 16; i++) buf.writeBoolean(recipe.getEmptySlots()[i]);
            ItemStack.STREAM_CODEC.encode(buf, recipe.getResult());
        }

        @Override public MapCodec<SwingShapedRecipe> codec() { return CODEC; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, SwingShapedRecipe> streamCodec() { return STREAM_CODEC; }

        private static SwingShapedRecipe fromIngredients(List<Ingredient> list, List<Boolean> empty, ItemStack result) {
            NonNullList<Ingredient> nn = NonNullList.withSize(16, Ingredient.of(Items.STONE));
            boolean[] emptySlots = new boolean[16];
            boolean hasEmpty = empty.size() == 16;
            for (int i = 0; i < 16; i++) {
                nn.set(i, list.get(i));
                if (hasEmpty) emptySlots[i] = empty.get(i);
            }
            return new SwingShapedRecipe(nn, emptySlots, result);
        }

        private static List<Boolean> toBoolList(boolean[] flags) {
            List<Boolean> list = new java.util.ArrayList<>(flags.length);
            for (boolean flag : flags) list.add(flag);
            return list;
        }

        private static DataResult<SwingShapedRecipe> fromPattern(List<String> pattern, Map<String, Ingredient> key, ItemStack result) {
            NonNullList<Ingredient> nn = NonNullList.withSize(16, Ingredient.of(Items.STONE));
            boolean[] empty = new boolean[16];
            int idx = 0;

            for (int r = 0; r < 4; r++) {
                String row = pattern.get(r);
                for (int c = 0; c < 4; c++) {
                    char raw = row.charAt(c);
                    // Espace = case vide (doit rester libre dans la machine), comme en craft vanilla.
                    if (raw == ' ') {
                        empty[idx++] = true;
                        continue;
                    }
                    String ch = String.valueOf(raw);
                    Ingredient ing = key.get(ch);
                    if (ing == null) return DataResult.error(() -> "Pattern uses '" + ch + "' but key does not define it");
                    if (ing.isEmpty()) return DataResult.error(() -> "Key '" + ch + "' is empty");
                    nn.set(idx++, ing);
                }
            }
            return DataResult.success(new SwingShapedRecipe(nn, empty, result));
        }
    }
}