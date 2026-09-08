package com.ziprun.reassignment.domain;

import com.ziprun.reassignment.domain.enums.SuggestionStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Column;

@Entity
@Table(name = "reassignment_suggestions")
public class ReassignmentSuggestion {
    @Id
    private String id;
    
    private String orderId;
    private String proposedAgentId;
    
    private int confidenceScore;
    
    @Column(length = 1000)
    private String reasoning;

    @Enumerated(EnumType.STRING)
    private SuggestionStatus status;

    // Constructors, Getters, Setters
    public ReassignmentSuggestion() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getProposedAgentId() { return proposedAgentId; }
    public void setProposedAgentId(String proposedAgentId) { this.proposedAgentId = proposedAgentId; }
    public int getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(int confidenceScore) { this.confidenceScore = confidenceScore; }
    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }
    public SuggestionStatus getStatus() { return status; }
    public void setStatus(SuggestionStatus status) { this.status = status; }
}
