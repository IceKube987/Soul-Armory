package com.iceKube.soulArmory.items;

import net.minecraft.world.item.ItemStack;

public interface CanApplySpeedBoost {

    int getPointPerSpeedPercent();

    double getSpeedAdditionPercentage(ItemStack stack);

}
