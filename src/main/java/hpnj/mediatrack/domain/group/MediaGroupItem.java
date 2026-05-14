package hpnj.mediatrack.domain.group;

import hpnj.mediatrack.domain.media.Media;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "media_group_item")
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

    public MediaGroupItemId getId() {
        return id;
    }

    public void setId(MediaGroupItemId id) {
        this.id = id;
    }

    public MediaGroup getGroup() {
        return group;
    }

    public void setGroup(MediaGroup group) {
        this.group = group;
    }

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Instant getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Instant addedAt) {
        this.addedAt = addedAt;
    }
}
