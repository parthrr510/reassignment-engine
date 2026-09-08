package com.ziprun.reassignment;

import com.ziprun.reassignment.domain.Agent;
import com.ziprun.reassignment.domain.Order;
import com.ziprun.reassignment.domain.ReassignmentSuggestion;
import com.ziprun.reassignment.domain.enums.AgentStatus;
import com.ziprun.reassignment.domain.enums.OrderStatus;
import com.ziprun.reassignment.domain.enums.SuggestionStatus;
import com.ziprun.reassignment.repository.AgentRepository;
import com.ziprun.reassignment.repository.OrderRepository;
import com.ziprun.reassignment.repository.SuggestionRepository;
import com.ziprun.reassignment.service.AgentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AutomaticReassignmentTest {

    @Autowired
    private AgentService agentService;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private SuggestionRepository suggestionRepository;

    @BeforeEach
    void setUp() {
        suggestionRepository.deleteAll();
        orderRepository.deleteAll();
        agentRepository.deleteAll();
    }

    @Test
    void testAutomaticReassignmentFlow() {
        // 1. Setup: Pre-populate an assigned order and an online agent, and an available replacement
        Agent offlineAgent = new Agent();
        offlineAgent.setId("A-OFFLINE");
        offlineAgent.setName("Going Offline Agent");
        offlineAgent.setStatus(AgentStatus.BUSY);
        offlineAgent.setActiveOrderCount(1);
        agentRepository.save(offlineAgent);

        Agent availableAgent = new Agent();
        availableAgent.setId("A-REPLACEMENT");
        availableAgent.setName("Replacement Agent");
        availableAgent.setStatus(AgentStatus.AVAILABLE);
        availableAgent.setActiveOrderCount(0);
        agentRepository.save(availableAgent);

        Order order = new Order();
        order.setId("O-123");
        order.setDescription("Needs reassignment");
        order.setStatus(OrderStatus.ASSIGNED);
        order.setAssignedAgentId(offlineAgent.getId());
        order.setCreatedAt(LocalDateTime.now());
        orderRepository.save(order);

        // 2. Trigger markAgentOffline
        agentService.markAgentOffline(offlineAgent.getId(), "Bike broke down");

        // 3. Assert offline state was persisted
        Agent updatedAgent = agentRepository.findById(offlineAgent.getId()).orElseThrow();
        assertEquals(AgentStatus.OFFLINE, updatedAgent.getStatus());

        // 4. Assert a ReassignmentSuggestion was automatically created with PENDING status
        List<ReassignmentSuggestion> suggestions = suggestionRepository.findAll();
        assertEquals(1, suggestions.size(), "Should have created exactly one suggestion");
        
        ReassignmentSuggestion suggestion = suggestions.get(0);
        assertEquals(SuggestionStatus.PENDING, suggestion.getStatus());
        assertEquals("O-123", suggestion.getOrderId());
        assertEquals("A-REPLACEMENT", suggestion.getProposedAgentId());
    }

    @Test
    void testDuplicateOfflineEventSilentlyIgnored() {
        Agent offlineAgent = new Agent();
        offlineAgent.setId("A-ALREADY-OFFLINE");
        offlineAgent.setName("Already Offline");
        offlineAgent.setStatus(AgentStatus.OFFLINE);
        offlineAgent.setActiveOrderCount(0);
        agentRepository.save(offlineAgent);

        // Should return early without throwing an exception
        assertDoesNotThrow(() -> agentService.markAgentOffline(offlineAgent.getId(), "Reason"));
        
        // Ensure no suggestions were accidentally created
        assertTrue(suggestionRepository.findAll().isEmpty());
    }
}
