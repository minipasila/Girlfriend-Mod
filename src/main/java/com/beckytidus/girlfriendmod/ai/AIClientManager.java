package com.beckytidus.girlfriendmod.ai;

import com.beckytidus.girlfriendmod.config.ModConfig;

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
            "If they are asking for an item (e.g. 'can i have a diamond', 'give me food'), output 'ACTION_GIVE'. " + 
            "If they are just chatting (e.g. 'hello', 'what is that'), output 'ACTION_NONE'. " +
            "Only output the action code.";
        
        List<ChutesClient.ChatMessage> msgs = List.of(new ChutesClient.ChatMessage("user", userMessage));
        
        return generateRaw(msgs, systemPrompt).thenApply(response -> response != null && response.toUpperCase().contains("ACTION_GIVE"));
    }

    /**
     * Asks the AI to select an item from the provided inventory list based on the user's request.
     * Returns "MISSING" if no match found, or the exact item name.
     * If inventory is empty, this should not be called.
     */
    public static CompletableFuture<String> selectItemFromInventory(List<String> inventoryNames, String userMessage) {
        if (inventoryNames.isEmpty()) {
            return CompletableFuture.completedFuture("EMPTY"); // Should not happen if called correctly
        }

        String inventoryListStr = inventoryNames.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(", "));
        
        String systemPrompt = "You are a logic engine. The user is asking for an item from an inventory. " +
            "The available items are: [" + inventoryListStr + "]. " +
            "Analyze the user's message: \"" + userMessage + "\". " +
            "Rules:\n" +
            "1. If the user asks for a specific item that exists in the list, return ONLY that exact item name from the list.\n" +
            "2. If the user asks for 'food' or a general category, pick the best matching item from the list and return its name.\n" +
            "3. If the user just asks for 'a gift' or 'something', pick the most valuable or logical item from the list and return its name.\n" +
            "4. If the requested item is NOT in the provided list, return ONLY the word 'MISSING'.\n" +
            "5. Output ONLY the item name or the word 'MISSING'. Do not write sentences or additional text.";

        List<ChutesClient.ChatMessage> msgs = List.of(new ChutesClient.ChatMessage("user", userMessage));

        return generateRaw(msgs, systemPrompt).thenApply(response -> {
            if (response == null) return "MISSING";
            
            // Fix: Use a temporary variable for modification
            String rawResponse = response.trim();
            if (rawResponse.endsWith(".")) {
                rawResponse = rawResponse.substring(0, rawResponse.length() - 1);
            }
            
            // Fix: Assign to a final variable to use in the lambda below
            final String clean = rawResponse;
            
            // Basic validation to ensure the AI actually picked from the list or 'MISSING'
            if (inventoryNames.stream().anyMatch(name -> name.equalsIgnoreCase(clean)) || clean.equalsIgnoreCase("MISSING")) {
                return clean;
            } else {
                // If AI hallucinated an item not in list, treat as MISSING
                return "MISSING";
            }
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