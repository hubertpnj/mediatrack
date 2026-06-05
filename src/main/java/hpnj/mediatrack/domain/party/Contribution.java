package hpnj.mediatrack.domain.party;

import hpnj.mediatrack.domain.media.Media;
import jakarta.persistence.*;

@Entity
public class Contribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "party_id")
    private Party party;

    @ManyToOne(optional = false)
    @JoinColumn(name = "media_id")
    private Media media;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContributionRole role;

    private Integer displayOrder;

    protected Contribution() {}

    public Contribution(Party party, Media media, ContributionRole role, Integer displayOrder) {
        this.party = party;
        this.media = media;
        this.role = role;
        this.displayOrder = displayOrder;
    }

    public Long getId() { return id; }
    public Party getParty() { return party; }
    public Media getMedia() { return media; }
    public ContributionRole getRole() { return role; }
    public void setRole(ContributionRole role) { this.role = role; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
}
