package com.mickdev.necromency.registry.init.Fluid.Item;

import com.mickdev.necromency.registry.init.ModFluids;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BucketItem;



public class BloodItem extends BucketItem {
    public BloodItem(Item.Properties properties) {
        super(ModFluids.BLOOD_SOURCE.get(), properties.craftRemainder(Items.BUCKET).stacksTo(1)

        );
    }
}
