package com.beckytidus.girlfriendmod.gui;

import com.beckytidus.girlfriendmod.ai.SystemPromptManager;
import com.beckytidus.girlfriendmod.config.ModConfig;
import com.beckytidus.girlfriendmod.network.ModNetwork;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AIConfigScreen extends Screen {
    private final Screen parent;

    // API Key Masking
    private static final String MASKED_KEY = "********";
    private String originalChutesApiKey = "";
    private String originalOpenRouterApiKey = "";
    private boolean chutesKeyEdited = false;
    private boolean openRouterKeyEdited = false;

    // Configuration Widgets
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
    private TextFieldWidget contextWindowField;
    private TextFieldWidget summarizationThresholdField;
    private TextFieldWidget safetyBufferField;
    private TextFieldWidget maxGenTokensField;
    private ButtonWidget providerButton;
    private ButtonWidget apiFormatButton;

    // State
    private int currentProviderIndex = 0;

    // Scroll State
    private double scrollAmount = 0;
    private int contentHeight = 0;

    // Layout Containers
    private final List<List<ClickableWidget>> layoutRows = new ArrayList<>();

    private final List<ClickableWidget> chutesWidgets = new ArrayList<>();
    private final List<ClickableWidget> openRouterWidgets = new ArrayList<>();
    private final List<ClickableWidget> koboldCppWidgets = new ArrayList<>();

    public AIConfigScreen(Screen parent) {
        super(Text.literal("Girlfriend AI Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        layoutRows.clear();
        chutesWidgets.clear();
        openRouterWidgets.clear();
        koboldCppWidgets.clear();

        int centerX = this.width / 2;
        int fieldHeight = 18;

        ModConfig config = ModConfig.get();
        currentProviderIndex = config.aiProvider.ordinal();

        // 1. Provider Button
        providerButton = ButtonWidget.builder(Text.literal("AI Provider: " + getCurrentProviderName()), b -> {
            currentProviderIndex = (currentProviderIndex + 1) % ModConfig.AIProvider.values().length;
            b.setMessage(Text.literal("AI Provider: " + getCurrentProviderName()));
            repositionWidgets();
        }).dimensions(centerX - 100, 40, 200, 20).build();
        addRow(providerButton);

        // 2. Chutes AI Fields
        originalChutesApiKey = config.chutesApiKey != null ? config.chutesApiKey : "";
        chutesKeyEdited = false;
        addApiKeyField(centerX, "Chutes API Key:", originalChutesApiKey, w -> chutesApiKeyField = w, chutesWidgets, () -> chutesKeyEdited = true);
        addLabelAndField(centerX, "Chutes Model:", config.chutesModelName, w -> chutesModelField = w, chutesWidgets);

        // 3. OpenRouter Fields
        originalOpenRouterApiKey = config.openRouterApiKey != null ? config.openRouterApiKey : "";
        openRouterKeyEdited = false;
        addApiKeyField(centerX, "OpenRouter API Key:", originalOpenRouterApiKey, w -> openRouterApiKeyField = w, openRouterWidgets, () -> openRouterKeyEdited = true);
        addLabelAndField(centerX, "OpenRouter Model:", config.openRouterModelName, w -> openRouterModelField = w, openRouterWidgets);

        // 4. KoboldCpp Fields
        addLabelAndField(centerX, "KoboldCpp URL:", config.koboldCppUrl, w -> koboldCppUrlField = w, koboldCppWidgets);
        addLabelAndField(centerX, "KoboldCpp Model:", config.koboldCppModel, w -> koboldCppModelField = w, koboldCppWidgets);

        String formatDesc = config.koboldCppUseChatCompletions ? "Format: OpenAI Chat" : "Format: KoboldAPI";
        apiFormatButton = ButtonWidget.builder(Text.literal(formatDesc), b -> {
            config.koboldCppUseChatCompletions = !config.koboldCppUseChatCompletions;
            b.setMessage(Text.literal(config.koboldCppUseChatCompletions ? "Format: OpenAI Chat" : "Format: KoboldAPI"));
        }).dimensions(centerX - 100, 0, 200, 20).build();
        koboldCppWidgets.add(apiFormatButton);
        addRow(apiFormatButton);

        // 5. Common Settings
        addLabelAndField(centerX, "Girlfriend Name:", config.customName, w -> nameField = w, null);

        ButtonWidget tempLabel = createLabel(centerX, "Temp (0.0-2.0) / Min P (0.0-1.0):");
        addRow(tempLabel);

        tempField = new TextFieldWidget(this.textRenderer, centerX - 100, 0, 98, fieldHeight, Text.literal("Temp"));
        tempField.setText(String.valueOf(config.temperature));

        minPField = new TextFieldWidget(this.textRenderer, centerX + 2, 0, 98, fieldHeight, Text.literal("Min P"));
        minPField.setText(String.valueOf(config.minP));

        List<ClickableWidget> splitRow = new ArrayList<>();
        splitRow.add(tempField);
        splitRow.add(minPField);
        layoutRows.add(splitRow);
        this.addDrawableChild(tempField);
        this.addDrawableChild(minPField);

        addLabelAndField(centerX, "Max History Tokens:", String.valueOf(config.maxHistoryTokens), w -> tokenField = w, null);
        addLabelAndField(centerX, "Skin Texture Path:", config.customTexturePath, w -> texturePathField = w, null);

        // 6. Token Counting Settings (Advanced)
        addRow(createLabel(centerX, "--- Token Counting Settings ---"));
        
        addLabelAndField(centerX, "Context Window (0=auto):",
            String.valueOf(config.getActiveContextWindow()), w -> contextWindowField = w, null);
        addLabelAndField(centerX, "Summarization Threshold (0.0-1.0):",
            String.valueOf(config.summarizationThreshold), w -> summarizationThresholdField = w, null);
        addLabelAndField(centerX, "Safety Buffer %:",
            String.valueOf(config.safetyBufferPercent), w -> safetyBufferField = w, null);
        addLabelAndField(centerX, "Max Generation Tokens:",
            String.valueOf(config.maxGenerationTokens), w -> maxGenTokensField = w, null);

        // 7. Action Buttons
        addRow(ButtonWidget.builder(Text.literal("Edit System Prompt"), b -> openSystemPromptFile())
                .dimensions(centerX - 100, 0, 200, 20).build());

        addRow(ButtonWidget.builder(Text.literal("Clear All Memories"), b -> ModNetwork.sendClearMemoryPacket())
                .dimensions(centerX - 100, 0, 200, 20).build());

        addRow(ButtonWidget.builder(Text.literal("Save & Exit"), b -> {
            saveConfig();
            this.client.setScreen(parent);
        }).dimensions(centerX - 100, 0, 200, 20).build());

        repositionWidgets();
    }

    private void addLabelAndField(int centerX, String labelText, String defaultValue, java.util.function.Consumer<TextFieldWidget> fieldSetter, List<ClickableWidget> categoryList) {
        ButtonWidget label = createLabel(centerX, labelText);

        TextFieldWidget field = new TextFieldWidget(this.textRenderer, centerX - 100, 0, 200, 18, Text.literal(labelText));
        field.setMaxLength(256);
        field.setText(defaultValue);
        fieldSetter.accept(field);

        if (categoryList != null) {
            categoryList.add(label);
            categoryList.add(field);
        }

        addRow(label);
        addRow(field);
    }

    private void addApiKeyField(int centerX, String labelText, String originalKey, java.util.function.Consumer<TextFieldWidget> fieldSetter, List<ClickableWidget> categoryList, Runnable onEdited) {
        ButtonWidget label = createLabel(centerX, labelText);

        final TextFieldWidget field = new TextFieldWidget(this.textRenderer, centerX - 100, 0, 200, 18, Text.literal(labelText));
        field.setMaxLength(256);
        
        // Show masked value if there's an existing key, otherwise show empty
        final boolean hasExistingKey = originalKey != null && !originalKey.isEmpty();
        if (hasExistingKey) {
            field.setText(MASKED_KEY);
        } else {
            field.setText("");
        }
        fieldSetter.accept(field);

        // Track when the field is edited
        field.setChangedListener(newText -> {
            onEdited.run();
        });
        
        if (categoryList != null) {
            categoryList.add(label);
            categoryList.add(field);
        }

        addRow(label);
        addRow(field);
    }

    private ButtonWidget createLabel(int centerX, String text) {
        ButtonWidget label = ButtonWidget.builder(Text.literal(text), b -> {}).dimensions(centerX - 100, 0, 200, 12).build();
        label.active = false;
        return label;
    }

    private void addRow(ClickableWidget widget) {
        List<ClickableWidget> row = new ArrayList<>();
        row.add(widget);
        layoutRows.add(row);
        this.addDrawableChild(widget);
    }

    private String getCurrentProviderName() {
        return ModConfig.AIProvider.values()[currentProviderIndex].getDisplayName();
    }

    private void repositionWidgets() {
        int startY = 70; // Start below the title
        int currentY = startY;
        int gap = 4;

        ModConfig.AIProvider provider = ModConfig.AIProvider.values()[currentProviderIndex];

        for (List<ClickableWidget> row : layoutRows) {
            boolean rowVisible = false;
            int rowHeight = 0;

            for (ClickableWidget w : row) {
                boolean isWidgetVisible = true;
                if (chutesWidgets.contains(w) && provider != ModConfig.AIProvider.CHUTES) isWidgetVisible = false;
                else if (openRouterWidgets.contains(w) && provider != ModConfig.AIProvider.OPENROUTER) isWidgetVisible = false;
                else if (koboldCppWidgets.contains(w) && provider != ModConfig.AIProvider.KOBOLDCPP) isWidgetVisible = false;

                if (isWidgetVisible) {
                    w.visible = true;
                    w.setY((int)(currentY - scrollAmount));
                    rowHeight = Math.max(rowHeight, w.getHeight());
                    rowVisible = true;
                } else {
                    w.visible = false;
                }
            }

            if (rowVisible && rowHeight > 0) {
                currentY += rowHeight + gap;
            }
        }

        this.contentHeight = currentY + (int)scrollAmount - startY + 30;

        int maxScroll = getMaxScroll();
        if (scrollAmount > maxScroll) scrollAmount = maxScroll;
        if (scrollAmount < 0) scrollAmount = 0;
    }

    private int getMaxScroll() {
        int scrollableAreaHeight = this.height - 65; // Account for title and bottom padding
        return Math.max(0, this.contentHeight - scrollableAreaHeight);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (getMaxScroll() > 0) {
            this.scrollAmount = MathHelper.clamp(this.scrollAmount - verticalAmount * 20, 0, getMaxScroll());
            repositionWidgets();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Draw semi-transparent background instead of calling renderBackground()
        context.fill(0, 0, this.width, this.height, 0x80000000);

        // Draw a panel background for the content area
        int panelX = this.width / 2 - 150;
        int panelY = 30;
        int panelWidth = 300;
        int panelHeight = this.height - 60;

        // Draw panel background with rounded corners effect
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xFF202020);
        context.fill(panelX + 1, panelY + 1, panelX + panelWidth - 1, panelY + panelHeight - 1, 0xFF404040);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);

        int topMargin = 45;
        context.enableScissor(panelX, panelY, panelX + panelWidth, panelY + panelHeight);

        super.render(context, mouseX, mouseY, delta);

        context.disableScissor();

        int maxScroll = getMaxScroll();
        if (maxScroll > 0) {
            int scrollbarX = panelX + panelWidth - 6;
            int scrollbarWidth = 4;
            int scrollableAreaTop = panelY;
            int scrollableAreaHeight = panelHeight;

            int thumbHeight = Math.max(20, (scrollableAreaHeight * scrollableAreaHeight) / Math.max(this.contentHeight, scrollableAreaHeight));
            int trackHeight = scrollableAreaHeight - thumbHeight;

            int thumbY = scrollableAreaTop + (int)((this.scrollAmount / maxScroll) * trackHeight);

            context.fill(scrollbarX, scrollableAreaTop, scrollbarX + scrollbarWidth, scrollableAreaTop + scrollableAreaHeight, 0x80000000);
            context.fill(scrollbarX, thumbY, scrollbarX + scrollbarWidth, thumbY + thumbHeight, 0xFF808080);
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Override to prevent calling the parent's renderBackground which causes the blur error
        // We already draw our own background in render()
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
                String defaultPrompt = SystemPromptManager.buildDefaultPromptFileContent();
                Files.writeString(promptFile.toPath(), defaultPrompt);
            }

            try {
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) {
                    Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", promptFile.getAbsolutePath()});
                } else if (os.contains("mac")) {
                    Runtime.getRuntime().exec(new String[]{"open", promptFile.getAbsolutePath()});
                } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
                    Runtime.getRuntime().exec(new String[]{"xdg-open", promptFile.getAbsolutePath()});
                } else {
                     throw new IOException("Unsupported OS");
                }
            } catch (IOException e) {
                this.client.setScreen(new EditPromptNoticeScreen(this, promptFile));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveConfig() {
        ModConfig config = ModConfig.get();

        config.aiProvider = ModConfig.AIProvider.values()[currentProviderIndex];
        
        // Handle API key fields - use original key if field still shows masked value
        String chutesKey = chutesApiKeyField.getText();
        if (chutesKey.equals(MASKED_KEY) || chutesKey.isEmpty()) {
            config.chutesApiKey = originalChutesApiKey;
        } else {
            config.chutesApiKey = chutesKey;
        }
        config.chutesModelName = chutesModelField.getText();
        
        String openRouterKey = openRouterApiKeyField.getText();
        if (openRouterKey.equals(MASKED_KEY) || openRouterKey.isEmpty()) {
            config.openRouterApiKey = originalOpenRouterApiKey;
        } else {
            config.openRouterApiKey = openRouterKey;
        }
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

        // Token counting settings
        try {
            int contextWindow = Integer.parseInt(contextWindowField.getText());
            // Set context window for the current provider
            switch (config.aiProvider) {
                case CHUTES -> {
                    config.chutesModelContextWindow = contextWindow;
                    config.chutesContextWindowUserSet = contextWindow > 0;
                }
                case OPENROUTER -> {
                    config.openRouterModelContextWindow = contextWindow;
                    config.openRouterContextWindowUserSet = contextWindow > 0;
                }
                case KOBOLDCPP -> {
                    config.koboldCppContextWindow = contextWindow;
                    config.koboldCppContextWindowUserSet = contextWindow > 0;
                }
            }
        } catch (NumberFormatException ignored) {}

        try {
            config.summarizationThreshold = Double.parseDouble(summarizationThresholdField.getText());
            // Clamp to valid range
            if (config.summarizationThreshold < 0.1) config.summarizationThreshold = 0.1;
            if (config.summarizationThreshold > 1.0) config.summarizationThreshold = 1.0;
        } catch (NumberFormatException ignored) {}

        try {
            config.safetyBufferPercent = Integer.parseInt(safetyBufferField.getText());
            // Clamp to valid range
            if (config.safetyBufferPercent < 0) config.safetyBufferPercent = 0;
            if (config.safetyBufferPercent > 50) config.safetyBufferPercent = 50;
        } catch (NumberFormatException ignored) {}

        try {
            config.maxGenerationTokens = Integer.parseInt(maxGenTokensField.getText());
            // Minimum 256 tokens for generation
            if (config.maxGenerationTokens < 256) config.maxGenerationTokens = 256;
        } catch (NumberFormatException ignored) {}

        ModConfig.save();
        ModNetwork.sendConfigUpdatePacket(config);
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
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
