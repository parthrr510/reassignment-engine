package com.ziprun.reassignment.service.routing;

import com.ziprun.reassignment.domain.vo.RoutingRecommendation;
import com.ziprun.reassignment.domain.vo.RoutingRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RoutingEngineManager {

    private final Map<String, RoutingStrategy> strategies;
    private volatile String activeStrategyKey = "RULE";

    @Autowired
    public RoutingEngineManager(Map<String, RoutingStrategy> strategies) {
        this.strategies = strategies;
    }

    public void setActiveStrategy(String key) {
        if (!strategies.containsKey(key)) {
            throw new IllegalArgumentException("Unknown routing strategy: " + key);
        }
        this.activeStrategyKey = key;
    }

    public String getActiveStrategyKey() {
        return activeStrategyKey;
    }

    public RoutingRecommendation getRecommendation(RoutingRequest request) {
        RoutingStrategy strategy = strategies.get(activeStrategyKey);
        try {
            return strategy.route(request);
        } catch (Exception e) {
            // Fallback to RULE if AI fails
            if (!"RULE".equals(activeStrategyKey)) {
                return strategies.get("RULE").route(request);
            }
            throw e;
        }
    }
}
