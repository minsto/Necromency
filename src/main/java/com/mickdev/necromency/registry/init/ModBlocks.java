package com.mickdev.necromency.registry.init;

import com.mickdev.necromency.Necromency;
import com.mickdev.necromency.registry.Altar.Block.ALTARBlock;
import com.mickdev.necromency.registry.BrainMaker.Block.BrainMakerBlock;
import com.mickdev.necromency.registry.Swing.Block.SwingBlock;
import com.mickdev.necromency.registry.block.AltarBuildingBlock;
import com.mickdev.necromency.registry.block.DecorativeBlocks;
import com.mickdev.necromency.registry.block.NecromancerJobsStandBlock;
import com.mickdev.necromency.registry.block.SkullWallBlock;
import com.mickdev.necromency.registry.init.Fluid.Block.BloodBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Necromency.MODID);
    public static final DeferredBlock<Block> SWING;

    public static final DeferredBlock<Block> BLOOD;

    public static final DeferredBlock<Block> ALTAR;

    public static final DeferredBlock<Block> BRAIN_MAKER;

    public static final DeferredBlock<Block> SKULL_WALL;

    public static final DeferredBlock<Block> ALTAR_BUILDING;

    public static final DeferredBlock<Block> ALTAR_OBSIDIAN;

    public static final DeferredBlock<Block> FLAME_TOWER;

    public static final DeferredBlock<Block> NECRO_SOUL_FLAME;

    public static final DeferredBlock<Block> JOBS_STAND;
    static {
        BRAIN_MAKER = register("brain_maker", BrainMakerBlock::new);
        ALTAR = register("altar", ALTARBlock::new);
        SWING = register("swing", SwingBlock::new);
        BLOOD = register("blood", BloodBlock::new);
        SKULL_WALL = register("skull_wall", SkullWallBlock::new);
        ALTAR_BUILDING = register("altar_block", AltarBuildingBlock::new);
        ALTAR_OBSIDIAN = register("altarobsidian", p -> new DecorativeBlocks.AltarObsidianBlock(DecorativeBlocks.altarObsidianProps(p)));
        FLAME_TOWER = register("flametower", p -> new DecorativeBlocks.FlameTowerBlock(DecorativeBlocks.flameTowerProps(p)));
        NECRO_SOUL_FLAME = register("necrosoulflame", p -> new DecorativeBlocks.NecroSoulFlameBlock(DecorativeBlocks.soulFlameProps(p)));
        JOBS_STAND = register("necromency_jobs_stand", p -> new NecromancerJobsStandBlock(p.strength(2.5F).sound(SoundType.WOOD).noOcclusion()));



    }

    // Start of user code block custom blocks
    // End of user code block custom blocks
    private static <B extends Block> DeferredBlock<B> register(String name, Function<BlockBehaviour.Properties, ? extends B> supplier) {
        return BLOCKS.registerBlock(name, supplier);
    }
}