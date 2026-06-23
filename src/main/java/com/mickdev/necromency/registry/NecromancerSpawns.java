package com.mickdev.necromency.registry;

import com.mickdev.necromency.registry.data.NecroMobCatalog;
import com.mickdev.necromency.registry.init.ModItems;
import com.mickdev.necromency.registry.init.ModVillager;
import com.mickdev.necromency.registry.item.MobPart.BodyPartItem;
import com.mickdev.necromency.registry.item.MobPart.BodyPartStacks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/** Fait apparaître un villageois nécromancien (métier {@link ModVillager#NECROMANCER}). */
public final class NecromancerSpawns {

    private NecromancerSpawns() {}

    /**
     * Mobs dont on peut acheter une partie de corps (16 émeraudes).
     * Liste demandée : chat, fox, ghast, happy ghast, illusioner, piglin,
     * pillager, sniffer, turtle, trader, warden, armadillo.
     */
    private static final List<String> BODY_PART_MOBS = List.of(
            "minecraft:cat",
            "minecraft:fox",
            "minecraft:ghast",
            "minecraft:happy_ghast",
            "minecraft:illusioner",
            "minecraft:piglin",
            "minecraft:pillager",
            "minecraft:sniffer",
            "minecraft:turtle",
            "minecraft:wandering_trader",
            "minecraft:warden",
            "minecraft:armadillo"
    );

    @Nullable
    public static Villager spawn(ServerLevel level, BlockPos pos) {
        Villager villager = EntityType.VILLAGER.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        if (villager == null) {
            return null;
        }
        villager.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        // Adulte (jamais bébé) pour qu'il propose ses échanges immédiatement.
        villager.setBaby(false);
        // Métier nécromancien + niveau 1 -> apparence nécromancien.
        villager.setVillagerData(villager.getVillagerData()
                .withProfession(level.registryAccess(), ModVillager.NECROMANCER.getKey())
                .withLevel(1));
        // XP > 0 : empêche le comportement vanilla ResetProfession de le remettre au
        // chômage (il perd sinon son métier car il n'a pas de poste de travail).
        villager.setVillagerXp(1);
        // Offres injectées directement : en 1.21.10 les trades sont data-driven
        // (TradeSet) et VillagerTradesEvent n'est plus relu, donc on pose nous-mêmes.
        villager.setOffers(buildOffers(level.getRandom()));
        // Ne despawn jamais et garde son métier même sans poste de travail.
        villager.setPersistenceRequired();
        villager.setCustomName(Component.translatable("entity.necromency.necromancer"));
        level.addFreshEntity(villager);
        return villager;
    }

    private static MerchantOffers buildOffers(RandomSource random) {
        MerchantOffers offers = new MerchantOffers();

        // Necronomicon : 4 émeraudes.
        offers.add(new MerchantOffer(
                new ItemCost(Items.EMERALD, 4),
                Optional.empty(),
                ModItems.Necronomicon.get().getDefaultInstance(),
                Integer.MAX_VALUE, 5, 0.05F
        ));

        // Brain Cutter : 1 émeraude.
        offers.add(new MerchantOffer(
                new ItemCost(Items.EMERALD, 1),
                Optional.empty(),
                ModItems.Brain_Cutter.get().getDefaultInstance(),
                Integer.MAX_VALUE, 2, 0.05F
        ));

        // Altarobsidian : 8 blocs pour 2 émeraudes.
        offers.add(new MerchantOffer(
                new ItemCost(Items.EMERALD, 2),
                Optional.empty(),
                new ItemStack(ModItems.ALTAR_OBSIDIAN_ITEM.get(), 8),
                Integer.MAX_VALUE, 5, 0.05F
        ));

        // Brain Maker : 1 pour 64 émeraudes.
        offers.add(new MerchantOffer(
                new ItemCost(Items.EMERALD, 64),
                Optional.empty(),
                new ItemStack(ModItems.BRAIN_MAKER_ITEM.get(), 1),
                Integer.MAX_VALUE, 15, 0.05F
        ));

        // Parties de corps aléatoires : 16 émeraudes chacune (sélection variée).
        BodyPartItem.PartType[] parts = BodyPartItem.PartType.values();
        for (int i = 0; i < 6; i++) {
            String mobId = BODY_PART_MOBS.get(random.nextInt(BODY_PART_MOBS.size()));
            BodyPartItem.PartType part = parts[random.nextInt(parts.length)];
            offers.add(new MerchantOffer(
                    new ItemCost(Items.EMERALD, 16),
                    Optional.empty(),
                    BodyPartStacks.create(part, mobId),
                    4, 8, 0.05F
            ));
        }

        return offers;
    }
}
