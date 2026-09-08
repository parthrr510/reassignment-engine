package com.ziprun.reassignment.repository;

import com.ziprun.reassignment.domain.ReassignmentSuggestion;
import com.ziprun.reassignment.domain.enums.SuggestionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SuggestionRepository extends JpaRepository<ReassignmentSuggestion, String> {
    List<ReassignmentSuggestion> findByStatus(SuggestionStatus status);
}
