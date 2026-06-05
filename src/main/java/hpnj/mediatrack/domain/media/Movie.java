package hpnj.mediatrack.domain.media;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@DiscriminatorValue("MOVIE")
public class Movie extends Media {

    private Integer durationMinutes;

    protected Movie() {}

    public Movie(String title, LocalDate releaseDate, Integer durationMinutes) {
        super(title, releaseDate);
        this.durationMinutes = durationMinutes;
    }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
}
