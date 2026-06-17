package hpnj.mediatrack.media;

import hpnj.mediatrack.domain.media.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    Optional<Movie> findByTmdbId(Integer tmdbId);
    boolean existsByTmdbIdIsNotNull();
}
