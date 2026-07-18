package com.iceKube.soulArmory.client;

import com.iceKube.soulArmory.client.shaders.CoreShaders;
import com.iceKube.soulArmory.items.BaseSoulWeaponItem;
import com.iceKube.soulArmory.items.UseSoulSkillSystem;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public class OverlayHandler {

    private static final ResourceLocation BAR_TEXTURE = new ResourceLocation("textures/gui/bars.png");

    // --- Switch-skill VFX state ---
    // Written only when a skill switch is signaled from the server (see triggerSwitchSkillVFX).
    // The render path reads these every frame and is otherwise read-only.
    private static long stanceSeed;
    private static int stanceSpawnTick;

    // Client-side tick counter, advanced once per client tick (see ModForgeEvents.onClientTick).
    private static int clientTick;

    public static void renderSoulBar(GuiGraphics gui, int x, int y, int tex_w, int tex_h, int w, int h) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        ItemStack itemStack = mc.player.getMainHandItem();
        Item item = itemStack.getItem();
        if (item == null) return;
        if (!(item instanceof BaseSoulWeaponItem baseSoulWeaponItem)) return;
        float soulPoint = 0;
        if (itemStack.getTag() != null && itemStack.getTag().contains(BaseSoulWeaponItem.SOUL_AMOUNT)) {
            soulPoint = itemStack.getTag().getFloat(BaseSoulWeaponItem.SOUL_AMOUNT);
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

        if (!(item instanceof UseSoulSkillSystem skillItem)) return;

        ResourceLocation texture = skillItem.getCurrentSkill(itemStack).soulSkillTexture;
        if (texture == null) return;
        if (!mc.getResourceManager().getResource(texture).isPresent()) return;

        gui.blit(texture, x, y, w, h, 0, 0, 32, 32, 32, 32);
    }

    // Advances the client tick counter. Called once per client tick.
    public static void onClientTick() {
        clientTick++;
    }

    // Signals a skill switch: seeds a fresh burst of bars starting from the current client tick.
    // This is the ONLY place stanceSeed / stanceSpawnTick are written.
    public static void triggerSwitchSkillVFX() {
        stanceSeed = System.nanoTime();
        stanceSpawnTick = clientTick;
    }

    // Used when soul sword switches skill.
    // Draws a burst of translucent blue vertical bars rising from the bottom of the HUD.
    // Purely cosmetic and client-side; all motion/alpha is derived deterministically from the seed.
    public static void renderSwitchSkillVFX(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
        // Skip if no switch has happened yet.
        if (stanceSeed == 0) return;

        // Constants. Speed, height and width are fractions of the Minecraft
        // window size, so the effect scales with the window and GUI scale rather than being
        // fixed. Width is a fraction of window width; height and vertical speed
        // are fractions of window height (the natural axes for vertical bars).
        final int MIN_COUNT = 15;
        final int MAX_COUNT_EXTRA = 6;         // total count: 15-20
        final float MIN_SPEED = 0.04f;        // fraction of window height per tick
        final float MAX_SPEED = 0.12f;
        final float MIN_HEIGHT = 0.025f;        // bar height as fraction of window height
        final float MAX_HEIGHT_EXTRA = 0.2f;
        final float MIN_WIDTH = 0.01f;        // bar width as fraction of window width
        final float MAX_WIDTH_EXTRA = 0.01f;
        final float LIFETIME_BASE = 15.0f;     // ticks, for the slowest bar
        final int COLOR_RGB = 0x4488FF;        // soul blue

        int currentTick = clientTick;

        float partialTick = Minecraft.getInstance().getPartialTick();

        Random rand = new Random(stanceSeed);
        int count = rand.nextInt(MAX_COUNT_EXTRA) + MIN_COUNT;

        float elapsed = (currentTick - stanceSpawnTick) + partialTick;

        boolean anyVisible = false;

        for (int i = 0; i < count; i++) {
            // All per-bar properties derived deterministically from the seed.
            // Dimensions/speed are fractions of the window size, converted to pixels here.
            float x = rand.nextFloat() * screenWidth;
            float baseY = rand.nextFloat() * screenHeight * 0.1f; // spawn at random locations at the bottom 10% of the screen
            float height = (rand.nextFloat() * MAX_HEIGHT_EXTRA + MIN_HEIGHT) * screenHeight;
            float width = (rand.nextFloat() * MAX_WIDTH_EXTRA + MIN_WIDTH) * screenWidth;
            float speedFrac = rand.nextFloat() * (MAX_SPEED - MIN_SPEED) + MIN_SPEED;
            float speed = speedFrac * screenHeight;   // pixels per tick

            // Faster bars fade out sooner so they don't outlive their travel.
            // The ratio is scale-independent, so the raw fractions work directly.
            float lifetime = LIFETIME_BASE * (MIN_SPEED / speedFrac);
            float alpha = 1.0f - (elapsed / lifetime);

            if (alpha <= 0.0f) continue;
            anyVisible = true;

            float y = screenHeight - elapsed * speed - baseY;

            int a = (int) (alpha * 180) << 24;   // cap base opacity at ~70%
            int argb = a | COLOR_RGB;

            guiGraphics.fill(
                    (int) x,
                    (int) y,
                    (int) (x + width),
                    (int) (y + height),
                    argb
            );
        }

        // Once all bars have faded, stop rendering.
        if (!anyVisible) {
            stanceSeed = 0;
        }
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
