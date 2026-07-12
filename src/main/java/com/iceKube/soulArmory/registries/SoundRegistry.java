package com.iceKube.soulArmory.registries;

import com.iceKube.soulArmory.SoulArmoryMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SoundRegistry {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, SoulArmoryMod.MODID);

    public static final RegistryObject<SoundEvent> SOUL_SWORD_HEAL = SOUNDS.register("soul_sword_heal",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(SoulArmoryMod.MODID, "soul_sword_heal")));
}
