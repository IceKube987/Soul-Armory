package com.iceKube.soulArmory.client.gui;

import com.iceKube.soulArmory.ClientConfig;
import com.iceKube.soulArmory.items.BaseSoulWeaponItem;
import com.iceKube.soulArmory.items.UseSoulSkillSystem;
import com.iceKube.soulArmory.networking.ModPacketHandler;
import com.iceKube.soulArmory.networking.packets.C2S.SelectSkillC2SPacket;
import com.iceKube.soulArmory.soulSkill.BaseSoulSkill;
import com.iceKube.soulArmory.utils.KeyBinding;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

import java.util.List;

/**
 * The hold-to-open skill picker. Opened from {@code ClientForgeEvents.onKeyInput} while the player
 * holds {@link KeyBinding#SWITCH_SKILL} with a {@link UseSoulSkillSystem} item carrying at least
 * three skills; releasing that same key commits whichever wedge the cursor is over.
 * <p>
 * Being a real {@link Screen} is what makes the mouse usable here: Minecraft ungrabs the cursor for
 * us, so moving it picks a wedge instead of turning the camera. The world keeps ticking — see
 * {@link #isPauseScreen()} — so opening the menu mid-fight isn't a free pause.
 */
public class SkillRadialMenuScreen extends Screen {

    private static final double TAU = Math.PI * 2;

    // Subdivisions per wedge
    private static final int ARC_STEPS = 24;

    /**
     * Half the thickness of the line dividing two wedges, as a fraction of the menu radius.
     * <p>
     * A fraction of the radius rather than of the sector angle, so every divider is the same
     * thickness end to end instead of splaying outwards — and so the thickness doesn't change when
     * a skill is unlocked and the wedges get narrower.
     */
    private static final float SEPARATOR_HALF_WIDTH = 0.006f;

    // color settings
    private static final int COLOR_IDLE = 0x60101820;
    private static final int COLOR_CURRENT = 0xA0142A55;
    private static final int COLOR_HOVERED = 0xA04488FF;   // the soul blue used by the switch VFX
    private static final int COLOR_SEPARATOR = 0xC0080C12;

    /** Icon box side, as a fraction of the menu radius. */
    private static final float ICON_SCALE = 0.28f;

    /** Where the labels sit, as a multiple of the menu radius. */
    private static final float LABEL_RADIUS_SCALE = 1.12f;

    private final List<BaseSoulSkill> skills;
    private final int currentIndex;

    // The key that opened the menu, captured at open time so a rebound SWITCH_SKILL still closes it.
    private final int openKeyCode;
    private final boolean openKeyIsMouse;

    private int hovered = -1;

    public SkillRadialMenuScreen(ItemStack stack, List<BaseSoulSkill> skills) {
        super(Component.translatable("gui.soul_armory.skill_radial_menu"));
        this.skills = skills;
        this.currentIndex = stack.hasTag() ? stack.getTag().getInt(BaseSoulWeaponItem.CURRENT_SKILL_INDEX) : -1;

        InputConstants.Key key = KeyBinding.SWITCH_SKILL.getKey();
        this.openKeyCode = key.getValue();
        this.openKeyIsMouse = key.getType() == InputConstants.Type.MOUSE;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        // Deliberately no renderBackground() — the vanilla blur/dim is far too heavy to drop over a
        // fight for the fraction of a second this menu is up.
        if (ClientConfig.radialMenuRenderBackground) {
            gui.fill(0, 0, width, height, 0x40000000);
            // Force the scrim out before the raw-Tesselator wedges, which draw immediately and would
            // otherwise end up underneath GuiGraphics' batched geometry.
            gui.flush();
        }

        // Both axes matter: sizing off the smaller one is what keeps the whole ring on screen no
        // matter how the window is shaped.
        float radius = Math.min(width, height) * (float) ClientConfig.radialMenuDiameterRatio / 2f;
        float innerRadius = radius * (float) ClientConfig.radialMenuDeadZoneRatio;
        float cx = width / 2f;
        float cy = height / 2f;

        hovered = sectorAt(mouseX, mouseY, cx, cy, radius);

        drawSectors(gui, cx, cy, innerRadius, radius);
        drawIcons(gui, cx, cy, innerRadius, radius);
        if (ClientConfig.radialMenuRenderLabels) {
            drawLabels(gui, cx, cy, radius);
        }
    }

    /**
     * Which wedge the cursor is over, or -1 for none.
     * <p>
     * There is no outer bound on purpose — flicking well past the ring still selects, which is what
     * a radial menu is expected to do. Only the central dead zone rejects.
     */
    private int sectorAt(double mouseX, double mouseY, float cx, float cy, float radius) {
        double dx = mouseX - cx;
        double dy = mouseY - cy;

        if (Math.sqrt(dx * dx + dy * dy) < radius * ClientConfig.radialMenuDeadZoneRatio) return -1;

        // atan2 is 0 at +X and grows clockwise on screen (Y points down). Shifting by a quarter turn
        // puts 0 straight up, and by half a sector centres sector 0 on "up" rather than starting there.
        int n = skills.size();
        double angle = Math.atan2(dy, dx) + Math.PI / 2 + Math.PI / n;
        angle = ((angle % TAU) + TAU) % TAU;

        return (int) (angle / TAU * n);
    }

    private void drawSectors(GuiGraphics gui, float cx, float cy, float innerRadius, float outerRadius) {
        int n = skills.size();
        double sectorAngle = TAU / n;

        Matrix4f matrix = gui.pose().last().pose();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        // The wedges are drawn edge to edge; the dividing lines go on top afterwards.
        for (int i = 0; i < n; i++) {
            int color = i == hovered ? COLOR_HOVERED : (i == currentIndex ? COLOR_CURRENT : COLOR_IDLE);

            double start = midAngle(i) - sectorAngle / 2;
            double step = sectorAngle / ARC_STEPS;

            for (int s = 0; s < ARC_STEPS; s++) {
                double a0 = start + step * s;
                double a1 = a0 + step;

                float cos0 = (float) Math.cos(a0), sin0 = (float) Math.sin(a0);
                float cos1 = (float) Math.cos(a1), sin1 = (float) Math.sin(a1);

                quad(builder, matrix,
                        cx + cos0 * innerRadius, cy + sin0 * innerRadius,
                        cx + cos1 * innerRadius, cy + sin1 * innerRadius,
                        cx + cos1 * outerRadius, cy + sin1 * outerRadius,
                        cx + cos0 * outerRadius, cy + sin0 * outerRadius,
                        color);
            }
        }

        // One divider per boundary, laid along the boundary angle and given its thickness
        // perpendicular to it — constant in pixels from the inner edge to the outer.
        float halfWidth = outerRadius * SEPARATOR_HALF_WIDTH;

        for (int i = 0; i < n; i++) {
            double angle = midAngle(i) - sectorAngle / 2;
            float cos = (float) Math.cos(angle), sin = (float) Math.sin(angle);

            // Perpendicular to the boundary direction, scaled to half the line's thickness.
            float px = -sin * halfWidth;
            float py = cos * halfWidth;

            float ix = cx + cos * innerRadius, iy = cy + sin * innerRadius;
            float ox = cx + cos * outerRadius, oy = cy + sin * outerRadius;

            quad(builder, matrix,
                    ix - px, iy - py,
                    ix + px, iy + py,
                    ox + px, oy + py,
                    ox - px, oy - py,
                    COLOR_SEPARATOR);
        }

        Tesselator.getInstance().end();

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    /** Emits one ARGB-coloured quad from four already-positioned corners, wound in order. */
    private static void quad(BufferBuilder builder, Matrix4f matrix,
                             float x0, float y0, float x1, float y1,
                             float x2, float y2, float x3, float y3,
                             int color) {
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        builder.vertex(matrix, x0, y0, 0).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x1, y1, 0).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x2, y2, 0).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x3, y3, 0).color(r, g, b, a).endVertex();
    }

    private void drawIcons(GuiGraphics gui, float cx, float cy, float innerRadius, float outerRadius) {
        float iconSide = outerRadius * ICON_SCALE;
        float iconRadius = (innerRadius + outerRadius) / 2f;

        for (int i = 0; i < skills.size(); i++) {
            ResourceLocation texture = skills.get(i).soulSkillTexture;
            // Same guards as OverlayHandler.renderSkillIcon: several skills borrow vanilla textures,
            // and a missing one must not take the whole menu down.
            if (texture == null) continue;
            if (minecraft == null || !minecraft.getResourceManager().getResource(texture).isPresent()) continue;

            double angle = midAngle(i);
            int x = (int) (cx + Math.cos(angle) * iconRadius - iconSide / 2);
            int y = (int) (cy + Math.sin(angle) * iconRadius - iconSide / 2);

            gui.blit(texture, x, y, (int) iconSide, (int) iconSide, 0, 0, 32, 32, 32, 32);
        }
    }

    private void drawLabels(GuiGraphics gui, float cx, float cy, float outerRadius) {
        float labelRadius = outerRadius * LABEL_RADIUS_SCALE;

        for (int i = 0; i < skills.size(); i++) {
            double angle = midAngle(i);
            int x = (int) (cx + Math.cos(angle) * labelRadius);
            int y = (int) (cy + Math.sin(angle) * labelRadius - font.lineHeight / 2f);

            // Always white: the highlighted wedge already says which skill is under the cursor, and
            // dimming the rest would only make them harder to read while choosing.
            gui.drawCenteredString(font, Component.translatable(skills.get(i).getTranslationKey()), x, y, 0xFFFFFFFF);
        }
    }

    /** The angle, in screen space, at which sector {@code i} is centred. Sector 0 points straight up. */
    private double midAngle(int i) {
        return -Math.PI / 2 + TAU / skills.size() * i;
    }

    // Both release handlers report the event as unconsumed on purpose. By the time they return the
    // screen has already closed, so vanilla goes on to clear the KeyMapping's held state — swallow
    // the release and SWITCH_SKILL would be left stuck "down" for the rest of the session.

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (!openKeyIsMouse && keyCode == openKeyCode) {
            commitAndClose();
            return false;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (openKeyIsMouse && button == openKeyCode) {
            commitAndClose();
            return false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void tick() {
        // Failsafe: if the item goes away underneath us — death, a swapped hotbar slot, a dropped
        // weapon — bail out rather than leaving the player stuck behind a menu they can't commit.
        if (minecraft == null || minecraft.player == null) {
            onClose();
            return;
        }

        ItemStack stack = minecraft.player.getMainHandItem();
        if (!(stack.getItem() instanceof UseSoulSkillSystem skillItem)) {
            onClose();
            return;
        }

        List<BaseSoulSkill> available = skillItem.getAvailableSkills(stack);
        if (available == null || available.size() != skills.size()) {
            onClose();
        }
    }

    /**
     * Applies the pick, if there is one. A release inside the dead zone, or back on the skill the
     * player started with, is a deliberate no-op — no packet, no sound, no cost.
     */
    private void commitAndClose() {
        if (hovered >= 0 && hovered != currentIndex) {
            ModPacketHandler.sendToServer(new SelectSkillC2SPacket(hovered));
        }
        onClose();
    }
}
