package hpnj.mediatrack.service;

import hpnj.mediatrack.domain.enums.SuggestionEntityType;
import hpnj.mediatrack.domain.enums.SuggestionStatus;
import hpnj.mediatrack.domain.suggestion.Suggestion;
import hpnj.mediatrack.domain.user.UserAccount;
import hpnj.mediatrack.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuggestionServiceTest {

    @Mock SuggestionRepository suggestionRepository;
    @Mock MediaRepository mediaRepository;
    @Mock PartyRepository partyRepository;
    @Mock MediaGroupRepository mediaGroupRepository;
    @Mock UserAccountRepository userAccountRepository;
    @InjectMocks SuggestionService suggestionService;

    @Test
    void submit_createsPendingSuggestion() {
        UserAccount user = new UserAccount();
        user.setId(UUID.randomUUID());
        when(userAccountRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(suggestionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> data = Map.of("title", "Inception", "releaseYear", 2010);
        suggestionService.submit(user.getId(), SuggestionEntityType.MOVIE, data);

        ArgumentCaptor<Suggestion> captor = ArgumentCaptor.forClass(Suggestion.class);
        verify(suggestionRepository).save(captor.capture());
        Suggestion saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(SuggestionStatus.PENDING);
        assertThat(saved.getEntityType()).isEqualTo(SuggestionEntityType.MOVIE);
        assertThat(saved.getProposedData()).isEqualTo(data);
    }

    @Test
    void approve_changesStatusAndMaterializesMovie() {
        UserAccount reviewer = new UserAccount();
        reviewer.setId(UUID.randomUUID());

        Suggestion suggestion = new Suggestion();
        suggestion.setId(UUID.randomUUID());
        suggestion.setStatus(SuggestionStatus.PENDING);
        suggestion.setEntityType(SuggestionEntityType.MOVIE);
        suggestion.setProposedData(Map.of("title", "Inception", "releaseYear", 2010));

        when(suggestionRepository.findById(suggestion.getId())).thenReturn(Optional.of(suggestion));
        when(userAccountRepository.findById(reviewer.getId())).thenReturn(Optional.of(reviewer));
        when(suggestionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        suggestionService.approve(suggestion.getId(), reviewer.getId());

        assertThat(suggestion.getStatus()).isEqualTo(SuggestionStatus.APPROVED);
        assertThat(suggestion.getReviewedBy()).isEqualTo(reviewer);
        verify(mediaRepository).save(any());
    }

    @Test
    void reject_changesStatusWithNote() {
        UserAccount reviewer = new UserAccount();
        reviewer.setId(UUID.randomUUID());

        Suggestion suggestion = new Suggestion();
        suggestion.setId(UUID.randomUUID());
        suggestion.setStatus(SuggestionStatus.PENDING);

        when(suggestionRepository.findById(suggestion.getId())).thenReturn(Optional.of(suggestion));
        when(userAccountRepository.findById(reviewer.getId())).thenReturn(Optional.of(reviewer));
        when(suggestionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        suggestionService.reject(suggestion.getId(), reviewer.getId(), "Duplicate entry");

        assertThat(suggestion.getStatus()).isEqualTo(SuggestionStatus.REJECTED);
        assertThat(suggestion.getReviewNote()).isEqualTo("Duplicate entry");
        assertThat(suggestion.getReviewedBy()).isEqualTo(reviewer);
        verify(mediaRepository, never()).save(any());
    }

    @Test
    void approve_throwsWhenNotPending() {
        Suggestion suggestion = new Suggestion();
        suggestion.setId(UUID.randomUUID());
        suggestion.setStatus(SuggestionStatus.APPROVED);

        when(suggestionRepository.findById(suggestion.getId())).thenReturn(Optional.of(suggestion));

        assertThatThrownBy(() -> suggestionService.approve(suggestion.getId(), UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not PENDING");
    }
}
