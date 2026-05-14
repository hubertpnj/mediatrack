package com.mediatrack.domain.user;

import com.mediatrack.domain.media.Media;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "user_list_item")
@Getter
@Setter
public class UserListItem {

    @EmbeddedId
    private UserListItemId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("listId")
    @JoinColumn(name = "list_id")
    private UserList list;

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
