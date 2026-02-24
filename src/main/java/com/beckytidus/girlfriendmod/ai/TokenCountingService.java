package com.beckytidus.girlfriendmod.ai;

import com.beckytidus.girlfriendmod.config.ModConfig;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for accurate token counting using API-provided token usage data.
 * Falls back to estimation when API data is not available.
 */
public class TokenCountingService {
    private static final Logger LOGGER = LoggerFactory.getLogger("girlfriend-mod-tokens");
    
    // Cache for token counts from API responses
    private int lastPromptTokens = 0;
    private int lastCompletionTokens = 0;
    private int lastTotalTokens = 0;
    
    // Running total for conversation
    private int conversationTokens = 0;
    
    /**
     * Updates token counts from an API response usage object.
     * Call this after each API request that returns usage information.
     * 
     * @param usage The usage object from the API response containing prompt_tokens, completion_tokens, total_tokens
     */
    public void updateFromResponse(JsonObject usage) {
        if (usage != null && usage.has("prompt_tokens")) {
            lastPromptTokens = usage.get("prompt_tokens").getAsInt();
            lastCompletionTokens = usage.has("completion_tokens") ? usage.get("completion_tokens").getAsInt() : 0;
            lastTotalTokens = usage.has("total_tokens") ? usage.get("total_tokens").getAsInt() : lastPromptTokens + lastCompletionTokens;
            conversationTokens = lastPromptTokens;
            LOGGER.debug("Token counts updated - Prompt: {}, Completion: {}, Total: {}", 
                lastPromptTokens, lastCompletionTokens, lastTotalTokens);
        }
    }
    
    /**
     * Updates token counts from individual values.
     * Alternative method when usage object is already parsed.
     */
    public void updateFromValues(int promptTokens, int completionTokens, int totalTokens) {
        this.lastPromptTokens = promptTokens;
        this.lastCompletionTokens = completionTokens;
        this.lastTotalTokens = totalTokens;
        this.conversationTokens = promptTokens;
        LOGGER.debug("Token counts updated - Prompt: {}, Completion: {}, Total: {}", 
            promptTokens, completionTokens, totalTokens);
    }
    
    /**
     * Gets the effective available context for conversation history.
     * Formula: contextWindow - generationTokens - safetyBuffer
     * 
     * @return The number of tokens available for conversation history
     */
    public int getEffectiveAvailableContext() {
        ModConfig config = ModConfig.get();
        int contextWindow = config.getActiveContextWindow();
        int generationTokens = config.maxGenerationTokens;
        int safetyBuffer = (int)(contextWindow * (config.safetyBufferPercent / 100.0));
        
        return contextWindow - generationTokens - safetyBuffer;
    }
    
    /**
     * Calculates the summarization threshold.
     * Returns the token count at which summarization should trigger.
     * 
     * @return Token count threshold for triggering summarization
     */
    public int getSummarizationThreshold() {
        return (int)(getEffectiveAvailableContext() * ModConfig.get().summarizationThreshold);
    }
    
    /**
     * Checks if summarization should be triggered based on current token count.
     * 
     * @param currentTokens The current token count to check
     * @return true if summarization should be triggered
     */
    public boolean shouldSummarize(int currentTokens) {
        return currentTokens >= getSummarizationThreshold();
    }
    
    /**
     * Estimates token count for a given text using character-based heuristic.
     * This is a fallback when API token counting is not available.
     * Uses approximately 4 characters per token as a rough estimate.
     * 
     * @param text The text to estimate tokens for
     * @return Estimated token count
     */
    public int estimateTokens(String text) {
        if (text == null || text.isEmpty()) return 0;
        return text.length() / 4;
    }
    
    /**
     * Gets the current conversation token count from the last API response.
     * 
     * @return The prompt tokens from the last API response, or 0 if no response yet
     */
    public int getConversationTokens() {
        return conversationTokens;
    }
    
    /**
     * Gets the last prompt token count from API response.
     */
    public int getLastPromptTokens() { 
        return lastPromptTokens; 
    }
    
    /**
     * Gets the last completion token count from API response.
     */
    public int getLastCompletionTokens() { 
        return lastCompletionTokens; 
    }
    
    /**
     * Gets the last total token count from API response.
     */
    public int getLastTotalTokens() { 
        return lastTotalTokens; 
    }
    
    /**
     * Resets all token counts to zero.
     * Call this when clearing conversation history.
     */
    public void reset() {
        lastPromptTokens = 0;
        lastCompletionTokens = 0;
        lastTotalTokens = 0;
        conversationTokens = 0;
        LOGGER.debug("Token counts reset to zero");
    }
    
    /**
     * Gets a formatted string with current token statistics.
     * Useful for debugging and logging.
     */
    public String getTokenStats() {
        return String.format("Tokens: %d/%d (threshold: %d, available: %d)", 
            conversationTokens, 
            ModConfig.get().getActiveContextWindow(),
            getSummarizationThreshold(),
            getEffectiveAvailableContext());
    }
}
