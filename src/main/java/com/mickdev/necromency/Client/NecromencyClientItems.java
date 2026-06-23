package com.mickdev.necromency.Client;

import com.mickdev.necromency.Client.item.SpecialScytheProperty;
import com.mickdev.necromency.Necromency;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;

public final class NecromencyClientItems {

    public static final ResourceLocation SPECIAL_SCYTHE =
            ResourceLocation.fromNamespaceAndPath(Necromency.MODID, "special_scythe");

    private NecromencyClientItems() {}

    public static void registerRangeSelectProperties(RegisterRangeSelectItemModelPropertyEvent event) {
        event.register(SPECIAL_SCYTHE, SpecialScytheProperty.MAP_CODEC);
    }
}
