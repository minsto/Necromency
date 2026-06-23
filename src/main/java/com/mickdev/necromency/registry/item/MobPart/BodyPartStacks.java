package com.mickdev.necromency.registry.item.MobPart;

import com.mickdev.necromency.registry.init.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Fabrique des stacks de morceaux de corps (NBT mob + type de partie).
 */
public final class BodyPartStacks {

    private BodyPartStacks() {}

    public static ItemStack create(BodyPartItem.PartType part, String mobId) {
        Item item = resolveItem(part, mobId);
        ItemStack stack = new ItemStack(item);
        BodyPartItem.setPart(stack, part);
        BodyPartItem.setMobId(stack, mobId);
        BodyPartItem.applyModel(stack);
        return stack;
    }

    public static ItemStack create(BodyPartItem.PartType part, EntityType<?> type) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        return create(part, id.toString());
    }

    private static Item resolveItem(BodyPartItem.PartType part, String mobId) {
        Item legacy = legacyItem(part, mobId);
        return legacy != null ? legacy : ModItems.BODY_PART.get();
    }

    @Nullable
    private static Item legacyItem(BodyPartItem.PartType part, String mobId) {
        if ("minecraft:chicken".equals(mobId)) {
            return switch (part) {
                case HEAD -> ModItems.Chiken_HEAD_PART.get();
                case BODY -> ModItems.Chiken_BODY_PART.get();
                case ARM_LEFT -> ModItems.Chiken_Left_Arms_PART.get();
                case ARM_RIGHT -> ModItems.Chiken_Right_ARMS_PART.get();
                case LEGS -> ModItems.CHICKEN_LEG_PART.get();
            };
        }
        if ("minecraft:zombie".equals(mobId)) {
            return switch (part) {
                case HEAD -> ModItems.ZOMBIE_HEAD_PART.get();
                case BODY -> ModItems.ZOMBIE_BODY_PART.get();
                case ARM_LEFT -> ModItems.ZOMBIE_Left_Arms_PART.get();
                case ARM_RIGHT -> ModItems.ZOMBIE_Right_ARMS_PART.get();
                case LEGS -> ModItems.ZOMBIE_LEG_PART.get();
            };
        }
        if ("minecraft:villager".equals(mobId) && part == BodyPartItem.PartType.HEAD) {
            return ModItems.VILLAGER_HEAD_PART.get();
        }
        return null;
    }
}
