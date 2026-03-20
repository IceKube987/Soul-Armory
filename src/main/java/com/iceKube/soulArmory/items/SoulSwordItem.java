package com.iceKube.soulArmory.items;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.iceKube.soulArmory.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SoulSwordItem extends Item {

    private String soulAmountNBT = "soul_armory.soul_sword.soulAmount";

    private String lastHeldGameTimeNBT = "soul_armory.soul_sword.lastHeldGameTime";

    /**
     * Modifiers applied when the item is in the mainhand of a user.
     */
    private Multimap<Attribute, AttributeModifier> attributeModifiers;

    public SoulSwordItem(Properties pProperties) {
        super(pProperties);
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

    /**
     * Calculates how much soul should be decayed based on how long the sword hasn't been held.
     * After a grace period of 10 seconds (200 ticks), soul decays at 1 point per 6 ticks.
     * Returns the total accumulated decay (floored to int).
     * <p>
     * Call it when:
     * 1. The player picks up the sword (is holding it in main hand) — to apply deferred decay
     * 2. When appendHoverText is called — to show the effective soul amount
     */
    private int calculateSoulDecay(ItemStack stack, long currentGameTime) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return 0;
        long lastHeld = tag.getLong(lastHeldGameTimeNBT);
        long ticksDecaying = (currentGameTime - lastHeld) - 200; // 200 ticks = 10 second grace period
        if (ticksDecaying <= 0) return 0;
        return (int) (ticksDecaying / 6); // 1 soul per 6 ticks (= 10 soul/sec)
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if (pStack.getTag() == null) {
//            pTooltipComponents.add(Component.literal("Soul: 0 / " + maxSoul));
            pTooltipComponents.add(Component.literal(Component.translatable("tooltip.soul_armory.soul").getString() + "0 / " + Config.soulSwordMaxSoul));
            return;
        }

        long currentGameTime = pLevel != null ? pLevel.getGameTime() : 0;
        float currentSoul = pStack.getTag().getFloat(soulAmountNBT);
        int effectiveSoul = ((int) Math.max(0, currentSoul - calculateSoulDecay(pStack, currentGameTime))); // cast it to int to avoid showing decimals in tooltip.
        pTooltipComponents.add(Component.literal(Component.translatable("tooltip.soul_armory.soul").getString() + effectiveSoul + " / " + Config.soulSwordMaxSoul));
    }

    private float getAttackDamage(ItemStack stack) {
        if (stack.getTag() == null) return 1;
        return (float) (Config.soulSwordBaseDamage - 1 + (int) (stack.getTag().getFloat(soulAmountNBT) / Config.soulSwordPointsPerDamage));
    }

    // generic code from SwordItem

    public boolean canAttackBlock(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        return !pPlayer.isCreative();
    }

    public float getDestroySpeed(ItemStack pStack, BlockState pState) {
        if (pState.is(Blocks.COBWEB)) {
            return 15.0F;
        } else {
            return pState.is(BlockTags.SWORD_EFFICIENT) ? 1.5F : 1.0F;
        }
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return ToolActions.DEFAULT_SWORD_ACTIONS.contains(toolAction);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    public boolean isCorrectToolForDrops(BlockState pBlock) {
        return pBlock.is(Blocks.COBWEB);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2.4f, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon Modifier", getAttackDamage(stack), AttributeModifier.Operation.ADDITION));
        this.attributeModifiers = builder.build();

        return slot == EquipmentSlot.MAINHAND ? this.attributeModifiers : super.getAttributeModifiers(slot, stack);
    }
}
