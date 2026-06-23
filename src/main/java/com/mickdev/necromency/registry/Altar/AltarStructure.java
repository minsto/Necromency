package com.mickdev.necromency.registry.Altar;

import com.mickdev.necromency.registry.Altar.Block.ALTARBlock;
import com.mickdev.necromency.registry.Altar.Block.ALTARBlockEntity;
import com.mickdev.necromency.registry.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Repère le centre d'autel (3 blocs) et liste toutes les positions. */
public final class AltarStructure {

    private AltarStructure() {}

    public static Optional<BlockPos> findCenter(Level level, BlockPos pos) {
        BlockState here = level.getBlockState(pos);
        if (here.is(ModBlocks.ALTAR.get())) {
            return Optional.of(pos);
        }
        if (!here.is(ModBlocks.ALTAR_BUILDING.get())) {
            return Optional.empty();
        }
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos one = pos.relative(dir);
            if (level.getBlockState(one).is(ModBlocks.ALTAR.get())) {
                return Optional.of(one);
            }
            BlockPos two = pos.relative(dir, 2);
            if (level.getBlockState(two).is(ModBlocks.ALTAR.get())) {
                return Optional.of(two);
            }
        }
        return Optional.empty();
    }

    public static List<BlockPos> allParts(Level level, BlockPos center) {
        List<BlockPos> parts = new ArrayList<>(3);
        parts.add(center);
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos ext1 = center.relative(dir);
            BlockPos ext2 = center.relative(dir, 2);
            if (level.getBlockState(ext1).is(ModBlocks.ALTAR_BUILDING.get())
                    && level.getBlockState(ext2).is(ModBlocks.ALTAR_BUILDING.get())) {
                parts.add(ext1);
                parts.add(ext2);
                return parts;
            }
        }
        return parts;
    }

    public static Optional<ALTARBlockEntity> altarEntity(Level level, BlockPos anyPart) {
        return findCenter(level, anyPart)
                .map(center -> level.getBlockEntity(center))
                .filter(ALTARBlockEntity.class::isInstance)
                .map(ALTARBlockEntity.class::cast);
    }

    public static void destroyAll(Level level, BlockPos anyPart) {
        findCenter(level, anyPart).ifPresent(center -> {
            if (CASCADE.get()) return;
            CASCADE.set(true);
            try {
                for (BlockPos p : allParts(level, center)) {
                    level.removeBlock(p, false);
                }
            } finally {
                CASCADE.set(false);
            }
        });
    }

    private static final ThreadLocal<Boolean> CASCADE = ThreadLocal.withInitial(() -> false);

    /** Casse les autres blocs du multibloc quand l'un est détruit (port 1.7.10). */
    public static void cascadeRemove(Level level, BlockPos brokenPos) {
        if (CASCADE.get()) return;
        findCenter(level, brokenPos).ifPresent(center -> {
            CASCADE.set(true);
            try {
                for (BlockPos part : allParts(level, center)) {
                    if (!part.equals(brokenPos)) {
                        var partState = level.getBlockState(part);
                        if (partState.is(com.mickdev.necromency.registry.init.ModBlocks.ALTAR.get())
                                || partState.is(com.mickdev.necromency.registry.init.ModBlocks.ALTAR_BUILDING.get())) {
                            level.removeBlock(part, false);
                        }
                    }
                }
            } finally {
                CASCADE.set(false);
            }
        });
    }
}
