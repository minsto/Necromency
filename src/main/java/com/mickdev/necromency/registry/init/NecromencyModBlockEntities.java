package com.mickdev.necromency.registry.init;



import com.mickdev.necromency.Necromency;
import com.mickdev.necromency.registry.Altar.Block.ALTARBlockEntity;
import com.mickdev.necromency.registry.Swing.Block.SwingBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = Necromency.MODID)
public class NecromencyModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> REGISTRY =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Necromency.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ALTARBlockEntity>> ALTAR =
            register("altar", ModBlocks.ALTAR, ALTARBlockEntity::new);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SwingBlockEntity>> SWING =
            register("swing", ModBlocks.SWING, SwingBlockEntity::new);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.mickdev.necromency.registry.block.SkullWallBlockEntity>> SKULL_WALL =
            register("skull_wall", ModBlocks.SKULL_WALL, com.mickdev.necromency.registry.block.SkullWallBlockEntity::new);

    private static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> register(
            String name, DeferredHolder<Block, Block> block, BlockEntityType.BlockEntitySupplier<T> supplier
    ) {
        return REGISTRY.register(name, () -> new BlockEntityType<>(supplier, block.get()));
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, ALTAR.get(),
                (be, side) -> new WorldlyContainerWrapper(be, side));

        event.registerBlockEntity(Capabilities.Item.BLOCK, SWING.get(),
                (be, side) -> new WorldlyContainerWrapper(be, side));
    }
}