package com.mickdev.necromency.registry.data;

import com.mickdev.necromency.registry.init.ModItems;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

/** Mobs portés depuis les {@code NecroEntity*} 1.7.10 + extensions 1.21. */
public final class NecroMobCatalog {

    private static final List<NecroMobDefinition> ALL = List.of(
            NecroMobDefinition.uniform("minecraft:zombie", Ingredient.of(Items.ROTTEN_FLESH)),
            NecroMobDefinition.uniform("minecraft:skeleton", Ingredient.of(Items.BONE)),
            NecroMobDefinition.uniform("minecraft:creeper", Ingredient.of(Items.GUNPOWDER), false),
            NecroMobDefinition.uniform("minecraft:spider", Ingredient.of(Items.SPIDER_EYE), false),
            NecroMobDefinition.uniform("minecraft:cave_spider", Ingredient.of(Items.STRING), false),
            NecroMobDefinition.uniform("minecraft:enderman", Ingredient.of(Items.ENDER_PEARL)),
            NecroMobDefinition.uniform("minecraft:witch", Ingredient.of(Items.POISONOUS_POTATO)),
            NecroMobDefinition.uniform("minecraft:chicken", Ingredient.of(Items.CHICKEN)),
            NecroMobDefinition.uniform("minecraft:pig", Ingredient.of(Items.PORKCHOP)),
            NecroMobDefinition.uniform("minecraft:cow", Ingredient.of(Items.BEEF)),
            NecroMobDefinition.uniform("minecraft:sheep", Ingredient.of(Items.WHITE_WOOL)),
            NecroMobDefinition.uniform("minecraft:squid", Ingredient.of(Items.INK_SAC), false),
            NecroMobDefinition.uniform("minecraft:villager", Ingredient.of(Items.BOOK)),
            NecroMobDefinition.uniform("minecraft:wolf", Ingredient.of(Items.BONE)),
            NecroMobDefinition.uniform("minecraft:zombified_piglin", Ingredient.of(Items.COOKED_BEEF)),
            new NecroMobDefinition(
                    "minecraft:iron_golem",
                    Ingredient.of(Blocks.CARVED_PUMPKIN),
                    Ingredient.of(Blocks.IRON_BLOCK),
                    Ingredient.of(Blocks.IRON_BLOCK),
                    Ingredient.of(Blocks.IRON_BLOCK),
                    true, true, true, true
            ),
            NecroMobDefinition.uniform("minecraft:copper_golem", Ingredient.of(Items.COPPER_INGOT)),
            NecroMobDefinition.uniform("minecraft:axolotl", Ingredient.of(Items.TROPICAL_FISH)),
            NecroMobDefinition.uniform("minecraft:goat", Ingredient.of(Items.GOAT_HORN)),
            NecroMobDefinition.uniform("minecraft:sniffer", Ingredient.of(Items.SNIFFER_EGG)),
            NecroMobDefinition.uniform("minecraft:turtle", Ingredient.of(Items.TURTLE_SCUTE)),
            NecroMobDefinition.uniform("minecraft:fox", Ingredient.of(Items.SWEET_BERRIES)),
            NecroMobDefinition.uniform("minecraft:cat", Ingredient.of(Items.COD)),
            NecroMobDefinition.uniform("minecraft:ghast", Ingredient.of(Items.GHAST_TEAR), false),
            NecroMobDefinition.uniform("minecraft:happy_ghast", Ingredient.of(Items.SNOWBALL), false),
            NecroMobDefinition.uniform("minecraft:piglin", Ingredient.of(Items.GOLD_INGOT)),
            NecroMobDefinition.uniform("minecraft:warden", Ingredient.of(Items.ECHO_SHARD)),
            NecroMobDefinition.uniform("minecraft:pillager", Ingredient.of(Items.CROSSBOW)),
            NecroMobDefinition.uniform("minecraft:wandering_trader", Ingredient.of(Items.EMERALD)),
            NecroMobDefinition.uniform("minecraft:illusioner", Ingredient.of(Items.SPECTRAL_ARROW)),
            NecroMobDefinition.uniform("minecraft:armadillo", Ingredient.of(Items.ARMADILLO_SCUTE))
    );

    private NecroMobCatalog() {}

    public static List<NecroMobDefinition> all() {
        return ALL;
    }
}
