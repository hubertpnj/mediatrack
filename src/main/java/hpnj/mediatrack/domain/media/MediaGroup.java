package hpnj.mediatrack.domain.media;

import jakarta.persistence.*;

@Entity
public class MediaGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaGroupType groupType;

    private String description;

    protected MediaGroup() {}

    public MediaGroup(String name, MediaGroupType groupType, String description) {
        this.name = name;
        this.groupType = groupType;
        this.description = description;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public MediaGroupType getGroupType() { return groupType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
