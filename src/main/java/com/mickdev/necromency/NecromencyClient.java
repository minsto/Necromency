package com.mickdev.necromency;

import com.mickdev.necromency.Client.NecromencyClientItems;
import com.mickdev.necromency.Client.render.AltarMinionPreview;
import com.mickdev.necromency.registry.init.NecromencyModScreens;
import com.mickdev.necromency.Client.render.MinionCompositeAtlas;
import com.mickdev.necromency.registry.Altar.AltarPreviewAccess;
import com.mickdev.necromency.registry.Swing.Recipes.SwingRecipeSerializer;
import com.mickdev.necromency.registry.Swing.Recipes.SwingRecipeType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = Necromency.MODID, dist = Dist.CLIENT)
public final class NecromencyClient {
    public NecromencyClient(IEventBus modBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modBus.addListener(NecromencyClient::registerMinionAtlasReload);
        modBus.addListener(NecromencyClientItems::registerRangeSelectProperties);
        modBus.addListener(NecromencyModScreens::clientLoad);
    }

    private static void registerMinionAtlasReload(AddClientReloadListenersEvent event) {
        event.addListener(
                ResourceLocation.fromNamespaceAndPath(Necromency.MODID, "minion_composite_atlas"),
                new SimplePreparableReloadListener<Void>() {
                    @Override
                    protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
                        return null;
                    }

                    @Override
                    protected void apply(Void prepared, ResourceManager resourceManager, ProfilerFiller profiler) {
                        MinionCompositeAtlas.rebuild(resourceManager);
                    }
                });
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        AltarPreviewAccess.bindPreviewRemoved(AltarMinionPreview::remove);
        Necromency.LOGGER.info("Necromency client init");
        Necromency.LOGGER.info("Swing type = {}", SwingRecipeType.SWING_SHAPED.get());
        Necromency.LOGGER.info("Swing serializer = {}", SwingRecipeSerializer.SWING_SHAPED.get());


        // plus tard: renderers, model layers, screens, etc.
    }
}