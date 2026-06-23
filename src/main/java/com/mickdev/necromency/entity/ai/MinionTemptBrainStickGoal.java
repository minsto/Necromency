package com.mickdev.necromency.entity.ai;

import com.mickdev.necromency.entity.MinionEntity;
import com.mickdev.necromency.registry.item.BrainOnAStickItem;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

/** Suit le propriétaire s'il tient un brain on a stick (1.12 {@code EntityAITempt}). */
public class MinionTemptBrainStickGoal extends Goal {

    private final MinionEntity minion;
    private final double speed;

    public MinionTemptBrainStickGoal(MinionEntity minion, double speed) {
        this.minion = minion;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private static boolean holdsBrainStick(Player player) {
        return BrainOnAStickItem.isBrainOnAStick(player.getMainHandItem())
                || BrainOnAStickItem.isBrainOnAStick(player.getOffhandItem());
    }

    @Override
    public boolean canUse() {
        if (minion.isMinionStaying()) return false;
        Player owner = minion.getOwnerPlayer();
        return owner != null && holdsBrainStick(owner) && minion.distanceTo(owner) > 2.5;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void tick() {
        Player owner = minion.getOwnerPlayer();
        if (owner == null) return;
        minion.getLookControl().setLookAt(owner, 30.0F, 30.0F);
        minion.getNavigation().moveTo(owner, speed);
    }
}
