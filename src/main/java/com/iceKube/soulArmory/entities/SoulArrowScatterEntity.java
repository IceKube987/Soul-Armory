package com.iceKube.soulArmory.entities;

import com.iceKube.soulArmory.utils.ModDamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

// Soul Arrow that has no homing behavior and grants no soul points when hit enemy
public class SoulArrowScatterEntity extends AbstractArrow {
    public SoulArrowScatterEntity(EntityType<? extends AbstractArrow> pEntityType, LivingEntity pOwner, Level pLevel) {
        super(pEntityType, pOwner, pLevel);
    }

    public SoulArrowScatterEntity(EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    private int inGroundTicks = 0;
    private int flyingTicks = 0;

    @Override
    public boolean isNoGravity() {
        return true;
    }

    // No homing behaviour
    @Override
    public void tick() {
        // Apply homing steering on the server side before the standard tick so
        // the modified velocity is used for movement this same tick.
        if (!inGround) {
            flyingTicks++;
            // discard if the arrow had been flying for more than 10 seconds.
            if (flyingTicks >= 200) {
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
                ? ModDamageTypes.skillArrow(level(), this, owner)
                : ModDamageTypes.skillArrow(level(), this, this);

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
