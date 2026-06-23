package com.mickdev.necromency.data;

import com.mickdev.necromency.Necromency;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = Necromency.MODID)
public final class NecromencyDataGenerators {

    private NecromencyDataGenerators() {}

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        event.createProvider(NecromencySwingRecipeProvider.Runner::new);
    }
}
