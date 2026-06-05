package hpnj.mediatrack.domain.media;

import jakarta.persistence.*;

@Entity
public class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "album_id")
    private Album album;

    @Column(nullable = false)
    private Integer trackNumber;

    @Column(nullable = false)
    private String title;

    private Integer durationSeconds;

    protected Track() {}

    public Track(Album album, Integer trackNumber, String title, Integer durationSeconds) {
        this.album = album;
        this.trackNumber = trackNumber;
        this.title = title;
        this.durationSeconds = durationSeconds;
    }

    public Long getId() { return id; }
    public Album getAlbum() { return album; }
    public Integer getTrackNumber() { return trackNumber; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }
}
