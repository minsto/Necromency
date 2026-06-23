package com.mickdev.necromency.registry.block;

import com.mickdev.necromency.registry.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** Blocs décoratifs (assets portés, pas dans le 1.7.10 d'origine). */
public final class DecorativeBlocks {

    private DecorativeBlocks() {}

    public static class AltarObsidianBlock extends Block {
        public AltarObsidianBlock(Properties properties) {
            super(properties);
        }
    }

    public static class FlameTowerBlock extends Block {
        public FlameTowerBlock(Properties properties) {
            super(properties.lightLevel(state -> 14).noOcclusion());
        }

        /** Repose une flamme d'âme au sommet si l'emplacement est libre. */
        private static void ensureSoulFlameAbove(Level world, BlockPos towerPos) {
            if (world.isClientSide()) {
                return;
            }
            BlockPos above = towerPos.above();
            BlockState aboveState = world.getBlockState(above);
            if (aboveState.is(ModBlocks.NECRO_SOUL_FLAME.get())) {
                return;
            }
            if (aboveState.canBeReplaced()) {
                world.setBlock(above, ModBlocks.NECRO_SOUL_FLAME.get().defaultBlockState(), 3);
            }
        }

        /** À la pose, allume une flamme d'âme juste au-dessus (porté de MCreator). */
        @Override
        public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
            super.setPlacedBy(world, pos, state, entity, stack);
            ensureSoulFlameAbove(world, pos);
        }

        @Override
        public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean moving) {
            super.onPlace(state, world, pos, oldState, moving);
            ensureSoulFlameAbove(world, pos);
        }

        /** Si la flamme est cassée ou retirée, la remettre immédiatement. */
        @Override
        public void neighborChanged(BlockState state, Level world, BlockPos pos, Block neighborBlock,
                                    @Nullable Orientation orientation, boolean moving) {
            super.neighborChanged(state, world, pos, neighborBlock, orientation, moving);
            ensureSoulFlameAbove(world, pos);
        }

        @Override
        public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
            if (random.nextInt(3) == 0) {
                level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        pos.getX() + 0.5 + random.nextGaussian() * 0.15,
                        pos.getY() + 0.9,
                        pos.getZ() + 0.5 + random.nextGaussian() * 0.15,
                        0, 0.02, 0);
            }
        }
    }

    /**
     * Flamme d'âme décorative, alignée sur la version MCreator :
     * sans collision, cutout. Sur une flametower elle se repose toujours
     * (sans drop) ; ailleurs elle tombe si le support disparaît.
     */
    public static class NecroSoulFlameBlock extends Block {
        public NecroSoulFlameBlock(Properties properties) {
            super(properties
                    .sound(SoundType.GRAVEL)
                    .instabreak()
                    .noCollision()
                    .noOcclusion()
                    .isRedstoneConductor((bs, br, bp) -> false));
        }

        @Override
        public boolean propagatesSkylightDown(BlockState state) {
            return true;
        }

        @Override
        public int getLightBlock(BlockState state) {
            return 0;
        }

        @Override
        public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
            return Shapes.empty();
        }

        /** Détruit la flamme si le support en dessous est absent (sauf flametower). */
        private static void checkSupport(Level world, BlockPos pos) {
            BlockState below = world.getBlockState(pos.below());
            if (below.is(ModBlocks.FLAME_TOWER.get())) {
                return;
            }
            if (below.isAir()) {
                world.destroyBlock(pos, false);
            }
        }

        @Override
        public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean moving) {
            super.onPlace(state, world, pos, oldState, moving);
            checkSupport(world, pos);
        }

        @Override
        public void neighborChanged(BlockState state, Level world, BlockPos pos, Block neighborBlock,
                                    @Nullable Orientation orientation, boolean moving) {
            super.neighborChanged(state, world, pos, neighborBlock, orientation, moving);
            checkSupport(world, pos);
        }

        @Override
        public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
            super.tick(state, world, pos, random);
            checkSupport(world, pos);
        }

        @Override
        public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
            super.animateTick(state, world, pos, random);
            checkSupport(world, pos);
        }

        @Override
        public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
            super.setPlacedBy(world, pos, state, entity, stack);
            checkSupport(world, pos);
        }
    }

    public static BlockBehaviour.Properties altarObsidianProps(BlockBehaviour.Properties props) {
        return props.strength(50.0F, 1200.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE);
    }

    public static BlockBehaviour.Properties flameTowerProps(BlockBehaviour.Properties props) {
        return props.strength(2.0F, 6.0F)
                .sound(SoundType.STONE);
    }

    public static BlockBehaviour.Properties soulFlameProps(BlockBehaviour.Properties props) {
        // Les propriétés (son, solidité, collision, etc.) sont appliquées dans
        // le constructeur de NecroSoulFlameBlock pour coller à la version MCreator.
        return props;
    }
}
