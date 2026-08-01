package com.iceKube.soulArmory.items;

import com.iceKube.soulArmory.soulForging.ForgingCriterion;
import com.iceKube.soulArmory.soulForging.ForgingTask;
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
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class BaseSoulWeaponItem extends Item {
    public static final String SOUL_AMOUNT = "soul_armory.soul_weapon.soulAmount";
    public static final String LAST_HELD_GAME_TIME = "soul_armory.soul_weapon.lastHeldGameTime";
    public static final String LAST_SOUL_OVERFLOW_TIME = "soul_armory.soul_weapon.lastSoulOverflowTime";
    // Skill System
    public static final String LAST_EXECUTED_TIME = "soul_armory.soul_weapon.last_executed_time";
    public static final String AVAILABLE_SKILLS = "soul_armory.soul_weapon.available_skills";
    public static final String CURRENT_SKILL = "soul_armory.soul_weapon.current_skill";
    public static final String CURRENT_SKILL_INDEX = "soul_armory.soul_weapon.current_skill_index";
    // Forging system
    public static final String CURRENT_FORGING_TASK = "soul_armory.soul_weapon.current_forging_task";
    public static final String NO_FORGING_TASK = "none";

    public BaseSoulWeaponItem(Properties pProperties) {
        super(pProperties);
    }

    // No durability, not enchantable.
    @Override
    public int getDamage(ItemStack stack) {
        return 0;
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

    public abstract int getGracePeriodTicks();

    public abstract int getSoulDecaySpeed();

    public abstract int getMaxSoul();

    public abstract int getOverflowSpeed();

    public abstract int getOverflowThreshold();

    public boolean canUseSkill() {
        return this instanceof UseSoulSkillSystem;
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
    public int calculateSoulDecay(ItemStack stack, long currentGameTime) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return 0;
        long lastHeld = tag.getLong(LAST_HELD_GAME_TIME);
        long ticksDecaying = (currentGameTime - lastHeld) - getGracePeriodTicks();
        if (ticksDecaying <= 0) return 0;
        return (int) (ticksDecaying / getSoulDecaySpeed());
    }

    protected int calculateOverflowDecay(ItemStack stack, long currentGameTime) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return 0;
        long lastDecayed = tag.getLong(LAST_SOUL_OVERFLOW_TIME);
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
        float currentSoul = pStack.getTag().getFloat(SOUL_AMOUNT);
        int effectiveSoul = ((int) Math.max(0, currentSoul - calculateSoulDecay(pStack, currentGameTime))); // cast to avoid showing decimals in tooltip
        pTooltipComponents.add(Component.literal(Component.translatable("tooltip.soul_armory.soul").getString() + effectiveSoul + " / " + getMaxSoul()));

        // Show forging progress
        if (this instanceof Forgeable forgeable) {
            ForgingTask task = forgeable.getActiveForgingTask(pStack);
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
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if (pStack.getTag() == null) {
            CompoundTag NBT = new CompoundTag();
            NBT.putFloat(SOUL_AMOUNT, 0);
            NBT.putLong(LAST_HELD_GAME_TIME, pLevel.getGameTime());
            NBT.putLong(LAST_SOUL_OVERFLOW_TIME, pLevel.getGameTime());
            NBT.putUUID("soul_armory.instanceId", UUID.randomUUID());

            if (this instanceof Forgeable) {
                NBT.putString(CURRENT_FORGING_TASK, NO_FORGING_TASK);
            }

            pStack.setTag(NBT);

            if (this instanceof UseSoulSkillSystem soulSkillSystemItem) {
                soulSkillSystemItem.setDefaultSkill(pStack, pLevel);
            }
        }

        if (pEntity instanceof Player player) {
            CompoundTag NBT = pStack.getTag();
            if (player.getMainHandItem() == pStack) {
                // Apply accumulated soul decay (calculated since the weapon was last held)
                NBT.putFloat(SOUL_AMOUNT, Math.max(0, NBT.getFloat(SOUL_AMOUNT) - calculateSoulDecay(pStack, pLevel.getGameTime())));
                // Refresh the last held game time since the weapon is held in player's main hand.
                NBT.putLong(LAST_HELD_GAME_TIME, pLevel.getGameTime());
            }

            // Handle soul decay beyond the threshold.
            // 2 points of soul is decayed per second by default.
            if (NBT.getFloat(SOUL_AMOUNT) > getOverflowThreshold()) {
                if (pLevel.getGameTime() - NBT.getLong(LAST_SOUL_OVERFLOW_TIME) >= getOverflowSpeed()) {
                    NBT.putFloat(SOUL_AMOUNT, Math.max(getOverflowThreshold(), NBT.getFloat(SOUL_AMOUNT) - calculateOverflowDecay(pStack, pLevel.getGameTime())));
                    NBT.putLong(LAST_SOUL_OVERFLOW_TIME, pLevel.getGameTime());
                }
            } else {
                NBT.putLong(LAST_SOUL_OVERFLOW_TIME, pLevel.getGameTime());
            }

            // test for forging tasks
            if (pLevel.getGameTime() % 20 == 0) {
                if (pStack.getItem() instanceof Forgeable forgeable) {
                    ForgingTask task = forgeable.getActiveForgingTask(pStack);
                    if (task != null) {
                        task.checkTimeouts(NBT, pLevel.getGameTime());
                    }
                }
            }

        }

        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    public BaseSoulSkill getCurrentSkill(ItemStack stack) {
        if (!canUseSkill()) return null;
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(CURRENT_SKILL, Tag.TAG_STRING)) {
            return null;
        }

        String skillIdString = tag.getString(CURRENT_SKILL);

        ResourceLocation skillId = ResourceLocation.tryParse(skillIdString);

        if (skillId == null) {
            tag.remove(CURRENT_SKILL);
            tag.putInt(CURRENT_SKILL_INDEX, 0);
            List<BaseSoulSkill> available = getAvailableSkills(stack);
            if (available == null) return null;
            if (!available.isEmpty()) {
                tag.putString(CURRENT_SKILL, available.get(0).soulSkillId.toString());
            }
            return null;
        }

        return SoulSkills.getSkill(skillId);
    }

    public List<BaseSoulSkill> getAvailableSkills(ItemStack stack) {
        if (!canUseSkill()) return null;
        List<BaseSoulSkill> skills = new ArrayList<>();
        if (!stack.hasTag()) return skills;

        ListTag listTag = stack.getTag().getList(AVAILABLE_SKILLS, Tag.TAG_STRING);

        for (int i = 0; i < listTag.size(); i++) {
            ResourceLocation id = ResourceLocation.tryParse(listTag.getString(i));
            if (id != null) {
                BaseSoulSkill skill = SoulSkills.getSkill(id);
                if (skill != null) {
                    skills.add(skill);
                } else {
                    // if the skill is somehow missing, remove it from the list.
                    listTag.remove(listTag.getString(i));
                    stack.getTag().put(AVAILABLE_SKILLS, listTag);
                    i--;
                }
            } else {
                // if the string somehow does not represent a skill, remove it from the list.
                listTag.remove(listTag.getString(i));
                stack.getTag().put(AVAILABLE_SKILLS, listTag);
                i--;
            }
        }
        return skills;
    }

    /**
     * Points the stack at the skill sitting at {@code index} in {@link #getAvailableSkills}.
     * <p>
     * The index arrives from the client (the radial menu sends whichever sector was hovered), so it
     * is re-checked here against the server's own copy of the skill list rather than trusted.
     * Writes nothing and returns false when the index doesn't name a skill the stack actually has.
     *
     * @return whether the skill was changed
     */
    protected boolean applySkillIndex(ItemStack stack, int index) {
        if (!stack.hasTag()) return false;

        List<BaseSoulSkill> skills = getAvailableSkills(stack);
        if (skills == null || index < 0 || index >= skills.size()) return false;

        CompoundTag tag = stack.getTag();
        if (tag.getInt(CURRENT_SKILL_INDEX) == index) return false;

        tag.putInt(CURRENT_SKILL_INDEX, index);
        tag.putString(CURRENT_SKILL, skills.get(index).soulSkillId.toString());
        return true;
    }
}
