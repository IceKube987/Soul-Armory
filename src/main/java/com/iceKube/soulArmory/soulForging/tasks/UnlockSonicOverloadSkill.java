package com.iceKube.soulArmory.soulForging.tasks;

import com.iceKube.soulArmory.Config;
import com.iceKube.soulArmory.SoulArmoryMod;
import com.iceKube.soulArmory.soulForging.ForgingCriterion;
import com.iceKube.soulArmory.soulForging.ForgingEventType;
import com.iceKube.soulArmory.soulForging.ForgingTask;
import com.iceKube.soulArmory.soulForging.SkillUnlockHelper;
import com.iceKube.soulArmory.soulSkill.SoulSkills;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageTypes;

import java.util.List;

public class UnlockSonicOverloadSkill extends ForgingTask {
    public UnlockSonicOverloadSkill() {
        super(new ResourceLocation(SoulArmoryMod.MODID, "unlock_sonic_overload"),
                List.of(
                        new ForgingCriterion("deal_damage_sonic", ForgingEventType.DEAL_DAMAGE,
                                Config.unlockSonicOverloadDamageTarget,
                                Config.unlockSonicOverloadTimeout,
                                null,
                                damageType -> damageType.is(DamageTypes.SONIC_BOOM),
                                null)
                ),
                (player, stack, level) -> SkillUnlockHelper.unlockSkill(player, stack, level, SoulSkills.SONIC_OVERLOAD),
                SoulSkills.SONIC_OVERLOAD,
                false);
    }
}
