package com.mickdev.necromency.registry.BrainMaker;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BrainMakerCraftPayload(int containerId) implements CustomPacketPayload {
    public static final Type<BrainMakerCraftPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("necromency", "brainmaker_craft"));

    public static final StreamCodec<FriendlyByteBuf, BrainMakerCraftPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, msg) -> buf.writeVarInt(msg.containerId),
                    buf -> new BrainMakerCraftPayload(buf.readVarInt())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
