package com.iceKube.soulArmory.utils;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyBinding {

    public static final String KEY_CATEGORY = "key.category.soul_armory";
    public static final String KEY_TEST = "key.soul_armory.test";
    public static final String KEY_SWITCH_SKILL = "key.soul_armory.switch_skill";
    public static final String KEY_ACTIVATE_RAGE = "key.soul_armory.activate_rage";

    public static final KeyMapping TEST_KEY = new KeyMapping(KEY_TEST, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, KEY_CATEGORY);

    public static final KeyMapping SWITCH_SKILL = new KeyMapping(KEY_SWITCH_SKILL, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, KEY_CATEGORY);

    public static final KeyMapping ACTIVATE_RAGE = new KeyMapping(KEY_ACTIVATE_RAGE, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, KEY_CATEGORY);

}
