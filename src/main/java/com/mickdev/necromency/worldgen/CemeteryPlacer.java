package com.mickdev.necromency.worldgen;

import com.mickdev.necromency.registry.NecromancerSpawns;
import com.mickdev.necromency.registry.data.NecroMobCatalog;
import com.mickdev.necromency.registry.init.ModBlocks;
import com.mickdev.necromency.registry.item.MobPart.BodyPartItem;
import com.mickdev.necromency.registry.item.MobPart.BodyPartStacks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** Port simplifié de {@code ComponentVillageCemetery} (1.7.10). */
public final class CemeteryPlacer {

    private CemeteryPlacer() {}

    public static void place(WorldGenLevel level, BlockPos origin, RandomSource random) {
        int ox = origin.getX();
        int oy = origin.getY();
        int oz = origin.getZ();

        // sol
        for (int x = 0; x <= 17; x++) {
            for (int z = 0; z <= 18; z++) {
                set(level, ox + x, oy, oz + z, Blocks.GRASS_BLOCK.defaultBlockState());
            }
        }

        // murs
        for (int x = 0; x <= 17; x++) {
            set(level, ox + x, oy + 1, oz, Blocks.COBBLESTONE_WALL.defaultBlockState());
            set(level, ox + x, oy + 1, oz + 17, Blocks.COBBLESTONE_WALL.defaultBlockState());
        }
        for (int z = 0; z <= 17; z++) {
            set(level, ox, oy + 1, oz + z, Blocks.COBBLESTONE_WALL.defaultBlockState());
            set(level, ox + 17, oy + 1, oz + z, Blocks.COBBLESTONE_WALL.defaultBlockState());
        }

        // tombes + sable des âmes
        for (int i = 0; i < 4; i++) {
            set(level, ox + 3, oy + 1, oz + 2 + i * 2, Blocks.COBBLESTONE.defaultBlockState());
            set(level, ox + 4, oy, oz + 2 + i * 2, Blocks.SOUL_SAND.defaultBlockState());
            set(level, ox + 5, oy, oz + 2 + i * 2, Blocks.SOUL_SAND.defaultBlockState());
            set(level, ox + 13, oy + 1, oz + 2 + i * 2, Blocks.COBBLESTONE.defaultBlockState());
            set(level, ox + 11, oy, oz + 2 + i * 2, Blocks.SOUL_SAND.defaultBlockState());
            set(level, ox + 12, oy, oz + 2 + i * 2, Blocks.SOUL_SAND.defaultBlockState());
            if (random.nextInt(10) == 0) {
                placeBodyPartChest(level, new BlockPos(ox + 3, oy, oz + 2 + i * 2), random);
            }
            if (random.nextInt(10) == 0) {
                placeBodyPartChest(level, new BlockPos(ox + 13, oy, oz + 2 + i * 2), random);
            }
        }

        // chemin
        for (int z = 0; z <= 14; z++) {
            for (int x = 7; x <= 9; x++) {
                set(level, ox + x, oy, oz + z, Blocks.GRAVEL.defaultBlockState());
            }
        }

        // tombe centrale
        for (int x = 3; x <= 5; x++) {
            for (int z = 12; z <= 14; z++) {
                set(level, ox + x, oy, oz + z, Blocks.COBBLESTONE.defaultBlockState());
                set(level, ox + x, oy + 4, oz + z, Blocks.COBBLESTONE.defaultBlockState());
            }
        }
        for (int y = 1; y <= 3; y++) {
            for (int z = 12; z <= 14; z++) {
                set(level, ox + 2, oy + y, oz + z, Blocks.COBBLESTONE.defaultBlockState());
                set(level, ox + 6, oy + y, oz + z, Blocks.COBBLESTONE.defaultBlockState());
            }
        }
        placeBodyPartChest(level, new BlockPos(ox + 3, oy + 1, oz + 12), random);
        placeBodyPartChest(level, new BlockPos(ox + 3, oy + 1, oz + 14), random);

        // maison du nécromancien (planches)
        for (int x = 10; x <= 14; x++) {
            for (int z = 11; z <= 15; z++) {
                set(level, ox + x, oy, oz + z, Blocks.COBBLESTONE.defaultBlockState());
            }
        }
        for (int y = 1; y <= 3; y++) {
            for (int x = 10; x <= 14; x++) {
                set(level, ox + x, oy + y, oz + 11, Blocks.OAK_PLANKS.defaultBlockState());
                set(level, ox + x, oy + y, oz + 15, Blocks.OAK_PLANKS.defaultBlockState());
            }
            for (int z = 11; z <= 15; z++) {
                set(level, ox + 10, oy + y, oz + z, Blocks.OAK_PLANKS.defaultBlockState());
                set(level, ox + 14, oy + y, oz + z, Blocks.OAK_PLANKS.defaultBlockState());
            }
        }
        for (int x = 10; x <= 14; x++) {
            for (int z = 11; z <= 15; z++) {
                set(level, ox + x, oy + 4, oz + z, Blocks.OAK_LOG.defaultBlockState());
            }
        }

        // machine à coudre dans la maison
        set(level, ox + 12, oy + 1, oz + 13, ModBlocks.SWING.get().defaultBlockState());

        spawnNecromancer(level, new BlockPos(ox + 12, oy + 1, oz + 12));
    }

    private static void spawnNecromancer(WorldGenLevel level, BlockPos pos) {
        if (level instanceof ServerLevel server) {
            NecromancerSpawns.spawn(server, pos);
        }
    }

    private static void placeBodyPartChest(WorldGenLevel level, BlockPos pos, RandomSource random) {
        BlockState chest = Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, BlockStateProperties.HORIZONTAL_FACING.getPossibleValues().get(random.nextInt(4)));
        set(level, pos.getX(), pos.getY(), pos.getZ(), chest);
        if (level.getBlockEntity(pos) instanceof ChestBlockEntity chestBe) {
            var mobs = NecroMobCatalog.all();
            var mob = mobs.get(random.nextInt(mobs.size()));
            BodyPartItem.PartType[] parts = BodyPartItem.PartType.values();
            for (int slot = 0; slot < Math.min(5, parts.length); slot++) {
                chestBe.setItem(slot, BodyPartStacks.create(parts[slot], mob.mobId()));
            }
        }
    }

    private static void set(WorldGenLevel level, int x, int y, int z, BlockState state) {
        BlockPos pos = new BlockPos(x, y, z);
        if (level.getBlockState(pos).isAir() || level.getBlockState(pos).is(Blocks.GRASS_BLOCK)) {
            level.setBlock(pos, state, 2);
        }
    }
}
