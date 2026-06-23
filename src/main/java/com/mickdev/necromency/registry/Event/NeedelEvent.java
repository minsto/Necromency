package com.mickdev.necromency.registry.Event;

import com.mickdev.necromency.Necromency;
import com.mickdev.necromency.registry.init.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid = Necromency.MODID)
public class NeedelEvent {
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        // l'entité morte
        LivingEntity dead = event.getEntity();

        // le tueur
        if (!(event.getSource().getEntity() instanceof Player player)) return;

        // option: ignorer les joueurs tués
        if (dead instanceof Player) return;

        ItemStack hand = player.getMainHandItem();

        // doit tenir Bone Needle
        if (!hand.is(ModItems.BONE_NEEDLE.get())) return;

        // donne 1 jar_of_blood
        ItemStack blood = new ItemStack(ModItems.Jar_of_Blood.get(), 1);
        if (!player.getInventory().add(blood)) {
            player.drop(blood, false);
        }
    }
}

