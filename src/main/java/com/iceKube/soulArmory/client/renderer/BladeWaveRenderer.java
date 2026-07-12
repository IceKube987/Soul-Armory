package com.iceKube.soulArmory.client.renderer;

import com.iceKube.soulArmory.Config;
import com.iceKube.soulArmory.SoulArmoryMod;
import com.iceKube.soulArmory.entities.BladeWaveEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class BladeWaveRenderer extends EntityRenderer<BladeWaveEntity> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(SoulArmoryMod.MODID,"textures/entity/blade_wave.png");

    public BladeWaveRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public void render(BladeWaveEntity entity, float yaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();

        Vec3 dir   = entity.getMoveDirection();
        Vec3 right = entity.getRightVector();

        float half = (float) (Config.bladeWaveLength * 0.5F);
        Vec3 forward = dir.scale(half);
        Vec3 side    = right.scale(half);

        // Four corners: forward/back left/right
        Vec3 fl = forward.subtract(side);
        Vec3 fr = forward.add(side);
        Vec3 br = side.subtract(forward);
        Vec3 bl = forward.reverse().subtract(side);

        PoseStack.Pose pose = poseStack.last();
        Matrix4f pos  = pose.pose();
        Matrix3f norm = pose.normal();

        VertexConsumer consumer = bufferSource.getBuffer(
                RenderType.entityCutoutNoCull(getTextureLocation(entity)));

        vertex(consumer, pos, norm, fl, 0F, 0F, light);
        vertex(consumer, pos, norm, fr, 1F, 0F, light);
        vertex(consumer, pos, norm, br, 1F, 1F, light);
        vertex(consumer, pos, norm, bl, 0F, 1F, light);

        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
    }

    private void vertex(VertexConsumer consumer, Matrix4f pos, Matrix3f norm,
                        Vec3 v, float u, float uv, int light) {
        consumer.vertex(pos, (float) v.x, 0F, (float) v.z)
                .color(1F, 1F, 1F, 1F)
                .uv(u, uv)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(norm, 0F, 1F, 0F)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(BladeWaveEntity entity) {
        return TEXTURE;
    }
}
