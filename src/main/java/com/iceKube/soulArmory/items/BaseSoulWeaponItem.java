package com.iceKube.soulArmory.items;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class BaseSoulWeaponItem extends Item {
    public BaseSoulWeaponItem(Properties pProperties) {
        super(pProperties);
    }

    public static final String soulAmountNBT = "soul_armory.soul_weapon.soulAmount";

    public static final String lastHeldGameTimeNBT = "soul_armory.soul_weapon.lastHeldGameTime";

    public boolean doApplySpeedModifier;

    public abstract int getGracePeriodTicks();
    public abstract int getSoulDecaySpeed();
    public abstract int getMaxSoul();
    public abstract int getPointPerSpeedPercent();

    /**
     * Calculates how much soul should be decayed based on how long the sword hasn't been held.
     * After a grace period of 10 seconds (200 ticks), soul decays at 1 point per 6 ticks.
     * Returns the total accumulated decay (floored to int).
     * <p>
     * Called when:
     * <li>
     *         The player picks up the sword (is holding it in main hand) — to apply deferred decay
     * </li>
     * <li>
     *     When appendHoverText is called — to show the effective soul amount
     * </li>
     */
    protected int calculateSoulDecay(ItemStack stack, long currentGameTime) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return 0;
        long lastHeld = tag.getLong(lastHeldGameTimeNBT);
        long ticksDecaying = (currentGameTime - lastHeld) - getGracePeriodTicks(); // 200 ticks = 10 second grace period
        if (ticksDecaying <= 0) return 0;
        return (int) (ticksDecaying / getSoulDecaySpeed()); // 1 soul per 6 ticks (= 10 soul/sec)
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if (pStack.getTag() == null) {
            pTooltipComponents.add(Component.literal(Component.translatable("tooltip.soul_armory.soul").getString() + "0 / " + getMaxSoul()));
            return;
        }

        long currentGameTime = pLevel != null ? pLevel.getGameTime() : 0;
        float currentSoul = pStack.getTag().getFloat(soulAmountNBT);
        int effectiveSoul = ((int) Math.max(0, currentSoul - calculateSoulDecay(pStack, currentGameTime))); // cast it to int to avoid showing decimals in tooltip.
        pTooltipComponents.add(Component.literal(Component.translatable("tooltip.soul_armory.soul").getString() + effectiveSoul + " / " + getMaxSoul()));
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if (pStack.getTag() == null) {
            CompoundTag NBT = new CompoundTag();
            NBT.putFloat(soulAmountNBT, 0);
            NBT.putLong(lastHeldGameTimeNBT, pLevel.getGameTime());
            pStack.setTag(NBT);
        }

        if (pEntity instanceof Player player) {
            CompoundTag NBT = pStack.getTag();
            if (player.getMainHandItem() == pStack) {
                // Apply accumulated soul decay (calculated since the sword was last held)
                NBT.putFloat(soulAmountNBT, Math.max(0, NBT.getFloat(soulAmountNBT) - calculateSoulDecay(pStack, pLevel.getGameTime())));
                // Refresh the last held game time since the sword is held in player's main hand.
                NBT.putLong(lastHeldGameTimeNBT, pLevel.getGameTime());
            }
        }

        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
    }

    public double getSpeedAdditionPercentage(ItemStack stack){
        if (!doApplySpeedModifier || stack.getTag()==null) return 0;
        return 0.01 * (int)(stack.getTag().getFloat(soulAmountNBT) / getPointPerSpeedPercent());
    }
}
