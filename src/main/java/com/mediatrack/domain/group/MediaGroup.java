package com.mediatrack.domain.group;

import com.mediatrack.domain.Auditable;
import com.mediatrack.domain.enums.MediaGroupType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "media_group")
@Getter
@Setter
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
}
