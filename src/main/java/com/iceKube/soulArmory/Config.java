package com.iceKube.soulArmory;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = SoulArmoryMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.IntValue SOUL_SWORD_MAX_SOUL;
    private static final ForgeConfigSpec.DoubleValue SOUL_SWORD_BASE_DAMAGE;
    private static final ForgeConfigSpec.IntValue SOUL_SWORD_POINT_PER_DAMAGE;
    private static final ForgeConfigSpec.IntValue SOUL_SWORD_POINT_PER_HEALING;
    private static final ForgeConfigSpec.IntValue SOUL_SWORD_GRACE_PERIOD;
    private static final ForgeConfigSpec.IntValue SOUL_SWORD_SOUL_DECAY_SPEED;
    private static final ForgeConfigSpec.IntValue SOUL_SWORD_POINT_PER_SPEED_PERCENT;

    private static final ForgeConfigSpec.IntValue SOUL_BOW_MAX_SOUL;
    private static final ForgeConfigSpec.DoubleValue SOUL_BOW_BASE_DAMAGE;
    private static final ForgeConfigSpec.IntValue SOUL_BOW_POINT_PER_DAMAGE_PERCENT;
    private static final ForgeConfigSpec.IntValue SOUL_BOW_POINT_PER_SPEED_PERCENT;
    private static final ForgeConfigSpec.IntValue SOUL_BOW_GRACE_PERIOD;
    private static final ForgeConfigSpec.IntValue SOUL_BOW_SOUL_DECAY_SPEED;

    private static final ForgeConfigSpec.DoubleValue SOUL_ARROW_HOMING_RANGE;
    private static final ForgeConfigSpec.DoubleValue SOUL_ARROW_TURN_FACTOR;

    private static final ForgeConfigSpec.BooleanValue SOUL_SPEED_BOOST_HAS_CEIL;
    private static final ForgeConfigSpec.DoubleValue SOUL_SPEED_BOOST_CEIL;


    static {
        SOUL_SPEED_BOOST_HAS_CEIL = BUILDER
                .comment("Whether the speed boost provided by soul gears is capped at a maximum multiplier.")
                .comment("If false, the boost has no upper limit and speedBoostCeil will be ignored.")
                .define("speedBoostHasCeil", true);

        SOUL_SPEED_BOOST_CEIL = BUILDER
                .comment("The maximum speed multiplier (relative to base movement speed) at which soul gears will no longer provide additional speed boosts.")
                .comment("This value is ignored if speedBoostHasCeil is false.")
                .defineInRange("speedBoostCeil", 2.0, 1.0, Double.MAX_VALUE);

        BUILDER.comment("Soul Sword Settings").push("soul-sword");

        SOUL_SWORD_MAX_SOUL = BUILDER
                .comment("The maximum soul amount of Soul Sword")
                .defineInRange("soulSwordMaxSoul", 300, 0, Integer.MAX_VALUE);

        SOUL_SWORD_BASE_DAMAGE = BUILDER
                .comment("The base damage of Soul Sword")
                .defineInRange("soulSwordBaseDamage", 2.0, 1.0, Double.MAX_VALUE);

        SOUL_SWORD_POINT_PER_DAMAGE = BUILDER
                .comment("How many points of soul is required for 1 extra damage")
                .defineInRange("soulSwordPointPerDamage", 10, 1, Integer.MAX_VALUE);

        SOUL_SWORD_POINT_PER_HEALING = BUILDER
                .comment("How many points of soul is required for 1 HP in healing.")
                .defineInRange("soulSwordPointPerHealing", 7, 1, Integer.MAX_VALUE);

        SOUL_SWORD_GRACE_PERIOD = BUILDER
                .comment("How long (in ticks) will the soul start to decay after not holding.")
                .defineInRange("soulSwordGracePeriod", 200, 1, Integer.MAX_VALUE);

        SOUL_SWORD_SOUL_DECAY_SPEED = BUILDER
                .comment("How fast should 1 point of soul decay (in ticks)")
                .defineInRange("soulSwordSoulDecaySpeed", 6, 1, Integer.MAX_VALUE);

        SOUL_SWORD_POINT_PER_SPEED_PERCENT = BUILDER
                .comment("How many points of soul is required for 1 percent of speed boost")
                .defineInRange("soulSwordPointPerSpeedPercent", 3, 1, Integer.MAX_VALUE);

        BUILDER.pop();

        BUILDER.comment("Soul Bow Settings").push("soul-bow");

        SOUL_BOW_MAX_SOUL = BUILDER
                .comment("The maximum soul amount of Soul Bow")
                .defineInRange("soulBowMaxSoul", 300, 0, Integer.MAX_VALUE);

        SOUL_BOW_BASE_DAMAGE = BUILDER
                .comment("The base damage of Soul Bow")
                .defineInRange("soulBowBaseDamage", 2.0, 1.0, Double.MAX_VALUE);

        SOUL_BOW_POINT_PER_DAMAGE_PERCENT = BUILDER
                .comment("How many points of soul is required for 1 percent of extra damage and arrow speed.")
                .defineInRange("soulBowPointPerDamagePercent", 10, 1, Integer.MAX_VALUE);

        SOUL_BOW_POINT_PER_SPEED_PERCENT = BUILDER
                .comment("How many points of soul is required for 1 percent of speed boost")
                .defineInRange("soulBowPointPerSpeedPercent", 3, 1, Integer.MAX_VALUE);

        SOUL_BOW_GRACE_PERIOD = BUILDER
                .comment("How long (in ticks) will the soul start to decay after not holding.")
                .defineInRange("soulBowGracePeriod", 200, 0, Integer.MAX_VALUE);

        SOUL_BOW_SOUL_DECAY_SPEED = BUILDER
                .comment("How fast should 1 point of soul decay (in ticks)")
                .defineInRange("soulBowSoulDecaySpeed", 6, 1, Integer.MAX_VALUE);

        BUILDER.pop();

        BUILDER.comment("Soul Arrow Settings").push("soul-arrow");

        SOUL_ARROW_HOMING_RANGE = BUILDER
                .comment("The radius (in blocks) within which the Soul Arrow will search for and home in on the nearest hostile mob.")
                .defineInRange("soulArrowHomingRange", 32.0, 1.0, Double.MAX_VALUE);

        SOUL_ARROW_TURN_FACTOR = BUILDER
                .comment("How sharply the Soul Arrow steers toward its target each tick (0.0 = no steering, 1.0 = instant snap).")
                .comment("A value around 0.08–0.15 produces a smooth, missile-like curve.")
                .defineInRange("soulArrowTurnFactor", 0.1, 0.0, 1.0);

        BUILDER.pop();

    }

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static int soulSwordMaxSoul;
    public static int soulSwordPointsPerDamage;
    public static int soulSwordPointsPerHealing;
    public static double soulSwordBaseDamage;
    public static int soulSwordGracePeriod;
    public static int soulSwordSoulDecaySpeed;
    public static int soulSwordPointPerSpeedPercent;
    public static int soulBowMaxSoul;
    public static double soulBowBaseDamage;
    public static int soulBowPointPerDamagePercent;
    public static int soulBowPointPerSpeedPercent;
    public static int soulBowGracePeriod;
    public static  int soulBowSoulDecaySpeed;
    public static boolean speedBoostHasCeil;
    public static double speedBoostCeil;
    public static double soulArrowHomingRange;
    public static double soulArrowTurnFactor;

    @SubscribeEvent
    static void onLoad(ModConfigEvent event) {
        soulSwordMaxSoul = SOUL_SWORD_MAX_SOUL.get();
        soulSwordPointsPerDamage = SOUL_SWORD_POINT_PER_DAMAGE.get();
        soulSwordPointsPerHealing = SOUL_SWORD_POINT_PER_HEALING.get();
        soulSwordBaseDamage = SOUL_SWORD_BASE_DAMAGE.get();
        soulSwordGracePeriod = SOUL_SWORD_GRACE_PERIOD.get();
        soulSwordSoulDecaySpeed = SOUL_SWORD_SOUL_DECAY_SPEED.get();
        soulSwordPointPerSpeedPercent = SOUL_SWORD_POINT_PER_SPEED_PERCENT.get();
        soulBowMaxSoul = SOUL_BOW_MAX_SOUL.get();
        soulBowBaseDamage = SOUL_BOW_BASE_DAMAGE.get();
        soulBowPointPerDamagePercent = SOUL_BOW_POINT_PER_DAMAGE_PERCENT.get();
        soulBowPointPerSpeedPercent = SOUL_BOW_POINT_PER_SPEED_PERCENT.get();
        speedBoostHasCeil = SOUL_SPEED_BOOST_HAS_CEIL.get();
        speedBoostCeil = SOUL_SPEED_BOOST_CEIL.get();
        soulBowGracePeriod = SOUL_BOW_GRACE_PERIOD.get();
        soulBowSoulDecaySpeed = SOUL_BOW_SOUL_DECAY_SPEED.get();
        soulArrowHomingRange = SOUL_ARROW_HOMING_RANGE.get();
        soulArrowTurnFactor = SOUL_ARROW_TURN_FACTOR.get();
    }
}
