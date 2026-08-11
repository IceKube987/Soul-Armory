package com.iceKube.soulArmory.soulForging;

import com.iceKube.soulArmory.advancements.ModCriteriaTriggers;
import com.iceKube.soulArmory.advancements.SoulAction;
import com.iceKube.soulArmory.items.BaseSoulWeaponItem;
import com.iceKube.soulArmory.items.SoulBowItem;
import com.iceKube.soulArmory.networking.ModPacketHandler;
import com.iceKube.soulArmory.networking.packets.S2C.ForgingCompleteVFXS2CPacket;
import com.iceKube.soulArmory.soulSkill.BaseSoulSkill;
import com.iceKube.soulArmory.soulSkill.SoulSkills;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class SkillUnlockHelper {

    public static void unlockSkill(Player player, ItemStack stack, Level level, BaseSoulSkill skill) {
        CompoundTag tag = stack.getTag();
        tag.putString(BaseSoulWeaponItem.CURRENT_FORGING_TASK, BaseSoulWeaponItem.NO_FORGING_TASK);

        ListTag listTag = tag.getList(BaseSoulWeaponItem.AVAILABLE_SKILLS, Tag.TAG_STRING);
        listTag.add(StringTag.valueOf(skill.soulSkillId.toString()));
        tag.put(BaseSoulWeaponItem.AVAILABLE_SKILLS, listTag);

        grantUnlockAdvancements(player, stack, skill);

        if (player instanceof ServerPlayer serverPlayer) {
            ModPacketHandler.sendToPlayer(new ForgingCompleteVFXS2CPacket(), serverPlayer);
        }
    }

    private static void grantUnlockAdvancements(Player player, ItemStack stack, BaseSoulSkill skill) {
        ModCriteriaTriggers.grant(player, SoulAction.SKILL_UNLOCKED);

        if (skill == SoulSkills.BLADE_WAVE) {
            ModCriteriaTriggers.grant(player, SoulAction.BLADE_WAVE_UNLOCKED);
        }

        if (stack.getItem() instanceof SoulBowItem bow) {
            List<BaseSoulSkill> known = bow.getAvailableSkills(stack);
            if (known != null && known.containsAll(SoulBowItem.BOW_SKILLS)) {
                ModCriteriaTriggers.grant(player, SoulAction.BOW_ALL_SKILLS_UNLOCKED);
            }
        }
    }
}
