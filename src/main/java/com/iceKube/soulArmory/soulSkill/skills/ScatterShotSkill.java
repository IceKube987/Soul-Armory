package com.iceKube.soulArmory.soulSkill.skills;

import com.iceKube.soulArmory.Config;
import com.iceKube.soulArmory.SoulArmoryMod;
import com.iceKube.soulArmory.entities.SoulArrowScatterEntity;
import com.iceKube.soulArmory.items.SoulBowItem;
import com.iceKube.soulArmory.registries.EntityRegistry;
import com.iceKube.soulArmory.soulSkill.InstantSoulSkill;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import static com.iceKube.soulArmory.items.BaseSoulWeaponItem.soulAmountNBT;

public class ScatterShotSkill extends InstantSoulSkill {
    public ScatterShotSkill() {
        super(new ResourceLocation(SoulArmoryMod.MODID, "scatter_shot"),
                new ResourceLocation("textures/item/arrow.png"),
                "scatter_shot",
                Config.soulBowSkillSSConsumption
        );
    }

    @Override
    public boolean execute(ItemStack stack, Level level, Player player) {
        if (!(stack.getItem() instanceof SoulBowItem)) return false;
        if (stack.getTag() == null) return false;
        if (!stack.getTag().contains(soulAmountNBT)) return false;
        if (stack.getTag().getFloat(soulAmountNBT) < soulCost) return false;

        double damage = Config.soulBowSkillSSDamage;

        // Get player's look direction
        Vec3 viewVector = player.getViewVector(1.0F);
        Vec3 upVector = player.getUpVector(1.0F);

        Vec3 rightVector = viewVector.cross(upVector).normalize();

        // Spawn multiple arrows in a spread pattern
        int arrowCount = Config.soulBowSkillSSArrowCount;

        for (int i = 0; i < arrowCount; i++) {
            // Calculate spread angles
            // Distribute arrows evenly
            double offsetFraction = (arrowCount == 1) ? 0 : (double) i / (arrowCount - 1) - 0.5; // -0.5 to 0.5
            double horizontalOffset = offsetFraction * Config.soulBowSkillSSSpreadAngle * 2; // horizontal spread in degrees

            double angleRad = Math.toRadians(horizontalOffset);

            Vec3 shootDir = viewVector.scale(Math.cos(angleRad))
                    .add(rightVector.scale(Math.sin(angleRad)));

            // Create and spawn the arrow
            SoulArrowScatterEntity arrow = new SoulArrowScatterEntity(
                    EntityRegistry.SOUL_ARROW_SCATTER.get(), player, level);

            arrow.shoot(shootDir.x, shootDir.y, shootDir.z, 3.0F, 0.0F);
            arrow.setBaseDamage(damage);
            level.addFreshEntity(arrow);
        }

        // Play shoot sound
        level.playSound(
                null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS,
                1.0F,
                1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + 0.5F
        );

        // Consume soul
        return super.execute(stack, level, player);
    }
}
