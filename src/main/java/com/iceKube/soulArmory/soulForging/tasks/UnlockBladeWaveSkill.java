package com.iceKube.soulArmory.soulForging.tasks;

import com.iceKube.soulArmory.Config;
import com.iceKube.soulArmory.SoulArmoryMod;
import com.iceKube.soulArmory.items.BaseSoulWeaponItem;
import com.iceKube.soulArmory.soulForging.ForgingCriterion;
import com.iceKube.soulArmory.soulForging.ForgingEventType;
import com.iceKube.soulArmory.soulForging.ForgingTask;
import com.iceKube.soulArmory.soulSkill.SoulSkills;
import com.iceKube.soulArmory.soulSkill.skills.HealSkill;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class UnlockBladeWaveSkill extends ForgingTask {
    public UnlockBladeWaveSkill() {
        super(new ResourceLocation(SoulArmoryMod.MODID,"unlock_blade_wave"),
                List.of(
                        new ForgingCriterion("deal_damage", ForgingEventType.DEAL_DAMAGE,
                                Config.unlockBladeWaveDamageTarget,
                                Config.unlockBladeWaveDamageTimeout,
                                null,
                                null,
                                null),
                        new ForgingCriterion("heal",ForgingEventType.SKILL,
                                Config.unlockBladeWaveHealTarget,
                                Config.unlockBladeWaveHealTimeout,
                                null,
                                null,
                                skill -> skill instanceof HealSkill),
                        new ForgingCriterion("kill_wardens_in_time", ForgingEventType.KILL_ENTITY,
                                Config.unlockBladeWaveKillTarget,
                                Config.unlockBladeWaveKillTimeout,
                                entityType -> entityType == EntityType.WARDEN,
                                null,
                                null)
                        ),
                UnlockBladeWaveSkill::onComplete,
                SoulSkills.BLADE_WAVE,
                false);
    }

    public static void onComplete(Player player, ItemStack stack, Level level){
        CompoundTag tag = stack.getTag();
        tag.putString(BaseSoulWeaponItem.currentForgingTask, BaseSoulWeaponItem.NO_FORGING_TASK);

        ListTag listTag = tag.getList(BaseSoulWeaponItem.availableSkills, Tag.TAG_STRING);
        listTag.add(StringTag.valueOf(SoulSkills.BLADE_WAVE.soulSkillId.toString()));
        tag.put(BaseSoulWeaponItem.availableSkills, listTag);
    }
}
