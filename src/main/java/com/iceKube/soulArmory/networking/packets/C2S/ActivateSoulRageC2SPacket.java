package com.iceKube.soulArmory.networking.packets.C2S;

import com.iceKube.soulArmory.items.SoulChestplateItem;
import com.iceKube.soulArmory.networking.BasePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Sent when the player presses the Soul Rage key. Carries no payload — whether the rage may
 * actually start is decided entirely on the server from the worn chestplate's soul.
 */
public class ActivateSoulRageC2SPacket extends BasePacket {
    public ActivateSoulRageC2SPacket(FriendlyByteBuf buf) {
        super(buf);
    }

    public ActivateSoulRageC2SPacket() {
    }

    @Override
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            // Silently ignored when there is no chestplate, not enough soul, or a rage is already
            // running. The client finds out either way through the chestplate's synced NBT.
            SoulChestplateItem.tryActivateRage(player);
        });

        context.setPacketHandled(true);
        return true;
    }

}
