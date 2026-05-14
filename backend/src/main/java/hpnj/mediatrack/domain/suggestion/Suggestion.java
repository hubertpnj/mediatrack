package hpnj.mediatrack.domain.suggestion;

import hpnj.mediatrack.domain.Auditable;
import hpnj.mediatrack.domain.enums.SuggestionEntityType;
import hpnj.mediatrack.domain.enums.SuggestionStatus;
import hpnj.mediatrack.domain.user.UserAccount;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "suggestion")
public class Suggestion extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 20)
    private SuggestionEntityType entityType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "proposed_data", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> proposedData;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SuggestionStatus status = SuggestionStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by", nullable = false)
    private UserAccount submittedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private UserAccount reviewedBy;

    @Column(name = "review_note", columnDefinition = "TEXT")
    private String reviewNote;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public SuggestionEntityType getEntityType() { return entityType; }
    public void setEntityType(SuggestionEntityType entityType) { this.entityType = entityType; }

    public Map<String, Object> getProposedData() { return proposedData; }
    public void setProposedData(Map<String, Object> proposedData) { this.proposedData = proposedData; }

    public SuggestionStatus getStatus() { return status; }
    public void setStatus(SuggestionStatus status) { this.status = status; }

    public UserAccount getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(UserAccount submittedBy) { this.submittedBy = submittedBy; }

    public UserAccount getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(UserAccount reviewedBy) { this.reviewedBy = reviewedBy; }

    public String getReviewNote() { return reviewNote; }
    public void setReviewNote(String reviewNote) { this.reviewNote = reviewNote; }
}
