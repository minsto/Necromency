package com.mickdev.necromency.registry.item;

import com.mickdev.necromency.registry.NecromancerSpawns;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** Invoque un villageois nécromancien (échanges morceaux / Nécronomicon). */
public class NecromancerSpawnItem extends Item {

    public NecromancerSpawnItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ServerLevel server = (ServerLevel) level;
        var pos = ctx.getClickedPos().relative(ctx.getClickedFace());
        if (NecromancerSpawns.spawn(server, pos) == null) {
            return InteractionResult.FAIL;
        }

        if (ctx.getPlayer() == null || !ctx.getPlayer().isCreative()) {
            ctx.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }
}
