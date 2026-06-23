package com.mickdev.necromency.entity;

import com.mickdev.necromency.Necromency;
import com.mickdev.necromency.entity.ai.MinionCollectItemsGoal;
import com.mickdev.necromency.entity.ai.MinionFollowOwnerGoal;
import com.mickdev.necromency.entity.ai.MinionOrganizeChestGoal;
import com.mickdev.necromency.entity.ai.MinionStayWhenOrderedGoal;
import com.mickdev.necromency.entity.ai.MinionTargetHostilesGoal;
import com.mickdev.necromency.entity.ai.MinionTargetPlayersGoal;
import com.mickdev.necromency.entity.ai.MinionTemptBrainStickGoal;
import com.mickdev.necromency.necroapi.MinionPartAttackEffects;
import com.mickdev.necromency.necroapi.MinionPartAttributes;
import com.mickdev.necromency.necroapi.MinionSaddleables;
import com.mickdev.necromency.registry.BrainMaker.BrainTypes;
import com.mickdev.necromency.registry.BrainMaker.Item.BrainCoreItem;
import com.mickdev.necromency.registry.item.BrainOnAStickItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MinionEntity extends Zombie {

    // ===== Default (summon safe) =====
    private static final ResourceLocation DEFAULT_TYPE = ResourceLocation.parse("minecraft:zombie");
    private static final ResourceLocation CHICKEN = ResourceLocation.parse("minecraft:chicken");

    // ===== Owner (UUID split) =====
    private static final EntityDataAccessor<Long> OWNER_MOST =
            SynchedEntityData.defineId(MinionEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Long> OWNER_LEAST =
            SynchedEntityData.defineId(MinionEntity.class, EntityDataSerializers.LONG);

    // ===== Parts (ResourceLocation as "namespace:path") =====
    private static final EntityDataAccessor<String> HEAD_ID =
            SynchedEntityData.defineId(MinionEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> BODY_ID =
            SynchedEntityData.defineId(MinionEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> ARM_L_ID =
            SynchedEntityData.defineId(MinionEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> ARM_R_ID =
            SynchedEntityData.defineId(MinionEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> LEGS_ID =
            SynchedEntityData.defineId(MinionEntity.class, EntityDataSerializers.STRING);

    /** Id du cerveau crafté (ex. {@code necromency:brain_copper_golem}), slot autel 6. */
    private static final EntityDataAccessor<String> BRAIN_SOURCE_ID =
            SynchedEntityData.defineId(MinionEntity.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<Boolean> STAYING =
            SynchedEntityData.defineId(MinionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SADDLED =
            SynchedEntityData.defineId(MinionEntity.class, EntityDataSerializers.BOOLEAN);

    private static final ResourceLocation HEAVY_STRIKER_MODIFIER =
            ResourceLocation.fromNamespaceAndPath(Necromency.MODID, "iron_golem_brain");

    /** Aptitudes actives du brain core (organizer, collector, teleporter, heavy_striker). */
    private final Set<String> brainAttributeIds = new HashSet<>();

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes();
    }

    public MinionEntity(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    /**
     * Hitbox proche du poulet vanilla (le type reste {@code sized(0.6, 1.8)} style zombie / craft).
     * Tant qu’on affiche le mesh poulet, utiliser une boîte basse évite d’enfoncer le modèle dans le sol
     * (rendu + ombre alignés sur la hauteur réelle).
     */
    public static final float CHICKEN_MINION_WIDTH = 0.4F;
    public static final float CHICKEN_MINION_HEIGHT = 0.7F;

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        if (isAllChickenParts()) {
            return EntityDimensions.fixed(CHICKEN_MINION_WIDTH, CHICKEN_MINION_HEIGHT);
        }
        return super.getDefaultDimensions(pose);
    }

    public boolean isAllChickenParts() {
        return CHICKEN.equals(getHeadId())
                && CHICKEN.equals(getBodyId())
                && CHICKEN.equals(getArmLId())
                && CHICKEN.equals(getArmRId())
                && CHICKEN.equals(getLegsId());
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.removeAllGoals(goal -> goal instanceof NearestAttackableTargetGoal);
        this.goalSelector.addGoal(0, new MinionStayWhenOrderedGoal(this));
        this.targetSelector.addGoal(1, new MinionTargetPlayersGoal(this));
        this.targetSelector.addGoal(3, new MinionTargetHostilesGoal(this));
        this.goalSelector.addGoal(2, new MinionOrganizeChestGoal(this));
        this.goalSelector.addGoal(2, new MinionCollectItemsGoal(this));
        this.goalSelector.addGoal(3, new MinionTemptBrainStickGoal(this, 1.1));
        this.goalSelector.addGoal(5, new MinionFollowOwnerGoal(this, 1.0, 4.0F, 14.0F));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);

        // owner = "none"
        builder.define(OWNER_MOST, 0L);
        builder.define(OWNER_LEAST, 0L);

        String def = DEFAULT_TYPE.toString();
        builder.define(HEAD_ID, def);
        builder.define(BODY_ID, def);
        builder.define(ARM_L_ID, def);
        builder.define(ARM_R_ID, def);
        builder.define(LEGS_ID, def);
        builder.define(BRAIN_SOURCE_ID, "");
        builder.define(STAYING, false);
        builder.define(SADDLED, false);
    }

    public boolean isSaddled() {
        return entityData.get(SADDLED);
    }

    public void setSaddled(boolean saddled) {
        entityData.set(SADDLED, saddled);
    }

    public boolean isOwnedBy(Player player) {
        UUID id = getOwnerUUID();
        return id != null && id.equals(player.getUUID());
    }

    @Override
    public void positionRider(Entity passenger, Entity.MoveFunction callback) {
        super.positionRider(passenger, callback);
        if (passenger instanceof Player) {
            callback.accept(passenger,
                    this.getX(),
                    this.getY() + MinionSaddleables.riderHeight(getBodyId()),
                    this.getZ());
        }
    }

    @Override
    public LivingEntity getControllingPassenger() {
        Entity passenger = getFirstPassenger();
        if (passenger instanceof Player player && isSaddled() && isOwnedBy(player)) {
            if (BrainOnAStickItem.isBrainOnAStick(player.getMainHandItem())
                    || BrainOnAStickItem.isBrainOnAStick(player.getOffhandItem())) {
                return player;
            }
        }
        return null;
    }

    @Override
    protected Vec3 getRiddenInput(Player player, Vec3 deltaIn) {
        float forward = player.zza;
        float strafe = player.xxa;
        if (forward <= 0.0F) {
            forward *= 0.5F;
        }
        return new Vec3(strafe * 0.3F, 0.0, forward);
    }

    @Override
    protected float getRiddenSpeed(Player player) {
        return (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.75F;
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        if (hasBrainAttribute(BrainTypes.ATTR_TELEPORTER) && target instanceof LivingEntity living) {
            tryBrainTeleportNear(living);
        }
        boolean hit = super.doHurtTarget(level, target);
        if (hit && target instanceof LivingEntity living) {
            float dmg = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
            MinionPartAttackEffects.applyAllParts(this, living, dmg);
        }
        return hit;
    }

    /** Téléportation style enderman : se place près de la cible avant/frappe. */
    private void tryBrainTeleportNear(LivingEntity target) {
        if (this.random.nextInt(100) >= 45) return;
        for (int attempt = 0; attempt < 8; attempt++) {
            double dx = target.getX() + (this.random.nextDouble() - 0.5) * 12.0;
            double dy = Math.min(target.getY(), this.getY()) + this.random.nextInt(3);
            double dz = target.getZ() + (this.random.nextDouble() - 0.5) * 12.0;
            if (this.randomTeleport(dx, dy, dz, true)) {
                this.level().playSound(null, this.xOld, this.yOld, this.zOld,
                        SoundEvents.ENDERMAN_TELEPORT, this.getSoundSource(), 1.0F, 1.0F);
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.ENDERMAN_TELEPORT, this.getSoundSource(), 1.0F, 1.0F);
                return;
            }
        }
    }

    public boolean isMinionStaying() {
        return entityData.get(STAYING);
    }

    public void setMinionStaying(boolean staying) {
        entityData.set(STAYING, staying);
        if (staying) {
            this.getNavigation().stop();
            this.setTarget(null);
        }
    }

    /** Recalcule vie / dégâts / vitesse selon les 5 morceaux (comme 1.12 {@code calculateAttributes}). */
    public void calculateAttributes() {
        if (level().isClientSide()) return;
        MinionPartAttributes.applyFromParts(this);
    }

    // ------------------------------------------------------------------
    // Owner API
    // ------------------------------------------------------------------

    public void setOwnerUUID(UUID id) {
        if (id == null) {
            entityData.set(OWNER_MOST, 0L);
            entityData.set(OWNER_LEAST, 0L);
        } else {
            entityData.set(OWNER_MOST, id.getMostSignificantBits());
            entityData.set(OWNER_LEAST, id.getLeastSignificantBits());
        }
    }

    public UUID getOwnerUUID() {
        long most = entityData.get(OWNER_MOST);
        long least = entityData.get(OWNER_LEAST);
        if (most == 0L && least == 0L) return null;
        return new UUID(most, least);
    }

    /** ✅ la méthode que tu voulais */
    public Player getOwnerPlayer() {
        UUID id = getOwnerUUID();
        if (id == null) return null;

        // côté client: level.getPlayerByUUID marche aussi en général
        // côté serveur: c’est la source fiable
        if (level() instanceof ServerLevel sl) {
            return sl.getPlayerByUUID(id);
        }
        return level().getPlayerByUUID(id);
    }

    // ------------------------------------------------------------------
    // Parts getters (ce que ton renderer attend)
    // ------------------------------------------------------------------

    public ResourceLocation getHeadId() { return safeRL(entityData.get(HEAD_ID)); }
    public ResourceLocation getBodyId() { return safeRL(entityData.get(BODY_ID)); }
    public ResourceLocation getArmLId() { return safeRL(entityData.get(ARM_L_ID)); }
    public ResourceLocation getArmRId() { return safeRL(entityData.get(ARM_R_ID)); }
    public ResourceLocation getLegsId() { return safeRL(entityData.get(LEGS_ID)); }

    // ------------------------------------------------------------------
    // Parts setters (quand tu craft le mob)
    // ------------------------------------------------------------------

    public void setHeadId(ResourceLocation id) {
        entityData.set(HEAD_ID, safeString(id));
        refreshBodySize();
    }
    public void setBodyId(ResourceLocation id) {
        entityData.set(BODY_ID, safeString(id));
        refreshBodySize();
    }
    public void setArmLId(ResourceLocation id) {
        entityData.set(ARM_L_ID, safeString(id));
        refreshBodySize();
    }
    public void setArmRId(ResourceLocation id) {
        entityData.set(ARM_R_ID, safeString(id));
        refreshBodySize();
    }
    public void setLegsId(ResourceLocation id) {
        entityData.set(LEGS_ID, safeString(id));
        refreshBodySize();
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        // Les setters d’IDs peuvent être appelés avant addFreshEntity (autel) : les dimensions
        // ne sont alors pas toujours recalculées ; on force une fois à l’entrée dans le monde.
        this.refreshDimensions();
    }

    private void refreshBodySize() {
        this.refreshDimensions();
    }

    // ------------------------------------------------------------------
    // Cerveau (autel slot 6 / Brain Maker)
    // ------------------------------------------------------------------

    public String getBrainSourceId() {
        return entityData.get(BRAIN_SOURCE_ID);
    }

    public void setBrainSourceId(String id) {
        entityData.set(BRAIN_SOURCE_ID, id == null ? "" : id);
    }

    /** Applique un brain_core crafté (1 ou 2 cerveaux fusionnés) au minion. */
    public void applyBrainCore(ItemStack brainCore) {
        brainAttributeIds.clear();
        if (brainCore == null || brainCore.isEmpty()) {
            setBrainSourceId("");
            refreshBrainBehavior();
            return;
        }
        var ids = BrainCoreItem.readBrainIds(brainCore);
        setBrainSourceId(String.join(",", ids));
        brainAttributeIds.addAll(BrainCoreItem.readAttributeIds(brainCore));
        if (brainAttributeIds.isEmpty()) {
            inferLegacyBrainAttributes(getBrainSourceId());
        }
        refreshBrainBehavior();
    }

    public boolean hasBrainAttribute(String attributeId) {
        return brainAttributeIds.contains(attributeId);
    }

    /** True si le minion a le cerveau copper golem → ramasse et range dans les coffres. */
    public boolean hasOrganizerBrain() {
        return hasBrainAttribute(BrainTypes.ATTR_ORGANIZER);
    }

    public void refreshBrainBehavior() {
        if (level().isClientSide()) return;
        boolean canOrganize = hasBrainAttribute(BrainTypes.ATTR_ORGANIZER)
                || hasBrainAttribute(BrainTypes.ATTR_COLLECTOR);
        this.setCanPickUpLoot(canOrganize);

        var attack = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) {
            attack.removeModifier(HEAVY_STRIKER_MODIFIER);
            if (hasBrainAttribute(BrainTypes.ATTR_HEAVY_STRIKER)) {
                attack.addTransientModifier(new AttributeModifier(
                        HEAVY_STRIKER_MODIFIER,
                        0.75,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                ));
            }
        }
    }

    private void inferLegacyBrainAttributes(String brainSourceId) {
        if (brainSourceId == null || brainSourceId.isEmpty()) return;
        if (brainSourceId.contains("brain_copper_golem")) brainAttributeIds.add(BrainTypes.ATTR_ORGANIZER);
        if (brainSourceId.contains("brain_allay")) brainAttributeIds.add(BrainTypes.ATTR_COLLECTOR);
        if (brainSourceId.contains("brain_enderman")) brainAttributeIds.add(BrainTypes.ATTR_TELEPORTER);
        if (brainSourceId.contains("brain_iron_golem")) brainAttributeIds.add(BrainTypes.ATTR_HEAVY_STRIKER);
    }

    // ------------------------------------------------------------------
    // Behavior
    // ------------------------------------------------------------------

    @Override
    public boolean hurtServer(ServerLevel level, net.minecraft.world.damagesource.DamageSource source, float amount) {
        net.minecraft.world.entity.Entity attacker = source.getEntity();
        Player owner = getOwnerPlayer();
        boolean hitByOwner = owner != null && attacker == owner;

        boolean damaged = super.hurtServer(level, source, amount);
        if (damaged && hitByOwner) {
            // Le maître peut taper son minion sans déclencher la vengeance zombie.
            if (getTarget() == owner) {
                setTarget(null);
            }
            setLastHurtByMob(null);
        }
        return damaged;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!isOwnedBy(player)) {
            if (getOwnerUUID() != null) {
                player.displayClientMessage(Component.literal("<Minion> I obey only my master."), true);
            }
            return InteractionResult.PASS;
        }

        var held = player.getItemInHand(hand);

        if (held.is(Items.SADDLE) && !isSaddled() && MinionSaddleables.torsoAcceptsSaddle(getBodyId())) {
            setSaddled(true);
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            player.displayClientMessage(Component.literal("Minion saddled."), true);
            return InteractionResult.SUCCESS;
        }

        if (held.isEmpty() && isSaddled() && !player.isPassenger() && getPassengers().isEmpty()) {
            player.startRiding(this);
            return InteractionResult.SUCCESS;
        }

        if (held.isEmpty() && player.isShiftKeyDown()) {
            setMinionStaying(!isMinionStaying());
            player.displayClientMessage(
                    Component.literal(isMinionStaying() ? "Minion is staying put." : "Minion is free to move."),
                    true
            );
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    protected boolean isSunBurnTick() {
        return false; // ✅ ne brûle pas au soleil
    }

    // ------------------------------------------------------------------
    // Save / Load (1.21+)
    // ------------------------------------------------------------------

    @Override
    public void addAdditionalSaveData(ValueOutput out) {
        super.addAdditionalSaveData(out);

        // owner en 2 longs
        out.putLong("OwnerMost", entityData.get(OWNER_MOST));
        out.putLong("OwnerLeast", entityData.get(OWNER_LEAST));

        // parts
        out.putString("HeadId", entityData.get(HEAD_ID));
        out.putString("BodyId", entityData.get(BODY_ID));
        out.putString("ArmLId", entityData.get(ARM_L_ID));
        out.putString("ArmRId", entityData.get(ARM_R_ID));
        out.putString("LegsId", entityData.get(LEGS_ID));
        out.putString("BrainSourceId", entityData.get(BRAIN_SOURCE_ID));
        out.putString("BrainAttributes", String.join(",", brainAttributeIds));
        out.putInt("Staying", entityData.get(STAYING) ? 1 : 0);
        out.putInt("Saddled", entityData.get(SADDLED) ? 1 : 0);
    }

    @Override
    public void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in);

        entityData.set(OWNER_MOST, in.getLong("OwnerMost").orElse(0L));
        entityData.set(OWNER_LEAST, in.getLong("OwnerLeast").orElse(0L));

        entityData.set(HEAD_ID, in.getString("HeadId").orElse(DEFAULT_TYPE.toString()));
        entityData.set(BODY_ID, in.getString("BodyId").orElse(DEFAULT_TYPE.toString()));
        entityData.set(ARM_L_ID, in.getString("ArmLId").orElse(DEFAULT_TYPE.toString()));
        entityData.set(ARM_R_ID, in.getString("ArmRId").orElse(DEFAULT_TYPE.toString()));
        entityData.set(LEGS_ID, in.getString("LegsId").orElse(DEFAULT_TYPE.toString()));
        entityData.set(BRAIN_SOURCE_ID, in.getString("BrainSourceId").orElse(""));
        brainAttributeIds.clear();
        in.getString("BrainAttributes").ifPresent(saved -> {
            if (!saved.isEmpty()) {
                for (String part : saved.split(",")) {
                    if (!part.isBlank()) brainAttributeIds.add(part.trim());
                }
            }
        });
        if (brainAttributeIds.isEmpty()) {
            inferLegacyBrainAttributes(entityData.get(BRAIN_SOURCE_ID));
        }
        entityData.set(STAYING, in.getInt("Staying").orElse(0) != 0);
        entityData.set(SADDLED, in.getInt("Saddled").orElse(0) != 0);
        refreshBrainBehavior();
        refreshBodySize();
        calculateAttributes();
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static String safeString(ResourceLocation id) {
        return (id == null) ? DEFAULT_TYPE.toString() : id.toString();
    }

    private static ResourceLocation safeRL(String s) {
        if (s == null || s.isBlank()) return DEFAULT_TYPE;
        try {
            return ResourceLocation.parse(s);
        } catch (Exception ignored) {
            return DEFAULT_TYPE;
        }
    }
}