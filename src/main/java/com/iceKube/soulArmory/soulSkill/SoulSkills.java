package com.iceKube.soulArmory.soulSkill;

import com.iceKube.soulArmory.soulSkill.skills.ScatterShotSkill;
import com.iceKube.soulArmory.soulSkill.skills.SonicBoomSkill;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SoulSkills {
    private static final Map<ResourceLocation, BaseSoulSkill> SKILLS = new HashMap<>();

    public static void register(BaseSoulSkill skill) {
        ResourceLocation id = skill.soulSkillId;
        if (SKILLS.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate Soul Skill registered! ID: " + id);
        }
        SKILLS.put(id, skill);
    }

    @Nullable
    public static BaseSoulSkill getSkill(ResourceLocation id) {
        return SKILLS.get(id);
    }

    // ------
    // Registered skills.
    // ------
    public static final BaseSoulSkill SONIC_BOOM = new SonicBoomSkill();
    public static final BaseSoulSkill SCATTER_SHOT = new ScatterShotSkill();

    public static void registerSoulSkills() {
        register(SONIC_BOOM);
        register(SCATTER_SHOT);
    }
}
