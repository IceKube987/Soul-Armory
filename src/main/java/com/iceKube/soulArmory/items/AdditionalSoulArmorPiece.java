package com.iceKube.soulArmory.items;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

// Every piece of the soul armor set other than the chestplate.
public class AdditionalSoulArmorPiece extends ArmorItem {

    public AdditionalSoulArmorPiece(Type pType, Properties pProperties) {
        super(ModArmorMaterials.SOUL_ARMOR, pType, pProperties);
    }

    // The material already rules out durability and the enchanting table, but Forge's anvil-side
    // defaults are not gated on that, so enchanted books still have to be turned away by hand.
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return false;
    }

    @Override
    public boolean isEnchantable(ItemStack pStack) {
        return false;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }
}
