package com.mickdev.necromency.worldgen;

import com.mickdev.necromency.registry.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;

/** Port simplifié mais fidèle de {@code WorldGenNetherChalice} (1.7.10). */
public final class NetherChalicePlacer {

    private NetherChalicePlacer() {}

    public static void place(WorldGenLevel level, BlockPos lava, net.minecraft.util.RandomSource random) {
        int i = lava.getX();
        int j = lava.getY();
        int k = lava.getZ();

        BlockState blood = ModBlocks.BLOOD.get().defaultBlockState();
        BlockState brick = Blocks.NETHER_BRICKS.defaultBlockState();
        BlockState fence = Blocks.NETHER_BRICK_FENCE.defaultBlockState();
        BlockState bars = Blocks.IRON_BARS.defaultBlockState();
        BlockState torch = Blocks.REDSTONE_TORCH.defaultBlockState();

        // colonne de sang centrale (tige)
        for (int y = 0; y <= 18; y++) {
            set(level, i + 3, j + y, k + 3, blood);
        }

        // barreaux autour de la tige
        for (int y = 0; y <= 17; y++) {
            set(level, i + 2, j + y, k + 2, bars);
            set(level, i + 2, j + y, k + 4, bars);
            set(level, i + 4, j + y, k + 2, bars);
            set(level, i + 4, j + y, k + 4, bars);
            set(level, i + 3, j + y, k + 2, bars);
            set(level, i + 3, j + y, k + 4, bars);
            set(level, i + 2, j + y, k + 3, bars);
            set(level, i + 4, j + y, k + 3, bars);
        }

        // bol (7×7) en briques du nether
        for (int dx = 0; dx <= 6; dx++) {
            for (int dz = 0; dz <= 6; dz++) {
                boolean rim = dx == 0 || dx == 6 || dz == 0 || dz == 6;
                if (rim) {
                    set(level, i + dx, j + 19, k + dz, fence);
                    set(level, i + dx, j + 21, k + dz, fence);
                } else {
                    set(level, i + dx, j + 19, k + dz, brick);
                }
                set(level, i + dx, j + 20, k + dz, brick);
            }
        }

        // coupe intérieure remplie de sang
        for (int dx = 1; dx <= 5; dx++) {
            for (int dz = 1; dz <= 5; dz++) {
                set(level, i + dx, j + 20, k + dz, blood);
            }
        }

        // escaliers aux coins
        setStair(level, i + 0, j + 19, k + 2, Direction.WEST, Half.BOTTOM);
        setStair(level, i + 6, j + 19, k + 4, Direction.EAST, Half.BOTTOM);
        setStair(level, i + 2, j + 19, k + 0, Direction.NORTH, Half.BOTTOM);
        setStair(level, i + 4, j + 19, k + 6, Direction.SOUTH, Half.BOTTOM);

        // torches rouges
        set(level, i + 0, j + 22, k + 1, Blocks.REDSTONE_TORCH.defaultBlockState());
        set(level, i + 6, j + 22, k + 5, Blocks.REDSTONE_TORCH.defaultBlockState());
    }

    private static void setStair(WorldGenLevel level, int x, int y, int z, Direction facing, Half half) {
        BlockState stair = Blocks.NETHER_BRICK_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, facing)
                .setValue(StairBlock.HALF, half);
        set(level, x, y, z, stair);
    }

    private static void set(WorldGenLevel level, int x, int y, int z, BlockState state) {
        BlockPos pos = new BlockPos(x, y, z);
        if (level.isEmptyBlock(pos) || level.getBlockState(pos).getBlock() == Blocks.LAVA) {
            level.setBlock(pos, state, 2);
        }
    }
}
