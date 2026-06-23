package com.mickdev.necromency.registry.item;

import com.mickdev.necromency.registry.init.ModEntities;
import com.mickdev.necromency.registry.init.ModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class TearProjectile extends ThrowableItemProjectile {

    public TearProjectile(EntityType<? extends TearProjectile> type, Level level) {
        super(type, level);
    }

    public TearProjectile(Level level, LivingEntity shooter) {
        super(ModEntities.TEAR.get(), level);
        this.setOwner(shooter);
        this.setPos(
                shooter.getX(),
                shooter.getEyeY() - 0.1,
                shooter.getZ()
        );
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.TEAR_ITEM.get(); // peut être un item dummy, ou ton IsaacsHead si tu veux
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);

        if (!level().isClientSide()) {
            Entity target = hit.getEntity();
            Entity owner = getOwner();

            // 2 coeurs = 4.0F
            target.hurt(level().damageSources().thrown(this, owner), 4.0F);

            discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        if (!level().isClientSide()) discard();
    }
}
