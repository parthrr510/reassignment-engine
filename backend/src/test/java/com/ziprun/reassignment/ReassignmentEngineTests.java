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
import com.ziprun.reassignment.service.ReassignmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ReassignmentEngineTests {

    @Autowired
    private AgentService agentService;
    
    @Autowired
    private ReassignmentService reassignmentService;
    
    @Autowired
    private AgentRepository agentRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private SuggestionRepository suggestionRepository;

    @BeforeEach
    void setup() {
        // Data is seeded via data.sql
        // Reset suggestion repository before each test to avoid cross-pollution
        suggestionRepository.deleteAll();
        
        // Ensure Agent 1 is BUSY
        Agent a1 = agentRepository.findById("AGT-001").get();
        a1.setStatus(AgentStatus.BUSY);
        agentRepository.save(a1);
    }

    @Test
    void testHappyPathReassignment() {
        // 1. Agent has assigned orders.
        List<Order> originalOrders = orderRepository.findByAssignedAgentIdAndStatus("AGT-001", OrderStatus.ASSIGNED);
        assertThat(originalOrders).hasSize(2);

        // 2 & 3 & 4 & 5 & 6. Agent becomes OFFLINE -> loop triggers -> suggestion persisted.
        agentService.markAgentOffline("AGT-001", "Bike broken");

        // 7. Suggestion can be retrieved.
        List<ReassignmentSuggestion> pending = suggestionRepository.findByStatus(SuggestionStatus.PENDING);
        assertThat(pending).hasSize(2); // Two orders reassigned
        
        ReassignmentSuggestion firstSuggestion = pending.get(0);
        
        // Verify rule-based logic (should pick AGT-002 or AGT-004 as they have 0 active orders)
        assertThat(firstSuggestion.getProposedAgentId()).isIn("AGT-002", "AGT-004");
        
        String newAgentId = firstSuggestion.getProposedAgentId();
        Agent newAgent = agentRepository.findById(newAgentId).get();
        int initialActiveCount = newAgent.getActiveOrderCount();

        // 8 & 9. Ops approves it.
        reassignmentService.approveSuggestion(firstSuggestion.getId());

        // 10. Persisted state is correct.
        ReassignmentSuggestion approved = suggestionRepository.findById(firstSuggestion.getId()).get();
        assertThat(approved.getStatus()).isEqualTo(SuggestionStatus.APPROVED);

        Order updatedOrder = orderRepository.findById(firstSuggestion.getOrderId()).get();
        assertThat(updatedOrder.getAssignedAgentId()).isEqualTo(newAgentId);

        Agent updatedNewAgent = agentRepository.findById(newAgentId).get();
        assertThat(updatedNewAgent.getActiveOrderCount()).isEqualTo(initialActiveCount + 1);
        
        Agent updatedOldAgent = agentRepository.findById("AGT-001").get();
        // Since we only approved one out of two, count drops by 1
        assertThat(updatedOldAgent.getActiveOrderCount()).isEqualTo(1);
    }

    @Test
    void testDuplicateOfflineEvent() {
        agentService.markAgentOffline("AGT-001", "Bike broken");
        
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            agentService.markAgentOffline("AGT-001", "Still broken");
        });
        
        assertThat(e.getMessage()).isEqualTo("Agent is already offline");
    }

    @Test
    void testInvalidStateTransitionOnApproval() {
        agentService.markAgentOffline("AGT-001", "Bike broken");
        List<ReassignmentSuggestion> pending = suggestionRepository.findByStatus(SuggestionStatus.PENDING);
        
        ReassignmentSuggestion suggestion = pending.get(0);
        reassignmentService.approveSuggestion(suggestion.getId());
        
        // Try approving again
        Exception e = assertThrows(IllegalStateException.class, () -> {
            reassignmentService.approveSuggestion(suggestion.getId());
        });
        
        assertThat(e.getMessage()).isEqualTo("Suggestion is not PENDING");
    }

    @Test
    void testRejection() {
        agentService.markAgentOffline("AGT-001", "Bike broken");
        List<ReassignmentSuggestion> pending = suggestionRepository.findByStatus(SuggestionStatus.PENDING);
        
        ReassignmentSuggestion suggestion = pending.get(0);
        reassignmentService.rejectSuggestion(suggestion.getId());
        
        ReassignmentSuggestion rejected = suggestionRepository.findById(suggestion.getId()).get();
        assertThat(rejected.getStatus()).isEqualTo(SuggestionStatus.REJECTED);
        
        // Order remains with old agent
        Order order = orderRepository.findById(suggestion.getOrderId()).get();
        assertThat(order.getAssignedAgentId()).isEqualTo("AGT-001");
    }
}
