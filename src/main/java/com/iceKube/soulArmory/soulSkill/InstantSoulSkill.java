package com.iceKube.soulArmory.soulSkill;

import net.minecraft.resources.ResourceLocation;

public abstract class InstantSoulSkill extends BaseSoulSkill {

    public InstantSoulSkill(ResourceLocation soulSkillId, ResourceLocation soulSkillTexture, String soulSkillName, int soulCost) {
        super(soulSkillId, soulSkillTexture, soulSkillName, soulCost);
    }
}
