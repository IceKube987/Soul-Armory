package com.iceKube.soulArmory.soulForging;

import com.iceKube.soulArmory.Config;
import com.iceKube.soulArmory.SoulArmoryMod;
import com.iceKube.soulArmory.registries.ItemRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
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

    public static ForgingTask FORGE_SOUL_SWORD;
    public static ForgingTask FORGE_SOUL_BOW;
    public static ForgingTask FORGE_SOUL_CHESTPLATE;

    public static void registerForgingTasks() {
        FORGE_SOUL_SWORD = new ForgingTask(
                new ResourceLocation(SoulArmoryMod.MODID, "forge_soul_sword"),
                List.of(
                        new ForgingCriterion("deal_damage", ForgingEventType.DEAL_DAMAGE,
                                Config.forgingSwordDamageTarget,
                                Config.forgingSwordTimeoutTicks,
                                type -> type == EntityType.WARDEN),
                        new ForgingCriterion("kill_warden", ForgingEventType.KILL_ENTITY,
                                1,
                                0,
                                type -> type == EntityType.WARDEN)
                ),
                (player, stack, level) -> TransformHelper.transformToFullWeapon(
                        player, stack, ItemRegistry.SOUL_SWORD, Config.soulSwordMaxSoul, level),
                false
        );
        register(FORGE_SOUL_SWORD);

        FORGE_SOUL_BOW = new ForgingTask(
                new ResourceLocation(SoulArmoryMod.MODID, "forge_soul_bow"),
                List.of(
                        new ForgingCriterion("deal_damage", ForgingEventType.DEAL_DAMAGE,
                                Config.forgingBowDamageTarget,
                                Config.forgingBowTimeoutTicks,
                                type -> type == EntityType.WARDEN),
                        new ForgingCriterion("kill_warden", ForgingEventType.KILL_ENTITY,
                                1,
                                0,
                                type -> type == EntityType.WARDEN)
                ),
                (player, stack, level) -> TransformHelper.transformToFullWeapon(
                        player, stack, ItemRegistry.SOUL_BOW, Config.soulBowMaxSoul, level),
                false
        );
        register(FORGE_SOUL_BOW);

        FORGE_SOUL_CHESTPLATE = new ForgingTask(
                new ResourceLocation(SoulArmoryMod.MODID, "forge_soul_chestplate"),
                List.of(
                        new ForgingCriterion("sonic_hits", ForgingEventType.RECEIVE_DAMAGE,
                                Config.forgingChestplateSonicHits,
                                Config.forgingChestplateTimeoutTicks,
                                type -> type == EntityType.WARDEN)
                ),
                (player, stack, level) -> TransformHelper.transformChestplate(player, stack, level),
                false
        );
        register(FORGE_SOUL_CHESTPLATE);
    }
}
