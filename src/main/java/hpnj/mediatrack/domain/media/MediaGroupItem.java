package hpnj.mediatrack.domain.media;

import jakarta.persistence.*;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "media_id"}))
public class MediaGroupItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id")
    private MediaGroup group;

    @ManyToOne(optional = false)
    @JoinColumn(name = "media_id")
    private Media media;

    @Column(nullable = false)
    private Integer position;

    protected MediaGroupItem() {}

    public MediaGroupItem(MediaGroup group, Media media, Integer position) {
        this.group = group;
        this.media = media;
        this.position = position;
    }

    public Long getId() { return id; }
    public MediaGroup getGroup() { return group; }
    public Media getMedia() { return media; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
}
