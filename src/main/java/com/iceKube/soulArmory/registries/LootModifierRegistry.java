package com.iceKube.soulArmory.registries;

import com.iceKube.soulArmory.SoulArmoryMod;
import com.iceKube.soulArmory.loot.AncientCityLootModifier;
import com.mojang.serialization.Codec;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class LootModifierRegistry {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, SoulArmoryMod.MODID);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> ANCIENT_CITY_INCOMPLETE_SOUL_ITEMS =
            LOOT_MODIFIER_SERIALIZERS.register("ancient_city_incomplete_soul_items",
                    AncientCityLootModifier.CODEC);
}
