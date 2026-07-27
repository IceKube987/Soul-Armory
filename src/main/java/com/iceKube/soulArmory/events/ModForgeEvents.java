package com.iceKube.soulArmory.events;

import com.iceKube.soulArmory.Config;
import com.iceKube.soulArmory.SoulArmoryMod;
import com.iceKube.soulArmory.items.*;
import com.iceKube.soulArmory.soulForging.ForgingEventType;
import com.iceKube.soulArmory.soulForging.ForgingTask;
import com.iceKube.soulArmory.utils.ModDamageTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = SoulArmoryMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModForgeEvents {

    private static final String SOUL_AMOUNT_NBT = "soul_armory.soul_weapon.soulAmount";
    private static final UUID SOUL_SPEED_MODIFIER_UUID = UUID.fromString("00929c63-7970-49d5-bd65-43fae58e3b96"); // Randomly generated UUID

    // When player is hurting entity with soul weapons.
    @SubscribeEvent
    public static void onLivingHurt(LivingDamageEvent event) {
        // Check if the source of the damage is a player holding a Soul Weapon
        if (!(event.getSource().getEntity() instanceof Player player)) return;

        ItemStack mainHandItem = player.getMainHandItem();

        // Handle "Deal Damage" forging criterion.
        ForgingDealDamage(event, player, mainHandItem);

        // Soul armor accumulates from damage dealt in any way, skills included, so it has to be
        // credited before the skill damage types are filtered out below. The landing shockwave is
        // the one exception: it is attributed to the player, so paying it back into the pool would
        // let a Soul Rage feed itself.
        if (!event.getSource().is(ModDamageTypes.FALL_SHOCKWAVE)) {
            AddArmorSoulPoints(event, player);
            ApplyRageLifesteal(event, player);
        }

        // Check if the damage is caused by skills.
        if (event.getSource().is(DamageTypes.SONIC_BOOM) || event.getSource().is(ModDamageTypes.SKILL_ARROW) || event.getSource().is(ModDamageTypes.SKILL_DAMAGE))
            return;

        // Handle add soul points for regular soul weapons.
        AddSoulPoints(event, mainHandItem);

        // Activate incomplete weapons
        EntityType<?> targetType = event.getEntity().getType();
        if (targetType == EntityType.WARDEN && mainHandItem.getItem() instanceof BaseIncompleteSoulItem incompleteItem) {
            incompleteItem.activate(mainHandItem);
        }
    }

    /**
     * The soul armor's defensive effects, keyed on the player being hit rather than the one
     * attacking (which is what {@link #onLivingHurt}, despite the name, handles on
     * {@link LivingDamageEvent}).
     * <p>
     * Hooked on {@link LivingHurtEvent} so the Soul Rage reduction lands <em>before</em> armor:
     * {@code LivingEntity.actuallyHurt} fires this first, then applies armor, resistance and
     * protection, then absorption. So the 60% comes off the raw hit and the armor's own mitigation
     * compounds on top of it, and it still covers sources that bypass armor entirely — which is
     * what "global damage reduction" asks for.
     * <p>
     * {@link EventPriority#LOWEST} so the multiplicative reduction is the last thing applied within
     * this phase.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onSoulArmorHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack chestplate = SoulChestplateItem.getWornChestplate(player);
        if (chestplate.isEmpty()) return;

        // A sonic boom charges the chestplate instead of hurting the wearer. The damage is zeroed
        // rather than cancelled so the hit still registers: the Warden's knockback and the camera
        // shake are meant to land regardless, and both happen in hurt() around this call.
        // Zeroing it here also means an absorbed boom no longer eats the wearer's golden hearts,
        // which sit further down actuallyHurt than this event does.
        if (event.getSource().is(DamageTypes.SONIC_BOOM)) {
            SoulChestplateItem.addSoul(player, Config.soulArmorSonicBoomSoulReward);
            if (Config.soulArmorAbsorbSonicBoom) {
                event.setAmount(0);
                return;
            }
        }

        if (SoulChestplateItem.isRaging(chestplate)) {
            event.setAmount((float) (event.getAmount() * (1 - Config.soulArmorRageDamageReduction)));
        }
    }

    /**
     * Soul Boots: no fall damage during Soul Rage, and the impact is passed on to whatever is
     * standing nearby instead.
     */
    @SubscribeEvent
    public static void onSoulArmorFall(LivingFallEvent event) {
        // Fires on the client's own player too — without this the shockwave would be dealt twice.
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (!SoulChestplateItem.isRaging(player)) return;
        if (!SoulChestplateItem.isSlotEquippedWithSoulArmor(player, EquipmentSlot.FEET)) return;

        // Read the fall before cancelling it: this event fires ahead of calculateFallDamage, so
        // the distance is still the untouched one. Mirrors vanilla's own damage formula so the
        // shockwave is worth exactly as much as the fall the boots just absorbed.
        MobEffectInstance jumpBoost = player.getEffect(MobEffects.JUMP);
        float jumpBoostAmplifier = jumpBoost == null ? 0.0F : (float) (jumpBoost.getAmplifier() + 1);
        int fallDamage = Mth.ceil((event.getDistance() - 3.0F - jumpBoostAmplifier) * event.getDamageMultiplier());

        event.setCanceled(true);

        if (fallDamage <= 0) return;

        float shockwaveDamage = (float) (fallDamage * Config.soulArmorRageFallAoeDamageMultiplier);
        if (shockwaveDamage <= 0) return;

        List<LivingEntity> targets = player.level().getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(Config.soulArmorRageFallAoeRadius),
                livingEntity -> livingEntity instanceof Enemy);

        for (LivingEntity target : targets) {
            target.hurt(ModDamageTypes.fallShockwave(player.level(), player), shockwaveDamage);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;

        ItemStack mainHandItem = player.getMainHandItem();
        if (!(mainHandItem.getItem() instanceof Forgeable forgeable)) return;

        ForgingTask task = forgeable.getActiveForgingTask(mainHandItem);
        if (task == null) return;

        CompoundTag tag = mainHandItem.getOrCreateTag();
        EntityType<?> killedType = event.getEntity().getType();

        boolean completed = task.processEvent(tag, ForgingEventType.KILL_ENTITY,
                killedType, 1, player.level().getGameTime());

        if (completed) {
            task.onComplete.execute(player, mainHandItem, player.level());
            task.removeTaskTag(tag);
        }
    }

    // Apply speed modifier if the player is holding a soul weapon that applies speed modifier.
    @SubscribeEvent
    public static void onPlayerTickApplySpeedModifier(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        Player player = event.player;
        AttributeInstance attr = player.getAttributes().getInstance(Attributes.MOVEMENT_SPEED);

        if (attr == null) return;
        if (attr.getModifier(SOUL_SPEED_MODIFIER_UUID) != null) {
            attr.removeModifier(SOUL_SPEED_MODIFIER_UUID);
        }

        // The held weapon and the soul leggings share this one modifier, so their contributions are
        // summed rather than either one winning outright.
        double speedAddition = 0;

        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (stack.getItem() instanceof CanApplySpeedBoost item) {
            speedAddition += item.getSpeedAdditionPercentage(stack);
        }
        speedAddition += SoulChestplateItem.getArmorSpeedAdditionPercentage(player);

        // Nothing to add, and the old modifier is already gone — same outcome as before.
        if (speedAddition <= 0) return;

        double baseSpeed = attr.getBaseValue();
        double currentSpeed = attr.getValue();
        double targetSpeed = currentSpeed * (1 + speedAddition);

        double modifierAmount = Config.speedBoostHasCeil
                ? Math.max(0, Math.min(baseSpeed * Config.speedBoostCeil, targetSpeed))
                : targetSpeed;

        // Subtract current speed because it's an addition modifier.
        modifierAmount -= currentSpeed;

        // otherwise the speed will be negative if the holder is applied too much speed boost effect.
        modifierAmount = Math.max(0, modifierAmount);

        attr.addTransientModifier(new AttributeModifier(
                SOUL_SPEED_MODIFIER_UUID,
                "Soul Speed Modifier",
                modifierAmount,
                AttributeModifier.Operation.ADDITION));

    }

    @SubscribeEvent
    public static void onPlayerRightClick(PlayerInteractEvent event) {
        if (!Config.soulSwordDisableShieldUsage) return;
        if (event.getHand() != InteractionHand.OFF_HAND) return;

        Player player = event.getEntity();
        ItemStack mainHandItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack offHandItem = player.getItemInHand(InteractionHand.OFF_HAND);

        if (!(mainHandItem.getItem() instanceof SoulSwordItem)) return;
        if (!(offHandItem.getItem() instanceof ShieldItem)) return;

        event.setCanceled(true);
    }

    private static void ForgingDealDamage(LivingDamageEvent event, Player player, ItemStack mainHandItem) {
        if (mainHandItem.getItem() instanceof Forgeable forgeable) {
            ForgingTask task = forgeable.getActiveForgingTask(mainHandItem);
            if (task == null) return;

            CompoundTag tag = mainHandItem.getOrCreateTag();
            EntityType<?> targetType = event.getEntity().getType();
            float damage = Math.min(event.getAmount(), event.getEntity().getHealth());
            if (damage <= 0) return;

            boolean completed = task.processEvent(tag, ForgingEventType.DEAL_DAMAGE,
                    targetType, event.getSource(), damage, player.level().getGameTime());

            if (completed) {
                task.onComplete.execute(player, mainHandItem, player.level());
                task.removeTaskTag(tag);
            }
        }
    }

    // Soul armor charges off any damage the wearer deals, no matter what it was dealt with.
    private static void AddArmorSoulPoints(LivingDamageEvent event, Player player) {
        float damageDealt = event.getAmount();
        if (damageDealt <= 0) return;
        damageDealt = Math.min(event.getEntity().getHealth(), damageDealt);

        SoulChestplateItem.addSoul(player, damageDealt);
    }

    private static void ApplyRageLifesteal(LivingDamageEvent event, Player player) {
        if (!SoulChestplateItem.isRaging(player)) return;

        float damageDealt = event.getAmount();
        if (damageDealt <= 0) return;
        damageDealt = Math.min(event.getEntity().getHealth(), damageDealt);

        player.heal((float) (damageDealt / Config.soulArmorRageLifestealRatio));
    }

    private static void AddSoulPoints(LivingDamageEvent event, ItemStack mainHandItem) {
        if (mainHandItem.getItem() instanceof BaseSoulWeaponItem item) {
            // Add soul equal to the damage dealt, capped at maxSoul
            float damageDealt = event.getAmount();
            if (damageDealt <= 0) return;
            damageDealt = Math.min(event.getEntity().getHealth(), damageDealt);

            CompoundTag tag = mainHandItem.getOrCreateTag();
            float currentSoul = tag.getFloat(BaseSoulWeaponItem.SOUL_AMOUNT);
            float newSoul = Math.min(item.getMaxSoul(), currentSoul + damageDealt);
            tag.putFloat(BaseSoulWeaponItem.SOUL_AMOUNT, newSoul);
        }
    }
}
