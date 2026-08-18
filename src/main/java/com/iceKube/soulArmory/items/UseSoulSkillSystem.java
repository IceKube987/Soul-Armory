package com.iceKube.soulArmory.items;

import com.iceKube.soulArmory.soulSkill.BaseSoulSkill;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public interface UseSoulSkillSystem {
    BaseSoulSkill getCurrentSkill(ItemStack stack);

    List<BaseSoulSkill> getAvailableSkills(ItemStack stack);

    // Every skill this weapon type can ever hold, unlocked or not.
    List<BaseSoulSkill> getAllPossibleSkills();

    void setDefaultSkill(ItemStack stack, Level level);
    void setCurrentSkill(ItemStack stack, int index, ServerPlayer player);
}
