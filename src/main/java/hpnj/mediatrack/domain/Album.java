package hpnj.mediatrack.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("ALBUM")
public class Album extends Media {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlbumType albumType;

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("trackNumber ASC")
    private List<Track> tracks = new ArrayList<>();

    protected Album() {}

    public Album(String title, LocalDate releaseDate, AlbumType albumType) {
        super(title, releaseDate);
        this.albumType = albumType;
    }

    public AlbumType getAlbumType() { return albumType; }
    public void setAlbumType(AlbumType albumType) { this.albumType = albumType; }
    public List<Track> getTracks() { return tracks; }
}
