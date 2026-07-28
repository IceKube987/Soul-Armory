package com.iceKube.soulArmory.soulForging;

import com.iceKube.soulArmory.items.BaseSoulWeaponItem;
import com.iceKube.soulArmory.items.SoulChestplateItem;
import com.iceKube.soulArmory.items.UseSoulSkillSystem;
import com.iceKube.soulArmory.registries.ItemRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

import java.util.UUID;

public class TransformHelper {

    // The chest slot is deliberately absent: by the time any of this runs it already holds a Soul
    // Chestplate, so there is never an iron one there to convert.
    private static final EquipmentSlot[] IRON_CONVERTIBLE_SLOTS =
            {EquipmentSlot.HEAD, EquipmentSlot.LEGS, EquipmentSlot.FEET};

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

    /**
     * Turns the worn prototype into a real Soul Chestplate, already in Soul Rage. The order here
     * matters: the soul cap is read off the set the player is wearing, so the swap and the iron
     * conversion both have to land before the pool is filled, or a full set would only be charged
     * to the chestplate's own 300.
     */
    public static void transformChestplate(Player player, ItemStack oldStack, Level level) {
        ItemStack newStack = new ItemStack(ItemRegistry.SOUL_CHESTPLATE.get());

        CompoundTag tag = newStack.getOrCreateTag();
        tag.putLong(SoulChestplateItem.LAST_UPDATE_GAME_TIME, level.getGameTime());
        tag.putBoolean(SoulChestplateItem.RAGE_ACTIVE, true);
        tag.putLong(SoulChestplateItem.LAST_RAGE_UPDATE_GAME_TIME, level.getGameTime());
        tag.putUUID("soul_armory.instanceId", UUID.randomUUID());

        replaceStackInInventory(player, oldStack, newStack);
        convertAllWornIronPieces(player);

        tag.putFloat(SoulChestplateItem.SOUL_AMOUNT, SoulChestplateItem.getMaxSoulForArmor(player));
    }

    /**
     * Converts every worn iron armor piece into its Soul counterpart, discarding durability and
     * enchantments. The chest slot is left alone: forging has already filled it.
     */
    public static void convertAllWornIronPieces(Player player) {
        for (EquipmentSlot slot : IRON_CONVERTIBLE_SLOTS) {
            convertWornIronPiece(player, slot);
        }
    }

    /**
     * The same conversion, but one piece at a time — what a finished chestplate does each time it
     * eats a sonic boom.
     *
     * @return true if a piece was converted
     */
    public static boolean convertOneWornIronPiece(Player player) {
        for (EquipmentSlot slot : IRON_CONVERTIBLE_SLOTS) {
            if (convertWornIronPiece(player, slot)) return true;
        }
        return false;
    }

    private static boolean convertWornIronPiece(Player player, EquipmentSlot slot) {
        RegistryObject<Item> replacement = soulEquivalent(player.getItemBySlot(slot).getItem());
        if (replacement == null) return false;

        player.setItemSlot(slot, new ItemStack(replacement.get()));
        return true;
    }

    private static RegistryObject<Item> soulEquivalent(Item item) {
        if (item == Items.IRON_HELMET) return ItemRegistry.SOUL_HELMET;
        if (item == Items.IRON_LEGGINGS) return ItemRegistry.SOUL_LEGGINGS;
        if (item == Items.IRON_BOOTS) return ItemRegistry.SOUL_BOOTS;
        return null;
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
