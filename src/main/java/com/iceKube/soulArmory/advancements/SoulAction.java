package com.iceKube.soulArmory.advancements;

import com.google.gson.JsonSyntaxException;

public enum SoulAction {
    INCOMPLETE_HIT_WARDEN("incomplete_hit_warden"),
    FORGING_COMPLETE("forging_complete"),
    FULL_SET_CONVERTED("full_set_converted"),
    ABSORB_SONIC_BOOM("absorb_sonic_boom"),
    IRON_GOLEM_OVERKILL("iron_golem_overkill"),
    WARDEN_SONIC_HIT("warden_sonic_hit"),
    WARDEN_SONIC_KILL("warden_sonic_kill"),
    FAILSAFE_SAVED_FROM_CREEPER("failsafe_saved_from_creeper"),
    SWORD_HEAL("sword_heal"),
    SKILL_UNLOCKED("skill_unlocked"),
    BLADE_WAVE_UNLOCKED("blade_wave_unlocked"),
    BOW_ALL_SKILLS_UNLOCKED("bow_all_skills_unlocked"),
    DRAGON_FLAK("dragon_flak"),
    DRAGON_KILLED_AFTER_REVIVAL("dragon_killed_after_revival");

    private final String serializedName;

    SoulAction(String serializedName) {
        this.serializedName = serializedName;
    }

    public String getSerializedName() {
        return serializedName;
    }

    /**
     * Throws rather than returning null so a misspelled action in an advancement JSON fails loudly
     * at datapack load instead of quietly never firing.
     */
    public static SoulAction byName(String name) {
        for (SoulAction action : values()) {
            if (action.serializedName.equals(name)) return action;
        }
        throw new JsonSyntaxException("Unknown soul_armory soul action '" + name + "'");
    }
}
