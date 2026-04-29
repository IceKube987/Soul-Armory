package com.iceKube.soulArmory.soulSkill.skills;

import com.iceKube.soulArmory.Config;
import com.iceKube.soulArmory.SoulArmoryMod;
import com.iceKube.soulArmory.soulSkill.InstantSoulSkill;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import static com.iceKube.soulArmory.items.BaseSoulWeaponItem.soulAmountNBT;

public class HealSkill extends InstantSoulSkill {
    public HealSkill() {
        super(new ResourceLocation(SoulArmoryMod.MODID, "heal"),
                null,
                "heal",
                0); // dynamically deduce soul
    }

    @Override
    public boolean execute(ItemStack stack, Level level, Player player) {
        if (stack.getTag() == null) return false;

        CompoundTag tag = stack.getTag();
        float currentSoul = tag.getFloat(soulAmountNBT);

        float currentHealth = player.getHealth();
        float maxHealth = player.getMaxHealth();
        float healthMissing = maxHealth - currentHealth;

        if (healthMissing <= 0 || currentSoul <= 0) return false;

        // How many HP we can afford to heal with available soul
        int affordableHeal = (int) (currentSoul / Config.soulSwordPointsPerHealing);

        // Actual healing: min of what's needed (rounded up to nearest int) and what we can afford
        int healingAmount = (int) Math.min(Math.ceil(healthMissing), affordableHeal);

        if (healingAmount <= 0) return false;

        // Deduct soul
        tag.putFloat(soulAmountNBT, currentSoul - (float) (healingAmount * Config.soulSwordPointsPerHealing));

        // Apply healing
        player.heal(healingAmount);

        return true; // dynamically deduce soul, not use super()
    }
}
