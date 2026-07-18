package com.iceKube.soulArmory.soulForging;

import com.iceKube.soulArmory.soulSkill.BaseSoulSkill;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class ForgingCriterion {
    public final String id;
    public final ForgingEventType eventType;
    public final int targetValue;
    public final long timeoutTicks;
    @Nullable
    public final Predicate<EntityType<?>> entityFilter;
    @Nullable
    public final Predicate<DamageSource> damageSourceFilter;
    @Nullable
    public final Predicate<BaseSoulSkill> skillFilter; // Only used when the eventType is SKILL.

    public ForgingCriterion(String id, ForgingEventType eventType, int targetValue,
                            long timeoutTicks, @Nullable Predicate<EntityType<?>> entityFilter,
                            @Nullable Predicate<DamageSource> damageSourceFilter, @Nullable Predicate<BaseSoulSkill> skillFilter) {
        this.id = id;
        this.eventType = eventType;
        this.targetValue = targetValue;
        this.timeoutTicks = timeoutTicks;
        this.entityFilter = entityFilter;
        this.damageSourceFilter = damageSourceFilter;
        this.skillFilter = skillFilter;
    }

    private String progressKey() {
        return id + ".progress";
    }

    private String lastActivityKey() {
        return id + ".lastActivityTime";
    }

    private String completeKey() {
        return id + ".complete";
    }

    public boolean isComplete(CompoundTag taskTag) {
        return taskTag.getBoolean(completeKey());
    }

    public int getProgress(CompoundTag taskTag) {
        return taskTag.getInt(progressKey());
    }

    public boolean hasTimedOut(CompoundTag taskTag, long gameTime) {
        if (timeoutTicks <= 0) return false;
        if (isComplete(taskTag)) return false;
        if (!taskTag.contains(lastActivityKey())) return false;
        return gameTime - taskTag.getLong(lastActivityKey()) > timeoutTicks;
    }

    /**
     * @param taskTag the task's CompoundTag (not the item's root tag)
     * @return true if this call caused the criterion to become complete
     */
    public boolean addProgress(CompoundTag taskTag, float amount, long gameTime) {
        if (isComplete(taskTag)) return false;

        if (timeoutTicks > 0 && taskTag.contains(lastActivityKey())) {
            if (gameTime - taskTag.getLong(lastActivityKey()) > timeoutTicks) {
                taskTag.putFloat(progressKey(), 0);
            }
        }

        taskTag.putLong(lastActivityKey(), gameTime);

        float newProgress = Math.min(targetValue, taskTag.getFloat(progressKey()) + amount);
        taskTag.putFloat(progressKey(), newProgress);

        if (newProgress >= targetValue) {
            taskTag.putBoolean(completeKey(), true);
            return true;
        }
        return false;
    }

    public void resetProgress(CompoundTag taskTag) {
        taskTag.putFloat(progressKey(), 0);
        taskTag.putBoolean(completeKey(), false);
        taskTag.remove(lastActivityKey());
    }
}
