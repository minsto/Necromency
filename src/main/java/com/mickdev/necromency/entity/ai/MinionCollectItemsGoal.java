package com.mickdev.necromency.entity.ai;

import com.mickdev.necromency.entity.MinionEntity;
import com.mickdev.necromency.registry.BrainMaker.BrainTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

/**
 * Cerveau allay ({@code necromency:collector}) : ramasse les items au sol (sans les ranger en coffre —
 * voir {@link MinionOrganizeChestGoal} pour le copper golem / fusion organizer+collector).
 */
public class MinionCollectItemsGoal extends Goal {

    private final MinionEntity minion;
    private int cooldown;

    public MinionCollectItemsGoal(MinionEntity minion) {
        this.minion = minion;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!minion.hasBrainAttribute(BrainTypes.ATTR_COLLECTOR)) return false;
        if (minion.hasBrainAttribute(BrainTypes.ATTR_ORGANIZER)) return false;
        if (minion.isMinionStaying()) return false;
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        if (!minion.getMainHandItem().isEmpty()) return false;
        return findNearbyDrop() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void tick() {
        ItemEntity pick = findNearbyDrop();
        if (pick == null) {
            cooldown = 20;
            return;
        }
        minion.getNavigation().moveTo(pick, 1.0);
        minion.getLookControl().setLookAt(pick, 30.0F, minion.getMaxHeadXRot());
        if (minion.distanceToSqr(pick) < 2.5) {
            ItemStack stack = pick.getItem();
            if (!stack.isEmpty()) {
                minion.setItemInHand(InteractionHand.MAIN_HAND, stack.copy());
                pick.discard();
                cooldown = 10;
            }
        }
    }

    private ItemEntity findNearbyDrop() {
        AABB box = minion.getBoundingBox().inflate(8.0, 2.0, 8.0);
        List<ItemEntity> drops = minion.level().getEntitiesOfClass(ItemEntity.class, box,
                e -> !e.getItem().isEmpty() && e.isAlive());
        return drops.isEmpty() ? null : drops.get(0);
    }
}
