package com.iceKube.soulArmory.soulSkill;

import com.iceKube.soulArmory.soulSkill.skills.*;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
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
    public static final BaseSoulSkill RAPID_FIRE = new RapidFireSkill();
    public static final BaseSoulSkill BARRAGE = new BarrageSkill();
    public static final BaseSoulSkill SONIC_OVERLOAD = new SonicOverloadSkill();
    public static final BaseSoulSkill HEAL = new HealSkill();
    public static final BaseSoulSkill BLADE_WAVE = new BladeWaveSkill();

    public static void registerSoulSkills() {
        register(SONIC_BOOM);
        register(SCATTER_SHOT);
        register(RAPID_FIRE);
        register(BARRAGE);
        register(SONIC_OVERLOAD);
        register(HEAL);
        register(BLADE_WAVE);
    }
}
