package com.iceKube.soulArmory.items;

import com.iceKube.soulArmory.soulSkill.BaseSoulSkill;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public interface UseSoulSkillSystem {
    BaseSoulSkill getCurrentSkill(ItemStack stack);

    List<BaseSoulSkill> getAvailableSkills(ItemStack stack);

    void setDefaultSkill(ItemStack stack, Level level);

    /**
     * Switches the stack to the skill at {@code index} within {@link #getAvailableSkills}, the seam
     * the skill radial menu goes through. Server-side only.
     * <p>
     * The index comes straight off the wire, so implementations must validate it before writing
     * anything. Each item owns its own switch cost, sound and VFX from here.
     */
    void setCurrentSkill(ItemStack stack, int index, ServerPlayer player);
}
