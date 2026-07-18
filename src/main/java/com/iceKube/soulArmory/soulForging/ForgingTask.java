package com.iceKube.soulArmory.soulForging;

import com.iceKube.soulArmory.soulSkill.BaseSoulSkill;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class ForgingTask {
    private static final String FORGING_NBT_PREFIX = "soul_armory.forging.";
    public final ResourceLocation taskId;
    public final List<ForgingCriterion> criteria;
    public final CompletionAction onComplete;
    /**
     * The skill this task unlocks, or {@code null} for tasks that forge an incomplete weapon into a full one.
     */
    @Nullable
    public final BaseSoulSkill unlockedSkill;
    public final boolean resetAllOnTimeout;

    public ForgingTask(ResourceLocation taskId, List<ForgingCriterion> criteria,
                       CompletionAction onComplete, @Nullable BaseSoulSkill unlockedSkill,
                       boolean resetAllOnTimeout) {
        this.taskId = taskId;
        this.criteria = List.copyOf(criteria);
        this.onComplete = onComplete;
        this.unlockedSkill = unlockedSkill;
        this.resetAllOnTimeout = resetAllOnTimeout;
    }

    public String getTaskNbtKey() {
        return FORGING_NBT_PREFIX + taskId.getPath();
    }

    public CompoundTag getOrCreateTaskTag(CompoundTag itemTag) {
        String key = getTaskNbtKey();
        if (!itemTag.contains(key, CompoundTag.TAG_COMPOUND)) {
            itemTag.put(key, new CompoundTag());
        }
        return itemTag.getCompound(key);
    }

    public void removeTaskTag(CompoundTag itemTag) {
        itemTag.remove(getTaskNbtKey());
    }

    public boolean processEvent(CompoundTag itemTag, ForgingEventType eventType, float amount, long gameTime) {
        return processEvent(itemTag, eventType, null, null, null, amount, gameTime);
    }

    public boolean processEvent(CompoundTag itemTag, ForgingEventType eventType,
                                EntityType<?> entityType, float amount, long gameTime) {
        return processEvent(itemTag, eventType, entityType, null, null, amount, gameTime);
    }

    public boolean processEvent(CompoundTag itemTag, ForgingEventType eventType,
                                EntityType<?> entityType, DamageSource damageSource, float amount, long gameTime) {
        return processEvent(itemTag, eventType, entityType, damageSource, null, amount, gameTime);
    }

    public boolean processEvent(CompoundTag itemTag, ForgingEventType eventType,
                                EntityType<?> entityType, DamageSource damageSource, BaseSoulSkill skill, float amount, long gameTime) {
        CompoundTag taskTag = getOrCreateTaskTag(itemTag);

        if (resetAllOnTimeout) {
            for (ForgingCriterion criterion : criteria) {
                if (!criterion.isComplete(taskTag) && criterion.hasTimedOut(taskTag, gameTime)) {
                    resetAll(taskTag);
                    break;
                }
            }
        }

        for (ForgingCriterion criterion : criteria) {
            if (criterion.eventType != eventType) continue;
            if (criterion.isComplete(taskTag)) continue;
            if (criterion.entityFilter != null && !criterion.entityFilter.test(entityType)) continue;
            if (criterion.damageSourceFilter != null && !criterion.damageSourceFilter.test(damageSource)) continue;
            if (criterion.skillFilter != null && !criterion.skillFilter.test(skill)) continue;
            criterion.addProgress(taskTag, amount, gameTime);
        }

        return isComplete(taskTag);
    }

    public boolean isComplete(CompoundTag taskTag) {
        for (ForgingCriterion criterion : criteria) {
            if (!criterion.isComplete(taskTag)) return false;
        }
        return true;
    }

    public void checkTimeouts(CompoundTag itemTag, long gameTime) {
        CompoundTag taskTag = getOrCreateTaskTag(itemTag);

        if (resetAllOnTimeout) {
            for (ForgingCriterion criterion : criteria) {
                if (!criterion.isComplete(taskTag) && criterion.hasTimedOut(taskTag, gameTime)) {
                    resetAll(taskTag);
                    return;
                }
            }
        } else {
            for (ForgingCriterion criterion : criteria) {
                if (!criterion.isComplete(taskTag) && criterion.hasTimedOut(taskTag, gameTime)) {
                    criterion.resetProgress(taskTag);
                }
            }
        }
    }

    public void resetAll(CompoundTag taskTag) {
        for (ForgingCriterion criterion : criteria) {
            criterion.resetProgress(taskTag);
        }
    }

    @FunctionalInterface
    public interface CompletionAction {
        void execute(Player player, ItemStack stack, Level level);
    }
}
