package com.iceKube.soulArmory.events;

import com.iceKube.soulArmory.Config;
import com.iceKube.soulArmory.SoulArmoryMod;
import com.iceKube.soulArmory.client.OverlayHandler;
import com.iceKube.soulArmory.client.gui.SkillRadialMenuScreen;
import com.iceKube.soulArmory.items.SoulChestplateItem;
import com.iceKube.soulArmory.items.UseSoulSkillSystem;
import com.iceKube.soulArmory.networking.ModPacketHandler;
import com.iceKube.soulArmory.networking.packets.C2S.ActivateSoulRageC2SPacket;
import com.iceKube.soulArmory.networking.packets.C2S.SwitchSkillC2SPacket;
import com.iceKube.soulArmory.soulSkill.BaseSoulSkill;
import com.iceKube.soulArmory.utils.KeyBinding;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

import static com.iceKube.soulArmory.client.OverlayHandler.*;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = SoulArmoryMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientForgeEvents {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (KeyBinding.SWITCH_SKILL.consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            // This event fires whether or not a screen is open, so without the guard, holding the
            // key would re-open the radial menu on top of itself.
            if (mc.screen == null && mc.player != null) {
                ItemStack stack = mc.player.getMainHandItem();

                List<BaseSoulSkill> skills = stack.getItem() instanceof UseSoulSkillSystem skillItem
                        ? skillItem.getAvailableSkills(stack)
                        : null;

                if (skills != null && skills.size() >= 3) {
                    mc.setScreen(new SkillRadialMenuScreen(stack, skills));
                } else {
                    ModPacketHandler.sendToServer(new SwitchSkillC2SPacket());
                }
            }
        }
        if (KeyBinding.ACTIVATE_RAGE.consumeClick()) {
            // The server decides whether the rage actually starts
            ModPacketHandler.sendToServer(new ActivateSoulRageC2SPacket());
        }
        if (KeyBinding.TEST_KEY.consumeClick()) {
            OverlayHandler.triggerForgingCompleteVFX();
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent event) {

        // Only render ONCE. Rendered before HOTBAR and after PORTAL.
        if (event.getOverlay() != VanillaGuiOverlay.PORTAL.type()) {
            return;
        }

        // Draw after rendering vanilla GUIs, but before rendering hotbars (So that my overlays do not block the hotbars)
        GuiGraphics gui = event.getGuiGraphics();
        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();

        int barWidth = 182;
        int barHeight = 5;
        int renderWidth = (int) (screenWidth * 0.2);
        int renderHeight = (int) (screenWidth * 0.01);

        int weaponX = (int) (screenWidth * 0.025);
        int weaponY = (int) (screenHeight * 0.95);
        int armorX = (int) (screenWidth * 0.025);
        int armorY = (int) (screenHeight * 0.91);

        // Call method, vignette first to prevent blocking soul bars
        renderVignette(gui, screenWidth, screenHeight);

        renderWeaponSoulBar(gui, weaponX, weaponY, barWidth, barHeight, renderWidth, renderHeight);

        renderArmorSoulBar(gui, armorX, armorY, barWidth, barHeight, renderWidth, renderHeight);

        renderSkillIcon(gui, (int) (screenWidth * 0.94), (int) (screenHeight - (screenWidth * 0.06)), ((int) (screenWidth * 0.05)), (int) (screenWidth * 0.05));

        renderSwitchSkillVFX(gui, screenWidth, screenHeight);

        renderForgingCompleteVFX(gui, screenWidth, screenHeight);
    }

    // Advance the client-side tick counter used by the switch-skill VFX.
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        OverlayHandler.onClientTick();

        Minecraft mc = Minecraft.getInstance();
        OverlayHandler.setVignette(mc.player != null && SoulChestplateItem.isRaging(mc.player));
    }

    // Drop the values adopted from the server, otherwise they would carry into the next world.
    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        Config.clearSynced();
    }
}
