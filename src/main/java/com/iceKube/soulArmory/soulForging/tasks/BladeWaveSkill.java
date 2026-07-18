package com.iceKube.soulArmory.soulForging.tasks;

import com.iceKube.soulArmory.SoulArmoryMod;
import com.iceKube.soulArmory.items.BaseSoulWeaponItem;
import com.iceKube.soulArmory.soulForging.ForgingCriterion;
import com.iceKube.soulArmory.soulForging.ForgingEventType;
import com.iceKube.soulArmory.soulForging.ForgingTask;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class BladeWaveSkill extends ForgingTask {
    public BladeWaveSkill() {
        super(new ResourceLocation(SoulArmoryMod.MODID,"blade_wave_unlock"),
                List.of(
                        new ForgingCriterion("deal_damage", ForgingEventType.DEAL_DAMAGE,
                                2000,
                                0,
                                null,
                                null),
                        new ForgingCriterion("kill_3_warden_in_2_min", ForgingEventType.KILL_ENTITY,
                                3,
                                2400,
                                entityType -> entityType == EntityType.WARDEN,
                                null)
                        ),
                BladeWaveSkill::onComplete,
                false);
    }

    public static void onComplete(Player player, ItemStack stack, Level level){
        stack.getTag().putString(BaseSoulWeaponItem.currentForgingTask,"none");
    }
}
