package com.iceKube.soulArmory.networking.packets.C2S;

import com.iceKube.soulArmory.items.SoulBowItem;
import com.iceKube.soulArmory.items.SoulSwordItem;
import com.iceKube.soulArmory.networking.BasePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SwitchSkillC2SPacket extends BasePacket {
    public SwitchSkillC2SPacket(FriendlyByteBuf buf) {
        super(buf);
    }

    public SwitchSkillC2SPacket() {
    }

    @Override
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            Level level = player.level();

            ItemStack stack = player.getMainHandItem();
            Item item = stack.getItem();

            if (item instanceof SoulBowItem soulBowItem) {
                soulBowItem.cycleToNextSkill(stack);
            }

            if (item instanceof SoulSwordItem soulSwordItem) {
                soulSwordItem.cycleToNextSkill(stack, player);
            }
        });

        return true;
    }

}
