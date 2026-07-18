package com.iceKube.soulArmory.items;

import com.iceKube.soulArmory.soulForging.ForgingCriterion;
import com.iceKube.soulArmory.soulForging.ForgingTask;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public abstract class BaseIncompleteSoulItem extends Item implements Forgeable {

    public static final String ACTIVE_TIME_NBT = "soul_armory.forging.activeTime";
    public static final String INSTANCE_ID_NBT = "soul_armory.instanceId";

    public BaseIncompleteSoulItem(Properties pProperties) {
        super(pProperties);
    }

    public abstract int getMaxActiveTimeTicks();

    public void activate(ItemStack stack) {
        stack.getOrCreateTag().putInt(ACTIVE_TIME_NBT, getMaxActiveTimeTicks());
    }

    public boolean isActive(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return false;
        return tag.getInt(ACTIVE_TIME_NBT) > 0;
    }

    protected void onDeactivate(ItemStack stack, Level level, Entity entity) {
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if (pLevel.isClientSide()) return;

        if (pStack.getTag() == null) {
            CompoundTag tag = new CompoundTag();
            tag.putInt(ACTIVE_TIME_NBT, 0);
            tag.putUUID(INSTANCE_ID_NBT, UUID.randomUUID());
            pStack.setTag(tag);
        }

        CompoundTag tag = pStack.getTag();

        int activeTime = tag.getInt(ACTIVE_TIME_NBT);
        if (activeTime > 0) {
            tag.putInt(ACTIVE_TIME_NBT, activeTime - 1);
            if (activeTime - 1 == 0) {
                onDeactivate(pStack, pLevel, pEntity);
            }
        }

        if (pLevel.getGameTime() % 20 == 0) {
            ForgingTask task = getActiveForgingTask(pStack);
            if (task != null) {
                task.checkTimeouts(tag, pLevel.getGameTime());
            }
        }

        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        ForgingTask task = getActiveForgingTask(pStack);
        if (task == null) return;
        if (pStack.getTag() == null) {
            for (ForgingCriterion criterion : task.criteria) {
                String label = Component.translatable("tooltip.soul_armory.forging." + criterion.id).getString();
                pTooltipComponents.add(Component.literal("§9§o" + label + ": " + "0" + " / " + criterion.targetValue));
            }
            return;
        }

        CompoundTag tag = pStack.getTag();
        CompoundTag taskTag = task.getOrCreateTaskTag(tag);

        for (ForgingCriterion criterion : task.criteria) {
            String label = Component.translatable("tooltip.soul_armory.forging." + criterion.id).getString();
            if (criterion.isComplete(taskTag)) {
                pTooltipComponents.add(Component.literal("§b§o" + label + ": " + criterion.targetValue + " / " + criterion.targetValue));
            } else {
                pTooltipComponents.add(Component.literal("§9§o" + label + ": " + criterion.getProgress(taskTag) + " / " + criterion.targetValue));
            }
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }
}
