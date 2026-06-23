package com.mickdev.necromency.registry.Altar.Block;

import com.mickdev.necromency.registry.Altar.AltarStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class ALTARBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

    public ALTARBlock(BlockBehaviour.Properties properties) {
        super(properties.sound(SoundType.GRAVEL).strength(1f, 10f).noOcclusion()
                .pushReaction(PushReaction.IGNORE)
                .isRedstoneConductor((bs, br, bp) -> false)
                .instrument(NoteBlockInstrument.BASS));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override public boolean propagatesSkylightDown(BlockState state) { return true; }
    @Override public int getLightBlock(BlockState state) { return 0; }
    @Override public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) { return Shapes.empty(); }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) {
        return switch (state.getValue(FACING)) {
            default -> box(0, 0, -16, 16, 16, 32);
            case NORTH -> box(0, 0, -16, 16, 16, 32);
            case EAST -> box(-16, 0, 0, 32, 16, 16);
            case WEST -> box(-16, 0, 0, 32, 16, 16);
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    // Clic sans item
    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return handleUse(state, level, pos, player, ItemStack.EMPTY, InteractionHand.MAIN_HAND, hit);
    }

    // Clic avec item (arme, etc.) -> IMPORTANT pour le shift+clic avec arme
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
        return handleUse(state, level, pos, player, stack, hand, hit);
    }

    private InteractionResult handleUse(BlockState state, Level level, BlockPos pos,
                                        Player player, ItemStack held, InteractionHand hand, BlockHitResult hit) {

        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ALTARBlockEntity altar)) return InteractionResult.PASS;

        // SHIFT + clic -> rituel (même si tu tiens une arme)
        if (player.isShiftKeyDown()) {
            altar.debugSlots(sp);

            // tryBuildAbomination affiche déjà le slot précis qui pose problème
            // (ex. "slot 0 blood vide"). Pas de message générique qui écrase et induit en erreur.
            boolean ok = altar.tryBuildAbomination(sp);
            if (ok) {
                sp.displayClientMessage(net.minecraft.network.chat.Component.literal("ALTAR: rituel reussi!"), true);
            }
            return InteractionResult.CONSUME;
        }

        // clic normal -> GUI
        sp.openMenu(state.getMenuProvider(level, pos), buf -> buf.writeBlockPos(pos));
        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public ALTARBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ALTARBlockEntity(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return super.getStateForPlacement(ctx).setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        BlockEntity te = level.getBlockEntity(pos);
        return te instanceof MenuProvider mp ? mp : null;
    }

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int eventID, int eventParam) {
        super.triggerEvent(state, level, pos, eventID, eventParam);
        BlockEntity be = level.getBlockEntity(pos);
        return be != null && be.triggerEvent(eventID, eventParam);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean flag) {
        Containers.updateNeighboursAfterDestroy(state, level, pos);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            AltarStructure.cascadeRemove(level, pos);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }
}