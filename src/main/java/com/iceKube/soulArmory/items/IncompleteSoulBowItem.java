package com.iceKube.soulArmory.items;

import com.iceKube.soulArmory.Config;
import com.iceKube.soulArmory.entities.SoulArrowEntity;
import com.iceKube.soulArmory.registries.EntityRegistry;
import com.iceKube.soulArmory.soulForging.ForgingTask;
import com.iceKube.soulArmory.soulForging.ForgingTasks;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IncompleteSoulBowItem extends BaseIncompleteSoulItem {

    public IncompleteSoulBowItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ForgingTask getActiveForgingTask(ItemStack stack) {
        return ForgingTasks.FORGE_SOUL_BOW;
    }

    @Override
    public int getMaxActiveTimeTicks() {
        return Config.forgingBowActiveTicks;
    }

    /// --- Copy-pasted from SoulBowItem ---

    // -------------------------------------------------------------------------
    // Bow usage, copied and modified from BowItem
    // -------------------------------------------------------------------------

    /**
     * How long (in ticks) the item can be "charged" before auto-firing.
     */
    public int getUseDuration(ItemStack pStack) {
        return 72000;
    }

    /**
     * Called when the player right-clicks — begin drawing the bow.
     * No physical arrow is consumed;
     */
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        pPlayer.startUsingItem(pHand);
        return InteractionResultHolder.consume(pPlayer.getItemInHand(pHand));
    }

    /**
     * Gets the velocity of the arrow entity from the bow's charge
     */
    public float getPowerForTime(int pCharge) {
        float f = (float) pCharge / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    }

    /**
     * Called when the player releases right-click.
     */
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving, int pTimeLeft) {
        if (!(pEntityLiving instanceof Player player)) return;

        double soulMultiplier = 1;

        if (isActive(pStack)) {
            soulMultiplier += 0.01 * (int) (Config.soulBowMaxSoul / Config.soulBowPointPerDamagePercent);
        }

        int chargedTicks = getUseDuration(pStack) - pTimeLeft;
        chargedTicks /= 2; // Base charge speed for soul bow is half of vanilla bow.
        chargedTicks = (int) (chargedTicks * soulMultiplier); // Apply drawing speed multiplier
        float power = getPowerForTime(chargedTicks);
        if (power < 0.9F) return; // Won't shoot unless charged to max.

        if (!pLevel.isClientSide) {

            // Try to use skill
            if (player.isShiftKeyDown()) {
                if (isActive(pStack)) {
                    execute(pStack, pLevel, player);
                    return;
                }
            }

            double damage = Config.soulBowBaseDamage * soulMultiplier; // Apply arrow damage multiplier

            SoulArrowEntity arrow = new SoulArrowEntity(
                    EntityRegistry.SOUL_ARROW.get(), player, pLevel);
            // Shoot in the direction the player is looking; power * 3.0 is
            // the max speed as a fully-charged vanilla bow.
            arrow.shootFromRotation(
                    player, player.getXRot(), player.getYRot(),
                    0.0F, power * 3.0f, 0.3f);
            arrow.setBaseDamage(damage);
            arrow.setTarget(getNearestEntityLookedAt(player, Config.soulBowTraceRange, Config.soulBowTraceAngle));
            pLevel.addFreshEntity(arrow);
        }

        pLevel.playSound(
                null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS,
                1.0F,
                1.0F / (pLevel.getRandom().nextFloat() * 0.4F + 1.2F) + power * 0.5F);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.BOW;
    }

    // --------------------
    // Unique methods
    // --------------------

    /**
     * Returns the LivingEntity closest to the player's crosshair within a cone-shaped region.
     * Uses a multi-stage filter: AABB coarse filter, cone filter, angle sort, then occlusion check.
     *
     * @param player       The player whose perspective is used.
     * @param maxDistance  The maximum distance along the look vector to consider.
     * @param halfAngleDeg Half of the cone angle in degrees (full cone = 2 * halfAngleDeg).
     * @return The closest unobstructed Enemy within the cone, or null if none found.
     */
    public LivingEntity getNearestEntityLookedAt(Player player, double maxDistance, double halfAngleDeg) {
        // Get normalized look vector and eye position
        Vec3 lookVec = player.getViewVector(1.0f).normalize();
        Vec3 eyePos = player.getEyePosition();

        // Precompute cosine of half-angle for cone filter
        double cosHalfAngle = Math.cos(Math.toRadians(halfAngleDeg));

        // Build AABB for coarse entity filtering
        // Expand player's bounding box along the look vector by maxDistance, and by maxDistance / 3 on all sides
        AABB playerBB = player.getBoundingBox();
        AABB searchBB = playerBB.expandTowards(lookVec.scale(maxDistance)).inflate(maxDistance / 3);

        // Query all enemies within the AABB (excluding the player)
        // Will get entities behind the player, but will be sorted out by angle later.
        List<LivingEntity> candidates = player.level().getEntitiesOfClass(
                LivingEntity.class, searchBB,
                e -> e instanceof Enemy
        );

        // Return null if there is no enemies.
        if (candidates.isEmpty()) {
            return null;
        }

        candidates.removeIf(entity -> {
            // Make sure it doesn't track Endermen.
            if (entity instanceof EnderMan) return true;

            // Get entity center at mid-height
            Vec3 entityCenter = new Vec3(entity.getX(), entity.getY() + entity.getBbHeight() / 2.0, entity.getZ());
            Vec3 toEntity = entityCenter.subtract(eyePos);

            // Project onto look vector to get depth along view axis
            double t = toEntity.dot(lookVec);

            // Discard entities if it is somewhat at the same position as the player.
            double toEntityLength = toEntity.length();
            if (toEntityLength == 0) {
                return true;
            }

            // Discard entities behind the player or beyond max distance
            if (t < 0 || t > maxDistance) {
                return true;
            }

            // Compute cosAngle - closer to 1 means closer to crosshair
            double cosAngle = t / toEntityLength;

            // Discard entities outside the cone
            if (cosAngle < cosHalfAngle) {
                return true;
            }

            return false;
        });

        // Filter candidates by cone constraints and sort by angle
        candidates.sort(Comparator.comparingDouble((LivingEntity entity) -> {
            // Get entity center at mid-height
            Vec3 entityCenter = new Vec3(entity.getX(), entity.getY() + entity.getBbHeight() / 2.0, entity.getZ());
            Vec3 toEntity = entityCenter.subtract(eyePos);

            // Project onto look vector to get depth along view axis
            double t = toEntity.dot(lookVec);

            double toEntityLength = toEntity.length();

            // Compute cosAngle - closer to 1 means closer to crosshair
            double cosAngle = t / toEntityLength;

            return -cosAngle; // Negative for descending sort (closest to crosshair first)
        }));

        // Occlusion check: perform block raycast from eye to entity center
        for (LivingEntity entity : candidates) {
            Vec3 entityCenter = new Vec3(entity.getX(), entity.getY() + entity.getBbHeight() / 2.0, entity.getZ());
            Vec3 entityEye = entity.getEyePosition();
            Vec3 entityFeet = entity.getPosition(1.0f).add(new Vec3(0, 0.05, 0));

            // Perform block raycast with COLLIDER shape and empty fluid handling
            ClipContext centerClipContext = new ClipContext(
                    eyePos,
                    entityCenter,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    player
            );
            BlockHitResult centerBlockHit = player.level().clip(centerClipContext);

            // If no block obstruction (MISS), return this entity
            if (centerBlockHit.getType() == HitResult.Type.MISS) {
                return entity;
            }

            // Eye check
            ClipContext eyeClipContext = new ClipContext(
                    eyePos,
                    entityEye,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    player
            );
            BlockHitResult eyeBlockHit = player.level().clip(eyeClipContext);

            // If no block obstruction (MISS), return this entity
            if (eyeBlockHit.getType() == HitResult.Type.MISS) {
                return entity;
            }

            // Feet check
            ClipContext feetClipContext = new ClipContext(
                    eyePos,
                    entityFeet,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    player
            );
            BlockHitResult feetBlockHit = player.level().clip(feetClipContext);

            // If no block obstruction (MISS), return this entity
            if (feetBlockHit.getType() == HitResult.Type.MISS) {
                return entity;
            }
        }

        return null;
    }

    // Copied and modified from Sonic Boom Skill
    public void execute(ItemStack stack, Level level, Player player) {

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
        stack.getTag().putInt(ACTIVE_TIME_NBT, 0);
        onDeactivate(stack, level, player);
    }
}
