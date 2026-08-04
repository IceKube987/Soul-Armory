package com.iceKube.soulArmory.events;

import com.iceKube.soulArmory.SoulArmoryMod;
import com.iceKube.soulArmory.client.renderer.BladeWaveRenderer;
import com.iceKube.soulArmory.client.renderer.SoulArrowRenderer;
import com.iceKube.soulArmory.client.shaders.CoreShaders;
import com.iceKube.soulArmory.registries.EntityRegistry;
import com.iceKube.soulArmory.utils.KeyBinding;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.io.UncheckedIOException;

@Mod.EventBusSubscriber(value = Dist.CLIENT,modid = SoulArmoryMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModEvents {

    /**
     * Registers the renderer for {@link com.iceKube.soulArmory.entities.SoulArrowEntity}.
     * Uses the vanilla tipped-arrow renderer as a placeholder.
     */
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegistry.SOUL_ARROW.get(), SoulArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SOUL_ARROW_SCATTER.get(), SoulArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SOUL_ARROW_RAPID_FIRE.get(), SoulArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.BLADE_WAVE.get(), BladeWaveRenderer::new);
    }

    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        event.register(KeyBinding.SWITCH_SKILL);
        event.register(KeyBinding.ACTIVATE_RAGE);
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent evt) {
        CoreShaders.init((id, vertexFormat, onLoaded) -> {
            try {
                evt.registerShader(
                        new ShaderInstance(evt.getResourceProvider(), id, vertexFormat),
                        onLoaded
                );
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }
}
