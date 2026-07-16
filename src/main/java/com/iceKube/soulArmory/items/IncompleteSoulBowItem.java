package com.iceKube.soulArmory.items;

import com.iceKube.soulArmory.Config;
import com.iceKube.soulArmory.soulForging.ForgingTask;
import com.iceKube.soulArmory.soulForging.ForgingTasks;
import net.minecraft.world.item.ItemStack;

public class IncompleteSoulBowItem extends BaseIncompleteSoulItem {

    public IncompleteSoulBowItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ForgingTask getActiveForgingTask(ItemStack stack) {
        return ForgingTasks.FORGE_SOUL_BOW;
    }

    @Override
    public int getMaxActiveTimeTicks() {
        return Config.forgingBowActiveTicks;
    }
}
