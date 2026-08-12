package com.iceKube.soulArmory.soulSkill.skills;

import com.iceKube.soulArmory.Config;
import com.iceKube.soulArmory.SoulArmoryMod;
import com.iceKube.soulArmory.entities.SoulArrowRapidFireEntity;
import com.iceKube.soulArmory.items.SoulBowItem;
import com.iceKube.soulArmory.registries.EntityRegistry;
import com.iceKube.soulArmory.soulSkill.ContinuousSoulSkill;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import static com.iceKube.soulArmory.items.BaseSoulWeaponItem.SOUL_AMOUNT;
import static com.iceKube.soulArmory.items.SoulBowItem.LAST_EXECUTED_TIME;

public class BarrageSkill extends ContinuousSoulSkill {
    public BarrageSkill() {
        super(new ResourceLocation(SoulArmoryMod.MODID, "barrage"),
                new ResourceLocation("textures/item/bow_pulling_2.png"),
                "barrage");
    }

    @Override
    public int getSoulCost() {
        return Config.soulBowSkillBConsumption;
    }

    @Override
    public int getExecuteInterval() {
        return Config.soulBowSkillBExecuteInterval;
    }

    @Override
    public boolean execute(ItemStack stack, Level level, Player player) {
        if (!(stack.getItem() instanceof SoulBowItem soulBowItem)) return false;
        if (stack.getTag() == null || !stack.getTag().contains(LAST_EXECUTED_TIME)) return false;
        if (!stack.getTag().contains(SOUL_AMOUNT)) return false;
        if (stack.getTag().getFloat(SOUL_AMOUNT) < getSoulCost()) return false;

        Long lastExecuted = stack.getTag().getLong(LAST_EXECUTED_TIME);
        Long currentTime = level.getGameTime();
        if (currentTime - lastExecuted < getExecuteInterval()) return false;

        double damage = Config.soulBowSkillBDamage;

        // Get player's look direction
        Vec3 viewVector = player.getViewVector(1.0F);
        Vec3 upVector = player.getUpVector(1.0F);

        // Make sure it is always RIGHT.
        Vec3 rightVector = viewVector.cross(upVector).normalize();

        // Spawn multiple arrows in a spread pattern
        int arrowCount = Config.soulBowSkillBArrowCount;

        double degrees = Config.soulBowSkillBAngle;

        for (int i = 0; i < arrowCount; i++) {
            // Calculate spread angles
            // Distribute arrows evenly within the spread cone
            double offsetFraction = (arrowCount == 1) ? 0 : (double) i / (arrowCount - 1) - 0.5; // -0.5 to 0.5
            double horizontalOffset = offsetFraction * degrees * 2; // horizontal spread in degrees

            double angleRad = Math.toRadians(horizontalOffset);

            Vec3 shootDir = viewVector.scale(Math.cos(angleRad))
                    .add(rightVector.scale(Math.sin(angleRad)));

            // Create and spawn the arrow
            SoulArrowRapidFireEntity arrow = new SoulArrowRapidFireEntity(
                    EntityRegistry.SOUL_ARROW_RAPID_FIRE.get(), player, level);

            arrow.shoot(shootDir.x, shootDir.y, shootDir.z, 3.0F, 0.0F);
            arrow.setBaseDamage(damage);
            arrow.setTarget(soulBowItem.getNearestEntityLookedAt(player, Config.soulBowTraceRange, Config.soulBowTraceAngle));
            level.addFreshEntity(arrow);
        }

        // Play shoot sound
        level.playSound(
                null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS,
                1.0F,
                1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + 0.5F
        );

        return super.execute(stack, level, player);
    }
}
