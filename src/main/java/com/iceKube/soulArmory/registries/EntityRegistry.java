package com.iceKube.soulArmory.registries;

import com.iceKube.soulArmory.SoulArmoryMod;
import com.iceKube.soulArmory.entities.BladeWaveEntity;
import com.iceKube.soulArmory.entities.SoulArrowEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntityRegistry {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, SoulArmoryMod.MODID);

    public static final RegistryObject<EntityType<SoulArrowEntity>> SOUL_ARROW =
            ENTITIES.register("soul_arrow", () ->
                    EntityType.Builder.<SoulArrowEntity>of(SoulArrowEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(4)
                            .updateInterval(20)
                            .build("soul_armory:soul_arrow"));

    public static final RegistryObject<EntityType<BladeWaveEntity>> BLADE_WAVE =
            ENTITIES.register("blade_wave", () ->
                    EntityType.Builder.<BladeWaveEntity>of(BladeWaveEntity::new, MobCategory.MISC)
                            .sized(0.1F, 0.1F)
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .noSummon()
                            .build("soul_armory:blade_wave"));
}
