package com.iceKube.soulArmory.events;

import com.iceKube.soulArmory.Config;
import com.iceKube.soulArmory.SoulArmoryMod;
import com.iceKube.soulArmory.items.SoulSwordItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SoulArmoryMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {

    private static final String SOUL_AMOUNT_NBT = "soul_armory.soul_sword.soulAmount";

    @SubscribeEvent
    public static void onLivingHurt(LivingDamageEvent event) {
        // Check if the source of the damage is a player holding a SoulSwordItem
        if (!(event.getSource().getEntity() instanceof Player player)) return;

        ItemStack mainHandItem = player.getMainHandItem();
        if (!(mainHandItem.getItem() instanceof SoulSwordItem)) return;

        // Add soul equal to the damage dealt (floored to int), capped at maxSoul
        int damageDealt = (int) event.getAmount();
        if (damageDealt <= 0) return;
        damageDealt = Math.round(Math.min(event.getEntity().getHealth(), damageDealt));

        CompoundTag tag = mainHandItem.getOrCreateTag();
        int currentSoul = tag.getInt(SOUL_AMOUNT_NBT);
        int newSoul = Math.min(Config.soulSwordMaxSoul, currentSoul + damageDealt);
        tag.putInt(SOUL_AMOUNT_NBT, newSoul);
    }
}
