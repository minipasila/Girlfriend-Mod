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
    public String openRouterModelName = "moonshotai/kimi-k2-0905";

    // KoboldCpp settings
    public String koboldCppUrl = "http://localhost:5001";
    public String koboldCppModel = "kcpp";
    public boolean koboldCppUseChatCompletions = true;

    // Common settings
    public String customName = "Girlfriend";
    public int maxHistoryTokens = 8192;
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

    // Legacy field for migration (no longer used directly)
    @SuppressWarnings("unused")
    private String apiKey = "";
}
