package com.iceKube.soulArmory.events;

import com.iceKube.soulArmory.Config;
import com.iceKube.soulArmory.SoulArmoryMod;
import com.iceKube.soulArmory.client.OverlayHandler;
import com.iceKube.soulArmory.client.shaders.CoreShaders;
import com.iceKube.soulArmory.items.*;
import com.iceKube.soulArmory.networking.ModPacketHandler;
import com.iceKube.soulArmory.networking.packets.C2S.SwitchSkillC2SPacket;
import com.iceKube.soulArmory.soulForging.ForgingEventType;
import com.iceKube.soulArmory.soulForging.ForgingTask;
import com.iceKube.soulArmory.utils.KeyBinding;
import com.iceKube.soulArmory.utils.ModDamageTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

import static com.iceKube.soulArmory.client.OverlayHandler.*;

@Mod.EventBusSubscriber(modid = SoulArmoryMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModForgeEvents {

    private static final String SOUL_AMOUNT_NBT = "soul_armory.soul_weapon.soulAmount";
    private static final UUID SOUL_SPEED_MODIFIER_UUID = UUID.fromString("00929c63-7970-49d5-bd65-43fae58e3b96"); // Randomly generated UUID

    // When player is hurting entity with soul weapons.
    @SubscribeEvent
    public static void onLivingHurt(LivingDamageEvent event) {
        // Check if the source of the damage is a player holding a Soul Weapon
        if (!(event.getSource().getEntity() instanceof Player player)) return;

        ItemStack mainHandItem = player.getMainHandItem();

        // Handle "Deal Any Damage" forging criterion.
        if (ForgingDealAnyDamage(event, player, mainHandItem)) return;

        // Check if the damage is caused by skills.
        if (event.getSource().is(DamageTypes.SONIC_BOOM) || event.getSource().is(ModDamageTypes.SKILL_ARROW) || event.getSource().is(ModDamageTypes.SKILL_DAMAGE))
            return;

        // Handle add soul points for regular soul weapons.
        if (AddSoulPoints(event, mainHandItem)) return;

        // Handle "deal damage" forging criterion, which will only calculate damage that is not from skills.
        ForgingDealDamage(event, player, mainHandItem);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;

        ItemStack mainHandItem = player.getMainHandItem();
        if (!(mainHandItem.getItem() instanceof Forgeable forgeable)) return;

        ForgingTask task = forgeable.getActiveForgingTask(mainHandItem);
        if (task == null) return;

        CompoundTag tag = mainHandItem.getOrCreateTag();
        EntityType<?> killedType = event.getEntity().getType();

        boolean completed = task.processEvent(tag, ForgingEventType.KILL_ENTITY,
                killedType, 1, player.level().getGameTime());

        if (completed) {
            task.onComplete.execute(player, mainHandItem, player.level());
            task.removeTaskTag(tag);
        }
    }

    // Apply speed modifier if the player is holding a soul weapon that applies speed modifier.
    @SubscribeEvent
    public static void onPlayerTickApplySpeedModifier(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        Player player = event.player;
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        AttributeInstance attr = player.getAttributes().getInstance(Attributes.MOVEMENT_SPEED);

        if (attr == null) return;
        if (attr.getModifier(SOUL_SPEED_MODIFIER_UUID) != null) {
            attr.removeModifier(SOUL_SPEED_MODIFIER_UUID);
        }

        if (!(player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof CanApplySpeedBoost item))
            return;

        double baseSpeed = attr.getBaseValue();
        double currentSpeed = attr.getValue();
        double targetSpeed = currentSpeed * (1 + item.getSpeedAdditionPercentage(stack));

        double modifierAmount = Config.speedBoostHasCeil
                ? Math.max(0, Math.min(baseSpeed * Config.speedBoostCeil, targetSpeed))
                : targetSpeed;

        // Subtract current speed because it's an addition modifier.
        modifierAmount -= currentSpeed;

        // otherwise the speed will be negative if the holder is applied too much speed boost effect.
        modifierAmount = Math.max(0, modifierAmount);

        attr.addTransientModifier(new AttributeModifier(
                SOUL_SPEED_MODIFIER_UUID,
                "Soul Speed Modifier",
                modifierAmount,
                AttributeModifier.Operation.ADDITION));

    }

    @SubscribeEvent
    public static void onPlayerRightClick(PlayerInteractEvent event) {
        if (!Config.soulSwordDisableShieldUsage) return;
        if (event.getHand() != InteractionHand.OFF_HAND) return;

        Player player = event.getEntity();
        ItemStack mainHandItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack offHandItem = player.getItemInHand(InteractionHand.OFF_HAND);

        if (!(mainHandItem.getItem() instanceof SoulSwordItem)) return;
        if (!(offHandItem.getItem() instanceof ShieldItem)) return;

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (KeyBinding.TEST_KEY.consumeClick()) {
            var shader = CoreShaders.soulVignette();
            if (shader == null) return;
            Minecraft mc = Minecraft.getInstance();
            shader.safeGetUniform("FadeinTime").set(mc.level.getDayTime());
            int i = shader.getUniform("Started").getIntBuffer().get();
            if (i == 0) {
                shader.getUniform("Started").set(1);
            } else {
                shader.getUniform("Started").set(0);
            }
        }
        if (KeyBinding.SWITCH_SKILL.consumeClick()) {
            ModPacketHandler.sendToServer(new SwitchSkillC2SPacket());
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {

        // Only render ONCE.
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) {
            return;
        }

        // Draw after rendering all vanilla GUIs
        GuiGraphics gui = event.getGuiGraphics();
        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();

        int barWidth = 182;
        int barHeight = 5;
        int renderWidth = (int) (screenWidth * 0.2);
        int renderHeight = (int) (screenWidth * 0.01);
        int x = (int) (screenWidth * 0.025);
        int y = screenHeight - 15;

        // Call method
        renderSoulBar(gui, x, y, barWidth, barHeight, renderWidth, renderHeight);

//        renderVignette(gui, screenWidth, screenHeight);

        renderSkillIcon(gui, (int) (screenWidth * 0.94), (int) (screenHeight - (screenWidth * 0.06)), ((int) (screenWidth * 0.05)), (int) (screenWidth * 0.05));

        renderSwitchSkillVFX(gui, screenWidth, screenHeight);
    }

    // Advance the client-side tick counter used by the switch-skill VFX.
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        OverlayHandler.onClientTick();
    }

    private static boolean ForgingDealAnyDamage(LivingDamageEvent event, Player player, ItemStack mainHandItem) {
        if (mainHandItem.getItem() instanceof Forgeable forgeable){
            ForgingTask task = forgeable.getActiveForgingTask(mainHandItem);
            if (task == null) return true;

            CompoundTag tag = mainHandItem.getOrCreateTag();
            EntityType<?> targetType = event.getEntity().getType();
            float damage = Math.min(event.getAmount(), event.getEntity().getHealth());
            if (damage <= 0) return true;

            boolean completed = task.processEvent(tag, ForgingEventType.DEAL_ANY_DAMAGE,
                    targetType, damage, player.level().getGameTime());

            if (completed) {
                task.onComplete.execute(player, mainHandItem, player.level());
                task.removeTaskTag(tag);
            }
        }
        return false;
    }

    private static void ForgingDealDamage(LivingDamageEvent event, Player player, ItemStack mainHandItem) {
        if (mainHandItem.getItem() instanceof Forgeable forgeable) {
            ForgingTask task = forgeable.getActiveForgingTask(mainHandItem);
            if (task == null) return;

            CompoundTag tag = mainHandItem.getOrCreateTag();
            EntityType<?> targetType = event.getEntity().getType();
            float damage = Math.min(event.getAmount(), event.getEntity().getHealth());
            if (damage <= 0) return;

            if (targetType == EntityType.WARDEN && mainHandItem.getItem() instanceof BaseIncompleteSoulItem incompleteItem) {
                incompleteItem.activate(mainHandItem);
            }

            boolean completed = task.processEvent(tag, ForgingEventType.DEAL_DAMAGE,
                    targetType, damage, player.level().getGameTime());

            if (completed) {
                task.onComplete.execute(player, mainHandItem, player.level());
                task.removeTaskTag(tag);
            }
        }
    }

    private static boolean AddSoulPoints(LivingDamageEvent event, ItemStack mainHandItem) {
        if (mainHandItem.getItem() instanceof BaseSoulWeaponItem item) {
            // Add soul equal to the damage dealt, capped at maxSoul
            float damageDealt = event.getAmount();
            if (damageDealt <= 0) return true;
            damageDealt = Math.min(event.getEntity().getHealth(), damageDealt);

            CompoundTag tag = mainHandItem.getOrCreateTag();
            float currentSoul = tag.getFloat(BaseSoulWeaponItem.soulAmountNBT);
            float newSoul = Math.min(item.getMaxSoul(), currentSoul + damageDealt);
            tag.putFloat(BaseSoulWeaponItem.soulAmountNBT, newSoul);
        }
        return false;
    }
}
