package com.mickdev.necromency;

import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs


public final class NecromencyConfig {
    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue RENDER_SPECIAL_SCYTHE = B
            .comment("Special scythes (only for a select number of people)")
            .define("other.renderSpecialScythe", true);

    public static final ModConfigSpec.IntValue RARITY_NIGHTCRAWLERS = B
            .comment("Randomly, one in THIS many Zombies will spawn as Nightcrawler instead")
            .defineInRange("spawns.rarityNightcrawlers", 30, 1, 1_000_000);

    public static final ModConfigSpec.IntValue RARITY_ISAACS = B
            .comment("Randomly, one in THIS many Skeletons will spawn as Isaac instead")
            .defineInRange("spawns.rarityIsaacs", 30, 1, 1_000_000);

    public static final ModConfigSpec.IntValue ORGAN_DROP_ROLL_MAX = B
            .comment("Organ drop table rolls 0..(max-1); lower max = more frequent organs (legacy used 100)")
            .defineInRange("drops.organRollMax", 100, 10, 10_000);

    public static final ModConfigSpec.ConfigValue<List<? extends String>> SPECIAL_SCYTHE_PLAYERS = B
            .comment("Player names (case-insensitive) that see the special scythe model when renderSpecialScythe is true")
            .defineList("other.specialScythePlayers", List.of(), o -> o instanceof String);

    /** -1 = illimité ; sinon nombre max de minions vivants par joueur (autel). */
    public static final ModConfigSpec.IntValue MAX_MINION_SPAWN = B
            .comment("Max minions per player (-1 = unlimited). server.properties max_minion_spawn overrides if set.")
            .defineInRange("minion.maxSpawn", -1, -1, 256);

    public static final ModConfigSpec SPEC = B.build();

    private NecromencyConfig() {}
}

