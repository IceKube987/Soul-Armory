package com.iceKube.soulArmory.soulForging;

import com.iceKube.soulArmory.soulForging.tasks.ForgeChestplate;
import com.iceKube.soulArmory.soulForging.tasks.ForgeSoulBow;
import com.iceKube.soulArmory.soulForging.tasks.ForgeSoulSword;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ForgingTasks {
    private static final Map<ResourceLocation, ForgingTask> TASKS = new HashMap<>();

    public static void register(ForgingTask task) {
        if (TASKS.containsKey(task.taskId)) {
            throw new IllegalArgumentException("Duplicate Forging Task: " + task.taskId);
        }
        TASKS.put(task.taskId, task);
    }

    @Nullable
    public static ForgingTask getTask(ResourceLocation id) {
        return TASKS.get(id);
    }

    public static ForgingTask FORGE_SOUL_SWORD = new ForgeSoulSword();
    public static ForgingTask FORGE_SOUL_BOW = new ForgeSoulBow();
    public static ForgingTask FORGE_SOUL_CHESTPLATE = new ForgeChestplate();

    public static void registerForgingTasks() {
        register(FORGE_SOUL_SWORD);
        register(FORGE_SOUL_BOW);
        register(FORGE_SOUL_CHESTPLATE);
    }
}
