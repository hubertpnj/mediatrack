package hpnj.mediatrack.service;

import hpnj.mediatrack.domain.enums.MediaGroupType;
import hpnj.mediatrack.domain.enums.SuggestionEntityType;
import hpnj.mediatrack.domain.enums.SuggestionStatus;
import hpnj.mediatrack.domain.group.MediaGroup;
import hpnj.mediatrack.domain.media.*;
import hpnj.mediatrack.domain.party.Organization;
import hpnj.mediatrack.domain.party.Person;
import hpnj.mediatrack.domain.suggestion.Suggestion;
import hpnj.mediatrack.domain.user.UserAccount;
import hpnj.mediatrack.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class SuggestionService {

    private final SuggestionRepository suggestionRepository;
    private final MediaRepository mediaRepository;
    private final PartyRepository partyRepository;
    private final MediaGroupRepository mediaGroupRepository;
    private final UserAccountRepository userAccountRepository;

    public SuggestionService(SuggestionRepository suggestionRepository,
                              MediaRepository mediaRepository,
                              PartyRepository partyRepository,
                              MediaGroupRepository mediaGroupRepository,
                              UserAccountRepository userAccountRepository) {
        this.suggestionRepository = suggestionRepository;
        this.mediaRepository = mediaRepository;
        this.partyRepository = partyRepository;
        this.mediaGroupRepository = mediaGroupRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional
    public Suggestion submit(UUID submitterId, SuggestionEntityType entityType, Map<String, Object> proposedData) {
        UserAccount submitter = userAccountRepository.findById(submitterId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + submitterId));
        Suggestion suggestion = new Suggestion();
        suggestion.setSubmittedBy(submitter);
        suggestion.setEntityType(entityType);
        suggestion.setProposedData(proposedData);
        suggestion.setStatus(SuggestionStatus.PENDING);
        return suggestionRepository.save(suggestion);
    }

    @Transactional(readOnly = true)
    public List<Suggestion> list(SuggestionStatus status) {
        return status == null ? suggestionRepository.findAll() : suggestionRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public Optional<Suggestion> findById(UUID id) {
        return suggestionRepository.findById(id);
    }

    @Transactional
    public void approve(UUID suggestionId, UUID reviewerId) {
        Suggestion suggestion = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> new IllegalArgumentException("Suggestion not found: " + suggestionId));
        if (suggestion.getStatus() != SuggestionStatus.PENDING) {
            throw new IllegalStateException("Suggestion is not PENDING: " + suggestion.getStatus());
        }
        UserAccount reviewer = userAccountRepository.findById(reviewerId)
                .orElseThrow(() -> new IllegalArgumentException("Reviewer not found: " + reviewerId));
        materialize(suggestion);
        suggestion.setStatus(SuggestionStatus.APPROVED);
        suggestion.setReviewedBy(reviewer);
        suggestionRepository.save(suggestion);
    }

    @Transactional
    public void reject(UUID suggestionId, UUID reviewerId, String note) {
        Suggestion suggestion = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> new IllegalArgumentException("Suggestion not found: " + suggestionId));
        if (suggestion.getStatus() != SuggestionStatus.PENDING) {
            throw new IllegalStateException("Suggestion is not PENDING: " + suggestion.getStatus());
        }
        UserAccount reviewer = userAccountRepository.findById(reviewerId)
                .orElseThrow(() -> new IllegalArgumentException("Reviewer not found: " + reviewerId));
        suggestion.setStatus(SuggestionStatus.REJECTED);
        suggestion.setReviewedBy(reviewer);
        suggestion.setReviewNote(note);
        suggestionRepository.save(suggestion);
    }

    private void materialize(Suggestion suggestion) {
        Map<String, Object> data = suggestion.getProposedData();
        switch (suggestion.getEntityType()) {
            case MOVIE -> {
                Movie e = new Movie();
                e.setTitle((String) data.get("title"));
                e.setReleaseYear(toInt(data.get("releaseYear")));
                mediaRepository.save(e);
            }
            case GAME -> {
                Game e = new Game();
                e.setTitle((String) data.get("title"));
                e.setReleaseYear(toInt(data.get("releaseYear")));
                mediaRepository.save(e);
            }
            case BOOK -> {
                Book e = new Book();
                e.setTitle((String) data.get("title"));
                e.setReleaseYear(toInt(data.get("releaseYear")));
                mediaRepository.save(e);
            }
            case ALBUM -> {
                Album e = new Album();
                e.setTitle((String) data.get("title"));
                e.setReleaseYear(toInt(data.get("releaseYear")));
                mediaRepository.save(e);
            }
            case PERSON -> {
                Person e = new Person();
                e.setName((String) data.get("name"));
                partyRepository.save(e);
            }
            case ORGANIZATION -> {
                Organization e = new Organization();
                e.setName((String) data.get("name"));
                partyRepository.save(e);
            }
            case MEDIA_GROUP -> {
                MediaGroup e = new MediaGroup();
                e.setName((String) data.get("name"));
                e.setGroupType(MediaGroupType.valueOf((String) data.get("groupType")));
                mediaGroupRepository.save(e);
            }
        }
    }

    private Integer toInt(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.intValue();
        return Integer.parseInt(value.toString());
    }
}
