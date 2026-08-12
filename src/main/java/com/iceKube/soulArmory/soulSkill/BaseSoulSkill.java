package com.iceKube.soulArmory.soulSkill;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import static com.iceKube.soulArmory.items.BaseSoulWeaponItem.SOUL_AMOUNT;

public abstract class BaseSoulSkill {
    public final ResourceLocation soulSkillId;
    public final ResourceLocation soulSkillTexture;
    private final String soulSkillName;

    public BaseSoulSkill(ResourceLocation soulSkillId, ResourceLocation soulSkillTexture, String soulSkillName) {
        this.soulSkillId = soulSkillId;
        this.soulSkillTexture = soulSkillTexture;
        this.soulSkillName = soulSkillName;
    }

    /**
     * How much soul one execution costs.
     * <p>
     * Read live instead of stored in a field: the skill instances are created while the item
     * registry is being built, which is before the config file is loaded, so anything captured
     * in the constructor would just be 0.
     */
    public abstract int getSoulCost();

    public String getTranslationKey() {
        return "skills.soul_armory." + this.soulSkillName;
    }

    public boolean execute(ItemStack stack, Level level, Player player) {
        stack.getTag().putFloat(SOUL_AMOUNT, stack.getTag().getFloat(SOUL_AMOUNT) - getSoulCost());
        return true;
    }
}
