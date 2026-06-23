package com.mickdev.necromency.registry.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class IsaacsHeadItem extends Item {
    public IsaacsHeadItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {

            // empêcher la mort par usage
            if (player.getHealth() <= 1.0F) {
                return InteractionResult.FAIL;
            }

            // coût : 1/2 coeur = 1 HP
            player.hurt(level.damageSources().magic(), 1.0F);

            TearProjectile tear = new TearProjectile(level, player);
            tear.shootFromRotation(
                    player,
                    player.getXRot(),
                    player.getYRot(),
                    0.0F,
                    1.6F,
                    0.5F
            );
            level.addFreshEntity(tear);

            player.getCooldowns().addCooldown(stack, 6);
        }

        return InteractionResult.SUCCESS;
    }
}