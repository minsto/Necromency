package com.mickdev.necromency.registry.init.Fluid;

import com.mickdev.necromency.registry.init.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class BloodLiquidBlock extends LiquidBlock {

    // Couleur rouge (RGB) + taille (scale)
    private static final DustParticleOptions RED_DUST =
            new DustParticleOptions(0xFF0000, 1.0F);

    public BloodLiquidBlock(BlockBehaviour.Properties props) {
        super(ModFluids.BLOOD_SOURCE.get(), props);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        if (rand.nextInt(4) != 0) return;

        double x = pos.getX() + rand.nextDouble();
        double y = pos.getY() + 0.85 + rand.nextDouble() * 0.05;
        double z = pos.getZ() + rand.nextDouble();

        level.addParticle(RED_DUST, x, y, z, 0.0, 0.01, 0.0);
    }
}