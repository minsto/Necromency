package com.mickdev.necromency.entity;

import com.mickdev.necromency.registry.NecromencyEntities;
import com.mickdev.necromency.registry.init.ModSounds;
import com.mickdev.necromency.registry.item.BloodTearProjectile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class EntityIsaacBlood extends Monster implements RangedAttackMob {

    public EntityIsaacBlood(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 75.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D);
    }
    @Override
    protected void registerGoals() {
        super.registerGoals();
      //  this.goalSelector.addGoal(2, new RangedAttackGoal(this, 0.45D, 18, 50.0F));
        this.goalSelector.addGoal(2, new RandomStrollGoal(this, 1));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new FloatGoal(this));
        this.goalSelector.addGoal(1, new ShootTearGoal(this));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }
    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (this.level() instanceof ServerLevel level) {
            ShootTearGoal.shootTear(level, this, target);
        }
        this.playSound(ModSounds.TEAR.get(), 1.0F, 0.85F + this.random.nextFloat() * 0.2F);
    }

    protected boolean shouldSplitOnDeath() {
        return true;
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
            BloodTearProjectile tear = new BloodTearProjectile(level, shooter);

            tear.setPos(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ());

            var dir = target.getEyePosition().subtract(shooter.getEyePosition());
            tear.shoot(dir.x, dir.y, dir.z, 1.6F, 0.5F);

            level.addFreshEntity(tear);
        }
    }
    @Override
    public void die(DamageSource src) {
        super.die(src);

        if (!shouldSplitOnDeath()) return;

        if (this.level() instanceof ServerLevel sl) {
            EntityIsaacHead head = new EntityIsaacHead(NecromencyEntities.ISAAC_HEAD.get(), sl);
            EntityIsaacBody body = new EntityIsaacBody(NecromencyEntities.ISAAC_BODY.get(), sl);

            head.setPos(this.getX(), this.getY() + 1.0D, this.getZ());
            head.setYRot(this.getYRot());
            head.setXRot(this.getXRot());
            sl.addFreshEntity(head);

            body.setPos(this.getX(), this.getY(), this.getZ());
            body.setYRot(this.getYRot());
            body.setXRot(this.getXRot());
            sl.addFreshEntity(body);
        }
    }
}