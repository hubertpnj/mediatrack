package hpnj.mediatrack.domain.media;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@DiscriminatorValue("MOVIE")
public class Movie extends Media {

    private Integer durationMinutes;

    @Column(name = "tmdb_id", unique = true)
    private Integer tmdbId;

    protected Movie() {}

    public Movie(String title, LocalDate releaseDate, Integer durationMinutes) {
        super(title, releaseDate);
        this.durationMinutes = durationMinutes;
    }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public Integer getTmdbId() { return tmdbId; }
    public void setTmdbId(Integer tmdbId) { this.tmdbId = tmdbId; }
}
