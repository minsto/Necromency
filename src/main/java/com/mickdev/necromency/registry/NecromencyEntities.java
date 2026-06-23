package com.mickdev.necromency.registry;

import com.mickdev.necromency.Necromency;
import com.mickdev.necromency.entity.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NecromencyEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Necromency.MODID);

    private static final ResourceLocation TEDDY_ID =
            ResourceLocation.fromNamespaceAndPath(Necromency.MODID, "teddy");

    private static final ResourceKey<EntityType<?>> TEDDY_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE, TEDDY_ID);


    public static final DeferredHolder<EntityType<?>, EntityType<EntityTeddy>> TEDDY =
            ENTITIES.register("teddy", () ->
                    EntityType.Builder.of(EntityTeddy::new, MobCategory.CREATURE)
                            .sized(0.9F, 1.3F)
                            .build(TEDDY_KEY) // ✅ plus de .toString()
            );


    public static final DeferredHolder<EntityType<?>, EntityType<MinionEntity>> MINION =
            registerMob("minion", MinionEntity::new, MobCategory.MONSTER, 0.6F, 1.8F, 25, 5);


    public static final DeferredHolder<EntityType<?>, EntityType<EntityIsaacBody>> ISAAC_BODY =
            registerMob("isaac_body", EntityIsaacBody::new, MobCategory.MONSTER, 0.6F, 1.8F, 25, 5);

    public static final DeferredHolder<EntityType<?>, EntityType<EntityIsaacNormal>> ISAAC_NORMAL =
            registerMob("isaac_normal", EntityIsaacNormal::new, MobCategory.MONSTER, 0.6F, 1.8F, 25, 5);

    public static final DeferredHolder<EntityType<?>, EntityType<EntityIsaacBlood>> ISAAC_BLOOD =
            registerMob("isaac_blood", EntityIsaacBlood::new, MobCategory.MONSTER, 0.6F, 1.8F, 25, 5);

    public static final DeferredHolder<EntityType<?>, EntityType<EntityIsaacHead>> ISAAC_HEAD =
            registerMob("isaac_head", EntityIsaacHead::new, MobCategory.MONSTER, 0.6F, 1.8F, 25, 5);
    public static final DeferredHolder<EntityType<?>, EntityType<NightCrawlerEntity>> NIGHTCRAWLER =
            registerMob("nightcrawler",NightCrawlerEntity::new,MobCategory.MONSTER,0.9f,0.7f,30,5);



    // helper
    private static <T extends net.minecraft.world.entity.Mob> DeferredHolder<EntityType<?>, EntityType<T>> registerMob(
            String id,
            EntityType.EntityFactory<T> factory,
            MobCategory cat,
            float w, float h,
            int trackingRange,
            int updateInterval
    ) {
        ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(Necromency.MODID, id);
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, rl);

        return ENTITIES.register(id, () ->
                EntityType.Builder.of(factory, cat)
                        .sized(w, h)
                        .clientTrackingRange(trackingRange)
                        .updateInterval(updateInterval)
                        .build(key)
        );
}
}