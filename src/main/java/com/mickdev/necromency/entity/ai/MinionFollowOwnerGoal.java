package com.mickdev.necromency.entity.ai;

import com.mickdev.necromency.entity.MinionEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

public class MinionFollowOwnerGoal extends Goal {

    private final MinionEntity minion;
    private final double speed;
    private final float stopDist;
    private final float startDist;

    public MinionFollowOwnerGoal(MinionEntity minion, double speed, float stopDist, float startDist) {
        this.minion = minion;
        this.speed = speed;
        this.stopDist = stopDist;
        this.startDist = startDist;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (minion.isMinionStaying()) return false;
        Player owner = minion.getOwnerPlayer();
        if (owner == null || !owner.isAlive()) return false;
        return minion.distanceTo(owner) > startDist;
    }

    @Override
    public boolean canContinueToUse() {
        Player owner = minion.getOwnerPlayer();
        if (owner == null || !owner.isAlive()) return false;
        return minion.distanceTo(owner) > stopDist;
    }

    @Override
    public void tick() {
        Player owner = minion.getOwnerPlayer();
        if (owner == null) return;
        minion.getLookControl().setLookAt(owner, 30.0F, 30.0F);
        minion.getNavigation().moveTo(owner, speed);
    }

    @Override
    public void stop() {
        minion.getNavigation().stop();
    }
}
