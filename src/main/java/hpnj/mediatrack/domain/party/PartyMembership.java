package hpnj.mediatrack.domain.party;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "party_membership")
public class PartyMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "person_id")
    private Person person;

    @ManyToOne(optional = false)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Column(nullable = false)
    private String role;

    private LocalDate startDate;
    private LocalDate endDate;

    protected PartyMembership() {}

    public PartyMembership(Person person, Organization organization, String role, LocalDate startDate, LocalDate endDate) {
        this.person = person;
        this.organization = organization;
        this.role = role;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getId() { return id; }
    public Person getPerson() { return person; }
    public Organization getOrganization() { return organization; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
