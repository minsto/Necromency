package com.mickdev.necromency.registry.item.MobPart;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import javax.annotation.Nullable;

public class BodyPartItem extends Item {

    public static final String TAG_PART = "Part";   // ex "legs"
    public static final String TAG_MOB  = "MobId";  // ex "minecraft:cow"

    public enum PartType {
        LEGS("legs"),
        BODY("body"),
        ARM_LEFT("arm_left"),
        ARM_RIGHT("arm_right"),
        HEAD("head");

        public final String id;
        PartType(String id) { this.id = id; }

        @Nullable
        public static PartType fromId(String id) {
            for (PartType p : values()) if (p.id.equals(id)) return p;
            return null;
        }
    }

    private final PartType fixedPart;
    @Nullable private final String fixedMobId; // si tu veux forcer ex "minecraft:chicken" sinon null

    public BodyPartItem(PartType fixedPart, @Nullable String fixedMobId, Properties props) {
        super(props);
        this.fixedPart = fixedPart;
        this.fixedMobId = fixedMobId;
    }

    /** Type de partie imposé par l'item (fallback quand le NBT {@code Part} est absent, ex. items legacy). */
    public PartType getFixedPart() {
        return fixedPart;
    }

    @Nullable
    public String getFixedMobId() {
        return fixedMobId;
    }

    /**
     * Type de partie effectif : NBT {@code Part}, sinon type fixe de l'item, sinon heuristique sur l'id
     * (ex. {@code zombie_right_arm} → {@link PartType#ARM_RIGHT}).
     */
    @Nullable
    public static PartType resolvePartType(ItemStack stack) {
        PartType part = getPart(stack);
        if (part != null) {
            return part;
        }
        if (stack.getItem() instanceof BodyPartItem item) {
            PartType fixed = item.getFixedPart();
            if (fixed == PartType.ARM_LEFT || fixed == PartType.ARM_RIGHT) {
                return fixed;
            }
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String path = id.getPath();
        if (path.contains("right_arm")) {
            return PartType.ARM_RIGHT;
        }
        if (path.contains("left_arm")) {
            return PartType.ARM_LEFT;
        }
        return null;
    }

    public static boolean isRightArm(ItemStack stack) {
        return resolvePartType(stack) == PartType.ARM_RIGHT;
    }

    public static boolean isLeftArm(ItemStack stack) {
        return resolvePartType(stack) == PartType.ARM_LEFT;
    }

    /**
     * Convertit un bras gauche ↔ droit en conservant le mob ({@code MobId}). Bras droit → bras gauche du même mob,
     * et inversement. Renvoie {@link ItemStack#EMPTY} si la stack n'est pas un bras valide.
     */
    public static ItemStack flipArmSide(ItemStack arm) {
        PartType part = resolvePartType(arm);
        String mobId = resolveMobId(arm);
        if (part == null || mobId == null) {
            return ItemStack.EMPTY;
        }
        if (part != PartType.ARM_LEFT && part != PartType.ARM_RIGHT) {
            return ItemStack.EMPTY;
        }
        PartType flipped = part == PartType.ARM_LEFT ? PartType.ARM_RIGHT : PartType.ARM_LEFT;
        ItemStack out = BodyPartStacks.create(flipped, mobId);
        out.setCount(arm.getCount());
        return out;
    }

    /** « Tête de Zombie », « Jambes de Golem de fer »… composé depuis le nom du mob. */
    @Override
    public Component getName(ItemStack stack) {
        PartType part = getPart(stack);
        if (part == null) {
            part = fixedPart;
        }
        String mobId = resolveMobId(stack);
        if (part != null && mobId != null) {
            ResourceLocation id = ResourceLocation.tryParse(mobId);
            if (id != null) {
                Component mobName = BuiltInRegistries.ENTITY_TYPE.getOptional(id)
                        .<Component>map(type -> Component.translatable(type.getDescriptionId()))
                        .orElseGet(() -> Component.literal(id.getPath()));
                return Component.translatable("item.necromency.body_part." + part.id, mobName);
            }
        }
        return super.getName(stack);
    }

    // ✅ Signature 1.21.x chez toi (ServerLevel + EquipmentSlot)
    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        ensureData(stack);
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack s = super.getDefaultInstance();
        ensureData(s);
        return s;
    }

    // ---------- CREATE ----------
    public static ItemStack create(Item item, PartType part, EntityType<?> mobType) {
        ItemStack stack = new ItemStack(item);
        setPart(stack, part);
        setMobId(stack, BuiltInRegistries.ENTITY_TYPE.getKey(mobType).toString());
        applyModel(stack);
        return stack;
    }

    // ---------- SET (CustomData) ----------
    public static void setPart(ItemStack stack, PartType part) {
        stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, cd -> {
            CompoundTag t = cd.copyTag();
            t.putString(TAG_PART, part.id);
            return CustomData.of(t);
        });
    }

    public static void setMobId(ItemStack stack, String mobId) {
        stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, cd -> {
            CompoundTag t = cd.copyTag();
            t.putString(TAG_MOB, mobId);
            return CustomData.of(t);
        });
    }

    // ---------- GET (CustomData) ----------
    @Nullable
    private static CompoundTag getDataTag(ItemStack stack) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        return cd == null ? null : cd.copyTag();
    }

    @Nullable
    public static PartType getPart(ItemStack stack) {
        CompoundTag tag = getDataTag(stack);
        if (tag == null) return null;

        // ✅ ton mapping renvoie Optional<String>
        String id = tag.getString(TAG_PART).orElse("");
        if (id.isEmpty()) return null;

        return PartType.fromId(id);
    }

    @Nullable
    public static String getMobId(ItemStack stack) {
        CompoundTag tag = getDataTag(stack);
        if (tag == null) return null;

        String mob = tag.getString(TAG_MOB).orElse("");
        return mob.isEmpty() ? null : mob;
    }

    /** NBT MobId, sinon id fixe de l'item (ex. {@code villager_head} → {@code minecraft:villager}). */
    @Nullable
    public static String resolveMobId(ItemStack stack) {
        String fromTag = getMobId(stack);
        if (fromTag != null && !fromTag.isBlank()) {
            return fromTag;
        }
        if (stack.getItem() instanceof BodyPartItem item && item.fixedMobId != null) {
            return item.fixedMobId;
        }
        return null;
    }

    public static boolean isPart(ItemStack stack, PartType part) {
        return getPart(stack) == part;
    }

    public static boolean hasValidMob(ItemStack stack) {
        String id = resolveMobId(stack);
        return id != null && ResourceLocation.tryParse(id) != null;
    }

    public static void applyModel(ItemStack stack) {
        if (!(stack.getItem() instanceof BodyPartItem item)) {
            return;
        }

        String mob = resolveMobId(stack);
        PartType part = getPart(stack);
        if (mob == null || part == null) {
            return;
        }

        // zombie_head, chiken_body, … : modèle dédié dans assets/items/<id>.json
        if (item.fixedMobId != null && part == item.fixedPart) {
            return;
        }

        ResourceLocation modelId = BodyPartModels.itemModelId(mob, part);
        if (modelId != null) {
            stack.set(DataComponents.ITEM_MODEL, modelId);
            stack.remove(DataComponents.CUSTOM_MODEL_DATA);
        }
    }

    private void ensureData(ItemStack stack) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = (cd != null) ? cd.copyTag() : new CompoundTag();

        boolean changed = false;

        if (!tag.contains(TAG_PART)) {
            tag.putString(TAG_PART, fixedPart.id);
            changed = true;
        }

        if (fixedMobId != null && !tag.contains(TAG_MOB)) {
            tag.putString(TAG_MOB, fixedMobId);
            changed = true;
        }

        if (changed) {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }

        if (getPart(stack) != null && resolveMobId(stack) != null
                && stack.get(DataComponents.ITEM_MODEL) == null) {
            applyModel(stack);
        }
    }
}