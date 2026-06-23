package com.mickdev.necromency.registry.block;

import com.mickdev.necromency.registry.Altar.AltarStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Bloc d'extension d'autel (port {@code BlockAltarBlock}). */
public class AltarBuildingBlock extends Block {

    public AltarBuildingBlock(Properties properties) {
        super(properties.strength(4.0F, 10.0F).sound(SoundType.STONE).requiresCorrectToolForDrops());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        var altar = AltarStructure.altarEntity(level, pos);
        if (altar.isEmpty()) {
            return InteractionResult.PASS;
        }
        if (player instanceof ServerPlayer sp) {
            sp.openMenu(altar.get(), buf -> buf.writeBlockPos(altar.get().getBlockPos()));
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            AltarStructure.cascadeRemove(level, pos);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
