package hpnj.mediatrack.domain;

import hpnj.mediatrack.domain.enums.ContributionRole;
import hpnj.mediatrack.domain.media.Media;
import hpnj.mediatrack.domain.party.Party;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "contribution")
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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Party getParty() {
        return party;
    }

    public void setParty(Party party) {
        this.party = party;
    }

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }

    public ContributionRole getRole() {
        return role;
    }

    public void setRole(ContributionRole role) {
        this.role = role;
    }

    public int getCreditOrder() {
        return creditOrder;
    }

    public void setCreditOrder(int creditOrder) {
        this.creditOrder = creditOrder;
    }
}
