package com.iceKube.soulArmory.networking.packets.S2C;

import com.iceKube.soulArmory.Config;
import com.iceKube.soulArmory.client.ClientConfigSync;
import com.iceKube.soulArmory.networking.BasePacket;
import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;

import java.util.Map;
import java.util.function.Supplier;

// Ships the server's whole Config cache to a joining client. Config is a COMMON spec so Forge
// won't sync it, and without this the client renders the soul bars against its own config file.
public class ConfigSyncS2CPacket extends BasePacket {

    private static final Logger LOGGER = LogUtils.getLogger();

    // Empty when the payload was rejected, in which case the client keeps its own values.
    private final Map<String, Object> values;

    public ConfigSyncS2CPacket(FriendlyByteBuf buf) {
        super(buf);
        int count = buf.readVarInt();
        int signature = buf.readInt();

        if (count != Config.syncedFieldCount() || signature != Config.fieldSignature()) {
            // The two builds disagree on the config layout, so the rest of the buffer can't be
            // read as anything meaningful. Keeping the local values beats misreading the stream.
            LOGGER.warn("Ignoring config sync from server: expected {} values (signature {}), got {} (signature {})",
                    Config.syncedFieldCount(), Config.fieldSignature(), count, signature);
            buf.readerIndex(buf.writerIndex());
            this.values = Map.of();
            return;
        }

        this.values = Config.readAll(buf);
    }

    public ConfigSyncS2CPacket() {
        this.values = Map.of();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(Config.syncedFieldCount());
        buf.writeInt(Config.fieldSignature());
        Config.writeAll(buf);
    }

    @Override
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        if (!values.isEmpty()) {
            context.enqueueWork(() -> {
                // Guard client-only class loading so this packet stays safe to reference on the server.
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientConfigSync.accept(values));
            });
        }
        context.setPacketHandled(true);
        return true;
    }
}
