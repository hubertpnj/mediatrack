package hpnj.mediatrack.domain.party;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("ORGANIZATION")
public class Organization extends Party {

    @Column(nullable = false)
    private String name;

    private Integer foundedYear;

    protected Organization() {}

    public Organization(String name, Integer foundedYear) {
        this.name = name;
        this.foundedYear = foundedYear;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getFoundedYear() { return foundedYear; }
    public void setFoundedYear(Integer foundedYear) { this.foundedYear = foundedYear; }
}
