package com.beckytidus.girlfriendmod.ai;

import com.beckytidus.girlfriendmod.config.ModConfig;
import com.beckytidus.girlfriendmod.ai.ResponseCleaner;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class KoboldCppClient {
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
    private static final Gson gson = new Gson();
    private static final Logger LOGGER = LoggerFactory.getLogger("girlfriend-mod-koboldcpp");

    /**
     * Generates a response using the KoboldCpp API.
     * Context is handled via generateRawWithContext() which appends current status at the end.
     */
    public static CompletableFuture<String> generateResponse(List<ChatMessage> history, String systemContext, String playerName) {
        ModConfig config = ModConfig.get();
        String name = config.customName.isEmpty() ? "girlfriend" : config.customName.toLowerCase();
        String prompt = SystemPromptManager.loadSystemPrompt(name, playerName);
        
        // CLEAN for chat
        return generateRawWithContext(history, prompt, systemContext)
                .thenApply(ResponseCleaner::cleanResponse);
    }

    public static CompletableFuture<String> generateResponse(List<ChatMessage> history, String systemContext) {
        return generateResponse(history, systemContext, "Player");
    }

    public static CompletableFuture<String> generateRaw(List<ChatMessage> history, String systemPrompt) {
        return generateRawWithContext(history, systemPrompt, null);
    }

    /**
     * Generates a response with token usage information.
     * Use this method when accurate token counting is needed.
     */
    public static CompletableFuture<AIResponse> generateRawWithContextWithTokens(List<ChatMessage> history, String systemPrompt, String currentStatus) {
        ModConfig config = ModConfig.get();
        String baseUrl = config.koboldCppUrl;

        if (baseUrl.isEmpty()) {
            return CompletableFuture.completedFuture(new AIResponse("please configure koboldcpp url in config... ^^"));
        }

        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        if (config.koboldCppUseChatCompletions) {
            return generateViaChatCompletionsWithTokens(baseUrl, history, systemPrompt, currentStatus);
        } else {
            return generateViaKoboldAPIWithTokens(baseUrl, history, systemPrompt, currentStatus);
        }
    }

    /**
     * Generates a response with the current status context placed AFTER the chat history.
     * This ensures the AI sees the most up-to-date status information at the end of the context.
     */
    public static CompletableFuture<String> generateRawWithContext(List<ChatMessage> history, String systemPrompt, String currentStatus) {
        ModConfig config = ModConfig.get();
        String baseUrl = config.koboldCppUrl;

        if (baseUrl.isEmpty()) {
            return CompletableFuture.completedFuture("please configure koboldcpp url in config... ^^");
        }

        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        if (config.koboldCppUseChatCompletions) {
            return generateViaChatCompletions(baseUrl, history, systemPrompt, currentStatus);
        } else {
            return generateViaKoboldAPI(baseUrl, history, systemPrompt, currentStatus);
        }
    }

    private static CompletableFuture<String> generateViaChatCompletions(String baseUrl, List<ChatMessage> history, String systemPrompt, String currentStatus) {
        ModConfig config = ModConfig.get();

        JsonObject body = new JsonObject();
        body.addProperty("model", config.koboldCppModel);
        body.addProperty("stream", false);
        body.addProperty("max_tokens", 1024);
        body.addProperty("temperature", config.temperature);
        body.addProperty("min_p", config.minP);

        JsonArray messages = new JsonArray();
        
        // 1. System prompt FIRST (personality, instructions)
        JsonObject system = new JsonObject();
        system.addProperty("role", "system");
        system.addProperty("content", systemPrompt);
        messages.add(system);

        // 2. Chat history in the middle
        for (ChatMessage msg : history) {
            JsonObject m = new JsonObject();
            m.addProperty("role", msg.role);
            m.addProperty("content", msg.content);
            messages.add(m);
        }

        // 3. Current status LAST (most recent context for the AI to see)
        if (currentStatus != null && !currentStatus.isEmpty()) {
            JsonObject statusMessage = new JsonObject();
            statusMessage.addProperty("role", "system");
            statusMessage.addProperty("content", "CURRENT STATUS: " + currentStatus);
            messages.add(statusMessage);
        }

        body.add("messages", messages);

        String url = baseUrl + "/v1/chat/completions";
        String requestJson = gson.toJson(body);
        LOGGER.info("[AI Debug] KoboldCpp (Chat) Request: {}", requestJson);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    String responseBody = response.body();
                    LOGGER.info("[AI Debug] KoboldCpp Response ({}): {}", response.statusCode(), responseBody);

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

                        // DO NOT CLEAN HERE. Return raw.
                        return content;
                    } catch (Exception e) {
                        LOGGER.error("Error parsing KoboldCpp response", e);
                        return "error parsing response... " + e.getMessage();
                    }
                });
    }

    private static CompletableFuture<AIResponse> generateViaChatCompletionsWithTokens(String baseUrl, List<ChatMessage> history, String systemPrompt, String currentStatus) {
        ModConfig config = ModConfig.get();

        JsonObject body = new JsonObject();
        body.addProperty("model", config.koboldCppModel);
        body.addProperty("stream", false);
        body.addProperty("max_tokens", config.maxGenerationTokens);
        body.addProperty("temperature", config.temperature);
        body.addProperty("min_p", config.minP);

        JsonArray messages = new JsonArray();
        
        // 1. System prompt FIRST (personality, instructions)
        JsonObject system = new JsonObject();
        system.addProperty("role", "system");
        system.addProperty("content", systemPrompt);
        messages.add(system);

        // 2. Chat history in the middle
        for (ChatMessage msg : history) {
            JsonObject m = new JsonObject();
            m.addProperty("role", msg.role);
            m.addProperty("content", msg.content);
            messages.add(m);
        }

        // 3. Current status LAST (most recent context for the AI to see)
        if (currentStatus != null && !currentStatus.isEmpty()) {
            JsonObject statusMessage = new JsonObject();
            statusMessage.addProperty("role", "system");
            statusMessage.addProperty("content", "CURRENT STATUS: " + currentStatus);
            messages.add(statusMessage);
        }

        body.add("messages", messages);

        String url = baseUrl + "/v1/chat/completions";
        String requestJson = gson.toJson(body);
        LOGGER.info("[AI Debug] KoboldCpp (Chat) Request: {}", requestJson);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    String responseBody = response.body();
                    LOGGER.info("[AI Debug] KoboldCpp Response ({}): {}", response.statusCode(), responseBody);

                    if (response.statusCode() != 200) {
                        return new AIResponse("error: " + response.statusCode() + "... sorry >.<");
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
                            return new AIResponse("...");
                        }

                        // Extract token usage
                        AIResponse.TokenUsage tokenUsage = null;
                        if (json.has("usage") && !json.get("usage").isJsonNull()) {
                            tokenUsage = AIResponse.TokenUsage.fromJson(json.getAsJsonObject("usage"));
                            LOGGER.debug("[Token Debug] KoboldCpp - Prompt: {}, Completion: {}, Total: {}",
                                tokenUsage.promptTokens, tokenUsage.completionTokens, tokenUsage.totalTokens);
                        }

                        return new AIResponse(content, tokenUsage);
                    } catch (Exception e) {
                        LOGGER.error("Error parsing KoboldCpp response", e);
                        return new AIResponse("error parsing response... " + e.getMessage());
                    }
                });
    }

    private static CompletableFuture<String> generateViaKoboldAPI(String baseUrl, List<ChatMessage> history, String systemPrompt, String currentStatus) {
        ModConfig config = ModConfig.get();
        StringBuilder promptBuilder = new StringBuilder();
        String name = config.customName.isEmpty() ? "girlfriend" : config.customName.toLowerCase();

        // 1. System prompt FIRST (personality, instructions)
        promptBuilder.append(systemPrompt).append("\n\n");
        
        // 2. Chat history in the middle
        for (ChatMessage msg : history) {
            String role = msg.role.equals("user") ? "User" :
                         msg.role.equals("assistant") ? name :
                         msg.role.toUpperCase();
            promptBuilder.append("<|").append(role).append("|>\n");
            promptBuilder.append(msg.content).append("\n");
        }
        
        // 3. Current status LAST (most recent context for the AI to see)
        if (currentStatus != null && !currentStatus.isEmpty()) {
            promptBuilder.append("<|SYSTEM|>\n");
            promptBuilder.append("CURRENT STATUS: ").append(currentStatus).append("\n");
        }
        
        promptBuilder.append("<|").append(name.toUpperCase()).append("|>\n");
        String prompt = promptBuilder.toString();

        JsonObject body = new JsonObject();
        body.addProperty("prompt", prompt);
        body.addProperty("max_length", 200);
        body.addProperty("max_context_length", config.getActiveContextWindow());
        body.addProperty("temperature", config.temperature);
        body.addProperty("min_p", config.minP);
        
        JsonArray stopSequences = new JsonArray();
        stopSequences.add("<|USER|>");
        stopSequences.add("<|SYSTEM|>");
        stopSequences.add("<|");
        body.add("stop_sequence", stopSequences);

        String url = baseUrl + "/api/v1/generate";
        String requestJson = gson.toJson(body);
        LOGGER.info("[AI Debug] KoboldCpp (Native) Request: {}", requestJson);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    String responseBody = response.body();
                    LOGGER.info("[AI Debug] KoboldCpp Response ({}): {}", response.statusCode(), responseBody);

                    if (response.statusCode() != 200) {
                        return "error: " + response.statusCode() + "... sorry >.<";
                    }
                    try {
                        JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                        String text = json.getAsJsonArray("results")
                                .get(0).getAsJsonObject()
                                .get("text").getAsString();

                        text = cleanKoboldResponse(text);

                        if (text == null || text.trim().isEmpty()) {
                            return "...";
                        }

                        // DO NOT CLEAN HERE. Return raw (but with Kobold specific artifacts removed by cleanKoboldResponse is fine)
                        return text;
                    } catch (Exception e) {
                        LOGGER.error("Error parsing KoboldCpp response", e);
                        return "error parsing response... " + e.getMessage();
                    }
                });
    }

    private static String cleanKoboldResponse(String text) {
        String[] stopSequences = {"<|USER|>", "<|SYSTEM|>", "<|", "\n<|"};
        for (String stop : stopSequences) {
            int idx = text.indexOf(stop);
            if (idx != -1) {
                text = text.substring(0, idx);
            }
        }
        return text.trim();
    }

    /**
     * Generates a response using the native KoboldCpp API with token counting.
     * Uses /api/extra/tokencount endpoint for accurate token counting.
     */
    private static CompletableFuture<AIResponse> generateViaKoboldAPIWithTokens(String baseUrl, List<ChatMessage> history, String systemPrompt, String currentStatus) {
        ModConfig config = ModConfig.get();
        StringBuilder promptBuilder = new StringBuilder();
        String name = config.customName.isEmpty() ? "girlfriend" : config.customName.toLowerCase();

        // 1. System prompt FIRST (personality, instructions)
        promptBuilder.append(systemPrompt).append("\n\n");
        
        // 2. Chat history in the middle
        for (ChatMessage msg : history) {
            String role = msg.role.equals("user") ? "User" :
                         msg.role.equals("assistant") ? name :
                         msg.role.toUpperCase();
            promptBuilder.append("<|").append(role).append("|>\n");
            promptBuilder.append(msg.content).append("\n");
        }
        
        // 3. Current status LAST (most recent context for the AI to see)
        if (currentStatus != null && !currentStatus.isEmpty()) {
            promptBuilder.append("<|SYSTEM|>\n");
            promptBuilder.append("CURRENT STATUS: ").append(currentStatus).append("\n");
        }
        
        promptBuilder.append("<|").append(name.toUpperCase()).append("|>\n");
        String prompt = promptBuilder.toString();

        JsonObject body = new JsonObject();
        body.addProperty("prompt", prompt);
        body.addProperty("max_length", 200);
        body.addProperty("max_context_length", config.getActiveContextWindow());
        body.addProperty("temperature", config.temperature);
        body.addProperty("min_p", config.minP);
        
        JsonArray stopSequences = new JsonArray();
        stopSequences.add("<|USER|>");
        stopSequences.add("<|SYSTEM|>");
        stopSequences.add("<|");
        body.add("stop_sequence", stopSequences);

        String url = baseUrl + "/api/v1/generate";
        String requestJson = gson.toJson(body);
        LOGGER.info("[AI Debug] KoboldCpp (Native) Request: {}", requestJson);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        // First, get token count for the prompt
        return countTokens(baseUrl, prompt).thenCompose(tokenCount -> {
            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        String responseBody = response.body();
                        LOGGER.info("[AI Debug] KoboldCpp Response ({}): {}", response.statusCode(), responseBody);

                        if (response.statusCode() != 200) {
                            return new AIResponse("error: " + response.statusCode() + "... sorry >.<");
                        }
                        try {
                            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                            String text = json.getAsJsonArray("results")
                                    .get(0).getAsJsonObject()
                                    .get("text").getAsString();

                            text = cleanKoboldResponse(text);

                            if (text == null || text.trim().isEmpty()) {
                                return new AIResponse("...");
                            }

                            // Create token usage from our count
                            // For native API, we only have prompt tokens from tokencount endpoint
                            // Completion tokens would need another call, so we estimate
                            int completionTokens = text.length() / 4;  // Rough estimate
                            AIResponse.TokenUsage tokenUsage = new AIResponse.TokenUsage(
                                tokenCount,
                                completionTokens,
                                tokenCount + completionTokens
                            );
                            LOGGER.debug("[Token Debug] KoboldCpp (Native) - Prompt: {}, Completion (est): {}",
                                tokenCount, completionTokens);

                            return new AIResponse(text, tokenUsage);
                        } catch (Exception e) {
                            LOGGER.error("Error parsing KoboldCpp response", e);
                            return new AIResponse("error parsing response... " + e.getMessage());
                        }
                    });
        });
    }

    /**
     * Counts tokens using the KoboldCpp /api/extra/tokencount endpoint.
     *
     * @param baseUrl The KoboldCpp server base URL
     * @param text The text to count tokens for
     * @return A CompletableFuture containing the token count
     */
    public static CompletableFuture<Integer> countTokens(String baseUrl, String text) {
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        JsonObject body = new JsonObject();
        body.addProperty("prompt", text);

        String requestJson = gson.toJson(body);
        String url = baseUrl + "/api/extra/tokencount";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                            return json.get("value").getAsInt();
                        } catch (Exception e) {
                            LOGGER.warn("Error parsing token count response, using estimate", e);
                            return text.length() / 4;  // Fallback estimate
                        }
                    }
                    return text.length() / 4;  // Fallback estimate
                })
                .exceptionally(e -> {
                    LOGGER.warn("Error counting tokens, using estimate", e);
                    return text.length() / 4;  // Fallback estimate
                });
    }

    public static CompletableFuture<String> summarize(List<ChatMessage> history) {
        List<ChatMessage> summaryPrompt = new ArrayList<>(history);
        summaryPrompt.add(new ChatMessage("user", "Summarize our conversation and your memories of me so far in detail while keeping it concise."));
        // Summaries are text, so we clean them
        return generateRaw(summaryPrompt, "You are a helpful assistant summarizer.")
                .thenApply(ResponseCleaner::cleanResponse);
    }
    
    public static CompletableFuture<Boolean> checkServerAvailable(String url) {
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "/api/extra/version"))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> response.statusCode() == 200)
                .exceptionally(e -> false);
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
