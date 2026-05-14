package hpnj.mediatrack.domain.user;

import hpnj.mediatrack.domain.Auditable;
import hpnj.mediatrack.domain.enums.LibraryStatus;
import hpnj.mediatrack.domain.media.Media;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "library_entry",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_library_entry_user_media",
        columnNames = {"user_id", "media_id"}
    )
)
@Getter
@Setter
public class LibraryEntry extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "media_id", nullable = false)
    private Media media;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private LibraryStatus status;

    @Min(1)
    @Max(10)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String review;

    private Instant startedAt;

    private Instant completedAt;
}
