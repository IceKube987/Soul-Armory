package com.iceKube.soulArmory.soulForging.tasks;

import com.iceKube.soulArmory.Config;
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
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class UnlockSonicOverloadSkill extends ForgingTask {
    public UnlockSonicOverloadSkill() {
        super(new ResourceLocation(SoulArmoryMod.MODID, "unlock_sonic_overload"),
                List.of(
                        new ForgingCriterion("deal_damage_sonic", ForgingEventType.DEAL_DAMAGE,
                                Config.unlockSonicOverloadDamageTarget,
                                Config.unlockSonicOverloadTimeout,
                                null,
                                damageType -> damageType.is(DamageTypes.SONIC_BOOM),
                                null)
                ),
                UnlockSonicOverloadSkill::onComplete,
                SoulSkills.SONIC_OVERLOAD,
                false);
    }

    public static void onComplete(Player player, ItemStack stack, Level level) {
        CompoundTag tag = stack.getTag();
        tag.putString(BaseSoulWeaponItem.CURRENT_FORGING_TASK, BaseSoulWeaponItem.NO_FORGING_TASK);

        ListTag listTag = tag.getList(BaseSoulWeaponItem.AVAILABLE_SKILLS, Tag.TAG_STRING);
        listTag.add(StringTag.valueOf(SoulSkills.SONIC_OVERLOAD.soulSkillId.toString()));
        tag.put(BaseSoulWeaponItem.AVAILABLE_SKILLS, listTag);
    }
}
