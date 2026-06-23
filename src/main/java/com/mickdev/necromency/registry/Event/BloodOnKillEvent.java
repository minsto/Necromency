package com.mickdev.necromency.registry.Event;

import com.mickdev.necromency.Necromency;
import com.mickdev.necromency.registry.NecromencyEntities;
import com.mickdev.necromency.registry.init.ModBlocks; // ou ModFluids selon ton setup
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid = Necromency.MODID)
public final class BloodOnKillEvent {

    private static final int CHANCE_DENOM = 4;

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity dead = event.getEntity();
        if (dead.level().isClientSide()) return;

        // ❌ Ne rien faire si c'est un de tes Isaac
        if (dead.getType() == NecromencyEntities.ISAAC_BODY.get()
                || dead.getType() == NecromencyEntities.ISAAC_NORMAL.get()
                || dead.getType() == NecromencyEntities.ISAAC_BLOOD.get()
                || dead.getType() == NecromencyEntities.ISAAC_HEAD.get()) {
            return;
        }

        // (Optionnel) seulement si c'est un joueur qui tue
        if (!(event.getSource().getEntity() instanceof Player)) return;

        ServerLevel level = (ServerLevel) dead.level();
        if (level.random.nextInt(CHANCE_DENOM) != 0) return;

        Block bloodBlock = ModBlocks.BLOOD.get();
        if (!(bloodBlock instanceof LiquidBlock)) return;

        BlockPos pos = dead.blockPosition();

        placeIfPossible(level, pos, bloodBlock.defaultBlockState());
        placeIfPossible(level, pos.above(), bloodBlock.defaultBlockState());
    }

    private static void placeIfPossible(ServerLevel level, BlockPos pos, BlockState state) {
        if (!level.hasChunkAt(pos)) return;

        BlockState existing = level.getBlockState(pos);
        if (!existing.isAir() && !existing.canBeReplaced()) return;

        level.setBlock(pos, state, Block.UPDATE_ALL);
    }
}


