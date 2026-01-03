package com.beckytidus.girlfriendmod.client;

import com.beckytidus.girlfriendmod.gui.AIConfigScreen;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class PauseMenuIntegration {
    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (screen instanceof GameMenuScreen) {
                // Calculate position - usually below "Options" and "Advancements"
                int buttonY = height / 4 + 120 + 24; // Position it below the standard buttons

                Screens.getButtons(screen).add(
                    ButtonWidget.builder(Text.literal("Girlfriend AI Settings"), button -> {
                        client.setScreen(new AIConfigScreen(screen));
                    }).dimensions(width / 2 - 102, buttonY, 204, 20).build()
                );
            }
        });
    }
}
