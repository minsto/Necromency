package com.mickdev.necromency.registry.init.Fluid.Block;

import com.mickdev.necromency.registry.init.ModFluids;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class BloodBlock extends LiquidBlock {
    public BloodBlock(BlockBehaviour.Properties properties) {
        super(ModFluids.BLOOD_SOURCE.get(), properties.mapColor(MapColor.WATER).strength(100f).noOcclusion().noLootTable().liquid().pushReaction(PushReaction.DESTROY).sound(SoundType.EMPTY).replaceable());
    }
}