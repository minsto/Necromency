package com.mickdev.necromency.registry.init;

import com.mickdev.necromency.Necromency;
import com.mickdev.necromency.worldgen.CemeteryFeature;
import com.mickdev.necromency.worldgen.NetherChaliceFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, Necromency.MODID);

    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> NETHER_CHALICE =
            FEATURES.register("nether_chalice", () -> new NetherChaliceFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> CEMETERY =
            FEATURES.register("cemetery", () -> new CemeteryFeature(NoneFeatureConfiguration.CODEC));

    private ModFeatures() {}
}
