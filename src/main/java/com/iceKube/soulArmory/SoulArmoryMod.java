package com.iceKube.soulArmory;

import com.iceKube.soulArmory.networking.ModPacketHandler;
import com.iceKube.soulArmory.registries.*;
import com.iceKube.soulArmory.soulForging.ForgingTasks;
import com.iceKube.soulArmory.utils.ModItemProperties;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import static com.iceKube.soulArmory.soulSkill.SoulSkills.registerSoulSkills;

@Mod(SoulArmoryMod.MODID)
public class SoulArmoryMod {

    public static final String MODID = "soul_armory";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public SoulArmoryMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        ItemRegistry.ITEMS.register(modEventBus);
        EntityRegistry.ENTITIES.register(modEventBus);
        SoundRegistry.SOUNDS.register(modEventBus);
        LootModifierRegistry.LOOT_MODIFIER_SERIALIZERS.register(modEventBus);
        RecipeRegistry.RECIPE_SERIALIZERS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);

//        modEventBus.addListener(this::addCreative);

        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModPacketHandler.register();
            registerSoulSkills();
            ForgingTasks.registerForgingTasks();
        });
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        ModItemProperties.addCustomProperties();
    }

    // TODO: temp creative mode tab for debugging purpose. Will remove the tab and move the items into vanilla tags in future.
    public static final RegistryObject<CreativeModeTab> SOUL_ARMORY_TAB = CREATIVE_MODE_TABS.register("soul_armory_tab", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> ItemRegistry.SOUL_SWORD.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ItemRegistry.SOUL_SWORD.get());
                output.accept(ItemRegistry.SOUL_BOW.get());
                output.accept(ItemRegistry.INCOMPLETE_SOUL_SWORD.get());
                output.accept(ItemRegistry.INCOMPLETE_SOUL_BOW.get());
            }).build());

}
