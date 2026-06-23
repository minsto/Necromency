package com.mickdev.necromency.necroapi.NecroAPIRemake;


import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public final class MinionProxyCache {

    private static final Map<String, ProxyRender> CACHE = new HashMap<>();

    private MinionProxyCache() {}

    @Nullable
    public static ProxyRender getOrCreate(String mobId) {
        ProxyRender cached = CACHE.get(mobId);
        if (cached != null) return cached;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return null;

        ResourceLocation rl = ResourceLocation.tryParse(mobId);
        if (rl == null) return null;

        EntityType<?> rawType = BuiltInRegistries.ENTITY_TYPE.getOptional(rl).orElse(null);
        if (rawType == null) return null;

        if (!(rawType instanceof EntityType<?>)) return null;

        var created = rawType.create(mc.level, EntitySpawnReason.LOAD);
        if (!(created instanceof LivingEntity proxy)) return null;

        EntityRenderDispatcher disp = mc.getEntityRenderDispatcher();
        EntityRenderer<?, ?> base = disp.getRenderer(proxy);

        if (!(base instanceof LivingEntityRenderer<?, ?, ?> ler)) return null;

        @SuppressWarnings("unchecked")
        EntityType<? extends LivingEntity> type = (EntityType<? extends LivingEntity>) rawType;

        ProxyRender pr = new ProxyRender(type, ler);
        CACHE.put(mobId, pr);
        return pr;
    }
}