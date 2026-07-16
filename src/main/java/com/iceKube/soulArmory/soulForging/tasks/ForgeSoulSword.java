package com.iceKube.soulArmory.soulForging.tasks;

import com.iceKube.soulArmory.Config;
import com.iceKube.soulArmory.SoulArmoryMod;
import com.iceKube.soulArmory.registries.ItemRegistry;
import com.iceKube.soulArmory.soulForging.ForgingCriterion;
import com.iceKube.soulArmory.soulForging.ForgingEventType;
import com.iceKube.soulArmory.soulForging.ForgingTask;
import com.iceKube.soulArmory.soulForging.TransformHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.List;

public class ForgeSoulSword extends ForgingTask {
    public ForgeSoulSword() {
        super(
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
                false);
    }
}
