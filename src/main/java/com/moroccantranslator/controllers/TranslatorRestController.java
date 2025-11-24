package com.moroccantranslator.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.moroccantranslator.services.TranslationService;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller exposing the translation API endpoints.
 */
@RestController
@RequestMapping("/api/translate")
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
    @PostMapping
    public Map<String, String> translate(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        String translation = translationService.translate(text);
        
        Map<String, String> response = new HashMap<>();
        response.put("translation", translation);
        return response;
    }
}
