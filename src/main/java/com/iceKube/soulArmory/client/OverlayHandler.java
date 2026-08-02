package com.iceKube.soulArmory.client;

import com.iceKube.soulArmory.client.shaders.CoreShaders;
import com.iceKube.soulArmory.items.BaseSoulWeaponItem;
import com.iceKube.soulArmory.items.SoulChestplateItem;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public class OverlayHandler {

    private static final ResourceLocation BAR_TEXTURE = new ResourceLocation("textures/gui/bars.png");

    // Switch-skill VFX state
    // Written only when a skill switch is signaled from the server (see triggerSwitchSkillVFX).
    // The render path reads these every frame and is otherwise read-only.
    private static long stanceSeed;
    private static int stanceSpawnTick;

    // Forging-complete VFX state
    // written only by triggerForgingCompleteVFX.
    private static long forgingSeed;
    private static int forgingSpawnTick;

    // Client-side tick counter, advanced once per client tick (see ModForgeEvents.onClientTick).
    private static int clientTick;

    private static boolean doVignette;

    private static int vignetteUpdateTick;

    private static final int VIGNETTE_FADE_DURATION = 3;

    public static void renderWeaponSoulBar(GuiGraphics gui, int x, int y, int tex_w, int tex_h, int w, int h) {
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
        renderItem(gui, x - 8, y - 4, 0.5F, itemStack);
    }

    public static void renderArmorSoulBar(GuiGraphics gui, int x, int y, int tex_w, int tex_h, int w, int h) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        ItemStack itemStack = mc.player.getItemBySlot(EquipmentSlot.CHEST);
        Item item = itemStack.getItem();
        if (item == null) return;
        if (!(item instanceof SoulChestplateItem soulChestplateItem)) return;
        float soulPoint = SoulChestplateItem.getSoul(itemStack);

        float soulPercentage = soulPoint / SoulChestplateItem.getMaxSoulForArmor(mc.player);

        float soulOverflowPercentage = 0;
        if (SoulChestplateItem.isRaging(itemStack)) {
            soulOverflowPercentage = 0;
        } else {
            soulOverflowPercentage = 1;
        }

        drawSoulBarOutline(gui, x, y, 0, 10, tex_w, tex_h, w, h);
        drawSoulBar(gui, x, y, 0, 15, tex_w * soulPercentage, tex_h, w * soulPercentage, h, soulOverflowPercentage);
        renderItem(gui, x - 8, y - 4, 0.5F, itemStack);
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

    private static void renderItem(GuiGraphics gui, int x, int y, float scale, ItemStack itemStack) {
        PoseStack ps = gui.pose();
        ps.pushPose();

        ps.translate(x, y, 0);
        ps.scale(scale, scale, scale);

        gui.renderItem(itemStack, 0, 0);

        ps.popPose();
    }

    public static void renderVignette(GuiGraphics gui, int w, int h) {
        float alphaScale = getAlphaScaleForVignette();

        if (alphaScale == 0) return;

        var shader = CoreShaders.soulVignette();
        if (shader == null) return;
        shader.safeGetUniform("AlphaScale").set(alphaScale);

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

    private static float getAlphaScaleForVignette() {
        int currentTick = clientTick;
        float partialTick = Minecraft.getInstance().getPartialTick();
        float elapsed = (currentTick - vignetteUpdateTick) + partialTick;

        float alphaScale = 0;

        if (doVignette) {
            alphaScale = elapsed / VIGNETTE_FADE_DURATION;
        } else {
            alphaScale = 1 - elapsed / VIGNETTE_FADE_DURATION;
        }

        // Clamp into [0,1]
        alphaScale = Math.max(0, alphaScale);
        alphaScale = Math.min(1, alphaScale);
        return alphaScale;
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

    public static void onClientTick() {
        clientTick++;
    }

    // This is the ONLY place stanceSeed / stanceSpawnTick are written.
    public static void triggerSwitchSkillVFX() {
        stanceSeed = System.nanoTime();
        stanceSpawnTick = clientTick;
    }

    // This is the ONLY place forgingSeed / forgingSpawnTick are written.
    public static void triggerForgingCompleteVFX() {
        forgingSeed = System.nanoTime();
        forgingSpawnTick = clientTick;
    }

    // Used when a forging task completes.
    public static void renderForgingCompleteVFX(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
        // Skip if no forging has completed yet.
        if (forgingSeed == 0) return;

        // Constants. Sizes and speeds are fractions of the Minecraft window size.
        final int MIN_COUNT = 15;
        final int MAX_COUNT_EXTRA = 5;         // squares per side
        final float MIN_SPEED = 0.015f;         // fraction of window width per tick
        final float MAX_SPEED = 0.020f;
        final float MIN_SIZE = 0.012f;         // square edge as fraction of window width
        final float MAX_SIZE_EXTRA = 0.015f;
        final float SPAWN_BAND = 0.1f;         // spawn within the outer 10% of each side
        final float VERTICAL_SPREAD = 1.0f;    // fraction of window height the squares occupy
        final float LIFETIME_BASE = 12.0f;     // ticks, for the slowest square
        final int ALPHA_CAP = 150;             // cap base opacity, max 255
        final int COLOR_RGB = 0x4488FF;        // soul blue

        int currentTick = clientTick;

        float partialTick = Minecraft.getInstance().getPartialTick();

        Random rand = new Random(forgingSeed);
        int count = rand.nextInt(MAX_COUNT_EXTRA) + MIN_COUNT;

        float elapsed = (currentTick - forgingSpawnTick) + partialTick;

        boolean anyVisible = false;

        for (int i = 0; i < count; i++) {
            // All per-square properties derived deterministically from the seed, and drawn once per
            // pair: the left and right square of a pair are mirror images of each other.
            // Dimensions/speed are fractions of the window size, converted to pixels here.
            float baseInset = (rand.nextFloat() * SPAWN_BAND - 0.1f) * screenWidth;
            float y = screenHeight * 0.5f + (rand.nextFloat() * 2.0f - 1.0f) * VERTICAL_SPREAD * screenHeight * 0.5f;
            float size = (rand.nextFloat() * MAX_SIZE_EXTRA + MIN_SIZE) * screenWidth;
            float speedFrac = rand.nextFloat() * (MAX_SPEED - MIN_SPEED) + MIN_SPEED;
            float speed = speedFrac * screenWidth;   // pixels per tick

            // Faster squares fade out sooner so they don't outlive their travel.
            // The ratio is scale-independent, so the raw fractions work directly.
            float lifetime = LIFETIME_BASE * (MIN_SPEED / speedFrac);
            float alpha = 1.0f - (elapsed / lifetime);

            if (alpha <= 0.0f) continue;
            anyVisible = true;

            int a = (int) (alpha * ALPHA_CAP) << 24;
            int argb = a | COLOR_RGB;

            float travelled = elapsed * speed;

            // Left square flies right, right square flies left.
            float leftX = baseInset + travelled;
            float rightX = screenWidth - baseInset - travelled - size;

            guiGraphics.fill(
                    (int) leftX,
                    (int) y,
                    (int) (leftX + size),
                    (int) (y + size),
                    argb
            );

            guiGraphics.fill(
                    (int) rightX,
                    (int) y,
                    (int) (rightX + size),
                    (int) (y + size),
                    argb
            );
        }

        // Once all squares have faded, stop rendering.
        if (!anyVisible) {
            forgingSeed = 0;
        }
    }

    // Used when soul sword switches skill.
    public static void renderSwitchSkillVFX(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
        // Skip if no switch has happened yet.
        if (stanceSeed == 0) return;

        // Constants. Speed, height and width are fractions of the Minecraft
        // window size.
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

    // Turns the vignette on or off, restarting the fade from the current tick. Polled every client
    // tick, so calls that don't change anything have to be ignored — otherwise the fade would be
    // reset to its first frame every tick and never finish.
    public static void setVignette(boolean on) {
        if (doVignette == on) return;
        doVignette = on;
        vignetteUpdateTick = clientTick;
    }

}
