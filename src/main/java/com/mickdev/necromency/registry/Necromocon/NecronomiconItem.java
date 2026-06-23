package com.mickdev.necromency.registry.Necromocon;

import com.mickdev.necromency.registry.Altar.Block.ALTARBlock;
import com.mickdev.necromency.registry.NecromencyAdvancements;
import com.mickdev.necromency.registry.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port 1.7.10 : clic sur planches avec cobble-cobble dans une direction → autel + 2 blocs d'autel.
 */
public class NecronomiconItem extends Item {

    public NecronomiconItem(Properties props) {
        super(props.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos clicked = ctx.getClickedPos();
        BlockState clickedState = level.getBlockState(clicked);

        if (!clickedState.is(BlockTags.PLANKS)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos c1 = clicked.relative(dir);
            BlockPos c2 = c1.relative(dir);
            if (!isCobble(level.getBlockState(c1)) || !isCobble(level.getBlockState(c2))) {
                continue;
            }

            Direction altarFacing = dir.getOpposite();
            level.setBlock(clicked, ModBlocks.ALTAR.get().defaultBlockState()
                    .setValue(ALTARBlock.FACING, altarFacing), 3);
            level.setBlock(c1, ModBlocks.ALTAR_BUILDING.get().defaultBlockState(), 3);
            level.setBlock(c2, ModBlocks.ALTAR_BUILDING.get().defaultBlockState(), 3);

            Player player = ctx.getPlayer();
            if (player != null && !player.isCreative()) {
                ctx.getItemInHand().shrink(1);
            }
            if (player instanceof ServerPlayer sp) {
                NecromencyAdvancements.grant(sp, NecromencyAdvancements.ALTAR);
            }
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    private static boolean isCobble(BlockState state) {
        return state.is(Blocks.COBBLESTONE);
    }
}
