package hpnj.mediatrack.domain.media;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("TV_SHOW")
public class TVShow extends Media {

    @OneToMany(mappedBy = "show", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("seasonNumber ASC")
    private List<Season> seasons = new ArrayList<>();

    protected TVShow() {}

    public TVShow(String title, LocalDate releaseDate) {
        super(title, releaseDate);
    }

    public List<Season> getSeasons() { return seasons; }
}
