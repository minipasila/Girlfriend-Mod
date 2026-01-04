package com.beckytidus.girlfriendmod.ai;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Utility class for cleaning AI-generated responses to remove unwanted artifacts,
 * formatting, and tool call tags.
 */
public class ResponseCleaner {

    /**
     * Patterns to remove from AI responses
     */
    private static final Pattern[] CLEANUP_PATTERNS = {
        // Remove specific tags and their content
        Pattern.compile("\\[SYSTEM_PROMPT\\].*?\\[/SYSTEM_PROMPT\\]", Pattern.DOTALL),
        Pattern.compile("\\[AVAILABLE_TOOLS\\].*?\\[/AVAILABLE_TOOLS\\]", Pattern.DOTALL),
        Pattern.compile("\\[INST\\].*?\\[/INST\\]", Pattern.DOTALL),
        Pattern.compile("\\[ARGS\\].*?\\[/ARGS\\]", Pattern.DOTALL),
        Pattern.compile("\\[TOOL_CALLS\\].*?\\[/TOOL_CALLS\\]", Pattern.DOTALL),
        Pattern.compile("\\[TOOL_RESULTS\\].*?\\[/TOOL_RESULTS\\]", Pattern.DOTALL),
        Pattern.compile("\\[THINK\\].*?\\[/THINK\\]", Pattern.DOTALL),

        // Remove specific XML/HTML tags and their content
        Pattern.compile("<\\|start_header_id\\|>.*?<\\|end_header_id\\|>", Pattern.DOTALL),
        Pattern.compile("<\\|eot_id\\|>", Pattern.DOTALL),
        Pattern.compile("<\\|im_start\\|>", Pattern.DOTALL),
        Pattern.compile("<\\|im_end\\|>", Pattern.DOTALL),
        Pattern.compile("<\\|vision_start\\|>.*?<\\|vision_end\\|>", Pattern.DOTALL),
        Pattern.compile("<\\|image_pad\\|>", Pattern.DOTALL),
        Pattern.compile("<\\|video_pad\\|>", Pattern.DOTALL),
        Pattern.compile("<\\|python_tag\\|>", Pattern.DOTALL),
        Pattern.compile("<\\|eom_id\\|>", Pattern.DOTALL),

        // Remove tool call tags
        Pattern.compile("<tool_call>.*?</tool_call>", Pattern.DOTALL),
        Pattern.compile("<tool_response>.*?</tool_response>", Pattern.DOTALL),

        // Remove empty tags
        Pattern.compile("<tools></tools>", Pattern.DOTALL),

        // Remove  blocks
        Pattern.compile("<think>.*?</think>", Pattern.DOTALL),

        // Remove common AI prefixes/suffixes (keep these more specific)
        Pattern.compile("^\\s*(assistant|system|user|AI):\\s*", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
        Pattern.compile("^\\s*(response|output|answer):\\s*", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),

        // Remove excessive newlines (keep single newlines for natural flow)
        Pattern.compile("\\n{3,}", Pattern.DOTALL),

        // Remove excessive whitespace (but preserve single spaces)
        Pattern.compile("[ \\t\\x0B\\f]{2,}", Pattern.DOTALL),

        // Remove control characters (except newline)
        Pattern.compile("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", Pattern.DOTALL)
    };

    /**
     * Clean an AI response by removing unwanted artifacts and formatting.
     * Designed for Chat/Dialogue (removes trailing punctuation).
     *
     * @param response The raw AI response
     * @return Cleaned response
     */
    public static String cleanResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return "...";
        }

        String cleaned = response.trim();

        // Apply all cleanup patterns
        for (Pattern pattern : CLEANUP_PATTERNS) {
            cleaned = pattern.matcher(cleaned).replaceAll("");
        }

        // Trim again and ensure it's not empty
        cleaned = cleaned.trim();

        // If cleaning removed everything, return a default response
        if (cleaned.isEmpty()) {
            return "...";
        }

        // Remove any trailing punctuation that might have been left by cleanup
        // WARNING: This strips '}' from JSON, use cleanJsonResponse for JSON
        cleaned = cleaned.replaceAll("[\\s\\p{Punct}]+$", "");

        // Add ellipsis if the response ends abruptly (only if it fits the character's style)
        if (!cleaned.endsWith("...") && !cleaned.endsWith("~") && !cleaned.endsWith(":3") &&
            !cleaned.endsWith(">.<") && !cleaned.endsWith("^-^") && !cleaned.endsWith(".") &&
            !cleaned.endsWith("!") && !cleaned.endsWith("?")) {
            // Only add ellipsis if it seems like an incomplete thought
            if (cleaned.matches(".*[a-zA-Z0-9]$") && Math.random() < 0.3) {
                cleaned += "...";
            }
        }

        return cleaned;
    }

    /**
     * Clean an AI response intended to be JSON.
     * Removes artifacts but PRESERVES JSON structure (braces, quotes, etc).
     *
     * @param response The raw AI response
     * @return Cleaned response for JSON parsing
     */
    public static String cleanJsonResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return "{}";
        }

        String cleaned = response.trim();

        // Apply all cleanup patterns
        for (Pattern pattern : CLEANUP_PATTERNS) {
            cleaned = pattern.matcher(cleaned).replaceAll("");
        }

        // Just trim, DO NOT remove trailing punctuation
        return cleaned.trim();
    }

    /**
     * Check if response contains unwanted artifacts that need cleaning.
     */
    public static boolean needsCleaning(String response) {
        if (response == null) return false;

        // Check for common artifacts from various AI providers
        return response.contains("[SYSTEM_PROMPT]") ||
               response.contains("[AVAILABLE_TOOLS]") ||
               response.contains("[INST]") ||
               response.contains("[TOOL_CALLS]") ||
               response.contains("[THINK]") ||
               response.contains("<|start_header_id|>") ||
               response.contains("<|eot_id|>") ||
               response.contains("<|im_start|>") ||
               response.contains("<tool_call>") ||
               response.contains("<think>") ||
               response.matches("^\\s*(assistant|system|user|AI|response|output|answer):.*") ||
               response.matches(".*```.*```.*");
    }

    /**
     * Extract only the dialogue/content part from a response that might contain
     * metadata or other unwanted content.
     */
    public static String extractDialogue(String response) {
        if (response == null) return "...";

        String cleaned = cleanResponse(response);

        // If the response still looks like it contains metadata, try to extract just the dialogue
        if (cleaned.contains(": ") && cleaned.split(": ").length > 1) {
            String[] parts = cleaned.split(": ");
            // Take the last part after the last colon (most likely the actual dialogue)
            return parts[parts.length - 1].trim();
        }

        return cleaned;
    }
}