package com.iceKube.soulArmory.utils;

import com.iceKube.soulArmory.items.BaseSoulWeaponItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import static com.iceKube.soulArmory.items.BaseSoulWeaponItem.SOUL_AMOUNT;

public class ActiveTextureHelper {
    public static boolean isActiveTexture(ItemStack stack, Level level){
        if (stack.getItem() instanceof BaseSoulWeaponItem soulWeaponItem){
            if (stack.getTag() != null){
                if (soulWeaponItem.getOverflowThreshold() < soulWeaponItem.getMaxSoul()){
                    long currentGameTime = level != null ? level.getGameTime() : 0;
                    float currentSoul = stack.getTag().getFloat(SOUL_AMOUNT);
                    float effectiveSoul = Math.max(0, currentSoul - soulWeaponItem.calculateSoulDecay(stack, currentGameTime));

                    return effectiveSoul > soulWeaponItem.getOverflowThreshold();
                }
            }
        }
        return false;
    }
}
