package com.iceKube.soulArmory.soulSkill.skills;

import com.iceKube.soulArmory.Config;
import com.iceKube.soulArmory.SoulArmoryMod;
import com.iceKube.soulArmory.items.SoulBowItem;
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

public class SonicOverloadSkill extends InstantSoulSkill {
    public SonicOverloadSkill() {
        super(new ResourceLocation(SoulArmoryMod.MODID, "sonic_overload"),
                new ResourceLocation("textures/item/echo_shard.png"),
                "sonic_overload",
                Config.soulBowSkillSOConsumption);
    }

    @Override
    public boolean execute(ItemStack stack, Level level, Player player) {
        if (!(stack.getItem() instanceof SoulBowItem)) return false;
        if (stack.getTag() == null) return false;
        if (!stack.getTag().contains(soulAmountNBT)) return false;
        if (stack.getTag().getFloat(soulAmountNBT) < soulCost) return false;

        int blastCount = Config.soulBowSkillSOBlastCount;

        // Get player's look direction
        Vec3 viewVector = player.getViewVector(1.0F);
        Vec3 upVector = player.getUpVector(1.0F);

        // Make sure it is always RIGHT.
        Vec3 rightVector = viewVector.cross(upVector).normalize();

        Set<LivingEntity> hitEntities = new HashSet<>();

        for (int a = 0; a < blastCount; a++) {
            // Sonic Boom: trace 2-block AABBs along player's view vector and damage all entities hit

            // Calculate spread angles
            // Distribute arrows evenly
            double offsetFraction = (blastCount == 1) ? 0 : (double) a / (blastCount - 1) - 0.5; // -0.5 to 0.5
            double horizontalOffset = offsetFraction * Config.soulBowSkillSOSpreadAngle * 2; // horizontal spread in degrees

            double angleRad = Math.toRadians(horizontalOffset);

            Vec3 shootDir = viewVector.scale(Math.cos(angleRad))
                    .add(rightVector.scale(Math.sin(angleRad))).normalize();

            Vec3 currentCenter = player.getEyePosition().add(shootDir.scale(2)); // 2 blocks ahead of player's eye

            for (int i = 0; i < (int) Math.ceil(Config.soulBowSkillSORange / 2.0); i++) {
                AABB searchBox = new AABB(currentCenter, currentCenter).inflate(1); // 2-block AABB centered on currentCenter
                List<LivingEntity> entities = level.getEntitiesOfClass(
                        LivingEntity.class, searchBox,
                        livingEntity -> livingEntity instanceof Enemy
                );
                hitEntities.addAll(entities);
                currentCenter = currentCenter.add(shootDir.scale(2)); // Move 2 blocks ahead along shoot vector
            }

            // Add particle effect.
            if (level instanceof ServerLevel serverLevel) {
                Vec3 origin = player.getEyePosition();

                for (int i = 1; i < Config.soulBowSkillSORange; ++i) {
                    Vec3 particlePos = origin.add(shootDir.scale(i));
                    serverLevel.sendParticles(
                            ParticleTypes.SONIC_BOOM,
                            particlePos.x, particlePos.y, particlePos.z,
                            1, 0.0D, 0.0D, 0.0D, 0.0D
                    );
                }
            }
        }

        // Apply damage to all collected entities
        for (LivingEntity entity : hitEntities) {
            entity.hurt(player.damageSources().sonicBoom(player), (float) Config.soulBowSkillSODamage);
        }

        level.playSound(
                null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS,
                3.0F,
                1.0F);

        return super.execute(stack, level, player);
    }
}
