package com.beckytidus.girlfriendmod.ai;

/**
 * Represents a response from an AI provider including both content and token usage information.
 */
public class AIResponse {
    public final String content;
    public final TokenUsage tokenUsage;
    
    public AIResponse(String content, TokenUsage tokenUsage) {
        this.content = content;
        this.tokenUsage = tokenUsage;
    }
    
    /**
     * Creates an AIResponse with no token usage information.
     * Used for error cases or when token counting is not available.
     */
    public AIResponse(String content) {
        this.content = content;
        this.tokenUsage = null;
    }
    
    /**
     * Checks if this response has valid token usage information.
     */
    public boolean hasTokenUsage() {
        return tokenUsage != null && tokenUsage.promptTokens > 0;
    }
    
    /**
     * Represents token usage information from an API response.
     */
    public static class TokenUsage {
        public final int promptTokens;
        public final int completionTokens;
        public final int totalTokens;
        
        public TokenUsage(int promptTokens, int completionTokens, int totalTokens) {
            this.promptTokens = promptTokens;
            this.completionTokens = completionTokens;
            this.totalTokens = totalTokens;
        }
        
        /**
         * Creates a TokenUsage from a JSON usage object.
         */
        public static TokenUsage fromJson(com.google.gson.JsonObject usage) {
            if (usage == null) return null;
            
            int prompt = usage.has("prompt_tokens") ? usage.get("prompt_tokens").getAsInt() : 0;
            int completion = usage.has("completion_tokens") ? usage.get("completion_tokens").getAsInt() : 0;
            int total = usage.has("total_tokens") ? usage.get("total_tokens").getAsInt() : prompt + completion;
            
            return new TokenUsage(prompt, completion, total);
        }
        
        @Override
        public String toString() {
            return String.format("TokenUsage{prompt=%d, completion=%d, total=%d}", 
                promptTokens, completionTokens, totalTokens);
        }
    }
}
