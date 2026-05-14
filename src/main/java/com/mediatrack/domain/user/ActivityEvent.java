package com.mediatrack.domain.user;

import com.mediatrack.domain.media.Media;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Persisted by the Kafka consumer – does not extend Auditable,
 * only carries the event timestamp from the message itself.
 */
@Entity
@Table(name = "activity_event")
@Getter
@Setter
public class ActivityEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id")
    private Media media;

    @Column(nullable = false, length = 50)
    private String eventType;

    @Column(columnDefinition = "jsonb")
    private String payload;

    @Column(nullable = false)
    private Instant occurredAt;
}
