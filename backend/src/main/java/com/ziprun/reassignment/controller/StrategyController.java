package com.ziprun.reassignment.controller;

import com.ziprun.reassignment.service.routing.RoutingEngineManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/strategy")
@CrossOrigin(origins = "*")
public class StrategyController {

    private final RoutingEngineManager routingEngineManager;

    public StrategyController(RoutingEngineManager routingEngineManager) {
        this.routingEngineManager = routingEngineManager;
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActiveStrategy() {
        return ResponseEntity.ok(Map.of("activeStrategy", routingEngineManager.getActiveStrategyKey()));
    }

    @PutMapping("/active")
    public ResponseEntity<?> setActiveStrategy(@RequestBody Map<String, String> payload) {
        String strategy = payload.get("strategy");
        if (strategy == null || (!strategy.equals("RULE") && !strategy.equals("AI"))) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid strategy. Must be RULE or AI"));
        }
        routingEngineManager.setActiveStrategy(strategy);
        return ResponseEntity.ok(Map.of("activeStrategy", strategy));
    }
}
