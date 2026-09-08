package com.ziprun.reassignment.service.routing;

import com.ziprun.reassignment.domain.Agent;
import com.ziprun.reassignment.domain.Order;
import com.ziprun.reassignment.domain.enums.AgentStatus;
import com.ziprun.reassignment.domain.enums.OrderStatus;
import com.ziprun.reassignment.domain.vo.RoutingRecommendation;
import com.ziprun.reassignment.domain.vo.RoutingRequest;
import com.ziprun.reassignment.domain.vo.SituationContext;
import com.ziprun.reassignment.exception.AIAdvisorException;
import com.ziprun.reassignment.service.ai.AIAdvisorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AIRoutingStrategyTest {

    private AIAdvisorService aiAdvisorService;
    private AIRoutingStrategy aiRoutingStrategy;
    private RoutingRequest sampleRequest;

    @BeforeEach
    void setUp() {
        aiAdvisorService = Mockito.mock(AIAdvisorService.class);
        aiRoutingStrategy = new AIRoutingStrategy(aiAdvisorService);

        Order order = new Order();
        order.setId("O1");
        order.setDescription("Test Order");
        order.setStatus(OrderStatus.ASSIGNED);

        Agent agent1 = new Agent();
        agent1.setId("A1");
        agent1.setName("Agent 1");
        agent1.setStatus(AgentStatus.AVAILABLE);

        SituationContext context = new SituationContext("OFFLINE_A", "Bike breakdown", LocalDateTime.now());

        sampleRequest = new RoutingRequest(order, List.of(agent1), context);
    }

    @Test
    void testSuccessfulAIRouting() {
        RoutingRecommendation mockRec = new RoutingRecommendation("A1", 90, "Good agent");
        when(aiAdvisorService.getReassignmentRecommendation(any(RoutingRequest.class))).thenReturn(mockRec);

        RoutingRecommendation result = aiRoutingStrategy.route(sampleRequest);
        
        assertNotNull(result);
        assertEquals("A1", result.getRecommendedAgentId());
        assertEquals(90, result.getConfidenceScore());
    }

    @Test
    void testAIFailure() {
        when(aiAdvisorService.getReassignmentRecommendation(any(RoutingRequest.class)))
                .thenThrow(new RuntimeException("API Down"));

        assertThrows(AIAdvisorException.class, () -> aiRoutingStrategy.route(sampleRequest));
    }

    @Test
    void testInvalidRecommendation_UnknownAgent() {
        // "A2" is not in the sampleRequest's available agents list
        RoutingRecommendation mockRec = new RoutingRecommendation("A2", 90, "Good agent");
        when(aiAdvisorService.getReassignmentRecommendation(any(RoutingRequest.class))).thenReturn(mockRec);

        assertThrows(AIAdvisorException.class, () -> aiRoutingStrategy.route(sampleRequest));
    }

    @Test
    void testEligibleReplacement() {
        RoutingRecommendation mockRec = new RoutingRecommendation("A1", 100, "Good agent");
        when(aiAdvisorService.getReassignmentRecommendation(any(RoutingRequest.class))).thenReturn(mockRec);

        RoutingRecommendation result = aiRoutingStrategy.route(sampleRequest);
        assertEquals("A1", result.getRecommendedAgentId());
    }
}
