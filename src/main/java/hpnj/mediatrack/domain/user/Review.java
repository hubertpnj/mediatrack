package hpnj.mediatrack.domain.user;

import hpnj.mediatrack.domain.media.Media;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private UserAccount user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "media_id")
    private Media media;

    @Column(precision = 3, scale = 1)
    private BigDecimal rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(nullable = false, updatable = false)
    private final Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();

    protected Review() {}

    public Review(UserAccount user, Media media, BigDecimal rating, String comment) {
        this.user = user;
        this.media = media;
        this.rating = rating;
        this.comment = comment;
    }

    public Long getId() { return id; }
    public UserAccount getUser() { return user; }
    public Media getMedia() { return media; }

    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
