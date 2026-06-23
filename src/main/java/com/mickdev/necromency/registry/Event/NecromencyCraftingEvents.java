package com.mickdev.necromency.registry.Event;

import com.mickdev.necromency.registry.NecromencyAdvancements;
import com.mickdev.necromency.registry.init.ModBlocks;
import com.mickdev.necromency.registry.init.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = com.mickdev.necromency.Necromency.MODID)
public final class NecromencyCraftingEvents {

    private NecromencyCraftingEvents() {}

    /** Port {@code ForgeEventHandler#onCrafting} retours seau / fioles. */
    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack crafted = event.getCrafting();
        if (crafted.isEmpty()) return;

        if (crafted.is(ModItems.Jar_of_Blood.get())) {
            event.getEntity().getInventory().add(new ItemStack(Items.BUCKET));
        } else if (crafted.is(ModItems.Blood_BUCKET.get())) {
            event.getEntity().getInventory().add(new ItemStack(Items.GLASS_BOTTLE, 8));
        } else if (crafted.is(ModItems.Necronomicon.get()) && event.getEntity() instanceof ServerPlayer sp) {
            NecromencyAdvancements.grant(sp, NecromencyAdvancements.NECRONOMICON);
        } else if (crafted.is(ModBlocks.SWING.get().asItem()) && event.getEntity() instanceof ServerPlayer sp) {
            NecromencyAdvancements.grant(sp, NecromencyAdvancements.SEWING);
        } else if (crafted.is(ModBlocks.SKULL_WALL.get().asItem())) {
            com.mickdev.necromency.registry.block.SkullWallData.writeDefaults(crafted);
        }
    }
}
