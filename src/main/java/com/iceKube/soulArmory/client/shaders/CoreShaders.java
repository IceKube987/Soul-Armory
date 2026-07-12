package com.iceKube.soulArmory.client.shaders;

import com.iceKube.soulArmory.SoulArmoryMod;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.function.Consumer;

public class CoreShaders {
    private static ShaderInstance soulBar;
    private static ShaderInstance showUV;
    private static ShaderInstance soulVignette;

    public static void init(TriConsumer<ResourceLocation, VertexFormat, Consumer<ShaderInstance>> registrations) {
        registrations.accept(
                new ResourceLocation(SoulArmoryMod.MODID, "soul_bar"),
                DefaultVertexFormat.POSITION_TEX,
                inst -> soulBar = inst
        );
        registrations.accept(
                new ResourceLocation(SoulArmoryMod.MODID, "show_uv"),
                DefaultVertexFormat.POSITION_TEX,
                inst -> showUV = inst
        );
        registrations.accept(
                new ResourceLocation(SoulArmoryMod.MODID, "soul_vignette"),
                DefaultVertexFormat.POSITION_TEX,
                inst -> soulVignette = inst
        );

    }

    public static ShaderInstance soulBar() {
        return soulBar;
    }

    public static ShaderInstance showUV() {
        return showUV;
    }

    public static ShaderInstance soulVignette() {
        return soulVignette;
    }
}
