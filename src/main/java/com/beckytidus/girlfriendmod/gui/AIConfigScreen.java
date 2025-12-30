package com.beckytidus.girlfriendmod.gui;

import com.beckytidus.girlfriendmod.config.ModConfig;
import com.beckytidus.girlfriendmod.network.ModNetwork;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class AIConfigScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget apiKeyField;
    private TextFieldWidget modelField;
    private TextFieldWidget tokenField;

    public AIConfigScreen(Screen parent) {
        super(Text.literal("Girlfriend AI Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = 40;

        // API Key
        this.addDrawableChild(ButtonWidget.builder(Text.literal("API Key:"), b -> {}).dimensions(centerX - 100, y - 15, 200, 15).build()).active = false;
        apiKeyField = new TextFieldWidget(this.textRenderer, centerX - 100, y, 200, 20, Text.literal("API Key"));
        apiKeyField.setMaxLength(256);
        apiKeyField.setText(ModConfig.get().apiKey);
        this.addDrawableChild(apiKeyField);

        y += 45;
        // Model
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Model:"), b -> {}).dimensions(centerX - 100, y - 15, 200, 15).build()).active = false;
        modelField = new TextFieldWidget(this.textRenderer, centerX - 100, y, 200, 20, Text.literal("Model"));
        modelField.setText(ModConfig.get().modelName);
        this.addDrawableChild(modelField);

        y += 45;
        // Max Tokens
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Max History Tokens:"), b -> {}).dimensions(centerX - 100, y - 15, 200, 15).build()).active = false;
        tokenField = new TextFieldWidget(this.textRenderer, centerX - 100, y, 200, 20, Text.literal("Tokens"));
        tokenField.setText(String.valueOf(ModConfig.get().maxHistoryTokens));
        this.addDrawableChild(tokenField);

        y += 40;
        // Clear History Button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Clear All Memories"), button -> {
            ModNetwork.sendClearMemoryPacket();
        }).dimensions(centerX - 100, y, 200, 20).build());

        y += 30;
        // Save & Exit
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Save & Exit"), button -> {
            saveConfig();
            this.client.setScreen(parent);
        }).dimensions(centerX - 100, y, 200, 20).build());
    }

    private void saveConfig() {
        ModConfig config = ModConfig.get();
        config.apiKey = apiKeyField.getText();
        config.modelName = modelField.getText();
        try {
            config.maxHistoryTokens = Integer.parseInt(tokenField.getText());
        } catch (NumberFormatException ignored) {}
        ModConfig.save();
        
        // Sync to server
        ModNetwork.sendConfigUpdatePacket(config);
    }
    
    @Override
    public void close() {
        this.client.setScreen(parent);
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Fix: Use manual fill instead of renderBackground to avoid "Can only blur once per frame" crash in 1.21+
        // 0xA0000000 is a semi-transparent black
        context.fill(0, 0, this.width, this.height, 0xA0000000);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }
}