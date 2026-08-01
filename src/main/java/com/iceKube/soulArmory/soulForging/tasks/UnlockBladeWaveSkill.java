package com.iceKube.soulArmory.soulForging.tasks;

import com.iceKube.soulArmory.Config;
import com.iceKube.soulArmory.SoulArmoryMod;
import com.iceKube.soulArmory.soulForging.ForgingCriterion;
import com.iceKube.soulArmory.soulForging.ForgingEventType;
import com.iceKube.soulArmory.soulForging.ForgingTask;
import com.iceKube.soulArmory.soulForging.SkillUnlockHelper;
import com.iceKube.soulArmory.soulSkill.SoulSkills;
import com.iceKube.soulArmory.soulSkill.skills.HealSkill;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.List;

public class UnlockBladeWaveSkill extends ForgingTask {
    public UnlockBladeWaveSkill() {
        super(new ResourceLocation(SoulArmoryMod.MODID, "unlock_blade_wave"),
                List.of(
                        new ForgingCriterion("deal_damage", ForgingEventType.DEAL_DAMAGE,
                                Config.unlockBladeWaveDamageTarget,
                                Config.unlockBladeWaveDamageTimeout,
                                null,
                                null,
                                null),
                        new ForgingCriterion("heal", ForgingEventType.SKILL,
                                Config.unlockBladeWaveHealTarget,
                                Config.unlockBladeWaveHealTimeout,
                                null,
                                null,
                                skill -> skill instanceof HealSkill),
                        new ForgingCriterion("kill_wardens_in_time", ForgingEventType.KILL_ENTITY,
                                Config.unlockBladeWaveKillTarget,
                                Config.unlockBladeWaveKillTimeout,
                                entityType -> entityType == EntityType.WARDEN,
                                null,
                                null)
                ),
                (player, stack, level) -> SkillUnlockHelper.unlockSkill(player, stack, level, SoulSkills.BLADE_WAVE),
                SoulSkills.BLADE_WAVE,
                false);
    }
}
