package com.iceKube.soulArmory.items;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.iceKube.soulArmory.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

public class SoulSwordItem extends BaseSoulWeaponItem {

    public SoulSwordItem(Properties pProperties) {
        super(pProperties);
        doApplySpeedModifier = true;
    }

    @Override
    public int getGracePeriodTicks() {
        return Config.soulSwordGracePeriod;
    }

    @Override
    public int getSoulDecaySpeed() {
        return Config.soulSwordSoulDecaySpeed;
    }

    @Override
    public int getMaxSoul() {
        return Config.soulSwordMaxSoul;
    }

    @Override
    public int getPointPerSpeedPercent() {
        return Config.soulSwordPointPerSpeedPercent;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (pUsedHand != InteractionHand.MAIN_HAND) return super.use(pLevel, pPlayer, pUsedHand);

        heal(pPlayer.getItemInHand(InteractionHand.MAIN_HAND), pPlayer);

        return super.use(pLevel, pPlayer, pUsedHand);
    }


    private float getAttackDamage(ItemStack stack) {
        if (stack.getTag() == null) return 1;
        return (float) (Config.soulSwordBaseDamage - 1 + (int) (stack.getTag().getFloat(soulAmountNBT) / Config.soulSwordPointsPerDamage));
    }

    private void heal(ItemStack stack, Player player) {
        if (stack.getTag() == null) return;

        CompoundTag tag = stack.getTag();
        float currentSoul = tag.getFloat(soulAmountNBT);

        float currentHealth = player.getHealth();
        float maxHealth = player.getMaxHealth();
        float healthMissing = maxHealth - currentHealth;

        if (healthMissing <= 0 || currentSoul <= 0) return;

        // How many HP we can afford to heal with available soul
        int affordableHeal = (int) (currentSoul / Config.soulSwordPointsPerHealing);

        // Actual healing: min of what's needed (rounded up to nearest int) and what we can afford
        int healingAmount = (int) Math.min(Math.ceil(healthMissing), affordableHeal);

        if (healingAmount <= 0) return;

        // Deduct soul cost
        tag.putFloat(soulAmountNBT, currentSoul - (float) (healingAmount * Config.soulSwordPointsPerHealing));

        // Apply healing
        player.heal(healingAmount);
    }

    // generic code from SwordItem

    @Override
    public boolean canAttackBlock(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        return !pPlayer.isCreative();
    }

    @Override
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

    @Override
    public boolean isCorrectToolForDrops(BlockState pBlock) {
        return pBlock.is(Blocks.COBWEB);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2.4f, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon Modifier", getAttackDamage(stack), AttributeModifier.Operation.ADDITION));
//        this.attributeModifiers = builder.build();

        return slot == EquipmentSlot.MAINHAND ? builder.build() : super.getAttributeModifiers(slot, stack);
    }
}
