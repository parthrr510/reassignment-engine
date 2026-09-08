package com.ziprun.reassignment.service.routing;

import com.ziprun.reassignment.domain.vo.RoutingRecommendation;
import com.ziprun.reassignment.domain.vo.RoutingRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RoutingEngineManagerTest {

    private RoutingStrategy ruleStrategy;
    private RoutingStrategy aiStrategy;
    private RoutingEngineManager routingEngineManager;

    @BeforeEach
    void setUp() {
        ruleStrategy = Mockito.mock(RoutingStrategy.class);
        aiStrategy = Mockito.mock(RoutingStrategy.class);
        
        Map<String, RoutingStrategy> strategies = Map.of(
                "RULE_BASED", ruleStrategy,
                "AI", aiStrategy
        );
        
        routingEngineManager = new RoutingEngineManager(strategies);
    }

    @Test
    void testDefaultStrategyIsRuleBased() {
        assertEquals("RULE_BASED", routingEngineManager.getActiveStrategyKey());
    }

    @Test
    void testSwitchStrategyToAI() {
        routingEngineManager.setActiveStrategy("AI");
        assertEquals("AI", routingEngineManager.getActiveStrategyKey());
    }

    @Test
    void testSwitchStrategyBackToRuleBased() {
        routingEngineManager.setActiveStrategy("AI");
        routingEngineManager.setActiveStrategy("RULE_BASED");
        assertEquals("RULE_BASED", routingEngineManager.getActiveStrategyKey());
    }

    @Test
    void testInvalidStrategyRejection() {
        assertThrows(IllegalArgumentException.class, () -> routingEngineManager.setActiveStrategy("UNKNOWN"));
    }

    @Test
    void testRoutingEngineUsesChangedStrategy() {
        RoutingRequest request = Mockito.mock(RoutingRequest.class);
        
        RoutingRecommendation ruleRec = new RoutingRecommendation("A1", 100, "Rule");
        RoutingRecommendation aiRec = new RoutingRecommendation("A2", 95, "AI");
        
        when(ruleStrategy.route(any())).thenReturn(ruleRec);
        when(aiStrategy.route(any())).thenReturn(aiRec);

        // Default is RULE_BASED
        RoutingRecommendation rec1 = routingEngineManager.getRecommendation(request);
        assertEquals("A1", rec1.getRecommendedAgentId());
        verify(ruleStrategy, times(1)).route(any());
        verify(aiStrategy, never()).route(any());

        // Switch to AI
        routingEngineManager.setActiveStrategy("AI");
        RoutingRecommendation rec2 = routingEngineManager.getRecommendation(request);
        assertEquals("A2", rec2.getRecommendedAgentId());
        verify(ruleStrategy, times(1)).route(any()); // Still 1 from before
        verify(aiStrategy, times(1)).route(any());
    }

    @Test
    void testFallbackToRuleBasedIfAIFails() {
        RoutingRequest request = Mockito.mock(RoutingRequest.class);
        
        RoutingRecommendation ruleRec = new RoutingRecommendation("A1", 100, "Rule");
        
        when(aiStrategy.route(any())).thenThrow(new RuntimeException("AI Down"));
        when(ruleStrategy.route(any())).thenReturn(ruleRec);

        routingEngineManager.setActiveStrategy("AI");
        
        RoutingRecommendation rec = routingEngineManager.getRecommendation(request);
        
        // Should fallback to rule base recommendation
        assertEquals("A1", rec.getRecommendedAgentId());
        verify(aiStrategy, times(1)).route(any());
        verify(ruleStrategy, times(1)).route(any());
    }
}
