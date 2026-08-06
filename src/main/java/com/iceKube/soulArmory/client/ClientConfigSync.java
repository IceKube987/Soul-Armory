package com.iceKube.soulArmory.client;

import com.iceKube.soulArmory.Config;
import net.minecraft.client.Minecraft;

import java.util.Map;

// Client-side landing point for ConfigSyncS2CPacket. Split out from the packet so the Minecraft
// reference stays off the server's classloader.
public class ClientConfigSync {

    public static void accept(Map<String, Object> values) {
        // Single player shares one JVM with the integrated server, so Config's cache is already the
        // server's and there is nothing to adopt.
        if (Minecraft.getInstance().hasSingleplayerServer()) return;

        Config.applySynced(values);
    }
}
