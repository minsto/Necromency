package com.mickdev.necromency.necroapi;

import com.mickdev.necromency.entity.MinionEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.HashMap;
import java.util.Map;

/**
 * Bonus de stats par morceau de mob (port 1.12 {@code NecroEntity*} / {@code addAttributeMods}).
 * Les valeurs sont ajoutées en {@link AttributeModifier.Operation#ADD_VALUE} sur les bases vanilla du zombie.
 */
public final class MinionPartAttributes {

    private record PartMods(double health, double followRange, double knockback, double speed, double damage) {}

    private static final Map<String, Map<BodyPartLocation, PartMods>> BY_MOB = new HashMap<>();

    static {
        registerVanillaMobs();
    }

    private MinionPartAttributes() {}

    public static void applyFromParts(MinionEntity minion) {
        clearPartModifiers(minion);

        addPart(minion, BodyPartLocation.Head, minion.getHeadId());
        addPart(minion, BodyPartLocation.Torso, minion.getBodyId());
        addPart(minion, BodyPartLocation.ArmLeft, minion.getArmLId());
        addPart(minion, BodyPartLocation.ArmRight, minion.getArmRId());
        addPart(minion, BodyPartLocation.Legs, minion.getLegsId());

        resetMovementSpeedToZombie(minion);
        minion.setHealth(minion.getMaxHealth());
    }

    /** Vitesse fixe zombie vanilla — les bonus par morceau ne s'appliquent pas à la marche. */
    private static void resetMovementSpeedToZombie(MinionEntity minion) {
        AttributeInstance speed = minion.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        speed.getModifiers().stream()
                .filter(MinionPartAttributes::isPartModifier)
                .map(AttributeModifier::id)
                .toList()
                .forEach(speed::removeModifier);
        speed.setBaseValue(0.23D);
    }

    private static void clearPartModifiers(MinionEntity minion) {
        for (AttributeInstance inst : new AttributeInstance[] {
                minion.getAttribute(Attributes.MAX_HEALTH),
                minion.getAttribute(Attributes.FOLLOW_RANGE),
                minion.getAttribute(Attributes.KNOCKBACK_RESISTANCE),
                minion.getAttribute(Attributes.MOVEMENT_SPEED),
                minion.getAttribute(Attributes.ATTACK_DAMAGE)
        }) {
            if (inst != null) {
                inst.getModifiers().stream()
                        .filter(MinionPartAttributes::isPartModifier)
                        .map(AttributeModifier::id)
                        .toList()
                        .forEach(inst::removeModifier);
            }
        }
    }

    private static void addPart(MinionEntity minion, BodyPartLocation slot, ResourceLocation mobId) {
        PartMods mods = lookup(mobId, slot);
        if (mods == null) return;

        apply(minion, slot, Attributes.MAX_HEALTH, mods.health());
        apply(minion, slot, Attributes.FOLLOW_RANGE, mods.followRange());
        apply(minion, slot, Attributes.KNOCKBACK_RESISTANCE, mods.knockback());
        apply(minion, slot, Attributes.ATTACK_DAMAGE, mods.damage());
    }

    private static void apply(MinionEntity minion, BodyPartLocation slot, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr, double amount) {
        if (amount == 0.0D) return;
        AttributeInstance inst = minion.getAttribute(attr);
        if (inst == null) return;
        ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(
                "necromency",
                "minion_part/" + slot.name().toLowerCase() + "/" + attr.unwrapKey().orElseThrow().location().getPath()
        );
        inst.addPermanentModifier(new AttributeModifier(modId, amount, AttributeModifier.Operation.ADD_VALUE));
    }

    private static boolean isPartModifier(AttributeModifier modifier) {
        String id = modifier.id().toString();
        return id.startsWith("necromency:minion_part/") || id.startsWith("necromency:minion_");
    }

    private static PartMods lookup(ResourceLocation mobId, BodyPartLocation slot) {
        if (mobId == null) return null;
        Map<BodyPartLocation, PartMods> map = BY_MOB.get(mobId.toString());
        if (map == null) map = BY_MOB.get(mobId.getPath());
        return map != null ? map.get(slot) : null;
    }

    private static void register(String key, BodyPartLocation slot, double hp, double fr, double kb, double spd, double dmg) {
        BY_MOB.computeIfAbsent(key, k -> new HashMap<>()).put(slot, new PartMods(hp, fr, kb, spd, dmg));
    }

    private static void registerVanillaMobs() {
        // Zombie (1.12 NecroEntityZombie)
        register("minecraft:zombie", BodyPartLocation.Head, 1, 1, 0, 0, 1);
        register("minecraft:zombie", BodyPartLocation.Torso, 2, 0, 0, 0, 0);
        register("minecraft:zombie", BodyPartLocation.ArmLeft, 0.5, 0, 0, 0, 0.5);
        register("minecraft:zombie", BodyPartLocation.ArmRight, 0.5, 0, 0, 0, 0.5);
        register("minecraft:zombie", BodyPartLocation.Legs, 1, 0, 3, 3, 0);

        register("minecraft:skeleton", BodyPartLocation.Head, 0.5, 1, 0, 0, 0.5);
        register("minecraft:skeleton", BodyPartLocation.Torso, 1, 0, 0, 0, 0);
        register("minecraft:skeleton", BodyPartLocation.ArmLeft, 0.25, 0, 0, 0, 0.5);
        register("minecraft:skeleton", BodyPartLocation.ArmRight, 0.25, 0, 0, 0, 0.5);
        register("minecraft:skeleton", BodyPartLocation.Legs, 0.5, 0, 2, 2, 0);

        register("minecraft:creeper", BodyPartLocation.Head, 0.5, 1, 0, 0, 0);
        register("minecraft:creeper", BodyPartLocation.Torso, 1, 0, 0, 0, 0);
        register("minecraft:creeper", BodyPartLocation.Legs, 0.25, 0, 3, 3, 0);

        register("minecraft:spider", BodyPartLocation.Head, 0.5, 1, 0, 0, 0.5);
        register("minecraft:spider", BodyPartLocation.Torso, 1, 0, 0, 0, 0);
        register("minecraft:spider", BodyPartLocation.Legs, 0.5, 0, 1, 2, 0.5);

        register("minecraft:cave_spider", BodyPartLocation.Head, 0.5, 1, 0, 0, 0.5);
        register("minecraft:cave_spider", BodyPartLocation.Torso, 2, 0, 0, 0, 0);
        register("minecraft:cave_spider", BodyPartLocation.Legs, 0.5, 0, 1, 2, 0.5);

        register("minecraft:enderman", BodyPartLocation.Head, 1, 1, 1, 1, 0.5);
        register("minecraft:enderman", BodyPartLocation.Torso, 4, 0, 1, 0, 0);
        register("minecraft:enderman", BodyPartLocation.ArmLeft, 1, 0, 0, 0, 1.5);
        register("minecraft:enderman", BodyPartLocation.ArmRight, 1, 0, 0, 0, 1.5);
        register("minecraft:enderman", BodyPartLocation.Legs, 1, 0, 4, 3, 0);

        register("minecraft:iron_golem", BodyPartLocation.Head, 1, 1, 2, 0, 0.5);
        register("minecraft:iron_golem", BodyPartLocation.Torso, 5, 0, 2, 0, 0);
        register("minecraft:iron_golem", BodyPartLocation.ArmLeft, 1, 0, 1, 0, 1.5);
        register("minecraft:iron_golem", BodyPartLocation.ArmRight, 1, 0, 1, 0, 1.5);
        register("minecraft:iron_golem", BodyPartLocation.Legs, 4, 0, 3, 1, 0);

        register("minecraft:witch", BodyPartLocation.Head, 1.5, 1, 0, 0, 0);
        register("minecraft:witch", BodyPartLocation.Torso, 2, 0, 0, 0, 0);
        register("minecraft:witch", BodyPartLocation.ArmLeft, 0.5, 0, 0, 0, 0.75);
        register("minecraft:witch", BodyPartLocation.ArmRight, 0.5, 0, 0, 0, 0.75);
        register("minecraft:witch", BodyPartLocation.Legs, 1.5, 0, 3, 3, 0);

        register("minecraft:chicken", BodyPartLocation.Head, 0.5, 1, 0, 0, 0.25);
        register("minecraft:chicken", BodyPartLocation.Torso, 0.25, 0, 0, 0, 0);
        register("minecraft:chicken", BodyPartLocation.ArmLeft, 0.1, 0, 0, 0, 0);
        register("minecraft:chicken", BodyPartLocation.ArmRight, 0.1, 0, 0, 0, 0);
        register("minecraft:chicken", BodyPartLocation.Legs, 0.1, 0, 0, 0.5, 0);

        register("minecraft:pig", BodyPartLocation.Head, 0.5, 1, 0, 0, 0);
        register("minecraft:pig", BodyPartLocation.Torso, 1, 0, 0, 0, 0);
        register("minecraft:pig", BodyPartLocation.ArmLeft, 0.25, 0, 0, 0, 0.25);
        register("minecraft:pig", BodyPartLocation.ArmRight, 0.25, 0, 0, 0, 0.25);
        register("minecraft:pig", BodyPartLocation.Legs, 0.25, 0, 1, 3, 0);

        register("minecraft:cow", BodyPartLocation.Head, 0.5, 1, 0, 0, 0);
        register("minecraft:cow", BodyPartLocation.Torso, 1, 0, 0, 0, 0);
        register("minecraft:cow", BodyPartLocation.ArmLeft, 0.25, 0, 0, 0, 0.25);
        register("minecraft:cow", BodyPartLocation.ArmRight, 0.25, 0, 0, 0, 0.25);
        register("minecraft:cow", BodyPartLocation.Legs, 0.25, 0, 1, 3, 0);

        register("minecraft:wolf", BodyPartLocation.Head, 2, 1, 1, 1, 2);
        register("minecraft:wolf", BodyPartLocation.Torso, 1, 0, 0, 0, 0);
        register("minecraft:wolf", BodyPartLocation.ArmLeft, 0.25, 0, 0, 0, 0.5);
        register("minecraft:wolf", BodyPartLocation.ArmRight, 0.25, 0, 0, 0, 0.5);
        register("minecraft:wolf", BodyPartLocation.Legs, 0.5, 0, 1, 3, 0);

        register("minecraft:villager", BodyPartLocation.Head, 0.5, 1, 0, 0, 0);
        register("minecraft:villager", BodyPartLocation.Torso, 1, 0, 0, 0, 0);
        register("minecraft:villager", BodyPartLocation.ArmLeft, 0.25, 0, 0, 0, 0.25);
        register("minecraft:villager", BodyPartLocation.ArmRight, 0.25, 0, 0, 0, 0.25);
        register("minecraft:villager", BodyPartLocation.Legs, 0.25, 0, 1, 3, 0);

        register("minecraft:piglin", BodyPartLocation.Head, 1, 1, 0, 0, 0.5);
        register("minecraft:piglin", BodyPartLocation.Torso, 2, 0, 0, 0, 0);
        register("minecraft:piglin", BodyPartLocation.ArmLeft, 0.5, 0, 0, 0, 0.5);
        register("minecraft:piglin", BodyPartLocation.ArmRight, 0.5, 0, 0, 0, 0.5);
        register("minecraft:piglin", BodyPartLocation.Legs, 1, 0, 3, 3, 0);

        register("minecraft:zombified_piglin", BodyPartLocation.Head, 1.5, 1, 0, 0, 0.5);
        register("minecraft:zombified_piglin", BodyPartLocation.Torso, 3, 0, 0, 0, 0);
        register("minecraft:zombified_piglin", BodyPartLocation.ArmLeft, 0.5, 0, 0, 0, 0.75);
        register("minecraft:zombified_piglin", BodyPartLocation.ArmRight, 0.5, 0, 0, 0, 0.75);
        register("minecraft:zombified_piglin", BodyPartLocation.Legs, 1.5, 0, 3, 3, 0);

        register("minecraft:sheep", BodyPartLocation.Head, 0.5, 1, 0, 0, 0);
        register("minecraft:sheep", BodyPartLocation.Torso, 1, 0, 0, 0, 0);
        register("minecraft:sheep", BodyPartLocation.ArmLeft, 0.25, 0, 0, 0, 0.25);
        register("minecraft:sheep", BodyPartLocation.ArmRight, 0.25, 0, 0, 0, 0.25);
        register("minecraft:sheep", BodyPartLocation.Legs, 0.25, 0, 1, 3, 0);

        register("minecraft:squid", BodyPartLocation.Head, 0.5, 1, 0, 0, 0.5);
        register("minecraft:squid", BodyPartLocation.Torso, 2, 0, 0, 0, 0);
        register("minecraft:squid", BodyPartLocation.Legs, 0.5, 0, 1, 2, 0.5);

        register("minecraft:wither_skeleton", BodyPartLocation.Head, 1, 1, 0, 0, 1);
        register("minecraft:wither_skeleton", BodyPartLocation.Torso, 2, 0, 0, 0, 0);
        register("minecraft:wither_skeleton", BodyPartLocation.ArmLeft, 0.5, 0, 0, 0, 0.5);
        register("minecraft:wither_skeleton", BodyPartLocation.ArmRight, 0.5, 0, 0, 0, 0.5);
        register("minecraft:wither_skeleton", BodyPartLocation.Legs, 1, 0, 3, 3, 0);

        register("minecraft:slime", BodyPartLocation.Torso, 2, 0, 2, 0, 0);
        register("minecraft:magma_cube", BodyPartLocation.Torso, 2, 0, 2, 0, 1);

        register("necromency:isaac_normal", BodyPartLocation.Head, 2, 1, 1, 0, 0.5);
        register("necromency:isaac_blood", BodyPartLocation.Head, 2, 1, 1, 0, 0.5);
        register("necromency:isaac_body", BodyPartLocation.Head, 2, 1, 1, 0, 0.5);
        register("necromency:isaac_head", BodyPartLocation.Head, 2, 1, 1, 0, 0.5);
        register("necromency:teddy", BodyPartLocation.Head, 1, 1, 1, 0, 1);
        register("necromency:nightcrawler", BodyPartLocation.Head, 1.5, 1, 0, 0, 1);
        register("necromency:nightcrawler", BodyPartLocation.Torso, 2, 0, 0, 0, 0);

        register("minecraft:copper_golem", BodyPartLocation.Head, 0.5, 1, 1, 0, 0.5);
        register("minecraft:copper_golem", BodyPartLocation.Torso, 2, 0, 1, 0, 0);
        register("minecraft:copper_golem", BodyPartLocation.ArmLeft, 0.5, 0, 0, 0, 0.75);
        register("minecraft:copper_golem", BodyPartLocation.ArmRight, 0.5, 0, 0, 0, 0.75);
        register("minecraft:copper_golem", BodyPartLocation.Legs, 1, 0, 2, 1, 0);

        register("minecraft:axolotl", BodyPartLocation.Head, 0.25, 1, 0, 0, 0.25);
        register("minecraft:axolotl", BodyPartLocation.Torso, 0.5, 0, 0, 0, 0);
        register("minecraft:axolotl", BodyPartLocation.ArmLeft, 0.1, 0, 0, 0, 0);
        register("minecraft:axolotl", BodyPartLocation.ArmRight, 0.1, 0, 0, 0, 0);
        register("minecraft:axolotl", BodyPartLocation.Legs, 0.25, 0, 0, 1, 0);

        register("minecraft:goat", BodyPartLocation.Head, 0.5, 1, 0, 0, 0.75);
        register("minecraft:goat", BodyPartLocation.Torso, 1, 0, 0, 0, 0);
        register("minecraft:goat", BodyPartLocation.ArmLeft, 0.25, 0, 0, 0, 0.25);
        register("minecraft:goat", BodyPartLocation.ArmRight, 0.25, 0, 0, 0, 0.25);
        register("minecraft:goat", BodyPartLocation.Legs, 0.5, 0, 2, 2, 0);

        register("minecraft:sniffer", BodyPartLocation.Head, 0.5, 1, 0, 0, 0);
        register("minecraft:sniffer", BodyPartLocation.Torso, 2, 0, 0, 0, 0);
        register("minecraft:sniffer", BodyPartLocation.ArmLeft, 0.5, 0, 0, 0, 0.25);
        register("minecraft:sniffer", BodyPartLocation.ArmRight, 0.5, 0, 0, 0, 0.25);
        register("minecraft:sniffer", BodyPartLocation.Legs, 1, 0, 1, 1, 0);

        register("minecraft:turtle", BodyPartLocation.Head, 0.25, 1, 0, 0, 0);
        register("minecraft:turtle", BodyPartLocation.Torso, 1, 0, 2, 0, 0);
        register("minecraft:turtle", BodyPartLocation.ArmLeft, 0.25, 0, 0, 0, 0);
        register("minecraft:turtle", BodyPartLocation.ArmRight, 0.25, 0, 0, 0, 0);
        register("minecraft:turtle", BodyPartLocation.Legs, 0.5, 0, 3, 1, 0);

        register("minecraft:fox", BodyPartLocation.Head, 0.5, 1, 0, 0, 0.5);
        register("minecraft:fox", BodyPartLocation.Torso, 0.5, 0, 0, 0, 0);
        register("minecraft:fox", BodyPartLocation.ArmLeft, 0.1, 0, 0, 0, 0.25);
        register("minecraft:fox", BodyPartLocation.ArmRight, 0.1, 0, 0, 0, 0.25);
        register("minecraft:fox", BodyPartLocation.Legs, 0.25, 0, 0, 4, 0);

        register("minecraft:cat", BodyPartLocation.Head, 0.5, 1, 0, 0, 0.5);
        register("minecraft:cat", BodyPartLocation.Torso, 0.5, 0, 0, 0, 0);
        register("minecraft:cat", BodyPartLocation.ArmLeft, 0.1, 0, 0, 0, 0.25);
        register("minecraft:cat", BodyPartLocation.ArmRight, 0.1, 0, 0, 0, 0.25);
        register("minecraft:cat", BodyPartLocation.Legs, 0.25, 0, 0, 4, 0);

        register("minecraft:ghast", BodyPartLocation.Head, 1, 2, 0, 0, 0.5);
        register("minecraft:ghast", BodyPartLocation.Torso, 2, 0, 0, 0, 0);
        register("minecraft:ghast", BodyPartLocation.Legs, 0.5, 0, 0, 2, 0);

        register("minecraft:happy_ghast", BodyPartLocation.Head, 1, 2, 0, 0, 0);
        register("minecraft:happy_ghast", BodyPartLocation.Torso, 3, 0, 0, 0, 0);
        register("minecraft:happy_ghast", BodyPartLocation.Legs, 0.5, 0, 0, 2, 0);

        register("minecraft:warden", BodyPartLocation.Head, 2, 2, 2, 0, 1);
        register("minecraft:warden", BodyPartLocation.Torso, 6, 0, 2, 0, 0);
        register("minecraft:warden", BodyPartLocation.ArmLeft, 1.5, 0, 1, 0, 2);
        register("minecraft:warden", BodyPartLocation.ArmRight, 1.5, 0, 1, 0, 2);
        register("minecraft:warden", BodyPartLocation.Legs, 3, 0, 3, 1, 0);

        register("minecraft:pillager", BodyPartLocation.Head, 1, 1, 0, 0, 0.5);
        register("minecraft:pillager", BodyPartLocation.Torso, 2, 0, 0, 0, 0);
        register("minecraft:pillager", BodyPartLocation.ArmLeft, 0.5, 0, 0, 0, 0.75);
        register("minecraft:pillager", BodyPartLocation.ArmRight, 0.5, 0, 0, 0, 0.75);
        register("minecraft:pillager", BodyPartLocation.Legs, 1, 0, 2, 3, 0);

        register("minecraft:wandering_trader", BodyPartLocation.Head, 0.5, 1, 0, 0, 0);
        register("minecraft:wandering_trader", BodyPartLocation.Torso, 1, 0, 0, 0, 0);
        register("minecraft:wandering_trader", BodyPartLocation.ArmLeft, 0.25, 0, 0, 0, 0.25);
        register("minecraft:wandering_trader", BodyPartLocation.ArmRight, 0.25, 0, 0, 0, 0.25);
        register("minecraft:wandering_trader", BodyPartLocation.Legs, 0.25, 0, 1, 3, 0);

        register("minecraft:illusioner", BodyPartLocation.Head, 1.5, 1, 0, 0, 0.5);
        register("minecraft:illusioner", BodyPartLocation.Torso, 2, 0, 0, 0, 0);
        register("minecraft:illusioner", BodyPartLocation.ArmLeft, 0.5, 0, 0, 0, 0.75);
        register("minecraft:illusioner", BodyPartLocation.ArmRight, 0.5, 0, 0, 0, 0.75);
        register("minecraft:illusioner", BodyPartLocation.Legs, 1.5, 0, 2, 3, 0);

        register("minecraft:armadillo", BodyPartLocation.Head, 0.5, 1, 1, 0, 0);
        register("minecraft:armadillo", BodyPartLocation.Torso, 1.5, 0, 2, 0, 0);
        register("minecraft:armadillo", BodyPartLocation.ArmLeft, 0.25, 0, 0, 0, 0);
        register("minecraft:armadillo", BodyPartLocation.ArmRight, 0.25, 0, 0, 0, 0);
        register("minecraft:armadillo", BodyPartLocation.Legs, 0.5, 0, 2, 1, 0);
    }
}
