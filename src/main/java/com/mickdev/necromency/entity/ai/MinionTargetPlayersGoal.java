package com.mickdev.necromency.entity.ai;

import com.mickdev.necromency.entity.MinionEntity;
import com.mickdev.necromency.entity.MinionPlayerData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/**
 * Cible les joueurs selon {@link MinionPlayerData} (ami / ennemi / mode agressif), comme 1.12.
 */
public class MinionTargetPlayersGoal extends TargetGoal {

    private final MinionEntity minion;

    public MinionTargetPlayersGoal(MinionEntity minion) {
        super(minion, false);
        this.minion = minion;
    }

    @Override
    public boolean canUse() {
        if (minion.isMinionStaying()) return false;

        Player owner = minion.getOwnerPlayer();
        if (owner == null) return false;

        List<Player> players = minion.level().getEntitiesOfClass(
                Player.class,
                minion.getBoundingBox().inflate(10.0, 4.0, 10.0),
                p -> p.isAlive() && MinionPlayerData.shouldMinionAttackPlayer(owner, p)
        );

        if (players.isEmpty()) return false;

        minion.setTarget(players.get(minion.getRandom().nextInt(players.size())));
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity t = minion.getTarget();
        if (!(t instanceof Player player)) return false;
        Player owner = minion.getOwnerPlayer();
        return owner != null && player.isAlive() && MinionPlayerData.shouldMinionAttackPlayer(owner, player);
    }
}
