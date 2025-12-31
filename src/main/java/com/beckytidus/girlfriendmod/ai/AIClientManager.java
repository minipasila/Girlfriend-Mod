package com.beckytidus.girlfriendmod.ai;

import com.beckytidus.girlfriendmod.config.ModConfig;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Unified AI client that routes requests to the selected provider.
 */
public class AIClientManager {

    /**
     * Generates a response using the configured AI provider.
     */
    public static CompletableFuture<String> generateResponse(List<ChutesClient.ChatMessage> history, String systemContext) {
        ModConfig config = ModConfig.get();

        switch (config.aiProvider) {
            case OPENROUTER:
                List<OpenRouterClient.ChatMessage> orMessages = new java.util.ArrayList<>();
                for (ChutesClient.ChatMessage msg : history) {
                    orMessages.add(new OpenRouterClient.ChatMessage(msg.role, msg.content));
                }
                return OpenRouterClient.generateResponse(orMessages, systemContext);

            case KOBOLDCPP:
                List<KoboldCppClient.ChatMessage> kcMessages = new java.util.ArrayList<>();
                for (ChutesClient.ChatMessage msg : history) {
                    kcMessages.add(new KoboldCppClient.ChatMessage(msg.role, msg.content));
                }
                return KoboldCppClient.generateResponse(kcMessages, systemContext);

            case CHUTES:
            default:
                return ChutesClient.generateResponse(history, systemContext);
        }
    }

    /**
     * Generates a response using the configured AI provider with a raw system prompt.
     * Used for internal logic checks.
     */
    public static CompletableFuture<String> generateRaw(List<ChutesClient.ChatMessage> history, String systemPrompt) {
        ModConfig config = ModConfig.get();

        switch (config.aiProvider) {
            case OPENROUTER:
                List<OpenRouterClient.ChatMessage> orMessages = new java.util.ArrayList<>();
                for (ChutesClient.ChatMessage msg : history) {
                    orMessages.add(new OpenRouterClient.ChatMessage(msg.role, msg.content));
                }
                return OpenRouterClient.generateRaw(orMessages, systemPrompt);

            case KOBOLDCPP:
                List<KoboldCppClient.ChatMessage> kcMessages = new java.util.ArrayList<>();
                for (ChutesClient.ChatMessage msg : history) {
                    kcMessages.add(new KoboldCppClient.ChatMessage(msg.role, msg.content));
                }
                return KoboldCppClient.generateRaw(kcMessages, systemPrompt);

            case CHUTES:
            default:
                return ChutesClient.generateRaw(history, systemPrompt);
        }
    }

    /**
     * Analyzes the user's message to determine if they are asking for a gift.
     */
    public static CompletableFuture<Boolean> analyzeIntent(String userMessage) {
        String systemPrompt = "You are a logic engine. Analyze the user's message to see if they are explicitly asking for an item, gift, food, or resource. " +
            "If they are asking for an item (e.g. 'can I have a diamond', 'give me food'), output 'ACTION_GIVE'. " + 
            "If they are just chatting (e.g. 'hello', 'what is that'), output 'ACTION_NONE'. " +
            "Only output the action code.";
        
        List<ChutesClient.ChatMessage> msgs = List.of(new ChutesClient.ChatMessage("user", userMessage));
        
        return generateRaw(msgs, systemPrompt).thenApply(response -> response != null && response.toUpperCase().contains("ACTION_GIVE"));
    }

    /**
     * Summarizes conversation history using the configured AI provider.
     */
    public static CompletableFuture<String> summarize(List<ChutesClient.ChatMessage> history) {
        ModConfig config = ModConfig.get();

        switch (config.aiProvider) {
            case OPENROUTER:
                List<OpenRouterClient.ChatMessage> orMessages = new java.util.ArrayList<>();
                for (ChutesClient.ChatMessage msg : history) {
                    orMessages.add(new OpenRouterClient.ChatMessage(msg.role, msg.content));
                }
                return OpenRouterClient.summarize(orMessages);

            case KOBOLDCPP:
                List<KoboldCppClient.ChatMessage> kcMessages = new java.util.ArrayList<>();
                for (ChutesClient.ChatMessage msg : history) {
                    kcMessages.add(new KoboldCppClient.ChatMessage(msg.role, msg.content));
                }
                return KoboldCppClient.summarize(kcMessages);

            case CHUTES:
            default:
                return ChutesClient.summarize(history);
        }
    }

    /**
     * Check if the current AI provider is available
     */
    public static CompletableFuture<Boolean> checkProviderAvailable() {
        ModConfig config = ModConfig.get();

        if (config.aiProvider == ModConfig.AIProvider.KOBOLDCPP) {
            return KoboldCppClient.checkServerAvailable(config.koboldCppUrl);
        }

        // Other providers are cloud-based and assumed available
        return CompletableFuture.completedFuture(true);
    }
}