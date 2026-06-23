package com.mickdev.necromency.entity.ai;

import com.mickdev.necromency.entity.MinionEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/** Bloque déplacement / cibles tant que le propriétaire a ordonné « rester ». */
public class MinionStayWhenOrderedGoal extends Goal {

    private final MinionEntity minion;

    public MinionStayWhenOrderedGoal(MinionEntity minion) {
        this.minion = minion;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.TARGET, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return minion.isMinionStaying();
    }

    @Override
    public boolean canContinueToUse() {
        return minion.isMinionStaying();
    }

    @Override
    public void start() {
        minion.getNavigation().stop();
        minion.setTarget(null);
    }

    @Override
    public void tick() {
        minion.getNavigation().stop();
    }
}
