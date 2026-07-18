package com.iceKube.soulArmory.soulForging.tasks;

import com.iceKube.soulArmory.SoulArmoryMod;
import com.iceKube.soulArmory.items.BaseSoulWeaponItem;
import com.iceKube.soulArmory.soulForging.ForgingCriterion;
import com.iceKube.soulArmory.soulForging.ForgingEventType;
import com.iceKube.soulArmory.soulForging.ForgingTask;
import com.iceKube.soulArmory.soulSkill.SoulSkills;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class UnlockBarrageSkill extends ForgingTask {
    public UnlockBarrageSkill() {
        super(new ResourceLocation(SoulArmoryMod.MODID, "unlock_barrage"),
                List.of(
                        new ForgingCriterion("deal_damage", ForgingEventType.DEAL_DAMAGE,
                                500,
                                0,
                                null,
                                null,
                                null)
                ),
                UnlockBarrageSkill::onComplete,
                SoulSkills.BARRAGE,
                false);
    }

    public static void onComplete(Player player, ItemStack stack, Level level) {
        CompoundTag tag = stack.getTag();
        tag.putString(BaseSoulWeaponItem.CURRENT_FORGING_TASK, BaseSoulWeaponItem.NO_FORGING_TASK);

        ListTag listTag = tag.getList(BaseSoulWeaponItem.AVAILABLE_SKILLS, Tag.TAG_STRING);
        listTag.add(StringTag.valueOf(SoulSkills.BARRAGE.soulSkillId.toString()));
        tag.put(BaseSoulWeaponItem.AVAILABLE_SKILLS, listTag);
    }
}
