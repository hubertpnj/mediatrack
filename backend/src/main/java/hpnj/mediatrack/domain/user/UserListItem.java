package hpnj.mediatrack.domain.user;

import hpnj.mediatrack.domain.media.Media;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "user_list_item")
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

    public UserListItemId getId() {
        return id;
    }

    public void setId(UserListItemId id) {
        this.id = id;
    }

    public UserList getList() {
        return list;
    }

    public void setList(UserList list) {
        this.list = list;
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
