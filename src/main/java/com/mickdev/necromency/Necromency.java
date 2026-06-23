package com.mickdev.necromency;

import com.mickdev.necromency.Client.NecromencyEntityAttributes;
import com.mickdev.necromency.registry.NecromencyEntities;
import com.mickdev.necromency.registry.Swing.Recipes.SwingRecipeSerializer;
import com.mickdev.necromency.registry.Swing.Recipes.SwingRecipeType;
import com.mickdev.necromency.registry.init.*;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(Necromency.MODID)
public final class Necromency {
    public static final String MODID = "necromency";
    public static final Logger LOGGER = LogUtils.getLogger();

    // Tu mettras ici tes DeferredRegister (items/blocks/entities) plus tard



    public Necromency(IEventBus modBus, ModContainer container) {
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModFluids.FLUID_TYPES.register(modBus);
        ModFluids.FLUIDS.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        NecromencyEntities.ENTITIES.register(modBus);
        ModFeatures.FEATURES.register(modBus);
        ModVillager.POI_TYPES.register(modBus);
        ModVillager.PROFESSIONS.register(modBus);
        NecromencyTabs.register(modBus);

        NecromencyModMenus.REGISTRY.register(modBus);
        SwingRecipeType.TYPES.register(modBus);
        SwingRecipeSerializer.SERIALIZERS.register(modBus);

        NecromencyModBlockEntities.REGISTRY.register(modBus);
        ModSounds.SOUNDS.register(modBus);
       // modBus.addListener(NecromencyModScreens::clientLoad);

        container.registerConfig(ModConfig.Type.COMMON, NecromencyConfig.SPEC);


    }

}