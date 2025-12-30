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
        OPENROUTER("OpenRouter");
        
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
    public String chutesModelName = "Qwen/Qwen2.5-VL-72B-Instruct-TEE";
    
    // OpenRouter settings
    public String openRouterApiKey = "";
    public String openRouterModelName = "anthropic/claude-sonnet-4-20250514";
    
    // Common settings
    public String customName = "Girlfriend";
    public int maxHistoryTokens = 8192;
    public double minP = 0.05;
    public double temperature = 0.85;
    public boolean enableAI = true;
    
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
                if (INSTANCE != null && INSTANCE.chutesApiKey.isEmpty() && !INSTANCE.apiKey.isEmpty()) {
                    INSTANCE.chutesApiKey = INSTANCE.apiKey;
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
        return aiProvider == AIProvider.CHUTES ? chutesApiKey : openRouterApiKey;
    }
    
    public String getActiveModelName() {
        return aiProvider == AIProvider.CHUTES ? chutesModelName : openRouterModelName;
    }
    
    // Legacy field for migration
    private String apiKey = "";
}