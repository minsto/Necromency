package com.mickdev.necromency.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class CemeteryFeature extends Feature<NoneFeatureConfiguration> {

    public CemeteryFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        BlockPos origin = ctx.origin();
        if (!level.getBlockState(origin.below()).isSolidRender()) {
            return false;
        }
        if (!level.isEmptyBlock(origin) && level.getBlockState(origin).getBlock() != Blocks.GRASS_BLOCK) {
            return false;
        }
        CemeteryPlacer.place(level, origin, ctx.random());
        return true;
    }
}
