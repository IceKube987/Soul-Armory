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
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SoulBowItem extends BaseSoulWeaponItem {

    public SoulBowItem(Properties pProperties) {
        super(pProperties);
        doApplySpeedModifier = true;
    }

    // -------------------------------------------------------------------------
    // BaseSoulWeaponItem abstract method implementations
    // -------------------------------------------------------------------------

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

    /** How long (in ticks) the item can be "charged" before auto-firing. */
    public int getUseDuration(ItemStack pStack) {
        return 72000;
    }

    /**
     * Called when the player right-clicks — begin drawing the bow.
     * No physical arrow is consumed; Soul Arrows are conjured from soul energy.
     */
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        pPlayer.startUsingItem(pHand);
        return InteractionResultHolder.consume(pPlayer.getItemInHand(pHand));
    }

    /**
     * Gets the velocity of the arrow entity from the bow's charge
     */
    public float getPowerForTime(int pCharge) {
        float f = (float)pCharge / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    }

    /**
     * Called when the player releases right-click.
     * Fires a {@link SoulArrowEntity} whose speed scales with charge time,
     * exactly like a vanilla bow.
     */
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving, int pTimeLeft) {
        if (!(pEntityLiving instanceof Player player)) return;

        int chargedTicks = getUseDuration(pStack) - pTimeLeft;
        float power = getPowerForTime(chargedTicks);
        if (power < 0.1F) return; // too short a draw — don't fire

        if (!pLevel.isClientSide) {
            SoulArrowEntity arrow = new SoulArrowEntity(
                    EntityRegistry.SOUL_ARROW.get(), player, pLevel);
            // Shoot in the direction the player is looking; power * 3.0 is
            // the same max speed as a fully-charged vanilla bow.
            arrow.shootFromRotation(
                    player, player.getXRot(), player.getYRot(),
                    0.0F, power * 3.0F, 1.0F);
            arrow.setBaseDamage(Config.soulBowBaseDamage);
            pLevel.addFreshEntity(arrow);
        }

        pLevel.playSound(
                null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS,
                1.0F,
                1.0F / (pLevel.getRandom().nextFloat() * 0.4F + 1.2F) + power * 0.5F);
    }
}
