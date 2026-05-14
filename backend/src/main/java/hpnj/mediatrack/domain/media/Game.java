package hpnj.mediatrack.domain.media;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "game")
@DiscriminatorValue("GAME")
public class Game extends Media {

    @ElementCollection
    @CollectionTable(name = "game_platform", joinColumns = @JoinColumn(name = "game_id"))
    @Column(name = "platform", nullable = false)
    private List<String> platforms = new ArrayList<>();

    private String esrbRating;

    public List<String> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(List<String> platforms) {
        this.platforms = platforms;
    }

    public String getEsrbRating() {
        return esrbRating;
    }

    public void setEsrbRating(String esrbRating) {
        this.esrbRating = esrbRating;
    }
}
