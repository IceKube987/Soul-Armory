package com.iceKube.soulArmory.soulForging;

import com.iceKube.soulArmory.items.BaseSoulWeaponItem;
import com.iceKube.soulArmory.items.UseSoulSkillSystem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

import java.util.UUID;

public class TransformHelper {

    public static void transformToFullWeapon(Player player, ItemStack oldStack,
                                             RegistryObject<Item> newItemReg,
                                             int maxSoul, Level level) {
        ItemStack newStack = new ItemStack(newItemReg.get());

        CompoundTag tag = newStack.getOrCreateTag();
        tag.putFloat(BaseSoulWeaponItem.SOUL_AMOUNT, maxSoul);
        tag.putLong(BaseSoulWeaponItem.LAST_HELD_GAME_TIME, level.getGameTime());
        tag.putLong(BaseSoulWeaponItem.LAST_SOUL_OVERFLOW_TIME, level.getGameTime());
        tag.putUUID("soul_armory.instanceId", UUID.randomUUID());

        if (newItemReg.get() instanceof UseSoulSkillSystem useSoulSkillSystem) {
            useSoulSkillSystem.setDefaultSkill(newStack, level);
        }

        replaceStackInInventory(player, oldStack, newStack);
    }

    public static void transformChestplate(Player player, ItemStack oldStack, Level level) {
        // TODO: implement when soul armor items are registered
    }

    private static void replaceStackInInventory(Player player, ItemStack oldStack, ItemStack newStack) {
        if (player.getMainHandItem() == oldStack) {
            player.setItemInHand(InteractionHand.MAIN_HAND, newStack);
            return;
        }
        if (player.getOffhandItem() == oldStack) {
            player.setItemInHand(InteractionHand.OFF_HAND, newStack);
            return;
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                if (player.getItemBySlot(slot) == oldStack) {
                    player.setItemSlot(slot, newStack);
                    return;
                }
            }
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i) == oldStack) {
                player.getInventory().setItem(i, newStack);
                return;
            }
        }
    }
}
