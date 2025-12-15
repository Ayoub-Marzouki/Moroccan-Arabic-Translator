package com.moroccantranslator.controllers;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.moroccantranslator.services.TranslationService;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;

/**
 * REST Controller exposing the translation API endpoints.
 */
@RestController
@RequestMapping("/api")
public class TranslatorRestController {
    private final TranslationService translationService;

    public TranslatorRestController(TranslationService translationService) {
        this.translationService = translationService;
    }

    /**
     * Handles POST requests to translate text.
     *
     * @param request A map containing the "text" key with the text to translate.
     *                @RequestBody tells Spring to deserialize the JSON in the HTTP request body
     *                into this Map. We use Map<String, String> here as a simple way to capture 
     *                JSON like {"text": "Hello"} without creating a dedicated DTO class.
     * @return A map containing the "translation" key with the result.
     */
    @PostMapping("/translate")
    public Map<String, String> translate(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        String translation = translationService.translate(text);
        
        Map<String, String> response = new HashMap<>();
        response.put("translation", translation);
        return response;
    }

    /**
     * Proxies a Text-to-Speech request to Google Translate TTS.
     * <p>
     * This endpoint accepts text via a POST request (JSON body), encodes it, and fetches the audio
     * from Google's unofficial TTS API. It masquerades as a browser (User-Agent) to avoid blocking.
     * The resulting MP3 audio bytes are returned to the client to be played.
     * </p>
     * 
     * @param request A map containing the "text" key with the Arabic text to be spoken.
     * @return A ResponseEntity containing the MP3 audio bytes (Content-Type: audio/mpeg) 
     *         or a 500 status code if the request fails.
     */
    @PostMapping("/tts")
    public ResponseEntity<byte[]> speak(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        try {
            // Log Hex to verify encoding (Windows console often fails to print Arabic)
            System.out.println("Received TTS Text (Hex): " + HexFormat.of().formatHex(text.getBytes(StandardCharsets.UTF_8)));

            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String urlStr = "https://translate.google.com/translate_tts?ie=UTF-8&client=gtx&tl=ar&q=" + encodedText;
            URI uri = URI.create(urlStr);
            
            RestTemplate restTemplate = new RestTemplate();
            
            HttpHeaders headers = new HttpHeaders();
            headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<byte[]> response = restTemplate.exchange(uri, HttpMethod.GET, entity, byte[].class);

            System.out.println("Google TTS Response Code: " + response.getStatusCode());
            System.out.println("Google TTS Content-Type: " + response.getHeaders().getContentType());
            System.out.println("Google TTS Body Size: " + (response.getBody() != null ? response.getBody().length : 0));

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "audio/mpeg")
                    .body(response.getBody());
        } catch (Exception e) {
            e.printStackTrace(); // Log the error to see what happened
            return ResponseEntity.internalServerError().build();
        }
    }
}