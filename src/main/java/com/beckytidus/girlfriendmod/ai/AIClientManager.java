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
