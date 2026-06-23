package com.mickdev.necromency.registry.BrainMaker;

import com.mickdev.necromency.registry.init.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Cerveaux typés (copper golem, allay, enderman, iron golem) : lien item ↔ mob source ↔ aptitude minion.
 */
public final class BrainTypes {

    public static final String ATTR_ORGANIZER = "necromency:organizer";
    public static final String ATTR_COLLECTOR = "necromency:collector";
    public static final String ATTR_TELEPORTER = "necromency:teleporter";
    public static final String ATTR_HEAVY_STRIKER = "necromency:heavy_striker";

    public record Entry(Item item, ResourceLocation sourceMob, String attributeId) {}

    private static final Map<Item, Entry> BY_ITEM = new LinkedHashMap<>();

    static {
        register(ModItems.BRAIN_COPPER_GOLEM.get(), "minecraft:copper_golem", ATTR_ORGANIZER);
        register(ModItems.BRAIN_ALLAY.get(), "minecraft:allay", ATTR_COLLECTOR);
        register(ModItems.BRAIN_ENDERMAN.get(), "minecraft:enderman", ATTR_TELEPORTER);
        register(ModItems.BRAIN_IRON_GOLEM.get(), "minecraft:iron_golem", ATTR_HEAVY_STRIKER);
    }

    private BrainTypes() {}

    private static void register(Item item, String mobPath, String attributeId) {
        BY_ITEM.put(item, new Entry(item, ResourceLocation.parse(mobPath), attributeId));
    }

    public static boolean isTypedBrain(ItemStack stack) {
        return !stack.isEmpty() && BY_ITEM.containsKey(stack.getItem());
    }

    @Nullable
    public static Entry get(ItemStack stack) {
        if (stack.isEmpty()) return null;
        return BY_ITEM.get(stack.getItem());
    }

    /** Drop de cerveau typé (8 %) pour un mob tué au brain cutter. */
    @Nullable
    public static ItemStack brainDropForEntity(EntityType<?> type) {
        for (Entry e : BY_ITEM.values()) {
            if (e.sourceMob().equals(type.builtInRegistryHolder().key().location())) {
                return new ItemStack(e.item());
            }
        }
        return null;
    }

    public static Optional<Entry> entryForAttribute(String attributeId) {
        return BY_ITEM.values().stream()
                .filter(e -> e.attributeId().equals(attributeId))
                .findFirst();
    }
}
