package com.iceKube.soulArmory.networking.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public abstract class BasePacket {
    public BasePacket() {

    }

    public BasePacket(FriendlyByteBuf buf) {

    }

    public void toBytes(FriendlyByteBuf buf) {

    }

    public abstract boolean handle(Supplier<NetworkEvent.Context> supplier);
}
