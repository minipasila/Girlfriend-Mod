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
    private TextFieldWidget nameField;
    private TextFieldWidget tokenField;
    private TextFieldWidget minPField;
    private TextFieldWidget tempField;

    public AIConfigScreen(Screen parent) {
        super(Text.literal("Girlfriend AI Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = 25; 
        int labelHeight = 12;
        int fieldHeight = 18;
        int gap = 34;

        // API Key
        this.addDrawableChild(ButtonWidget.builder(Text.literal("API Key:"), b -> {}).dimensions(centerX - 100, y - labelHeight, 200, labelHeight).build()).active = false;
        apiKeyField = new TextFieldWidget(this.textRenderer, centerX - 100, y, 200, fieldHeight, Text.literal("API Key"));
        apiKeyField.setMaxLength(256);
        apiKeyField.setText(ModConfig.get().apiKey);
        this.addDrawableChild(apiKeyField);

        y += gap;
        // Model
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Model:"), b -> {}).dimensions(centerX - 100, y - labelHeight, 200, labelHeight).build()).active = false;
        modelField = new TextFieldWidget(this.textRenderer, centerX - 100, y, 200, fieldHeight, Text.literal("Model"));
        modelField.setText(ModConfig.get().modelName);
        this.addDrawableChild(modelField);

        y += gap;
        // Name
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Girlfriend Name:"), b -> {}).dimensions(centerX - 100, y - labelHeight, 200, labelHeight).build()).active = false;
        nameField = new TextFieldWidget(this.textRenderer, centerX - 100, y, 200, fieldHeight, Text.literal("Name"));
        nameField.setText(ModConfig.get().customName);
        this.addDrawableChild(nameField);

        y += gap;
        // Temperature & Min P (Split Row)
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Temp (0.0-2.0) / Min P (0.0-1.0):"), b -> {}).dimensions(centerX - 100, y - labelHeight, 200, labelHeight).build()).active = false;
        
        tempField = new TextFieldWidget(this.textRenderer, centerX - 100, y, 98, fieldHeight, Text.literal("Temp"));
        tempField.setText(String.valueOf(ModConfig.get().temperature));
        this.addDrawableChild(tempField);

        minPField = new TextFieldWidget(this.textRenderer, centerX + 2, y, 98, fieldHeight, Text.literal("Min P"));
        minPField.setText(String.valueOf(ModConfig.get().minP));
        this.addDrawableChild(minPField);

        y += gap;
        // Max Tokens
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Max History Tokens:"), b -> {}).dimensions(centerX - 100, y - labelHeight, 200, labelHeight).build()).active = false;
        tokenField = new TextFieldWidget(this.textRenderer, centerX - 100, y, 200, fieldHeight, Text.literal("Tokens"));
        tokenField.setText(String.valueOf(ModConfig.get().maxHistoryTokens));
        this.addDrawableChild(tokenField);

        y += 35;
        // Clear History Button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Clear All Memories"), button -> {
            ModNetwork.sendClearMemoryPacket();
        }).dimensions(centerX - 100, y, 200, 20).build());

        y += 25;
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
        config.customName = nameField.getText();
        
        try {
            config.maxHistoryTokens = Integer.parseInt(tokenField.getText());
        } catch (NumberFormatException ignored) {}
        
        try {
            config.minP = Double.parseDouble(minPField.getText());
        } catch (NumberFormatException ignored) {}

        try {
            config.temperature = Double.parseDouble(tempField.getText());
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
        context.fill(0, 0, this.width, this.height, 0xA0000000);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 8, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }
}