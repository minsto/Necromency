package com.mickdev.necromency.registry.item;

import com.mickdev.necromency.registry.NecromencyEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class NecroSpawnerItem extends Item {

    public NecroSpawnerItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ServerLevel server = (ServerLevel) level;
        var types = new net.minecraft.world.entity.EntityType<?>[] {
                NecromencyEntities.ISAAC_NORMAL.get(),
                NecromencyEntities.TEDDY.get(),
                NecromencyEntities.NIGHTCRAWLER.get()
        };
        var type = types[server.random.nextInt(types.length)];
        Entity entity = type.create(server, EntitySpawnReason.SPAWN_ITEM_USE);
        if (entity == null) {
            return InteractionResult.FAIL;
        }

        var pos = ctx.getClickedPos().relative(ctx.getClickedFace());
        entity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        entity.setYRot(server.random.nextFloat() * 360.0F);
        server.addFreshEntity(entity);

        if (ctx.getPlayer() == null || !ctx.getPlayer().isCreative()) {
            ctx.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }
}
