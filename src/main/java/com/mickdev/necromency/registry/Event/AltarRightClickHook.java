package com.mickdev.necromency.registry.Event;

import com.mickdev.necromency.registry.Altar.Block.ALTARBlockEntity;
import com.mickdev.necromency.registry.init.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = "necromency")
public final class AltarRightClickHook {

    private AltarRightClickHook() {}

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;

        Level level = sp.level();
        if (level.isClientSide()) return;

        if (!sp.isShiftKeyDown()) return;

        BlockPos pos = e.getPos();
        if (!level.getBlockState(pos).is(ModBlocks.ALTAR.get())) return;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ALTARBlockEntity altar)) return;

        altar.debugSlots(sp);

        // tryBuildAbomination affiche déjà un message précis (ex. "slot 0 blood vide").
        // On ne l'écrase plus par un message générique qui listait tous les slots
        // et laissait croire à tort qu'il manquait les jambes.
        boolean ok = altar.tryBuildAbomination(sp);
        if (ok) {
            sp.displayClientMessage(net.minecraft.network.chat.Component.literal("ALTAR: rituel reussi!"), true);
        }

        // ✅ Empêche l’item en main de faire autre chose
        e.setCanceled(true);
        e.setCancellationResult(InteractionResult.CONSUME);
    }
}
