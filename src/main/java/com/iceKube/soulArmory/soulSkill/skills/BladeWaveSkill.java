package com.iceKube.soulArmory.soulSkill.skills;

import com.iceKube.soulArmory.Config;
import com.iceKube.soulArmory.SoulArmoryMod;
import com.iceKube.soulArmory.entities.BladeWaveEntity;
import com.iceKube.soulArmory.items.SoulSwordItem;
import com.iceKube.soulArmory.soulSkill.InstantSoulSkill;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import static com.iceKube.soulArmory.items.BaseSoulWeaponItem.SOUL_AMOUNT;

// Skill for Soul Sword
public class BladeWaveSkill extends InstantSoulSkill {
    public BladeWaveSkill() {
        super(new ResourceLocation(SoulArmoryMod.MODID, "blade_wave"),
                new ResourceLocation("textures/item/iron_sword.png"),
                "blade_wave",
                Config.bladeWaveCost);
    }

    @Override
    public boolean execute(ItemStack stack, Level level, Player player) {
        if (!(stack.getItem() instanceof SoulSwordItem)) return false;
        if (stack.getTag() == null) return false;
        if (!stack.getTag().contains(SOUL_AMOUNT)) return false;
        if (stack.getTag().getFloat(SOUL_AMOUNT) < soulCost) return false;
        if (player.getAttackStrengthScale(0f) < 0.95f) return false;

        float effectiveSoul = Math.min(stack.getTag().getFloat(SOUL_AMOUNT) + 100, Config.soulSwordMaxSoul);

        float damage = (float) (Config.soulSwordBaseDamage + (int) (effectiveSoul / Config.soulSwordPointsPerDamage));

        // Get the direction the player is looking
        Vec3 direction = player.getLookAngle();

        // Do not cast skill if the player is looking directly up or down
        if (direction.y == 1) return false;

        // Create the entity
        BladeWaveEntity wave = BladeWaveEntity.create(player, direction, level, damage);

        // Add it to the world
        level.addFreshEntity(wave);

        player.resetAttackStrengthTicker();
        player.swing(InteractionHand.MAIN_HAND, true);

        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP,
                SoundSource.PLAYERS,
                1.0F,
                0.8F + player.getRandom().nextFloat() * 0.4F
        );

        return super.execute(stack, level, player);
    }
}
