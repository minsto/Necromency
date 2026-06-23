package com.mickdev.necromency.registry.block;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class SkullWallData {

    public static final String TAG_BASE = "Base";
    public static final String TAG_SKULL1 = "Skull1";
    public static final String TAG_SKULL2 = "Skull2";
    public static final String TAG_SKULL3 = "Skull3";

    private SkullWallData() {}

    public static void writeDefaults(ItemStack stack) {
        write(stack, "minecraft:obsidian", "skeleton", "zombie", "creeper");
    }

    public static void write(ItemStack stack, String base, String skull1, String skull2, String skull3) {
        stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, cd -> {
            CompoundTag tag = cd.copyTag();
            tag.putString(TAG_BASE, base);
            tag.putString(TAG_SKULL1, skull1);
            tag.putString(TAG_SKULL2, skull2);
            tag.putString(TAG_SKULL3, skull3);
            return CustomData.of(tag);
        });
    }

    public static void applyToBlockEntity(ItemStack stack, BlockEntity be) {
        if (!(be instanceof SkullWallBlockEntity wall)) return;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return;
        CompoundTag tag = data.copyTag();
        wall.setSkullType(tag.getStringOr(TAG_SKULL1, "skeleton"));
        wall.setSkull2(tag.getStringOr(TAG_SKULL2, "zombie"));
        wall.setSkull3(tag.getStringOr(TAG_SKULL3, "creeper"));
        wall.setBaseBlock(tag.getStringOr(TAG_BASE, "minecraft:obsidian"));
    }
}
