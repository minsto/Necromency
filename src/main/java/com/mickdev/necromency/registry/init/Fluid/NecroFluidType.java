package com.mickdev.necromency.registry.init.Fluid;



import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidType;

public class NecroFluidType extends FluidType {

    public final ResourceLocation still;
    public final ResourceLocation flowing;
    public final ResourceLocation overlay;

    public NecroFluidType(ResourceLocation still, ResourceLocation flowing, ResourceLocation overlay) {
        super(Properties.create()
                .canDrown(true)
                .canSwim(true)
                .supportsBoating(true)
                .fallDistanceModifier(0.0F)
        );
        this.still = still;
        this.flowing = flowing;
        this.overlay = overlay;
    }
}
