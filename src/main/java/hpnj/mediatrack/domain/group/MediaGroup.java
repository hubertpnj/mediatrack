package hpnj.mediatrack.domain.group;

import hpnj.mediatrack.domain.Auditable;
import hpnj.mediatrack.domain.enums.MediaGroupType;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "media_group")
public class MediaGroup extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MediaGroupType groupType;

    @Column(columnDefinition = "TEXT")
    private String description;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MediaGroupType getGroupType() {
        return groupType;
    }

    public void setGroupType(MediaGroupType groupType) {
        this.groupType = groupType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
