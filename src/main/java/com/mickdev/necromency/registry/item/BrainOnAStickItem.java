package com.mickdev.necromency.registry.item;

import com.mickdev.necromency.registry.init.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class BrainOnAStickItem extends Item {

    public BrainOnAStickItem(Properties props) {
        super(props);
    }

    public static boolean isBrainOnAStick(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ModItems.BRAIN_ON_A_STICK.get());
    }
}
