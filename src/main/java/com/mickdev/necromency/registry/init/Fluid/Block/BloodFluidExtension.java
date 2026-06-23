package com.mickdev.necromency.registry.init.Fluid.Block;

import com.mickdev.necromency.registry.init.ModFluids;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.resources.ResourceLocation;



@EventBusSubscriber(Dist.CLIENT)
public class BloodFluidExtension {
    @SubscribeEvent
    public static void registerFluidTypeExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
            private static final ResourceLocation STILL_TEXTURE = ResourceLocation.parse("necromency:block/blood_still");
            private static final ResourceLocation FLOWING_TEXTURE = ResourceLocation.parse("necromency:block/blood_flow");

            @Override
            public ResourceLocation getStillTexture() {
                return STILL_TEXTURE;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return FLOWING_TEXTURE;
            }
        }, ModFluids.BLOOD_TYPE.get());
    }
}
