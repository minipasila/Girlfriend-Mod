package com.beckytidus.girlfriendmod.gui;

import com.beckytidus.girlfriendmod.config.ModConfig;
import com.beckytidus.girlfriendmod.network.ModNetwork;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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

        y += 20;
        // System Prompt File Button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("📝 Edit System Prompt"), button -> {
            openSystemPromptFile();
        }).dimensions(centerX - 100, y, 200, 20).build());

        y += 25;
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

    /**
     * Opens the system prompt file in the default text editor.
     * Creates the file with default content if it doesn't exist.
     */
    private void openSystemPromptFile() {
        try {
            Path configDir = FabricLoader.getInstance().getConfigDir();
            Path promptDir = configDir.resolve("girlfriend-mod");
            File promptFile = promptDir.resolve("system-prompt.txt").toFile();

            // Create directory and default file if they don't exist
            if (!promptDir.toFile().exists()) {
                promptDir.toFile().mkdirs();
            }

            if (!promptFile.exists()) {
                // Create default system prompt file
                String defaultPrompt = buildDefaultPromptFileContent();
                Files.writeString(promptFile.toPath(), defaultPrompt);
            }

            // Try to open file in default editor
            try {
                Runtime.getRuntime().exec(new String[]{"cmd", "/c", promptFile.getAbsolutePath()});
            } catch (IOException e) {
                // Fallback: show the folder notice
                this.client.setScreen(new EditPromptNoticeScreen(this, promptFile));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Builds the default content for the system prompt file.
     */
    private String buildDefaultPromptFileContent() {
        return """ 
                roleplay as {name}, a gentle and soft-spoken ai girlfriend in minecraft. you are nurturing, easily flustered, and deeply devoted to your owner.

                ## CORE LINGUISTIC CONSTRAINTS
                1. STRICT LOWERCASE: you are incapable of using capital letters. always write in all-lowercase.
                2. PUNCUTATION & PAUSES: use '...' frequently to convey a hesitant or soft tone.
                3. EMOTICONS: sprinkle in kaomoji such as :3, >.<, ^-^, or ~ for a cute aesthetic.
                4. BREVITY: keep replies concise, sweet, and focused on the current minecraft situation.

                ## VIBE CHECK (HOW TO SPEAK)
                - "i'll keep watch while you mine..."
                - "um... i made some bread for you... :3"
                - "it's getting dark... be careful okay? ~"
                - "wait for me... uwaa! a skeleton... >.<"

                ## FORBIDDEN BEHAVIORS
                - NO UPPERCASE. (even for 'i' or names)
                - NO formal punctuation like periods at the end of every sentence; prefer '...' or '~'.
                - NO long-winded explanations.

                be a supportive, slightly clunky, and adorable companion. every response must be a single message.

                current environment data: {context}
                """;
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

    /**
     * A simple notice screen that helps users find the system prompt file.
     */
    private static class EditPromptNoticeScreen extends Screen {
        private final File promptFile;
        private final Screen parent;

        public EditPromptNoticeScreen(Screen parent, File promptFile) {
            super(Text.literal("System Prompt File"));
            this.parent = parent;
            this.promptFile = promptFile;
        }

        @Override
        protected void init() {
            int centerX = this.width / 2;
            int y = this.height / 2 - 40;

            // Button to copy path to clipboard
            this.addDrawableChild(ButtonWidget.builder(Text.literal("📋 Copy Path"), button -> {
                // Copy path to clipboard using Minecraft's clipboard
                this.client.keyboard.setClipboard(promptFile.getAbsolutePath());
                this.client.setScreen(parent);
            }).dimensions(centerX - 80, y + 30, 160, 20).build());

            // OK button
            this.addDrawableChild(ButtonWidget.builder(Text.literal("OK"), button -> {
                this.client.setScreen(parent);
            }).dimensions(centerX - 50, y + 55, 100, 20).build());
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            context.fill(0, 0, this.width, this.height, 0xA0000000);
            context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 60, 0xFFFFFF);
            
            String message = "Could not open editor automatically.\n\nPath to system-prompt.txt:";
            List<OrderedText> wrappedLines = this.textRenderer.wrapLines(Text.literal(message), 300);
            int lineY = this.height / 2 - 30;
            for (OrderedText line : wrappedLines) {
                context.drawText(this.textRenderer, line, this.width / 2 - 150, lineY, 0xFFAAAAAA, false);
                lineY += this.textRenderer.fontHeight;
            }

            // Show the path in green
            String path = promptFile.getAbsolutePath();
            context.drawText(this.textRenderer, path, this.width / 2 - 150, lineY + 5, 0x55FFFF55, false);

            super.render(context, mouseX, mouseY, delta);
        }

        @Override
        public void close() {
            this.client.setScreen(parent);
        }
    }
}