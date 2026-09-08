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
    private volatile String activeStrategyKey = "RULE_BASED";

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
            org.slf4j.LoggerFactory.getLogger(RoutingEngineManager.class).warn("Strategy {} failed, falling back to RULE_BASED. Error: {}", activeStrategyKey, e.getMessage());
            // Fallback to RULE_BASED if AI fails
            if (!"RULE_BASED".equals(activeStrategyKey)) {
                return strategies.get("RULE_BASED").route(request);
            }
            throw e;
        }
    }
}
