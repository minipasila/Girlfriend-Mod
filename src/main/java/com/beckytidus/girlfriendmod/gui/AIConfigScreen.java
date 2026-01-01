package com.beckytidus.girlfriendmod.gui;

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
        }).dimensions(centerX - 100, 0, 200, 20).build();
        addRow(providerButton);

        // 2. Chutes AI Fields
        addLabelAndField(centerX, "Chutes API Key:", config.chutesApiKey, w -> chutesApiKeyField = w, chutesWidgets);
        addLabelAndField(centerX, "Chutes Model:", config.chutesModelName, w -> chutesModelField = w, chutesWidgets);

        // 3. OpenRouter Fields
        addLabelAndField(centerX, "OpenRouter API Key:", config.openRouterApiKey, w -> openRouterApiKeyField = w, openRouterWidgets);
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

        // 6. Action Buttons
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
        int startY = 30;
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
        int scrollableAreaHeight = this.height - 25; 
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
        context.fill(0, 0, this.width, this.height, 0xA0000000);
        
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 8, 0xFFFFFF);
        
        int topMargin = 25;
        context.enableScissor(0, topMargin, this.width, this.height);
        
        super.render(context, mouseX, mouseY, delta);
        
        context.disableScissor();
        
        int maxScroll = getMaxScroll();
        if (maxScroll > 0) {
            int scrollbarX = this.width - 6;
            int scrollbarWidth = 4;
            int scrollableAreaTop = topMargin;
            int scrollableAreaHeight = this.height - scrollableAreaTop;
            
            int thumbHeight = Math.max(20, (scrollableAreaHeight * scrollableAreaHeight) / Math.max(this.contentHeight, scrollableAreaHeight));
            int trackHeight = scrollableAreaHeight - thumbHeight;
            
            int thumbY = scrollableAreaTop + (int)((this.scrollAmount / maxScroll) * trackHeight);
            
            context.fill(scrollbarX, scrollableAreaTop, scrollbarX + scrollbarWidth, scrollableAreaTop + scrollableAreaHeight, 0x80000000);
            context.fill(scrollbarX, thumbY, scrollbarX + scrollbarWidth, thumbY + thumbHeight, 0xFF808080);
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
                - NO asterisks or narration in your message, only talk to your owner.

                ## IMPORTANT INFORMATION
                - When your owner gives you an item you cannot give anything back at that moment.
                - Do not say you're eating something, wait for context to tell you that you ate something then you can say that.
                - Never say you're giving an item you don't have in your inventory and if you want to give an item to your owner first ask.

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