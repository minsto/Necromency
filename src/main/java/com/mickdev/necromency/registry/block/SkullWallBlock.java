package com.mickdev.necromency.registry.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;

public class SkullWallBlock extends Block implements EntityBlock {

    /**
     * Comme {@code WallSkullBlock} vanilla : direction où le crâne <em>regarde</em> (vers le joueur).
     * La plaque d'obsidienne est du côté opposé ({@code facing.getOpposite()}).
     */
    public static final net.minecraft.world.level.block.state.properties.EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

    /** Plaque obsidienne (2 px), indexée par le côté mur = {@code facing.getOpposite()}. */
    private static final Map<Direction, VoxelShape> SHAPES_BY_WALL = new EnumMap<>(Direction.class);

    static {
        SHAPES_BY_WALL.put(Direction.NORTH, Block.box(0, 0, 0, 16, 16, 2));
        SHAPES_BY_WALL.put(Direction.SOUTH, Block.box(0, 0, 14, 16, 16, 16));
        SHAPES_BY_WALL.put(Direction.EAST, Block.box(14, 0, 0, 16, 16, 16));
        SHAPES_BY_WALL.put(Direction.WEST, Block.box(0, 0, 0, 2, 16, 16));
    }

    public SkullWallBlock(Properties properties) {
        super(properties.strength(50.0F, 2000.0F).sound(SoundType.STONE).noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        // Comme WallSkullBlock vanilla : mur solide dans {@code direction}, crâne regarde {@code direction.getOpposite()}.
        BlockState state = defaultBlockState();
        BlockGetter level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        for (Direction direction : ctx.getNearestLookingDirections()) {
            if (direction.getAxis().isHorizontal()) {
                state = state.setValue(FACING, direction.getOpposite());
                if (!level.getBlockState(pos.relative(direction)).canBeReplaced(ctx)) {
                    return state;
                }
            }
        }
        return null;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    private static VoxelShape shapeFor(BlockState state) {
        Direction wallSide = state.getValue(FACING).getOpposite();
        return SHAPES_BY_WALL.getOrDefault(wallSide, SHAPES_BY_WALL.get(Direction.NORTH));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state);
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return shapeFor(state);
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SkullWallBlockEntity(pos, state);
    }
}
