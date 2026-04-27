package com.iceKube.soulArmory.networking.packets.C2S;

import com.iceKube.soulArmory.networking.BasePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ExampleC2SPacket extends BasePacket {

    public ExampleC2SPacket(FriendlyByteBuf buf) {
        super(buf);
    }

    public ExampleC2SPacket() {
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            ServerLevel level = player.serverLevel();

            EntityType.COW.spawn(level, player.getOnPos(), MobSpawnType.COMMAND);

//            EntityType.COW.spawn(level, null, null, player.blockPosition(),
//                    MobSpawnType.COMMAND, true, false);

            context.setPacketHandled(true);
        });
        return true;
    }
}
