package com.beckytidus.girlfriendmod.ai;

import com.beckytidus.girlfriendmod.config.ModConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.text.Text;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ChutesClient {
    private static final String API_URL = "https://llm.chutes.ai/v1/chat/completions";
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final Gson gson = new Gson();

    public static CompletableFuture<String> generateResponse(List<ChatMessage> history, String systemContext) {
        String apiKey = ModConfig.get().apiKey;
        if (apiKey == null || apiKey.isEmpty()) {
            return CompletableFuture.completedFuture("Please set your Chutes AI API Key in /girlfriend config");
        }

        JsonObject body = new JsonObject();
        body.addProperty("model", ModConfig.get().modelName);
        body.addProperty("stream", false);
        body.addProperty("max_tokens", 1024);
        body.addProperty("temperature", 0.85);

        JsonArray messages = new JsonArray();
        
        // System Prompt
        JsonObject system = new JsonObject();
        system.addProperty("role", "system");
        system.addProperty("content", "You are a loving, loyal girlfriend in Minecraft. " +
                "Keep responses concise (under 2 sentences) unless told a story. " +
                "Use ♥ sparingly. Current game context: " + systemContext);
        messages.add(system);

        // History
        for (ChatMessage msg : history) {
            JsonObject m = new JsonObject();
            m.addProperty("role", msg.role);
            m.addProperty("content", msg.content);
            messages.add(m);
        }

        body.add("messages", messages);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        return "Error: " + response.statusCode() + " - " + response.body();
                    }
                    try {
                        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                        return json.getAsJsonArray("choices")
                                .get(0).getAsJsonObject()
                                .get("message").getAsJsonObject()
                                .get("content").getAsString();
                    } catch (Exception e) {
                        return "Error parsing response: " + e.getMessage();
                    }
                });
    }

    public static CompletableFuture<String> summarize(List<ChatMessage> history) {
        // Create a temporary request to summarize
        List<ChatMessage> summaryPrompt = new ArrayList<>(history);
        summaryPrompt.add(new ChatMessage("user", "Summarize our conversation and your memories of me so far in detail while keeping it concise."));
        return generateResponse(summaryPrompt, "You are a helpful assistant summarizer.");
    }

    public static class ChatMessage {
        public String role;
        public String content;

        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}