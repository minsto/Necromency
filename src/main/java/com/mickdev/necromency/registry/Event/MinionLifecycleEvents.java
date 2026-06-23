package com.mickdev.necromency.registry.Event;

import com.mickdev.necromency.Necromency;
import com.mickdev.necromency.entity.MinionEntity;
import com.mickdev.necromency.entity.MinionPlayerData;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid = Necromency.MODID)
public final class MinionLifecycleEvents {

    private MinionLifecycleEvents() {}

    @SubscribeEvent
    public static void onMinionDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof MinionEntity minion)) return;
        if (minion.level().isClientSide()) return;

        Player owner = minion.getOwnerPlayer();
        if (owner != null) {
            MinionPlayerData.decrementMinionCount(owner);
        }
    }
}
