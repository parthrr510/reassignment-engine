package com.ziprun.reassignment.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ziprun.reassignment.domain.Agent;
import com.ziprun.reassignment.domain.Order;
import com.ziprun.reassignment.domain.enums.AgentStatus;
import com.ziprun.reassignment.domain.enums.OrderStatus;
import com.ziprun.reassignment.domain.vo.RoutingRecommendation;
import com.ziprun.reassignment.domain.vo.RoutingRequest;
import com.ziprun.reassignment.domain.vo.SituationContext;
import com.ziprun.reassignment.exception.AIAdvisorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class AIAdvisorServiceTest {

    private LLMGateway llmGateway;
    private ObjectMapper objectMapper;
    private AIAdvisorService aiAdvisorService;

    private RoutingRequest sampleRequest;

    @BeforeEach
    void setUp() {
        llmGateway = Mockito.mock(LLMGateway.class);
        objectMapper = new ObjectMapper();
        aiAdvisorService = new AIAdvisorService(llmGateway, objectMapper);

        Order order = new Order();
        order.setId("O1");
        order.setDescription("Test Order");
        order.setStatus(OrderStatus.ASSIGNED);

        Agent agent1 = new Agent();
        agent1.setId("A1");
        agent1.setName("Agent 1");
        agent1.setStatus(AgentStatus.AVAILABLE);

        Agent agent2 = new Agent();
        agent2.setId("A2");
        agent2.setName("Agent 2");
        agent2.setStatus(AgentStatus.AVAILABLE);

        SituationContext context = new SituationContext("OFFLINE_A", "Bike breakdown", LocalDateTime.now());

        sampleRequest = new RoutingRequest(order, List.of(agent1, agent2), context);
    }

    @Test
    void testValidResponse() {
        String validJson = "{\"recommendedAgentId\":\"A1\",\"confidence\":85,\"reasoning\":\"Agent 1 is the best\"}";
        when(llmGateway.callGemini(anyString())).thenReturn(validJson);

        RoutingRecommendation rec = aiAdvisorService.getReassignmentRecommendation(sampleRequest);

        assertEquals("A1", rec.getRecommendedAgentId());
        assertEquals(85, rec.getConfidenceScore());
        assertEquals("Agent 1 is the best", rec.getReasoning());
    }

    @Test
    void testMalformedResponse() {
        when(llmGateway.callGemini(anyString())).thenReturn("This is not JSON");

        assertThrows(AIAdvisorException.class, () -> 
            aiAdvisorService.getReassignmentRecommendation(sampleRequest)
        );
    }

    @Test
    void testUnknownAgent() {
        // A3 is not in the list of available agents
        String invalidAgentJson = "{\"recommendedAgentId\":\"A3\",\"confidence\":85,\"reasoning\":\"Test\"}";
        when(llmGateway.callGemini(anyString())).thenReturn(invalidAgentJson);

        assertThrows(AIAdvisorException.class, () -> 
            aiAdvisorService.getReassignmentRecommendation(sampleRequest)
        );
    }

    @Test
    void testInvalidConfidence() {
        String invalidConfidenceJson = "{\"recommendedAgentId\":\"A1\",\"confidence\":150,\"reasoning\":\"Test\"}";
        when(llmGateway.callGemini(anyString())).thenReturn(invalidConfidenceJson);

        assertThrows(AIAdvisorException.class, () -> 
            aiAdvisorService.getReassignmentRecommendation(sampleRequest)
        );
    }

    @Test
    void testMissingFields() {
        String missingFieldsJson = "{\"recommendedAgentId\":\"A1\"}";
        when(llmGateway.callGemini(anyString())).thenReturn(missingFieldsJson);

        assertThrows(AIAdvisorException.class, () -> 
            aiAdvisorService.getReassignmentRecommendation(sampleRequest)
        );
    }

    @Test
    void testGeminiFailure() {
        when(llmGateway.callGemini(anyString())).thenThrow(new AIAdvisorException("API Error"));

        assertThrows(AIAdvisorException.class, () -> 
            aiAdvisorService.getReassignmentRecommendation(sampleRequest)
        );
    }
}
