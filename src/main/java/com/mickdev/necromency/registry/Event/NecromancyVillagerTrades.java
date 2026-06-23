package com.mickdev.necromency.registry.Event;

import com.mickdev.necromency.registry.data.NecroMobCatalog;
import com.mickdev.necromency.registry.init.ModItems;
import com.mickdev.necromency.registry.init.ModVillager;
import com.mickdev.necromency.registry.item.MobPart.BodyPartItem;
import com.mickdev.necromency.registry.item.MobPart.BodyPartStacks;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

import java.util.List;
import java.util.Optional;

@EventBusSubscriber(modid = com.mickdev.necromency.Necromency.MODID)
public final class NecromancyVillagerTrades {

    private NecromancyVillagerTrades() {}

    @SubscribeEvent
    public static void onTrades(VillagerTradesEvent event) {
        if (!event.getType().equals(ModVillager.NECROMANCER.getKey())) {
            return;
        }
        List<net.minecraft.world.entity.npc.VillagerTrades.ItemListing> novice = event.getTrades().get(1);
        List<net.minecraft.world.entity.npc.VillagerTrades.ItemListing> apprentice = event.getTrades().get(2);

        novice.add((trader, random) -> new MerchantOffer(
                new ItemCost(Items.EMERALD, 6),
                Optional.of(new ItemCost(Items.BOOK)),
                ModItems.Necronomicon.get().getDefaultInstance(),
                2, 5, 0.05F
        ));

        novice.add((trader, random) -> {
            var mob = NecroMobCatalog.all().get(random.nextInt(NecroMobCatalog.all().size()));
            BodyPartItem.PartType part = BodyPartItem.PartType.values()[random.nextInt(BodyPartItem.PartType.values().length)];
            return new MerchantOffer(
                    new ItemCost(Items.EMERALD, 1 + random.nextInt(3)),
                    Optional.empty(),
                    BodyPartStacks.create(part, mob.mobId()),
                    8, 2, 0.02F
            );
        });

        apprentice.add((trader, random) -> {
            var mob = NecroMobCatalog.all().get(random.nextInt(NecroMobCatalog.all().size()));
            BodyPartItem.PartType part = BodyPartItem.PartType.values()[random.nextInt(BodyPartItem.PartType.values().length)];
            ItemStack partStack = BodyPartStacks.create(part, mob.mobId());
            return new MerchantOffer(
                    new ItemCost(partStack.getItemHolder(), 1, DataComponentExactPredicate.allOf(partStack.getComponents()), partStack),
                    Optional.empty(),
                    new ItemStack(Items.EMERALD, 1 + random.nextInt(3)),
                    12, 5, 0.05F
            );
        });
    }
}
