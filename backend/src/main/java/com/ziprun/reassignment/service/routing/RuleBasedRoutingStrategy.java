package com.ziprun.reassignment.service.routing;

import com.ziprun.reassignment.domain.Agent;
import com.ziprun.reassignment.domain.vo.RoutingRecommendation;
import com.ziprun.reassignment.domain.vo.RoutingRequest;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Optional;

@Component("RULE_BASED")
public class RuleBasedRoutingStrategy implements RoutingStrategy {

    @Override
    public RoutingRecommendation route(RoutingRequest request) {
        // Find the agent with the lowest active order count, resolving ties by ID for determinism.
        Optional<Agent> bestAgentOpt = request.getAvailableAgents().stream()
                .min(Comparator.comparingInt(Agent::getActiveOrderCount)
                        .thenComparing(Agent::getId));

        if (bestAgentOpt.isEmpty()) {
            throw new IllegalStateException("No eligible agents available for routing");
        }

        Agent bestAgent = bestAgentOpt.get();
        String reasoning = String.format("Rule-based selection: Agent %s has the lowest active order count (%d).",
                bestAgent.getName(), bestAgent.getActiveOrderCount());

        // Confidence score is arbitrary for rule-based, let's say 100 since it strictly follows the rule.
        return new RoutingRecommendation(bestAgent.getId(), 100, reasoning);
    }
}
