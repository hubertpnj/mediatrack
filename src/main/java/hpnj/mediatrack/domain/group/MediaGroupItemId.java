package hpnj.mediatrack.domain.group;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class MediaGroupItemId implements Serializable {

    @Column(name = "group_id")
    private UUID groupId;

    @Column(name = "media_id")
    private UUID mediaId;

    public MediaGroupItemId() {
    }

    public MediaGroupItemId(UUID groupId, UUID mediaId) {
        this.groupId = groupId;
        this.mediaId = mediaId;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public UUID getMediaId() {
        return mediaId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MediaGroupItemId that)) return false;
        return Objects.equals(groupId, that.groupId) && Objects.equals(mediaId, that.mediaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, mediaId);
    }
}
