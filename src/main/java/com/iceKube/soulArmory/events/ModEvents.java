package com.iceKube.soulArmory.events;

import com.iceKube.soulArmory.Config;
import com.iceKube.soulArmory.SoulArmoryMod;
import com.iceKube.soulArmory.items.BaseSoulWeaponItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = SoulArmoryMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {

    private static final String SOUL_AMOUNT_NBT = "soul_armory.soul_weapon.soulAmount";
    private static final UUID SOUL_SPEED_MODIFIER_UUID = UUID.fromString("00929c63-7970-49d5-bd65-43fae58e3b96");

    // Add soul when player is hurting entity with soul weapons.
    @SubscribeEvent
    public static void onLivingHurt(LivingDamageEvent event) {
        // Check if the source of the damage is a player holding a Soul Weapon
        if (!(event.getSource().getEntity() instanceof Player player)) return;

        ItemStack mainHandItem = player.getMainHandItem();
        if (mainHandItem.getItem() instanceof BaseSoulWeaponItem) {
            // Add soul equal to the damage dealt, capped at maxSoul
            float damageDealt = event.getAmount();
            if (damageDealt <= 0) return;
            damageDealt = Math.min(event.getEntity().getHealth(), damageDealt);

            CompoundTag tag = mainHandItem.getOrCreateTag();
            float currentSoul = tag.getFloat(BaseSoulWeaponItem.soulAmountNBT);
            float newSoul = Math.min(Config.soulSwordMaxSoul, currentSoul + damageDealt);
            tag.putFloat(BaseSoulWeaponItem.soulAmountNBT, newSoul);
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

        if (!(player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof BaseSoulWeaponItem soulWeaponItem))
            return;
        if (!soulWeaponItem.doApplySpeedModifier) return;

        double baseSpeed = attr.getBaseValue();
        double currentSpeed = attr.getValue();
        double targetSpeed = currentSpeed * (1 + soulWeaponItem.getSpeedAdditionPercentage(stack));

        double modifierAmount = Config.speedBoostHasCeil
                ? Math.max(0, Math.min(baseSpeed * Config.speedBoostCeil, targetSpeed))
                : targetSpeed;

        // Subtract current speed because it's an addition modifier.
        modifierAmount -= currentSpeed;

        attr.addTransientModifier(new AttributeModifier(
                SOUL_SPEED_MODIFIER_UUID,
                "Soul Weapon Modifier",
                modifierAmount,
                AttributeModifier.Operation.ADDITION));

    }
}
