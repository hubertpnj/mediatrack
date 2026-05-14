package hpnj.mediatrack.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class UserListItemId implements Serializable {

    @Column(name = "list_id")
    private UUID listId;

    @Column(name = "media_id")
    private UUID mediaId;

    public UserListItemId() {
    }

    public UserListItemId(UUID listId, UUID mediaId) {
        this.listId = listId;
        this.mediaId = mediaId;
    }

    public UUID getListId() {
        return listId;
    }

    public UUID getMediaId() {
        return mediaId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserListItemId that)) return false;
        return Objects.equals(listId, that.listId) && Objects.equals(mediaId, that.mediaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(listId, mediaId);
    }
}
