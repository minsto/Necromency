package com.mickdev.necromency.registry.BrainMaker;

import com.mickdev.necromency.Necromency;

import com.mickdev.necromency.registry.init.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

@EventBusSubscriber(modid = Necromency.MODID)
public final class BloodBottleHandler {

    private static final String KEY_HITS = "necromency_blood_hits";

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer sp)) return;

        if (!(event.getTarget() instanceof LivingEntity)) return;

        // Server only
        if (sp.level().isClientSide()) return;

        ItemStack held = sp.getItemInHand(InteractionHand.MAIN_HAND);
        if (!held.is(ModItems.BONE_NEEDLE.get())) return;

        // Il faut avoir une bouteille vide
        if (!hasGlassBottle(sp)) return;

        CompoundTag tag = sp.getPersistentData();
        int hits = tag.getInt(KEY_HITS).orElse(0);
        hits++;

        if (hits >= 3) {
            hits = 0;

            // retire 1 bouteille vide
            consumeOneGlassBottle(sp);

            // donne 1 blood bottle
            ItemStack blood = new ItemStack(ModItems.Jar_of_Blood.get());
            if (!sp.getInventory().add(blood)) {
                sp.drop(blood, false);
            }
        }

// sauvegarde le compteur
        tag.putInt(KEY_HITS, hits);
    }

    private static boolean hasGlassBottle(ServerPlayer sp) {
        for (int i = 0; i < sp.getInventory().getContainerSize(); i++) {
            ItemStack s = sp.getInventory().getItem(i);
            if (s.is(Items.GLASS_BOTTLE) && s.getCount() > 0) return true;
        }
        return false;
    }

    private static void consumeOneGlassBottle(ServerPlayer sp) {
        for (int i = 0; i < sp.getInventory().getContainerSize(); i++) {
            ItemStack s = sp.getInventory().getItem(i);
            if (s.is(Items.GLASS_BOTTLE) && s.getCount() > 0) {
                s.shrink(1);
                return;
            }
        }
    }
}
