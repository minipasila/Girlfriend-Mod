package com.beckytidus.girlfriendmod.client;

import com.beckytidus.girlfriendmod.gui.AIConfigScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class KeyInputHandler {
    public static KeyBinding configKey;

    public static void register() {
        // Create a Category object using Identifier.of (New 1.21+ API)
        KeyBinding.Category category = KeyBinding.Category.create(
            Identifier.of("girlfriend-mod", "general")
        );

        // Use the constructor: (String translationKey, InputUtil.Type type, int code, KeyBinding.Category category)
        configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.girlfriend-mod.config",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (configKey.wasPressed()) {
                client.setScreen(new AIConfigScreen(client.currentScreen));
            }
        });
    }
}