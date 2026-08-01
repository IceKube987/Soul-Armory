package com.iceKube.soulArmory.soulForging;

import com.iceKube.soulArmory.items.BaseSoulWeaponItem;
import com.iceKube.soulArmory.networking.ModPacketHandler;
import com.iceKube.soulArmory.networking.packets.S2C.ForgingCompleteVFXS2CPacket;
import com.iceKube.soulArmory.soulSkill.BaseSoulSkill;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SkillUnlockHelper {

    public static void unlockSkill(Player player, ItemStack stack, Level level, BaseSoulSkill skill) {
        CompoundTag tag = stack.getTag();
        tag.putString(BaseSoulWeaponItem.CURRENT_FORGING_TASK, BaseSoulWeaponItem.NO_FORGING_TASK);

        ListTag listTag = tag.getList(BaseSoulWeaponItem.AVAILABLE_SKILLS, Tag.TAG_STRING);
        listTag.add(StringTag.valueOf(skill.soulSkillId.toString()));
        tag.put(BaseSoulWeaponItem.AVAILABLE_SKILLS, listTag);

        if (player instanceof ServerPlayer serverPlayer) {
            ModPacketHandler.sendToPlayer(new ForgingCompleteVFXS2CPacket(), serverPlayer);
        }
    }
}
