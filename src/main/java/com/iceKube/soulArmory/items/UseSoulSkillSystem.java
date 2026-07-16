package com.iceKube.soulArmory.items;

import com.iceKube.soulArmory.soulSkill.BaseSoulSkill;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public interface UseSoulSkillSystem {
    BaseSoulSkill getCurrentSkill(ItemStack stack);

    List<BaseSoulSkill> getAvailableSkills(ItemStack stack);

    void setDefaultSkill(ItemStack stack, Level level);
}
