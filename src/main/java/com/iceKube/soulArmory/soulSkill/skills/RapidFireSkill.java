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

import static com.iceKube.soulArmory.items.BaseSoulWeaponItem.SOUL_AMOUNT;
import static com.iceKube.soulArmory.items.SoulBowItem.LAST_EXECUTED_TIME;

public class RapidFireSkill extends ContinuousSoulSkill {
    public RapidFireSkill() {
        super(new ResourceLocation(SoulArmoryMod.MODID, "rapid_fire"),
                new ResourceLocation("textures/item/bow.png"),
                "rapid_fire");
    }

    @Override
    public int getSoulCost() {
        return Config.soulBowSkillRFConsumption;
    }

    @Override
    public int getExecuteInterval() {
        return Config.soulBowSkillRFExecuteInterval;
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

        if (!level.isClientSide) {

            double damage = Config.soulBowSkillRFDamage;

            SoulArrowRapidFireEntity arrow = new SoulArrowRapidFireEntity(
                    EntityRegistry.SOUL_ARROW_RAPID_FIRE.get(), player, level);

            // Shoot in the direction the player is looking; power * 3.0 is
            // the max speed as a fully-charged vanilla bow.
            arrow.shootFromRotation(
                    player, player.getXRot(), player.getYRot(),
                    0.0F, 3.0f, 0.3f);
            arrow.setBaseDamage(damage);
            arrow.setTarget(soulBowItem.getNearestEntityLookedAt(player, Config.soulBowTraceRange, Config.soulBowTraceAngle));
            level.addFreshEntity(arrow);
        }

        level.playSound(
                null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS,
                1.0F,
                1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + 0.5F);

        return super.execute(stack, level, player);
    }
}
