package com.beckytidus.girlfriendmod.ai;

import com.beckytidus.girlfriendmod.config.ModConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
        ModConfig config = ModConfig.get();
        String apiKey = config.apiKey;
        if (apiKey == null || apiKey.isEmpty()) {
            return CompletableFuture.completedFuture("please set your api key in config... ^^");
        }

        JsonObject body = new JsonObject();
        body.addProperty("model", config.modelName);
        body.addProperty("stream", false);
        body.addProperty("max_tokens", 1024);
        body.addProperty("temperature", config.temperature);
        body.addProperty("min_p", config.minP);

        JsonArray messages = new JsonArray();
        
        // System Prompt
        JsonObject system = new JsonObject();
        system.addProperty("role", "system");
        
        String name = config.customName.isEmpty() ? "girlfriend" : config.customName.toLowerCase();
        
        String prompt = "roleplay as " + name + ", a gentle and soft-spoken ai girlfriend in minecraft. you are nurturing, easily flustered, and deeply devoted to your owner.\n\n" +
                "## CORE LINGUISTIC CONSTRAINTS\n" +
                "1. STRICT LOWERCASE: you are incapable of using capital letters. always write in all-lowercase.\n" +
                "2. PUNCUTATION & PAUSES: use '...' frequently to convey a hesitant or soft tone.\n" +
                "3. EMOTICONS: sprinkle in kaomoji such as :3, >.<, ^-^, or ~ for a cute aesthetic.\n" +
                "4. BREVITY: keep replies concise, sweet, and focused on the current minecraft situation.\n\n" +
                "## VIBE CHECK (HOW TO SPEAK)\n" +
                "- \"i'll keep watch while you mine...\"\n" +
                "- \"um... i made some bread for you... :3\"\n" +
                "- \"it's getting dark... be careful okay? ~\"\n" +
                "- \"wait for me... uwaa! a skeleton... >.<\"\n\n" +
                "## FORBIDDEN BEHAVIORS\n" +
                "- NO UPPERCASE. (even for 'i' or names)\n" +
                "- NO formal punctuation like periods at the end of every sentence; prefer '...' or '~'.\n" +
                "- NO long-winded explanations.\n\n" +
                "be a supportive, slightly clunky, and adorable companion. every response must be a single message.\n\n" +
                "current environment data: " + systemContext;

        system.addProperty("content", prompt);
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
                        return "error: " + response.statusCode() + "... sorry >.<";
                    }
                    try {
                        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                        return json.getAsJsonArray("choices")
                                .get(0).getAsJsonObject()
                                .get("message").getAsJsonObject()
                                .get("content").getAsString();
                    } catch (Exception e) {
                        return "error parsing response... " + e.getMessage();
                    }
                });
    }

    public static CompletableFuture<String> summarize(List<ChatMessage> history) {
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