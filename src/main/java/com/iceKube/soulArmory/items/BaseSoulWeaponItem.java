package com.iceKube.soulArmory.items;

import com.iceKube.soulArmory.soulSkill.BaseSoulSkill;
import com.iceKube.soulArmory.soulSkill.SoulSkills;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class BaseSoulWeaponItem extends Item {
    public BaseSoulWeaponItem(Properties pProperties) {
        super(pProperties);
    }

    public static final String soulAmountNBT = "soul_armory.soul_weapon.soulAmount";

    public static final String lastHeldGameTimeNBT = "soul_armory.soul_weapon.lastHeldGameTime";

    public static final String lastSoulOverflowTimeNBT = "soul_armory.soul_weapon.lastSoulOverflowTime";

    public boolean doApplySpeedModifier = false;

    // Skill System
    public static final String lastExecutedTime = "soul_armory.soul_bow.last_executed_time";
    public static final String availableSkills = "soul_armory.soul_bow.available_skills";
    public static final String currentSkill = "soul_armory.soul_bow.current_skill";
    public static final String currentSkillIndex = "soul_armory.soul_bow.current_skill_index";

    public abstract int getGracePeriodTicks();

    public abstract int getSoulDecaySpeed();

    public abstract int getMaxSoul();

    public abstract int getPointPerSpeedPercent();

    public abstract int getOverflowSpeed();

    public abstract int getOverflowThreshold();

    public boolean canUseSkill(){
        return this instanceof SoulSkillSystemItem;
    }

    /**
     * Calculates how much soul should be decayed based on how long the weapon hasn't been held.
     * After a grace period of 10 seconds (200 ticks), soul decays at 1 point per 6 ticks.
     * Returns the total accumulated decay (floored to int).
     * <p>
     * Called when:
     * <li>
     * The player picks up the weapon (is holding it in main hand) — to apply deferred decay
     * </li>
     * <li>
     * When appendHoverText is called — to show the effective soul amount
     * </li>
     */
    protected int calculateSoulDecay(ItemStack stack, long currentGameTime) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return 0;
        long lastHeld = tag.getLong(lastHeldGameTimeNBT);
        long ticksDecaying = (currentGameTime - lastHeld) - getGracePeriodTicks(); // 200 ticks = 10 second grace period
        if (ticksDecaying <= 0) return 0;
        return (int) (ticksDecaying / getSoulDecaySpeed()); // 1 soul per 6 ticks
    }

    protected int calculateOverflowDecay(ItemStack stack, long currentGameTime) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return 0;
        long lastDecayed = tag.getLong(lastSoulOverflowTimeNBT);
        long ticksDecaying = (currentGameTime - lastDecayed);
        if (ticksDecaying <= 0) return 0;
        return (int) (ticksDecaying / getOverflowSpeed());
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
            NBT.putLong(lastSoulOverflowTimeNBT, pLevel.getGameTime());
            NBT.putUUID("soul_armory.instanceId", UUID.randomUUID());

            pStack.setTag(NBT);

            if (this instanceof SoulSkillSystemItem soulSkillSystemItem) {
                soulSkillSystemItem.setDefaultSkill(pStack,pLevel);
            }
        }

        if (pEntity instanceof Player player) {
            CompoundTag NBT = pStack.getTag();
            if (player.getMainHandItem() == pStack) {
                // Apply accumulated soul decay (calculated since the weapon was last held)
                NBT.putFloat(soulAmountNBT, Math.max(0, NBT.getFloat(soulAmountNBT) - calculateSoulDecay(pStack, pLevel.getGameTime())));
                // Refresh the last held game time since the weapon is held in player's main hand.
                NBT.putLong(lastHeldGameTimeNBT, pLevel.getGameTime());
            }

            // Handle soul decay beyond the threshold.
            // 2 points of soul is decayed per second by default.
            if (NBT.getFloat(soulAmountNBT) > getOverflowThreshold()) {
                if (pLevel.getGameTime() - NBT.getLong(lastSoulOverflowTimeNBT) >= getOverflowSpeed()) {
                    NBT.putFloat(soulAmountNBT, Math.max(getOverflowThreshold(), NBT.getFloat(soulAmountNBT) - calculateOverflowDecay(pStack, pLevel.getGameTime())));
                    NBT.putLong(lastSoulOverflowTimeNBT, pLevel.getGameTime());
                }
            } else {
                NBT.putLong(lastSoulOverflowTimeNBT, pLevel.getGameTime());
            }
        }

        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    public double getSpeedAdditionPercentage(ItemStack stack) {
        if (!doApplySpeedModifier || stack.getTag() == null) return 0;
        return 0.01 * (int) (stack.getTag().getFloat(soulAmountNBT) / getPointPerSpeedPercent());
    }

    public BaseSoulSkill getCurrentSkill(ItemStack stack) {
        if (!canUseSkill()) return null;
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(currentSkill, Tag.TAG_STRING)) {
            return null;
        }

        String skillIdString = tag.getString(currentSkill);

        ResourceLocation skillId = ResourceLocation.tryParse(skillIdString);

        if (skillId == null) {
            tag.remove(currentSkill);
            tag.putInt(currentSkillIndex, 0);
            List<BaseSoulSkill> available = getAvailableSkills(stack);
            if (!available.isEmpty()) {
                tag.putString(currentSkill, available.get(0).soulSkillId.toString());
            }
            return null;
        }

        return SoulSkills.getSkill(skillId);
    }

    public List<BaseSoulSkill> getAvailableSkills(ItemStack stack) {
        if (!canUseSkill()) return null;
        List<BaseSoulSkill> skills = new ArrayList<>();
        if (!stack.hasTag()) return skills;

        ListTag listTag = stack.getTag().getList(availableSkills, Tag.TAG_STRING);

        for (int i = 0; i < listTag.size(); i++) {
            ResourceLocation id = ResourceLocation.tryParse(listTag.getString(i));
            if (id != null) {
                BaseSoulSkill skill = SoulSkills.getSkill(id);
                if (skill != null) {
                    skills.add(skill);
                } else {
                    // if the skill is somehow missing, remove it from the list.
                    listTag.remove(listTag.getString(i));
                    stack.getTag().put(availableSkills, listTag);
                    i--;
                }
            }else {
                // if the string somehow does not represent a skill, remove it from the list.
                listTag.remove(listTag.getString(i));
                stack.getTag().put(availableSkills, listTag);
                i--;
            }
        }
        return skills;
    }
}
