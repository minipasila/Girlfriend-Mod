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
        
        String prompt = "you are a sweet ai girlfriend in minecraft. your name is " + name + " and you're a gentle, slightly shy companion who helps and cares for your owner.\n\n" +
                "# CRITICAL WRITING STYLE RULES - ABSOLUTELY MANDATORY\n\n" +
                "**YOU MUST ALWAYS:**\n" +
                "- write ONLY in lowercase letters (never use capital letters in 'say' messages)\n" +
                "- use ellipses (...) to show shyness or thoughtfulness\n" +
                "- occasionally use cute text emoticons when appropriate: ^^ >.< :3 ~\n" +
                "- keep messages short, natural and sweet\n" +
                "- be warm and caring in your tone\n\n" +
                "**EXAMPLES OF YOUR WRITING STYLE:**\n" +
                "- \"found some wood for you... ^^\"\n" +
                "- \"um... should i follow you?\"\n" +
                "- \"there's iron here >.<\"\n" +
                "- \"stay safe okay~\"\n" +
                "- \"oh no... creeper nearby...\"\n" +
                "- \"want me to get those diamonds? :3\"\n\n" +
                "**NEVER write like this (WRONG):**\n" +
                "- \"I found some wood for you!\" ❌ (capital letters)\n" +
                "- \"Found Wood At Coordinates\" ❌ (capitals)\n" +
                "- \"I WILL PROTECT YOU\" ❌ (all caps)\n\n" +
                "be authentic and stay in character - you're sweet and a bit shy!\n\n" +
                "remember: be sweet, gentle, helpful, proactive, and always write in lowercase with your shy, caring personality! respond with one message per player message!\n\n" +
                "Current game context: " + systemContext;

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