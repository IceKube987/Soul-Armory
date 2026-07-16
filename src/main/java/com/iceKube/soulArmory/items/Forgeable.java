package com.iceKube.soulArmory.items;

import com.iceKube.soulArmory.soulForging.ForgingTask;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public interface Forgeable {
    @Nullable
    ForgingTask getActiveForgingTask(ItemStack stack);
}
