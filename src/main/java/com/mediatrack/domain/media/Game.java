package com.mediatrack.domain.media;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "game")
@DiscriminatorValue("GAME")
@Getter
@Setter
public class Game extends Media {

    @ElementCollection
    @CollectionTable(name = "game_platform", joinColumns = @JoinColumn(name = "game_id"))
    @Column(name = "platform", nullable = false)
    private List<String> platforms = new ArrayList<>();

    private String esrbRating;
}
