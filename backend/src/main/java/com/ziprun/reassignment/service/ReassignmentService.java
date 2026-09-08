package com.ziprun.reassignment.service;

import com.ziprun.reassignment.domain.Agent;
import com.ziprun.reassignment.domain.Order;
import com.ziprun.reassignment.domain.ReassignmentSuggestion;
import com.ziprun.reassignment.domain.enums.SuggestionStatus;
import com.ziprun.reassignment.domain.vo.RoutingRecommendation;
import com.ziprun.reassignment.domain.vo.RoutingRequest;
import com.ziprun.reassignment.domain.vo.SituationContext;
import com.ziprun.reassignment.repository.AgentRepository;
import com.ziprun.reassignment.repository.OrderRepository;
import com.ziprun.reassignment.repository.SuggestionRepository;
import com.ziprun.reassignment.service.routing.RoutingEngineManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ReassignmentService {

    private final OrderRepository orderRepository;
    private final AgentRepository agentRepository;
    private final OrderService orderService;
    private final AgentService agentService;
    private final RoutingEngineManager routingEngineManager;
    private final SuggestionRepository suggestionRepository;

    public ReassignmentService(OrderRepository orderRepository, AgentRepository agentRepository,
                               OrderService orderService, @Lazy AgentService agentService,
                               RoutingEngineManager routingEngineManager, SuggestionRepository suggestionRepository) {
        this.orderRepository = orderRepository;
        this.agentRepository = agentRepository;
        this.orderService = orderService;
        this.agentService = agentService;
        this.routingEngineManager = routingEngineManager;
        this.suggestionRepository = suggestionRepository;
    }

    @Transactional
    public void triggerReassignment(String offlineAgentId, String reason) {
        List<Order> affectedOrders = orderService.getActiveOrdersForAgent(offlineAgentId);
        if (affectedOrders.isEmpty()) return;

        List<Agent> availableAgents = agentService.getAvailableAgents();
        if (availableAgents.isEmpty()) return;

        SituationContext context = new SituationContext(offlineAgentId, reason, LocalDateTime.now());

        for (Order order : affectedOrders) {
            RoutingRequest request = new RoutingRequest(order, availableAgents, context);
            try {
                RoutingRecommendation rec = routingEngineManager.getRecommendation(request);
                
                ReassignmentSuggestion suggestion = new ReassignmentSuggestion();
                suggestion.setId("SUG-" + UUID.randomUUID().toString().substring(0, 8));
                suggestion.setOrderId(order.getId());
                suggestion.setProposedAgentId(rec.getRecommendedAgentId());
                suggestion.setConfidenceScore(rec.getConfidenceScore());
                suggestion.setReasoning(rec.getReasoning());
                suggestion.setStatus(SuggestionStatus.PENDING);
                
                suggestionRepository.save(suggestion);
                
            } catch (Exception e) {
                // Skip this order if routing completely fails
            }
        }
    }

    @Transactional
    public void approveSuggestion(String suggestionId) {
        ReassignmentSuggestion suggestion = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> new IllegalArgumentException("Suggestion not found"));
                
        if (suggestion.getStatus() != SuggestionStatus.PENDING) {
            throw new IllegalStateException("Suggestion is not PENDING");
        }

        Order order = orderRepository.findById(suggestion.getOrderId())
                .orElseThrow(() -> new IllegalStateException("Order not found"));
                
        Agent oldAgent = agentRepository.findById(order.getAssignedAgentId())
                .orElseThrow(() -> new IllegalStateException("Old agent not found"));
                
        Agent newAgent = agentRepository.findById(suggestion.getProposedAgentId())
                .orElseThrow(() -> new IllegalStateException("Proposed agent not found"));

        suggestion.setStatus(SuggestionStatus.APPROVED);
        suggestionRepository.save(suggestion);

        order.setAssignedAgentId(newAgent.getId());
        orderRepository.save(order);

        oldAgent.setActiveOrderCount(Math.max(0, oldAgent.getActiveOrderCount() - 1));
        agentRepository.save(oldAgent);

        newAgent.setActiveOrderCount(newAgent.getActiveOrderCount() + 1);
        agentRepository.save(newAgent);
    }

    @Transactional
    public void rejectSuggestion(String suggestionId) {
        ReassignmentSuggestion suggestion = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> new IllegalArgumentException("Suggestion not found"));
                
        if (suggestion.getStatus() != SuggestionStatus.PENDING) {
            throw new IllegalStateException("Suggestion is not PENDING");
        }
        
        suggestion.setStatus(SuggestionStatus.REJECTED);
        suggestionRepository.save(suggestion);
    }
}
