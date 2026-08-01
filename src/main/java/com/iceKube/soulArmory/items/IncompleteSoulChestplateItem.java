package com.iceKube.soulArmory.items;

import com.iceKube.soulArmory.Config;
import com.iceKube.soulArmory.soulForging.ForgingTask;
import com.iceKube.soulArmory.soulForging.ForgingTasks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class IncompleteSoulChestplateItem extends ArmorItem implements Forgeable {

    public IncompleteSoulChestplateItem(Properties pProperties) {
        super(ArmorMaterials.IRON, Type.CHESTPLATE, pProperties);
    }

    @Override
    public ForgingTask getActiveForgingTask(ItemStack stack) {
        return ForgingTasks.FORGE_SOUL_CHESTPLATE;
    }

    public int getMaxActiveTimeTicks() {
        return Config.forgingChestplateActiveTicks;
    }

    /** Starts the short Soul Rage burst the prototype grants for eating a sonic boom. */
    public void activate(ItemStack stack) {
        BaseIncompleteSoulItem.setActiveTime(stack, getMaxActiveTimeTicks());
    }

    public boolean isActive(ItemStack stack) {
        return BaseIncompleteSoulItem.isActive(stack);
    }

    // Does not break
    @Override
    public int getDamage(ItemStack stack) {
        return 0;
    }

    // ArmorItem's constructor forces a durability onto the properties, so the getDamage override
    // above is not enough on its own.
    @Override
    public boolean canBeDepleted() {
        return false;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return false;
    }

    @Override
    public boolean isEnchantable(ItemStack pStack) {
        return false;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    // The Forge overload rather than Item#inventoryTick: that one does not fire for a stack sitting
    // in an armor slot, which is the only place this item does anything.
    @Override
    public void onInventoryTick(ItemStack stack, Level level, Player player, int slotIndex, int selectedIndex) {
        if (level.isClientSide()) {
            super.onInventoryTick(stack, level, player, slotIndex, selectedIndex);
            return;
        }

        if (stack.getTag() == null) {
            CompoundTag NBT = new CompoundTag();
            NBT.putInt(BaseIncompleteSoulItem.ACTIVE_TIME_NBT, 0);
            NBT.putUUID(BaseIncompleteSoulItem.INSTANCE_ID_NBT, UUID.randomUUID());
            stack.setTag(NBT);
        }

        CompoundTag NBT = stack.getTag();

        int activeTime = NBT.getInt(BaseIncompleteSoulItem.ACTIVE_TIME_NBT);
        if (activeTime > 0) {
            NBT.putInt(BaseIncompleteSoulItem.ACTIVE_TIME_NBT, activeTime - 1);
        }

        if (level.getGameTime() % 20 == 0) {
            getActiveForgingTask(stack).checkTimeouts(NBT, level.getGameTime());
        }

        super.onInventoryTick(stack, level, player, slotIndex, selectedIndex);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        BaseIncompleteSoulItem.appendForgingTooltip(pStack, getActiveForgingTask(pStack), pTooltipComponents);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }
}
