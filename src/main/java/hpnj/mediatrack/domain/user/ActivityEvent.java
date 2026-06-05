package hpnj.mediatrack.domain.user;

import hpnj.mediatrack.domain.media.Media;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class ActivityEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private UserAccount user;

    @Column(nullable = false)
    private String eventType;

    @ManyToOne
    @JoinColumn(name = "media_id")
    private Media media;

    @ManyToOne
    @JoinColumn(name = "entry_id")
    private LibraryEntry entry;

    private final Instant occurredAt = Instant.now();

    protected ActivityEvent() {}

    public ActivityEvent(UserAccount user, String eventType, Media media, LibraryEntry entry) {
        this.user = user;
        this.eventType = eventType;
        this.media = media;
        this.entry = entry;
    }

    public Long getId() { return id; }
    public UserAccount getUser() { return user; }
    public String getEventType() { return eventType; }
    public Media getMedia() { return media; }
    public LibraryEntry getEntry() { return entry; }
    public Instant getOccurredAt() { return occurredAt; }
}
