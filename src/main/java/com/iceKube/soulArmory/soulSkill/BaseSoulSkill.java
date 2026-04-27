package com.iceKube.soulArmory.soulSkill;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class BaseSoulSkill {
    public final ResourceLocation soulSkillId;
    public final ResourceLocation soulSkillTexture;
    private final String soulSkillName;
    public final int soulCost;

    public BaseSoulSkill(ResourceLocation soulSkillId, ResourceLocation soulSkillTexture, String soulSkillName, int soulCost) {
        this.soulSkillId = soulSkillId;
        this.soulSkillTexture = soulSkillTexture;
        this.soulSkillName = soulSkillName;
        this.soulCost = soulCost;
    }

    public String getTranslationKey() {
        return "skills.soul_armory." + this.soulSkillName;
    }

    public abstract boolean execute(ItemStack stack, Level level, Player player);
}
