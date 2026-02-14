package com.beckytidus.girlfriendmod.ai;

import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class RelationshipManager {
    // ... existing fields and methods ...
    private final GirlFriendEntity entity;
    private int relationshipLevel;

    public RelationshipManager(GirlFriendEntity entity) {
        this.entity = entity;
        this.relationshipLevel = entity.getRelationshipLevel();
    }

    /**
     * Represents a single item request with optional quantity.
     * Used when the player asks for items.
     */
    public static class ItemRequest {
        public final String itemName;
        public final Integer quantity; // null = give all, specific number = give that many

        public ItemRequest(String itemName, Integer quantity) {
            this.itemName = itemName;
            this.quantity = quantity;
        }

        @Override
        public String toString() {
            return quantity == null ? itemName : quantity + "x " + itemName;
        }
    }

    public static class InteractionResult {
        public String reasoning;
        public String sentiment; // "positive", "negative", "neutral"
        public String action; // "give_item", "none", "compliment_response", "insult_response"
        public String item; // item name or null (legacy, for single items)
        public List<ItemRequest> items; // list of item requests (for multiple items)

        public InteractionResult(String reasoning, String sentiment, String action, String item) {
            this.reasoning = reasoning;
            this.sentiment = sentiment;
            this.action = action;
            this.item = item;
            this.items = new ArrayList<>();
        }

        public InteractionResult(String reasoning, String sentiment, String action, List<ItemRequest> items) {
            this.reasoning = reasoning;
            this.sentiment = sentiment;
            this.action = action;
            this.item = items.isEmpty() ? null : items.get(0).itemName;
            this.items = items;
        }

        /**
         * Check if this result has multiple item requests.
         */
        public boolean hasMultipleItems() {
            return items != null && items.size() > 1;
        }

        /**
         * Check if any item request asks for "all" of an item.
         */
        public boolean hasGiveAllRequest() {
            return items != null && items.stream().anyMatch(req -> req.quantity == null);
        }
    }

    /**
     * Analyzes player message for sentiment and item requests with reasoning
     */
    public static CompletableFuture<InteractionResult> analyzeInteraction(
        String userMessage,
        List<ChutesClient.ChatMessage> history,
        List<String> inventoryItems,
        String girlfriendName
    ) {
        // ... existing prompt logic ...
        String inventoryListStr = inventoryItems.isEmpty() ? "Empty" :
            inventoryItems.stream()
                .map(s -> "\"" + s + "\"")
                .collect(java.util.stream.Collectors.joining(", "));

        String conversationContext = history.stream()
            .skip(Math.max(0, history.size() - 3))
            .map(msg -> {
                String role = msg.role.equals("assistant") ? girlfriendName : msg.role;
                return role + ": " + msg.content;
            })
            .collect(java.util.stream.Collectors.joining("\n"));

        String systemPrompt = String.format("""
            You are a logic engine analyzing player messages for a girlfriend AI named %s.

            THINKING PROCESS:
            1. First, analyze the CONTEXT: Is the player talking to %s directly? Or about something else?
            2. Then, analyze SENTIMENT: Is this a compliment, insult, or neutral statement TOWARD %s?
            3. Then, analyze INTENT: Is the player asking for items from %s's inventory?
            4. Finally, make a DECISION based on your analysis.

            IMPORTANT RULES:
            - If the player is talking about someone/something else (e.g., "that villager is stupid"), it's NEUTRAL sentiment
            - Only count as POSITIVE/NEGATIVE if directed at %s (e.g., "you are beautiful" vs "that is beautiful")
            - For item requests, be specific: "give me food" = look for any food item in inventory
            - For vague requests like "give me that", check conversation context for what "that" refers to
            - If unsure about sentiment, default to NEUTRAL
            - If unsure about item request, default to NONE

            ITEM REQUEST PARSING:
            - "give me diamonds" -> items: [{"name": "Diamond", "quantity": null}] (null = give all)
            - "give me 32 diamonds" -> items: [{"name": "Diamond", "quantity": 32}]
            - "give me all your diamonds" -> items: [{"name": "Diamond", "quantity": null}]
            - "give me diamonds and iron" -> items: [{"name": "Diamond", "quantity": null}, {"name": "Iron Ingot", "quantity": null}]
            - "give me 10 diamonds and 20 iron" -> items: [{"name": "Diamond", "quantity": 10}, {"name": "Iron Ingot", "quantity": 20}]
            - Match item names to the available inventory (case-insensitive, partial matches OK)
            - If quantity not specified, use null to indicate "give all"

            Available inventory: [%s]

            Conversation context (last 3 messages):
            %s

            Player's latest message: "%s"

            Output format (JSON only):
            {
              "reasoning": "Brief explanation of your analysis",
              "sentiment": "positive|negative|neutral",
              "action": "give_item|none|compliment_response|insult_response",
              "items": [{"name": "item_name", "quantity": number_or_null}]
            }

            Only output the JSON object, nothing else.
            """,
            girlfriendName, girlfriendName, girlfriendName, girlfriendName, girlfriendName,
            inventoryListStr, conversationContext, userMessage
        );

        return AIClientManager.generateRaw(history, systemPrompt).thenApply(response -> {
            if (response == null) {
                return new InteractionResult("No response from AI", "neutral", "none", (String) null);
            }

            // USE cleanJsonResponse instead of cleanResponse
            // This preserves the closing '}' that was being stripped
            String cleaned = ResponseCleaner.cleanJsonResponse(response).trim();

            try {
                // Try to extract JSON from the response
                String jsonStr = cleaned;

                // Handle Markdown Code Blocks (which might now appear in raw response)
                if (jsonStr.contains("```")) {
                    int jsonStart = jsonStr.indexOf("{");
                    if (jsonStart != -1) {
                         // Find the closing brace AFTER the start
                         int jsonEnd = jsonStr.lastIndexOf("}");
                         if (jsonEnd != -1 && jsonEnd > jsonStart) {
                             jsonStr = jsonStr.substring(jsonStart, jsonEnd + 1);
                         }
                    }
                } else {
                    // Standard extraction
                    int startIdx = jsonStr.indexOf('{');
                    int endIdx = jsonStr.lastIndexOf('}');
                    if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
                        jsonStr = jsonStr.substring(startIdx, endIdx + 1);
                    }
                }

                JsonObject json = JsonParser.parseString(jsonStr).getAsJsonObject();
                
                // ... rest of processing ...
                String reasoning = json.has("reasoning") ? json.get("reasoning").getAsString() : "No reasoning provided";
                String sentiment = json.has("sentiment") ? json.get("sentiment").getAsString().toLowerCase() : "neutral";
                String action = json.has("action") ? json.get("action").getAsString().toLowerCase() : "none";
                String item = json.has("item") && !json.get("item").isJsonNull() ?
                             json.get("item").getAsString() : null;

                // Validate sentiment
                if (!sentiment.equals("positive") && !sentiment.equals("negative")) {
                    sentiment = "neutral";
                }

                // Validate action
                if (!action.equals("give_item") && !action.equals("compliment_response") &&
                    !action.equals("insult_response")) {
                    action = "none";
                }

                // Parse items array if present (new format)
                List<ItemRequest> parsedItems = new ArrayList<>();
                if (action.equals("give_item") && json.has("items") && json.get("items").isJsonArray()) {
                    JsonArray itemsArray = json.getAsJsonArray("items");
                    for (JsonElement elem : itemsArray) {
                        if (elem.isJsonObject()) {
                            JsonObject itemObj = elem.getAsJsonObject();
                            String itemName = itemObj.has("name") ? itemObj.get("name").getAsString() : null;
                            Integer quantity = null;
                            if (itemObj.has("quantity") && !itemObj.get("quantity").isJsonNull()) {
                                try {
                                    quantity = itemObj.get("quantity").getAsInt();
                                } catch (NumberFormatException e) {
                                    quantity = null; // Default to "all" if parsing fails
                                }
                            }
                            if (itemName != null && !itemName.equalsIgnoreCase("null") && !itemName.equalsIgnoreCase("missing")) {
                                // Match item name to inventory (case-insensitive, partial matches OK)
                                String matchedName = inventoryItems.stream()
                                    .filter(invItem -> invItem.equalsIgnoreCase(itemName) ||
                                        invItem.toLowerCase().contains(itemName.toLowerCase()) ||
                                        itemName.toLowerCase().contains(invItem.toLowerCase()))
                                    .findFirst()
                                    .orElse(null);
                                if (matchedName != null) {
                                    parsedItems.add(new ItemRequest(matchedName, quantity));
                                }
                            }
                        }
                    }
                }
                
                // Fallback to legacy single item format if no items array or empty
                if (parsedItems.isEmpty() && action.equals("give_item")) {
                    final String finalItem = item;
                    if (finalItem == null || finalItem.equalsIgnoreCase("null") || finalItem.equalsIgnoreCase("missing")) {
                        // Still create an empty result for MISSING
                    } else {
                        // Check if item exists in inventory (case-insensitive)
                        String matchedName = inventoryItems.stream()
                            .filter(invItem -> invItem.equalsIgnoreCase(finalItem) ||
                                invItem.toLowerCase().contains(finalItem.toLowerCase()) ||
                                finalItem.toLowerCase().contains(invItem.toLowerCase()))
                            .findFirst()
                            .orElse("MISSING");
                        if (!matchedName.equals("MISSING")) {
                            parsedItems.add(new ItemRequest(matchedName, null)); // null quantity = give all
                        }
                    }
                }

                // Create result with items list
                if (!parsedItems.isEmpty()) {
                    return new InteractionResult(reasoning, sentiment, action, parsedItems);
                } else {
                    // No valid items found
                    InteractionResult result = new InteractionResult(reasoning, sentiment, action, (String) null);
                    result.item = "MISSING";
                    return result;
                }

            } catch (Exception e) {
                System.err.println("Failed to parse AI response as JSON: " + cleaned);
                System.err.println("Error: " + e.getMessage());

                // Fallback: simple keyword check (but with context awareness)
                String lowerMsg = userMessage.toLowerCase();
                String lowerGirlfriendName = girlfriendName.toLowerCase();

                // Check if message is directed at girlfriend
                boolean isDirected = lowerMsg.contains("you ") ||
                                    lowerMsg.contains(lowerGirlfriendName) ||
                                    lowerMsg.startsWith("hey ") ||
                                    lowerMsg.startsWith("hi ") ||
                                    lowerMsg.startsWith("hello ");

                String sentiment = "neutral";
                if (isDirected) {
                    // Very basic sentiment check (fallback only)
                    if (lowerMsg.contains("love") || lowerMsg.contains("beautiful") ||
                        lowerMsg.contains("amazing") || lowerMsg.contains("great") ||
                        lowerMsg.contains("awesome") || lowerMsg.contains("wonderful")) {
                        sentiment = "positive";
                    } else if (lowerMsg.contains("hate") || lowerMsg.contains("stupid") ||
                              lowerMsg.contains("ugly") || lowerMsg.contains("annoying") ||
                              lowerMsg.contains("idiot")) {
                        sentiment = "negative";
                    }
                }

                // Check for item request
                String action = "none";
                String itemResult = null;
                if (lowerMsg.contains("give me") || lowerMsg.contains("can i have") ||
                    lowerMsg.contains("hand me") || lowerMsg.contains("pass me") ||
                    lowerMsg.contains("got any")) {
                    action = "give_item";
                    itemResult = "MISSING"; // Can't determine item from keywords alone
                }

                InteractionResult fallbackResult = new InteractionResult(
                    "Fallback analysis (JSON parse failed)",
                    sentiment,
                    action,
                    (String) null
                );
                fallbackResult.item = itemResult;
                return fallbackResult;
            }
        });
    }

    // ... rest of the class methods (processInteraction, etc.) ...
    public void processInteraction(String userMessage, List<ChutesClient.ChatMessage> history) {
        List<String> inventory = entity.getItemNamesFromInventory();
        String name = entity.getNameForContext();

        analyzeInteraction(userMessage, history, inventory, name).thenAccept(result -> {
            // Apply relationship changes based on sentiment
            if (result.sentiment.equals("positive")) {
                int increase = calculatePositiveIncrease();
                entity.addRelationship(increase);

                // Log the reasoning
                entity.getMemory().addMessage("system",
                    String.format("Player complimented %s: \"%s\" [Reasoning: %s] (+%d relationship)",
                        name, userMessage, result.reasoning, increase));

                // Visual feedback for positive interaction
                if (entity.getEntityWorld() instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(ParticleTypes.HEART,
                        entity.getX(), entity.getY() + 1.5, entity.getZ(),
                        3, 0.3, 0.3, 0.3, 0.05);
                }

            } else if (result.sentiment.equals("negative")) {
                int decrease = calculateNegativeDecrease();
                entity.addRelationship(-decrease);

                entity.getMemory().addMessage("system",
                    String.format("Player insulted %s: \"%s\" [Reasoning: %s] (-%d relationship)",
                        name, userMessage, result.reasoning, decrease));

                // Visual feedback for negative interaction
                if (entity.getEntityWorld() instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(ParticleTypes.ANGRY_VILLAGER,
                        entity.getX(), entity.getY() + 1.5, entity.getZ(),
                        3, 0.3, 0.3, 0.3, 0.05);
                }
            } else {
                // Neutral interaction - just log it
                entity.getMemory().addMessage("system",
                    String.format("Player said to %s: \"%s\" [Reasoning: %s]",
                        name, userMessage, result.reasoning));
            }

            // Handle actions
            handleInteractionResult(result, userMessage);
        });
    }

    // ... rest of helper methods ...
    private int calculatePositiveIncrease() {
        int current = relationshipLevel;
        if (current < 25) return 5;
        if (current < 50) return 3;
        if (current < 75) return 2;
        if (current < 90) return 1;
        return 0;
    }

    private int calculateNegativeDecrease() {
        int current = relationshipLevel;
        if (current > 75) return 10;
        if (current > 50) return 7;
        if (current > 25) return 5;
        return 3;
    }

    // Relationship-based bonuses
    public double getCombatDamageMultiplier() {
        if (relationshipLevel >= 90) return 1.25;
        if (relationshipLevel >= 75) return 1.15;
        if (relationshipLevel >= 50) return 1.10;
        if (relationshipLevel >= 25) return 1.05;
        return 1.0;
    }

    public double getHealingMultiplier() {
        if (relationshipLevel >= 90) return 1.50;
        if (relationshipLevel >= 75) return 1.30;
        if (relationshipLevel >= 50) return 1.15;
        if (relationshipLevel >= 25) return 1.05;
        return 1.0;
    }

    public double getDamageReductionMultiplier() {
        if (relationshipLevel >= 90) return 0.75;
        if (relationshipLevel >= 75) return 0.85;
        if (relationshipLevel >= 50) return 0.90;
        if (relationshipLevel >= 25) return 0.95;
        return 1.0;
    }

    public boolean willShareRareItems() {
        return relationshipLevel > 60;
    }

    public boolean willTakeDamageForOwner() {
        return relationshipLevel > 80;
    }

    public boolean willForgiveAccidentalHits() {
        return relationshipLevel > 20;
    }

    public boolean willAutoHealOwner() {
        return relationshipLevel > 40;
    }

    public int getRelationshipLevel() {
        return relationshipLevel;
    }

    /**
     * FIXED: Removed the recursive call to entity.setRelationshipLevel()
     * This method now just updates the manager's local state and checks milestones.
     */
    public void setRelationshipLevel(int level) {
        int oldLevel = relationshipLevel;
        this.relationshipLevel = Math.max(0, Math.min(100, level));
        
        // REMOVED: entity.setRelationshipLevel(this.relationshipLevel); 
        // The Entity is the one who called this method, so calling it back causes a loop!
        
        checkMilestoneTransition(oldLevel, this.relationshipLevel);
    }

    public void addRelationship(int amount) {
        int oldLevel = relationshipLevel;
        setRelationshipLevel(relationshipLevel + amount);
    }

    private void checkMilestoneTransition(int oldLevel, int newLevel) {
        // ... [milestone logic remains unchanged] ...
        int[] milestones = {25, 50, 75, 90, 100};
        for (int milestone : milestones) {
            if (oldLevel < milestone && newLevel >= milestone) {
                onMilestoneReached(milestone);
            }
        }
        for (int milestone : milestones) {
            if (oldLevel >= milestone && newLevel < milestone) {
                onMilestoneLost(milestone);
            }
        }
    }

    private void onMilestoneReached(int milestone) {
        String name = entity.getNameForContext();
        String message = "";
        String aiPrompt = "";

        switch(milestone) {
            case 25:
                message = name + " reached relationship level 25! " + name + " now forgives accidental hits.";
                aiPrompt = name + " just reached relationship level 25! Express happiness about growing closer and mention that you'll forgive accidental hits.";
                break;
            case 50:
                message = name + " reached relationship level 50! " + name + " now shares rare items more willingly and heals you when you're hurt.";
                aiPrompt = name + " just reached relationship level 50! Express deep affection and commitment, mentioning you'll share rare items and heal them when needed.";
                break;
            case 75:
                message = name + " reached relationship level 75! " + name + " will take more risks to protect you and deals more damage in combat.";
                aiPrompt = name + " just reached relationship level 75! Express strong devotion and willingness to protect them at all costs.";
                break;
            case 90:
                message = name + " reached relationship level 90! " + name + " is deeply devoted to you and takes significantly less damage.";
                aiPrompt = name + " just reached relationship level 90! Express profound love and commitment, saying you'd do anything for them.";
                break;
            case 100:
                message = name + " reached max relationship level 100! " + name + " is completely devoted to you. ♥";
                aiPrompt = name + " just reached the maximum relationship level 100! Express ultimate love and devotion, saying this is the happiest moment.";
                break;
        }

        entity.getMemory().addMessage("system", "Relationship milestone: " + message);

        if (entity.getOwner() != null) {
            entity.getOwner().sendMessage(
                Text.literal("♥ " + message).formatted(Formatting.LIGHT_PURPLE),
                false
            );
        }

        if (entity.getEntityWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(
                ParticleTypes.HEART,
                entity.getX(), entity.getY() + 2, entity.getZ(),
                15, 0.5, 0.5, 0.5, 0.2
            );

            if (milestone >= 75) {
                serverWorld.spawnParticles(
                    ParticleTypes.END_ROD,
                    entity.getX(), entity.getY() + 2, entity.getZ(),
                    10, 0.3, 0.3, 0.3, 0.1
                );
            }
        }

        entity.generateAndSayResponse(aiPrompt);
    }

    private void onMilestoneLost(int milestone) {
        String name = entity.getNameForContext();
        String message = "";

        switch(milestone) {
            case 25: message = name + " fell below relationship level 25. " + name + " no longer forgives accidental hits."; break;
            case 50: message = name + " fell below relationship level 50. " + name + " is less willing to share rare items."; break;
            case 75: message = name + " fell below relationship level 75. " + name + " is less protective."; break;
            case 90: message = name + " fell below relationship level 90. " + name + " feels less devoted."; break;
            case 100: message = name + " fell below max relationship level. " + name + " is heartbroken."; break;
        }

        entity.getMemory().addMessage("system", "Relationship downgrade: " + message);

        if (entity.getOwner() != null) {
            entity.getOwner().sendMessage(Text.literal("⚠ " + message).formatted(Formatting.YELLOW), false);
        }
    }

    private void handleInteractionResult(InteractionResult result, String userMessage) {
        String name = entity.getNameForContext();

        switch(result.action) {
            case "give_item":
                // Check if we have multiple items in the new format
                if (result.items != null && !result.items.isEmpty()) {
                    handleMultipleItemRequest(result.items, userMessage);
                } else {
                    // Fallback to legacy single item
                    handleItemRequest(result.item, userMessage);
                }
                break;

            case "compliment_response":
                entity.generateAndSayResponse(String.format(
                    "Player complimented %s: \"%s\" [Reasoning: %s]. Respond with genuine appreciation and affection.",
                    name, userMessage, result.reasoning
                ));
                break;

            case "insult_response":
                String responseType = relationshipLevel > 50 ?
                    "Respond with hurt feelings but willingness to forgive." :
                    "Respond defensively or with sadness.";

                entity.generateAndSayResponse(String.format(
                    "Player insulted %s: \"%s\" [Reasoning: %s]. %s",
                    name, userMessage, result.reasoning, responseType
                ));
                break;

            case "none":
            default:
                String context = String.format("Player said: \"%s\" [Context: %s]",
                    userMessage, result.reasoning);
                entity.generateAndSayResponse(context);
                break;
        }
    }

    /**
     * Handle requests for multiple items at once.
     */
    private void handleMultipleItemRequest(List<ItemRequest> items, String userMessage) {
        if (items == null || items.isEmpty()) {
            entity.generateAndSayResponse(String.format(
                "The user asked for something, but %s couldn't understand what they wanted.",
                entity.getNameForContext()
            ));
            return;
        }

        // Convert to GirlFriendEntity.ItemRequest list
        List<GirlFriendEntity.ItemRequest> entityRequests = new ArrayList<>();
        for (ItemRequest req : items) {
            entityRequests.add(new GirlFriendEntity.ItemRequest(req.itemName, req.quantity));
        }

        GirlFriendEntity.MultiGiveResult result = entity.giveItems(entityRequests);
        String name = entity.getNameForContext();
        String response;

        if (result.hasAnySuccess()) {
            // Build success message
            StringBuilder givenStr = new StringBuilder();
            for (Map.Entry<String, Integer> entry : result.givenItems.entrySet()) {
                if (givenStr.length() > 0) givenStr.append(", ");
                givenStr.append(entry.getValue()).append("x ").append(entry.getKey());
            }

            response = String.format("%s gave the player: %s. Express happiness about sharing.", name, givenStr);

            // Add relationship for each item given (if willing to share rare items or item isn't rare)
            for (String itemName : result.givenItems.keySet()) {
                if (willShareRareItems() || !isRareItem(itemName)) {
                    entity.addRelationship(1);
                }
            }

            // If there were also failures, mention them
            if (!result.failedItems.isEmpty() || !result.notFoundItems.isEmpty()) {
                response += " However, ";
                if (!result.failedItems.isEmpty()) {
                    response += "some items couldn't be given because the player's inventory is full. ";
                }
                if (!result.notFoundItems.isEmpty()) {
                    response += "some items weren't found in " + name + "'s inventory. ";
                }
                response += "Apologize for the partial success.";
            }
        } else if (!result.failedItems.isEmpty()) {
            response = String.format("%s tried to give items but the player's inventory is full. Apologize and suggest they make space.", name);
        } else {
            // Nothing was found
            StringBuilder notFoundStr = new StringBuilder();
            for (String itemName : result.notFoundItems) {
                if (notFoundStr.length() > 0) notFoundStr.append(", ");
                notFoundStr.append(itemName);
            }
            response = String.format("%s couldn't find %s to give. Apologize for not having the requested items.", name, notFoundStr);
        }

        entity.generateAndSayResponse(response);
    }

    private void handleItemRequest(String itemName, String userMessage) {
        if (itemName == null || itemName.equals("MISSING") || itemName.equals("null")) {
            entity.generateAndSayResponse(String.format(
                "The user asked for something, but %s doesn't have that item or couldn't understand what they wanted.",
                entity.getNameForContext()
            ));
            return;
        }

        GirlFriendEntity.GiveResult result = entity.giveSpecificItem(itemName);
        String name = entity.getNameForContext();
        String response;

        switch (result) {
            case SUCCESS:
                response = String.format("%s gave %s to the player. Express happiness about sharing.", name, itemName);
                if (willShareRareItems() || !isRareItem(itemName)) {
                    entity.addRelationship(1);
                }
                break;

            case PARTIAL:
                response = String.format("%s gave some of the %s to the player (inventory was partially full). Express happiness about sharing what could be given.", name, itemName);
                if (willShareRareItems() || !isRareItem(itemName)) {
                    entity.addRelationship(1);
                }
                break;

            case FULL:
                response = String.format("%s tried to give %s but the player's inventory is full. Apologize and suggest they make space.", name, itemName);
                break;

            default:
                response = String.format("%s couldn't find %s to give. Apologize for not having it.", name, itemName);
                break;
        }

        entity.generateAndSayResponse(response);
    }

    private boolean isRareItem(String itemName) {
        if (itemName == null) return false;
        String lower = itemName.toLowerCase();
        return lower.contains("diamond") || lower.contains("emerald") ||
               lower.contains("netherite") || lower.contains("ender") ||
               lower.contains("enchanted") || lower.contains("gold") ||
               lower.contains("iron") || lower.contains("ancient") || 
               lower.contains("nether star") || lower.contains("elytra") || 
               lower.contains("shulker") || lower.contains("totem") || lower.contains("beacon");
    }

    public boolean shouldTakeDamageForOwner(float damageAmount) {
        if (!willTakeDamageForOwner()) return false;
        float maxHealth = entity.getMaxHealth();
        float currentHealth = entity.getHealth();
        float healthPercentage = currentHealth / maxHealth;
        if (healthPercentage < 0.3f) return false;
        if (damageAmount > currentHealth * 0.5f) return false;
        return true;
    }
}