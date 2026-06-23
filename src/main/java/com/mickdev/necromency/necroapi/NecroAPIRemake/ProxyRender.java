package com.mickdev.necromency.necroapi.NecroAPIRemake;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ProxyRender {
    private final EntityType<? extends LivingEntity> type;
    private final LivingEntityRenderer<?, ?, ?> renderer;

    private LivingEntity cachedEntity;

    public ProxyRender(EntityType<? extends LivingEntity> type,
                       LivingEntityRenderer<?, ?, ?> renderer) {
        this.type = type;
        this.renderer = renderer;
    }

    public EntityType<? extends LivingEntity> type() { return type; }
    public LivingEntityRenderer<?, ?, ?> renderer() { return renderer; }

    public LivingEntity proxyEntity(Level level) {
        if (cachedEntity == null || cachedEntity.level() != level) {
            cachedEntity = type.create(level, EntitySpawnReason.LOAD);
        }
        return cachedEntity;
    }

    /** ✅ Récupérer le modèle du renderer (1.21: parfois getModel() existe) */
    public EntityModel<?> model() {
        try {
            Method m = renderer.getClass().getMethod("getModel");
            Object o = m.invoke(renderer);
            if (o instanceof EntityModel<?> em) return em;
        } catch (Throwable ignored) {}

        try {
            Field f = LivingEntityRenderer.class.getDeclaredField("model");
            f.setAccessible(true);
            return (EntityModel<?>) f.get(renderer);
        } catch (Throwable ignored) {}

        return null;
    }
}