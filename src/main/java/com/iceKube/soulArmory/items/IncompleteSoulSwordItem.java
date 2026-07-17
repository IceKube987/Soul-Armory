package com.iceKube.soulArmory.items;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.iceKube.soulArmory.Config;
import com.iceKube.soulArmory.soulForging.ForgingTask;
import com.iceKube.soulArmory.soulForging.ForgingTasks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
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

public class IncompleteSoulSwordItem extends BaseIncompleteSoulItem implements CanApplySpeedBoost {

    public IncompleteSoulSwordItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ForgingTask getActiveForgingTask(ItemStack stack) {
        return ForgingTasks.FORGE_SOUL_SWORD;
    }

    @Override
    public int getMaxActiveTimeTicks() {
        return Config.forgingSwordActiveTicks;
    }

    private float getAttackDamage(ItemStack stack) {
        if (isActive(stack)) {
            return (float) (Config.soulSwordBaseDamage - 1
                    + Config.soulSwordMaxSoul / Config.soulSwordPointsPerDamage);
        }
        return (float) (Config.soulSwordBaseDamage - 1);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot != EquipmentSlot.MAINHAND) return super.getAttributeModifiers(slot, stack);

        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(
                BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2.4f, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                BASE_ATTACK_DAMAGE_UUID, "Weapon Modifier", getAttackDamage(stack), AttributeModifier.Operation.ADDITION));

        return builder.build();
    }

    @Override
    public boolean canAttackBlock(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        return !pPlayer.isCreative();
    }

    @Override
    public float getDestroySpeed(ItemStack pStack, BlockState pState) {
        if (pState.is(Blocks.COBWEB)) return 15.0F;
        return pState.is(BlockTags.SWORD_EFFICIENT) ? 1.5F : 1.0F;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return ToolActions.DEFAULT_SWORD_ACTIONS.contains(toolAction);
    }

    @Override
    public boolean isCorrectToolForDrops(BlockState pBlock) {
        return pBlock.is(Blocks.COBWEB);
    }

    @Override
    public int getPointPerSpeedPercent() {
        return Config.soulSwordPointPerSpeedPercent;
    }

    @Override
    public double getSpeedAdditionPercentage(ItemStack stack) {
        if (!isActive(stack) || stack.getTag() == null) return 0;
        return 0.01 * Config.soulSwordMaxSoul / getPointPerSpeedPercent();
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
        if (isActive(pStack)){
            if (pLevel.getGameTime() % 20 == 0){
                if (pEntity instanceof Player player){
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,100,2,false,false,true));
                }
            }
        }
    }
}
