package hpnj.mediatrack.domain.party;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "organization")
@DiscriminatorValue("ORGANIZATION")
@Getter
@Setter
public class Organization extends Party {

    private Integer foundedYear;

    private String country;

    private String website;
}
