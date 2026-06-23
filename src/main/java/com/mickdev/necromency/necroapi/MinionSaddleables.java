package com.mickdev.necromency.necroapi;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Set;

/** Torso mob ids that accept a saddle (1.12 {@code ISaddleAble} — araignée, etc.). */
public final class MinionSaddleables {

    private record SaddleInfo(ResourceLocation texture, float riderHeight) {}

    /** ISaddleAble 1.7.10 : araignées (spidersaddle), vache (cowsaddle), poulpe (squidsaddle). */
    private static final Set<String> SADDLE_TORSOS = Set.of(
            "minecraft:spider",
            "minecraft:cave_spider",
            "minecraft:cow",
            "minecraft:squid"
    );

    private static final Map<String, SaddleInfo> BY_MOB = Map.ofEntries(
            Map.entry("minecraft:spider", new SaddleInfo(
                    ResourceLocation.fromNamespaceAndPath("necromency", "textures/entities/spidersaddle.png"), 0.35F)),
            Map.entry("minecraft:cave_spider", new SaddleInfo(
                    ResourceLocation.fromNamespaceAndPath("necromency", "textures/entities/spidersaddle.png"), 0.25F)),
            Map.entry("minecraft:cow", new SaddleInfo(
                    ResourceLocation.fromNamespaceAndPath("necromency", "textures/entities/cowsaddle.png"), 0.9F)),
            Map.entry("minecraft:squid", new SaddleInfo(
                    ResourceLocation.fromNamespaceAndPath("necromency", "textures/entities/squidsaddle.png"), 0.6F))
    );

    private MinionSaddleables() {}

    public static boolean torsoAcceptsSaddle(ResourceLocation bodyId) {
        return bodyId != null && SADDLE_TORSOS.contains(bodyId.toString());
    }

    public static ResourceLocation saddleTexture(ResourceLocation bodyId) {
        if (bodyId == null) return null;
        SaddleInfo info = BY_MOB.get(bodyId.toString());
        return info != null ? info.texture() : ResourceLocation.parse("minecraft:textures/entity/pig/pig_saddle.png");
    }

    public static float riderHeight(ResourceLocation bodyId) {
        if (bodyId == null) return 0.6F;
        SaddleInfo info = BY_MOB.get(bodyId.toString());
        return info != null ? info.riderHeight() : 0.6F;
    }
}
