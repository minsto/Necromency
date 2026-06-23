package com.mickdev.necromency.registry.Event;

import com.mickdev.necromency.Necromency;
import com.mickdev.necromency.registry.init.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid = Necromency.MODID)
public final class SoulJarEvents {

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) return;

        // Le vrai "tueur" (joueur) si c'est une attaque melee/proj etc.
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

        // Arme utilisée (mainhand)
        ItemStack weapon = player.getMainHandItem();

        boolean scythe = weapon.is(ModItems.SCYTHE.get());
        boolean boneScythe = weapon.is(ModItems.BONE_SCYTHE.get());

        if (!scythe && !boneScythe) return;

        // Consomme 1 bouteille vide
        if (!consumeOneGlassBottle(player)) return;

        int amount = boneScythe ? 2 : 1;

        ItemStack reward = new ItemStack(ModItems.JAR_OF_SOUL.get(), amount);

        // Donne au joueur (sinon drop si inventaire plein)
        if (!player.getInventory().add(reward)) {
            player.drop(reward, false);
        }
    }

    private static boolean consumeOneGlassBottle(ServerPlayer player) {
        var inv = player.getInventory();

        // Inventaire + hotbar
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.is(Items.GLASS_BOTTLE)) {
                inv.removeItem(i, 1);
                inv.setChanged();
                return true;
            }
        }

        // Offhand (optionnel)
        ItemStack offhand = player.getOffhandItem();
        if (offhand.is(Items.GLASS_BOTTLE)) {
            offhand.shrink(1);
            return true;
        }

        return false;
    }
}