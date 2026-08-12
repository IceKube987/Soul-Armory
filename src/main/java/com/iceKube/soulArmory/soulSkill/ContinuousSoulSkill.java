package com.iceKube.soulArmory.soulSkill;

import net.minecraft.resources.ResourceLocation;

public abstract class ContinuousSoulSkill extends BaseSoulSkill {

    public ContinuousSoulSkill(ResourceLocation soulSkillId, ResourceLocation soulSkillTexture, String soulSkillName) {
        super(soulSkillId, soulSkillTexture, soulSkillName);
    }

    /**
     * Minimum number of ticks between two executions. Read live, see {@link #getSoulCost()}.
     */
    public abstract int getExecuteInterval();
}
