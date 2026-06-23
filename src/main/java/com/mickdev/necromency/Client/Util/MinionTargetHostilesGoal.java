package com.mickdev.necromency.Client.Util;

import com.mickdev.necromency.entity.MinionEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class MinionTargetHostilesGoal extends TargetGoal {

    private final MinionEntity minion;

    public MinionTargetHostilesGoal(MinionEntity minion) {
        super(minion, false);
        this.minion = minion;
    }

    @Override
    public boolean canUse() {

        // ✅ Owner
        Player owner = minion.getOwnerPlayer();

        // =====================================================
        // 1) Si owner est attaqué → cible l'attaquant
        // =====================================================
        if (owner != null) {
            LivingEntity attacker = owner.getLastHurtByMob();

            if (attacker instanceof Monster monster && monster.isAlive()) {
                minion.setTarget(monster);
                return true;
            }

            // =====================================================
            // 2) Si owner attaque un hostile → aide
            // =====================================================
            LivingEntity ownersTarget = owner.getLastHurtMob();

            if (ownersTarget instanceof Monster monster && monster.isAlive()) {
                minion.setTarget(monster);
                return true;
            }
        }

        // =====================================================
        // 3) Sinon → cherche un Monster proche
        // =====================================================

        List<Monster> monsters = minion.level().getEntitiesOfClass(
                Monster.class,
                minion.getBoundingBox().inflate(12.0),
                m -> m.isAlive() && !(m instanceof MinionEntity)
        );

        if (!monsters.isEmpty()) {
            minion.setTarget(monsters.get(0));
            return true;
        }

        return false;
    }
}