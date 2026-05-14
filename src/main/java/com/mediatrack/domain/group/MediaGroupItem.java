package com.mediatrack.domain.group;

import com.mediatrack.domain.media.Media;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "media_group_item")
@Getter
@Setter
public class MediaGroupItem {

    @EmbeddedId
    private MediaGroupItemId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("groupId")
    @JoinColumn(name = "group_id")
    private MediaGroup group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("mediaId")
    @JoinColumn(name = "media_id")
    private Media media;

    @Column(nullable = false)
    private int sortOrder = 0;

    @Column(nullable = false)
    private Instant addedAt;

    @PrePersist
    private void prePersist() {
        if (addedAt == null) {
            addedAt = Instant.now();
        }
    }
}
