package com.mediatrack.domain;

import com.mediatrack.domain.enums.ContributionRole;
import com.mediatrack.domain.media.Media;
import com.mediatrack.domain.party.Party;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "contribution")
@Getter
@Setter
public class Contribution extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "party_id", nullable = false)
    private Party party;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "media_id", nullable = false)
    private Media media;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContributionRole role;

    @Column(nullable = false)
    private int creditOrder = 0;
}
