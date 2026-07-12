package com.iceKube.soulArmory.networking;

import com.iceKube.soulArmory.SoulArmoryMod;
import com.iceKube.soulArmory.networking.packets.C2S.SwitchSkillC2SPacket;
import com.iceKube.soulArmory.networking.packets.S2C.SwitchSkillVFXS2CPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModPacketHandler {
    private static final String PROTOCOL_VERSION = "1.0";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(SoulArmoryMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static int id() {
        return packetId++;
    }

    public static void register() {

        INSTANCE.messageBuilder(SwitchSkillC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SwitchSkillC2SPacket::new)
                .encoder(SwitchSkillC2SPacket::toBytes)
                .consumerMainThread(SwitchSkillC2SPacket::handle)
                .add();

        INSTANCE.messageBuilder(SwitchSkillVFXS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SwitchSkillVFXS2CPacket::new)
                .encoder(SwitchSkillVFXS2CPacket::toBytes)
                .consumerMainThread(SwitchSkillVFXS2CPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }


}
