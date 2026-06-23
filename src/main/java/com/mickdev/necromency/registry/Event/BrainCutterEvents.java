package com.mickdev.necromency.registry.Event;

import com.mickdev.necromency.Necromency;
import com.mickdev.necromency.entity.NightCrawlerEntity;
import com.mickdev.necromency.registry.BrainMaker.BrainTypes;
import com.mickdev.necromency.registry.init.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

/**
 * Brain cutter : extrait cerveaux typés (8 %), organes génériques (15 %), soulheart nightcrawler (10 %).
 */
@EventBusSubscriber(modid = Necromency.MODID)
public final class BrainCutterEvents {

    private static final int TYPED_BRAIN_CHANCE = 8;
    private static final int ORGAN_CHANCE = 15;
    private static final int SOULHEART_CHANCE = 10;

    private BrainCutterEvents() {}

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (!player.getMainHandItem().is(ModItems.Brain_Cutter.get())) return;

        LivingEntity dead = event.getEntity();
        var random = dead.getRandom();

        // Nightcrawler : soulheart 10 %
        if (dead instanceof NightCrawlerEntity) {
            if (random.nextInt(100) < SOULHEART_CHANCE) {
                drop(event, dead, new ItemStack(ModItems.SOULHEART.get()));
            }
            return;
        }

        // Mobs à cerveau typé : 8 %
        ItemStack typedBrain = BrainTypes.brainDropForEntity(dead.getType());
        if (typedBrain != null) {
            if (random.nextInt(100) < TYPED_BRAIN_CHANCE) {
                drop(event, dead, typedBrain);
            }
            return;
        }

        // Autres mobs : organe générique 15 %
        if (random.nextInt(100) < ORGAN_CHANCE) {
            ItemStack organ = switch (random.nextInt(4)) {
                case 0 -> new ItemStack(ModItems.BRAINS.get());
                case 1 -> new ItemStack(ModItems.HEART.get());
                case 2 -> new ItemStack(ModItems.LUNGS.get());
                default -> new ItemStack(ModItems.MUSCLE.get());
            };
            drop(event, dead, organ);
        }
    }

    private static void drop(LivingDropsEvent event, LivingEntity dead, ItemStack stack) {
        event.getDrops().add(new ItemEntity(
                dead.level(),
                dead.getX(),
                dead.getY(),
                dead.getZ(),
                stack
        ));
    }
}
