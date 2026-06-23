package com.mickdev.necromency.Client;

import com.mickdev.necromency.Necromency;
import com.mickdev.necromency.entity.MinionEntity;
import com.mickdev.necromency.registry.NecromencyEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = Necromency.MODID)
public final class NecromencyEntityAttributes {

    @SubscribeEvent
    public static void onEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(
                NecromencyEntities.MINION.get(),
                MinionEntity.createAttributes().build()
        );
    }

    private NecromencyEntityAttributes() {}
}