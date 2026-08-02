package com.iceKube.soulArmory.networking.packets.S2C;

import com.iceKube.soulArmory.client.OverlayHandler;
import com.iceKube.soulArmory.networking.BasePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

// Signals the client that a soul weapon skill switch happened, so it can play the switch VFX.
public class SwitchSkillVFXS2CPacket extends BasePacket {
    public SwitchSkillVFXS2CPacket(FriendlyByteBuf buf) {
        super(buf);
    }

    public SwitchSkillVFXS2CPacket() {
    }

    @Override
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
                    // Guard client-only class loading so this packet stays safe to reference on the server.
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> OverlayHandler::triggerSwitchSkillVFX);
                }
        );
        context.setPacketHandled(true);
        return true;
    }
}
