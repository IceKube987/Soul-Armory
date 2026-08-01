package com.iceKube.soulArmory.soulForging.tasks;

import com.iceKube.soulArmory.SoulArmoryMod;
import com.iceKube.soulArmory.soulForging.ForgingCriterion;
import com.iceKube.soulArmory.soulForging.ForgingEventType;
import com.iceKube.soulArmory.soulForging.ForgingTask;
import com.iceKube.soulArmory.soulForging.SkillUnlockHelper;
import com.iceKube.soulArmory.soulSkill.SoulSkills;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class UnlockBarrageSkill extends ForgingTask {
    public UnlockBarrageSkill() {
        super(new ResourceLocation(SoulArmoryMod.MODID, "unlock_barrage"),
                List.of(
                        new ForgingCriterion("deal_damage", ForgingEventType.DEAL_DAMAGE,
                                500,
                                0,
                                null,
                                null,
                                null)
                ),
                (player, stack, level) -> SkillUnlockHelper.unlockSkill(player, stack, level, SoulSkills.BARRAGE),
                SoulSkills.BARRAGE,
                false);
    }
}
