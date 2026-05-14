package hpnj.mediatrack.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserListItemId implements Serializable {

    @Column(name = "list_id")
    private UUID listId;

    @Column(name = "media_id")
    private UUID mediaId;
}
