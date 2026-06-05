package hpnj.mediatrack.domain.user;

import hpnj.mediatrack.domain.media.Media;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class UserListItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "list_id")
    private UserList list;

    @ManyToOne(optional = false)
    @JoinColumn(name = "media_id")
    private Media media;

    @Column(nullable = false)
    private Integer position;

    private final Instant addedAt = Instant.now();

    protected UserListItem() {}

    public UserListItem(UserList list, Media media, Integer position) {
        this.list = list;
        this.media = media;
        this.position = position;
    }

    public Long getId() { return id; }
    public UserList getList() { return list; }
    public Media getMedia() { return media; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
    public Instant getAddedAt() { return addedAt; }
}
