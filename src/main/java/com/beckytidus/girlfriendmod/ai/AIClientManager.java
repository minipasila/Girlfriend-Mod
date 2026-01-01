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
     * Analyzes the conversation history to determine if the user is asking for a gift.
     * Uses the latest messages for context.
     */
    public static CompletableFuture<Boolean> analyzeIntent(String userMessage, List<ChutesClient.ChatMessage> history) {
        String systemPrompt = "You are a logic engine. Analyze the conversation history, specifically the latest message from the user, to see if they are explicitly asking for an item, gift, food, or resource from you. " +
            "If they are asking for an item (e.g. 'can i have a diamond', 'give me food', 'hand it over', 'do you have that?'), output 'ACTION_GIVE'. " + 
            "If they are just chatting (e.g. 'hello', 'what is that', 'cool'), output 'ACTION_NONE'. " +
            "Only output the action code.";
        
        return generateRaw(history, systemPrompt).thenApply(response -> response != null && response.toUpperCase().contains("ACTION_GIVE"));
    }

    /**
     * Asks the AI to select an item from the provided inventory list based on the user's request and context.
     * Returns "MISSING" if no match found, or the exact item name.
     */
    public static CompletableFuture<String> selectItemFromInventory(List<String> inventoryNames, String userMessage, List<ChutesClient.ChatMessage> history) {
        if (inventoryNames.isEmpty()) {
            return CompletableFuture.completedFuture("EMPTY");
        }

        String inventoryListStr = inventoryNames.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(", "));
        
        String systemPrompt = "You are a logic engine. The user is asking for an item from your inventory. " +
            "The available items are: [" + inventoryListStr + "]. " +
            "Analyze the conversation history and the user's latest message: \"" + userMessage + "\". " +
            "Rules:\n" +
            "1. If the user asks for a specific item, or refers to one from context (e.g. 'give me that', 'the food'), pick the best matching item from the list and return its name.\n" +
            "2. If the user asks for 'food' or a general category, pick the best matching item from the list and return its name.\n" +
            "3. If the user just asks for 'a gift' or 'something', pick the most valuable or logical item from the list and return its name.\n" +
            "4. If the requested item is NOT in the provided list, return ONLY the word 'MISSING'.\n" +
            "5. Output ONLY the item name or the word 'MISSING'. Do not write sentences or additional text.";

        return generateRaw(history, systemPrompt).thenApply(response -> {
            if (response == null) return "MISSING";
            
            String rawResponse = response.trim();
            if (rawResponse.endsWith(".")) {
                rawResponse = rawResponse.substring(0, rawResponse.length() - 1);
            }
            final String clean = rawResponse;
            
            if (inventoryNames.stream().anyMatch(name -> name.equalsIgnoreCase(clean)) || clean.equalsIgnoreCase("MISSING")) {
                return clean;
            } else {
                return "MISSING";
            }
        });
    }

    /**
     * Asks the AI to select the least valuable/useless item to drop from the inventory.
     */
    public static CompletableFuture<String> selectItemToDrop(List<String> inventoryNames, List<ChutesClient.ChatMessage> history) {
        if (inventoryNames.isEmpty()) {
            return CompletableFuture.completedFuture("NONE");
        }

        String inventoryListStr = inventoryNames.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(", "));
        
        String systemPrompt = "You are a logic engine managing an inventory in Minecraft. The inventory is full. " +
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
            
            String clean = response.trim();
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