package com.iceKube.soulArmory.utils;

import com.iceKube.soulArmory.Config;
import com.iceKube.soulArmory.items.BaseIncompleteSoulItem;
import com.iceKube.soulArmory.items.IncompleteSoulBowItem;
import com.iceKube.soulArmory.registries.ItemRegistry;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import static com.iceKube.soulArmory.items.BaseSoulWeaponItem.SOUL_AMOUNT;

public class ModItemProperties {
    public static void addCustomProperties() {
        makeSoulBow();
        makeIncompleteSoulBow();
        makeSoulSword();
        makeIncompleteSoulSword();
    }

    private static void makeSoulBow() {
        ItemProperties.register(ItemRegistry.SOUL_BOW.get(), new ResourceLocation("pull"), (stack, clientLevel, livingEntity, i) -> {
            if (livingEntity == null) return 0.0F;
            if (!livingEntity.getUseItem().is(ItemRegistry.SOUL_BOW.get())) return 0.0F;
            if (stack.getTag() == null) return 0.0F;
            // Compare ID
            CompoundTag stackTag = stack.getTag();
            CompoundTag useTag = livingEntity.getUseItem().getTag();
            if (useTag == null) return 0.0F;
            if (!stackTag.contains("soul_armory.instanceId")) return 0.0F;

            // speed up the bow pulling animation.
            double soulMultiplier = (1 + 0.01 * (int) (stack.getTag().getFloat(SOUL_AMOUNT) / Config.soulBowPointPerDamagePercent));
            int chargedTicks = stack.getUseDuration() - livingEntity.getUseItemRemainingTicks();
            chargedTicks /= 2; // Same as releaseUsing
            return (float) Math.min(chargedTicks * soulMultiplier / 20.0F, 1.0F);
        });

        ItemProperties.register(ItemRegistry.SOUL_BOW.get(), new ResourceLocation("pulling"), (stack, clientLevel, livingEntity, i) -> {
            if (livingEntity == null) return 0.0F;
            if (!livingEntity.isUsingItem()) return 0.0F;
            if (!livingEntity.getUseItem().is(ItemRegistry.SOUL_BOW.get())) return 0.0F;
            // Compare ID
            CompoundTag stackTag = stack.getTag();
            CompoundTag useTag = livingEntity.getUseItem().getTag();
            if (stackTag == null || useTag == null) return 0.0F;
            if (!stackTag.contains("soul_armory.instanceId")) return 0.0F;
            return stackTag.getUUID("soul_armory.instanceId")
                    .equals(useTag.getUUID("soul_armory.instanceId")) ? 1.0F : 0.0F;
        });

        ItemProperties.register(ItemRegistry.SOUL_BOW.get(), new ResourceLocation("active"), (stack, clientLevel, livingEntity, i) -> {
            if (livingEntity == null) return 0.0F;
            if (stack.getTag() == null) return 0.0F;

            CompoundTag stackTag = stack.getTag();
            if (stackTag == null) return 0.0F;
            if (!stackTag.contains("soul_armory.instanceId")) return 0.0F;

            if (ActiveTextureHelper.isActiveTexture(stack, clientLevel)) return 1;

            return 0;
        });
    }

    private static void makeIncompleteSoulBow() {
        ItemProperties.register(ItemRegistry.INCOMPLETE_SOUL_BOW.get(), new ResourceLocation("pull"), (stack, clientLevel, livingEntity, i) -> {
            if (livingEntity == null) return 0.0F;
            if (!livingEntity.getUseItem().is(ItemRegistry.INCOMPLETE_SOUL_BOW.get())) return 0.0F;
            if (stack.getTag() == null) return 0.0F;
            // Compare ID
            CompoundTag stackTag = stack.getTag();
            CompoundTag useTag = livingEntity.getUseItem().getTag();
            if (useTag == null) return 0.0F;
            if (!stackTag.contains("soul_armory.instanceId")) return 0.0F;

            // speed up the bow pulling animation.
            double soulMultiplier = 1;
            if (stack.getItem() instanceof IncompleteSoulBowItem && BaseIncompleteSoulItem.isActive(stack)) {
                soulMultiplier += 0.01 * (int) (Config.soulBowMaxSoul / Config.soulBowPointPerDamagePercent);
            }
            int chargedTicks = stack.getUseDuration() - livingEntity.getUseItemRemainingTicks();
            chargedTicks /= 2; // Same as releaseUsing
            return (float) Math.min(chargedTicks * soulMultiplier / 20.0F, 1.0F);
        });

        ItemProperties.register(ItemRegistry.INCOMPLETE_SOUL_BOW.get(), new ResourceLocation("pulling"), (stack, clientLevel, livingEntity, i) -> {
            if (livingEntity == null) return 0.0F;
            if (!livingEntity.isUsingItem()) return 0.0F;
            if (!livingEntity.getUseItem().is(ItemRegistry.INCOMPLETE_SOUL_BOW.get())) return 0.0F;
            // Compare ID
            CompoundTag stackTag = stack.getTag();
            CompoundTag useTag = livingEntity.getUseItem().getTag();
            if (stackTag == null || useTag == null) return 0.0F;
            if (!stackTag.contains("soul_armory.instanceId")) return 0.0F;
            return stackTag.getUUID("soul_armory.instanceId")
                    .equals(useTag.getUUID("soul_armory.instanceId")) ? 1.0F : 0.0F;
        });

        ItemProperties.register(ItemRegistry.INCOMPLETE_SOUL_BOW.get(), new ResourceLocation("active"), (stack, clientLevel, livingEntity, i) -> {
            if (livingEntity == null) return 0.0F;
            if (stack.getTag() == null) return 0.0F;

            CompoundTag stackTag = stack.getTag();
            if (stackTag == null) return 0.0F;
            if (!stackTag.contains("soul_armory.instanceId")) return 0.0F;

            if (BaseIncompleteSoulItem.isActive(stack)) return 1;

            return 0;
        });
    }

    private static void makeSoulSword() {
        ItemProperties.register(ItemRegistry.SOUL_SWORD.get(), new ResourceLocation("active"), (stack, clientLevel, livingEntity, i) -> {
            if (livingEntity == null) return 0.0F;
            if (stack.getTag() == null) return 0.0F;

            CompoundTag stackTag = stack.getTag();
            if (stackTag == null) return 0.0F;
            if (!stackTag.contains("soul_armory.instanceId")) return 0.0F;

            if (ActiveTextureHelper.isActiveTexture(stack, clientLevel)) return 1;

            return 0;
        });
    }

    private static void makeIncompleteSoulSword() {
        ItemProperties.register(ItemRegistry.INCOMPLETE_SOUL_SWORD.get(), new ResourceLocation("active"), (stack, clientLevel, livingEntity, i) -> {
            if (livingEntity == null) return 0.0F;
            if (stack.getTag() == null) return 0.0F;

            CompoundTag stackTag = stack.getTag();
            if (stackTag == null) return 0.0F;
            if (!stackTag.contains("soul_armory.instanceId")) return 0.0F;

            if (BaseIncompleteSoulItem.isActive(stack)) return 1;

            return 0;
        });
    }
}
