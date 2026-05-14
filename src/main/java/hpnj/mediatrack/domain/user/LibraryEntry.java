package hpnj.mediatrack.domain.user;

import hpnj.mediatrack.domain.Auditable;
import hpnj.mediatrack.domain.enums.LibraryStatus;
import hpnj.mediatrack.domain.media.Media;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "library_entry",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_library_entry_user_media",
        columnNames = {"user_id", "media_id"}
    )
)
public class LibraryEntry extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "media_id", nullable = false)
    private Media media;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private LibraryStatus status;

    @Min(1)
    @Max(10)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String review;

    private Instant startedAt;

    private Instant completedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UserAccount getUser() {
        return user;
    }

    public void setUser(UserAccount user) {
        this.user = user;
    }

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }

    public LibraryStatus getStatus() {
        return status;
    }

    public void setStatus(LibraryStatus status) {
        this.status = status;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
