package com.mickdev.necromency.entity.ai;

import com.mickdev.necromency.entity.MinionEntity;
import com.mickdev.necromency.registry.BrainMaker.BrainTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/**
 * Cerveau copper golem (BrainId {@code necromency:brain_copper_golem}) : ramasse des items et les met dans un coffre / baril.
 */
public class MinionOrganizeChestGoal extends Goal {

    private static final int SCAN_RANGE = 10;
    private static final double REACH = 2.75;

    private final MinionEntity minion;
    private BlockPos targetChest;
    private int cooldown;

    public MinionOrganizeChestGoal(MinionEntity minion) {
        this.minion = minion;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private boolean mobGriefingAllowed() {
        if (minion.level() instanceof ServerLevel sl) {
            return sl.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
        }
        return true;
    }

    @Override
    public boolean canUse() {
        if (!mobGriefingAllowed()) return false;
        if (!minion.hasBrainAttribute(BrainTypes.ATTR_ORGANIZER)) return false;
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        if (!minion.getMainHandItem().isEmpty()) {
            targetChest = findDepositTarget();
            return targetChest != null;
        }
        return tryPickupNearbyDrop();
    }

    @Override
    public boolean canContinueToUse() {
        if (!minion.hasBrainAttribute(BrainTypes.ATTR_ORGANIZER)) return false;
        if (!mobGriefingAllowed()) return false;
        if (!minion.getMainHandItem().isEmpty()) {
            if (targetChest == null) targetChest = findDepositTarget();
            return targetChest != null;
        }
        return tryPickupNearbyDrop();
    }

    @Override
    public void start() {
        if (targetChest == null) targetChest = findDepositTarget();
    }

    @Override
    public void stop() {
        targetChest = null;
        minion.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (!minion.hasBrainAttribute(BrainTypes.ATTR_ORGANIZER)) return;

        ItemStack hand = minion.getMainHandItem();
        if (hand.isEmpty()) {
            tryPickupNearbyDrop();
            return;
        }

        if (targetChest == null) {
            targetChest = findDepositTarget();
            if (targetChest == null) {
                cooldown = 40;
                return;
            }
        }

        Container chest = getChestLikeContainer(targetChest);
        if (chest == null) {
            targetChest = null;
            return;
        }

        Vec3 center = Vec3.atCenterOf(targetChest);
        minion.getLookControl().setLookAt(center.x, center.y, center.z, 30, minion.getMaxHeadXRot());
        double dist = minion.position().distanceTo(center);
        if (dist > REACH) {
            minion.getNavigation().moveTo(center.x, center.y, center.z, 1.0);
            return;
        }

        Direction insert = facingFromChestToEntity(targetChest, minion.position());
        ItemStack remaining = HopperBlockEntity.addItem(null, chest, hand.copy(), insert);
        minion.setItemInHand(InteractionHand.MAIN_HAND, remaining);
        chest.setChanged();
        if (remaining.isEmpty()) {
            targetChest = null;
            cooldown = 20;
        } else {
            cooldown = 10;
        }
    }

    private boolean tryPickupNearbyDrop() {
        AABB box = minion.getBoundingBox().inflate(2.0, 1.0, 2.0);
        List<ItemEntity> drops = minion.level().getEntitiesOfClass(ItemEntity.class, box, e -> !e.getItem().isEmpty() && e.isAlive());
        if (drops.isEmpty()) return false;
        ItemEntity pick = drops.get(0);
        ItemStack stack = pick.getItem();
        if (stack.isEmpty()) return false;
        minion.setItemInHand(InteractionHand.MAIN_HAND, stack.copy());
        pick.discard();
        targetChest = findDepositTarget();
        return true;
    }

    private BlockPos findDepositTarget() {
        ItemStack hand = minion.getMainHandItem();
        if (hand.isEmpty()) return null;

        BlockPos origin = minion.blockPosition();
        for (int dx = -SCAN_RANGE; dx <= SCAN_RANGE; dx++) {
            for (int dz = -SCAN_RANGE; dz <= SCAN_RANGE; dz++) {
                for (int dy = -3; dy <= 3; dy++) {
                    BlockPos p = origin.offset(dx, dy, dz);
                    if (!minion.level().isLoaded(p)) continue;
                    Container c = getChestLikeContainer(p);
                    if (c == null) continue;
                    Direction ins = facingFromChestToEntity(p, minion.position());
                    if (canAcceptSome(c, hand, ins)) return p;
                }
            }
        }
        return null;
    }

    private static boolean canAcceptSome(Container c, ItemStack stack, Direction face) {
        if (stack.isEmpty()) return false;
        if (c instanceof WorldlyContainer wc) {
            for (int slot : wc.getSlotsForFace(face)) {
                if (slotAccepts(c, stack, slot, face)) return true;
            }
            return false;
        }
        for (int i = 0; i < c.getContainerSize(); i++) {
            if (slotAccepts(c, stack, i, face)) return true;
        }
        return false;
    }

    private static boolean slotAccepts(Container c, ItemStack stack, int slot, Direction face) {
        if (!c.canPlaceItem(slot, stack)) return false;
        if (c instanceof WorldlyContainer wc && !wc.canPlaceItemThroughFace(slot, stack, face)) return false;
        ItemStack in = c.getItem(slot);
        if (in.isEmpty()) return true;
        return ItemStack.isSameItemSameComponents(in, stack) && in.getCount() < in.getMaxStackSize();
    }

    private static Direction facingFromChestToEntity(BlockPos chest, Vec3 entityPos) {
        Vec3 chestCenter = Vec3.atCenterOf(chest);
        Vec3 d = entityPos.subtract(chestCenter);
        if (Math.abs(d.y) > Math.max(Math.abs(d.x), Math.abs(d.z)) + 0.25) {
            return d.y > 0 ? Direction.UP : Direction.DOWN;
        }
        if (Math.abs(d.x) > Math.abs(d.z)) {
            return d.x > 0 ? Direction.EAST : Direction.WEST;
        }
        return d.z > 0 ? Direction.SOUTH : Direction.NORTH;
    }

    private Container getChestLikeContainer(BlockPos pos) {
        BlockState state = minion.level().getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof ChestBlock chestBlock && minion.level().getBlockEntity(pos) instanceof ChestBlockEntity) {
            return ChestBlock.getContainer(chestBlock, state, minion.level(), pos, true);
        }
        if (block instanceof BarrelBlock && minion.level().getBlockEntity(pos) instanceof Container barrel) {
            return barrel;
        }
        return null;
    }
}
