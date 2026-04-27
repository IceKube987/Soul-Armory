package com.iceKube.soulArmory.soulSkill.skills;

import com.iceKube.soulArmory.Config;
import com.iceKube.soulArmory.SoulArmoryMod;
import com.iceKube.soulArmory.soulSkill.InstantSoulSkill;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.iceKube.soulArmory.items.BaseSoulWeaponItem.soulAmountNBT;

public class SonicBoomSkill extends InstantSoulSkill {

    public SonicBoomSkill() {
        super(new ResourceLocation(SoulArmoryMod.MODID, "sonic_boom"),
                null,
                "sonic_boom",
                100
        );
    }

    public SonicBoomSkill(ResourceLocation soulSkillId, ResourceLocation soulSkillTexture, String soulSkillName, int soulCost) {
        super(soulSkillId, soulSkillTexture, soulSkillName, soulCost);
    }

    @Override
    public boolean execute(ItemStack stack, Level level, Player player) {
        if (stack.getTag() == null) return false;
        if (!stack.getTag().contains(soulAmountNBT)) return false;
        if (stack.getTag().getFloat(soulAmountNBT) < Config.soulBowSkillSBConsumption) return false;

        // Sonic Boom: trace 15 2-block AABBs along player's view vector and damage all entities hit
        Set<LivingEntity> hitEntities = new HashSet<>();
        Vec3 lookVec = player.getViewVector(1.0f).normalize();
        Vec3 currentCenter = player.getEyePosition().add(lookVec.scale(2)); // 2 blocks ahead of player's eye

        for (int i = 0; i < (int) Math.ceil(Config.soulBowSkillSBRange / 2.0); i++) {
            AABB searchBox = new AABB(currentCenter, currentCenter).inflate(1); // 2-block AABB centered on currentCenter
            List<LivingEntity> entities = level.getEntitiesOfClass(
                    LivingEntity.class, searchBox,
                    livingEntity -> livingEntity instanceof Enemy
            );
            hitEntities.addAll(entities);
            currentCenter = currentCenter.add(lookVec.scale(2)); // Move 2 blocks ahead along view vector
        }

        // Apply damage to all collected entities
        for (LivingEntity entity : hitEntities) {
            entity.hurt(player.damageSources().sonicBoom(player), (float) Config.soulBowSkillSBDamage);
        }

//        player.playSound(SoundEvents.WARDEN_SONIC_BOOM, 3.0F, 1.0F);
        level.playSound(
                null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS,
                3.0F,
                1.0F);

        // Add particle effect.
        if (level instanceof ServerLevel serverLevel) {
            Vec3 origin = player.getEyePosition();
            Vec3 direction = player.getViewVector(1.0f).normalize();

            for (int i = 1; i < Config.soulBowSkillSBRange; ++i) {
                Vec3 particlePos = origin.add(direction.scale(i));
                serverLevel.sendParticles(
                        ParticleTypes.SONIC_BOOM,
                        particlePos.x, particlePos.y, particlePos.z,
                        1, 0.0D, 0.0D, 0.0D, 0.0D
                );
            }
        }

        stack.getTag().putFloat(soulAmountNBT, stack.getTag().getFloat(soulAmountNBT) - Config.soulBowSkillSBConsumption);

        return true;
    }
}
