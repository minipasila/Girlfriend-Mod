package com.beckytidus.girlfriendmod.ai;

import com.beckytidus.girlfriendmod.ai.AIClientManager;
import com.beckytidus.girlfriendmod.config.ModConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ConversationManager {
    private final UUID entityId;
    private List<ChutesClient.ChatMessage> history = new ArrayList<>();
    private String summary = "";
    
    public ConversationManager(UUID entityId) {
        this.entityId = entityId;
        load();
    }

    public void addMessage(String role, String content) {
        history.add(new ChutesClient.ChatMessage(role, content));
        checkSummarization();
        save();
    }

    public void clear() {
        history.clear();
        summary = "";
        save();
    }

    public List<ChutesClient.ChatMessage> getContextWindow() {
        List<ChutesClient.ChatMessage> context = new ArrayList<>();
        if (!summary.isEmpty()) {
            context.add(new ChutesClient.ChatMessage("system", "Previous Memory Summary: " + summary));
        }
        context.addAll(history);
        return context;
    }

    private void checkSummarization() {
        // Rough token estimation: 4 chars per token
        int estimatedTokens = history.stream().mapToInt(m -> m.content.length()).sum() / 4;
        
        if (estimatedTokens > ModConfig.get().maxHistoryTokens) {
            AIClientManager.summarize(history).thenAccept(newSummary -> {
                this.summary = newSummary;
                // Keep last few messages
                if (history.size() > 5) {
                    history = new ArrayList<>(history.subList(history.size() - 5, history.size()));
                }
                save();
            });
        }
    }

    private File getFile() {
        File dir = FabricLoader.getInstance().getConfigDir().resolve("girlfriend-mod/memories").toFile();
        dir.mkdirs();
        return new File(dir, entityId.toString() + ".json");
    }

    public void save() {
        try (FileWriter w = new FileWriter(getFile())) {
            new Gson().toJson(this, w);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void load() {
        File f = getFile();
        if (f.exists()) {
            try (FileReader r = new FileReader(f)) {
                ConversationManager data = new Gson().fromJson(r, ConversationManager.class);
                this.history = data.history;
                this.summary = data.summary;
            } catch (IOException e) { e.printStackTrace(); }
        }
    }
}