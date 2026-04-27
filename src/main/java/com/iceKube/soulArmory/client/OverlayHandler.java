package com.iceKube.soulArmory.client;

import com.iceKube.soulArmory.client.shaders.CoreShaders;
import com.iceKube.soulArmory.items.BaseSoulWeaponItem;
import com.iceKube.soulArmory.items.SoulBowItem;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class OverlayHandler {

    private static final ResourceLocation BAR_TEXTURE = new ResourceLocation("textures/gui/bars.png");

    public static void onRenderGUI(GuiGraphics gui, int x, int y, int tex_w, int tex_h, int w, int h) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        ItemStack itemStack = mc.player.getMainHandItem();
        Item item = itemStack.getItem();
        if (item == null) return;
        if (!(item instanceof BaseSoulWeaponItem baseSoulWeaponItem)) return;
        float soulPoint = 0;
        if (itemStack.getTag() != null && itemStack.getTag().contains(BaseSoulWeaponItem.soulAmountNBT)) {
            soulPoint = itemStack.getTag().getFloat(BaseSoulWeaponItem.soulAmountNBT);
        }

        float soulPercentage = soulPoint / baseSoulWeaponItem.getMaxSoul();
        float soulOverflowPercentage = ((float) baseSoulWeaponItem.getOverflowThreshold()) / baseSoulWeaponItem.getMaxSoul();

        drawSoulBarOutline(gui, x, y, 0, 10, tex_w, tex_h, w, h);
        drawSoulBar(gui, x, y, 0, 15, tex_w * soulPercentage, tex_h, w * soulPercentage, h, soulOverflowPercentage);
        renderItem(gui, x - 8, y - 4, 0.5F);
    }

    private static void drawSoulBarOutline(GuiGraphics gui, int x, int y, int u, int v, float tex_w, float tex_h, float w, float h) {
        var matrix = gui.pose().last().pose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        float minU = u / 256f;
        float maxU = (u + tex_w) / 256f;
        float minV = v / 256f;
        float maxV = (v + tex_h) / 256f;

        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        RenderSystem.setShaderTexture(0, BAR_TEXTURE);

        builder.vertex(matrix, x, y + h, 0).uv(minU, maxV).endVertex();
        builder.vertex(matrix, x + w, y + h, 0).uv(maxU, maxV).endVertex();
        builder.vertex(matrix, x + w, y, 0).uv(maxU, minV).endVertex();
        builder.vertex(matrix, x, y, 0).uv(minU, minV).endVertex();

        Tesselator.getInstance().end();
    }

    private static void drawSoulBar(GuiGraphics gui, int x, int y, int u, int v, float tex_w, float tex_h, float w, float h, float soulOverFlowPercentage) {
        var shader = CoreShaders.soulBar();
        if (shader == null) return;
        shader.safeGetUniform("SoulOverflowPercentage").set(soulOverFlowPercentage);
        drawSoulBar(gui, x, y, u, v, tex_w, tex_h, w, h);
    }

    private static void drawSoulBar(GuiGraphics gui, int x, int y, int u, int v, float tex_w, float tex_h, float w, float h) {
        var shader = CoreShaders.soulBar();
        if (shader == null) return;

        var matrix = gui.pose().last().pose();
        RenderSystem.setShader(CoreShaders::soulBar);

        float minU = u / 256f;
        float maxU = (u + tex_w) / 256f;
        float minV = v / 256f;
        float maxV = (v + tex_h) / 256f;

        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
//        RenderSystem.setShaderTexture(0, BAR_TEXTURE);

        builder.vertex(matrix, x, y + h, 0).uv(minU, maxV).endVertex();
        builder.vertex(matrix, x + w, y + h, 0).uv(maxU, maxV).endVertex();
        builder.vertex(matrix, x + w, y, 0).uv(maxU, minV).endVertex();
        builder.vertex(matrix, x, y, 0).uv(minU, minV).endVertex();

        Tesselator.getInstance().end();
    }

    private static void renderItem(GuiGraphics gui, int x, int y, float scale) {
        PoseStack ps = gui.pose();
        ps.pushPose();

        Minecraft mc = Minecraft.getInstance();
        ItemStack itemStack = mc.player.getMainHandItem();

        ps.translate(x, y, 0);
        ps.scale(scale, scale, scale);

        gui.renderItem(itemStack, 0, 0);

        ps.popPose();
    }

    public static void renderVignette(GuiGraphics gui, int w, int h) {
        var shader = CoreShaders.soulVignette();
        if (shader == null) return;

        var matrix = gui.pose().last().pose();
        RenderSystem.setShader(CoreShaders::soulVignette);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        builder.vertex(matrix, 0, 0 + h, 0).uv(-1, 1).endVertex();
        builder.vertex(matrix, 0 + w, 0 + h, 0).uv(1, 1).endVertex();
        builder.vertex(matrix, 0 + w, 0, 0).uv(1, -1).endVertex();
        builder.vertex(matrix, 0, 0, 0).uv(-1, -1).endVertex();

        Tesselator.getInstance().end();
    }

    public static void renderSkillIcon(GuiGraphics gui, int x, int y, int w, int h) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        ItemStack itemStack = mc.player.getMainHandItem();
        Item item = itemStack.getItem();

        if (!(item instanceof SoulBowItem soulBowItem)) return;

        ResourceLocation texture = soulBowItem.getCurrentSkill(itemStack).soulSkillTexture;
        if (!mc.getResourceManager().getResource(texture).isPresent()) return;

        gui.blit(texture, x, y, 0, 0, w, h,48,48);
    }

    // This method is just for fun.
    // Could come in handy in later development...?
    public static void renderRotatingBlock(GuiGraphics gui, float partialTicks, int x, int y, float scale) {
        Minecraft mc = Minecraft.getInstance();

        PoseStack poseStack = gui.pose();
        poseStack.pushPose();

        poseStack.translate(x, y, 10);

        poseStack.scale(scale, -scale, scale);

        float angle = (System.currentTimeMillis() % 3600) / 10.0f;
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        BlockState state = Blocks.OBSERVER.defaultBlockState();

        mc.getBlockRenderer().renderSingleBlock(
                state,
                poseStack,
                bufferSource,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY
        );

        bufferSource.endBatch();
        poseStack.popPose();
    }

}
