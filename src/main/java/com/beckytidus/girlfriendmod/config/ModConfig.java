package com.beckytidus.girlfriendmod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ModConfig {
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("girlfriend-mod.json").toFile();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ModConfig INSTANCE;

    public enum AIProvider {
        CHUTES("Chutes AI"),
        OPENROUTER("OpenRouter"),
        KOBOLDCPP("KoboldCpp (Local)");

        private final String displayName;

        AIProvider(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Provider selection
    public AIProvider aiProvider = AIProvider.CHUTES;

    // Chutes AI settings
    public String chutesApiKey = "";
    public String chutesModelName = "deepseek-ai/DeepSeek-V3-0324-TEE";

    // OpenRouter settings
    public String openRouterApiKey = "";
    public String openRouterModelName = "x-ai/grok-4.1-fast";

    // KoboldCpp settings
    public String koboldCppUrl = "http://localhost:5001";
    public String koboldCppModel = "kcpp";
    public boolean koboldCppUseChatCompletions = true;

    // Context window settings (0 = auto-detect from API)
    public int chutesModelContextWindow = 0;  // Auto-detect on first request (DeepSeek-V3 default: 128000)
    public int openRouterModelContextWindow = 0;  // Auto-detect from /api/v1/models
    public int koboldCppContextWindow = 0;  // Auto-detect from server or use conservative default
    
    // Flags to track if user has manually set context windows (for auto-detection override)
    public boolean chutesContextWindowUserSet = false;
    public boolean openRouterContextWindowUserSet = false;
    public boolean koboldCppContextWindowUserSet = false;

    // Token counting and summarization settings
    public int maxGenerationTokens = 1024;  // Reserved for response generation
    public double summarizationThreshold = 0.75;  // Trigger at 75% of available context
    public int safetyBufferPercent = 10;  // 10% safety buffer by default

    // Common settings
    public String customName = "Girlfriend";
    public int maxHistoryTokens = 8192;  // Legacy - kept for backward compatibility
    public double minP = 0.05;
    public double temperature = 0.85;
    public boolean enableAI = true;
    public String customTexturePath = "girlfriend-mod:textures/entity/girlfriend.png";

    public static ModConfig get() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(reader, ModConfig.class);
                // Handle migration from old config format
                if (INSTANCE != null) {
                    // Migrate legacy apiKey field if present
                    try {
                        java.lang.reflect.Field apiKeyField = ModConfig.class.getDeclaredField("apiKey");
                        apiKeyField.setAccessible(true);
                        String legacyApiKey = (String) apiKeyField.get(INSTANCE);
                        if (legacyApiKey != null && !legacyApiKey.isEmpty() && INSTANCE.chutesApiKey.isEmpty()) {
                            INSTANCE.chutesApiKey = legacyApiKey;
                        }
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        // Field doesn't exist or can't access, ignore
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                INSTANCE = new ModConfig();
            }
        } else {
            INSTANCE = new ModConfig();
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper methods for getting the active API key and model
    public String getActiveApiKey() {
        return switch (aiProvider) {
            case CHUTES -> chutesApiKey;
            case OPENROUTER -> openRouterApiKey;
            case KOBOLDCPP -> ""; // No API key for local KoboldCpp
        };
    }

    public String getActiveModelName() {
        return switch (aiProvider) {
            case CHUTES -> chutesModelName;
            case OPENROUTER -> openRouterModelName;
            case KOBOLDCPP -> koboldCppModel;
        };
    }

    /**
     * Gets the active context window size for the current provider.
     * Returns auto-detected defaults if not user-set.
     *
     * @return The context window size in tokens
     */
    public int getActiveContextWindow() {
        return switch (aiProvider) {
            case CHUTES -> chutesModelContextWindow > 0 ? chutesModelContextWindow : 128000;  // DeepSeek-V3 default
            case OPENROUTER -> openRouterModelContextWindow > 0 ? openRouterModelContextWindow : 131072;  // Grok default
            case KOBOLDCPP -> koboldCppContextWindow > 0 ? koboldCppContextWindow : 8192;  // Conservative default for local
        };
    }

    /**
     * Checks if the context window was manually set by the user for the current provider.
     * Used to determine whether auto-detection should update the value.
     *
     * @return true if the user manually set the context window
     */
    public boolean isContextWindowUserSet() {
        return switch (aiProvider) {
            case CHUTES -> chutesContextWindowUserSet;
            case OPENROUTER -> openRouterContextWindowUserSet;
            case KOBOLDCPP -> koboldCppContextWindowUserSet;
        };
    }

    /**
     * Sets the context window for the current provider if not user-set.
     * This is called by auto-detection logic.
     *
     * @param contextWindow The detected context window size
     */
    public void setDetectedContextWindow(int contextWindow) {
        if (contextWindow <= 0) return;
        
        switch (aiProvider) {
            case CHUTES -> {
                if (!chutesContextWindowUserSet) {
                    chutesModelContextWindow = contextWindow;
                }
            }
            case OPENROUTER -> {
                if (!openRouterContextWindowUserSet) {
                    openRouterModelContextWindow = contextWindow;
                }
            }
            case KOBOLDCPP -> {
                if (!koboldCppContextWindowUserSet) {
                    koboldCppContextWindow = contextWindow;
                }
            }
        }
    }

    /**
     * Gets the effective available context for conversation history.
     * This accounts for generation tokens and safety buffer.
     *
     * @return The number of tokens available for conversation history
     */
    public int getEffectiveAvailableContext() {
        int contextWindow = getActiveContextWindow();
        int generationTokens = maxGenerationTokens;
        int safetyBuffer = (int)(contextWindow * (safetyBufferPercent / 100.0));
        
        return contextWindow - generationTokens - safetyBuffer;
    }

    /**
     * Gets the token count threshold at which summarization should trigger.
     *
     * @return The summarization threshold in tokens
     */
    public int getSummarizationThreshold() {
        return (int)(getEffectiveAvailableContext() * summarizationThreshold);
    }

    // Legacy field for migration (no longer used directly)
    @SuppressWarnings("unused")
    private String apiKey = "";
}
