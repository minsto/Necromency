package com.mickdev.necromency.necroapi;

import com.mickdev.necromency.entity.MinionEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Effets spéciaux à l'attaque selon les morceaux (extension du hook 1.12 {@code NecroEntityBase#attackEntityAsMob}).
 */
public final class MinionPartAttackEffects {

    private MinionPartAttackEffects() {}

    public static void applyAllParts(MinionEntity minion, LivingEntity target, float damage) {
        if (!(minion.level() instanceof ServerLevel)) return;
        applyForMobId(minion, target, minion.getHeadId(), BodyPartLocation.Head, damage);
        applyForMobId(minion, target, minion.getBodyId(), BodyPartLocation.Torso, damage);
        applyForMobId(minion, target, minion.getArmLId(), BodyPartLocation.ArmLeft, damage);
        applyForMobId(minion, target, minion.getArmRId(), BodyPartLocation.ArmRight, damage);
        applyForMobId(minion, target, minion.getLegsId(), BodyPartLocation.Legs, damage);
    }

    private static void applyForMobId(
            MinionEntity minion,
            LivingEntity target,
            ResourceLocation mobId,
            BodyPartLocation slot,
            float damage) {
        if (mobId == null || target == null) return;
        String id = mobId.toString();

        switch (id) {
            case "minecraft:witch" -> {
                if (slot == BodyPartLocation.Head || slot == BodyPartLocation.ArmLeft || slot == BodyPartLocation.ArmRight) {
                    target.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0));
                }
            }
            case "minecraft:cave_spider", "minecraft:spider" -> {
                if (slot == BodyPartLocation.Head || slot == BodyPartLocation.Legs) {
                    target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, slot == BodyPartLocation.Head ? 1 : 0));
                }
            }
            case "minecraft:wither_skeleton" -> {
                if (slot == BodyPartLocation.Head) {
                    target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));
                }
            }
            case "minecraft:creeper" -> {
                if (slot == BodyPartLocation.Head && minion.getRandom().nextFloat() < 0.12F) {
                    Level level = minion.level();
                    level.explode(minion, target.getX(), target.getY() + 0.5, target.getZ(),
                            1.2F, Level.ExplosionInteraction.MOB);
                }
            }
            case "minecraft:slime", "minecraft:magma_cube" -> {
                if (slot == BodyPartLocation.Torso) {
                    target.knockback(0.35, minion.getX() - target.getX(), minion.getZ() - target.getZ());
                }
            }
            case "minecraft:enderman" -> {
                if (slot == BodyPartLocation.ArmLeft || slot == BodyPartLocation.ArmRight) {
                    Vec3 push = target.position().subtract(minion.position()).normalize().scale(0.6);
                    target.push(push);
                }
            }
            case "necromency:isaac_normal", "necromency:isaac_blood", "necromency:isaac_body", "necromency:isaac_head" -> {
                if (slot == BodyPartLocation.Head) {
                    target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1));
                }
            }
            case "minecraft:goat" -> {
                if (slot == BodyPartLocation.Head) {
                    target.knockback(0.5, minion.getX() - target.getX(), minion.getZ() - target.getZ());
                }
            }
            case "minecraft:turtle" -> {
                if (slot == BodyPartLocation.Torso || slot == BodyPartLocation.Legs) {
                    target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 0));
                }
            }
            default -> { }
        }
    }
}
