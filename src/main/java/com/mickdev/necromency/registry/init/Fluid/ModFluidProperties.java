package com.mickdev.necromency.registry.init.Fluid;


import com.mickdev.necromency.registry.init.ModBlocks;
import com.mickdev.necromency.registry.init.ModFluids;
import com.mickdev.necromency.registry.init.ModItems;
import net.minecraft.world.level.block.LiquidBlock;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

import java.util.function.Supplier;

public final class ModFluidProperties {

    public static final BaseFlowingFluid.Properties PROPS = new BaseFlowingFluid.Properties(
            ModFluids.BLOOD_TYPE,
            ModFluids.BLOOD_SOURCE,
            ModFluids.BLOOD_FLOWING
    )
            .bucket(ModItems.Blood_BUCKET)
            .block(() -> (net.minecraft.world.level.block.LiquidBlock) ModBlocks.BLOOD.get())
            .slopeFindDistance(1)
            .levelDecreasePerBlock(1)
            .tickRate(5)
            .explosionResistance(100.0F);

    private ModFluidProperties() {}
}

