package hpnj.mediatrack.repository;

import hpnj.mediatrack.domain.enums.SuggestionStatus;
import hpnj.mediatrack.domain.suggestion.Suggestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SuggestionRepository extends JpaRepository<Suggestion, UUID> {
    List<Suggestion> findByStatus(SuggestionStatus status);
}
