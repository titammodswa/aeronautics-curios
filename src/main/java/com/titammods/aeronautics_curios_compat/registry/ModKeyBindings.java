package com.titammods.aeronautics_curios_compat.registry;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class ModKeyBindings {

    public static final String CATEGORY = "key.categories.aeronautics_curios_compat";

    public static KeyMapping REMOTE_USE;

    public static void register() {
        REMOTE_USE = new KeyMapping(
                "key.aeronautics_curios_compat.remote_use",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_Y,
                CATEGORY
        );
    }
}