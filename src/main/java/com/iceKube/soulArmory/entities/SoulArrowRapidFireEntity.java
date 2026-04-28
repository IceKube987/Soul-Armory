package com.iceKube.soulArmory.entities;

import com.iceKube.soulArmory.utils.ModDamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class SoulArrowRapidFireEntity extends SoulArrowEntity{
    public SoulArrowRapidFireEntity(EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public SoulArrowRapidFireEntity(EntityType<? extends AbstractArrow> pEntityType, LivingEntity pOwner, Level pLevel) {
        super(pEntityType, pOwner, pLevel);
    }

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
}
