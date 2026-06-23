package com.mickdev.necromency.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariants;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/**
 * Teddy animé — port fidèle de l'{@code EntityTeddy} 1.7.10 : compagnon apprivoisé
 * (Marche / Défend / Assis au clic droit), suit son propriétaire, effraie les monstres
 * en mode défense, 8 PV, lâche du cuir. Bruits de chien « arrangés » (jouet) :
 * sons de loup avec une voix aiguë.
 */
public class EntityTeddy extends TamableAnimal {

    public enum EntityState {
        WALKING, DEFENDING, SITTING
    }

    public EntityState entityState = EntityState.WALKING;

    public EntityTeddy(EntityType<? extends EntityTeddy> type, Level level) {
        super(type, level);
        setTame(true, false);
        setOrderedToSit(true);
        entityState = EntityState.SITTING;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 8.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.0D, 8.0F, 5.0F));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(6, new ScareMonstersGoal(this, 10.0F, 7.0F));
    }

    // =========================
    // États (1.7.10 : interact cycle)
    // =========================

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return super.mobInteract(player, hand);
        }
        if (!level().isClientSide()) {
            if (getOwnerReference() == null) {
                tame(player);
            }
            switch (entityState) {
                case WALKING -> {
                    entityState = EntityState.DEFENDING;
                    setOrderedToSit(false);
                }
                case DEFENDING -> {
                    entityState = EntityState.SITTING;
                    setOrderedToSit(true);
                }
                case SITTING -> {
                    entityState = EntityState.WALKING;
                    setOrderedToSit(false);
                }
            }
            player.displayClientMessage(
                    Component.literal("Animated Teddy is now " + entityState.toString().toLowerCase()), true);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput out) {
        super.addAdditionalSaveData(out);
        out.putInt("state", entityState.ordinal());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in);
        int idx = in.getInt("state").orElse(0);
        EntityState[] values = EntityState.values();
        entityState = values[Math.floorMod(idx, values.length)];
        setOrderedToSit(entityState == EntityState.SITTING);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    // =========================
    // Bruits de chien « arrangés »
    // =========================

    @Override
    public SoundEvent getAmbientSound() {
        return SoundEvents.WOLF_SOUNDS.get(WolfSoundVariants.SoundSet.CUTE).ambientSound().value();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WOLF_SOUNDS.get(WolfSoundVariants.SoundSet.CUTE).whineSound().value();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WOLF_SOUNDS.get(WolfSoundVariants.SoundSet.SAD).deathSound().value();
    }

    /** Voix de peluche : aboiements remontés d'un cran. */
    @Override
    public float getVoicePitch() {
        return 1.4F + this.random.nextFloat() * 0.2F;
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, net.minecraft.world.entity.Entity target) {
        return target.hurtServer(level, damageSources().mobAttack(this), 3.0F);
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }

    @Override
    public boolean isFood(net.minecraft.world.item.ItemStack stack) {
        return false;
    }

    /**
     * Port de {@code EntityAIScareEntities} 1.7.10 : en mode DÉFENSE, le teddy fonce sur
     * le monstre le plus proche et le fait fuir.
     */
    static class ScareMonstersGoal extends Goal {

        private final EntityTeddy teddy;
        private final float seekingRange;
        private final float scaringRange;
        private LivingEntity target;

        ScareMonstersGoal(EntityTeddy teddy, float seekingRange, float scaringRange) {
            this.teddy = teddy;
            this.seekingRange = seekingRange;
            this.scaringRange = scaringRange;
            setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (teddy.entityState != EntityState.DEFENDING) {
                return false;
            }
            target = findClosestMonster();
            return target != null;
        }

        @Override
        public boolean canContinueToUse() {
            return target != null && target.isAlive() && teddy.entityState == EntityState.DEFENDING;
        }

        @Override
        public void stop() {
            target = null;
            teddy.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (target == null) {
                return;
            }
            teddy.getNavigation().moveTo(target, 1.0D);
            if (teddy.distanceTo(target) < scaringRange && target instanceof PathfinderMob mob) {
                Vec3 away = DefaultRandomPos.getPosAway(mob, 16, 7, teddy.position());
                if (away != null && mob.distanceToSqr(away.x, away.y, away.z) >= mob.distanceToSqr(teddy)) {
                    mob.getNavigation().moveTo(away.x, away.y, away.z, 1.2D);
                }
            }
        }

        private LivingEntity findClosestMonster() {
            List<Monster> monsters = teddy.level().getEntitiesOfClass(Monster.class,
                    teddy.getBoundingBox().inflate(seekingRange));
            Monster closest = null;
            double best = Double.MAX_VALUE;
            for (Monster m : monsters) {
                double d = teddy.distanceToSqr(m);
                if (d < best) {
                    best = d;
                    closest = m;
                }
            }
            return closest;
        }
    }
}
