package com.iceKube.soulArmory.utils;

import com.iceKube.soulArmory.Config;
import com.iceKube.soulArmory.registries.ItemRegistry;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;

import static com.iceKube.soulArmory.items.BaseSoulWeaponItem.soulAmountNBT;

public class ModItemProperties {
    public static void addCustomProperties(){
        makeSoulBow();
    }

    private static void makeSoulBow(){
        ItemProperties.register(ItemRegistry.SOUL_BOW.get(), new ResourceLocation("pull"), (stack, clientLevel, livingEntity, i) -> {
            if (livingEntity == null) return 0.0F;
            if (livingEntity.getUseItem() != stack) return 0.0F;
            if (stack.getTag() == null) return 0.0F;

            // speed up the bow pulling animation.
            double soulMultiplier = (1 + 0.01 * (int)(stack.getTag().getFloat(soulAmountNBT) / Config.soulBowPointPerDamagePercent));
            int chargedTicks = stack.getUseDuration() - livingEntity.getUseItemRemainingTicks();
            chargedTicks /= 2; // Same as releaseUsing
            return (float) Math.min(chargedTicks * soulMultiplier / 20.0F, 1.0F);
        });

        ItemProperties.register(ItemRegistry.SOUL_BOW.get(), new ResourceLocation("pulling"), (stack, clientLevel, livingEntity, i) -> {
            return livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == stack ? 1.0F : 0.0F;
        });
    }
}
