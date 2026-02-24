package com.beckytidus.girlfriendmod.ai;

import com.beckytidus.girlfriendmod.ai.AIClientManager;
import com.beckytidus.girlfriendmod.config.ModConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConversationManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("girlfriend-mod-conversation");
    
    private final UUID entityId;
    private List<ChutesClient.ChatMessage> history = new ArrayList<>();
    private String summary = "";
    
    // Flag to prevent concurrent modifications during summarization
    private final AtomicBoolean isSummarizing = new AtomicBoolean(false);
    
    public ConversationManager(UUID entityId) {
        this.entityId = entityId;
        load();
    }

    public void addMessage(String role, String content) {
        synchronized (this) {
            history.add(new ChutesClient.ChatMessage(role, content));
            checkSummarization();
            save();
        }
    }

    public void clear() {
        synchronized (this) {
            history.clear();
            summary = "";
            save();
        }
    }

    public List<ChutesClient.ChatMessage> getContextWindow() {
        synchronized (this) {
            List<ChutesClient.ChatMessage> context = new ArrayList<>();
            if (!summary.isEmpty()) {
                context.add(new ChutesClient.ChatMessage("system", "Previous Memory Summary: " + summary));
            }
            context.addAll(history);
            return context;
        }
    }

    /**
     * Retrieves the last N messages from the history.
     * Useful for logic engines that need immediate context without the full summary.
     */
    public List<ChutesClient.ChatMessage> getRecentHistory(int count) {
        synchronized (this) {
            int start = Math.max(0, history.size() - count);
            return new ArrayList<>(history.subList(start, history.size()));
        }
    }
    
    /**
     * Returns a copy of the current history for summarization.
     * This is used to avoid holding the lock during the async summarization process.
     */
    private List<ChutesClient.ChatMessage> getHistoryCopy() {
        return new ArrayList<>(history);
    }

    /**
     * Estimates the token count for a given text.
     * Uses a simple heuristic of ~4 characters per token.
     */
    private int estimateTokens(String text) {
        if (text == null || text.isEmpty()) return 0;
        return text.length() / 4;
    }
    
    /**
     * Estimates total tokens used by the conversation context.
     * This includes:
     * - System prompt (estimated ~1500 chars)
     * - Summary if present
     * - Chat history
     * - Current status context (estimated ~1000 chars average)
     * - Overhead for role labels and JSON structure
     */
    public int estimateTotalContextTokens() {
        synchronized (this) {
            int total = 0;
            
            // System prompt estimation (~1500 chars for default prompt)
            total += 375;
            
            // Summary if present
            if (!summary.isEmpty()) {
                total += estimateTokens(summary);
                total += 10; // overhead for "Previous Memory Summary: " prefix
            }
            
            // Chat history
            for (ChutesClient.ChatMessage msg : history) {
                total += estimateTokens(msg.content);
                total += 10; // overhead for role label and JSON structure
            }
            
            // Current status context estimation (~1000 chars average)
            total += 250;
            
            return total;
        }
    }

    private void checkSummarization() {
        // Only count history tokens for the threshold check
        int historyTokens = history.stream()
                .mapToInt(m -> estimateTokens(m.content))
                .sum();
        
        if (historyTokens > ModConfig.get().maxHistoryTokens && isSummarizing.compareAndSet(false, true)) {
            LOGGER.info("Starting summarization. History tokens: {}, threshold: {}", 
                    historyTokens, ModConfig.get().maxHistoryTokens);
            
            // Get a copy of history to avoid holding lock during async operation
            List<ChutesClient.ChatMessage> historyCopy = getHistoryCopy();
            
            AIClientManager.summarize(historyCopy).thenAccept(newSummary -> {
                synchronized (ConversationManager.this) {
                    this.summary = newSummary;
                    // Keep last few messages
                    if (history.size() > 5) {
                        history = new ArrayList<>(history.subList(history.size() - 5, history.size()));
                    }
                    save();
                    isSummarizing.set(false);
                    LOGGER.info("Summarization complete. New summary length: {} chars", 
                            newSummary != null ? newSummary.length() : 0);
                }
            }).exceptionally(e -> {
                LOGGER.error("Error during summarization", e);
                isSummarizing.set(false);
                return null;
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
        } catch (IOException e) { 
            LOGGER.error("Error saving conversation memory", e);
        }
    }

    private void load() {
        File f = getFile();
        if (f.exists()) {
            try (FileReader r = new FileReader(f)) {
                ConversationManager data = new Gson().fromJson(r, ConversationManager.class);
                if (data != null) {
                    this.history = data.history != null ? data.history : new ArrayList<>();
                    this.summary = data.summary != null ? data.summary : "";
                }
            } catch (IOException e) { 
                LOGGER.error("Error loading conversation memory", e);
            }
        }
    }
    
    /**
     * Returns the current summary (for debugging/testing).
     */
    public String getSummary() {
        synchronized (this) {
            return summary;
        }
    }
    
    /**
     * Returns the history size (for debugging/testing).
     */
    public int getHistorySize() {
        synchronized (this) {
            return history.size();
        }
    }
    
    /**
     * Returns whether a summarization is currently in progress.
     */
    public boolean isSummarizing() {
        return isSummarizing.get();
    }
}
