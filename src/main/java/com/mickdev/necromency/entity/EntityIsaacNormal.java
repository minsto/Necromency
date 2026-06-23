package com.mickdev.necromency.entity;

import com.mickdev.necromency.registry.NecromencyEntities;
import com.mickdev.necromency.registry.item.TearProjectile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class EntityIsaacNormal extends Monster implements RangedAttackMob {

    public EntityIsaacNormal(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        AttributeSupplier.Builder builder = Mob.createMobAttributes();
        builder = builder.add(Attributes.MOVEMENT_SPEED, 0.3);
        builder = builder.add(Attributes.MAX_HEALTH, 10);
        builder = builder.add(Attributes.ARMOR, 0);
        builder = builder.add(Attributes.ATTACK_DAMAGE, 3);
        builder = builder.add(Attributes.FOLLOW_RANGE, 16);
        builder = builder.add(Attributes.STEP_HEIGHT, 0.6);
        return builder;
    }


    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, new RandomStrollGoal(this, 1));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new FloatGoal(this));
        this.goalSelector.addGoal(1, new ShootTearGoal(this));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (level() instanceof ServerLevel server) {
            shoot(server, this, target);
            playSound(SoundEvents.GHAST_SHOOT, 1.0F, 1.0F);
        }
    }
    private static void shoot(ServerLevel level, LivingEntity shooter, LivingEntity target) {
        TearProjectile proj = new TearProjectile(level, shooter);

        // spawn au niveau des yeux (visage)
        var eye = shooter.getEyePosition();
        proj.setPos(eye.x, eye.y - 0.05, eye.z);

        // direction vers les yeux de la cible
        var dir = target.getEyePosition().subtract(eye);

        // vitesse + précision
        proj.shoot(dir.x, dir.y, dir.z, 1.6F, 0.2F);

        level.addFreshEntity(proj);
    }
    public class ShootTearGoal extends Goal {
        private final Mob mob;
        private int cooldownTicks;

        public ShootTearGoal(Mob mob) {
            this.mob = mob;
        }

        @Override
        public boolean canUse() {
            return mob.getTarget() != null && mob.getTarget().isAlive();
        }

        @Override
        public void tick() {
            LivingEntity target = mob.getTarget();
            if (target == null) return;

            // Option: distance max
            double distSq = mob.distanceToSqr(target);
            if (distSq > 20 * 20) return;

            // regarde la cible (visuellement)
            mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

            if (cooldownTicks > 0) {
                cooldownTicks--;
                return;
            }

            if (mob.level() instanceof ServerLevel level) {
                shootTear(level, mob, target);
                cooldownTicks = 20; // 20 ticks = 1 seconde
            }
        }

        private static void shootTear(ServerLevel level, LivingEntity shooter, LivingEntity target) {
            TearProjectile tear = new TearProjectile(level, shooter);

            tear.setPos(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ());

            var dir = target.getEyePosition().subtract(shooter.getEyePosition());
            tear.shoot(dir.x, dir.y, dir.z, 1.6F, 0.5F);

            level.addFreshEntity(tear);
        }
    }
    @Override
    public void die(DamageSource src) {
        super.die(src);

        if (this.level() instanceof ServerLevel sl) {
            // ✅ en 1.21.x: évite EntityType#create(...)
            EntityIsaacBlood blood = new EntityIsaacBlood(NecromencyEntities.ISAAC_BLOOD.get(), sl);

            blood.setPos(this.getX(), this.getY(), this.getZ());
            blood.setYRot(this.getYRot());
            blood.setXRot(this.getXRot());

            sl.addFreshEntity(blood);
        }
    }
}