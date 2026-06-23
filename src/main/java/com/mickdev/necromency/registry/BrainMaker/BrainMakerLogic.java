package com.mickdev.necromency.registry.BrainMaker;

import com.mickdev.necromency.registry.BrainMaker.Item.BrainCoreItem;
import com.mickdev.necromency.registry.init.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.LinkedHashSet;
import java.util.Set;

public final class BrainMakerLogic {

    private BrainMakerLogic() {}

    /** Cerveau typé (copper golem, allay…) ou brain_core déjà crafté (pour fusionner une 2e aptitude). */
    public static boolean isValidInput(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return BrainTypes.isTypedBrain(stack) || stack.is(ModItems.BRAIN_CORE.get());
    }

    /** @deprecated utiliser {@link #isValidInput} */
    public static boolean isBrain(ItemStack stack) {
        return isValidInput(stack);
    }

    /**
     * Fusionne 1–2 entrées (cerveau typé et/ou brain_core) + sang → {@code brain_core} cumulatif.
     */
    public static ItemStack makeBrainCore(ItemStack input1, ItemStack input2) {
        ItemStack out = new ItemStack(ModItems.BRAIN_CORE.get());

        CustomData cd = out.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = (cd != null) ? cd.copyTag() : new CompoundTag();

        Set<String> seenAttrs = new LinkedHashSet<>();
        ListTag attrList = new ListTag();
        ListTag brainIds = new ListTag();

        mergeInput(input1, attrList, brainIds, seenAttrs);
        if (!input2.isEmpty() && isValidInput(input2)
                && !ItemStack.isSameItemSameComponents(input1, input2)) {
            mergeInput(input2, attrList, brainIds, seenAttrs);
        }

        if (!brainIds.isEmpty()) {
            tag.putString("BrainId", brainIds.getString(0).orElse(""));
        }
        tag.put("BrainIds", brainIds);
        tag.put("NecromencyAttributes", attrList);

        out.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return out;
    }

    public static ItemStack makeBrainCore(ItemStack brainInput) {
        return makeBrainCore(brainInput, ItemStack.EMPTY);
    }

    private static void mergeInput(ItemStack stack, ListTag attrList, ListTag brainIds, Set<String> seenAttrs) {
        if (stack.isEmpty() || !isValidInput(stack)) return;

        if (stack.is(ModItems.BRAIN_CORE.get())) {
            for (String id : BrainCoreItem.readBrainIds(stack)) {
                brainIds.add(StringTag.valueOf(id));
            }
            for (String attr : BrainCoreItem.readAttributeIds(stack)) {
                if (seenAttrs.add(attr)) {
                    attrList.add(attr(attr, 1));
                }
            }
            return;
        }

        BrainTypes.Entry entry = BrainTypes.get(stack);
        if (entry == null) return;

        ResourceLocation rid = stack.getItem().builtInRegistryHolder().key().location();
        brainIds.add(StringTag.valueOf(rid.toString()));

        if (seenAttrs.add(entry.attributeId())) {
            attrList.add(attr(entry.attributeId(), 1));
        }
    }

    private static CompoundTag attr(String id, int lvl) {
        CompoundTag a = new CompoundTag();
        a.putString("Id", id);
        a.putInt("Lvl", lvl);
        return a;
    }
}
