package hpnj.mediatrack.domain.media;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "media_external_id",
       uniqueConstraints = @UniqueConstraint(name = "uq_source_external_id", columnNames = {"source", "external_id"}))
public class MediaExternalId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id", nullable = false)
    private Media media;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ExternalIdSource source;

    @Column(name = "external_id", nullable = false, length = 255)
    private String externalId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected MediaExternalId() {}

    public MediaExternalId(Media media, ExternalIdSource source, String externalId) {
        this.media = media;
        this.source = source;
        this.externalId = externalId;
    }

    public Long getId() { return id; }
    public Media getMedia() { return media; }
    public ExternalIdSource getSource() { return source; }
    public String getExternalId() { return externalId; }
    public Instant getCreatedAt() { return createdAt; }
}
