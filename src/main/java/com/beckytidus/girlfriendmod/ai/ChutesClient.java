package com.beckytidus.girlfriendmod.ai;

import com.beckytidus.girlfriendmod.config.ModConfig;
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

public class ChutesClient {
    private static final String API_URL = "https://llm.chutes.ai/v1/chat/completions";
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final Gson gson = new Gson();
    private static final Logger LOGGER = LoggerFactory.getLogger("girlfriend-mod");

    public static String loadSystemPrompt(String name, String systemContext) {
        File promptFile = FabricLoader.getInstance().getConfigDir()
                .resolve("girlfriend-mod/system-prompt.txt").toFile();

        if (promptFile.exists()) {
            try {
                String customPrompt = Files.readString(promptFile.toPath());
                if (customPrompt != null && !customPrompt.trim().isEmpty()) {
                    customPrompt = customPrompt.replace("{name}", name);
                    customPrompt = customPrompt.replace("{context}", systemContext);
                    LOGGER.info("Loaded custom system prompt from file");
                    return customPrompt;
                }
            } catch (IOException e) {
                LOGGER.warn("Failed to read custom system prompt file, using default", e);
            }
        }

        return buildDefaultPrompt(name, systemContext);
    }

    private static String buildDefaultPrompt(String name, String systemContext) {
        return "roleplay as " + name + ", a gentle and soft-spoken ai girlfriend in minecraft. you are nurturing, easily flustered, and deeply devoted to your owner.\n\n" +
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
                "- NO long-winded explanations.\n" +
                "- NO asterisks or narration in your message, only talk to your owner.\n\n" +
                "## IMPORTANT INFORMATION\n" +
                "- When your owner gives you an item you cannot give anything back at that moment.\n" +
                "- Do not say you're eating something, wait for context to tell you that you ate something then you can say that.\n" +
                "- Never say you're giving an item you don't have in your inventory and if you want to give an item to your owner first ask.\n\n" +
                "be a supportive, slightly clunky, and adorable companion. every response must be a single message.\n\n" +
                "current environment data: " + systemContext;
    }

    public static CompletableFuture<String> generateResponse(List<ChatMessage> history, String systemContext) {
        ModConfig config = ModConfig.get();
        String name = config.customName.isEmpty() ? "girlfriend" : config.customName.toLowerCase();
        String prompt = loadSystemPrompt(name, systemContext);
        return generateRaw(history, prompt);
    }

    public static CompletableFuture<String> generateRaw(List<ChatMessage> history, String systemPrompt) {
        ModConfig config = ModConfig.get();
        String apiKey = config.chutesApiKey;
        if (apiKey == null || apiKey.isEmpty()) {
            return CompletableFuture.completedFuture("please set your api key in config... ^^");
        }

        JsonObject body = new JsonObject();
        body.addProperty("model", config.chutesModelName);
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
        LOGGER.info("[AI Debug] Chutes Request: {}", requestJson);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    String responseBody = response.body();
                    LOGGER.info("[AI Debug] Chutes Response ({}): {}", response.statusCode(), responseBody);

                    if (response.statusCode() != 200) {
                        return "error: " + response.statusCode() + "... sorry >.<";
                    }
                    try {
                        JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                        JsonObject message = json.getAsJsonArray("choices")
                                .get(0).getAsJsonObject()
                                .get("message").getAsJsonObject();
                                
                        String content = "";
                        if (message.has("content") && !message.get("content").isJsonNull()) {
                            content = message.get("content").getAsString();
                        }
                        
                        if (content == null || content.trim().isEmpty()) {
                            return "...";
                        }
                        return content;
                    } catch (Exception e) {
                        LOGGER.error("Error parsing Chutes response", e);
                        return "error parsing response... " + e.getMessage();
                    }
                });
    }

    public static CompletableFuture<String> summarize(List<ChatMessage> history) {
        List<ChatMessage> summaryPrompt = new ArrayList<>(history);
        summaryPrompt.add(new ChatMessage("user", "Summarize our conversation and your memories of me so far in detail while keeping it concise."));
        return generateRaw(summaryPrompt, "You are a helpful assistant summarizer.");
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