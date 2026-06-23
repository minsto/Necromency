package com.mickdev.necromency.registry.BrainMaker.Item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.Optional;
import java.util.function.Consumer;

public class BrainCoreItem extends Item {

    public static final String TAG_ATTR_LIST = "NecromencyAttributes";
    public static final String TAG_BRAIN_ID  = "BrainId";
    public static final String TAG_BRAIN_IDS = "BrainIds";

    public BrainCoreItem(Properties props) {
        super(props);
    }

    /** {@value TAG_BRAIN_ID} sur le stack Brain Core (ex. {@code necromency:brain_copper_golem}). */
    public static String readBrainId(ItemStack stack) {
        if (stack.isEmpty()) return "";
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd == null) return "";
        return cd.copyTag().getString(TAG_BRAIN_ID).orElse("");
    }

    /** Tous les cerveaux fusionnés (1 ou 2). */
    public static java.util.List<String> readBrainIds(ItemStack stack) {
        java.util.List<String> ids = new java.util.ArrayList<>();
        if (stack.isEmpty()) return ids;
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd == null) return ids;
        CompoundTag tag = cd.copyTag();
        tag.getList(TAG_BRAIN_IDS).ifPresent(list -> {
            for (int i = 0; i < list.size(); i++) {
                list.getString(i).ifPresent(ids::add);
            }
        });
        if (ids.isEmpty()) {
            tag.getString(TAG_BRAIN_ID).filter(s -> !s.isEmpty()).ifPresent(ids::add);
        }
        return ids;
    }

    /** Aptitudes du brain core (ex. {@code necromency:organizer}). */
    public static java.util.Set<String> readAttributeIds(ItemStack stack) {
        java.util.Set<String> out = new java.util.LinkedHashSet<>();
        if (stack.isEmpty()) return out;
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd == null) return out;
        cd.copyTag().getList(TAG_ATTR_LIST).ifPresent(list -> {
            for (int i = 0; i < list.size(); i++) {
                list.getCompound(i).flatMap(a -> a.getString("Id")).ifPresent(out::add);
            }
        });
        return out;
    }

    @Override
    public void appendHoverText(ItemStack itemstack,
                                TooltipContext context,
                                TooltipDisplay tooltipDisplay,
                                Consumer<Component> componentConsumer,
                                TooltipFlag flag) {

        super.appendHoverText(itemstack, context, tooltipDisplay, componentConsumer, flag);

        // ✅ 1.21 : custom data via components
        CustomData cd = itemstack.get(DataComponents.CUSTOM_DATA);
        if (cd == null) return;

        CompoundTag tag = cd.copyTag();

        // Brain Id
        tag.getString(TAG_BRAIN_ID).ifPresent(brainId ->
                componentConsumer.accept(Component.literal("Brain: " + brainId).withStyle(ChatFormatting.GRAY))
        );

        // Attributes list
        Optional<ListTag> optList = tag.getList(TAG_ATTR_LIST);
        if (optList.isEmpty()) return;

        ListTag list = optList.get();
        if (list.isEmpty()) return;

        componentConsumer.accept(Component.literal("Attributes:").withStyle(ChatFormatting.AQUA));

        for (int i = 0; i < list.size(); i++) {
            Optional<CompoundTag> optA = list.getCompound(i);
            if (optA.isEmpty()) continue;

            CompoundTag a = optA.get();
            String id = a.getString("Id").orElse("unknown");
            int lvl = a.getInt("Lvl").orElse(1);

            componentConsumer.accept(
                    Component.literal(" - " + id + " (Lvl " + lvl + ")").withStyle(ChatFormatting.YELLOW)
            );
        }
    }
}