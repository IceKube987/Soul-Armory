package com.iceKube.soulArmory.soulSkill.skills;

import com.iceKube.soulArmory.Config;
import com.iceKube.soulArmory.SoulArmoryMod;
import com.iceKube.soulArmory.items.Forgeable;
import com.iceKube.soulArmory.registries.SoundRegistry;
import com.iceKube.soulArmory.soulForging.ForgingEventType;
import com.iceKube.soulArmory.soulForging.ForgingTask;
import com.iceKube.soulArmory.soulSkill.InstantSoulSkill;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SculkChargeParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

import static com.iceKube.soulArmory.items.BaseSoulWeaponItem.SOUL_AMOUNT;

// Healing Skill for Soul Sword
public class HealSkill extends InstantSoulSkill {
    public HealSkill() {
        super(new ResourceLocation(SoulArmoryMod.MODID, "heal"),
                new ResourceLocation(SoulArmoryMod.MODID, "textures/icon/soul_heal.png"),
                "heal",
                0); // dynamically deduce soul
    }

    @Override
    public boolean execute(ItemStack stack, Level level, Player player) {
        if (stack.getTag() == null) return false;

        CompoundTag tag = stack.getTag();
        float currentSoul = tag.getFloat(SOUL_AMOUNT);

        float currentHealth = player.getHealth();
        float maxHealth = player.getMaxHealth();
        float healthMissing = maxHealth - currentHealth;

        if (healthMissing <= 0 || currentSoul <= 0) return false;

        // How many HP we can afford to heal with available soul
        int affordableHeal = (int) (currentSoul / Config.soulSwordPointsPerHealing);

        // Actual healing: min of what's needed (rounded up to nearest int) and what we can afford
        int healingAmount = (int) Math.min(Math.ceil(healthMissing), affordableHeal);

        if (healingAmount <= 0) return false;

        // Deduct soul
        tag.putFloat(SOUL_AMOUNT, currentSoul - (float) (healingAmount * Config.soulSwordPointsPerHealing));

        // Apply healing
        player.heal(healingAmount);

        handleHealTask(player, stack, healingAmount);

        playParticle(level, player);

        playSound(level, player);

        return true; // dynamically deduce soul, not use super()
    }

    private void playParticle(Level level, Player player) {
        AABB aabb = player.getBoundingBox().inflate(0.5, 0, 0.5);
        Random random = new Random();
        double xSize = aabb.getXsize();
        double ySize = aabb.getYsize();
        double zSize = aabb.getZsize();
        Vec3 center = aabb.getCenter();

        // add Sculk Charge Particles
        for (int i = 0; i < 15; i++) {
            double xOffset = xSize * random.nextFloat(1);
            double yOffset = ySize * 0.25 * random.nextFloat(1);
            double zOffset = zSize * random.nextFloat(1);

            level.addParticle(new SculkChargeParticleOptions(0), aabb.minX + xOffset, aabb.minY + yOffset, aabb.minZ + zOffset, 0, 0, 0);
        }

        // add Sculk Pop Particles
        for (int i = 0; i < 30; i++) {
            double xOffset = xSize * random.nextFloat(1);
            double yOffset = (ySize * 0.5 * random.nextFloat(1)) + (ySize * 0.25);
            double zOffset = zSize * random.nextFloat(1);

            Vec3 particlePos = new Vec3(aabb.minX + xOffset, aabb.minY + yOffset, aabb.minZ + zOffset);
            Vec3 speedVector = particlePos.vectorTo(center).scale(0.1);

            level.addParticle(ParticleTypes.SCULK_CHARGE_POP, particlePos.x, particlePos.y, particlePos.z, speedVector.x, speedVector.y, speedVector.z);
        }

        // add Sculk Soul and Soul Flame Particles
        for (int i = 0; i < 10; i++) {
            double xOffset = xSize * random.nextFloat(1);
            double yOffset = ySize * random.nextFloat(1);
            double zOffset = zSize * random.nextFloat(1);

            level.addParticle(ParticleTypes.SCULK_SOUL, aabb.minX + xOffset, aabb.minY + yOffset, aabb.minZ + zOffset, 0, 0.05, 0);
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, aabb.minX + xOffset, aabb.minY + yOffset, aabb.minZ + zOffset, 0, 0, 0);
        }
    }

    private void playSound(Level level, Player player) {
        level.playSound(null, player.getOnPos(), SoundRegistry.SOUL_SWORD_HEAL.get(), SoundSource.PLAYERS, 1f, 1f);
    }

    private void handleHealTask(Player player, ItemStack stack, float amount) {
        if (stack.getItem() instanceof Forgeable forgeable) {
            ForgingTask task = forgeable.getActiveForgingTask(stack);
            if (task == null) return;

            CompoundTag tag = stack.getOrCreateTag();

            boolean completed = task.processEvent(tag, ForgingEventType.SKILL,
                    null, null, this,
                    amount, player.level().getGameTime());

            if (completed) {
                task.onComplete.execute(player, stack, player.level());
                task.removeTaskTag(tag);
            }
        }
    }
}
