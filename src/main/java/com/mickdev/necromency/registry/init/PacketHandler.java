package com.mickdev.necromency.registry.init;

import com.mickdev.necromency.Necromency;
import com.mickdev.necromency.registry.BrainMaker.BrainMakerCraftPayload;
import com.mickdev.necromency.registry.BrainMaker.Gui.BrainMakerGuiMenu;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
@EventBusSubscriber(modid = Necromency.MODID)
public class PacketHandler {
    private PacketHandler(){}

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar r = event.registrar("1");

        r.playToServer(
                BrainMakerCraftPayload.TYPE,
                BrainMakerCraftPayload.STREAM_CODEC,
                (msg, ctx) -> ctx.enqueueWork(() -> {
                    if (!(ctx.player() instanceof ServerPlayer player)) return;

                    if (player.containerMenu.containerId != msg.containerId()) return;
                    if (!(player.containerMenu instanceof BrainMakerGuiMenu menu)) return;

                    // ✅ ton menu a handleAdd, pas craft
                    menu.handleAdd(player);
                })
        );
    }

    public static void sendToServer(BrainMakerCraftPayload payload) {
        ClientPacketDistributor.sendToServer(payload);
    }
}