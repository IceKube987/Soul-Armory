package com.iceKube.soulArmory.soulSkill;

import net.minecraft.resources.ResourceLocation;

public abstract class ContinuousSoulSkill extends BaseSoulSkill{

    public final int executeInterval;

    public ContinuousSoulSkill(ResourceLocation soulSkillId, ResourceLocation soulSkillTexture, String soulSkillName, int soulCost, int executeInterval) {
        super(soulSkillId, soulSkillTexture, soulSkillName, soulCost);
        this.executeInterval = executeInterval;
    }
}
