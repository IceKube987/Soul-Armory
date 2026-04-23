package com.iceKube.soulArmory.client;

import com.iceKube.soulArmory.client.shaders.CoreShaders;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class OverlayHandler {

    public static void drawSoulBar(GuiGraphics gui, int x, int y, int u, int v, int tex_w, int tex_h, int w, int h) {
        var shader = CoreShaders.soulBar;
        if (shader == null) return;

        var matrix = gui.pose().last().pose();
        RenderSystem.setShader(CoreShaders::soulBar);

        float minU = u / 256f;
        float maxU = (u + tex_w) / 256f;
        float minV = v / 256f;
        float maxV = (v + tex_h) / 256f;

        shader.safeGetUniform("UVRange").set(new float[]{minU, maxU, minV, maxV});

        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        RenderSystem.setShaderTexture(0, new ResourceLocation("textures/gui/bars.png"));

        builder.vertex(matrix, x, y + h, 0).uv(minU, maxV).endVertex();
        builder.vertex(matrix, x + w, y + h, 0).uv(maxU, maxV).endVertex();
        builder.vertex(matrix, x + w, y, 0).uv(maxU, minV).endVertex();
        builder.vertex(matrix, x, y, 0).uv(minU, minV).endVertex();

        Tesselator.getInstance().end();
    }

    public static void renderRotatingBlock(GuiGraphics gui, float partialTicks, int x, int y, float scale) {
        Minecraft mc = Minecraft.getInstance();

        PoseStack poseStack = gui.pose();
        poseStack.pushPose(); // 压栈，防止影响后续渲染

        // 1. 移动到中心
        poseStack.translate(x, y, 10); // 100 是 Z 轴深度，确保它在 UI 背景之上

        // 2. 缩放
        poseStack.scale(scale, -scale, scale); // Y 轴翻转，因为 GUI Y 轴向下，模型 Y 轴向上

        // 3. 旋转 (随时间变化)
        float angle = (System.currentTimeMillis() % 3600) / 10.0f; // 3.6秒转一圈
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));

        // 4. 渲染方块
        // 我们需要告诉渲染引擎使用哪种渲染类型（贴图、着色器）
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        // 获取草方块的 BlockState
        BlockState state = Blocks.OBSERVER.defaultBlockState();

        // 渲染！
        mc.getBlockRenderer().renderSingleBlock(
                state,
                poseStack,
                bufferSource,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY
        );

        bufferSource.endBatch(); // 立即绘制
        poseStack.popPose(); // 出栈
    }

}
