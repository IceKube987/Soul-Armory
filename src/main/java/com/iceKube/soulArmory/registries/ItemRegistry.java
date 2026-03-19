package com.iceKube.soulArmory.registries;

import com.iceKube.soulArmory.SoulArmoryMod;
import com.iceKube.soulArmory.items.SoulSword;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SoulArmoryMod.MODID);

    public static final RegistryObject<Item> SOUL_SWORD = ITEMS.register("soul_sword",
            () -> new SoulSword(new Item.Properties()));
}
