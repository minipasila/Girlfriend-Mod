package com.beckytidus.girlfriendmod.ai;

import com.beckytidus.girlfriendmod.config.ModConfig;
import com.beckytidus.girlfriendmod.ai.ResponseCleaner;
// ... imports ...
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class OpenRouterClient {
    // ... fields ...
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
    private static final Gson gson = new Gson();
    private static final Logger LOGGER = LoggerFactory.getLogger("girlfriend-mod-openrouter");

    public static String loadSystemPrompt(String name, String systemContext, String playerName) {
        return SystemPromptManager.loadSystemPrompt(name, systemContext, playerName);
    }

    public static String loadSystemPrompt(String name, String systemContext) {
        return SystemPromptManager.loadSystemPrompt(name, systemContext);
    }

    public static CompletableFuture<String> generateResponse(List<ChatMessage> history, String systemContext, String playerName) {
        ModConfig config = ModConfig.get();
        String name = config.customName.isEmpty() ? "girlfriend" : config.customName.toLowerCase();
        String prompt = loadSystemPrompt(name, systemContext, playerName);
        
        // CLEAN for chat
        return generateRaw(history, prompt)
                .thenApply(ResponseCleaner::cleanResponse);
    }

    public static CompletableFuture<String> generateResponse(List<ChatMessage> history, String systemContext) {
        return generateResponse(history, systemContext, "Player");
    }

    public static CompletableFuture<String> generateRaw(List<ChatMessage> history, String systemPrompt) {
        ModConfig config = ModConfig.get();
        String apiKey = config.getActiveApiKey();

        if (apiKey == null || apiKey.isEmpty()) {
            return CompletableFuture.completedFuture("please set your api key in config... ^^");
        }

        // ... request construction ...
        JsonObject body = new JsonObject();
        body.addProperty("model", config.getActiveModelName());
        body.addProperty("stream", false);
        body.addProperty("max_tokens", 1024);
        body.addProperty("temperature", config.temperature);
        body.addProperty("min_p", config.minP);

        JsonArray messages = new JsonArray();
        JsonObject system = new JsonObject();
        system.addProperty("role", "system");
        system.addProperty("content", systemPrompt);
        messages.add(system);

        for (ChatMessage msg : history) {
            JsonObject m = new JsonObject();
            m.addProperty("role", msg.role);
            m.addProperty("content", msg.content);
            messages.add(m);
        }

        body.add("messages", messages);

        String requestJson = gson.toJson(body);
        LOGGER.info("[AI Debug] OpenRouter Request: {}", requestJson);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("HTTP-Referer", "https://github.com/minipasila/Girlfriend-Mod")
                .header("X-Title", "Girlfriend Mod")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    String responseBody = response.body();
                    LOGGER.info("[AI Debug] OpenRouter Response ({}): {}", response.statusCode(), responseBody);

                    if (response.statusCode() != 200) {
                        return "error: " + response.statusCode() + "... sorry >.<";
                    }
                    try {
                        JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                        JsonObject choice = json.getAsJsonArray("choices").get(0).getAsJsonObject();
                        JsonObject message = choice.get("message").getAsJsonObject();

                        String content = "";
                        if (message.has("content") && !message.get("content").isJsonNull()) {
                            content = message.get("content").getAsString();
                        }

                        if (content == null || content.trim().isEmpty()) {
                            return "...";
                        }

                        // DO NOT CLEAN HERE. Return raw.
                        return content;
                    } catch (Exception e) {
                        LOGGER.error("Error parsing OpenRouter response", e);
                        return "error parsing response... " + e.getMessage();
                    }
                });
    }

    public static CompletableFuture<String> summarize(List<ChatMessage> history) {
        List<ChatMessage> summaryPrompt = new ArrayList<>(history);
        summaryPrompt.add(new ChatMessage("user", "Summarize our conversation and your memories of me so far in detail while keeping it concise."));
        // Summaries are text, so we clean them
        return generateRaw(summaryPrompt, "You are a helpful assistant summarizer.")
                .thenApply(ResponseCleaner::cleanResponse);
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