package com.mickdev.necromency.registry.Event;

import com.mickdev.necromency.registry.init.Fluid.Item.ScytheItem;
import com.mickdev.necromency.registry.init.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid = com.mickdev.necromency.Necromency.MODID)
public final class ScytheCombatEvents {

    private ScytheCombatEvents() {}

    @SubscribeEvent
    public static void onKillWithScythe(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        var killer = event.getSource().getEntity();
        if (!(killer instanceof Player player)) return;
        ItemStack weapon = player.getMainHandItem();
        if (!(weapon.getItem() instanceof ScytheItem)) return;
        if (!player.getInventory().contains(new ItemStack(Items.GLASS_BOTTLE))) return;
        if (!player.getInventory().add(new ItemStack(ModItems.JAR_OF_SOUL.get()))) return;
        player.getInventory().removeItem(new ItemStack(Items.GLASS_BOTTLE));
        if (event.getEntity().level() instanceof ServerLevel server) {
            var target = event.getEntity();
            server.sendParticles(ParticleTypes.SOUL,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                    12, 0.2, 0.2, 0.2, 0.02);
        }
    }
}
