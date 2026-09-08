package com.ziprun.reassignment.domain.vo;

import com.ziprun.reassignment.domain.Agent;
import com.ziprun.reassignment.domain.Order;
import java.util.List;

public class RoutingRequest {
    private Order affectedOrder;
    private List<Agent> availableAgents;
    private SituationContext context;

    public RoutingRequest(Order affectedOrder, List<Agent> availableAgents, SituationContext context) {
        if (availableAgents == null || availableAgents.isEmpty()) {
            throw new IllegalArgumentException("Available agents must not be empty");
        }
        this.affectedOrder = affectedOrder;
        this.availableAgents = availableAgents;
        this.context = context;
    }

    public Order getAffectedOrder() { return affectedOrder; }
    public List<Agent> getAvailableAgents() { return availableAgents; }
    public SituationContext getContext() { return context; }
}
