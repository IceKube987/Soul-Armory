package com.iceKube.soulArmory.registries;

import com.iceKube.soulArmory.SoulArmoryMod;
import com.iceKube.soulArmory.items.AdditionalSoulArmorPiece;
import com.iceKube.soulArmory.items.IncompleteSoulBowItem;
import com.iceKube.soulArmory.items.IncompleteSoulSwordItem;
import com.iceKube.soulArmory.items.SoulBowItem;
import com.iceKube.soulArmory.items.SoulChestplateItem;
import com.iceKube.soulArmory.items.SoulSwordItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SoulArmoryMod.MODID);

    public static final RegistryObject<Item> SOUL_SWORD = ITEMS.register("soul_sword",
            () -> new SoulSwordItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SOUL_BOW = ITEMS.register("soul_bow",
            () -> new SoulBowItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> INCOMPLETE_SOUL_SWORD = ITEMS.register("incomplete_soul_sword",
            () -> new IncompleteSoulSwordItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> INCOMPLETE_SOUL_BOW = ITEMS.register("incomplete_soul_bow",
            () -> new IncompleteSoulBowItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SOUL_CHESTPLATE = ITEMS.register("soul_chestplate",
            () -> new SoulChestplateItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SOUL_HELMET = ITEMS.register("soul_helmet",
            () -> new AdditionalSoulArmorPiece(ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SOUL_LEGGINGS = ITEMS.register("soul_leggings",
            () -> new AdditionalSoulArmorPiece(ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SOUL_BOOTS = ITEMS.register("soul_boots",
            () -> new AdditionalSoulArmorPiece(ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1)));
}
