package com.iceKube.soulArmory.registries;

import com.iceKube.soulArmory.SoulArmoryMod;
import com.iceKube.soulArmory.recipes.SoulForgingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RecipeRegistry {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, SoulArmoryMod.MODID);

    public static final RegistryObject<RecipeSerializer<SoulForgingRecipe>> SOUL_FORGING_SERIALIZER =
            RECIPE_SERIALIZERS.register("soul_forging", SoulForgingRecipe.Serializer::new);
}
