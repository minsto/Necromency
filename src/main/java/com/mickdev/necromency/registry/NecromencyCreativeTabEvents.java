package com.mickdev.necromency.registry;

import com.mickdev.necromency.Necromency;
import com.mickdev.necromency.registry.init.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@EventBusSubscriber(modid = Necromency.MODID)
public final class NecromencyCreativeTabEvents {

    private NecromencyCreativeTabEvents() {}

    @SubscribeEvent
    public static void onBuildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.TEDDY_SPAWN_EGG.get());
        }
    }
}
