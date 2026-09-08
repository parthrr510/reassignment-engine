package com.ziprun.reassignment.controller;

import com.ziprun.reassignment.domain.ReassignmentSuggestion;
import com.ziprun.reassignment.domain.enums.SuggestionStatus;
import com.ziprun.reassignment.repository.SuggestionRepository;
import com.ziprun.reassignment.service.ReassignmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/suggestions")
@CrossOrigin(origins = "*")
public class SuggestionController {

    private final SuggestionRepository suggestionRepository;
    private final ReassignmentService reassignmentService;

    public SuggestionController(SuggestionRepository suggestionRepository, ReassignmentService reassignmentService) {
        this.suggestionRepository = suggestionRepository;
        this.reassignmentService = reassignmentService;
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ReassignmentSuggestion>> getPendingSuggestions() {
        return ResponseEntity.ok(suggestionRepository.findByStatus(SuggestionStatus.PENDING));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveSuggestion(@PathVariable String id) {
        reassignmentService.approveSuggestion(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectSuggestion(@PathVariable String id) {
        reassignmentService.rejectSuggestion(id);
        return ResponseEntity.noContent().build();
    }
}
