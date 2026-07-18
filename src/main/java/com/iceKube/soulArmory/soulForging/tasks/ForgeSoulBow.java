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

public class ForgeSoulBow extends ForgingTask {
    public ForgeSoulBow() {
        super(new ResourceLocation(SoulArmoryMod.MODID, "forge_soul_bow"),
                List.of(
                        new ForgingCriterion("deal_damage_to_warden", ForgingEventType.DEAL_DAMAGE,
                                Config.forgingBowDamageTarget,
                                Config.forgingBowTimeoutTicks,
                                type -> type == EntityType.WARDEN,
                                null,
                                null),
                        new ForgingCriterion("kill_warden", ForgingEventType.KILL_ENTITY,
                                1,
                                0,
                                type -> type == EntityType.WARDEN,
                                null,
                                null)
                ),
                (player, stack, level) -> TransformHelper.transformToFullWeapon(
                        player, stack, ItemRegistry.SOUL_BOW, Config.soulBowMaxSoul, level),
                null,
                false);
    }
}
