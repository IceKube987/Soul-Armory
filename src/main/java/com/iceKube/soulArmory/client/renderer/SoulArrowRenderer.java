package com.iceKube.soulArmory.client.renderer;

import com.iceKube.soulArmory.entities.SoulArrowEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class SoulArrowRenderer extends ArrowRenderer<SoulArrowEntity> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("textures/entity/projectiles/arrow.png");

    public SoulArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(SoulArrowEntity entity) {
        return TEXTURE;
    }
}
