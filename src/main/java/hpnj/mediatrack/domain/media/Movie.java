package hpnj.mediatrack.domain.media;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "movie")
@DiscriminatorValue("MOVIE")
@Getter
@Setter
public class Movie extends Media {

    /** Duration in minutes */
    private Integer duration;

    private String originalLanguage;
}
