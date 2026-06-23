package com.mickdev.necromency.Client.Util;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class PartProfiles {

    private static final ResourceLocation RL_CHICKEN = ResourceLocation.parse("minecraft:chicken");
    private static final ResourceLocation RL_ZOMBIE = ResourceLocation.parse("minecraft:zombie");

    private static final Map<ResourceLocation, PartProfile> HEAD = new HashMap<>();
    private static final Map<ResourceLocation, PartProfile> LEGS = new HashMap<>();
    private static final Map<ResourceLocation, PartProfile> BODY = new HashMap<>();
    private static final Map<ResourceLocation, PartProfile> ARM_L = new HashMap<>();
    private static final Map<ResourceLocation, PartProfile> ARM_R = new HashMap<>();

    static {
        ResourceLocation VILLAGER = ResourceLocation.parse("minecraft:villager");
        ResourceLocation CHICKEN = ResourceLocation.parse("minecraft:chicken");
        ResourceLocation ZOMBIE = ResourceLocation.parse("minecraft:zombie");

        HEAD.put(VILLAGER, PartProfile.identity());

        // Poulet sur squelette humanoïde (offsets / échelles)
        HEAD.put(CHICKEN, new PartProfile(
                0.85f,
                0f, -4.0f, 0f,
                0f, 0f, 0f
        ));
        BODY.put(CHICKEN, new PartProfile(
                0.75f,
                0f, -1.5f, 0f,
                0f, 0f, 0f
        ));
        ARM_L.put(CHICKEN, new PartProfile(
                0.35f,
                0f, -1.0f, 0f,
                0f, 0f, 0f
        ));
        ARM_R.put(CHICKEN, new PartProfile(
                0.35f,
                0f, -1.0f, 0f,
                0f, 0f, 0f
        ));
        LEGS.put(CHICKEN, new PartProfile(
                0.45f,
                0f, 0.5f, 0f,
                0f, 0f, 0f
        ));

        PartProfile id = PartProfile.identity();
        HEAD.put(ZOMBIE, id);
        BODY.put(ZOMBIE, id);
        ARM_L.put(ZOMBIE, id);
        ARM_R.put(ZOMBIE, id);
        LEGS.put(ZOMBIE, id);
    }

    public static PartProfile get(PartSlot slot, ResourceLocation type) {
        if (type == null) return PartProfile.identity();
        return switch (slot) {
            case HEAD -> HEAD.getOrDefault(type, PartProfile.identity());
            case BODY -> BODY.getOrDefault(type, PartProfile.identity());
            case ARM_L -> ARM_L.getOrDefault(type, PartProfile.identity());
            case ARM_R -> ARM_R.getOrDefault(type, PartProfile.identity());
            case LEGS -> LEGS.getOrDefault(type, PartProfile.identity());
        };
    }

    /**
     * Tête sur composite poulet : garder le profil du type de tête choisi (ex. zombie),
     * pas celui du poulet (évite tête zombie flottante).
     */
    public static PartProfile getHeadProfile(ResourceLocation headType, @Nullable ResourceLocation anchor) {
        return get(PartSlot.HEAD, headType);
    }

    private PartProfiles() {}
}
