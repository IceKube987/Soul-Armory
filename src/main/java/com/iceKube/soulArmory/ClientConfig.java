package com.iceKube.soulArmory;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

/**
 * Purely visual, per-player settings. Kept apart from {@link Config} because these belong to the
 * player looking at the screen, not to the server hosting the world — they ride their own
 * {@code ModConfig.Type.CLIENT} spec and are never synced.
 */
@Mod.EventBusSubscriber(modid = SoulArmoryMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.DoubleValue RADIAL_MENU_DIAMETER_RATIO;
    private static final ForgeConfigSpec.DoubleValue RADIAL_MENU_DEAD_ZONE_RATIO;

    static {
        BUILDER.comment("Skill Radial Menu Settings").push("radial-menu");

        RADIAL_MENU_DIAMETER_RATIO = BUILDER
                .comment("The diameter of the skill radial menu, as a fraction of the smaller of the two screen axes")
                .comment("Using the smaller axis keeps the whole menu on screen at any window shape")
                .defineInRange("radialMenuDiameterRatio", 0.8, 0.1, 1.0);

        RADIAL_MENU_DEAD_ZONE_RATIO = BUILDER
                .comment("The fraction of the menu radius around the center in which no skill is selected")
                .comment("Releasing the key with the cursor still inside this circle leaves the skill unchanged")
                .defineInRange("radialMenuDeadZoneRatio", 0.25, 0.0, 0.9);

        BUILDER.pop();
    }

    static final ForgeConfigSpec SPEC = BUILDER.build();

    // Seeded with the spec defaults so the menu still draws sanely if it is somehow rendered
    // before the client config has loaded.
    public static double radialMenuDiameterRatio = 0.8;
    public static double radialMenuDeadZoneRatio = 0.25;

    @SubscribeEvent
    static void onLoad(ModConfigEvent event) {
        // Fires once per spec the mod owns, so ignore everything that isn't ours.
        if (event.getConfig().getSpec() != SPEC) return;

        radialMenuDiameterRatio = RADIAL_MENU_DIAMETER_RATIO.get();
        radialMenuDeadZoneRatio = RADIAL_MENU_DEAD_ZONE_RATIO.get();
    }
}
