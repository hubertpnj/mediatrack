package hpnj.mediatrack.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Season {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "show_id")
    private TVShow show;

    @Column(nullable = false)
    private Integer seasonNumber;

    private String title;
    private LocalDate releaseDate;

    @OneToMany(mappedBy = "season", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("episodeNumber ASC")
    private List<Episode> episodes = new ArrayList<>();

    protected Season() {}

    public Season(TVShow show, Integer seasonNumber, String title, LocalDate releaseDate) {
        this.show = show;
        this.seasonNumber = seasonNumber;
        this.title = title;
        this.releaseDate = releaseDate;
    }

    public Long getId() { return id; }
    public TVShow getShow() { return show; }
    public Integer getSeasonNumber() { return seasonNumber; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }
    public List<Episode> getEpisodes() { return episodes; }
}
