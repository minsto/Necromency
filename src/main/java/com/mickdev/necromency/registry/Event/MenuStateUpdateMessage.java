package com.mickdev.necromency.registry.Event;

import com.mickdev.necromency.Necromency;
import com.mickdev.necromency.registry.init.NecromencyModMenus;
import com.mickdev.necromency.registry.init.NecromencyModScreens;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@EventBusSubscriber
public record MenuStateUpdateMessage(int elementType, String name, Object elementState) implements CustomPacketPayload {

    public static final Type<MenuStateUpdateMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Necromency.MODID, "menustate_update"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MenuStateUpdateMessage> STREAM_CODEC = StreamCodec.of(MenuStateUpdateMessage::write, MenuStateUpdateMessage::read);
    public static void write(FriendlyByteBuf buffer, MenuStateUpdateMessage message) {
        buffer.writeInt(message.elementType);
        buffer.writeUtf(message.name);
        if (message.elementType == 0) {
            buffer.writeUtf((String) message.elementState);
        } else if (message.elementType == 1) {
            buffer.writeBoolean((boolean) message.elementState);
        } else if (message.elementType == 2 && message.elementState instanceof Number n) {
            buffer.writeDouble(n.doubleValue());
        }
    }

    public static MenuStateUpdateMessage read(FriendlyByteBuf buffer) {
        int elementType = buffer.readInt();
        String name = buffer.readUtf();
        Object elementState = null;
        if (elementType == 0) {
            elementState = buffer.readUtf();
        } else if (elementType == 1) {
            elementState = buffer.readBoolean();
        } else if (elementType == 2) {
            elementState = buffer.readDouble();
        }
        return new MenuStateUpdateMessage(elementType, name, elementState);
    }

    @Override
    public Type<MenuStateUpdateMessage> type() {
        return TYPE;
    }

    public static void handleMenuState(final MenuStateUpdateMessage message, final IPayloadContext context) {
        if (message.name.length() > 256 || message.elementState instanceof String string && string.length() > 8192)
            return;

        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof NecromencyModMenus.MenuAccessor menu) {

                // 🔁 met à jour l’état menu
                menu.getMenuState().put(message.elementType + ":" + message.name, message.elementState);

                // ✅ côté CLIENT uniquement : update GUI
                if (context.player().level().isClientSide()
                        && Minecraft.getInstance().screen instanceof NecromencyModScreens.ScreenAccessor accessor) {
                    accessor.updateMenuState(message.elementType, message.name, message.elementState);
                }
            }
        }).exceptionally(e -> {
            context.connection().disconnect(Component.literal(e.getMessage()));
            return null;
        });
    }


}
