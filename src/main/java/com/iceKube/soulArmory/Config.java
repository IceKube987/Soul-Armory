package com.iceKube.soulArmory;

import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gameplay values, owned by whoever is running the world. This is a {@code ModConfig.Type.COMMON}
 * spec so the file stays a single global {@code config/soul_armory-common.toml} that modpack makers
 * can ship — but Forge does not sync COMMON specs, so the server pushes this whole cache to each
 * client on login (see {@code ConfigSyncS2CPacket}). Without that, a client would render the soul
 * bars against its own numbers while the server ran on different ones.
 */
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
    private static final ForgeConfigSpec.BooleanValue SOUL_SWORD_DISABLE_SHIELD_USAGE;
    private static final ForgeConfigSpec.IntValue SOUL_SWORD_OVERFLOW_SPEED;
    private static final ForgeConfigSpec.IntValue SOUL_SWORD_OVERFLOW_THRESHOLD;
    private static final ForgeConfigSpec.IntValue SOUL_SWORD_SWITCH_SKILL_COST;

    private static final ForgeConfigSpec.IntValue SOUL_SWORD_WAVE_COST;
    private static final ForgeConfigSpec.DoubleValue SOUL_SWORD_WAVE_SPEED;
    private static final ForgeConfigSpec.DoubleValue SOUL_SWORD_WAVE_LENGTH;
    private static final ForgeConfigSpec.IntValue SOUL_SWORD_WAVE_LIFETIME;

    private static final ForgeConfigSpec.IntValue SOUL_BOW_MAX_SOUL;
    private static final ForgeConfigSpec.DoubleValue SOUL_BOW_BASE_DAMAGE;
    private static final ForgeConfigSpec.IntValue SOUL_BOW_POINT_PER_DAMAGE_PERCENT;
    private static final ForgeConfigSpec.IntValue SOUL_BOW_POINT_PER_SPEED_PERCENT;
    private static final ForgeConfigSpec.IntValue SOUL_BOW_GRACE_PERIOD;
    private static final ForgeConfigSpec.IntValue SOUL_BOW_SOUL_DECAY_SPEED;
    private static final ForgeConfigSpec.IntValue SOUL_BOW_OVERFLOW_SPEED;
    private static final ForgeConfigSpec.IntValue SOUL_BOW_OVERFLOW_THRESHOLD;
    private static final ForgeConfigSpec.DoubleValue SOUL_BOW_TRACE_ANGLE;
    private static final ForgeConfigSpec.DoubleValue SOUL_BOW_TRACE_RANGE;
    private static final ForgeConfigSpec.IntValue SOUL_BOW_SKILL_SONIC_BOOM_RANGE;
    private static final ForgeConfigSpec.IntValue SOUL_BOW_SKILL_SONIC_BOOM_CONSUMPTION;
    private static final ForgeConfigSpec.DoubleValue SOUL_BOW_SKILL_SONIC_BOOM_DAMAGE;
    private static final ForgeConfigSpec.IntValue SOUL_BOW_SKILL_SCATTER_CONSUMPTION;
    private static final ForgeConfigSpec.IntValue SOUL_BOW_SKILL_SCATTER_ARROW_COUNT;
    private static final ForgeConfigSpec.DoubleValue SOUL_BOW_SKILL_SCATTER_SPREAD_ANGLE;
    private static final ForgeConfigSpec.DoubleValue SOUL_BOW_SKILL_SCATTER_DAMAGE;
    private static final ForgeConfigSpec.IntValue SOUL_BOW_SKILL_RAPID_FIRE_CONSUMPTION;
    private static final ForgeConfigSpec.IntValue SOUL_BOW_SKILL_RAPID_FIRE_EXECUTE_INTERVAL;
    private static final ForgeConfigSpec.DoubleValue SOUL_BOW_SKILL_RAPID_FIRE_DAMAGE;
    private static final ForgeConfigSpec.IntValue SOUL_BOW_SKILL_BARRAGE_CONSUMPTION;
    private static final ForgeConfigSpec.IntValue SOUL_BOW_SKILL_BARRAGE_EXECUTE_INTERVAL;
    private static final ForgeConfigSpec.IntValue SOUL_BOW_SKILL_BARRAGE_ARROW_COUNT;
    private static final ForgeConfigSpec.DoubleValue SOUL_BOW_SKILL_BARRAGE_SPREAD_ANGLE;
    private static final ForgeConfigSpec.DoubleValue SOUL_BOW_SKILL_BARRAGE_DAMAGE;
    private static final ForgeConfigSpec.IntValue SOUL_BOW_SKILL_OVERLOAD_CONSUMPTION;
    private static final ForgeConfigSpec.IntValue SOUL_BOW_SKILL_OVERLOAD_BLAST_COUNT;
    private static final ForgeConfigSpec.DoubleValue SOUL_BOW_SKILL_OVERLOAD_SPREAD_ANGLE;
    private static final ForgeConfigSpec.DoubleValue SOUL_BOW_SKILL_OVERLOAD_DAMAGE;
    private static final ForgeConfigSpec.IntValue SOUL_BOW_SKILL_OVERLOAD_RANGE;
    private static final ForgeConfigSpec.DoubleValue SOUL_ARROW_TURN_FACTOR;

    private static final ForgeConfigSpec.BooleanValue SOUL_SPEED_BOOST_HAS_CEIL;
    private static final ForgeConfigSpec.DoubleValue SOUL_SPEED_BOOST_CEIL;

    private static final ForgeConfigSpec.IntValue FORGING_SWORD_DAMAGE_TARGET;
    private static final ForgeConfigSpec.IntValue FORGING_SWORD_TIMEOUT_TICKS;
    private static final ForgeConfigSpec.IntValue FORGING_SWORD_ACTIVE_TICKS;
    private static final ForgeConfigSpec.IntValue FORGING_BOW_DAMAGE_TARGET;
    private static final ForgeConfigSpec.IntValue FORGING_BOW_TIMEOUT_TICKS;
    private static final ForgeConfigSpec.IntValue FORGING_BOW_ACTIVE_TICKS;
    private static final ForgeConfigSpec.IntValue FORGING_CHESTPLATE_SONIC_HITS;
    private static final ForgeConfigSpec.IntValue FORGING_CHESTPLATE_TIMEOUT_TICKS;
    private static final ForgeConfigSpec.IntValue FORGING_CHESTPLATE_ACTIVE_TICKS;
    private static final ForgeConfigSpec.DoubleValue FORGING_CHESTPLATE_HEAL_FRACTION;

    private static final ForgeConfigSpec.DoubleValue INCOMPLETE_SWORD_SPAWN_CHANCE;
    private static final ForgeConfigSpec.DoubleValue INCOMPLETE_BOW_SPAWN_CHANCE;
    private static final ForgeConfigSpec.DoubleValue INCOMPLETE_CHESTPLATE_SPAWN_CHANCE;

    private static final ForgeConfigSpec.IntValue UNLOCK_BLADE_WAVE_DAMAGE;
    private static final ForgeConfigSpec.IntValue UNLOCK_BLADE_WAVE_DAMAGE_TIMEOUT;
    private static final ForgeConfigSpec.IntValue UNLOCK_BLADE_WAVE_HEAL;
    private static final ForgeConfigSpec.IntValue UNLOCK_BLADE_WAVE_HEAL_TIMEOUT;
    private static final ForgeConfigSpec.IntValue UNLOCK_BLADE_WAVE_WARDEN_KILL;
    private static final ForgeConfigSpec.IntValue UNLOCK_BLADE_WAVE_WARDEN_KILL_TIMEOUT;

    private static final ForgeConfigSpec.IntValue UNLOCK_SONIC_OVERLOAD_DAMAGE;
    private static final ForgeConfigSpec.IntValue UNLOCK_SONIC_OVERLOAD_TIMEOUT;

    private static final ForgeConfigSpec.IntValue UNLOCK_RAPID_FIRE_DAMAGE;
    private static final ForgeConfigSpec.IntValue UNLOCK_RAPID_FIRE_TIMEOUT;

    private static final ForgeConfigSpec.IntValue UNLOCK_BARRAGE_DAMAGE;
    private static final ForgeConfigSpec.IntValue UNLOCK_BARRAGE_TIMEOUT;

    private static final ForgeConfigSpec.IntValue SOUL_CHESTPLATE_MAX_SOUL;
    private static final ForgeConfigSpec.IntValue SOUL_ARMOR_SOUL_ADDITION_PER_PIECE;
    private static final ForgeConfigSpec.IntValue SOUL_ARMOR_GRACE_PERIOD;
    private static final ForgeConfigSpec.IntValue SOUL_ARMOR_SOUL_DECAY_SPEED;
    private static final ForgeConfigSpec.IntValue SOUL_ARMOR_RAGE_THRESHOLD;
    private static final ForgeConfigSpec.DoubleValue SOUL_ARMOR_RAGE_DAMAGE_REDUCTION;
    private static final ForgeConfigSpec.DoubleValue SOUL_ARMOR_RAGE_SOUL_PER_TICK;
    private static final ForgeConfigSpec.DoubleValue SOUL_ARMOR_RAGE_LIFESTEAL_RATIO;
    private static final ForgeConfigSpec.BooleanValue SOUL_ARMOR_ABSORB_SONIC_BOOM;
    private static final ForgeConfigSpec.IntValue SOUL_ARMOR_SONIC_BOOM_SOUL_REWARD;
    private static final ForgeConfigSpec.IntValue SOUL_ARMOR_RAGE_NIGHT_VISION_LINGER;
    private static final ForgeConfigSpec.IntValue SOUL_ARMOR_RAGE_SPEED_BONUS;
    private static final ForgeConfigSpec.DoubleValue SOUL_ARMOR_RAGE_STEP_HEIGHT_ADDITION;
    private static final ForgeConfigSpec.BooleanValue SOUL_ARMOR_RAGE_POISON_IMMUNITY;
    private static final ForgeConfigSpec.DoubleValue SOUL_ARMOR_RAGE_FALL_AOE_RADIUS;
    private static final ForgeConfigSpec.DoubleValue SOUL_ARMOR_RAGE_FALL_AOE_DAMAGE_MULTIPLIER;

    private static final ForgeConfigSpec.BooleanValue SOUL_ARMOR_FULL_SET_BONUS_ENABLED;
    private static final ForgeConfigSpec.IntValue SOUL_ARMOR_FULL_SET_SOUL_FLOOR;
    private static final ForgeConfigSpec.IntValue SOUL_ARMOR_FULL_SET_REGEN_SPEED;
    private static final ForgeConfigSpec.DoubleValue SOUL_ARMOR_FULL_SET_IDLE_FAILSAFE_MAX_HEALTH_FRACTION;
    private static final ForgeConfigSpec.BooleanValue SOUL_ARMOR_FULL_SET_IDLE_FAILSAFE_IGNORES_VOID;

    static {
        SOUL_SPEED_BOOST_HAS_CEIL = BUILDER
                .comment("Whether the speed boost provided by soul weapons or armor is capped at a maximum multiplier")
                .comment("If false, the boost has no upper limit and speedBoostCeil will be ignored")
                .define("speedBoostHasCeil", true);

        SOUL_SPEED_BOOST_CEIL = BUILDER
                .comment("The maximum speed multiplier (relative to base movement speed) at which soul weapons or armor will no longer provide additional speed boosts")
                .comment("This value is ignored if speedBoostHasCeil is false")
                .defineInRange("speedBoostCeil", 2.0, 1.0, Double.MAX_VALUE);

        {
            BUILDER.comment("Soul Sword Settings").push("soul-sword");

            SOUL_SWORD_MAX_SOUL = BUILDER
                    .comment("The maximum soul amount of Soul Sword")
                    .defineInRange("soulSwordMaxSoul", 300, 1, Integer.MAX_VALUE);

            SOUL_SWORD_BASE_DAMAGE = BUILDER
                    .comment("The base damage of Soul Sword")
                    .defineInRange("soulSwordBaseDamage", 4.0, 1.0, Double.MAX_VALUE);

            SOUL_SWORD_POINT_PER_DAMAGE = BUILDER
                    .comment("How many points of soul is required for 1 extra damage")
                    .defineInRange("soulSwordPointPerDamage", 12, 1, Integer.MAX_VALUE);

            SOUL_SWORD_GRACE_PERIOD = BUILDER
                    .comment("How long (in ticks) will the soul start to decay after putting away")
                    .defineInRange("soulSwordGracePeriod", 200, 1, Integer.MAX_VALUE);

            SOUL_SWORD_SOUL_DECAY_SPEED = BUILDER
                    .comment("How fast should 1 point of soul decay (in ticks)")
                    .defineInRange("soulSwordSoulDecaySpeed", 6, 1, Integer.MAX_VALUE);

            SOUL_SWORD_OVERFLOW_THRESHOLD = BUILDER
                    .comment("The soul amount threshold above which soul start to decay in overflow speed")
                    .comment("To disable this feature, change the value to the same as max soul")
                    .defineInRange("soulSwordOverflowThreshold", 200, 1, Integer.MAX_VALUE);

            SOUL_SWORD_OVERFLOW_SPEED = BUILDER
                    .comment("How fast should 1 point of soul decay (in ticks) when soul amount is above overflow threshold")
                    .defineInRange("soulSwordOverflowSpeed", 10, 1, Integer.MAX_VALUE);

            SOUL_SWORD_POINT_PER_SPEED_PERCENT = BUILDER
                    .comment("How many points of soul is required for 1 percent of speed boost")
                    .defineInRange("soulSwordPointPerSpeedPercent", 3, 1, Integer.MAX_VALUE);

            SOUL_SWORD_DISABLE_SHIELD_USAGE = BUILDER
                    .comment("Whether holding a Soul Sword in main hand prevents using shields in off-hand")
                    .define("soulSwordDisableShieldUsage", true);

            SOUL_SWORD_SWITCH_SKILL_COST = BUILDER
                    .comment("How much soul is consumed when switching skills on the Soul Sword")
                    .defineInRange("soulSwordSwitchSkillCost", 100, 0, Integer.MAX_VALUE);

            {
                BUILDER.push("healing");

                SOUL_SWORD_POINT_PER_HEALING = BUILDER
                        .comment("How many points of soul is required for 1 HP in healing")
                        .defineInRange("soulSwordPointPerHealing", 10, 1, Integer.MAX_VALUE);

                BUILDER.pop();
            }

            {
                BUILDER.push("blade-wave");

                SOUL_SWORD_WAVE_COST = BUILDER
                        .comment("How many points of soul will it take to use the skill")
                        .defineInRange("bladeWaveCost", 35, 0, Integer.MAX_VALUE);
                SOUL_SWORD_WAVE_SPEED = BUILDER
                        .comment("How far (in blocks) should the wave move in one tick")
                        .defineInRange("bladeWaveSpeed", 1, 0, Double.MAX_VALUE);
                SOUL_SWORD_WAVE_LENGTH = BUILDER
                        .comment("How long (in blocks) is the wave")
                        .defineInRange("bladeWaveLength", 3, 0, Double.MAX_VALUE);
                SOUL_SWORD_WAVE_LIFETIME = BUILDER
                        .comment("How long (in ticks) will it exist")
                        .defineInRange("bladeWaveLifetime", 20, 1, Integer.MAX_VALUE);

                BUILDER.pop();
            }

            BUILDER.pop();
        }

        {
            BUILDER.comment("Soul Bow Settings").push("soul-bow");

            SOUL_BOW_MAX_SOUL = BUILDER
                    .comment("The maximum soul amount of Soul Bow")
                    .defineInRange("soulBowMaxSoul", 300, 0, Integer.MAX_VALUE);

            SOUL_BOW_BASE_DAMAGE = BUILDER
                    .comment("The base damage of Soul Bow")
                    .defineInRange("soulBowBaseDamage", 6.0, 1.0, Double.MAX_VALUE);

            SOUL_BOW_POINT_PER_DAMAGE_PERCENT = BUILDER
                    .comment("How many points of soul is required for 1 percent of extra damage and drawing speed ")
                    .defineInRange("soulBowPointPerDamagePercent", 1, 1, Integer.MAX_VALUE);

            SOUL_BOW_GRACE_PERIOD = BUILDER
                    .comment("How long (in ticks) will the soul start to decay after putting away")
                    .defineInRange("soulBowGracePeriod", 200, 0, Integer.MAX_VALUE);

            SOUL_BOW_SOUL_DECAY_SPEED = BUILDER
                    .comment("How fast should 1 point of soul decay (in ticks)")
                    .defineInRange("soulBowSoulDecaySpeed", 6, 1, Integer.MAX_VALUE);

            SOUL_BOW_OVERFLOW_THRESHOLD = BUILDER
                    .comment("The soul amount threshold above which soul start to decay at overflow speed")
                    .comment("To disable this feature, change the value to the same as max soul")
                    .defineInRange("soulBowOverflowThreshold", 200, 1, Integer.MAX_VALUE);

            SOUL_BOW_OVERFLOW_SPEED = BUILDER
                    .comment("How fast should 1 point of soul decay (in ticks) when soul amount is above overflow threshold")
                    .defineInRange("soulBowOverflowSpeed", 10, 1, Integer.MAX_VALUE);

            SOUL_BOW_POINT_PER_SPEED_PERCENT = BUILDER
                    .comment("How many points of soul is required for 1 percent of speed boost")
                    .defineInRange("soulBowPointPerSpeedPercent", 6, 1, Integer.MAX_VALUE);

            SOUL_BOW_TRACE_ANGLE = BUILDER
                    .comment("Lock-on cone half-angle in degrees")
                    .comment("E.g. 10 means enemies within 10° left/right of your crosshair can be targeted")
                    .defineInRange("soulBowTraceAngle", 10.0, 0.0, 90.0);

            SOUL_BOW_TRACE_RANGE = BUILDER
                    .comment("Max distance for lock-on target acquisition. Enemies beyond this range are ignored")
                    .defineInRange("soulBowTraceRange", 50.0, 0.0, Double.MAX_VALUE);

            {
                BUILDER.comment("Different Skills for Soul Bow").push("skills");

                {
                    BUILDER.push("sonic-boom");

                    SOUL_BOW_SKILL_SONIC_BOOM_CONSUMPTION = BUILDER
                            .comment("How many points of soul will it take to use the skill")
                            .defineInRange("soulBowSkillSBConsumption", 100, 0, Integer.MAX_VALUE);

                    SOUL_BOW_SKILL_SONIC_BOOM_DAMAGE = BUILDER
                            .comment("The damage dealt by the skill")
                            .defineInRange("soulBowSkillSBDamage", 80.0, 1.0, Double.MAX_VALUE);

                    SOUL_BOW_SKILL_SONIC_BOOM_RANGE = BUILDER
                            .comment("The range of the skill")
                            .defineInRange("soulBowSkillSBRange", 50, 1, Integer.MAX_VALUE);

                    BUILDER.pop();
                }

                {
                    BUILDER.push("scatter-shot");

                    SOUL_BOW_SKILL_SCATTER_CONSUMPTION = BUILDER
                            .comment("How many points of soul will it take to use the skill")
                            .defineInRange("soulBowSkillSSConsumption", 20, 0, Integer.MAX_VALUE);

                    SOUL_BOW_SKILL_SCATTER_DAMAGE = BUILDER
                            .comment("The damage of each arrow")
                            .defineInRange("soulBowSkillSSDamage", 12.0, 0.0, Double.MAX_VALUE);

                    SOUL_BOW_SKILL_SCATTER_ARROW_COUNT = BUILDER
                            .comment("How many arrows will be shot when using the skill")
                            .defineInRange("soulBowSkillSSArrowCount", 5, 1, Integer.MAX_VALUE);

                    SOUL_BOW_SKILL_SCATTER_SPREAD_ANGLE = BUILDER
                            .comment("The maximum spread angle (in degrees) for each arrow from the center direction")
                            .defineInRange("soulBowSkillSSSpreadAngle", 10.0, 0.0, 90.0);

                    BUILDER.pop();
                }

                {
                    BUILDER.push("rapid-fire");

                    SOUL_BOW_SKILL_RAPID_FIRE_CONSUMPTION = BUILDER
                            .comment("How many points of soul will it take to use the skill")
                            .defineInRange("soulBowSkillRFConsumption", 5, 0, Integer.MAX_VALUE);

                    SOUL_BOW_SKILL_RAPID_FIRE_DAMAGE = BUILDER
                            .comment("The damage dealt by each arrow")
                            .defineInRange("soulBowSkillRFDamage", 10.0, 0.0, Double.MAX_VALUE);

                    SOUL_BOW_SKILL_RAPID_FIRE_EXECUTE_INTERVAL = BUILDER
                            .comment("The interval (in ticks) between arrow shots")
                            .defineInRange("soulBowSkillRFExecuteInterval", 3, 1, Integer.MAX_VALUE);

                    BUILDER.pop();
                }

                {
                    BUILDER.push("barrage");

                    SOUL_BOW_SKILL_BARRAGE_CONSUMPTION = BUILDER
                            .comment("How many points of soul will it take to use the skill")
                            .defineInRange("soulBowSkillBConsumption", 15, 0, Integer.MAX_VALUE);

                    SOUL_BOW_SKILL_BARRAGE_DAMAGE = BUILDER
                            .comment("The damage dealt by each arrow")
                            .defineInRange("soulBowSkillBDamage", 12.0, 0.0, Double.MAX_VALUE);

                    SOUL_BOW_SKILL_BARRAGE_EXECUTE_INTERVAL = BUILDER
                            .comment("The interval (in ticks) between barrage shots")
                            .defineInRange("soulBowSkillBExecuteInterval", 3, 1, Integer.MAX_VALUE);

                    SOUL_BOW_SKILL_BARRAGE_ARROW_COUNT = BUILDER
                            .comment("How many arrows will be shot when using the skill")
                            .defineInRange("soulBowSkillBArrowCount", 3, 1, Integer.MAX_VALUE);

                    SOUL_BOW_SKILL_BARRAGE_SPREAD_ANGLE = BUILDER
                            .comment("The spread angle (in degrees) for arrows")
                            .defineInRange("soulBowSkillBAngle", 10.0, 0.0, 90.0);

                    BUILDER.pop();
                }

                {
                    BUILDER.push("sonic-overload");

                    SOUL_BOW_SKILL_OVERLOAD_CONSUMPTION = BUILDER
                            .comment("How many points of soul will it take to use the skill")
                            .defineInRange("soulBowSkillSOConsumption", 250, 0, Integer.MAX_VALUE);

                    SOUL_BOW_SKILL_OVERLOAD_DAMAGE = BUILDER
                            .comment("The damage dealt by the skill")
                            .defineInRange("soulBowSkillSODamage", 150.0, 0.0, Double.MAX_VALUE);

                    SOUL_BOW_SKILL_OVERLOAD_RANGE = BUILDER
                            .comment("The range of the skill")
                            .defineInRange("soulBowSkillSORange", 50, 0, Integer.MAX_VALUE);

                    SOUL_BOW_SKILL_OVERLOAD_BLAST_COUNT = BUILDER
                            .comment("How many blasts will be fired when using the skill")
                            .defineInRange("soulBowSkillSOBlastCount", 7, 1, Integer.MAX_VALUE);

                    SOUL_BOW_SKILL_OVERLOAD_SPREAD_ANGLE = BUILDER
                            .comment("The maximum spread angle (in degrees) for each blast from the center direction")
                            .defineInRange("soulBowSkillSOSpreadAngle", 30.0, 0.0, 90.0);

                    BUILDER.pop();
                }

                BUILDER.pop();
            }
            BUILDER.pop();
        }

        {
            BUILDER.comment("Soul Arrow Settings").push("soul-arrow");

            SOUL_ARROW_TURN_FACTOR = BUILDER
                    .comment("How sharply the Soul Arrow steers toward its target each tick (0.0 = no steering, 1.0 = instant snap)")
                    .defineInRange("soulArrowTurnFactor", 0.5, 0.0, 1.0);

            BUILDER.pop();
        }

        {
            BUILDER.comment("Soul Armor Settings").push("soul-armor");

            SOUL_CHESTPLATE_MAX_SOUL = BUILDER
                    .comment("The maximum soul amount of armor set with only chestplate equipped")
                    .defineInRange("soulChestplateMaxSoul", 300, 1, Integer.MAX_VALUE);

            SOUL_ARMOR_SOUL_ADDITION_PER_PIECE = BUILDER
                    .comment("How many points of soul will be added with each additional armor piece (that is, every soul armor other than chestplate) equipped")
                    .defineInRange("soulArmorAdditionalSoul", 100, 1, Integer.MAX_VALUE);

            SOUL_ARMOR_GRACE_PERIOD = BUILDER
                    .comment("How long (in ticks) will the soul start to decay after gaining points of soul")
                    .defineInRange("soulArmorGracePeriod", 2400, 1, Integer.MAX_VALUE);

            SOUL_ARMOR_SOUL_DECAY_SPEED = BUILDER
                    .comment("How fast should 1 point of soul decay (in ticks)")
                    .defineInRange("soulArmorSoulDecaySpeed", 5, 1, Integer.MAX_VALUE);

            SOUL_ARMOR_RAGE_THRESHOLD = BUILDER
                    .comment("How many points of soul is required to start Soul Rage")
                    .defineInRange("soulArmorRageThreshold", 50, 1, Integer.MAX_VALUE);

            SOUL_ARMOR_RAGE_SOUL_PER_TICK = BUILDER
                    .comment("How many points of soul is consumed per tick while in Soul Rage")
                    .defineInRange("soulArmorRageSoulPerTick", 0.5, 0.0, Double.MAX_VALUE);

            SOUL_ARMOR_RAGE_DAMAGE_REDUCTION = BUILDER
                    .comment("The fraction of incoming damage negated while in Soul Rage (0.6 = 60% less damage taken)")
                    .defineInRange("soulArmorRageDamageReduction", 0.7, 0.0, 1.0);

            SOUL_ARMOR_RAGE_LIFESTEAL_RATIO = BUILDER
                    .comment("How much damage must be dealt to heal 1 point of health while in Soul Rage")
                    .defineInRange("soulArmorRageLifestealRatio", 10.0, 0.01, Double.MAX_VALUE);

            SOUL_ARMOR_RAGE_SPEED_BONUS = BUILDER
                    .comment("The percentage of movement speed granted by the soul leggings while in Soul Rage")
                    .comment("This boost is still subject to speedBoostCeil unless speedBoostHasCeil is false")
                    .defineInRange("soulArmorRageSpeedBonus", 100, 0, Integer.MAX_VALUE);

            SOUL_ARMOR_RAGE_STEP_HEIGHT_ADDITION = BUILDER
                    .comment("How much step height the soul leggings add while in Soul Rage (0.5 lets the player walk up a full block, like a horse)")
                    .comment("Set to 0 to disable the step assist entirely")
                    .defineInRange("soulArmorRageStepHeightAddition", 0.5, 0.0, 2.0);

            SOUL_ARMOR_RAGE_POISON_IMMUNITY = BUILDER
                    .comment("Whether Soul Rage makes the player immune to poison")
                    .comment("Poison already on the player is cured when the rage starts, and is not restored when it ends")
                    .define("soulArmorRagePoisonImmunity", true);

            SOUL_ARMOR_RAGE_NIGHT_VISION_LINGER = BUILDER
                    .comment("How long (in ticks) the night vision granted by the soul helmet persists after Soul Rage ends")
                    .defineInRange("soulArmorRageNightVisionLinger", 1200, 1, Integer.MAX_VALUE);

            SOUL_ARMOR_RAGE_FALL_AOE_RADIUS = BUILDER
                    .comment("The radius of the shockwave dealt by the soul boots when landing during Soul Rage")
                    .defineInRange("soulArmorRageFallAoeRadius", 5.0, 0.0, Double.MAX_VALUE);

            SOUL_ARMOR_RAGE_FALL_AOE_DAMAGE_MULTIPLIER = BUILDER
                    .comment("The damage of the landing shockwave, as a multiple of the fall damage that was negated, 0 to disable it.")
                    .defineInRange("soulArmorRageFallAoeDamageMultiplier", 0, 0.0, Double.MAX_VALUE);

            SOUL_ARMOR_ABSORB_SONIC_BOOM = BUILDER
                    .comment("Whether wearing a soul chestplate negates the damage of the Warden's sonic boom")
                    .comment("The soul reward is granted either way")
                    .define("soulArmorAbsorbSonicBoom", true);

            SOUL_ARMOR_SONIC_BOOM_SOUL_REWARD = BUILDER
                    .comment("How many points of soul is granted when the soul chestplate is hit by a sonic boom")
                    .defineInRange("soulArmorSonicBoomSoulReward", 60, 0, Integer.MAX_VALUE);

            SOUL_ARMOR_FULL_SET_BONUS_ENABLED = BUILDER
                    .comment("Whether wearing all four soul armor pieces grants the full set bonus")
                    .define("soulArmorFullSetBonusEnabled", true);

            SOUL_ARMOR_FULL_SET_SOUL_FLOOR = BUILDER
                    .comment("The soul level the full set sustains: natural decay stops here, and the pool regenerates back up to it")
                    .comment("Soul Rage still drains straight through this floor to zero")
                    .comment("Also the soul amount, in whole points, at which the idle failsafe is available")
                    .defineInRange("soulArmorFullSetSoulFloor", 100, 0, Integer.MAX_VALUE);

            SOUL_ARMOR_FULL_SET_REGEN_SPEED = BUILDER
                    .comment("How fast should 1 point of soul regenerate below soulArmorFullSetSoulFloor (in ticks)")
                    .defineInRange("soulArmorFullSetRegenSpeed", 20, 1, Integer.MAX_VALUE);

            SOUL_ARMOR_FULL_SET_IDLE_FAILSAFE_MAX_HEALTH_FRACTION = BUILDER
                    .comment("The largest share of the wearer's maximum health a single hit may deal while resting at soulArmorFullSetSoulFloor")
                    .comment("Measured after armor, resistance and absorption; anything larger is reduced to exactly this share")
                    .comment("1.0 disables the cap, 0.0 makes such hits deal nothing")
                    .defineInRange("soulArmorFullSetIdleFailsafeMaxHealthFraction", 0.75, 0.0, 1.0);

            SOUL_ARMOR_FULL_SET_IDLE_FAILSAFE_IGNORES_VOID = BUILDER
                    .comment("Whether damage that bypasses invulnerability, such as the void and /kill, is exempt from the idle failsafe")
                    .define("soulArmorFullSetIdleFailsafeIgnoresVoid", true);

            BUILDER.pop();
        }

        {
            BUILDER.comment("Soul Forging Settings").push("soul-forging");

            {
                BUILDER.comment("When forging incomplete sword into complete sword").push("sword");

                FORGING_SWORD_DAMAGE_TARGET = BUILDER
                        .comment("Total damage to Wardens required to forge the soul sword")
                        .defineInRange("forgingSwordDamageTarget", 450, 1, Integer.MAX_VALUE);

                FORGING_SWORD_TIMEOUT_TICKS = BUILDER
                        .comment("Ticks of inactivity before sword forging progress resets (0 = no timeout)")
                        .defineInRange("forgingSwordTimeoutTicks", 3600, 0, Integer.MAX_VALUE);

                FORGING_SWORD_ACTIVE_TICKS = BUILDER
                        .comment("Duration in ticks the incomplete sword stays active after hitting a Warden")
                        .defineInRange("forgingSwordActiveTicks", 200, 1, Integer.MAX_VALUE);

                BUILDER.pop();
            }

            {
                BUILDER.comment("When forging incomplete bow into complete bow").push("bow");

                FORGING_BOW_DAMAGE_TARGET = BUILDER
                        .comment("Total damage to Wardens required to forge the soul bow")
                        .defineInRange("forgingBowDamageTarget", 450, 1, Integer.MAX_VALUE);

                FORGING_BOW_TIMEOUT_TICKS = BUILDER
                        .comment("Ticks of inactivity before bow forging progress resets (0 = no timeout)")
                        .defineInRange("forgingBowTimeoutTicks", 3600, 0, Integer.MAX_VALUE);

                FORGING_BOW_ACTIVE_TICKS = BUILDER
                        .comment("Duration in ticks the incomplete bow stays active after hitting a Warden")
                        .defineInRange("forgingBowActiveTicks", 200, 1, Integer.MAX_VALUE);

                BUILDER.pop();
            }

            {
                BUILDER.comment("When forging incomplete chestplate into complete chestplate").push("chestplate");

                FORGING_CHESTPLATE_SONIC_HITS = BUILDER
                        .comment("Number of Warden sonic boom hits required to forge the chestplate")
                        .defineInRange("forgingChestplateSonicHits", 10, 1, Integer.MAX_VALUE);

                FORGING_CHESTPLATE_TIMEOUT_TICKS = BUILDER
                        .comment("Ticks of inactivity before chestplate forging progress resets (0 = no timeout)")
                        .defineInRange("forgingChestplateTimeoutTicks", 3600, 0, Integer.MAX_VALUE);

                FORGING_CHESTPLATE_ACTIVE_TICKS = BUILDER
                        .comment("Duration in ticks the incomplete chestplate stays active after a sonic boom hit")
                        .defineInRange("forgingChestplateActiveTicks", 120, 1, Integer.MAX_VALUE);

                FORGING_CHESTPLATE_HEAL_FRACTION = BUILDER
                        .comment("Fraction of the wearer's maximum health the incomplete chestplate heals when it absorbs a sonic boom (1.0 = a full heal, 0.0 = none)")
                        .defineInRange("forgingChestplateHealFraction", 1.0, 0.0, 1.0);

                BUILDER.pop();
            }

            {
                BUILDER.comment("When unlocking Blade Wave Skill for Soul Sword").push("unlock-blade-wave");

                UNLOCK_BLADE_WAVE_DAMAGE = BUILDER
                        .comment("Total damage required to unlock the skill")
                        .defineInRange("unlockBladeWaveDamageTarget", 3000, 1, Integer.MAX_VALUE);

                UNLOCK_BLADE_WAVE_DAMAGE_TIMEOUT = BUILDER
                        .comment("Ticks of inactivity before damage progress resets (0 = no timeout)")
                        .defineInRange("unlockBladeWaveDamageTimeout", 0, 0, Integer.MAX_VALUE);

                UNLOCK_BLADE_WAVE_HEAL = BUILDER
                        .comment("Total points of healing required to unlock the skill")
                        .defineInRange("unlockBladeWaveHealTarget", 100, 1, Integer.MAX_VALUE);

                UNLOCK_BLADE_WAVE_HEAL_TIMEOUT = BUILDER
                        .comment("Ticks of inactivity before healing progress resets (0 = no timeout)")
                        .defineInRange("unlockBladeWaveHealTimeout", 0, 0, Integer.MAX_VALUE);

                UNLOCK_BLADE_WAVE_WARDEN_KILL = BUILDER
                        .comment("Total Warden kills required to unlock the skill")
                        .defineInRange("unlockBladeWaveKillTarget", 3, 1, Integer.MAX_VALUE);

                UNLOCK_BLADE_WAVE_WARDEN_KILL_TIMEOUT = BUILDER
                        .comment("Ticks of inactivity before Warden kill progress resets (0 = no timeout)")
                        .defineInRange("unlockBladeWaveKillTimeout", 2400, 0, Integer.MAX_VALUE);

                BUILDER.pop();
            }

            {
                BUILDER.comment("When unlocking Sonic Overload Skill for Soul Bow").push("unlock-sonic-overload");

                UNLOCK_SONIC_OVERLOAD_DAMAGE = BUILDER
                        .comment("Total damage (caused by sonic boom skill) required to unlock the skill")
                        .defineInRange("unlockSonicOverloadDamageTarget", 400, 1, Integer.MAX_VALUE);

                UNLOCK_SONIC_OVERLOAD_TIMEOUT = BUILDER
                        .comment("Ticks of inactivity before forging progress resets (0 = no timeout)")
                        .defineInRange("unlockSonicOverloadTimeout", 0, 0, Integer.MAX_VALUE);

                BUILDER.pop();
            }

            {
                BUILDER.comment("When unlocking Rapid Fire Skill for Soul Bow").push("unlock-rapid-fire");

                UNLOCK_RAPID_FIRE_DAMAGE = BUILDER
                        .comment("Total damage required to unlock the skill")
                        .defineInRange("unlockRapidFireDamageTarget", 100, 1, Integer.MAX_VALUE);

                UNLOCK_RAPID_FIRE_TIMEOUT = BUILDER
                        .comment("Ticks of inactivity before forging progress resets (0 = no timeout)")
                        .defineInRange("unlockRapidFireTimeout", 20, 0, Integer.MAX_VALUE);

                BUILDER.pop();
            }

            {
                BUILDER.comment("When unlocking Barrage Skill for Soul Bow").push("unlock-barrage");

                UNLOCK_BARRAGE_DAMAGE = BUILDER
                        .comment("Total damage required to unlock the skill")
                        .defineInRange("unlockBarrageDamageTarget", 300, 1, Integer.MAX_VALUE);

                UNLOCK_BARRAGE_TIMEOUT = BUILDER
                        .comment("Ticks of inactivity before forging progress resets (0 = no timeout)")
                        .defineInRange("unlockBarrageTimeout", 20, 0, Integer.MAX_VALUE);

                BUILDER.pop();
            }

            BUILDER.pop();
        }

        {
            BUILDER.comment("Incomplete soul equipments spawn settings").push("spawn-settings");

            INCOMPLETE_SWORD_SPAWN_CHANCE = BUILDER
                    .comment("The chance of the incomplete sword spawning inside a chest in ancient city")
                    .defineInRange("swordSpawnChance", 0.1, 0.0, 1.0);

            INCOMPLETE_BOW_SPAWN_CHANCE = BUILDER
                    .comment("The chance of the incomplete bow spawning inside a chest in ancient city")
                    .defineInRange("bowSpawnChance", 0.1, 0.0, 1.0);

            INCOMPLETE_CHESTPLATE_SPAWN_CHANCE = BUILDER
                    .comment("The chance of the incomplete chestplate spawning inside a chest in ancient city")
                    .defineInRange("chestplateSpawnChance", 0.1, 0.0, 1.0);

            BUILDER.pop();
        }
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
    public static int soulBowSoulDecaySpeed;
    public static int soulBowSkillSBRange;
    public static int soulBowSkillSBConsumption;
    public static double soulBowSkillSBDamage;
    public static int soulBowSkillSSConsumption;
    public static int soulBowSkillSSArrowCount;
    public static double soulBowSkillSSSpreadAngle;
    public static double soulBowSkillSSDamage;
    public static int soulBowSkillRFConsumption;
    public static int soulBowSkillRFExecuteInterval;
    public static double soulBowSkillRFDamage;
    public static int soulBowSkillBConsumption;
    public static int soulBowSkillBExecuteInterval;
    public static double soulBowSkillBDamage;
    public static int soulBowSkillBArrowCount;
    public static double soulBowSkillBAngle;
    public static double soulBowTraceAngle;
    public static double soulBowTraceRange;
    public static boolean speedBoostHasCeil;
    public static double speedBoostCeil;
    public static boolean soulSwordDisableShieldUsage;
    public static double soulArrowTurnFactor;
    public static int soulSwordOverflowSpeed;
    public static int soulSwordOverflowThreshold;
    public static int soulSwordSwitchSkillCost;
    public static int soulBowOverflowSpeed;
    public static int soulBowOverflowThreshold;
    public static int soulBowSkillSOConsumption;
    public static int soulBowSkillSOBlastCount;
    public static double soulBowSkillSOSpreadAngle;
    public static double soulBowSkillSODamage;
    public static int soulBowSkillSORange;
    public static double bladeWaveSpeed;
    public static double bladeWaveLength;
    public static int bladeWaveLifetime;
    public static int bladeWaveCost;
    public static int forgingSwordDamageTarget;
    public static int forgingSwordTimeoutTicks;
    public static int forgingSwordActiveTicks;
    public static int forgingBowDamageTarget;
    public static int forgingBowTimeoutTicks;
    public static int forgingBowActiveTicks;
    public static int forgingChestplateSonicHits;
    public static int forgingChestplateTimeoutTicks;
    public static int forgingChestplateActiveTicks;
    public static double forgingChestplateHealFraction;
    public static double swordSpawnChance;
    public static double bowSpawnChance;
    public static double chestplateSpawnChance;
    public static int unlockBladeWaveDamageTarget;
    public static int unlockBladeWaveDamageTimeout;
    public static int unlockBladeWaveHealTarget;
    public static int unlockBladeWaveHealTimeout;
    public static int unlockBladeWaveKillTarget;
    public static int unlockBladeWaveKillTimeout;
    public static int unlockSonicOverloadDamageTarget;
    public static int unlockSonicOverloadTimeout;
    public static int unlockRapidFireDamageTarget;
    public static int unlockRapidFireTimeout;
    public static int unlockBarrageDamageTarget;
    public static int unlockBarrageTimeout;
    public static int soulChestplateMaxSoul;
    public static int soulArmorAdditionalSoul;
    public static int soulArmorGracePeriod;
    public static int soulArmorSoulDecaySpeed;
    public static int soulArmorRageThreshold;
    public static double soulArmorRageDamageReduction;
    public static double soulArmorRageSoulPerTick;
    public static double soulArmorRageLifestealRatio;
    public static boolean soulArmorAbsorbSonicBoom;
    public static int soulArmorSonicBoomSoulReward;
    public static int soulArmorRageNightVisionLinger;
    public static int soulArmorRageSpeedBonus;
    public static double soulArmorRageStepHeightAddition;
    public static boolean soulArmorRagePoisonImmunity;
    public static double soulArmorRageFallAoeRadius;
    public static double soulArmorRageFallAoeDamageMultiplier;
    public static boolean soulArmorFullSetBonusEnabled;
    public static int soulArmorFullSetSoulFloor;
    public static int soulArmorFullSetRegenSpeed;
    public static double soulArmorFullSetIdleFailsafeMaxHealthFraction;
    public static boolean soulArmorFullSetIdleFailsafeIgnoresVoid;


    @SubscribeEvent
    static void onLoad(ModConfigEvent event) {
        // Fires once per spec the mod owns, so ignore everything that isn't ours.
        if (event.getConfig().getSpec() != SPEC) return;

        loadFromSpec();

        // Forge's file watcher fires this whenever the local toml is edited. While a server's
        // values are in force they still win, otherwise the edit would silently put this client
        // back on its own numbers mid-session and desync the rendering again.
        if (serverSnapshot != null) applyValues(serverSnapshot);
    }

    // Repopulates the cache from this side's own soul_armory-common.toml.
    public static void loadFromSpec() {
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
        soulBowTraceAngle = SOUL_BOW_TRACE_ANGLE.get();
        soulBowTraceRange = SOUL_BOW_TRACE_RANGE.get();
        soulArrowTurnFactor = SOUL_ARROW_TURN_FACTOR.get();
        soulSwordDisableShieldUsage = SOUL_SWORD_DISABLE_SHIELD_USAGE.get();
        soulBowSkillSBRange = SOUL_BOW_SKILL_SONIC_BOOM_RANGE.get();
        soulBowSkillSBConsumption = SOUL_BOW_SKILL_SONIC_BOOM_CONSUMPTION.get();
        soulBowSkillSBDamage = SOUL_BOW_SKILL_SONIC_BOOM_DAMAGE.get();
        soulBowSkillSSConsumption = SOUL_BOW_SKILL_SCATTER_CONSUMPTION.get();
        soulBowSkillSSArrowCount = SOUL_BOW_SKILL_SCATTER_ARROW_COUNT.get();
        soulBowSkillSSSpreadAngle = SOUL_BOW_SKILL_SCATTER_SPREAD_ANGLE.get();
        soulBowSkillSSDamage = SOUL_BOW_SKILL_SCATTER_DAMAGE.get();
        soulBowSkillRFConsumption = SOUL_BOW_SKILL_RAPID_FIRE_CONSUMPTION.get();
        soulBowSkillRFExecuteInterval = SOUL_BOW_SKILL_RAPID_FIRE_EXECUTE_INTERVAL.get();
        soulBowSkillRFDamage = SOUL_BOW_SKILL_RAPID_FIRE_DAMAGE.get();
        soulBowSkillBConsumption = SOUL_BOW_SKILL_BARRAGE_CONSUMPTION.get();
        soulBowSkillBExecuteInterval = SOUL_BOW_SKILL_BARRAGE_EXECUTE_INTERVAL.get();
        soulBowSkillBDamage = SOUL_BOW_SKILL_BARRAGE_DAMAGE.get();
        soulBowSkillBArrowCount = SOUL_BOW_SKILL_BARRAGE_ARROW_COUNT.get();
        soulBowSkillBAngle = SOUL_BOW_SKILL_BARRAGE_SPREAD_ANGLE.get();
        soulSwordOverflowSpeed = SOUL_SWORD_OVERFLOW_SPEED.get();
        soulSwordOverflowThreshold = SOUL_SWORD_OVERFLOW_THRESHOLD.get();
        soulSwordSwitchSkillCost = SOUL_SWORD_SWITCH_SKILL_COST.get();
        soulBowOverflowSpeed = SOUL_BOW_OVERFLOW_SPEED.get();
        soulBowOverflowThreshold = SOUL_BOW_OVERFLOW_THRESHOLD.get();
        soulBowSkillSOConsumption = SOUL_BOW_SKILL_OVERLOAD_CONSUMPTION.get();
        soulBowSkillSOBlastCount = SOUL_BOW_SKILL_OVERLOAD_BLAST_COUNT.get();
        soulBowSkillSOSpreadAngle = SOUL_BOW_SKILL_OVERLOAD_SPREAD_ANGLE.get();
        soulBowSkillSODamage = SOUL_BOW_SKILL_OVERLOAD_DAMAGE.get();
        soulBowSkillSORange = SOUL_BOW_SKILL_OVERLOAD_RANGE.get();
        bladeWaveSpeed = SOUL_SWORD_WAVE_SPEED.get();
        bladeWaveLength = SOUL_SWORD_WAVE_LENGTH.get();
        bladeWaveLifetime = SOUL_SWORD_WAVE_LIFETIME.get();
        bladeWaveCost = SOUL_SWORD_WAVE_COST.get();
        forgingSwordDamageTarget = FORGING_SWORD_DAMAGE_TARGET.get();
        forgingSwordTimeoutTicks = FORGING_SWORD_TIMEOUT_TICKS.get();
        forgingSwordActiveTicks = FORGING_SWORD_ACTIVE_TICKS.get();
        forgingBowDamageTarget = FORGING_BOW_DAMAGE_TARGET.get();
        forgingBowTimeoutTicks = FORGING_BOW_TIMEOUT_TICKS.get();
        forgingBowActiveTicks = FORGING_BOW_ACTIVE_TICKS.get();
        forgingChestplateSonicHits = FORGING_CHESTPLATE_SONIC_HITS.get();
        forgingChestplateTimeoutTicks = FORGING_CHESTPLATE_TIMEOUT_TICKS.get();
        forgingChestplateActiveTicks = FORGING_CHESTPLATE_ACTIVE_TICKS.get();
        forgingChestplateHealFraction = FORGING_CHESTPLATE_HEAL_FRACTION.get();
        swordSpawnChance = INCOMPLETE_SWORD_SPAWN_CHANCE.get();
        bowSpawnChance = INCOMPLETE_BOW_SPAWN_CHANCE.get();
        chestplateSpawnChance = INCOMPLETE_CHESTPLATE_SPAWN_CHANCE.get();
        unlockBladeWaveDamageTarget = UNLOCK_BLADE_WAVE_DAMAGE.get();
        unlockBladeWaveDamageTimeout = UNLOCK_BLADE_WAVE_DAMAGE_TIMEOUT.get();
        unlockBladeWaveHealTarget = UNLOCK_BLADE_WAVE_HEAL.get();
        unlockBladeWaveHealTimeout = UNLOCK_BLADE_WAVE_HEAL_TIMEOUT.get();
        unlockBladeWaveKillTarget = UNLOCK_BLADE_WAVE_WARDEN_KILL.get();
        unlockBladeWaveKillTimeout = UNLOCK_BLADE_WAVE_WARDEN_KILL_TIMEOUT.get();
        unlockSonicOverloadDamageTarget = UNLOCK_SONIC_OVERLOAD_DAMAGE.get();
        unlockSonicOverloadTimeout = UNLOCK_SONIC_OVERLOAD_TIMEOUT.get();
        unlockRapidFireDamageTarget = UNLOCK_RAPID_FIRE_DAMAGE.get();
        unlockRapidFireTimeout = UNLOCK_RAPID_FIRE_TIMEOUT.get();
        unlockBarrageDamageTarget = UNLOCK_BARRAGE_DAMAGE.get();
        unlockBarrageTimeout = UNLOCK_BARRAGE_TIMEOUT.get();
        soulChestplateMaxSoul = SOUL_CHESTPLATE_MAX_SOUL.get();
        soulArmorAdditionalSoul = SOUL_ARMOR_SOUL_ADDITION_PER_PIECE.get();
        soulArmorGracePeriod = SOUL_ARMOR_GRACE_PERIOD.get();
        soulArmorSoulDecaySpeed = SOUL_ARMOR_SOUL_DECAY_SPEED.get();
        soulArmorRageThreshold = SOUL_ARMOR_RAGE_THRESHOLD.get();
        soulArmorRageDamageReduction = SOUL_ARMOR_RAGE_DAMAGE_REDUCTION.get();
        soulArmorRageSoulPerTick = SOUL_ARMOR_RAGE_SOUL_PER_TICK.get();
        soulArmorRageLifestealRatio = SOUL_ARMOR_RAGE_LIFESTEAL_RATIO.get();
        soulArmorAbsorbSonicBoom = SOUL_ARMOR_ABSORB_SONIC_BOOM.get();
        soulArmorSonicBoomSoulReward = SOUL_ARMOR_SONIC_BOOM_SOUL_REWARD.get();
        soulArmorRageNightVisionLinger = SOUL_ARMOR_RAGE_NIGHT_VISION_LINGER.get();
        soulArmorRageSpeedBonus = SOUL_ARMOR_RAGE_SPEED_BONUS.get();
        soulArmorRageStepHeightAddition = SOUL_ARMOR_RAGE_STEP_HEIGHT_ADDITION.get();
        soulArmorRagePoisonImmunity = SOUL_ARMOR_RAGE_POISON_IMMUNITY.get();
        soulArmorRageFallAoeRadius = SOUL_ARMOR_RAGE_FALL_AOE_RADIUS.get();
        soulArmorRageFallAoeDamageMultiplier = SOUL_ARMOR_RAGE_FALL_AOE_DAMAGE_MULTIPLIER.get();
        soulArmorFullSetBonusEnabled = SOUL_ARMOR_FULL_SET_BONUS_ENABLED.get();
        soulArmorFullSetSoulFloor = SOUL_ARMOR_FULL_SET_SOUL_FLOOR.get();
        soulArmorFullSetRegenSpeed = SOUL_ARMOR_FULL_SET_REGEN_SPEED.get();
        soulArmorFullSetIdleFailsafeMaxHealthFraction = SOUL_ARMOR_FULL_SET_IDLE_FAILSAFE_MAX_HEALTH_FRACTION.get();
        soulArmorFullSetIdleFailsafeIgnoresVoid = SOUL_ARMOR_FULL_SET_IDLE_FAILSAFE_IGNORES_VOID.get();
    }

    private static final Logger LOGGER = LogUtils.getLogger();

    // Every cached value above, in an order both sides agree on. The sync walks this list instead
    // of naming values one by one, so a config option added above is synced without touching the
    // packet. Only public static non-final fields qualify, which is exactly the cache: the spec
    // values and SPEC itself are final, and everything else here is private.
    private static final List<Field> SYNCED_FIELDS = collectSyncedFields();

    // Non-null only while a remote server's values are in force. See applySynced.
    private static Map<String, Object> serverSnapshot = null;

    private static List<Field> collectSyncedFields() {
        List<Field> fields = new ArrayList<>();
        for (Field field : Config.class.getFields()) {
            int modifiers = field.getModifiers();
            if (!Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)) continue;
            fields.add(field);
        }
        // Declaration order isn't guaranteed by reflection, so sort to give both sides the same
        // sequence to write and read.
        fields.sort(Comparator.comparing(Field::getName));
        return List.copyOf(fields);
    }

    public static int syncedFieldCount() {
        return SYNCED_FIELDS.size();
    }

    // Fingerprint of the field list. Both sides compute it from their own Config, so a mismatch
    // means the two builds disagree on the layout and the stream can't be read safely.
    public static int fieldSignature() {
        StringBuilder builder = new StringBuilder();
        for (Field field : SYNCED_FIELDS) {
            builder.append(field.getName()).append(':').append(field.getType().getName()).append(';');
        }
        return builder.toString().hashCode();
    }

    public static void writeAll(FriendlyByteBuf buf) {
        for (Field field : SYNCED_FIELDS) {
            Class<?> type = field.getType();
            try {
                if (type == int.class) buf.writeVarInt(field.getInt(null));
                else if (type == double.class) buf.writeDouble(field.getDouble(null));
                else if (type == boolean.class) buf.writeBoolean(field.getBoolean(null));
                // An unsupported type writes nothing, and readAll skips it on the same condition,
                // so the stream stays aligned either way.
                else LOGGER.error("Config value {} has unsupported type {} and will not be synced", field.getName(), type);
            } catch (IllegalAccessException e) {
                LOGGER.error("Could not read config value {} for sync", field.getName(), e);
            }
        }
    }

    public static Map<String, Object> readAll(FriendlyByteBuf buf) {
        Map<String, Object> values = new HashMap<>();
        for (Field field : SYNCED_FIELDS) {
            Class<?> type = field.getType();
            if (type == int.class) values.put(field.getName(), buf.readVarInt());
            else if (type == double.class) values.put(field.getName(), buf.readDouble());
            else if (type == boolean.class) values.put(field.getName(), buf.readBoolean());
        }
        return values;
    }

    // Adopts a remote server's values, and remembers them so a later local file reload doesn't
    // undo the adoption.
    public static void applySynced(Map<String, Object> values) {
        serverSnapshot = values;
        applyValues(values);
    }

    // Drops the remote server's values and goes back to this instance's own config file.
    public static void clearSynced() {
        serverSnapshot = null;
        loadFromSpec();
    }

    private static void applyValues(Map<String, Object> values) {
        for (Field field : SYNCED_FIELDS) {
            Object value = values.get(field.getName());
            if (value == null) continue;
            try {
                field.set(null, value);
            } catch (IllegalAccessException e) {
                LOGGER.error("Could not apply synced config value {}", field.getName(), e);
            }
        }
    }
}
