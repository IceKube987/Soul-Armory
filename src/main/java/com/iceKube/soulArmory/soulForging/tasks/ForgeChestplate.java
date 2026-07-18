package com.iceKube.soulArmory.soulForging.tasks;

import com.iceKube.soulArmory.Config;
import com.iceKube.soulArmory.SoulArmoryMod;
import com.iceKube.soulArmory.soulForging.ForgingCriterion;
import com.iceKube.soulArmory.soulForging.ForgingEventType;
import com.iceKube.soulArmory.soulForging.ForgingTask;
import com.iceKube.soulArmory.soulForging.TransformHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.List;

public class ForgeChestplate extends ForgingTask {
    public ForgeChestplate() {
        super(new ResourceLocation(SoulArmoryMod.MODID, "forge_soul_chestplate"),
                List.of(
                        new ForgingCriterion("sonic_hits", ForgingEventType.RECEIVE_DAMAGE,
                                Config.forgingChestplateSonicHits,
                                Config.forgingChestplateTimeoutTicks,
                                type -> type == EntityType.WARDEN,
                                null,
                                null)
                ),
                (player, stack, level) -> TransformHelper.transformChestplate(player, stack, level),
                null,
                false);
    }
}
