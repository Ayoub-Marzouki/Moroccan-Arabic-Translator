package com.moroccantranslator.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class TranslationService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Using Gemini 2.5 Flash
    private static final String GEMINI_API_URL_TEMPLATE = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=%s";

    public TranslationService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Translates the given English text to Moroccan Arabic (Darija).
     *
     * @param text The English text to translate.
     * @return The translated text in Darija.
     */
    public String translate(String text) {
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.contains("INSERT_YOUR_API_KEY")) {
            return "Configuration Error: Gemini API Key is not set. Please export GEMINI_API_KEY or update application.properties.";
        }

        String url = String.format(GEMINI_API_URL_TEMPLATE, apiKey);

        // Setup Headers as JSOn
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // The Request Body : we need to match Gemini's expected JSON structure:
        // { "contents": [ { "parts": [ { "text": "..." } ] } ] } (check readme for docs)
        // Map <=> JSON {}
        Map<String, Object> part = new HashMap<>();
        part.put("text", "Translate the following English text to Moroccan Arabic (Darija). Output ONLY the translation, no explanations: " + text);

        // List <=> JSON ()
        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(part));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(content));

        // HttpEntity represents HTTP REquest
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // Send the Request and Receive the Response
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            // Parse the Response to find the translation
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error during translation: " + e.getMessage();
        }
    }
}
