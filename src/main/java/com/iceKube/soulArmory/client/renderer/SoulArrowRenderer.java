package com.iceKube.soulArmory.client.renderer;

import com.iceKube.soulArmory.entities.SoulArrowEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

//TODO: Need a custom model (currently using vanilla model for arrow)
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
