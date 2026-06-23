package com.mickdev.necromency.registry.init.Fluid;

import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.minecraft.world.level.block.state.StateDefinition;

public abstract class BloodFluid extends BaseFlowingFluid {
    protected BloodFluid(Properties props) {
        super(props);
    }

    public static class Flowing extends BloodFluid {
        public Flowing(Properties props) {
            super(props);
        }

        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override public int getAmount(FluidState state) { return state.getValue(LEVEL); }
        @Override public boolean isSource(FluidState state) { return false; }
    }

    public static class Source extends BloodFluid {
        public Source(Properties props) {
            super(props);
        }

        @Override public int getAmount(FluidState state) { return 1; }
        @Override public boolean isSource(FluidState state) { return true; }
    }
}