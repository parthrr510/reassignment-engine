package com.ziprun.reassignment.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ziprun.reassignment.exception.AIAdvisorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class LLMGateway {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String geminiUrl;
    private final String geminiApiKey;

    public LLMGateway(ObjectMapper objectMapper,
                      @Value("${llm.base-url}/v1beta/models/${llm.model}:generateContent") String geminiUrl,
                      @Value("${llm.api-key}") String geminiApiKey) {
        this.restClient = RestClient.create();
        this.objectMapper = objectMapper;
        this.geminiUrl = geminiUrl;
        this.geminiApiKey = geminiApiKey;
    }

    public String callGemini(String promptText) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", promptText)
                            ))
                    )
            );

            String urlWithKey = geminiUrl + "?key=" + geminiApiKey;

            String responseJson = restClient.post()
                    .uri(urlWithKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(requestBody))
                    .retrieve()
                    .body(String.class);

            JsonNode rootNode = objectMapper.readTree(responseJson);
            JsonNode candidates = rootNode.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode firstCandidate = candidates.get(0);
                JsonNode parts = firstCandidate.path("content").path("parts");
                if (parts.isArray() && parts.size() > 0) {
                    return parts.get(0).path("text").asText();
                }
            }
            throw new AIAdvisorException("Invalid response structure from Gemini API");
        } catch (Exception e) {
            throw new AIAdvisorException("Failed to call Gemini API: " + e.getMessage(), e);
        }
    }
}
