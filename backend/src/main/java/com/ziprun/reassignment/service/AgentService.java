package com.ziprun.reassignment.service;

import com.ziprun.reassignment.domain.Agent;
import com.ziprun.reassignment.domain.enums.AgentStatus;
import com.ziprun.reassignment.repository.AgentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AgentService {
    private final AgentRepository agentRepository;
    private final ReassignmentService reassignmentService;

    public AgentService(AgentRepository agentRepository, ReassignmentService reassignmentService) {
        this.agentRepository = agentRepository;
        this.reassignmentService = reassignmentService;
    }

    @Transactional
    public Agent markAgentOffline(String agentId, String reason) {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new IllegalArgumentException("Agent not found"));

        if (agent.getStatus() == AgentStatus.OFFLINE) {
            return agent;
        }

        agent.setStatus(AgentStatus.OFFLINE);
        Agent savedAgent = agentRepository.save(agent);
        
        // Trigger the loop synchronously for MVP
        reassignmentService.triggerReassignment(agentId, reason);

        return savedAgent;
    }

    @Transactional(readOnly = true)
    public List<Agent> getAvailableAgents() {
        return agentRepository.findByStatus(AgentStatus.AVAILABLE);
    }

    @Transactional(readOnly = true)
    public List<Agent> getAllAgents() {
        return agentRepository.findAll();
    }
}
