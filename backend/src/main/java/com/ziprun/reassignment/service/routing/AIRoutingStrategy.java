package com.ziprun.reassignment.service.routing;

import com.ziprun.reassignment.domain.Agent;
import com.ziprun.reassignment.domain.vo.RoutingRecommendation;
import com.ziprun.reassignment.domain.vo.RoutingRequest;
import com.ziprun.reassignment.exception.AIAdvisorException;
import com.ziprun.reassignment.service.ai.AIAdvisorService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("AI")
public class AIRoutingStrategy implements RoutingStrategy {

    private final AIAdvisorService aiAdvisorService;

    public AIRoutingStrategy(AIAdvisorService aiAdvisorService) {
        this.aiAdvisorService = aiAdvisorService;
    }

    @Override
    public RoutingRecommendation route(RoutingRequest request) {
        try {
            RoutingRecommendation recommendation = aiAdvisorService.getReassignmentRecommendation(request);
            
            // Validate the recommendation to ensure it meets our domain constraints
            validateRecommendation(recommendation, request);
            
            return recommendation;
        } catch (Exception e) {
            // Handle AI failure safely by throwing a runtime exception that the RoutingEngineManager will catch
            throw new AIAdvisorException("AI Routing Strategy failed: " + e.getMessage(), e);
        }
    }

    private void validateRecommendation(RoutingRecommendation recommendation, RoutingRequest request) {
        if (recommendation == null) {
            throw new AIAdvisorException("AI Advisor returned a null recommendation");
        }

        List<String> validAgentIds = request.getAvailableAgents().stream()
                .map(Agent::getId)
                .toList();

        if (!validAgentIds.contains(recommendation.getRecommendedAgentId())) {
            throw new AIAdvisorException("AI Advisor recommended an ineligible or unknown agent: " + recommendation.getRecommendedAgentId());
        }

        if (recommendation.getConfidenceScore() < 0 || recommendation.getConfidenceScore() > 100) {
            throw new AIAdvisorException("AI Advisor returned an invalid confidence score: " + recommendation.getConfidenceScore());
        }
    }
}
