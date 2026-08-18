package com.iceKube.soulArmory.items;

import com.iceKube.soulArmory.Config;
import com.iceKube.soulArmory.entities.SoulArrowEntity;
import com.iceKube.soulArmory.registries.EntityRegistry;
import com.iceKube.soulArmory.soulForging.ForgingTask;
import com.iceKube.soulArmory.soulForging.ForgingTasks;
import com.iceKube.soulArmory.soulSkill.BaseSoulSkill;
import com.iceKube.soulArmory.soulSkill.ContinuousSoulSkill;
import com.iceKube.soulArmory.soulSkill.InstantSoulSkill;
import com.iceKube.soulArmory.soulSkill.SoulSkills;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
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
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class SoulBowItem extends BaseSoulWeaponItem implements UseSoulSkillSystem, CanApplySpeedBoost, Forgeable {

    /**
     * Every skill the bow can ever hold — the one it starts with plus the four unlockable ones.
     * Kept here so "does this bow know everything?" has a single source of truth.
     */
    public static final List<BaseSoulSkill> BOW_SKILLS = List.of(
            SoulSkills.SONIC_BOOM,
            SoulSkills.SCATTER_SHOT,
            SoulSkills.RAPID_FIRE,
            SoulSkills.BARRAGE,
            SoulSkills.SONIC_OVERLOAD);

    public SoulBowItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public List<BaseSoulSkill> getAllPossibleSkills() {
        return BOW_SKILLS;
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
     * How far away the Ender Dragon can still be locked onto. The dragon spends most of the fight
     * far outside {@code soulBowTraceRange}, so it gets its own, much larger range; past this it is
     * dropped from the candidate list like any other out-of-range entity.
     */
    static final double DRAGON_TRACE_RANGE = 200.0;


    static List<Vec3> traceTargets(LivingEntity entity) {
        if (entity instanceof EnderDragon dragon) {
            return Arrays.stream(dragon.getSubEntities())
                    .map(part -> part.getBoundingBox().getCenter())
                    .toList();
        }
        return List.of(
                new Vec3(entity.getX(), entity.getY() + entity.getBbHeight() / 2.0, entity.getZ()),
                entity.getEyePosition(),
                entity.getPosition(1.0f).add(0, 0.05, 0)
        );
    }

    /** The dragon is tracked far past the configured range; everything else uses the config value. */
    static double effectiveRange(LivingEntity entity, double maxDistance) {
        return entity instanceof EnderDragon ? DRAGON_TRACE_RANGE : maxDistance;
    }

    /**
     * Largest cosine of the angle between the look vector and any of the entity's trace targets,
     * ignoring targets that are behind the player or past {@code maxDistance}
     */
    static double bestCosAngle(LivingEntity entity, Vec3 eyePos, Vec3 lookVec, double maxDistance) {
        double best = -1;
        for (Vec3 point : traceTargets(entity)) {
            Vec3 toPoint = point.subtract(eyePos);
            double length = toPoint.length();
            if (length == 0) continue;

            // Depth along the view axis - skip targets behind the player or out of range
            double t = toPoint.dot(lookVec);
            if (t < 0 || t > maxDistance) continue;

            best = Math.max(best, t / length);
        }
        return best;
    }

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

        // The dragon is almost always further away than soulBowTraceRange, so the coarse box above
        // never catches it. Add it separately, capped at its own much larger range.
        if (player.level().dimension() == Level.END) {
            for (EnderDragon dragon : player.level().getEntitiesOfClass(
                    EnderDragon.class, playerBB.inflate(DRAGON_TRACE_RANGE))) {
                if (!candidates.contains(dragon)) {
                    candidates.add(dragon);
                }
            }
        }

        // Return null if there is no enemies.
        if (candidates.isEmpty()) {
            return null;
        }

        candidates.removeIf(entity -> {
            // Make sure it doesn't track Endermen.
            if (entity instanceof EnderMan) return true;

            // Discard entities outside the cone, behind the player, or beyond max distance
            // (all three collapse into a cosAngle of -1)
            return bestCosAngle(entity, eyePos, lookVec, effectiveRange(entity, maxDistance)) < cosHalfAngle;
        });

        // Sort by angle, closest to the crosshair first (negative for descending sort)
        candidates.sort(Comparator.comparingDouble(
                (LivingEntity entity) -> -bestCosAngle(entity, eyePos, lookVec, effectiveRange(entity, maxDistance))));

        // Occlusion check: block raycast from the eye to each of the entity's trace targets
        for (LivingEntity entity : candidates) {
            for (Vec3 point : traceTargets(entity)) {
                // Perform block raycast with COLLIDER shape and empty fluid handling
                ClipContext clipContext = new ClipContext(
                        eyePos,
                        point,
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        player
                );
                BlockHitResult blockHit = player.level().clip(clipContext);

                // If no block obstruction (MISS), return this entity
                if (blockHit.getType() == HitResult.Type.MISS) {
                    return entity;
                }
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

    public void cycleToNextSkill(ItemStack stack, ServerPlayer player) {
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

        setCurrentSkill(stack, nextIndex, player);
    }

    @Override
    public void setCurrentSkill(ItemStack stack, int index, ServerPlayer player) {
        if (!applySkillIndex(stack, index)) return;

        player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.get(), SoundSource.PLAYERS,1,1);
    }

    @Override
    public void setDefaultSkill(ItemStack stack, Level level) {
        CompoundTag tag = stack.getTag();
        tag.putLong(LAST_EXECUTED_TIME, level.getGameTime());
        tag.putString(CURRENT_SKILL, SoulSkills.SONIC_BOOM.soulSkillId.toString());
        tag.putInt(CURRENT_SKILL_INDEX, 0);

        ListTag listTag = tag.getList(AVAILABLE_SKILLS, Tag.TAG_STRING);
        listTag.add(StringTag.valueOf(SoulSkills.SONIC_BOOM.soulSkillId.toString()));

        tag.put(AVAILABLE_SKILLS, listTag);
    }

    @Override
    public @Nullable ForgingTask getActiveForgingTask(ItemStack stack) {
        String string = stack.getOrCreateTag().getString(CURRENT_FORGING_TASK);
        if (string.equals(NO_FORGING_TASK)) return null;
        return ForgingTasks.getTask(ResourceLocation.parse(string));
    }
}
