package com.ziprun.reassignment.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ziprun.reassignment.domain.Agent;
import com.ziprun.reassignment.domain.vo.RoutingRecommendation;
import com.ziprun.reassignment.domain.vo.RoutingRequest;
import com.ziprun.reassignment.exception.AIAdvisorException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AIAdvisorService {
    
    private final LLMGateway llmGateway;
    private final ObjectMapper objectMapper;

    public AIAdvisorService(LLMGateway llmGateway, ObjectMapper objectMapper) {
        this.llmGateway = llmGateway;
        this.objectMapper = objectMapper;
    }

    public RoutingRecommendation getReassignmentRecommendation(RoutingRequest request) {
        String prompt = buildPrompt(request);
        String rawResponse = llmGateway.callGemini(prompt);
        return parseAndValidateResponse(rawResponse, request);
    }

    private String buildPrompt(RoutingRequest request) {
        String orderInfo = String.format("Order ID: %s, Description: %s", 
                request.getAffectedOrder().getId(), request.getAffectedOrder().getDescription());
        
        String contextInfo = String.format("Agent %s went offline. Reason: %s", 
                request.getContext().getOfflineAgentId(), request.getContext().getTriggerReason());

        String agentsInfo = request.getAvailableAgents().stream()
                .map(a -> String.format("- ID: %s, Name: %s, Active Orders: %d", a.getId(), a.getName(), a.getActiveOrderCount()))
                .collect(Collectors.joining("\n"));

        return String.format("""
                You are an AI Dispatch Advisor for a delivery platform.
                A delivery agent has gone offline, and an order needs to be reassigned.
                
                Situation: %s
                Affected Order: %s
                
                Available Agents:
                %s
                
                Choose the best available agent to take this order. Consider the agent's current active order count.
                
                You must respond ONLY with a raw JSON object (no markdown, no backticks). The JSON must have exactly these fields:
                - "recommendedAgentId": (string) The ID of the chosen agent from the available list.
                - "confidence": (integer) A score from 0 to 100 representing your confidence in this choice.
                - "reasoning": (string) A plain-English explanation of why this agent was chosen.
                """, contextInfo, orderInfo, agentsInfo);
    }

    private RoutingRecommendation parseAndValidateResponse(String rawResponse, RoutingRequest request) {
        try {
            // Strip markdown formatting if the LLM ignores instructions
            String cleanJson = rawResponse.trim();
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.substring(7);
            } else if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson.substring(3);
            }
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
            }
            cleanJson = cleanJson.trim();

            JsonNode node = objectMapper.readTree(cleanJson);
            
            if (!node.hasNonNull("recommendedAgentId") || !node.hasNonNull("confidence") || !node.hasNonNull("reasoning")) {
                throw new AIAdvisorException("Missing required fields in LLM response");
            }

            String recommendedAgentId = node.get("recommendedAgentId").asText();
            int confidenceScore = node.get("confidence").asInt();
            String reasoning = node.get("reasoning").asText();

            if (confidenceScore < 0 || confidenceScore > 100) {
                throw new AIAdvisorException("Confidence score must be between 0 and 100");
            }

            List<String> validAgentIds = request.getAvailableAgents().stream()
                    .map(Agent::getId)
                    .toList();

            if (!validAgentIds.contains(recommendedAgentId)) {
                throw new AIAdvisorException("Recommended agent ID is not in the list of available agents");
            }

            return new RoutingRecommendation(recommendedAgentId, confidenceScore, reasoning);
        } catch (AIAdvisorException e) {
            throw e;
        } catch (Exception e) {
            throw new AIAdvisorException("Failed to parse LLM response: " + e.getMessage(), e);
        }
    }
}
