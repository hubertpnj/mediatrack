package hpnj.mediatrack.domain.media;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("GAME")
public class Game extends Media {

    @ElementCollection
    @CollectionTable(name = "game_platform", joinColumns = @JoinColumn(name = "game_id"))
    @Column(name = "platform")
    @Enumerated(EnumType.STRING)
    private Set<Platform> platforms = new HashSet<>();

    protected Game() {}

    public Game(String title, LocalDate releaseDate, Set<Platform> platforms) {
        super(title, releaseDate);
        this.platforms = platforms;
    }

    public Set<Platform> getPlatforms() { return platforms; }
}
