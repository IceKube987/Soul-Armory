package com.iceKube.soulArmory.utils;

import com.iceKube.soulArmory.SoulArmoryMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class ModDamageTypes {
    public static final ResourceKey<DamageType> SKILL_ARROW = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            new ResourceLocation(SoulArmoryMod.MODID, "skill_arrow")
    );

    public static final ResourceKey<DamageType> SKILL_DAMAGE = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            new ResourceLocation(SoulArmoryMod.MODID, "skill_damage")
    );

    public static DamageSource skillArrow(Level level, Entity directEntity, @Nullable Entity attacker) {
        Holder<DamageType> damageType = level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(SKILL_ARROW);

        return new DamageSource(damageType, directEntity, attacker);
    }

    public static DamageSource skillDamage(Level level, @Nullable Entity attacker) {
        Holder<DamageType> damageType = level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(SKILL_DAMAGE);

        return new DamageSource(damageType, attacker);
    }
}