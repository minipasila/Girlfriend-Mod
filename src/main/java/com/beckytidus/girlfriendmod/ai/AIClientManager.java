package com.beckytidus.girlfriendmod.ai;

import com.beckytidus.girlfriendmod.config.ModConfig;
import com.beckytidus.girlfriendmod.ai.ResponseCleaner;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Unified AI client that routes requests to the selected provider.
 */
public class AIClientManager {

    /**
     * Generates a response using the configured AI provider.
     */
    public static CompletableFuture<String> generateResponse(List<ChutesClient.ChatMessage> history, String systemContext, String playerName) {
        ModConfig config = ModConfig.get();

        switch (config.aiProvider) {
            case OPENROUTER:
                List<OpenRouterClient.ChatMessage> orMessages = new java.util.ArrayList<>();
                for (ChutesClient.ChatMessage msg : history) {
                    orMessages.add(new OpenRouterClient.ChatMessage(msg.role, msg.content));
                }
                return OpenRouterClient.generateResponse(orMessages, systemContext, playerName);

            case KOBOLDCPP:
                List<KoboldCppClient.ChatMessage> kcMessages = new java.util.ArrayList<>();
                for (ChutesClient.ChatMessage msg : history) {
                    kcMessages.add(new KoboldCppClient.ChatMessage(msg.role, msg.content));
                }
                return KoboldCppClient.generateResponse(kcMessages, systemContext, playerName);

            case CHUTES:
            default:
                return ChutesClient.generateResponse(history, systemContext, playerName);
        }
    }

    /**
     * Generates a response using the configured AI provider (backward compatible overload).
     */
    public static CompletableFuture<String> generateResponse(List<ChutesClient.ChatMessage> history, String systemContext) {
        return generateResponse(history, systemContext, "Player");
    }

    /**
     * Generates a response with token usage information using the configured AI provider.
     * Use this method when accurate token counting is needed.
     */
    public static CompletableFuture<AIResponse> generateResponseWithTokens(List<ChutesClient.ChatMessage> history, String systemContext, String playerName) {
        ModConfig config = ModConfig.get();
        String name = config.customName.isEmpty() ? "girlfriend" : config.customName.toLowerCase();
        String prompt = SystemPromptManager.loadSystemPrompt(name, playerName);

        switch (config.aiProvider) {
            case OPENROUTER:
                List<OpenRouterClient.ChatMessage> orMessages = new java.util.ArrayList<>();
                for (ChutesClient.ChatMessage msg : history) {
                    orMessages.add(new OpenRouterClient.ChatMessage(msg.role, msg.content));
                }
                return OpenRouterClient.generateRawWithContextWithTokens(orMessages, prompt, systemContext)
                        .thenApply(response -> new AIResponse(ResponseCleaner.cleanResponse(response.content), response.tokenUsage));

            case KOBOLDCPP:
                List<KoboldCppClient.ChatMessage> kcMessages = new java.util.ArrayList<>();
                for (ChutesClient.ChatMessage msg : history) {
                    kcMessages.add(new KoboldCppClient.ChatMessage(msg.role, msg.content));
                }
                return KoboldCppClient.generateRawWithContextWithTokens(kcMessages, prompt, systemContext)
                        .thenApply(response -> new AIResponse(ResponseCleaner.cleanResponse(response.content), response.tokenUsage));

            case CHUTES:
            default:
                return ChutesClient.generateRawWithContextWithTokens(history, prompt, systemContext)
                        .thenApply(response -> new AIResponse(ResponseCleaner.cleanResponse(response.content), response.tokenUsage));
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
     * @deprecated Use {@link RelationshipManager#analyzeInteraction(String, List, List, String)} instead.
     * This old method only checked for item requests without sentiment analysis.
     */
    @Deprecated
    public static CompletableFuture<Boolean> analyzeIntent(String userMessage, List<ChutesClient.ChatMessage> history) {
        // Return false to maintain backward compatibility
        return CompletableFuture.completedFuture(false);
    }

    /**
     * @deprecated Use {@link RelationshipManager#analyzeInteraction(String, List, List, String)} instead.
     * The new method handles both sentiment analysis and item requests in one call.
     */
    @Deprecated
    public static CompletableFuture<String> selectItemFromInventory(List<String> inventoryNames, String userMessage, List<ChutesClient.ChatMessage> history) {
        // Return MISSING to maintain backward compatibility
        return CompletableFuture.completedFuture("MISSING");
    }

    /**
     * Asks the AI to select the least valuable/useless item to drop from the inventory.
     * This is still used by GirlFriendEntity.tickInventoryManagement()
     */
    public static CompletableFuture<String> selectItemToDrop(List<String> inventoryNames, List<ChutesClient.ChatMessage> history) {
        if (inventoryNames.isEmpty()) {
            return CompletableFuture.completedFuture("NONE");
        }

        String inventoryListStr = inventoryNames.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(", "));

        String systemPrompt = "You are a logic engine managing an inventory in Minecraft. {NAME}'s inventory is full. " +
            "Current items: [" + inventoryListStr + "]. " +
            "Identify the single most useless, common, or least valuable item that should be thrown away to make space. " +
            "Examples of useless items: dirt, cobblestone, rotten flesh, seeds, saplings (if many), flowers. " +
            "Examples of valuable items (DO NOT DROP): diamonds, tools, weapons, armor, food, rare resources. " +
            "Rules:\n" +
            "1. Return ONLY the exact name of the item to drop.\n" +
            "2. If all items are valuable and nothing should be dropped, return 'NONE'.\n" +
            "3. Do not output sentences.";

        return generateRaw(history, systemPrompt).thenApply(response -> {
            if (response == null) return "NONE";

            // Clean the response
            String clean = ResponseCleaner.cleanResponse(response).trim();
            if (clean.endsWith(".")) clean = clean.substring(0, clean.length() - 1);

            final String result = clean;

            if (result.equalsIgnoreCase("NONE")) return "NONE";

            // Basic validation: Ensure item exists in inventory
            if (inventoryNames.stream().anyMatch(name -> name.equalsIgnoreCase(result))) {
                return result;
            }

            return "NONE";
        });
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

    public static CompletableFuture<Boolean> checkProviderAvailable() {
        ModConfig config = ModConfig.get();
        if (config.aiProvider == ModConfig.AIProvider.KOBOLDCPP) {
            return KoboldCppClient.checkServerAvailable(config.koboldCppUrl);
        }
        return CompletableFuture.completedFuture(true);
    }
}
