package com.mickdev.necromency.registry.Event;

import com.mickdev.necromency.Necromency;
import com.mickdev.necromency.registry.NecromencyEntities;
import com.mickdev.necromency.registry.init.ModFluids;
import com.mickdev.necromency.registry.init.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = Necromency.MODID)
public final class NecromencyDeathEvents {
    private static int ritualScanTicksLeft = 0;

    public static void startRitualScan30s() {
        ritualScanTicksLeft = 20 * 30; // 600 ticks
    }
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if ((event.getServer().getTickCount() % 30) != 0) return; // 2x/sec

        for (ServerLevel level : event.getServer().getAllLevels()) {

            for (var player : level.players()) {
                AABB box = new AABB(player.blockPosition()).inflate(64, 16, 64);

                for (ItemEntity drop : level.getEntitiesOfClass(ItemEntity.class, box,
                        it -> !it.getItem().isEmpty() && it.getItem().is(ModItems.SOULHEART.get()))) {

                    BlockPos base = drop.blockPosition();
                    BlockPos bloodPos = null;

                    for (int i = 0; i <= 3; i++) {
                        BlockPos p = base.below(i);
                        if (isBlood(level, p)) { bloodPos = p; break; }
                    }
                    if (bloodPos == null) continue;

                    // consume 1 direct (sur CET item)
                    ItemStack stack = drop.getItem();
                    stack.shrink(1);
                    if (stack.isEmpty()) drop.discard();
                    else drop.setItem(stack);

                    // spawn
                    spawnIsaacNormal(level, bloodPos);

                    // remove blood
                    level.setBlockAndUpdate(bloodPos, Blocks.AIR.defaultBlockState());
                    return;
                }
            }
        }
    }

    // ------------------------------------------------------------
    // Blood check
    // ------------------------------------------------------------
    private static boolean isBlood(ServerLevel level, BlockPos pos) {
        FluidState fs = level.getFluidState(pos);
        if (fs.isEmpty()) return false;

        return fs.getType() == ModFluids.BLOOD_SOURCE.get()
                || fs.getType() == ModFluids.BLOOD_FLOWING.get();
    }

    // ------------------------------------------------------------
    // Zone: 2 blocs d'air au-dessus du fluide (Y+1 -> Y+3)
    // ------------------------------------------------------------
    private static AABB twoBlocksAboveFluid(BlockPos bloodPos) {
        return new AABB(
                bloodPos.getX(),     bloodPos.getY() + 1.0, bloodPos.getZ(),
                bloodPos.getX() + 1, bloodPos.getY() + 3.0, bloodPos.getZ() + 1
        ).inflate(0.4);
    }

    // ------------------------------------------------------------
    // Check soulheart au-dessus
    // ------------------------------------------------------------
    private static boolean hasSoulheartTwoBlocksAbove(ServerLevel level, BlockPos bloodPos) {
        AABB box = twoBlocksAboveFluid(bloodPos);

        for (ItemEntity it : level.getEntitiesOfClass(ItemEntity.class, box, e -> true)) {
            ItemStack stack = it.getItem();
            if (!stack.isEmpty() && stack.is(ModItems.SOULHEART.get())) return true;
        }
        return false;
    }

    // ------------------------------------------------------------
    // Consume 1 soulheart (retourne true si consommé)
    // ------------------------------------------------------------
    private static boolean consumeOneSoulheartAboveBlood(ServerLevel level, BlockPos bloodPos) {
        AABB box = twoBlocksAboveFluid(bloodPos);

        for (ItemEntity it : level.getEntitiesOfClass(ItemEntity.class, box, e -> true)) {
            ItemStack stack = it.getItem();
            if (stack.isEmpty() || !stack.is(ModItems.SOULHEART.get())) continue;

            stack.shrink(1);
            if (stack.isEmpty()) it.discard();
            else it.setItem(stack);

            return true;
        }
        return false;
    }

    // ------------------------------------------------------------
    // Spawn Isaac (fiable : create + addFreshEntity)
    // ------------------------------------------------------------
    private static boolean spawnIsaacNormal(ServerLevel level, BlockPos bloodPos) {

        EntityType<?> type = NecromencyEntities.ISAAC_NORMAL.get(); // <-- OBLIGATOIRE

        if (type == null) {
            Necromency.LOGGER.error("ISAAC_NORMAL EntityType is NULL (registry error)");
            return false;
        }

        Entity e = type.create(level, EntitySpawnReason.EVENT);
        if (e == null) {
            Necromency.LOGGER.warn("Failed to create Isaac entity (create returned null)");
            return false;
        }

        // Position au-dessus du blood
        e.setPos(
                bloodPos.getX() + 0.5,
                bloodPos.getY() + 1.0,
                bloodPos.getZ() + 0.5
        );

        // Pas de rotation
        e.setYRot(0.0f);
        e.setXRot(0.0f);

        boolean ok = level.addFreshEntity(e);

        if (ok) Necromency.LOGGER.info("Isaac spawned at {}", bloodPos);
        else Necromency.LOGGER.warn("addFreshEntity returned false for Isaac at {}", bloodPos);

        return ok;
    }
}