package com.iceKube.soulArmory.items;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.iceKube.soulArmory.Config;
import com.iceKube.soulArmory.networking.ModPacketHandler;
import com.iceKube.soulArmory.networking.packets.S2C.SwitchSkillVFXS2CPacket;
import com.iceKube.soulArmory.soulSkill.BaseSoulSkill;
import com.iceKube.soulArmory.soulSkill.SoulSkills;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
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

import java.util.List;

public class SoulSwordItem extends BaseSoulWeaponItem implements SoulSkillSystemItem {

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
    public int getOverflowSpeed() {
        return Config.soulSwordOverflowSpeed;
    }

    @Override
    public int getOverflowThreshold() {
        return Config.soulSwordOverflowThreshold;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (pUsedHand != InteractionHand.MAIN_HAND) return super.use(pLevel, pPlayer, pUsedHand);

        executeSkill(pPlayer.getItemInHand(InteractionHand.MAIN_HAND), pLevel, pPlayer);

        return super.use(pLevel, pPlayer, pUsedHand);
    }

    private float getAttackDamage(ItemStack stack) {
        if (stack.getTag() == null) return (float) (Config.soulSwordBaseDamage - 1);
        return (float) (Config.soulSwordBaseDamage - 1 + (int) (stack.getTag().getFloat(soulAmountNBT) / Config.soulSwordPointsPerDamage));
    }

    @Override
    public void setDefaultSkill(ItemStack stack, Level level) {
        CompoundTag tag = stack.getTag();
        tag.putLong(lastExecutedTime, level.getGameTime());
        tag.putString(currentSkill, SoulSkills.HEAL.soulSkillId.toString());
        tag.putInt(currentSkillIndex, 0);

        ListTag listTag = tag.getList(availableSkills, Tag.TAG_STRING);

        listTag.add(StringTag.valueOf(SoulSkills.HEAL.soulSkillId.toString()));

        // TODO: Blade wave skill is temp.
        listTag.add(StringTag.valueOf(SoulSkills.BLADE_WAVE.soulSkillId.toString()));

        tag.put(availableSkills, listTag);
    }

    public void cycleToNextSkill(ItemStack stack, ServerPlayer player) {
        if (!stack.hasTag()) return;
        CompoundTag tag = stack.getTag();

        List<BaseSoulSkill> skills = getAvailableSkills(stack);
        if (skills == null) return;
        int size = skills.size();

        // Check if there is only one skill
        if (size <= 1) return;

        int currentIndex = tag.getInt(currentSkillIndex);
        // 0 -> 1 -> 2 -> 0 loop if there are 3 skills
        int nextIndex = (currentIndex + 1) % size;
        tag.putInt(currentSkillIndex, nextIndex);

        // actually switch to next skill
        String nextSkillId = skills.get(nextIndex).soulSkillId.toString();
        tag.putString(currentSkill, nextSkillId);

        // Soul Sword only: deduce soul when switching
        tag.putFloat(soulAmountNBT, Math.max(0, tag.getFloat(soulAmountNBT) - Config.soulSwordSwitchSkillCost));

        // Play the switch sound and signal the client to play the switch VFX.
        // TODO: Get my own SFX
        player.level().playSound(null, player.getOnPos(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1, 1);
        ModPacketHandler.sendToPlayer(new SwitchSkillVFXS2CPacket(), player);
    }

    private void executeSkill(ItemStack stack, Level level, Player player) {
        if (!(stack.getItem() instanceof SoulSwordItem)) return;
        BaseSoulSkill skill = getCurrentSkill(stack);
        if (skill == null) return;
        skill.execute(stack, level, player);
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
