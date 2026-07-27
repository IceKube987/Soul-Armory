package com.iceKube.soulArmory.items;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Every piece of the soul armor set other than the chestplate.
 * <p>
 * These pieces hold no state of their own — the soul pool and the Soul Rage flag live on the
 * chestplate (see {@link SoulChestplateItem}). All they do is raise the soul cap and unlock one
 * Soul Rage effect each, both of which the chestplate decides. So a single class covers the
 * helmet, the leggings and the boots; they differ only by their {@link Type}.
 */
public class AdditionalSoulArmorPiece extends ArmorItem {

    // Renders as iron armor for now. Swap in a custom ArmorMaterial once there are real textures.
    public AdditionalSoulArmorPiece(Type pType, Properties pProperties) {
        super(ArmorMaterials.IRON, pType, pProperties);
    }

    // No durability, not enchantable.
    // Unlike the soul weapons, overriding getDamage is not enough here: ArmorItem's constructor
    // forces a durability onto the properties, so the stack really is damageable unless we say
    // otherwise. canBeDepleted also hides the durability bar and blocks anvil repair for free.
    @Override
    public boolean canBeDepleted() {
        return false;
    }

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
