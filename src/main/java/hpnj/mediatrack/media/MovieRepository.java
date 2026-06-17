package hpnj.mediatrack.media;

import hpnj.mediatrack.domain.media.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {
}
