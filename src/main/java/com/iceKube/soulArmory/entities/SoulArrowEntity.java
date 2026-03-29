package com.iceKube.soulArmory.entities;

import com.iceKube.soulArmory.Config;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

public class SoulArrowEntity extends AbstractArrow {

    private int inGroundTicks = 0;
    private int flyingTicks = 0;

    // Sync the ID for the target to track.
    private static final EntityDataAccessor<Integer> TARGET_ID =
            SynchedEntityData.defineId(SoulArrowEntity.class, EntityDataSerializers.INT);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TARGET_ID, -1);
    }

    public void setTarget(LivingEntity entity) {
        this.entityData.set(TARGET_ID, entity != null ? entity.getId() : -1);
    }

    @Nullable
    public LivingEntity getTarget() {
        int id = this.entityData.get(TARGET_ID);
        if (id == -1) return null;
        Entity e = this.level().getEntity(id);
        return e instanceof LivingEntity le ? le : null;
    }

    /**
     * Required public constructor for EntityType factory (deserialization).
     */
    public SoulArrowEntity(EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        pickup = AbstractArrow.Pickup.DISALLOWED;
    }

    /**
     * Constructor used when firing from SoulBowItem — sets the shooter as owner.
     */
    public SoulArrowEntity(EntityType<? extends AbstractArrow> pEntityType, LivingEntity pOwner, Level pLevel) {
        super(pEntityType, pOwner, pLevel);
        pickup = AbstractArrow.Pickup.DISALLOWED;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    // -------------------------------------------------------------------------
    // Homing behaviour
    // -------------------------------------------------------------------------

    @Override
    public void tick() {
        // Apply homing steering on the server side before the standard tick so
        // the modified velocity is used for movement this same tick.
        if (!inGround) {
            inGroundTicks = 0;
            LivingEntity target = getTarget();
            if (target != null) {
                Vec3 currentVelocity = getDeltaMovement();
                double currentSpeed = currentVelocity.length();
                if (currentSpeed > 1e-5) {
                    Vec3 currentDir = currentVelocity.normalize();
                    // Direction vector pointing from the arrow to the target's centre
                    Vec3 toTarget = target.position()
                            .add(0, target.getBbHeight() * 0.5, 0)
                            .subtract(position())
                            .normalize();
                    // Smoothly interpolate the arrow's direction toward the target.
                    double turnFactor = Config.soulArrowTurnFactor * Math.min(1, flyingTicks/2);
                    Vec3 newDir = new Vec3(
                            currentDir.x + (toTarget.x - currentDir.x) * turnFactor,
                            currentDir.y + (toTarget.y - currentDir.y) * turnFactor,
                            currentDir.z + (toTarget.z - currentDir.z) * turnFactor
                    ).normalize();
                    setDeltaMovement(newDir.scale(currentSpeed));
                }
            }
            flyingTicks++;
            // discard if the arrow had been flying for more than 10 seconds.
            if (flyingTicks >= 200){
                this.discard();
            }
        } else {
            inGroundTicks++;
            if (inGroundTicks >= 20) {
                this.discard();
            }
        }
        super.tick();
    }

    // -------------------------------------------------------------------------
    // Constant damage — ignores velocity; always deals a certain amount of damage.
    // -------------------------------------------------------------------------

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        Entity target = pResult.getEntity();
        Entity owner = getOwner();

        DamageSource damageSource = owner != null
                ? damageSources().arrow(this, owner)
                : damageSources().arrow(this, this);

        if (owner instanceof LivingEntity livingOwner) {
            livingOwner.setLastHurtMob(target);
        }

        target.invulnerableTime = 0;
        target.hurt(damageSource, (float) getBaseDamage());
        discard();
    }

    // -------------------------------------------------------------------------
    // Non-pickupable
    // -------------------------------------------------------------------------

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isPickable() {
        return false;
    }
}
