package com.iceKube.soulArmory.items;

import com.iceKube.soulArmory.SoulArmoryMod;
import net.minecraft.Util;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;

/**
 * Armor materials the mod adds. In 1.20.1 these are plain objects rather than registry entries, so
 * this mirrors the shape of vanilla's {@link net.minecraft.world.item.ArmorMaterials}.
 */
public enum ModArmorMaterials implements ArmorMaterial {

    /**
     * Iron's protection values, but the armor never wears out and takes no enchantments.
     * <p>
     * A durability of 0 is what actually makes the armor unbreakable: {@link ArmorItem}'s
     * constructor feeds it to {@code Properties#defaultDurability}, which leaves the stack
     * non-damageable, and that in turn makes {@code Item#isEnchantable} false as well.
     */
    SOUL_ARMOR("soul_armor", 0, Util.make(new EnumMap<>(ArmorItem.Type.class), (map) -> {
        map.put(ArmorItem.Type.BOOTS, 2);
        map.put(ArmorItem.Type.LEGGINGS, 5);
        map.put(ArmorItem.Type.CHESTPLATE, 6);
        map.put(ArmorItem.Type.HELMET, 2);
    }), 0, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, Ingredient.EMPTY),
    SOUL_ARMOR_INCOMPLETE("soul_armor_incomplete", 0, Util.make(new EnumMap<>(ArmorItem.Type.class), (map) -> {
        map.put(ArmorItem.Type.BOOTS, 2);
        map.put(ArmorItem.Type.LEGGINGS, 5);
        map.put(ArmorItem.Type.CHESTPLATE, 6);
        map.put(ArmorItem.Type.HELMET, 2);
    }), 0, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, Ingredient.EMPTY);


    private final String name;
    private final int durability;
    private final EnumMap<ArmorItem.Type, Integer> protectionForType;
    private final int enchantmentValue;
    private final SoundEvent equipSound;
    private final float toughness;
    private final float knockbackResistance;
    private final Ingredient repairIngredient;

    ModArmorMaterials(String name, int durability, EnumMap<ArmorItem.Type, Integer> protectionForType, int enchantmentValue, SoundEvent equipSound, float toughness, float knockbackResistance, Ingredient repairIngredient) {
        this.name = name;
        this.durability = durability;
        this.protectionForType = protectionForType;
        this.enchantmentValue = enchantmentValue;
        this.equipSound = equipSound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = repairIngredient;
    }

    @Override
    public int getDurabilityForType(ArmorItem.Type pType) {
        return this.durability;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type pType) {
        return this.protectionForType.get(pType);
    }

    @Override
    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }

    @Override
    public SoundEvent getEquipSound() {
        return this.equipSound;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredient;
    }

    /**
     * Forge splits this on the colon to build the worn-armor texture path, so the mod id has to be
     * part of it: the layers are looked up at
     * {@code soul_armory:textures/models/armor/soul_armor_layer_{1,2}.png}.
     */
    @Override
    public String getName() {
        return SoulArmoryMod.MODID + ":" + this.name;
    }

    @Override
    public float getToughness() {
        return this.toughness;
    }

    @Override
    public float getKnockbackResistance() {
        return this.knockbackResistance;
    }
}
