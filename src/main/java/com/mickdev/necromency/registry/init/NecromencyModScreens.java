package com.mickdev.necromency.registry.init;

import com.mickdev.necromency.Necromency;
import com.mickdev.necromency.registry.Altar.Screen.AltarguiScreen;
import com.mickdev.necromency.registry.BrainMaker.Gui.BrainMakerGuiScreen;
import com.mickdev.necromency.registry.Swing.screen.SwingGuiScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
@EventBusSubscriber(modid = Necromency.MODID, value = Dist.CLIENT)
public class NecromencyModScreens {
    public static void clientLoad(RegisterMenuScreensEvent event) {

        event.register(NecromencyModMenus.ALTARGUI.get(), AltarguiScreen::new);

        event.register(NecromencyModMenus.BRAIN_MAKER_GUI.get(), BrainMakerGuiScreen::new);
        event.register(NecromencyModMenus.SWING_GUI.get(), SwingGuiScreen::new);

    }

    public interface ScreenAccessor {
        void updateMenuState(int elementType, String name, Object elementState);
    }
}
