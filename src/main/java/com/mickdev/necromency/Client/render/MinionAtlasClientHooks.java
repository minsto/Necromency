package com.mickdev.necromency.Client.render;

import com.mickdev.necromency.Necromency;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = Necromency.MODID, value = Dist.CLIENT)
public final class MinionAtlasClientHooks {

    private MinionAtlasClientHooks() {}

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) {
                MinionCompositeAtlas.rebuild(mc.getResourceManager());
            }
        });
    }
}
