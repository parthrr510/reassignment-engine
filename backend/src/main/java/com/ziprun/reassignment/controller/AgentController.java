package com.ziprun.reassignment.controller;

import com.ziprun.reassignment.service.AgentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/agents")
@CrossOrigin(origins = "*")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @GetMapping
    public ResponseEntity<?> getAgents() {
        // Return all agents (for simplicity we will use getAllAgents if it exists, else we need to check AgentService)
        return ResponseEntity.ok(agentService.getAllAgents());
    }

    @PutMapping("/{id}/offline")
    public ResponseEntity<?> markAgentOffline(@PathVariable String id, @RequestBody(required = false) Map<String, String> payload) {
        String reason = (payload != null && payload.containsKey("reason")) ? payload.get("reason") : "Unknown reason";
        agentService.markAgentOffline(id, reason);
        return ResponseEntity.ok(Map.of("message", "Agent marked offline and reassignment triggered"));
    }
}
