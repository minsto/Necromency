package com.mickdev.necromency.registry.init;

import com.mickdev.necromency.Necromency;
import com.mickdev.necromency.registry.item.BloodTearProjectile;
import com.mickdev.necromency.registry.item.TearProjectile;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Necromency.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<TearProjectile>> TEAR =
            ENTITIES.register("tear", () -> {

                ResourceKey<EntityType<?>> key = ResourceKey.create(
                        Registries.ENTITY_TYPE,
                        ResourceLocation.fromNamespaceAndPath(Necromency.MODID, "tear")
                );

                return EntityType.Builder
                        .<TearProjectile>of(TearProjectile::new, MobCategory.MISC)
                        .sized(0.25F, 0.25F)
                        .clientTrackingRange(8)
                        .updateInterval(10)
                        .build(key);
            });
    public static final DeferredHolder<EntityType<?>, EntityType<BloodTearProjectile>> BloodTEAR =
            ENTITIES.register("bloodtear", () -> {

                ResourceKey<EntityType<?>> key = ResourceKey.create(
                        Registries.ENTITY_TYPE,
                        ResourceLocation.fromNamespaceAndPath(Necromency.MODID, "bloodtear")
                );

                return EntityType.Builder
                        .<BloodTearProjectile>of(BloodTearProjectile::new, MobCategory.MISC)
                        .sized(0.25F, 0.25F)
                        .clientTrackingRange(8)
                        .updateInterval(10)
                        .build(key);
            });
    private ModEntities() {}
}
