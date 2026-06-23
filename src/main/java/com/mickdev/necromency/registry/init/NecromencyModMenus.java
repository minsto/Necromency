package com.mickdev.necromency.registry.init;

import com.mickdev.necromency.Necromency;
import com.mickdev.necromency.registry.Altar.Menu.AltarguiMenu;
import com.mickdev.necromency.registry.BrainMaker.Gui.BrainMakerGuiMenu;
import com.mickdev.necromency.registry.Event.MenuStateUpdateMessage;
import com.mickdev.necromency.registry.Swing.Menu.SwingGuiMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Map;

public class NecromencyModMenus {
    public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(Registries.MENU, Necromency.MODID);
    public static final DeferredHolder<MenuType<?>, MenuType<BrainMakerGuiMenu>> BRAIN_MAKER_GUI =
            REGISTRY.register("brain_maker_gui",
                    () -> IMenuTypeExtension.create((id, inv, buf) ->
                            new BrainMakerGuiMenu(id, inv, buf.readBlockPos())
                    )
            );
    public static final DeferredHolder<MenuType<?>, MenuType<AltarguiMenu>> ALTARGUI = REGISTRY.register("altargui", () -> IMenuTypeExtension.create(AltarguiMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<SwingGuiMenu>> SWING_GUI = REGISTRY.register("swing_gui", () -> IMenuTypeExtension.create(SwingGuiMenu::new));

    // public static final DeferredHolder<MenuType<?>, MenuType<AltarguiMenu>> ALTARGUI = REGISTRY.register("altargui", () -> IMenuTypeExtension.create(AltarguiMenu::new));


    public interface MenuAccessor {
        Map<String, Object> getMenuState();

        Map<Integer, Slot> getSlots();

        default void sendMenuStateUpdate(Player player, int elementType, String name, Object elementState, boolean needClientUpdate) {
            getMenuState().put(elementType + ":" + name, elementState);
            if (player instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new MenuStateUpdateMessage(elementType, name, elementState));
            } else if (player.level().isClientSide()) {
                if (Minecraft.getInstance().screen instanceof NecromencyModScreens.ScreenAccessor accessor && needClientUpdate)
                    accessor.updateMenuState(elementType, name, elementState);
                ClientPacketDistributor.sendToServer(new MenuStateUpdateMessage(elementType, name, elementState));
            }
        }

        default <T> T getMenuState(int elementType, String name, T defaultValue) {
            try {
                return (T) getMenuState().getOrDefault(elementType + ":" + name, defaultValue);
            } catch (ClassCastException e) {
                return defaultValue;
            }
        }
    }
}
