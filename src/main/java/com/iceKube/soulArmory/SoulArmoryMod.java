package com.iceKube.soulArmory;

import com.iceKube.soulArmory.advancements.ModCriteriaTriggers;
import com.iceKube.soulArmory.networking.ModPacketHandler;
import com.iceKube.soulArmory.registries.*;
import com.iceKube.soulArmory.soulForging.ForgingTasks;
import com.iceKube.soulArmory.utils.ModItemProperties;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import static com.iceKube.soulArmory.soulSkill.SoulSkills.registerSoulSkills;

@Mod(SoulArmoryMod.MODID)
public class SoulArmoryMod {

    public static final String MODID = "soul_armory";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public SoulArmoryMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        ItemRegistry.ITEMS.register(modEventBus);
        EntityRegistry.ENTITIES.register(modEventBus);
        SoundRegistry.SOUNDS.register(modEventBus);
        LootModifierRegistry.LOOT_MODIFIER_SERIALIZERS.register(modEventBus);
        RecipeRegistry.RECIPE_SERIALIZERS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::addCreative);

        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        context.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModPacketHandler.register();
            registerSoulSkills();
            ForgingTasks.registerForgingTasks();
            ModCriteriaTriggers.register();
        });
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        ModItemProperties.addCustomProperties();
    }

    private void addCreative(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() != CreativeModeTabs.COMBAT) return;

        event.accept(ItemRegistry.SOUL_SWORD);
        event.accept(ItemRegistry.SOUL_BOW);
        event.accept(ItemRegistry.SOUL_HELMET);
        event.accept(ItemRegistry.SOUL_CHESTPLATE);
        event.accept(ItemRegistry.SOUL_LEGGINGS);
        event.accept(ItemRegistry.SOUL_BOOTS);
        event.accept(ItemRegistry.INCOMPLETE_SOUL_SWORD);
        event.accept(ItemRegistry.INCOMPLETE_SOUL_BOW);
        event.accept(ItemRegistry.INCOMPLETE_SOUL_CHESTPLATE);
    }

}
