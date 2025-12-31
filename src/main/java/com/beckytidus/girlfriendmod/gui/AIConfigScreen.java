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
import java.util.ArrayList;
import java.util.List;

public class AIConfigScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget chutesApiKeyField;
    private TextFieldWidget chutesModelField;
    private TextFieldWidget openRouterApiKeyField;
    private TextFieldWidget openRouterModelField;
    private TextFieldWidget koboldCppUrlField;
    private TextFieldWidget koboldCppModelField;
    private TextFieldWidget nameField;
    private TextFieldWidget tokenField;
    private TextFieldWidget minPField;
    private TextFieldWidget tempField;
    private TextFieldWidget texturePathField;
    private ButtonWidget providerButton;
    private ButtonWidget apiFormatButton;
    private int currentProviderIndex = 0;

    // Lists to track widgets for visibility toggling
    private final List<ButtonWidget> chutesButtons = new ArrayList<>();
    private final List<TextFieldWidget> chutesFields = new ArrayList<>();
    private final List<ButtonWidget> openRouterButtons = new ArrayList<>();
    private final List<TextFieldWidget> openRouterFields = new ArrayList<>();
    private final List<ButtonWidget> koboldCppButtons = new ArrayList<>();
    private final List<TextFieldWidget> koboldCppFields = new ArrayList<>();

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

        // Provider Selection Button
        ModConfig config = ModConfig.get();
        currentProviderIndex = config.aiProvider.ordinal();

        providerButton = ButtonWidget.builder(Text.literal("AI Provider: " + getCurrentProviderName()), b -> {
            currentProviderIndex = (currentProviderIndex + 1) % ModConfig.AIProvider.values().length;
            b.setMessage(Text.literal("AI Provider: " + getCurrentProviderName()));
            updateFieldsVisibility();
        }).dimensions(centerX - 100, y - labelHeight, 200, labelHeight).build();
        this.addDrawableChild(providerButton);

        y += 30;

        // ========== CHUTES AI SECTION ==========
        ButtonWidget chutesApiKeyLabel = ButtonWidget.builder(Text.literal("Chutes API Key:"), b -> {}).dimensions(centerX - 100, y - labelHeight, 200, labelHeight).build();
        chutesApiKeyLabel.active = false;
        this.addDrawableChild(chutesApiKeyLabel);
        chutesButtons.add(chutesApiKeyLabel);

        chutesApiKeyField = new TextFieldWidget(this.textRenderer, centerX - 100, y, 200, fieldHeight, Text.literal("Chutes API Key"));
        chutesApiKeyField.setMaxLength(256);
        chutesApiKeyField.setText(config.chutesApiKey);
        this.addDrawableChild(chutesApiKeyField);
        chutesFields.add(chutesApiKeyField);

        y += gap;

        ButtonWidget chutesModelLabel = ButtonWidget.builder(Text.literal("Chutes Model:"), b -> {}).dimensions(centerX - 100, y - labelHeight, 200, labelHeight).build();
        chutesModelLabel.active = false;
        this.addDrawableChild(chutesModelLabel);
        chutesButtons.add(chutesModelLabel);

        chutesModelField = new TextFieldWidget(this.textRenderer, centerX - 100, y, 200, fieldHeight, Text.literal("Model"));
        chutesModelField.setText(config.chutesModelName);
        this.addDrawableChild(chutesModelField);
        chutesFields.add(chutesModelField);

        y += gap;

        // ========== OPENROUTER SECTION ==========
        ButtonWidget openRouterApiKeyLabel = ButtonWidget.builder(Text.literal("OpenRouter API Key:"), b -> {}).dimensions(centerX - 100, y - labelHeight, 200, labelHeight).build();
        openRouterApiKeyLabel.active = false;
        this.addDrawableChild(openRouterApiKeyLabel);
        openRouterButtons.add(openRouterApiKeyLabel);

        openRouterApiKeyField = new TextFieldWidget(this.textRenderer, centerX - 100, y, 200, fieldHeight, Text.literal("OpenRouter API Key"));
        openRouterApiKeyField.setMaxLength(256);
        openRouterApiKeyField.setText(config.openRouterApiKey);
        this.addDrawableChild(openRouterApiKeyField);
        openRouterFields.add(openRouterApiKeyField);

        y += gap;

        ButtonWidget openRouterModelLabel = ButtonWidget.builder(Text.literal("OpenRouter Model:"), b -> {}).dimensions(centerX - 100, y - labelHeight, 200, labelHeight).build();
        openRouterModelLabel.active = false;
        this.addDrawableChild(openRouterModelLabel);
        openRouterButtons.add(openRouterModelLabel);

        openRouterModelField = new TextFieldWidget(this.textRenderer, centerX - 100, y, 200, fieldHeight, Text.literal("Model"));
        openRouterModelField.setText(config.openRouterModelName);
        this.addDrawableChild(openRouterModelField);
        openRouterFields.add(openRouterModelField);

        y += gap;

        // ========== KOBOLDCPP SECTION ==========
        ButtonWidget koboldCppUrlLabel = ButtonWidget.builder(Text.literal("KoboldCpp URL:"), b -> {}).dimensions(centerX - 100, y - labelHeight, 200, labelHeight).build();
        koboldCppUrlLabel.active = false;
        this.addDrawableChild(koboldCppUrlLabel);
        koboldCppButtons.add(koboldCppUrlLabel);

        koboldCppUrlField = new TextFieldWidget(this.textRenderer, centerX - 100, y, 200, fieldHeight, Text.literal("KoboldCpp URL"));
        koboldCppUrlField.setMaxLength(256);
        koboldCppUrlField.setText(config.koboldCppUrl);
        this.addDrawableChild(koboldCppUrlField);
        koboldCppFields.add(koboldCppUrlField);

        y += gap;

        ButtonWidget koboldCppModelLabel = ButtonWidget.builder(Text.literal("KoboldCpp Model:"), b -> {}).dimensions(centerX - 100, y - labelHeight, 200, labelHeight).build();
        koboldCppModelLabel.active = false;
        this.addDrawableChild(koboldCppModelLabel);
        koboldCppButtons.add(koboldCppModelLabel);

        koboldCppModelField = new TextFieldWidget(this.textRenderer, centerX - 100, y, 200, fieldHeight, Text.literal("Model"));
        koboldCppModelField.setText(config.koboldCppModel);
        this.addDrawableChild(koboldCppModelField);
        koboldCppFields.add(koboldCppModelField);

        y += gap;

        // API Format Toggle (for KoboldCpp)
        String formatDesc = config.koboldCppUseChatCompletions ? "Format: OpenAI Chat" : "Format: KoboldAPI";
        apiFormatButton = ButtonWidget.builder(Text.literal(formatDesc), b -> {
            config.koboldCppUseChatCompletions = !config.koboldCppUseChatCompletions;
            b.setMessage(Text.literal(config.koboldCppUseChatCompletions ? "Format: OpenAI Chat" : "Format: KoboldAPI"));
        }).dimensions(centerX - 100, y - labelHeight, 200, labelHeight).build();
        this.addDrawableChild(apiFormatButton);
        koboldCppButtons.add(apiFormatButton);

        y += gap;

        // ========== COMMON SETTINGS ==========
        ButtonWidget nameLabel = ButtonWidget.builder(Text.literal("Girlfriend Name:"), b -> {}).dimensions(centerX - 100, y - labelHeight, 200, labelHeight).build();
        nameLabel.active = false;
        this.addDrawableChild(nameLabel);

        nameField = new TextFieldWidget(this.textRenderer, centerX - 100, y, 200, fieldHeight, Text.literal("Name"));
        nameField.setText(config.customName);
        this.addDrawableChild(nameField);

        y += gap;

        // Temperature & Min P (Split Row)
        ButtonWidget tempLabel = ButtonWidget.builder(Text.literal("Temp (0.0-2.0) / Min P (0.0-1.0):"), b -> {}).dimensions(centerX - 100, y - labelHeight, 200, labelHeight).build();
        tempLabel.active = false;
        this.addDrawableChild(tempLabel);

        tempField = new TextFieldWidget(this.textRenderer, centerX - 100, y, 98, fieldHeight, Text.literal("Temp"));
        tempField.setText(String.valueOf(config.temperature));
        this.addDrawableChild(tempField);

        minPField = new TextFieldWidget(this.textRenderer, centerX + 2, y, 98, fieldHeight, Text.literal("Min P"));
        minPField.setText(String.valueOf(config.minP));
        this.addDrawableChild(minPField);

        y += gap;

        ButtonWidget tokenLabel = ButtonWidget.builder(Text.literal("Max History Tokens:"), b -> {}).dimensions(centerX - 100, y - labelHeight, 200, labelHeight).build();
        tokenLabel.active = false;
        this.addDrawableChild(tokenLabel);

        tokenField = new TextFieldWidget(this.textRenderer, centerX - 100, y, 200, fieldHeight, Text.literal("Tokens"));
        tokenField.setText(String.valueOf(config.maxHistoryTokens));
        this.addDrawableChild(tokenField);

        y += gap;

        ButtonWidget textureLabel = ButtonWidget.builder(Text.literal("Skin Texture Path:"), b -> {}).dimensions(centerX - 100, y - labelHeight, 200, labelHeight).build();
        textureLabel.active = false;
        this.addDrawableChild(textureLabel);

        texturePathField = new TextFieldWidget(this.textRenderer, centerX - 100, y, 200, fieldHeight, Text.literal("Texture Path"));
        texturePathField.setMaxLength(256);
        texturePathField.setText(config.customTexturePath);
        this.addDrawableChild(texturePathField);

        y += 20;

        // System Prompt File Button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Edit System Prompt"), button -> {
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

        // Initialize field visibility
        updateFieldsVisibility();
    }

    private String getCurrentProviderName() {
        return ModConfig.AIProvider.values()[currentProviderIndex].getDisplayName();
    }

    private void updateFieldsVisibility() {
        ModConfig.AIProvider provider = ModConfig.AIProvider.values()[currentProviderIndex];

        // Set visibility by enabling/disabling widgets
        for (ButtonWidget btn : chutesButtons) {
            btn.visible = provider == ModConfig.AIProvider.CHUTES;
        }
        for (TextFieldWidget field : chutesFields) {
            field.setVisible(provider == ModConfig.AIProvider.CHUTES);
        }

        for (ButtonWidget btn : openRouterButtons) {
            btn.visible = provider == ModConfig.AIProvider.OPENROUTER;
        }
        for (TextFieldWidget field : openRouterFields) {
            field.setVisible(provider == ModConfig.AIProvider.OPENROUTER);
        }

        for (ButtonWidget btn : koboldCppButtons) {
            btn.visible = provider == ModConfig.AIProvider.KOBOLDCPP;
        }
        for (TextFieldWidget field : koboldCppFields) {
            field.setVisible(provider == ModConfig.AIProvider.KOBOLDCPP);
        }
    }

    private void openSystemPromptFile() {
        try {
            Path configDir = FabricLoader.getInstance().getConfigDir();
            Path promptDir = configDir.resolve("girlfriend-mod");
            File promptFile = promptDir.resolve("system-prompt.txt").toFile();

            if (!promptDir.toFile().exists()) {
                promptDir.toFile().mkdirs();
            }

            if (!promptFile.exists()) {
                String defaultPrompt = buildDefaultPromptFileContent();
                Files.writeString(promptFile.toPath(), defaultPrompt);
            }

            try {
                Runtime.getRuntime().exec(new String[]{"cmd", "/c", promptFile.getAbsolutePath()});
            } catch (IOException e) {
                this.client.setScreen(new EditPromptNoticeScreen(this, promptFile));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

        config.aiProvider = ModConfig.AIProvider.values()[currentProviderIndex];
        config.chutesApiKey = chutesApiKeyField.getText();
        config.chutesModelName = chutesModelField.getText();
        config.openRouterApiKey = openRouterApiKeyField.getText();
        config.openRouterModelName = openRouterModelField.getText();
        config.koboldCppUrl = koboldCppUrlField.getText();
        config.koboldCppModel = koboldCppModelField.getText();
        config.customName = nameField.getText();
        config.customTexturePath = texturePathField.getText();

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

            this.addDrawableChild(ButtonWidget.builder(Text.literal("Copy Path"), button -> {
                this.client.keyboard.setClipboard(promptFile.getAbsolutePath());
                this.client.setScreen(parent);
            }).dimensions(centerX - 80, y + 30, 160, 20).build());

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
