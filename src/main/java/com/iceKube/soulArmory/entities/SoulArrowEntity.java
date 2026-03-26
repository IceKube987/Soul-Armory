package com.iceKube.soulArmory.entities;

import com.iceKube.soulArmory.Config;
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

import java.util.Comparator;
import java.util.List;

public class SoulArrowEntity extends AbstractArrow {

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

    // -------------------------------------------------------------------------
    // Homing behaviour
    // -------------------------------------------------------------------------

    @Override
    public void tick() {
        // Apply homing steering on the server side before the standard tick so
        // the modified velocity is used for movement this same tick.
        if (!inGround && !level().isClientSide) {
            LivingEntity target = findNearestHostile(Config.soulArrowHomingRange);
            if (target != null) {
                Vec3 currentVelocity = getDeltaMovement();
                double currentSpeed = currentVelocity.length();
                if (currentSpeed > 1e-6) {
                    Vec3 currentDir = currentVelocity.normalize();
                    // Direction vector pointing from the arrow to the target's centre
                    Vec3 toTarget = target.position()
                            .add(0, target.getBbHeight() * 0.5, 0)
                            .subtract(position())
                            .normalize();
                    // Smoothly interpolate the arrow's direction toward the target.
                    double turnFactor = Config.soulArrowTurnFactor;
                    Vec3 newDir = new Vec3(
                            currentDir.x + (toTarget.x - currentDir.x) * turnFactor,
                            currentDir.y + (toTarget.y - currentDir.y) * turnFactor,
                            currentDir.z + (toTarget.z - currentDir.z) * turnFactor
                    ).normalize();
                    setDeltaMovement(newDir.scale(currentSpeed));
                }
            }
        }
        super.tick();
    }

    /**
     * Returns the nearest alive {@link Monster} within {@code range} blocks,
     * excluding the entity that fired this arrow.
     */
    private LivingEntity findNearestHostile(double range) {
        AABB searchBox = getBoundingBox().inflate(range);
        List<Monster> hostiles = level().getEntitiesOfClass(
                Monster.class, searchBox,
                e -> e.isAlive() && e != getOwner()
        );
        if (hostiles.isEmpty()) return null;
        Vec3 pos = position();
        return hostiles.stream()
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(pos.x, pos.y, pos.z)))
                .orElse(null);
    }

    // -------------------------------------------------------------------------
    // Constant damage — ignores velocity; always deals Config.soulBowBaseDamage
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

        target.hurt(damageSource, (float) Config.soulBowBaseDamage);
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
