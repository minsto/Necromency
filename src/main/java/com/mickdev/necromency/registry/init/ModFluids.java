package com.mickdev.necromency.registry.init;


import com.mickdev.necromency.Necromency;
import com.mickdev.necromency.registry.init.Fluid.BloodFluid;
import com.mickdev.necromency.registry.init.Fluid.ModFluidProperties;
import com.mickdev.necromency.registry.init.Fluid.NecroFluidType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModFluids {

    // ✅ FluidType = registre NeoForge
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, Necromency.MODID);

    // ✅ Fluid (source/flowing) = registre vanilla (c’est ça la correction)
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, Necromency.MODID);

    // ⚠️ Je te conseille des noms en lowercase (style MC)
    public static final DeferredHolder<FluidType, FluidType> BLOOD_TYPE =
            FLUID_TYPES.register("blood_fluid", () -> new NecroFluidType(
                    ResourceLocation.withDefaultNamespace("block/water_still"),
                    ResourceLocation.withDefaultNamespace("block/water_flow"),
                    ResourceLocation.withDefaultNamespace("block/water_overlay")
            ));

    public static final DeferredHolder<Fluid, BloodFluid.Source> BLOOD_SOURCE =
            FLUIDS.register("blood", () -> new BloodFluid.Source(ModFluidProperties.PROPS));

    public static final DeferredHolder<Fluid, BloodFluid.Flowing> BLOOD_FLOWING =
            FLUIDS.register("flowing_blood", () -> new BloodFluid.Flowing(ModFluidProperties.PROPS));

    private ModFluids() {}
}