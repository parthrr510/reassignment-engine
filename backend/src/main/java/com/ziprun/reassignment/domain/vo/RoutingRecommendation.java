package com.ziprun.reassignment.domain.vo;

public class RoutingRecommendation {
    private String recommendedAgentId;
    private int confidenceScore;
    private String reasoning;

    public RoutingRecommendation(String recommendedAgentId, int confidenceScore, String reasoning) {
        if (confidenceScore < 0 || confidenceScore > 100) {
            throw new IllegalArgumentException("Confidence score must be between 0 and 100");
        }
        this.recommendedAgentId = recommendedAgentId;
        this.confidenceScore = confidenceScore;
        this.reasoning = reasoning;
    }

    public String getRecommendedAgentId() { return recommendedAgentId; }
    public int getConfidenceScore() { return confidenceScore; }
    public String getReasoning() { return reasoning; }
}
