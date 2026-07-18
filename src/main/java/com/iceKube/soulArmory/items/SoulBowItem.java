package com.iceKube.soulArmory.items;

import com.iceKube.soulArmory.Config;
import com.iceKube.soulArmory.entities.SoulArrowEntity;
import com.iceKube.soulArmory.registries.EntityRegistry;
import com.iceKube.soulArmory.soulSkill.BaseSoulSkill;
import com.iceKube.soulArmory.soulSkill.ContinuousSoulSkill;
import com.iceKube.soulArmory.soulSkill.InstantSoulSkill;
import com.iceKube.soulArmory.soulSkill.SoulSkills;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
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
import java.util.List;

public class SoulBowItem extends BaseSoulWeaponItem implements UseSoulSkillSystem, CanApplySpeedBoost {

    public SoulBowItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public int getGracePeriodTicks() {
        return Config.soulBowGracePeriod;
    }

    @Override
    public int getSoulDecaySpeed() {
        return Config.soulBowSoulDecaySpeed;
    }

    @Override
    public int getMaxSoul() {
        return Config.soulBowMaxSoul;
    }

    @Override
    public int getPointPerSpeedPercent() {
        return Config.soulBowPointPerSpeedPercent;
    }

    @Override
    public int getOverflowSpeed() {
        return Config.soulBowOverflowSpeed;
    }

    @Override
    public int getOverflowThreshold() {
        return Config.soulBowOverflowThreshold;
    }

    @Override
    public double getSpeedAdditionPercentage(ItemStack stack) {
        if (stack.getTag() == null) return 0;
        return 0.01 * (int) (stack.getTag().getFloat(SOUL_AMOUNT) / getPointPerSpeedPercent());
    }

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

    @Override
    public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack pStack, int pRemainingUseDuration) {
        super.onUseTick(pLevel, pLivingEntity, pStack, pRemainingUseDuration);

        if (pLivingEntity instanceof Player player) {
            // For every Config.soulBowPointPerDamagePercent points of soul, add 1% of drawing speed and arrow damage.
            double soulMultiplier = (1 + 0.01 * (int) (pStack.getTag().getFloat(SOUL_AMOUNT) / Config.soulBowPointPerDamagePercent));

            int chargedTicks = getUseDuration(pStack) - pRemainingUseDuration;
            chargedTicks /= 2; // Base charge speed for soul bow is half of vanilla bow.
            chargedTicks = (int) (chargedTicks * soulMultiplier); // Apply drawing speed multiplier
            float power = getPowerForTime(chargedTicks);
            if (power < 0.9F) return; // Won't use skill unless charged to max.

            if (player.isShiftKeyDown()) {
                useContinuousSkill(pStack, pLevel, player);
            }
        }
    }

    /**
     * Called when the player releases right-click.
     */
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving, int pTimeLeft) {
        if (!(pEntityLiving instanceof Player player)) return;

        // For every Config.soulBowPointPerDamagePercent points of soul, add 1% of drawing speed and arrow damage.
        double soulMultiplier = (1 + 0.01 * (int) (pStack.getTag().getFloat(SOUL_AMOUNT) / Config.soulBowPointPerDamagePercent));

        int chargedTicks = getUseDuration(pStack) - pTimeLeft;
        chargedTicks /= 2; // Base charge speed for soul bow is half of vanilla bow.
        chargedTicks = (int) (chargedTicks * soulMultiplier); // Apply drawing speed multiplier
        float power = getPowerForTime(chargedTicks);
        if (power < 0.9F) return; // Won't shoot unless charged to max.

        if (!pLevel.isClientSide) {

            // Try to use skill
            if (player.isShiftKeyDown()) {
                if (getCurrentSkill(pStack) instanceof ContinuousSoulSkill)
                    return; // Do not shoot arrow if player is using a continuous skill
                if (useInstantSkill(pStack, pLevel, player)) return;
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

    /**
     * Handle instant skill usage
     *
     * @return Whether the skill is successfully executed
     */
    private boolean useInstantSkill(ItemStack stack, Level level, Player player) {
//        return sonicBoomSkill(stack, level, player);
        if (!(getCurrentSkill(stack) instanceof InstantSoulSkill skill)) return false;

        return skill.execute(stack, level, player);
    }

    /**
     * Handle continuous skill usage
     */
    private void useContinuousSkill(ItemStack stack, Level level, Player player) {
        if (!(getCurrentSkill(stack) instanceof ContinuousSoulSkill skill)) return;
        if (stack.getTag() == null || !stack.getTag().contains(LAST_EXECUTED_TIME)) return;

        if (skill.execute(stack, level, player)) {
            stack.getTag().putLong(LAST_EXECUTED_TIME, level.getGameTime());
        }
    }

    public void cycleToNextSkill(ItemStack stack) {
        if (!stack.hasTag()) return;
        CompoundTag tag = stack.getTag();

        List<BaseSoulSkill> skills = getAvailableSkills(stack);
        if (skills == null) return;
        int size = skills.size();

        // Check if there is only one skill
        if (size <= 1) return;

        int currentIndex = tag.getInt(CURRENT_SKILL_INDEX);
        // 0 -> 1 -> 2 -> 0 loop if there are 3 skills
        int nextIndex = (currentIndex + 1) % size;
        tag.putInt(CURRENT_SKILL_INDEX, nextIndex);

        // actually switch to next skill
        String nextSkillId = skills.get(nextIndex).soulSkillId.toString();
        tag.putString(CURRENT_SKILL, nextSkillId);

        Minecraft.getInstance().player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.get(), SoundSource.PLAYERS, 1, 1);
    }

    @Override
    public void setDefaultSkill(ItemStack stack, Level level) {
        CompoundTag tag = stack.getTag();
        tag.putLong(LAST_EXECUTED_TIME, level.getGameTime());
        tag.putString(CURRENT_SKILL, SoulSkills.SONIC_BOOM.soulSkillId.toString());
        tag.putInt(CURRENT_SKILL_INDEX, 0);

        ListTag listTag = tag.getList(AVAILABLE_SKILLS, Tag.TAG_STRING);
        listTag.add(StringTag.valueOf(SoulSkills.SONIC_BOOM.soulSkillId.toString()));

        // TODO: This is only for test and marked for removal when soul forging system is completed
        listTag.add(StringTag.valueOf(SoulSkills.SCATTER_SHOT.soulSkillId.toString()));
        listTag.add(StringTag.valueOf(SoulSkills.RAPID_FIRE.soulSkillId.toString()));
        listTag.add(StringTag.valueOf(SoulSkills.BARRAGE.soulSkillId.toString()));
        listTag.add(StringTag.valueOf(SoulSkills.SONIC_OVERLOAD.soulSkillId.toString()));

        tag.put(AVAILABLE_SKILLS, listTag);
    }
}
