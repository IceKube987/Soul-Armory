package com.iceKube.soulArmory;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = SoulArmoryMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.IntValue SOUL_SWORD_MAX_SOUL = BUILDER
            .comment("The maximum soul amount of Soul Sword")
            .defineInRange("soulSwordMaxSoul", 300, 0, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue SOUL_SWORD_POINTS_PER_DAMAGE = BUILDER
            .comment("How many points of soul is required for 1 extra damage")
            .defineInRange("soulSwordPointPerDamage", 10, 0, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue SOUL_SWORD_POINTS_PER_HEALING = BUILDER
            .comment("How many points of soul is required for 1 HP in healing.")
            .defineInRange("soulSwordPointPerHealing", 7, 0, Integer.MAX_VALUE);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static int soulSwordMaxSoul;
    public static int soulSwordPointsPerDamage;
    public static int soulSowrdPointsPerHealing;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        soulSwordMaxSoul = SOUL_SWORD_MAX_SOUL.get();
        soulSwordPointsPerDamage = SOUL_SWORD_POINTS_PER_DAMAGE.get();
        soulSowrdPointsPerHealing = SOUL_SWORD_POINTS_PER_HEALING.get();
    }
}
