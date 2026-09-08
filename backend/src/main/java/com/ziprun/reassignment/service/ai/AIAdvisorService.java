package com.ziprun.reassignment.service.ai;

import org.springframework.stereotype.Service;

@Service
public class AIAdvisorService {
    
    /**
     * Abstraction boundary for the AI advisor.
     * Takes a prompt representing the order, agents, and context, 
     * and returns the raw AI response.
     * Actual implementation deferred to later milestones.
     */
    public String getReassignmentRecommendation(String prompt) {
        // TODO: Implement Gemini API call via RestClient
        throw new UnsupportedOperationException("AI implementation pending");
    }
}
