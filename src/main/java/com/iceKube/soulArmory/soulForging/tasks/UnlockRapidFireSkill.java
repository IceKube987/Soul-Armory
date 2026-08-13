package com.iceKube.soulArmory.soulForging.tasks;

import com.iceKube.soulArmory.Config;
import com.iceKube.soulArmory.SoulArmoryMod;
import com.iceKube.soulArmory.soulForging.ForgingCriterion;
import com.iceKube.soulArmory.soulForging.ForgingEventType;
import com.iceKube.soulArmory.soulForging.ForgingTask;
import com.iceKube.soulArmory.soulForging.SkillUnlockHelper;
import com.iceKube.soulArmory.soulSkill.SoulSkills;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class UnlockRapidFireSkill extends ForgingTask {
    public UnlockRapidFireSkill() {
        super(new ResourceLocation(SoulArmoryMod.MODID, "unlock_rapid_fire"),
                List.of(
                        new ForgingCriterion("deal_damage_rapid", ForgingEventType.DEAL_DAMAGE,
                                Config.unlockRapidFireDamageTarget,
                                Config.unlockRapidFireTimeout,
                                null,
                                null,
                                null)
                ),
                (player, stack, level) -> SkillUnlockHelper.unlockSkill(player, stack, level, SoulSkills.RAPID_FIRE),
                SoulSkills.RAPID_FIRE,
                false);
    }
}
