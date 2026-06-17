package hpnj.mediatrack.domain.media;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tv_show")
@DiscriminatorValue("TV_SHOW")
public class TVShow extends Media {

    @Column(name = "tmdb_id", unique = true)
    private Integer tmdbId;

    @OneToMany(mappedBy = "show", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("seasonNumber ASC")
    private List<Season> seasons = new ArrayList<>();

    protected TVShow() {}

    public TVShow(String title, LocalDate releaseDate) {
        super(title, releaseDate);
    }

    public Integer getTmdbId() { return tmdbId; }
    public void setTmdbId(Integer tmdbId) { this.tmdbId = tmdbId; }

    public List<Season> getSeasons() { return seasons; }
}
