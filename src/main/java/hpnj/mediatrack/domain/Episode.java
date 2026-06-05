package hpnj.mediatrack.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Episode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "season_id")
    private Season season;

    @Column(nullable = false)
    private Integer episodeNumber;

    @Column(nullable = false)
    private String title;

    private Integer durationMinutes;
    private LocalDate releaseDate;

    protected Episode() {}

    public Episode(Season season, Integer episodeNumber, String title, Integer durationMinutes, LocalDate releaseDate) {
        this.season = season;
        this.episodeNumber = episodeNumber;
        this.title = title;
        this.durationMinutes = durationMinutes;
        this.releaseDate = releaseDate;
    }

    public Long getId() { return id; }
    public Season getSeason() { return season; }
    public Integer getEpisodeNumber() { return episodeNumber; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }
}
