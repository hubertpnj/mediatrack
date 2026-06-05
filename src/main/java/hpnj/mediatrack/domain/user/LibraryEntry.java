package hpnj.mediatrack.domain.user;

import hpnj.mediatrack.domain.media.Media;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class LibraryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private UserAccount user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "media_id")
    private Media media;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LibraryStatus status = LibraryStatus.PLANNED;

    private boolean completed = false;

    @Column(nullable = false, updatable = false)
    private final Instant addedAt = Instant.now();

    private Instant updatedAt = Instant.now();

    protected LibraryEntry() {}

    public LibraryEntry(UserAccount user, Media media) {
        this.user = user;
        this.media = media;
    }

    public Long getId() { return id; }
    public UserAccount getUser() { return user; }
    public Media getMedia() { return media; }

    public LibraryStatus getStatus() { return status; }
    public void setStatus(LibraryStatus status) { this.status = status; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public Instant getAddedAt() { return addedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
