package com.iceKube.soulArmory.events;

import com.iceKube.soulArmory.SoulArmoryMod;
import com.iceKube.soulArmory.client.renderer.SoulArrowRenderer;
import com.iceKube.soulArmory.registries.EntityRegistry;
import com.iceKube.soulArmory.utils.KeyBinding;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SoulArmoryMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {

    /**
     * Registers the renderer for {@link com.iceKube.soulArmory.entities.SoulArrowEntity}.
     * Uses the vanilla tipped-arrow renderer as a placeholder so the arrow is
     * visible in-game; replace with a custom renderer/model later if desired.
     */
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegistry.SOUL_ARROW.get(), SoulArrowRenderer::new);
    }

    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        event.register(KeyBinding.TEST_KEY);
    }
}
