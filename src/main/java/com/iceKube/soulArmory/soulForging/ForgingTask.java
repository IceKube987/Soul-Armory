package com.iceKube.soulArmory.soulForging;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class ForgingTask {
    public final ResourceLocation taskId;
    public final List<ForgingCriterion> criteria;
    public final CompletionAction onComplete;
    public final boolean resetAllOnTimeout;

    private static final String FORGING_NBT_PREFIX = "soul_armory.forging.";

    @FunctionalInterface
    public interface CompletionAction {
        void execute(Player player, ItemStack stack, Level level);
    }

    public ForgingTask(ResourceLocation taskId, List<ForgingCriterion> criteria,
                       CompletionAction onComplete, boolean resetAllOnTimeout) {
        this.taskId = taskId;
        this.criteria = List.copyOf(criteria);
        this.onComplete = onComplete;
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

    public boolean processEvent(CompoundTag itemTag, ForgingEventType eventType,
                                EntityType<?> entityType, float amount, long gameTime) {
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
}
