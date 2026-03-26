package com.iceKube.soulArmory.items;

import com.iceKube.soulArmory.Config;
import com.iceKube.soulArmory.entities.SoulArrowEntity;
import com.iceKube.soulArmory.registries.EntityRegistry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SoulBowItem extends BaseSoulWeaponItem {

    public SoulBowItem(Properties pProperties) {
        super(pProperties);
        doApplySpeedModifier = true;
    }

    @Override
    public int getGracePeriodTicks() {
        return Config.soulBowGracePeriod;
    }

    @Override
    public int getSoulDecaySpeed() {
        return Config.soulBowSoulDecaySpeed;
    }

    @Override
    public int getMaxSoul() {
        return Config.soulBowMaxSoul;
    }

    @Override
    public int getPointPerSpeedPercent() {
        return Config.soulBowPointPerSpeedPercent;
    }

    // -------------------------------------------------------------------------
    // Bow usage, copied and modified from BowItem
    // -------------------------------------------------------------------------

    /**
     * How long (in ticks) the item can be "charged" before auto-firing.
     */
    public int getUseDuration(ItemStack pStack) {
        return 72000;
    }

    /**
     * Called when the player right-clicks — begin drawing the bow.
     * No physical arrow is consumed;
     */
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        // TODO: Sonic boom skill implementation.
        pPlayer.startUsingItem(pHand);
        return InteractionResultHolder.consume(pPlayer.getItemInHand(pHand));
    }

    /**
     * Gets the velocity of the arrow entity from the bow's charge
     */
    public float getPowerForTime(int pCharge) {
        float f = (float) pCharge / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    }

    /**
     * Called when the player releases right-click.
     */
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving, int pTimeLeft) {
        if (!(pEntityLiving instanceof Player player)) return;

        // For every Config.soulBowPointPerDamagePercent points of soul, add 1% of drawing speed, arrow damage and arrow speed.
        double soulMultiplier = (1 + 0.01 * (int) (pStack.getTag().getFloat(soulAmountNBT) / Config.soulBowPointPerDamagePercent));

        int chargedTicks = getUseDuration(pStack) - pTimeLeft;
        chargedTicks /= 2; // Base charge speed for soul bow is half of vanilla bow.
        chargedTicks = (int) (chargedTicks * soulMultiplier); // Apply drawing speed multiplier
        float power = getPowerForTime(chargedTicks);
        if (power < 0.9F) return; // Won't shoot unless charged to max.

        if (!pLevel.isClientSide) {

            double damage = Config.soulBowBaseDamage * soulMultiplier; // Apply arrow damage multiplier

            SoulArrowEntity arrow = new SoulArrowEntity(
                    EntityRegistry.SOUL_ARROW.get(), player, pLevel);
            // Shoot in the direction the player is looking; power * 1.5 is
            // half of the max speed as a fully-charged vanilla bow.
            arrow.shootFromRotation(
                    player, player.getXRot(), player.getYRot(),
                    0.0F, ((float) (power * 1.5F * soulMultiplier)), 1.0F);
            arrow.setBaseDamage(damage);
            pLevel.addFreshEntity(arrow);
        }

        pLevel.playSound(
                null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS,
                1.0F,
                1.0F / (pLevel.getRandom().nextFloat() * 0.4F + 1.2F) + power * 0.5F);
    }
}
