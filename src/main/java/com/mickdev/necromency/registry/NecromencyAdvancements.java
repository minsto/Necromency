package com.mickdev.necromency.registry;

import com.mickdev.necromency.Necromency;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class NecromencyAdvancements {

    public static final ResourceLocation ROOT = id("root");
    public static final ResourceLocation NECRONOMICON = id("necronomicon");
    public static final ResourceLocation SEWING = id("sewing_machine");
    public static final ResourceLocation ALTAR = id("altar");
    public static final ResourceLocation MINION = id("spawn_minion");

    private NecromencyAdvancements() {}

    public static void grant(ServerPlayer player, ResourceLocation advancementId) {
        if (!(player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;
        MinecraftServer server = serverLevel.getServer();
        if (server == null) return;
        AdvancementHolder holder = server.getAdvancements().get(advancementId);
        if (holder == null) return;
        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(holder);
        if (progress.isDone()) return;
        for (String criterion : progress.getRemainingCriteria()) {
            player.getAdvancements().award(holder, criterion);
        }
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Necromency.MODID, path);
    }
}
